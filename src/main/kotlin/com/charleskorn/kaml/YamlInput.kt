/*

   Copyright 2018-2019 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package com.charleskorn.kaml

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
sealed class YamlInput(val node: YamlNode, override var serializersModule: SerializersModule, val configuration: YamlConfiguration) : AbstractDecoder() {
    companion object {
        private val unknownPolymorphicTypeExceptionMessage: Regex = """^Class '(.*?)' is not registered for polymorphic serialization in the scope of '(.*?)'.\nMark the base class as 'sealed' or register the serializer explicitly.$""".toRegex()
        private val missingFieldExceptionMessage: Regex = """^Field '(.*)' is required, but it was missing$""".toRegex()

        internal fun createFor(node: YamlNode, context: SerializersModule, configuration: YamlConfiguration, descriptor: SerialDescriptor): YamlInput = when (node) {
            is YamlNull -> when {
                descriptor.kind is PolymorphicKind && !descriptor.isNullable -> throw MissingTypeTagException(node.location)
                else -> YamlNullInput(node, context, configuration)
            }

            is YamlScalar -> when (descriptor.kind) {
                is PrimitiveKind, SerialKind.ENUM -> YamlScalarInput(node, context, configuration)
                is SerialKind.CONTEXTUAL -> YamlContextualInput(node, context, configuration)
                is PolymorphicKind -> throw MissingTypeTagException(node.location)
                else -> throw IncorrectTypeException("Expected ${descriptor.kind.friendlyDescription}, but got a scalar value", node.location)
            }

            is YamlList -> when (descriptor.kind) {
                is StructureKind.LIST -> YamlListInput(node, context, configuration)
                is SerialKind.CONTEXTUAL -> YamlContextualInput(node, context, configuration)
                else -> throw IncorrectTypeException("Expected ${descriptor.kind.friendlyDescription}, but got a list", node.location)
            }

            is YamlMap -> when (descriptor.kind) {
                is StructureKind.CLASS, StructureKind.OBJECT -> YamlObjectInput(node, context, configuration)
                is StructureKind.MAP -> YamlMapInput(node, context, configuration)
                is SerialKind.CONTEXTUAL -> YamlContextualInput(node, context, configuration)
                is PolymorphicKind -> when (configuration.polymorphismStyle) {
                    PolymorphismStyle.Tag -> throw MissingTypeTagException(node.location)
                    PolymorphismStyle.Property -> createPolymorphicMapDeserializer(node, context, configuration)
                }
                else -> throw IncorrectTypeException("Expected ${descriptor.kind.friendlyDescription}, but got a map", node.location)
            }

            is YamlTaggedNode -> when {
                descriptor.kind is PolymorphicKind && configuration.polymorphismStyle == PolymorphismStyle.Tag -> YamlPolymorphicInput(node.tag, node.innerNode, context, configuration)
                else -> createFor(node.innerNode, context, configuration, descriptor)
            }
        }

        private fun createPolymorphicMapDeserializer(node: YamlMap, context: SerializersModule, configuration: YamlConfiguration): YamlPolymorphicInput {
            when (val typeName = node.getValue("type")) {
                is YamlList -> throw InvalidPropertyValueException("type", "expected a string, but got a list", typeName.location)
                is YamlMap -> throw InvalidPropertyValueException("type", "expected a string, but got a map", typeName.location)
                is YamlNull -> throw InvalidPropertyValueException("type", "expected a string, but got a null value", typeName.location)
                is YamlTaggedNode -> throw InvalidPropertyValueException("type", "expected a string, but got a tagged value", typeName.location)
                is YamlScalar -> {
                    val remainingProperties = node.withoutKey("type")

                    return YamlPolymorphicInput(typeName.content, remainingProperties, context, configuration)
                }
            }
        }

        private fun YamlMap.getValue(desiredKey: String): YamlNode {
            this.entries.forEach { (keyNode, valueNode) ->
                if (keyNode is YamlScalar && keyNode.content == desiredKey) {
                    return valueNode
                }
            }

            throw MissingRequiredPropertyException(desiredKey, this.location)
        }

        private fun YamlMap.withoutKey(key: String): YamlMap {
            return this.copy(entries = entries.filterKeys { !(it is YamlScalar && it.content == key) })
        }
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        try {
            return super.decodeSerializableValue(deserializer)
        } catch (e: SerializationException) {
            throwMissingRequiredPropertyException(e)
            throwIfUnknownPolymorphicTypeException(e, deserializer)

            throw e
        }
    }

    private fun throwMissingRequiredPropertyException(e: SerializationException) {
        val match = missingFieldExceptionMessage.matchEntire(e.message!!) ?: return

        throw MissingRequiredPropertyException(match.groupValues[1], node.location, e)
    }

    private fun throwIfUnknownPolymorphicTypeException(e: Exception, deserializer: DeserializationStrategy<*>) {
        val message = e.message ?: return
        val match = unknownPolymorphicTypeExceptionMessage.matchEntire(message) ?: return
        val unknownType = match.groupValues[1]
        val className = match.groupValues[2]

        val knownTypes = when (deserializer.descriptor.kind) {
            PolymorphicKind.SEALED -> getKnownTypesForSealedType(deserializer)
            PolymorphicKind.OPEN -> getKnownTypesForOpenType(className)
            else -> throw IllegalArgumentException("Can't get known types for descriptor of kind ${deserializer.descriptor.kind}")
        }

        throw UnknownPolymorphicTypeException(unknownType, knownTypes, getCurrentLocation(), e)
    }

    private fun getKnownTypesForSealedType(deserializer: DeserializationStrategy<*>): Set<String> {
        val typesDescriptor = deserializer.descriptor.getElementDescriptor(1)

        return typesDescriptor.elementNames.toSet()
    }

    private fun getKnownTypesForOpenType(className: String): Set<String> {
        val knownTypes = mutableSetOf<String>()

        serializersModule.dumpTo(object : SerializersModuleCollector {
            override fun <T : Any> contextual(kClass: KClass<T>, serializer: KSerializer<T>) {}

            // FIXME: ideally we'd be able to get the name as used by the SerialModule (eg. the values in 'polyBase2NamedSerializers' in SerialModuleImpl, but these aren't exposed.
            // The serializer's descriptor's name seems to be the same value.
            override fun <Base : Any, Sub : Base> polymorphic(baseClass: KClass<Base>, actualClass: KClass<Sub>, actualSerializer: KSerializer<Sub>) {
                if (baseClass.simpleName == className) {
                    knownTypes.add(actualSerializer.descriptor.serialName)
                }
            }

            override fun <Base : Any> polymorphicDefault(baseClass: KClass<Base>, defaultSerializerProvider: (className: String?) -> DeserializationStrategy<out Base>?) {
                // not needed
            }
        })

        return knownTypes
    }

    abstract fun getCurrentLocation(): Location
}

@OptIn(ExperimentalSerializationApi::class)
private class YamlScalarInput(val scalar: YamlScalar, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(scalar, context, configuration) {
    override fun decodeString(): String = scalar.content
    override fun decodeInt(): Int = scalar.toInt()
    override fun decodeLong(): Long = scalar.toLong()
    override fun decodeShort(): Short = scalar.toShort()
    override fun decodeByte(): Byte = scalar.toByte()
    override fun decodeDouble(): Double = scalar.toDouble()
    override fun decodeFloat(): Float = scalar.toFloat()
    override fun decodeBoolean(): Boolean = scalar.toBoolean()
    override fun decodeChar(): Char = scalar.toChar()

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val index = enumDescriptor.getElementIndex(scalar.content)

        if (index != UNKNOWN_NAME) {
            return index
        }

        val choices = enumDescriptor.elementNames
            .sorted()
            .joinToString(", ")

        throw YamlScalarFormatException("Value ${scalar.contentToString()} is not a valid option, permitted choices are: $choices", scalar.location, scalar.content)
    }

    override fun getCurrentLocation(): Location = scalar.location

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = 0
}

private class YamlNullInput(val nullValue: YamlNode, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(nullValue, context, configuration) {
    override fun decodeNotNullMark(): Boolean = false

    override fun decodeValue(): Any = throw UnexpectedNullValueException(nullValue.location)
    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = throw UnexpectedNullValueException(nullValue.location)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = throw UnexpectedNullValueException(nullValue.location)

    override fun getCurrentLocation(): Location = nullValue.location

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = 0
}

@OptIn(ExperimentalSerializationApi::class)
private class YamlListInput(val list: YamlList, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(list, context, configuration) {
    private var nextElementIndex = 0
    private lateinit var currentElementDecoder: YamlInput

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = list.items.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (nextElementIndex == list.items.size) {
            return DECODE_DONE
        }

        currentElementDecoder = createFor(list.items[nextElementIndex], serializersModule, configuration, descriptor.getElementDescriptor(0))

        return nextElementIndex++
    }

    override fun decodeNotNullMark(): Boolean {
        if (!haveStartedReadingElements) {
            return true
        }

        return currentElementDecoder.decodeNotNullMark()
    }

    override fun decodeString(): String = currentElementDecoder.decodeString()
    override fun decodeInt(): Int = currentElementDecoder.decodeInt()
    override fun decodeLong(): Long = currentElementDecoder.decodeLong()
    override fun decodeShort(): Short = currentElementDecoder.decodeShort()
    override fun decodeByte(): Byte = currentElementDecoder.decodeByte()
    override fun decodeDouble(): Double = currentElementDecoder.decodeDouble()
    override fun decodeFloat(): Float = currentElementDecoder.decodeFloat()
    override fun decodeBoolean(): Boolean = currentElementDecoder.decodeBoolean()
    override fun decodeChar(): Char = currentElementDecoder.decodeChar()
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = currentElementDecoder.decodeEnum(enumDescriptor)

    private val haveStartedReadingElements: Boolean
        get() = nextElementIndex > 0

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (haveStartedReadingElements) {
            return currentElementDecoder
        }

        return super.beginStructure(descriptor)
    }

    override fun getCurrentLocation(): Location {
        return if (haveStartedReadingElements) {
            currentElementDecoder.node.location
        } else {
            list.location
        }
    }
}

private class YamlContextualInput(node: YamlNode, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(node, context, configuration) {
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = throw IllegalStateException("Must call beginStructure() and use returned Decoder")
    override fun decodeValue(): Any = throw IllegalStateException("Must call beginStructure() and use returned Decoder")

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        createFor(node, serializersModule, configuration, descriptor)

    override fun getCurrentLocation(): Location = node.location
}

private sealed class YamlMapLikeInputBase(map: YamlMap, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(map, context, configuration) {
    protected lateinit var currentValueDecoder: YamlInput
    protected lateinit var currentKey: YamlNode
    protected var currentlyReadingValue = false

    override fun decodeNotNullMark(): Boolean {
        if (!haveStartedReadingEntries) {
            return true
        }

        return fromCurrentValue { decodeNotNullMark() }
    }

    override fun decodeString(): String = fromCurrentValue { decodeString() }
    override fun decodeInt(): Int = fromCurrentValue { decodeInt() }
    override fun decodeLong(): Long = fromCurrentValue { decodeLong() }
    override fun decodeShort(): Short = fromCurrentValue { decodeShort() }
    override fun decodeByte(): Byte = fromCurrentValue { decodeByte() }
    override fun decodeDouble(): Double = fromCurrentValue { decodeDouble() }
    override fun decodeFloat(): Float = fromCurrentValue { decodeFloat() }
    override fun decodeBoolean(): Boolean = fromCurrentValue { decodeBoolean() }
    override fun decodeChar(): Char = fromCurrentValue { decodeChar() }
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = fromCurrentValue { decodeEnum(enumDescriptor) }

    protected fun <T> fromCurrentValue(action: YamlInput.() -> T): T {
        try {
            return action(currentValueDecoder)
        } catch (e: YamlException) {
            if (currentlyReadingValue) {
                throw InvalidPropertyValueException(getPropertyName(currentKey), e.message, e.location, e)
            } else {
                throw e
            }
        }
    }

    protected fun getPropertyName(key: YamlNode): String = when (key) {
        is YamlScalar -> key.content
        is YamlNull, is YamlMap, is YamlList, is YamlTaggedNode -> throw MalformedYamlException("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)", key.location)
    }

    protected val haveStartedReadingEntries: Boolean
        get() = this::currentValueDecoder.isInitialized

    override fun getCurrentLocation(): Location {
        return if (haveStartedReadingEntries) {
            currentValueDecoder.node.location
        } else {
            node.location
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class YamlMapInput(map: YamlMap, context: SerializersModule, configuration: YamlConfiguration) : YamlMapLikeInputBase(map, context, configuration) {
    private val entriesList = map.entries.entries.toList()
    private var nextIndex = 0
    private lateinit var currentEntry: Map.Entry<YamlNode, YamlNode>

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (nextIndex == entriesList.size * 2) {
            return DECODE_DONE
        }

        val entryIndex = nextIndex / 2
        currentEntry = entriesList[entryIndex]
        currentKey = currentEntry.key
        currentlyReadingValue = nextIndex % 2 != 0

        currentValueDecoder = when (currentlyReadingValue) {
            true -> try {
                createFor(currentEntry.value, serializersModule, configuration, descriptor.getElementDescriptor(1))
            } catch (e: IncorrectTypeException) {
                throw InvalidPropertyValueException(getPropertyName(currentKey), e.message, e.location, e)
            }

            false -> createFor(currentKey, serializersModule, configuration, descriptor.getElementDescriptor(0))
        }

        return nextIndex++
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (haveStartedReadingEntries) {
            return fromCurrentValue { beginStructure(descriptor) }
        }

        return super.beginStructure(descriptor)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class YamlObjectInput(map: YamlMap, context: SerializersModule, configuration: YamlConfiguration) : YamlMapLikeInputBase(map, context, configuration) {
    private val entriesList = map.entries.entries.toList()
    private var nextIndex = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (true) {
            if (nextIndex == entriesList.size) {
                return DECODE_DONE
            }

            val currentEntry = entriesList[nextIndex]
            currentKey = currentEntry.key
            val name = getPropertyName(currentKey)
            val fieldDescriptorIndex = descriptor.getElementIndex(name)

            if (fieldDescriptorIndex == UNKNOWN_NAME) {
                if (configuration.strictMode) {
                    throwUnknownProperty(name, currentKey.location, descriptor)
                } else {
                    nextIndex++
                    continue
                }
            }

            try {
                currentValueDecoder = createFor(entriesList[nextIndex].value, serializersModule, configuration, descriptor.getElementDescriptor(fieldDescriptorIndex))
            } catch (e: IncorrectTypeException) {
                throw InvalidPropertyValueException(getPropertyName(currentKey), e.message, e.location, e)
            }

            currentlyReadingValue = true
            nextIndex++

            return fieldDescriptorIndex
        }
    }

    private fun throwUnknownProperty(name: String, location: Location, desc: SerialDescriptor): Nothing {
        val knownPropertyNames = desc.elementNames.toSet()

        throw UnknownPropertyException(name, knownPropertyNames, location)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (haveStartedReadingEntries) {
            return fromCurrentValue { beginStructure(descriptor) }
        }

        return super.beginStructure(descriptor)
    }
}

private class YamlPolymorphicInput(private val typeName: String, private val contentNode: YamlNode, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(contentNode, context, configuration) {
    private var currentField = CurrentField.NotStarted
    private lateinit var contentDecoder: YamlInput

    override fun getCurrentLocation(): Location = contentNode.location

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return when (currentField) {
            CurrentField.NotStarted -> {
                currentField = CurrentField.Type
                0
            }
            CurrentField.Type -> {
                when (contentNode) {
                    is YamlScalar -> contentDecoder = YamlScalarInput(contentNode, serializersModule, configuration)
                    is YamlNull -> contentDecoder = YamlNullInput(contentNode, serializersModule, configuration)
                }

                currentField = CurrentField.Content
                1
            }
            CurrentField.Content -> DECODE_DONE
        }
    }

    override fun decodeNotNullMark(): Boolean = maybeCallOnContent(blockOnType = { true }, blockOnContent = YamlInput::decodeNotNullMark)
    override fun decodeNull(): Nothing? = maybeCallOnContent("decodeNull", blockOnContent = YamlInput::decodeNull)
    override fun decodeBoolean(): Boolean = maybeCallOnContent("decodeBoolean", blockOnContent = YamlInput::decodeBoolean)
    override fun decodeByte(): Byte = maybeCallOnContent("decodeByte", blockOnContent = YamlInput::decodeByte)
    override fun decodeShort(): Short = maybeCallOnContent("decodeShort", blockOnContent = YamlInput::decodeShort)
    override fun decodeInt(): Int = maybeCallOnContent("decodeInt", blockOnContent = YamlInput::decodeInt)
    override fun decodeLong(): Long = maybeCallOnContent("decodeLong", blockOnContent = YamlInput::decodeLong)
    override fun decodeFloat(): Float = maybeCallOnContent("decodeFloat", blockOnContent = YamlInput::decodeFloat)
    override fun decodeDouble(): Double = maybeCallOnContent("decodeDouble", blockOnContent = YamlInput::decodeDouble)
    override fun decodeChar(): Char = maybeCallOnContent("decodeChar", blockOnContent = YamlInput::decodeChar)
    override fun decodeString(): String = maybeCallOnContent(blockOnType = { typeName }, blockOnContent = YamlInput::decodeString)
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = maybeCallOnContent("decodeEnum") { decodeEnum(enumDescriptor) }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (currentField) {
            CurrentField.NotStarted, CurrentField.Type -> super.beginStructure(descriptor)
            CurrentField.Content -> {
                contentDecoder = createFor(contentNode, serializersModule, configuration, descriptor)

                return contentDecoder
            }
        }
    }

    private inline fun <T> maybeCallOnContent(functionName: String, blockOnContent: YamlInput.() -> T): T =
        maybeCallOnContent(blockOnType = { throw UnsupportedOperationException("Can't call $functionName() on type field") }, blockOnContent = blockOnContent)

    private inline fun <T> maybeCallOnContent(blockOnType: () -> T, blockOnContent: YamlInput.() -> T): T {
        return when (currentField) {
            CurrentField.NotStarted, CurrentField.Type -> blockOnType()
            CurrentField.Content -> contentDecoder.blockOnContent()
        }
    }

    private enum class CurrentField {
        NotStarted,
        Type,
        Content
    }
}

@OptIn(ExperimentalSerializationApi::class)
private val SerialKind.friendlyDescription: String
    get() {
        return when (this) {
            is StructureKind.MAP -> "a map"
            is StructureKind.CLASS -> "an object"
            is StructureKind.OBJECT -> "an object"
            is StructureKind.LIST -> "a list"
            is PrimitiveKind.STRING -> "a string"
            is PrimitiveKind.BOOLEAN -> "a boolean"
            is PrimitiveKind.BYTE -> "a byte"
            is PrimitiveKind.CHAR -> "a character"
            is PrimitiveKind.DOUBLE -> "a double"
            is PrimitiveKind.FLOAT -> "a float"
            is PrimitiveKind.INT -> "an integer"
            is PrimitiveKind.SHORT -> "a short"
            is PrimitiveKind.LONG -> "a long"
            is SerialKind.ENUM -> "an enumeration value"
            else -> "a $this value"
        }
    }
