import io.dahgan.parser.Code
import io.dahgan.parser.Escapable
import io.dahgan.parser.Token

sealed class YamlNode(open val location: Location) {
    abstract fun equivalentContentTo(other: YamlNode): Boolean
    abstract fun contentToString(): String

    companion object {
        fun fromParser(parser: YamlParser): YamlNode {
            return parser.readWrapped(Code.BeginNode, Code.EndNode) {
                val nextToken = parser.peekToken(Code.BeginScalar, Code.BeginSequence, Code.BeginMapping)
                val location = locationFrom(nextToken)

                when (nextToken.code) {
                    Code.BeginScalar -> readScalarOrNull(parser, location)
                    Code.BeginSequence -> readSequence(parser, location)
                    Code.BeginMapping -> readMapping(parser, location)
                    else -> throw IllegalStateException("Unexpected ${nextToken.code.name}")
                }
            }
        }

        private fun readScalarOrNull(parser: YamlParser, location: Location): YamlNode {
            return parser.readWrapped(Code.BeginScalar, Code.EndScalar) {
                val firstToken = parser.peekToken(Code.Indicator, Code.Text, Code.EndScalar)

                when (firstToken.code) {
                    Code.Indicator -> {
                        when (firstToken.decodeText()) {
                            "\"", "'" -> {
                                val quoteChar = parser.consumeToken(Code.Indicator).decodeText()
                                val text = readText(parser)
                                val endQuoteToken = parser.consumeAnyToken()

                                if (endQuoteToken.code != Code.Indicator || endQuoteToken.decodeText() != quoteChar) {
                                    throw YamlException("Missing trailing $quoteChar", endQuoteToken)
                                }

                                YamlScalar(text, location)
                            }
                            ">", "|" -> {
                                // Consume all the indicators - folded / literal, plus optional chomping style and optional indentation indicators
                                // (the YAML tokeniser takes care of the details of all of these for us)
                                while (parser.peekAnyToken().code == Code.Indicator) {
                                    parser.consumeToken(Code.Indicator)
                                }

                                YamlScalar(readText(parser), location)
                            }
                            else -> throw YamlException("Unexpected '${firstToken.decodeText()}'", firstToken)
                        }
                    }
                    Code.Text -> YamlScalar(readText(parser), location)
                    Code.EndScalar -> YamlNull(location)
                    else -> throw IllegalStateException()
                }
            }
        }

        private fun readText(parser: YamlParser): String {
            val builder = StringBuilder()

            while (true) {
                val nextToken = parser.peekToken(Code.Text, Code.LineFold, Code.LineFeed, Code.BeginEscape, Code.Indicator, Code.EndScalar)

                when (nextToken.code) {
                    Code.Text -> builder.append(parser.consumeToken(Code.Text).decodeText())
                    Code.LineFold -> {
                        parser.consumeToken(Code.LineFold)
                        builder.append(" ")
                    }
                    Code.LineFeed -> {
                        parser.consumeToken(Code.LineFeed)
                        builder.appendln()
                    }
                    Code.BeginEscape -> builder.append(readEscape(parser))
                    Code.Indicator, Code.EndScalar -> return builder.toString()
                    else -> throw IllegalStateException()
                }
            }
        }

        // Reference: http://yaml.org/spec/1.2/spec.html#escaping/in%20double-quoted%20scalars/
        private val escapingMap = mapOf(
            "0" to "\u0000",
            "a" to "\u0007",
            "b" to "\u0008",
            "t" to "\t",
            "\t" to "\t",
            "n" to "\n",
            "v" to "\u000B",
            "f" to "\u000C",
            "r" to "\r",
            "e" to "\u001B",
            "\u0020" to "\u0020",
            "\"" to "\"",
            "/" to "/",
            "\\" to "\\",
            "N" to "\u0085",
            "_" to "\u00A0",
            "L" to "\u2028",
            "P" to "\u2029"
        )

        private fun readEscape(parser: YamlParser): String {
            return parser.readWrapped(Code.BeginEscape, Code.EndEscape) {
                val indicator = parser.consumeToken(Code.Indicator)

                when (indicator.decodeText()) {
                    "\\" -> {
                        val nextToken = parser.consumeToken(Code.Meta, Code.Indicator)
                        val nextTokenText = nextToken.decodeText()

                        when (nextToken.code) {
                            Code.Meta -> escapingMap.get(nextTokenText)!! // The tokeniser will generate an error if the escape sequence is invalid
                            Code.Indicator -> {
                                val hexValue = parser.consumeToken(Code.Meta).decodeText()
                                hexValue.toInt(16).toChar().toString()
                            }
                            else -> throw IllegalStateException()
                        }
                    }
                    "'" -> parser.consumeToken(Code.Meta).decodeText()
                    else -> {
                        throw YamlException("Unexpected escape character '${indicator.text}'.", indicator)
                    }
                }
            }
        }

        private fun readSequence(parser: YamlParser, location: Location): YamlList {
            return parser.readWrapped(Code.BeginSequence, Code.EndSequence) {
                val firstToken = parser.peekToken(Code.Indicator, Code.EndSequence)

                if (firstToken.code == Code.Indicator && firstToken.decodeText() == "[") {
                    parser.consumeToken(Code.Indicator)

                    readList(parser, location).also {
                        readIndicator(parser, "]")
                    }
                } else {
                    readList(parser, location)
                }
            }
        }

        private fun readList(parser: YamlParser, location: Location): YamlList {
            val items = mutableListOf<YamlNode>()

            while (true) {
                val nextToken = parser.peekToken(Code.Indicator, Code.BeginNode, Code.EndSequence)

                when (nextToken.code) {
                    Code.Indicator -> when (nextToken.decodeText()) {
                        "]" -> return YamlList(items, location)
                        "-", "," -> {
                            parser.consumeToken(Code.Indicator)
                            items.add(fromParser(parser))
                        }
                        else -> throw YamlException("Unexpected '${nextToken.text}'", nextToken)
                    }
                    Code.BeginNode -> items.add(fromParser(parser))
                    Code.EndSequence -> return YamlList(items, location)
                    else -> throw IllegalStateException()
                }
            }
        }

        private fun readMapping(parser: YamlParser, location: Location): YamlMap {
            return parser.readWrapped(Code.BeginMapping, Code.EndMapping) {
                val firstToken = parser.peekToken(Code.Indicator, Code.BeginPair)

                if (firstToken.code == Code.Indicator && firstToken.decodeText() == "{") {
                    parser.consumeToken(Code.Indicator)

                    readMap(parser, location).also {
                        readIndicator(parser, "}")
                    }
                } else {
                    readMap(parser, location)
                }
            }
        }

        private fun readMap(parser: YamlParser, location: Location): YamlMap {
            val items = mutableMapOf<YamlNode, YamlNode>()

            while (true) {
                val nextToken = parser.peekToken(Code.BeginPair, Code.EndMapping, Code.Indicator)

                when (nextToken.code) {
                    Code.BeginPair -> {
                        val pair = readPair(parser)
                        items.put(pair.first, pair.second)
                    }
                    Code.EndMapping -> return YamlMap(items, location)
                    Code.Indicator -> when (nextToken.decodeText()) {
                        "}" -> return YamlMap(items, location)
                        "," -> {
                            parser.consumeToken(Code.Indicator)
                            val pair = readPair(parser)
                            items.put(pair.first, pair.second)
                        }
                        else -> throw YamlException("Unexpected '${nextToken.text}'", nextToken)
                    }
                    else -> throw IllegalStateException()
                }
            }
        }

        private fun readPair(parser: YamlParser): Pair<YamlNode, YamlNode> {
            return parser.readWrapped(Code.BeginPair, Code.EndPair) {
                val key = fromParser(parser)
                readIndicator(parser, ":")
                val value = fromParser(parser)

                key to value
            }
        }

        private fun readIndicator(parser: YamlParser, expected: String) {
            val indicator = parser.consumeToken(Code.Indicator)
            val actual = indicator.decodeText()

            if (actual != expected) {
                throw YamlException("Expected '$expected', but got '$actual'", indicator)
            }
        }

        private fun Token.decodeText(): String = when (this.text) {
            is Escapable.Code -> (this.text as Escapable.Code).codes.map { it.toChar() }.joinToString("")
            is Escapable.Text -> this.text.toString()
        }

        private fun <T> YamlParser.readWrapped(
            expectedFirstNode: Code,
            expectedLastNode: Code,
            reader: () -> T
        ): T {
            this.consumeToken(expectedFirstNode)

            return reader().also {
                this.consumeToken(expectedLastNode)
            }
        }

        private fun locationFrom(token: Token): Location = Location(token.line, token.lineChar + 1)
    }
}

data class YamlScalar(val content: String, override val location: Location) : YamlNode(location) {
    override fun equivalentContentTo(other: YamlNode): Boolean = other is YamlScalar && this.content == other.content
    override fun contentToString(): String = "'$content'"

    fun toByte() = convertToIntegerLikeValue(String::toByte, "byte")
    fun toShort() = convertToIntegerLikeValue(String::toShort, "short")
    fun toInt() = convertToIntegerLikeValue(String::toInt, "integer")
    fun toLong() = convertToIntegerLikeValue(String::toLong, "long")

    private fun <T> convertToIntegerLikeValue(converter: (String, Int) -> T, description: String): T {
        try {
            return when {
                content.startsWith("0x") -> converter(content.substring(2), 16)
                content.startsWith("-0x") -> converter("-" + content.substring(3), 16)
                content.startsWith("0o") -> converter(content.substring(2), 8)
                content.startsWith("-0o") -> converter("-" + content.substring(3), 8)
                else -> converter(content, 10)
            }
        } catch (e: NumberFormatException) {
            throw YamlException("Value '$content' is not a valid $description value.", location)
        }
    }

    fun toFloat(): Float {
        return when (content) {
            ".inf", ".Inf", ".INF" -> Float.POSITIVE_INFINITY
            "-.inf", "-.Inf", "-.INF" -> Float.NEGATIVE_INFINITY
            ".nan", ".NaN", ".NAN" -> Float.NaN
            else -> try {
                content.toFloat()
            } catch (e: NumberFormatException) {
                throw YamlException("Value '$content' is not a valid floating point value.", location)
            }
        }
    }

    fun toDouble(): Double {
        return when (content) {
            ".inf", ".Inf", ".INF" -> Double.POSITIVE_INFINITY
            "-.inf", "-.Inf", "-.INF" -> Double.NEGATIVE_INFINITY
            ".nan", ".NaN", ".NAN" -> Double.NaN
            else -> try {
                content.toDouble()
            } catch (e: NumberFormatException) {
                throw YamlException("Value '$content' is not a valid floating point value.", location)
            }
        }
    }

    fun toBoolean(): Boolean {
        return when (content) {
            "true", "True", "TRUE" -> true
            "false", "False", "FALSE" -> false
            else -> throw YamlException(
                "Value '$content' is not a valid boolean, permitted choices are: true or false",
                location
            )
        }
    }

    fun toChar(): Char =
        content.singleOrNull() ?: throw YamlException("Value '$content' is not a valid character value.", location)
}

data class YamlNull(override val location: Location) : YamlNode(location) {
    override fun equivalentContentTo(other: YamlNode): Boolean = other is YamlNull
    override fun contentToString(): String = "null"
}

data class YamlList(val items: List<YamlNode>, override val location: Location) : YamlNode(location) {
    override fun equivalentContentTo(other: YamlNode): Boolean {
        if (other !is YamlList) {
            return false
        }

        if (this.items.size != other.items.size) {
            return false
        }

        return this.items.zip(other.items).all { (mine, theirs) -> mine.equivalentContentTo(theirs) }
    }

    override fun contentToString(): String = "[" + items.joinToString(", ") { it.contentToString() } + "]"
}

data class YamlMap(val entries: Map<YamlNode, YamlNode>, override val location: Location) : YamlNode(location) {
    init {
        val keys = entries.keys.sortedWith(Comparator { a, b ->
            val lineComparison = a.location.line.compareTo(b.location.line)

            if (lineComparison != 0) {
                lineComparison
            } else {
                a.location.column.compareTo(b.location.column)
            }
        })

        keys.forEachIndexed { index, key ->
            val duplicate = keys.subList(0, index).firstOrNull { it.equivalentContentTo(key) }

            if (duplicate != null) {
                throw YamlException("Duplicate key ${key.contentToString()}. It was previously given at line ${duplicate.location.line}, column ${duplicate.location.column}.", key.location)
            }
        }
    }

    override fun equivalentContentTo(other: YamlNode): Boolean {
        if (other !is YamlMap) {
            return false
        }

        if (this.entries.size != other.entries.size) {
            return false
        }

        return this.entries.all { (thisKey, thisValue) ->
            other.entries.any { it.key.equivalentContentTo(thisKey) && it.value.equivalentContentTo(thisValue) }
        }
    }

    override fun contentToString(): String = "{" + entries.map { (key, value) -> "${key.contentToString()}: ${value.contentToString()}" }.joinToString(", ") + "}"
}

