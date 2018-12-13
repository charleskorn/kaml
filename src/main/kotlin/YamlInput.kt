import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.ElementValueDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.internal.EnumDescriptor

sealed class YamlInput : ElementValueDecoder() {
    companion object {
        fun createFor(node: YamlNode): YamlInput = when (node) {
            is YamlScalar -> YamlScalarInput(node)
            is YamlNull -> YamlNullInput
            is YamlList -> YamlListInput(node)
            is YamlMap -> YamlMapInput(node)
        }
    }

    override val updateMode: UpdateMode = UpdateMode.BANNED
}

private class YamlScalarInput(val scalar: YamlScalar) : YamlInput() {
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

        throw YamlException(
            "Value ${scalar.contentToString()} is not a valid option, permitted choices are: $choices",
            scalar.location
        )
    }
}

private object YamlNullInput : YamlInput() {
    override fun decodeNotNullMark(): Boolean = false
}

private class YamlListInput(val list: YamlList) : YamlInput() {
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
}

private class YamlMapInput(val map: YamlMap) : YamlInput() {
    private val entriesList = map.entries.entries.toList()
    private var nextIndex = 0
    private lateinit var currentValueDecoder: YamlInput
    private lateinit var readMode: MapReadMode

    override fun decodeElementIndex(desc: SerialDescriptor): Int = when (readMode) {
        MapReadMode.Object -> decodeElementIndexForObject(desc)
        MapReadMode.Map -> decodeElementIndexForMap()
    }

    private fun decodeElementIndexForObject(desc: SerialDescriptor): Int {
        if (nextIndex == entriesList.size) {
            return READ_DONE
        }

        val currentEntry = entriesList[nextIndex]
        val key = currentEntry.key
        val name = getPropertyName(key)
        val fieldDescriptorIndex = desc.getElementIndex(name)

        if (fieldDescriptorIndex == UNKNOWN_NAME) {
            throwUnknownProperty(name, key.location, desc)
        }

        currentValueDecoder = createFor(entriesList[nextIndex].value)
        nextIndex++

        return fieldDescriptorIndex
    }

    private fun decodeElementIndexForMap(): Int {
        if (nextIndex == entriesList.size * 2) {
            return READ_DONE
        }

        val entryIndex = nextIndex / 2

        currentValueDecoder = when {
            nextIndex % 2 == 0 -> createFor(entriesList[entryIndex].key)
            else -> createFor(entriesList[entryIndex].value)
        }

        return nextIndex++
    }

    private fun getPropertyName(key: YamlNode): String = when (key) {
        is YamlScalar -> key.content
        is YamlNull, is YamlMap, is YamlList -> throw YamlException(
            "Property name must not be a list, map or null value. (To use 'null' as a property name, enclose it in quotes.)",
            key.location
        )
    }

    private fun throwUnknownProperty(name: String, location: Location, desc: SerialDescriptor): Nothing {
        val knownPropertyNames = (0 until desc.elementsCount)
            .map { desc.getElementName(it) }
            .sorted()
            .joinToString(", ")

        throw YamlException("Unknown property '$name'. Known properties are: $knownPropertyNames", location)
    }

    override fun decodeNotNullMark(): Boolean = currentValueDecoder.decodeNotNullMark()
    override fun decodeString(): String = currentValueDecoder.decodeString()
    override fun decodeInt(): Int = currentValueDecoder.decodeInt()
    override fun decodeLong(): Long = currentValueDecoder.decodeLong()
    override fun decodeShort(): Short = currentValueDecoder.decodeShort()
    override fun decodeByte(): Byte = currentValueDecoder.decodeByte()
    override fun decodeDouble(): Double = currentValueDecoder.decodeDouble()
    override fun decodeFloat(): Float = currentValueDecoder.decodeFloat()
    override fun decodeBoolean(): Boolean = currentValueDecoder.decodeBoolean()
    override fun decodeChar(): Char = currentValueDecoder.decodeChar()
    override fun decodeEnum(enumDescription: EnumDescriptor): Int = currentValueDecoder.decodeEnum(enumDescription)

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
}
