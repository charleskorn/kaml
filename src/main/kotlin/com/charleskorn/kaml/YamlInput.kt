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
import kotlinx.serialization.ElementValueDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.internal.EnumDescriptor
import kotlinx.serialization.modules.SerialModule

sealed class YamlInput(val node: YamlNode, override var context: SerialModule) : ElementValueDecoder() {
    companion object {
        fun createFor(node: YamlNode, context: SerialModule): YamlInput = when (node) {
            is YamlScalar -> YamlScalarInput(node, context)
            is YamlNull -> YamlNullInput(node, context)
            is YamlList -> YamlListInput(node, context)
            is YamlMap -> YamlMapInput(node, context)
        }
    }

    override val updateMode: UpdateMode = UpdateMode.BANNED

    abstract fun getCurrentLocation(): Location
}

private class YamlScalarInput(val scalar: YamlScalar, context: SerialModule) : YamlInput(scalar, context) {
    override fun decodeString(): String = scalar.content
    override fun decodeInt(): Int = scalar.toInt()
    override fun decodeLong(): Long = scalar.toLong()
    override fun decodeShort(): Short = scalar.toShort()
    override fun decodeByte(): Byte = scalar.toByte()
    override fun decodeDouble(): Double = scalar.toDouble()
    override fun decodeFloat(): Float = scalar.toFloat()
    override fun decodeBoolean(): Boolean = scalar.toBoolean()
    override fun decodeChar(): Char = scalar.toChar()

    override fun decodeEnum(enumDescription: EnumDescriptor): Int {
        val index = enumDescription.getElementIndex(scalar.content)

        if (index != UNKNOWN_NAME) {
            return index
        }

        val choices = (0..enumDescription.elementsCount - 1)
            .map { enumDescription.getElementName(it) }
            .sorted()
            .joinToString(", ")

        throw YamlScalarFormatException("Value ${scalar.contentToString()} is not a valid option, permitted choices are: $choices", scalar.location, scalar.content)
    }

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        when (desc.kind) {
            is StructureKind.MAP -> throw IncorrectTypeException("Expected a map, but got a scalar value", scalar.location)
            is StructureKind.CLASS -> throw IncorrectTypeException("Expected an object, but got a scalar value", scalar.location)
            is StructureKind.LIST -> throw IncorrectTypeException("Expected a list, but got a scalar value", scalar.location)
        }

        return super.beginStructure(desc, *typeParams)
    }

    override fun getCurrentLocation(): Location = scalar.location
}

private class YamlNullInput(val nullValue: YamlNode, context: SerialModule) : YamlInput(nullValue, context) {
    override fun decodeNotNullMark(): Boolean = false

    override fun decodeValue(): Any = throw UnexpectedNullValueException(nullValue.location)
    override fun decodeCollectionSize(desc: SerialDescriptor): Int = throw UnexpectedNullValueException(nullValue.location)
    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder = throw UnexpectedNullValueException(nullValue.location)

    override fun getCurrentLocation(): Location = nullValue.location
}

private class YamlListInput(val list: YamlList, context: SerialModule) : YamlInput(list, context) {
    private var nextElementIndex = 0
    private lateinit var currentElementDecoder: YamlInput

    override fun decodeCollectionSize(desc: SerialDescriptor): Int = list.items.size

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        if (nextElementIndex == list.items.size) {
            return READ_DONE
        }

        currentElementDecoder = createFor(list.items[nextElementIndex], context)

        return nextElementIndex++
    }

    override fun decodeNotNullMark(): Boolean = checkTypeAndDecodeFromCurrentValue("a (possibly null) scalar value") { decodeNotNullMark() }
    override fun decodeString(): String = checkTypeAndDecodeFromCurrentValue("a string") { decodeString() }
    override fun decodeInt(): Int = checkTypeAndDecodeFromCurrentValue("an integer") { decodeInt() }
    override fun decodeLong(): Long = checkTypeAndDecodeFromCurrentValue("a long") { decodeLong() }
    override fun decodeShort(): Short = checkTypeAndDecodeFromCurrentValue("a short") { decodeShort() }
    override fun decodeByte(): Byte = checkTypeAndDecodeFromCurrentValue("a byte") { decodeByte() }
    override fun decodeDouble(): Double = checkTypeAndDecodeFromCurrentValue("a double") { decodeDouble() }
    override fun decodeFloat(): Float = checkTypeAndDecodeFromCurrentValue("a float") { decodeFloat() }
    override fun decodeBoolean(): Boolean = checkTypeAndDecodeFromCurrentValue("a boolean") { decodeBoolean() }
    override fun decodeChar(): Char = checkTypeAndDecodeFromCurrentValue("a character") { decodeChar() }
    override fun decodeEnum(enumDescription: EnumDescriptor): Int = checkTypeAndDecodeFromCurrentValue("an enumeration value") { decodeEnum(enumDescription) }

    private fun <T> checkTypeAndDecodeFromCurrentValue(expectedTypeDescription: String, action: YamlInput.() -> T): T {
        if (!::currentElementDecoder.isInitialized) {
            throw IncorrectTypeException("Expected $expectedTypeDescription, but got a list", list.location)
        }

        return action(currentElementDecoder)
    }

    private val haveStartedReadingElements: Boolean
        get() = nextElementIndex > 0

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        if (haveStartedReadingElements) {
            return currentElementDecoder.beginStructure(desc, *typeParams)
        }

        when (desc.kind) {
            is StructureKind.MAP -> throw IncorrectTypeException("Expected a map, but got a list", list.location)
            is StructureKind.CLASS -> throw IncorrectTypeException("Expected an object, but got a list", list.location)
            else -> return super.beginStructure(desc, *typeParams)
        }
    }

    override fun getCurrentLocation(): Location = currentElementDecoder.node.location
}

private class YamlMapInput(val map: YamlMap, context: SerialModule) : YamlInput(map, context) {
    private val entriesList = map.entries.entries.toList()
    private var nextIndex = 0
    private lateinit var currentEntry: Map.Entry<YamlNode, YamlNode>
    private lateinit var currentValueDecoder: YamlInput
    private lateinit var readMode: MapReadMode
    private var currentlyReadingValue: Boolean = false

    override fun decodeElementIndex(desc: SerialDescriptor): Int = when (readMode) {
        MapReadMode.Object -> decodeElementIndexForObject(desc)
        MapReadMode.Map -> decodeElementIndexForMap()
    }

    private fun decodeElementIndexForObject(desc: SerialDescriptor): Int {
        if (nextIndex == entriesList.size) {
            return READ_DONE
        }

        currentEntry = entriesList[nextIndex]
        val key = currentEntry.key
        val name = getPropertyName(key)
        val fieldDescriptorIndex = desc.getElementIndex(name)

        if (fieldDescriptorIndex == UNKNOWN_NAME) {
            throwUnknownProperty(name, key.location, desc)
        }

        currentValueDecoder = createFor(entriesList[nextIndex].value, context)
        currentlyReadingValue = true
        nextIndex++

        return fieldDescriptorIndex
    }

    private fun decodeElementIndexForMap(): Int {
        if (nextIndex == entriesList.size * 2) {
            return READ_DONE
        }

        val entryIndex = nextIndex / 2
        currentEntry = entriesList[entryIndex]
        currentlyReadingValue = nextIndex % 2 != 0

        currentValueDecoder = when (currentlyReadingValue) {
            true -> createFor(currentEntry.value, context)
            false -> createFor(currentEntry.key, context)
        }

        return nextIndex++
    }

    private fun getPropertyName(key: YamlNode): String = when (key) {
        is YamlScalar -> key.content
        is YamlNull, is YamlMap, is YamlList -> throw MalformedYamlException("Property name must not be a list, map or null value. (To use 'null' as a property name, enclose it in quotes.)", key.location)
    }

    private fun throwUnknownProperty(name: String, location: Location, desc: SerialDescriptor): Nothing {
        val knownPropertyNames = (0 until desc.elementsCount)
            .map { desc.getElementName(it) }
            .toSet()

        throw UnknownPropertyException(name, knownPropertyNames, location)
    }

    override fun decodeNotNullMark(): Boolean = checkTypeAndDecodeFromCurrentValue("a (possibly null) scalar value") { decodeNotNullMark() }
    override fun decodeString(): String = checkTypeAndDecodeFromCurrentValue("a string") { decodeString() }
    override fun decodeInt(): Int = checkTypeAndDecodeFromCurrentValue("an integer") { decodeInt() }
    override fun decodeLong(): Long = checkTypeAndDecodeFromCurrentValue("a long") { decodeLong() }
    override fun decodeShort(): Short = checkTypeAndDecodeFromCurrentValue("a short") { decodeShort() }
    override fun decodeByte(): Byte = checkTypeAndDecodeFromCurrentValue("a byte") { decodeByte() }
    override fun decodeDouble(): Double = checkTypeAndDecodeFromCurrentValue("a double") { decodeDouble() }
    override fun decodeFloat(): Float = checkTypeAndDecodeFromCurrentValue("a float") { decodeFloat() }
    override fun decodeBoolean(): Boolean = checkTypeAndDecodeFromCurrentValue("a boolean") { decodeBoolean() }
    override fun decodeChar(): Char = checkTypeAndDecodeFromCurrentValue("a character") { decodeChar() }
    override fun decodeEnum(enumDescription: EnumDescriptor): Int = checkTypeAndDecodeFromCurrentValue("an enumeration value") { decodeEnum(enumDescription) }

    private fun <T> checkTypeAndDecodeFromCurrentValue(expectedTypeDescription: String, action: YamlInput.() -> T): T {
        if (!::currentValueDecoder.isInitialized) {
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

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        if (haveStartedReadingEntries) {
            return fromCurrentValue { beginStructure(desc, *typeParams) }
        }

        readMode = when (desc.kind) {
            is StructureKind.MAP -> MapReadMode.Map
            is StructureKind.CLASS -> MapReadMode.Object
            is StructureKind.LIST -> throw IncorrectTypeException("Expected a list, but got a map", map.location)
            else -> throw YamlException("Can't decode into ${desc.kind}", map.location)
        }

        return super.beginStructure(desc, *typeParams)
    }

    private enum class MapReadMode {
        Object,
        Map
    }

    override fun getCurrentLocation(): Location = currentValueDecoder.node.location
}
