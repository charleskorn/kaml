/*

   Copyright 2018-2023 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

@file:OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)

package com.charleskorn.kaml

import com.charleskorn.kaml.testobjects.Team
import com.charleskorn.kaml.testobjects.TestEnum
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

class YamlNegativeReadingTest : FlatFunSpec({
    context("a YAML parser") {

        context("parsing values with mismatched types") {
            data class Scenario(
                val description: String,
                val serializer: KSerializer<out Any?>,
                val expectedErrorMessage: String = description,
            )

            @Serializable
            class ComplexStructure(val string: String)

            context("given a list") {

                listOf(
                    Scenario("a string", String.serializer()),
                    Scenario("an integer", Int.serializer()),
                    Scenario("a long", Long.serializer()),
                    Scenario("a short", Short.serializer()),
                    Scenario("a byte", Byte.serializer()),
                    Scenario("a double", Double.serializer()),
                    Scenario("a float", Float.serializer()),
                    Scenario("a boolean", Boolean.serializer()),
                    Scenario("a character", Char.serializer()),
                    Scenario("an enumeration value", TestEnum.serializer()),
                    Scenario("a map", MapSerializer(String.serializer(), String.serializer())),
                    Scenario("an object", ComplexStructure.serializer()),
                    Scenario("a nullable string", String.serializer().nullable, "a string"),
                ).forEach { (description, serializer, expectedErrorMessage) ->
                    val input = "- thing"

                    context("parsing that input as $description") {
                        test("throws an exception with the correct location information") {
                            val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(serializer, input) }

                            exception.asClue {
                                it.message shouldBe "Expected $expectedErrorMessage, but got a list"
                                it.line shouldBe 1
                                it.column shouldBe 1
                                it.path shouldBe YamlPath.root
                            }
                        }
                    }
                }

                context("parsing that input as the value in a map") {
                    val input = """
                        key:
                            - some_value
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'key' is invalid: Expected a string, but got a list"
                            it.line shouldBe 2
                            it.column shouldBe 5
                            it.path shouldBe YamlPath.root.withMapElementKey("key", Location(1, 1)).withMapElementValue(Location(2, 5))
                        }
                    }
                }

                context("parsing that input as the value in an object") {
                    val input = """
                        string:
                            - some_value
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(ComplexStructure.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'string' is invalid: Expected a string, but got a list"
                            it.line shouldBe 2
                            it.column shouldBe 5
                            it.path shouldBe YamlPath.root.withMapElementKey("string", Location(1, 1)).withMapElementValue(Location(2, 5))
                        }
                    }
                }

                context("parsing that input as the value in a list") {
                    val input = """
                        - [ some_value ]
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(ListSerializer(String.serializer()), input) }

                        exception.asClue {
                            it.message shouldBe "Expected a string, but got a list"
                            it.line shouldBe 1
                            it.column shouldBe 3
                            it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3))
                        }
                    }
                }
            }

            context("given a map") {
                listOf(
                    Scenario("a string", String.serializer()),
                    Scenario("an integer", Int.serializer()),
                    Scenario("a long", Long.serializer()),
                    Scenario("a short", Short.serializer()),
                    Scenario("a byte", Byte.serializer()),
                    Scenario("a double", Double.serializer()),
                    Scenario("a float", Float.serializer()),
                    Scenario("a boolean", Boolean.serializer()),
                    Scenario("a character", Char.serializer()),
                    Scenario("an enumeration value", TestEnum.serializer()),
                    Scenario("a list", ListSerializer(String.serializer())),
                    Scenario("a nullable string", String.serializer().nullable, "a string"),
                ).forEach { (description, serializer, expectedErrorMessage) ->
                    val input = "key: value"

                    context("parsing that input as $description") {
                        test("throws an exception with the correct location information") {
                            val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(serializer, input) }

                            exception.asClue {
                                it.message shouldBe "Expected $expectedErrorMessage, but got a map"
                                it.line shouldBe 1
                                it.column shouldBe 1
                                it.path shouldBe YamlPath.root
                            }
                        }
                    }
                }

                context("parsing that input as the value in a map") {
                    val input = """
                        key:
                            some_key: some_value
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'key' is invalid: Expected a string, but got a map"
                            it.line shouldBe 2
                            it.column shouldBe 5
                            it.path shouldBe YamlPath.root.withMapElementKey("key", Location(1, 1)).withMapElementValue(Location(2, 5))
                        }
                    }
                }

                context("parsing that input as the value in an object") {
                    val input = """
                        string:
                            some_key: some_value
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(ComplexStructure.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'string' is invalid: Expected a string, but got a map"
                            it.line shouldBe 2
                            it.column shouldBe 5
                            it.path shouldBe YamlPath.root.withMapElementKey("string", Location(1, 1)).withMapElementValue(Location(2, 5))
                        }
                    }
                }

                context("parsing that input as the value in a list") {
                    val input = """
                        - some_key: some_value
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(ListSerializer(String.serializer()), input) }

                        exception.asClue {
                            it.message shouldBe "Expected a string, but got a map"
                            it.line shouldBe 1
                            it.column shouldBe 3
                            it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3))
                        }
                    }
                }
            }

            context("given a scalar value") {
                mapOf(
                    "a list" to ListSerializer(String.serializer()),
                    "a map" to MapSerializer(String.serializer(), String.serializer()),
                    "an object" to ComplexStructure.serializer(),
                ).forEach { (description, serializer) ->
                    val input = "blah"

                    context("parsing that input as $description") {
                        test("throws an exception with the correct location information") {
                            val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(serializer, input) }

                            exception.asClue {
                                it.message shouldBe "Expected $description, but got a scalar value"
                                it.line shouldBe 1
                                it.column shouldBe 1
                                it.path shouldBe YamlPath.root
                            }
                        }
                    }
                }

                context("parsing that input as the value in a map") {
                    val input = """
                        key: some_value
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(MapSerializer(String.serializer(), ListSerializer(String.serializer())), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'key' is invalid: Expected a list, but got a scalar value"
                            it.line shouldBe 1
                            it.column shouldBe 6
                            it.path shouldBe YamlPath.root.withMapElementKey("key", Location(1, 1)).withMapElementValue(Location(1, 6))
                        }
                    }
                }

                context("parsing that input as the value in an object") {
                    val input = """
                        members: some_value
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(Team.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'members' is invalid: Expected a list, but got a scalar value"
                            it.line shouldBe 1
                            it.column shouldBe 10
                            it.path shouldBe YamlPath.root.withMapElementKey("members", Location(1, 1)).withMapElementValue(Location(1, 10))
                        }
                    }
                }

                context("parsing that input as the value in a list") {
                    val input = """
                        - some_value
                    """.trimIndent()

                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(ListSerializer((ListSerializer(String.serializer()))), input) }

                        exception.asClue {
                            it.message shouldBe "Expected a list, but got a scalar value"
                            it.line shouldBe 1
                            it.column shouldBe 3
                            it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3))
                        }
                    }
                }
            }
        }
    }
})
