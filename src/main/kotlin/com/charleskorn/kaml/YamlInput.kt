/*

   Copyright 2018 Charles Korn.

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

sealed class YamlInput(val node: YamlNode) : ElementValueDecoder() {
    companion object {
        fun createFor(node: YamlNode): YamlInput = when (node) {
            is YamlScalar -> YamlScalarInput(node)
            is YamlNull -> YamlNullInput(node)
            is YamlList -> YamlListInput(node)
            is YamlMap -> YamlMapInput(node)
        }
    }

    override val updateMode: UpdateMode = UpdateMode.BANNED

    abstract fun getCurrentLocation(): Location
}

private class YamlScalarInput(val scalar: YamlScalar) : YamlInput(scalar) {
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

    override fun getCurrentLocation(): Location = scalar.location
}

private class YamlNullInput(val nullValue: YamlNode) : YamlInput(nullValue) {
    override fun decodeNotNullMark(): Boolean = false

    override fun decodeValue(): Any = throw UnexpectedNullValueException(nullValue.location)
    override fun decodeCollectionSize(desc: SerialDescriptor): Int = throw UnexpectedNullValueException(nullValue.location)

    override fun getCurrentLocation(): Location = nullValue.location
}

private class YamlListInput(val list: YamlList) : YamlInput(list) {
    private var nextElementIndex = 0
    private lateinit var currentElementDecoder: YamlInput

    override fun decodeCollectionSize(desc: SerialDescriptor): Int = list.items.size

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        if (nextElementIndex == list.items.size) {
            return READ_DONE
        }

        currentElementDecoder = createFor(list.items[nextElementIndex])

        return nextElementIndex++
    }

    override fun decodeNotNullMark(): Boolean = currentElementDecoder.decodeNotNullMark()
    override fun decodeString(): String = currentElementDecoder.decodeString()
    override fun decodeInt(): Int = currentElementDecoder.decodeInt()
    override fun decodeLong(): Long = currentElementDecoder.decodeLong()
    override fun decodeShort(): Short = currentElementDecoder.decodeShort()
    override fun decodeByte(): Byte = currentElementDecoder.decodeByte()
    override fun decodeDouble(): Double = currentElementDecoder.decodeDouble()
    override fun decodeFloat(): Float = currentElementDecoder.decodeFloat()
    override fun decodeBoolean(): Boolean = currentElementDecoder.decodeBoolean()
    override fun decodeChar(): Char = currentElementDecoder.decodeChar()
    override fun decodeEnum(enumDescription: EnumDescriptor): Int = currentElementDecoder.decodeEnum(enumDescription)

    private val haveStartedReadingElements: Boolean
        get() = nextElementIndex > 0

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        if (haveStartedReadingElements) {
            return currentElementDecoder.beginStructure(desc, *typeParams)
        }

        return super.beginStructure(desc, *typeParams)
    }

    override fun getCurrentLocation(): Location = currentElementDecoder.node.location
}

private class YamlMapInput(val map: YamlMap) : YamlInput(map) {
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

        currentValueDecoder = createFor(entriesList[nextIndex].value)
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
            true -> createFor(currentEntry.value)
            false -> createFor(currentEntry.key)
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

    override fun decodeNotNullMark(): Boolean = fromCurrentValue { decodeNotNullMark() }
    override fun decodeString(): String = fromCurrentValue { decodeString() }
    override fun decodeInt(): Int = fromCurrentValue { decodeInt() }
    override fun decodeLong(): Long = fromCurrentValue { decodeLong() }
    override fun decodeShort(): Short = fromCurrentValue { decodeShort() }
    override fun decodeByte(): Byte = fromCurrentValue { decodeByte() }
    override fun decodeDouble(): Double = fromCurrentValue { decodeDouble() }
    override fun decodeFloat(): Float = fromCurrentValue { decodeFloat() }
    override fun decodeBoolean(): Boolean = fromCurrentValue { decodeBoolean() }
    override fun decodeChar(): Char = fromCurrentValue { decodeChar() }
    override fun decodeEnum(enumDescription: EnumDescriptor): Int = fromCurrentValue { decodeEnum(enumDescription) }

    private inline fun <T> fromCurrentValue(action: YamlInput.() -> T): T {
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
            return currentValueDecoder.beginStructure(desc, *typeParams)
        }

        readMode = when (desc.kind) {
            is StructureKind.MAP -> MapReadMode.Map
            is StructureKind.CLASS -> MapReadMode.Object
            else -> throw YamlException("Can't decode into ${desc.kind}", this.map.location)
        }

        return super.beginStructure(desc, *typeParams)
    }

    private enum class MapReadMode {
        Object,
        Map
    }

    override fun getCurrentLocation(): Location = currentValueDecoder.node.location
}
