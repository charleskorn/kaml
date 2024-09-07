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

package com.charleskorn.kaml

import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.TestEnum
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

class YamlListReadingTest : FlatFunSpec({
    context("a YAML parser parsing lists") {
        context("given a list of strings") {
            val input = """
                    - thing1
                    - thing2
                    - thing3
            """.trimIndent()

            context("parsing that input as a list") {
                val result = Yaml.default.decodeFromString(ListSerializer(String.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf("thing1", "thing2", "thing3")
                }
            }

            context("parsing that input as a nullable list") {
                val result = Yaml.default.decodeFromString(ListSerializer(String.serializer()).nullable, input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf("thing1", "thing2", "thing3")
                }
            }

            context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                test("throws an exception with the correct location information") {
                    val exception = shouldThrow<LocationInformationException> {
                        Yaml.default.decodeFromString(
                            ListSerializer(LocationThrowingSerializer),
                            input,
                        )
                    }

                    exception.asClue {
                        it.message shouldBe "Serializer called with location (1, 3) and path: [0]"
                    }
                }
            }
        }

        context("given a list of numbers") {
            val input = """
                    - 123
                    - 45
                    - 6
            """.trimIndent()

            context("parsing that input as a list of integers") {
                val result = Yaml.default.decodeFromString(ListSerializer(Int.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf(123, 45, 6)
                }
            }

            context("parsing that input as a list of longs") {
                val result = Yaml.default.decodeFromString(ListSerializer(Long.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf(123L, 45, 6)
                }
            }

            context("parsing that input as a list of shorts") {
                val result = Yaml.default.decodeFromString(ListSerializer(Short.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf(123.toShort(), 45, 6)
                }
            }

            context("parsing that input as a list of bytes") {
                val result = Yaml.default.decodeFromString(ListSerializer(Byte.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf(123.toByte(), 45, 6)
                }
            }

            context("parsing that input as a list of doubles") {
                val result = Yaml.default.decodeFromString(ListSerializer(Double.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf(123.0, 45.0, 6.0)
                }
            }

            context("parsing that input as a list of floats") {
                val result = Yaml.default.decodeFromString(ListSerializer(Float.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf(123.0f, 45.0f, 6.0f)
                }
            }
        }

        context("given a list of booleans") {
            val input = """
                    - true
                    - false
            """.trimIndent()

            context("parsing that input as a list") {
                val result = Yaml.default.decodeFromString(ListSerializer(Boolean.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf(true, false)
                }
            }
        }

        context("given a list of enum values") {
            val input = """
                    - Value1
                    - Value2
            """.trimIndent()

            context("parsing that input as a list") {
                val result = Yaml.default.decodeFromString(ListSerializer(TestEnum.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf(TestEnum.Value1, TestEnum.Value2)
                }
            }
        }

        context("given a list of characters") {
            val input = """
                    - a
                    - b
            """.trimIndent()

            context("parsing that input as a list") {
                val result = Yaml.default.decodeFromString(ListSerializer(Char.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf('a', 'b')
                }
            }
        }

        context("given a list of nullable strings") {
            val input = """
                    - thing1
                    - null
            """.trimIndent()

            context("parsing that input as a list") {
                val result = Yaml.default.decodeFromString(ListSerializer(String.serializer().nullable), input)

                test("deserializes it to the expected value") {
                    result shouldBe listOf("thing1", null)
                }
            }
        }

        context("given a list of lists") {
            val input = """
                    - [thing1, thing2]
                    - [thing3]
            """.trimIndent()

            context("parsing that input as a list") {
                val result = Yaml.default.decodeFromString(ListSerializer(ListSerializer(String.serializer())), input)

                test("deserializes it to the expected value") {
                    result shouldBe
                        listOf(
                            listOf("thing1", "thing2"),
                            listOf("thing3"),
                        )
                }
            }
        }

        context("given a list of objects") {
            val input = """
                    - name: thing1
                    - name: thing2
            """.trimIndent()

            context("parsing that input as a list") {
                val result = Yaml.default.decodeFromString(ListSerializer(SimpleStructure.serializer()), input)

                test("deserializes it to the expected value") {
                    result shouldBe
                        listOf(
                            SimpleStructure("thing1"),
                            SimpleStructure("thing2"),
                        )
                }
            }
        }
    }
})
