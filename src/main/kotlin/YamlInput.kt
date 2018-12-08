import io.dahgan.parser.Code
import io.dahgan.parser.Token
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.ElementValueDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlinx.serialization.internal.EnumDescriptor

open class YamlInput(val parser: YamlParser) : ElementValueDecoder() {
    constructor(tokens: Sequence<Token>) : this(YamlParser(tokens))

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        return when (desc.kind) {
            is StructureKind.LIST -> YamlListInput(parser)
            is StructureKind.CLASS -> YamlObjectInput(parser)
            else -> throw IllegalStateException("Unknown structure kind: ${desc.kind}")
        }
    }

    override fun decodeString(): String {
        parser.consumeToken(Code.BeginNode)
        parser.consumeToken(Code.BeginScalar)

        val firstToken = parser.peekToken(Code.Indicator, Code.Text)

        val text = when (firstToken.code) {
            Code.Indicator -> {
                val quoteChar = parser.consumeToken(Code.Indicator).text.toString()
                val text = readText()
                val endQuoteToken = parser.consumeAnyToken()

                if (endQuoteToken.code != Code.Indicator || endQuoteToken.text.toString() != quoteChar) {
                    throw YamlException("Missing trailing $quoteChar", endQuoteToken)
                }

                text
            }
            Code.Text -> readText()
            else -> throw IllegalStateException()
        }

        parser.consumeToken(Code.EndScalar)
        parser.consumeToken(Code.EndNode)
        return text
    }

    private fun readText(): String {
        val builder = StringBuilder()

        while (true) {
            val nextToken = parser.peekToken(Code.Text, Code.BeginEscape, Code.Indicator, Code.EndScalar)

            when (nextToken.code) {
                Code.Text -> builder.append(parser.consumeToken(Code.Text).text.toString())
                Code.BeginEscape -> {
                    parser.consumeToken(Code.BeginEscape)
                    parser.consumeToken(Code.Indicator)
                    val escapedCharacter = parser.consumeToken(Code.Meta).text.toString()
                    builder.append(escapedCharacter)
                    parser.consumeToken(Code.EndEscape)
                }
                Code.Indicator, Code.EndScalar -> return builder.toString()
                else -> throw IllegalStateException()
            }
        }
    }

    override fun decodeEnum(enumDescription: EnumDescriptor): Int {
        val token = readSimpleScalar()
        val value = token.text.toString()
        val index = enumDescription.getElementIndex(value)

        if (index == UNKNOWN_NAME) {
            val choices = (0..enumDescription.elementsCount - 1)
                .map { enumDescription.getElementName(it) }
                .sorted()
                .joinToString(", ")

            throw YamlException("Value '$value' is not a valid option, permitted choices are: $choices", token)
        }

        return index
    }

    private fun readSimpleScalar(): Token {
        parser.consumeToken(Code.BeginNode)
        parser.consumeToken(Code.BeginScalar)
        val text = parser.consumeToken(Code.Text)
        parser.consumeToken(Code.EndScalar)
        parser.consumeToken(Code.EndNode)

        return text
    }

    // TODO: UpdateMode - probably should be BANNED
}

private class YamlListInput(parser: YamlParser) : YamlInput(parser) {
    private var nextElementIndex = 0

    init {
        parser.consumeToken(Code.BeginNode)
        parser.consumeToken(Code.BeginSequence)
    }

    override fun endStructure(desc: SerialDescriptor) {
        parser.consumeToken(Code.EndSequence)
        parser.consumeToken(Code.EndNode)

        super.endStructure(desc)
    }

    override fun decodeCollectionSize(desc: SerialDescriptor): Int {
        return -1
    }

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        val nextToken = parser.peekToken(Code.Indicator, Code.EndSequence)

        when (nextToken.code) {
            Code.Indicator -> {
                parser.consumeToken(Code.Indicator)

                if (nextToken.text.toString() == "]") {
                    return -1
                }

                return nextElementIndex++
            }
            Code.EndSequence -> return -1
            else -> throw IllegalStateException()
        }
    }
}

private class YamlObjectInput(parser: YamlParser) : YamlInput(parser) {
    init {
        parser.consumeToken(Code.BeginNode)
        parser.consumeToken(Code.BeginMapping)
    }

    override fun endStructure(desc: SerialDescriptor) {
        parser.consumeToken(Code.EndMapping)
        parser.consumeToken(Code.EndNode)

        super.endStructure(desc)
    }

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        val nextToken = parser.peekToken(Code.BeginPair, Code.EndMapping)

        return when (nextToken.code) {
            Code.BeginPair -> {
                parser.consumeToken(Code.BeginPair)

                val name = super.decodeString()
                val index = desc.getElementIndex(name)

                parser.consumeToken(Code.Indicator)

                index
            }
            Code.EndMapping -> READ_DONE
            else -> throw IllegalStateException()
        }
    }

    override fun decodeString() = super.decodeString().also { parser.consumeToken(Code.EndPair) }
    override fun decodeByte() = super.decodeByte().also { parser.consumeToken(Code.EndPair) }
    override fun decodeShort() = super.decodeShort().also { parser.consumeToken(Code.EndPair) }
    override fun decodeInt() = super.decodeInt().also { parser.consumeToken(Code.EndPair) }
    override fun decodeLong() = super.decodeLong().also { parser.consumeToken(Code.EndPair) }
    override fun decodeFloat() = super.decodeFloat().also { parser.consumeToken(Code.EndPair) }
    override fun decodeDouble() = super.decodeDouble().also { parser.consumeToken(Code.EndPair) }
    override fun decodeEnum(enumDescription: EnumDescriptor) = super.decodeEnum(enumDescription).also { parser.consumeToken(Code.EndPair) }
    override fun decodeBoolean() = super.decodeBoolean().also { parser.consumeToken(Code.EndPair) }
}
