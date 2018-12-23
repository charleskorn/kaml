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
                throw MalformedYamlException("Invalid YAML. The level of indentation at this point or nearby may be incorrect.", it)
            }

            if (it.code == Code.Error) {
                throw MalformedYamlException(it.text.toString(), it)
            }

            if (it.code in unsupportedFeatures.keys) {
                throw UnsupportedYamlFeatureException(unsupportedFeatures.getValue(it.code), it)
            }
        }

        if (isEOF) {
            throw EmptyYamlDocumentException("The YAML document is empty.", Location(1, 1))
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
