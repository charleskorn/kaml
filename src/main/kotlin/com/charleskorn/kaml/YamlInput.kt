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

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.builtins.AbstractDecoder
import kotlinx.serialization.modules.SerialModule

sealed class YamlInput(val node: YamlNode, override var context: SerialModule, val configuration: YamlConfiguration) : AbstractDecoder() {
    companion object {
        fun createFor(node: YamlNode, context: SerialModule, configuration: YamlConfiguration, descriptor: SerialDescriptor): YamlInput = when (node) {
            is YamlScalar -> YamlScalarInput(node, context, configuration)
            is YamlNull -> YamlNullInput(node, context, configuration)
            is YamlList -> YamlListInput(node, context, configuration)
            is YamlMap -> YamlMapInput(node, context, configuration)
            is YamlTaggedNode -> if (descriptor.kind is PolymorphicKind) {
                YamlTaggedInput(node, context, configuration, descriptor)
            } else {
                createFor(node.node, context, configuration, descriptor)
            }
        }
    }

    override val updateMode: UpdateMode = UpdateMode.BANNED

    abstract fun getCurrentLocation(): Location
}

private class YamlScalarInput(val scalar: YamlScalar, context: SerialModule, configuration: YamlConfiguration) : YamlInput(scalar, context, configuration) {
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

        throw YamlScalarFormatException("Value ${scalar.contentToString()} is not a valid option, permitted choices are: $choices", scalar.location, scalar.content)
    }

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        when (descriptor.kind) {
            is StructureKind.MAP -> throw IncorrectTypeException("Expected a map, but got a scalar value", scalar.location)
            is StructureKind.CLASS -> throw IncorrectTypeException("Expected an object, but got a scalar value", scalar.location)
            is StructureKind.LIST -> throw IncorrectTypeException("Expected a list, but got a scalar value", scalar.location)
        }

        return super.beginStructure(descriptor, *typeParams)
    }

    override fun getCurrentLocation(): Location = scalar.location

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = 0
}

private class YamlNullInput(val nullValue: YamlNode, context: SerialModule, configuration: YamlConfiguration) : YamlInput(nullValue, context, configuration) {
    override fun decodeNotNullMark(): Boolean = false

    override fun decodeValue(): Any = throw UnexpectedNullValueException(nullValue.location)
    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = throw UnexpectedNullValueException(nullValue.location)
    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder = throw UnexpectedNullValueException(nullValue.location)

    override fun getCurrentLocation(): Location = nullValue.location

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = 0
}

private class YamlListInput(val list: YamlList, context: SerialModule, configuration: YamlConfiguration) : YamlInput(list, context, configuration) {
    private var nextElementIndex = 0
    private lateinit var currentElementDecoder: YamlInput

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = list.items.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (nextElementIndex == list.items.size) {
            return READ_DONE
        }

        currentElementDecoder = createFor(list.items[nextElementIndex], context, configuration, descriptor.getElementDescriptor(0))

        return nextElementIndex++
    }

    override fun decodeNotNullMark(): Boolean {
        if (!haveStartedReadingElements) {
            return true
        }

        return currentElementDecoder.decodeNotNullMark()
    }

    override fun decodeString(): String = checkTypeAndDecodeFromCurrentValue("a string") { decodeString() }
    override fun decodeInt(): Int = checkTypeAndDecodeFromCurrentValue("an integer") { decodeInt() }
    override fun decodeLong(): Long = checkTypeAndDecodeFromCurrentValue("a long") { decodeLong() }
    override fun decodeShort(): Short = checkTypeAndDecodeFromCurrentValue("a short") { decodeShort() }
    override fun decodeByte(): Byte = checkTypeAndDecodeFromCurrentValue("a byte") { decodeByte() }
    override fun decodeDouble(): Double = checkTypeAndDecodeFromCurrentValue("a double") { decodeDouble() }
    override fun decodeFloat(): Float = checkTypeAndDecodeFromCurrentValue("a float") { decodeFloat() }
    override fun decodeBoolean(): Boolean = checkTypeAndDecodeFromCurrentValue("a boolean") { decodeBoolean() }
    override fun decodeChar(): Char = checkTypeAndDecodeFromCurrentValue("a character") { decodeChar() }
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = checkTypeAndDecodeFromCurrentValue("an enumeration value") { decodeEnum(enumDescriptor) }

    private fun <T> checkTypeAndDecodeFromCurrentValue(expectedTypeDescription: String, action: YamlInput.() -> T): T {
        if (!haveStartedReadingElements) {
            throw IncorrectTypeException("Expected $expectedTypeDescription, but got a list", list.location)
        }

        return action(currentElementDecoder)
    }

    private val haveStartedReadingElements: Boolean
        get() = nextElementIndex > 0

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        if (haveStartedReadingElements) {
            return currentElementDecoder.beginStructure(descriptor, *typeParams)
        }

        when (descriptor.kind) {
            is StructureKind.MAP -> throw IncorrectTypeException("Expected a map, but got a list", list.location)
            is StructureKind.CLASS -> throw IncorrectTypeException("Expected an object, but got a list", list.location)
            else -> return super.beginStructure(descriptor, *typeParams)
        }
    }

    override fun getCurrentLocation(): Location {
        return if (haveStartedReadingElements) {
            currentElementDecoder.node.location
        } else {
            list.location
        }
    }
}

private class YamlMapInput(val map: YamlMap, context: SerialModule, configuration: YamlConfiguration) : YamlInput(map, context, configuration) {
    private val entriesList = map.entries.entries.toList()
    private var nextIndex = 0
    private lateinit var currentEntry: Map.Entry<YamlNode, YamlNode>
    private lateinit var currentValueDecoder: YamlInput
    private lateinit var readMode: MapReadMode
    private var currentlyReadingValue: Boolean = false

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = when (readMode) {
        MapReadMode.Object -> decodeElementIndexForObject(descriptor)
        MapReadMode.Map -> decodeElementIndexForMap(descriptor)
    }

    private fun decodeElementIndexForObject(desc: SerialDescriptor): Int {
        while (true) {
            if (nextIndex == entriesList.size) {
                return READ_DONE
            }

            currentEntry = entriesList[nextIndex]
            val key = currentEntry.key
            val name = getPropertyName(key)
            val fieldDescriptorIndex = desc.getElementIndex(name)

            if (fieldDescriptorIndex == UNKNOWN_NAME) {
                if (configuration.strictMode) {
                    throwUnknownProperty(name, key.location, desc)
                } else {
                    nextIndex++
                    continue
                }
            }

            currentValueDecoder = createFor(entriesList[nextIndex].value, context, configuration, desc.getElementDescriptor(fieldDescriptorIndex))
            currentlyReadingValue = true
            nextIndex++

            return fieldDescriptorIndex
        }
    }

    private fun decodeElementIndexForMap(descriptor: SerialDescriptor): Int {
        if (nextIndex == entriesList.size * 2) {
            return READ_DONE
        }

        val entryIndex = nextIndex / 2
        currentEntry = entriesList[entryIndex]
        currentlyReadingValue = nextIndex % 2 != 0

        currentValueDecoder = when (currentlyReadingValue) {
            true -> createFor(currentEntry.value, context, configuration, descriptor.getElementDescriptor(1))
            false -> createFor(currentEntry.key, context, configuration, descriptor.getElementDescriptor(0))
        }

        return nextIndex++
    }

    private fun getPropertyName(key: YamlNode): String = when (key) {
        is YamlScalar -> key.content
        is YamlNull, is YamlMap, is YamlList, is YamlTaggedNode -> throw MalformedYamlException("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)", key.location)
    }

    private fun throwUnknownProperty(name: String, location: Location, desc: SerialDescriptor): Nothing {
        val knownPropertyNames = (0 until desc.elementsCount)
            .map { desc.getElementName(it) }
            .toSet()

        throw UnknownPropertyException(name, knownPropertyNames, location)
    }

    override fun decodeNotNullMark(): Boolean {
        if (!haveStartedReadingEntries) {
            return true
        }

        return fromCurrentValue { decodeNotNullMark() }
    }

    override fun decodeString(): String = checkTypeAndDecodeFromCurrentValue("a string") { decodeString() }
    override fun decodeInt(): Int = checkTypeAndDecodeFromCurrentValue("an integer") { decodeInt() }
    override fun decodeLong(): Long = checkTypeAndDecodeFromCurrentValue("a long") { decodeLong() }
    override fun decodeShort(): Short = checkTypeAndDecodeFromCurrentValue("a short") { decodeShort() }
    override fun decodeByte(): Byte = checkTypeAndDecodeFromCurrentValue("a byte") { decodeByte() }
    override fun decodeDouble(): Double = checkTypeAndDecodeFromCurrentValue("a double") { decodeDouble() }
    override fun decodeFloat(): Float = checkTypeAndDecodeFromCurrentValue("a float") { decodeFloat() }
    override fun decodeBoolean(): Boolean = checkTypeAndDecodeFromCurrentValue("a boolean") { decodeBoolean() }
    override fun decodeChar(): Char = checkTypeAndDecodeFromCurrentValue("a character") { decodeChar() }
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = checkTypeAndDecodeFromCurrentValue("an enumeration value") { decodeEnum(enumDescriptor) }

    private fun <T> checkTypeAndDecodeFromCurrentValue(expectedTypeDescription: String, action: YamlInput.() -> T): T {
        if (!haveStartedReadingEntries) {
            throw IncorrectTypeException("Expected $expectedTypeDescription, but got a map", map.location)
        }

        return fromCurrentValue(action)
    }

    private fun <T> fromCurrentValue(action: YamlInput.() -> T): T {
        try {
            return action(currentValueDecoder)
        } catch (e: YamlException) {
            if (currentlyReadingValue) {
                throw InvalidPropertyValueException(getPropertyName(currentEntry.key), e.message, e.location, e)
            } else {
                throw e
            }
        }
    }

    private val haveStartedReadingEntries: Boolean
        get() = nextIndex > 0

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        if (haveStartedReadingEntries) {
            return fromCurrentValue { beginStructure(descriptor, *typeParams) }
        }

        readMode = when (descriptor.kind) {
            StructureKind.MAP -> MapReadMode.Map
            StructureKind.CLASS, StructureKind.OBJECT -> MapReadMode.Object
            StructureKind.LIST -> throw IncorrectTypeException("Expected a list, but got a map", map.location)
            else -> throw YamlException("Can't decode into ${descriptor.kind}", map.location)
        }

        return super.beginStructure(descriptor, *typeParams)
    }

    private enum class MapReadMode {
        Object,
        Map
    }

    override fun getCurrentLocation(): Location {
        return if (haveStartedReadingEntries) {
            currentValueDecoder.node.location
        } else {
            map.location
        }
    }
}

private class YamlTaggedInput(val taggedNode: YamlTaggedNode, context: SerialModule, configuration: YamlConfiguration, descriptor: SerialDescriptor) : YamlInput(taggedNode, context, configuration) {
    /**
     * index 0 -> tag
     * index 1 -> child node
     */
    private var currentIndex = -1
    private var isPolymorphic = false
    private val childDecoder: YamlInput = createFor(taggedNode.node, context, configuration, descriptor.getElementDescriptor(1))

    override fun getCurrentLocation(): Location = maybeCallOnChild(blockOnTag = taggedNode::location, blockOnChild = YamlInput::getCurrentLocation)

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        descriptor.calculatePolymorphic()
        return when (++currentIndex) {
            0, 1 -> currentIndex
            else -> READ_DONE
        }
    }

    override fun decodeNotNullMark(): Boolean = maybeCallOnChild(blockOnTag = { true }, blockOnChild = YamlInput::decodeNotNullMark)
    override fun decodeNull(): Nothing? = maybeCallOnChild("decodeNull", blockOnChild = YamlInput::decodeNull)
    override fun decodeUnit(): Unit = maybeCallOnChild("decodeUnit", blockOnChild = YamlInput::decodeUnit)
    override fun decodeBoolean(): Boolean = maybeCallOnChild("decodeBoolean", blockOnChild = YamlInput::decodeBoolean)
    override fun decodeByte(): Byte = maybeCallOnChild("decodeByte", blockOnChild = YamlInput::decodeByte)
    override fun decodeShort(): Short = maybeCallOnChild("decodeShort", blockOnChild = YamlInput::decodeShort)
    override fun decodeInt(): Int = maybeCallOnChild("decodeInt", blockOnChild = YamlInput::decodeInt)
    override fun decodeLong(): Long = maybeCallOnChild("decodeLong", blockOnChild = YamlInput::decodeLong)
    override fun decodeFloat(): Float = maybeCallOnChild("decodeFloat", blockOnChild = YamlInput::decodeFloat)
    override fun decodeDouble(): Double = maybeCallOnChild("decodeDouble", blockOnChild = YamlInput::decodeDouble)
    override fun decodeChar(): Char = maybeCallOnChild("decodeChar", blockOnChild = YamlInput::decodeChar)
    override fun decodeString(): String = maybeCallOnChild(blockOnTag = taggedNode::tag, blockOnChild = YamlInput::decodeString)
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = maybeCallOnChild("decodeEnum") { decodeEnum(enumDescriptor) }

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        descriptor.calculatePolymorphic()
        return maybeCallOnChild(blockOnTag = { super.beginStructure(descriptor, *typeParams) }) { beginStructure(descriptor, *typeParams) }
    }

    private fun SerialDescriptor.calculatePolymorphic() {
        isPolymorphic = kind is PolymorphicKind
    }

    private inline fun <T> maybeCallOnChild(functionName: String, blockOnChild: YamlInput.() -> T): T =
        maybeCallOnChild(blockOnTag = { throw IllegalArgumentException("can't call $functionName on tag") }, blockOnChild = blockOnChild)

    private inline fun <T> maybeCallOnChild(blockOnTag: () -> T, blockOnChild: YamlInput.() -> T): T {
        return if (isPolymorphic && currentIndex != 1) {
            blockOnTag()
        } else {
            childDecoder.blockOnChild()
        }
    }
}
