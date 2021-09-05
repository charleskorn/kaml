/*

   Copyright 2018-2021 Charles Korn.

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
public sealed class YamlInput(
    public val node: YamlNode,
    override var serializersModule: SerializersModule,
    public val configuration: YamlConfiguration
) : AbstractDecoder() {
    internal companion object {
        private val missingFieldExceptionMessage: Regex = """^Field '(.*)' is required for type with serial name '.*', but it was missing$""".toRegex()

        internal fun createFor(node: YamlNode, context: SerializersModule, configuration: YamlConfiguration, descriptor: SerialDescriptor): YamlInput = when (node) {
            is YamlNull -> when {
                descriptor.kind is PolymorphicKind && !descriptor.isNullable -> throw MissingTypeTagException(node.path)
                else -> YamlNullInput(node, context, configuration)
            }

            is YamlScalar -> when (descriptor.kind) {
                is PrimitiveKind, SerialKind.ENUM -> YamlScalarInput(node, context, configuration)
                is SerialKind.CONTEXTUAL -> YamlContextualInput(node, context, configuration)
                is PolymorphicKind -> throw MissingTypeTagException(node.path)
                else -> throw IncorrectTypeException("Expected ${descriptor.kind.friendlyDescription}, but got a scalar value", node.path)
            }

            is YamlList -> when (descriptor.kind) {
                is StructureKind.LIST -> YamlListInput(node, context, configuration)
                is SerialKind.CONTEXTUAL -> YamlContextualInput(node, context, configuration)
                else -> throw IncorrectTypeException("Expected ${descriptor.kind.friendlyDescription}, but got a list", node.path)
            }

            is YamlMap -> when (descriptor.kind) {
                is StructureKind.CLASS, StructureKind.OBJECT -> YamlObjectInput(node, context, configuration)
                is StructureKind.MAP -> YamlMapInput(node, context, configuration)
                is SerialKind.CONTEXTUAL -> YamlContextualInput(node, context, configuration)
                is PolymorphicKind -> when (configuration.polymorphismStyle) {
                    PolymorphismStyle.Tag -> throw MissingTypeTagException(node.path)
                    PolymorphismStyle.Property -> createPolymorphicMapDeserializer(node, context, configuration)
                }
                else -> throw IncorrectTypeException("Expected ${descriptor.kind.friendlyDescription}, but got a map", node.path)
            }

            is YamlTaggedNode -> when {
                descriptor.kind is PolymorphicKind && configuration.polymorphismStyle == PolymorphismStyle.Tag -> YamlPolymorphicInput(node.tag, node.path, node.innerNode, context, configuration)
                else -> createFor(node.innerNode, context, configuration, descriptor)
            }
        }

        private fun createPolymorphicMapDeserializer(node: YamlMap, context: SerializersModule, configuration: YamlConfiguration): YamlPolymorphicInput {
            val desiredKey = configuration.polymorphismPropertyName
            when (val typeName = node.getValue(desiredKey)) {
                is YamlList -> throw InvalidPropertyValueException(desiredKey, "expected a string, but got a list", typeName.path)
                is YamlMap -> throw InvalidPropertyValueException(desiredKey, "expected a string, but got a map", typeName.path)
                is YamlNull -> throw InvalidPropertyValueException(desiredKey, "expected a string, but got a null value", typeName.path)
                is YamlTaggedNode -> throw InvalidPropertyValueException(desiredKey, "expected a string, but got a tagged value", typeName.path)
                is YamlScalar -> {
                    val remainingProperties = node.withoutKey(desiredKey)

                    return YamlPolymorphicInput(typeName.content, typeName.path, remainingProperties, context, configuration)
                }
            }
        }

        private fun YamlMap.getValue(desiredKey: String): YamlNode {
            return this.get(desiredKey) ?: throw MissingRequiredPropertyException(desiredKey, this.path)
        }

        private fun YamlMap.withoutKey(key: String): YamlMap {
            return this.copy(entries = entries.filterKeys { it.content != key })
        }
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        try {
            return super.decodeSerializableValue(deserializer)
        } catch (e: SerializationException) {
            throwIfMissingRequiredPropertyException(e)

            throw e
        }
    }

    private fun throwIfMissingRequiredPropertyException(e: SerializationException) {
        val match = missingFieldExceptionMessage.matchEntire(e.message!!) ?: return

        throw MissingRequiredPropertyException(match.groupValues[1], node.path, e)
    }

    public abstract fun getCurrentLocation(): Location
    public abstract fun getCurrentPath(): YamlPath
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

        val choices = (0..enumDescriptor.elementsCount - 1)
            .map { enumDescriptor.getElementName(it) }
            .sorted()
            .joinToString(", ")

        throw YamlScalarFormatException("Value ${scalar.contentToString()} is not a valid option, permitted choices are: $choices", scalar.path, scalar.content)
    }

    override fun getCurrentLocation(): Location = scalar.location
    override fun getCurrentPath(): YamlPath = scalar.path

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = 0
}

@OptIn(ExperimentalSerializationApi::class)
private class YamlNullInput(val nullValue: YamlNode, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(nullValue, context, configuration) {
    override fun decodeNotNullMark(): Boolean = false

    override fun decodeValue(): Any = throw UnexpectedNullValueException(nullValue.path)
    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = throw UnexpectedNullValueException(nullValue.path)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = throw UnexpectedNullValueException(nullValue.path)

    override fun getCurrentLocation(): Location = nullValue.location
    override fun getCurrentPath(): YamlPath = nullValue.path

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = DECODE_DONE
}

@OptIn(ExperimentalSerializationApi::class)
private class YamlListInput(val list: YamlList, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(list, context, configuration) {
    private var nextElementIndex = 0
    private lateinit var currentElementDecoder: YamlInput

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = list.items.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (nextElementIndex == list.items.size) {
            return CompositeDecoder.DECODE_DONE
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

    override fun getCurrentPath(): YamlPath {
        return if (haveStartedReadingElements) {
            currentElementDecoder.node.path
        } else {
            list.path
        }
    }

    override fun getCurrentLocation(): Location = getCurrentPath().endLocation
}

private class YamlContextualInput(node: YamlNode, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(node, context, configuration) {
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = throw IllegalStateException("Must call beginStructure() and use returned Decoder")
    override fun decodeValue(): Any = throw IllegalStateException("Must call beginStructure() and use returned Decoder")

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        createFor(node, serializersModule, configuration, descriptor)

    override fun getCurrentLocation(): Location = node.location
    override fun getCurrentPath(): YamlPath = node.path
}

private sealed class YamlMapLikeInputBase(map: YamlMap, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(map, context, configuration) {
    protected lateinit var currentValueDecoder: YamlInput
    protected lateinit var currentKey: YamlScalar
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
                throw InvalidPropertyValueException(propertyName, e.message, e.path, e)
            } else {
                throw e
            }
        }
    }

    protected val haveStartedReadingEntries: Boolean
        get() = this::currentValueDecoder.isInitialized

    override fun getCurrentPath(): YamlPath {
        return if (haveStartedReadingEntries) {
            currentValueDecoder.node.path
        } else {
            node.path
        }
    }

    override fun getCurrentLocation(): Location = getCurrentPath().endLocation

    protected val propertyName: String
        get() = currentKey.content
}

@OptIn(ExperimentalSerializationApi::class)
private class YamlMapInput(map: YamlMap, context: SerializersModule, configuration: YamlConfiguration) : YamlMapLikeInputBase(map, context, configuration) {
    private val entriesList = map.entries.entries.toList()
    private var nextIndex = 0
    private lateinit var currentEntry: Map.Entry<YamlScalar, YamlNode>

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (nextIndex == entriesList.size * 2) {
            return CompositeDecoder.DECODE_DONE
        }

        val entryIndex = nextIndex / 2
        currentEntry = entriesList[entryIndex]
        currentKey = currentEntry.key
        currentlyReadingValue = nextIndex % 2 != 0

        currentValueDecoder = when (currentlyReadingValue) {
            true ->
                try {
                    createFor(currentEntry.value, serializersModule, configuration, descriptor.getElementDescriptor(1))
                } catch (e: IncorrectTypeException) {
                    throw InvalidPropertyValueException(propertyName, e.message, e.path, e)
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
                return CompositeDecoder.DECODE_DONE
            }

            val currentEntry = entriesList[nextIndex]
            currentKey = currentEntry.key
            val fieldDescriptorIndex = descriptor.getElementIndex(propertyName)

            if (fieldDescriptorIndex == UNKNOWN_NAME) {
                if (configuration.strictMode) {
                    throwUnknownProperty(propertyName, currentKey.path, descriptor)
                } else {
                    nextIndex++
                    continue
                }
            }

            try {
                currentValueDecoder = createFor(entriesList[nextIndex].value, serializersModule, configuration, descriptor.getElementDescriptor(fieldDescriptorIndex))
            } catch (e: IncorrectTypeException) {
                throw InvalidPropertyValueException(propertyName, e.message, e.path, e)
            }

            currentlyReadingValue = true
            nextIndex++

            return fieldDescriptorIndex
        }
    }

    private fun throwUnknownProperty(name: String, path: YamlPath, desc: SerialDescriptor): Nothing {
        val knownPropertyNames = (0 until desc.elementsCount)
            .map { desc.getElementName(it) }
            .toSet()

        throw UnknownPropertyException(name, knownPropertyNames, path)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (haveStartedReadingEntries) {
            return fromCurrentValue { beginStructure(descriptor) }
        }

        return super.beginStructure(descriptor)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class YamlPolymorphicInput(private val typeName: String, private val typeNamePath: YamlPath, private val contentNode: YamlNode, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(contentNode, context, configuration) {
    private var currentField = CurrentField.NotStarted
    private lateinit var contentDecoder: YamlInput

    override fun getCurrentLocation(): Location = contentNode.location
    override fun getCurrentPath(): YamlPath = contentNode.path

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
            CurrentField.Content -> CompositeDecoder.DECODE_DONE
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

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        try {
            return super.decodeSerializableValue(deserializer)
        } catch (e: SerializationException) {
            throwIfUnknownPolymorphicTypeException(e, deserializer)

            throw e
        }
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

        throw UnknownPolymorphicTypeException(unknownType, knownTypes, typeNamePath, e)
    }

    private fun getKnownTypesForSealedType(deserializer: DeserializationStrategy<*>): Set<String> {
        val typesDescriptor = deserializer.descriptor.getElementDescriptor(1)

        return typesDescriptor.elementNames.toSet()
    }

    private fun getKnownTypesForOpenType(className: String): Set<String> {
        val knownTypes = mutableSetOf<String>()

        serializersModule.dumpTo(object : SerializersModuleCollector {
            override fun <T : Any> contextual(kClass: KClass<T>, provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>) {}

            // FIXME: ideally we'd be able to get the name as used by the SerialModule (eg. the values in 'polyBase2NamedSerializers' in SerialModuleImpl, but these aren't exposed.
            // The serializer's descriptor's name seems to be the same value.
            override fun <Base : Any, Sub : Base> polymorphic(baseClass: KClass<Base>, actualClass: KClass<Sub>, actualSerializer: KSerializer<Sub>) {
                if (baseClass.simpleName == className) {
                    knownTypes.add(actualSerializer.descriptor.serialName)
                }
            }

            override fun <Base : Any> polymorphicDefault(baseClass: KClass<Base>, defaultSerializerProvider: (className: String?) -> DeserializationStrategy<out Base>?) {
                throw UnsupportedOperationException("This method should never be called.")
            }
        })

        return knownTypes
    }

    private enum class CurrentField {
        NotStarted,
        Type,
        Content
    }

    companion object {
        private val unknownPolymorphicTypeExceptionMessage: Regex = """^Class '(.*)' is not registered for polymorphic serialization in the scope of '(.*)'.\nMark the base class as 'sealed' or register the serializer explicitly.$""".toRegex()
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
