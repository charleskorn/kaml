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

open class YAMLInput(val parser: YAMLParser) : ElementValueDecoder() {
    constructor(tokens: Sequence<Token>) : this(YAMLParser(tokens))

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        return when (desc.kind) {
            is StructureKind.LIST -> YAMLListInput(parser)
            is StructureKind.CLASS -> YAMLObjectInput(parser)
            else -> throw IllegalStateException("Unknown structure kind: ${desc.kind}")
        }
    }

    override fun decodeString(): String {
        parser.readToken(Code.BeginNode)
        parser.readToken(Code.BeginScalar)

        val firstToken = parser.peekToken(Code.Indicator, Code.Text)

        val text = when (firstToken.code) {
            Code.Indicator -> {
                val quoteChar = parser.readToken(Code.Indicator).text.toString()
                val text = readText()
                val endQuoteToken = parser.readAnyToken()

                if (endQuoteToken.code != Code.Indicator || endQuoteToken.text.toString() != quoteChar) {
                    throw YAMLException("Missing trailing $quoteChar", endQuoteToken)
                }

                text
            }
            Code.Text -> readText()
            else -> throw IllegalStateException()
        }

        parser.readToken(Code.EndScalar)
        parser.readToken(Code.EndNode)
        return text
    }

    private fun readText(): String {
        val builder = StringBuilder()

        while (true) {
            val nextToken = parser.peekToken(Code.Text, Code.BeginEscape, Code.Indicator, Code.EndScalar)

            when (nextToken.code) {
                Code.Text -> builder.append(parser.readToken(Code.Text).text.toString())
                Code.BeginEscape -> {
                    parser.readToken(Code.BeginEscape)
                    parser.readToken(Code.Indicator)
                    val escapedCharacter = parser.readToken(Code.Meta).text.toString()
                    builder.append(escapedCharacter)
                    parser.readToken(Code.EndEscape)
                }
                Code.Indicator, Code.EndScalar -> return builder.toString()
                else -> throw IllegalStateException()
            }
        }
    }

    override fun decodeByte() = readIntegerLikeValue(String::toByte, "byte")
    override fun decodeShort() = readIntegerLikeValue(String::toShort, "short")
    override fun decodeInt() = readIntegerLikeValue(String::toInt, "integer")
    override fun decodeLong() = readIntegerLikeValue(String::toLong, "long")

    private fun <T> readIntegerLikeValue(converter: (String, Int) -> T, description: String): T {
        val token = readSimpleScalar()
        val text = token.text.toString()

        try {
            return when {
                text.startsWith("0x") -> converter(text.substring(2), 16)
                text.startsWith("-0x") -> converter("-" + text.substring(3), 16)
                text.startsWith("0o") -> converter(text.substring(2), 8)
                text.startsWith("-0o") -> converter("-" + text.substring(3), 8)
                else -> converter(text, 10)
            }
        } catch (e: NumberFormatException) {
            throw YAMLException("Value '$text' is not a valid $description value.", token)
        }
    }

    override fun decodeFloat(): Float {
        val token = readSimpleScalar()
        val text = token.text.toString()

        return when (text) {
            ".inf", ".Inf", ".INF" -> Float.POSITIVE_INFINITY
            "-.inf", "-.Inf", "-.INF" -> Float.NEGATIVE_INFINITY
            ".nan", ".NaN", ".NAN" -> Float.NaN
            else -> try {
                text.toFloat()
            } catch (e: NumberFormatException) {
                throw YAMLException("Value '$text' is not a valid floating point value.", token)
            }
        }
    }

    override fun decodeDouble(): Double {
        val token = readSimpleScalar()
        val text = token.text.toString()

        return when (text) {
            ".inf", ".Inf", ".INF" -> Double.POSITIVE_INFINITY
            "-.inf", "-.Inf", "-.INF" -> Double.NEGATIVE_INFINITY
            ".nan", ".NaN", ".NAN" -> Double.NaN
            else -> try {
                text.toDouble()
            } catch (e: NumberFormatException) {
                throw YAMLException("Value '$text' is not a valid floating point value.", token)
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

            throw YAMLException("Value '$value' is not a valid option, permitted choices are: $choices", token)
        }

        return index
    }

    override fun decodeBoolean(): Boolean {
        val token = readSimpleScalar()
        val value = token.text.toString()

        return when (value) {
            "true", "True", "TRUE" -> true
            "false", "False", "FALSE" -> false
            else -> throw YAMLException(
                "Value '$value' is not a valid boolean, permitted choices are: true or false",
                token
            )
        }
    }

    override fun decodeChar(): Char {
        val token = parser.peekAnyToken()
        val value = decodeString()
        return value.singleOrNull() ?: throw YAMLException("Value '$value' is not a valid character value.", token)
    }

    private fun readSimpleScalar(): Token {
        parser.readToken(Code.BeginNode)
        parser.readToken(Code.BeginScalar)
        val text = parser.readToken(Code.Text)
        parser.readToken(Code.EndScalar)
        parser.readToken(Code.EndNode)

        return text
    }

    // TODO: UpdateMode - probably should be BANNED
}

private class YAMLListInput(parser: YAMLParser) : YAMLInput(parser) {
    private var nextElementIndex = 0

    init {
        parser.readToken(Code.BeginNode)
        parser.readToken(Code.BeginSequence)
    }

    override fun endStructure(desc: SerialDescriptor) {
        parser.readToken(Code.EndSequence)
        parser.readToken(Code.EndNode)

        super.endStructure(desc)
    }

    override fun decodeCollectionSize(desc: SerialDescriptor): Int {
        return -1
    }

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        val nextToken = parser.peekToken(Code.Indicator, Code.EndSequence)

        when (nextToken.code) {
            Code.Indicator -> {
                parser.readToken(Code.Indicator)

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

private class YAMLObjectInput(parser: YAMLParser) : YAMLInput(parser) {
    init {
        parser.readToken(Code.BeginNode)
        parser.readToken(Code.BeginMapping)
    }

    override fun endStructure(desc: SerialDescriptor) {
        parser.readToken(Code.EndMapping)
        parser.readToken(Code.EndNode)

        super.endStructure(desc)
    }

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        val nextToken = parser.peekToken(Code.BeginPair, Code.EndMapping)

        return when (nextToken.code) {
            Code.BeginPair -> {
                parser.readToken(Code.BeginPair)

                val name = super.decodeString()
                val index = desc.getElementIndex(name)

                parser.readToken(Code.Indicator)

                index
            }
            Code.EndMapping -> READ_DONE
            else -> throw IllegalStateException()
        }
    }

    override fun decodeString() = super.decodeString().also { parser.readToken(Code.EndPair) }
    override fun decodeByte() = super.decodeByte().also { parser.readToken(Code.EndPair) }
    override fun decodeShort() = super.decodeShort().also { parser.readToken(Code.EndPair) }
    override fun decodeInt() = super.decodeInt().also { parser.readToken(Code.EndPair) }
    override fun decodeLong() = super.decodeLong().also { parser.readToken(Code.EndPair) }
    override fun decodeFloat() = super.decodeFloat().also { parser.readToken(Code.EndPair) }
    override fun decodeDouble() = super.decodeDouble().also { parser.readToken(Code.EndPair) }
    override fun decodeEnum(enumDescription: EnumDescriptor) = super.decodeEnum(enumDescription).also { parser.readToken(Code.EndPair) }
    override fun decodeBoolean() = super.decodeBoolean().also { parser.readToken(Code.EndPair) }
}
