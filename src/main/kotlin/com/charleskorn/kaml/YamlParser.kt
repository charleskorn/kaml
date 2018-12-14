package com.charleskorn.kaml

import io.dahgan.parser.Code
import io.dahgan.parser.Token
import io.dahgan.yaml

class YamlParser(tokens: Sequence<Token>) {
    constructor(yamlSource: String) : this(
        yaml().tokenize(
            "some-name-that-should-probably-be-the-filename",
            yamlSource.toByteArray(),
            false
        )
    )

    // TODO: this isn't very efficient (we read the entire structure into memory rather than streaming it)
    // but it makes things quite a bit easier
    private val tokens = tokens
        .filterNot { it.code in setOf(Code.White, Code.Break, Code.Indent) }
        .toList()

    private var nextTokenIndex = 0

    private val unsupportedFeatures = mapOf(
        Code.BeginDirective to "directives",
        Code.BeginAlias to "aliases",
        Code.BeginAnchor to "anchors",
        Code.BeginTag to "tags"
    )

    init {
        tokens.forEach { it ->
            if (it.code == Code.Unparsed || (it.code == Code.Error && it.text.toString() == "Expected start of line")) {
                throw YamlException("Invalid YAML. The level of indentation at this point or nearby may be incorrect.", it)
            }

            if (it.code == Code.Error) {
                throw YamlException(it.text.toString(), it)
            }

            if (it.code in unsupportedFeatures.keys) {
                throw YamlException("Unsupported YAML feature: ${unsupportedFeatures[it.code]}", it)
            }
        }

        if (isEOF) {
            throw YamlException("The YAML document is empty.", Location(1, 1))
        }

        consumeToken(Code.BeginDocument)
    }

    private val isEOF: Boolean
        get() {
            skipAnyComments()

            return nextTokenIndex == tokens.size
        }

    private fun skipAnyComments() {
        while (nextTokenIndex < tokens.size && tokens[nextTokenIndex].code == Code.BeginComment) {
            do {
                nextTokenIndex++
            } while (tokens[nextTokenIndex - 1].code != Code.EndComment)
        }
    }

    fun peekAnyToken(): Token {
        skipAnyComments()

        return tokens[nextTokenIndex]
    }

    fun peekToken(vararg expectedTypes: Code): Token {
        if (expectedTypes.isEmpty()) {
            throw IllegalStateException("Called peekToken() with no expected token types")
        }

        val nextToken = peekAnyToken()

        if (nextToken.code !in expectedTypes) {
            val explanation = if (expectedTypes.size == 1) {
                "expected ${expectedTypes.single().name}"
            } else {
                val types = expectedTypes.map { it.name }.joinToString(", ")
                "expected one of $types"
            }

            throw YamlException("Unexpected ${nextToken.code.name}, $explanation", nextToken)
        }

        return nextToken
    }

    fun consumeAnyToken(): Token = peekAnyToken().also { nextTokenIndex++ }
    fun consumeToken(vararg expectedTypes: Code): Token = peekToken(*expectedTypes).also { nextTokenIndex++ }
}
