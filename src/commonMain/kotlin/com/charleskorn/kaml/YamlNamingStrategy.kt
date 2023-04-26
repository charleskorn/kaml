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

public fun interface YamlNamingStrategy {
    public fun serialNameForYaml(serialName: String): String

    public companion object Builtins {
        public val SnakeCase: YamlNamingStrategy = object : YamlNamingStrategy {
            override fun serialNameForYaml(serialName: String): String = serialName.toDelimitedCase('_')

            override fun toString(): String = "com.charleskorn.kaml.YamlNamingStrategy.SnakeCase"
        }

        public val KebabCase: YamlNamingStrategy = object : YamlNamingStrategy {
            override fun serialNameForYaml(serialName: String): String = serialName.toDelimitedCase('-')

            override fun toString(): String = "com.charleskorn.kaml.YamlNamingStrategy.KebabCase"
        }

        public val PascalCase: YamlNamingStrategy = object : YamlNamingStrategy {
            override fun serialNameForYaml(serialName: String): String = serialName
                .split("[^a-zA-Z0-9]+".toRegex())
                .joinToString("") { it.replaceFirstChar(Char::titlecaseChar) }

            override fun toString(): String = "com.charleskorn.kaml.YamlNamingStrategy.PascalCase"
        }

        public val CamelCase: YamlNamingStrategy = object : YamlNamingStrategy {
            override fun serialNameForYaml(serialName: String): String = PascalCase
                .serialNameForYaml(serialName)
                .replaceFirstChar(Char::lowercaseChar)

            override fun toString(): String = "com.charleskorn.kaml.YamlNamingStrategy.CamelCase"
        }

        private fun String.toDelimitedCase(delimiter: Char): String = buildString(length * 2) {
            var bufferedChar: Char? = null
            var previousCaseCharsCount = 0

            for (character in this@toDelimitedCase) {
                if (character.isUpperCase()) {
                    if (previousCaseCharsCount == 0 && isNotEmpty() && last() != delimiter) {
                        append(delimiter)
                    }

                    bufferedChar?.let(::append)

                    previousCaseCharsCount++
                    bufferedChar = character.lowercaseChar()
                } else {
                    if (bufferedChar != null) {
                        if (previousCaseCharsCount > 1 && character.isLetter()) {
                            append(delimiter)
                        }
                        append(bufferedChar)
                        previousCaseCharsCount = 0
                        bufferedChar = null
                    }
                    append(character)
                }
            }

            if (bufferedChar != null) {
                append(bufferedChar)
            }
        }
    }
}
