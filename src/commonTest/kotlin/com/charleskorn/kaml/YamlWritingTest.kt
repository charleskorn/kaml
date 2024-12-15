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

import com.charleskorn.kaml.testobjects.NestedObjects
import com.charleskorn.kaml.testobjects.SealedWrapper
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.Team
import com.charleskorn.kaml.testobjects.TestClassWithNestedList
import com.charleskorn.kaml.testobjects.TestClassWithNestedMap
import com.charleskorn.kaml.testobjects.TestClassWithNestedNode
import com.charleskorn.kaml.testobjects.TestClassWithNestedScalar
import com.charleskorn.kaml.testobjects.TestClassWithNestedTaggedNode
import com.charleskorn.kaml.testobjects.TestEnum
import com.charleskorn.kaml.testobjects.TestSealedStructure
import com.charleskorn.kaml.testobjects.UnsealedClass
import com.charleskorn.kaml.testobjects.UnsealedString
import com.charleskorn.kaml.testobjects.UnwrappedInterface
import com.charleskorn.kaml.testobjects.UnwrappedString
import com.charleskorn.kaml.testobjects.polymorphicModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

class YamlWritingTest : FlatFunSpec({
    context("a YAML serializer") {
        val yamlWithCustomisedIndentation = Yaml(configuration = YamlConfiguration(encodingIndentationSize = 3))

        context("serializing null values") {
            val input = null as String?

            context("serializing a null string value") {
                val output = Yaml.default.encodeToString(String.serializer().nullable, input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe "null"
                }
            }
        }

        context("serializing boolean values") {
            context("serializing a true value") {
                val output = Yaml.default.encodeToString(Boolean.serializer(), true)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe "true"
                }
            }

            context("serializing a false value") {
                val output = Yaml.default.encodeToString(Boolean.serializer(), false)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe "false"
                }
            }
        }

        context("serializing byte values") {
            val output = Yaml.default.encodeToString(Byte.serializer(), 12)

            test("returns the value serialized in the expected YAML form") {
                output shouldBe "12"
            }
        }

        context("serializing character values") {
            context("serializing a alphanumeric character") {
                val output = Yaml.default.encodeToString(Char.serializer(), 'A')

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe """"A""""
                }
            }

            context("serializing a double-quote character") {
                val output = Yaml.default.encodeToString(Char.serializer(), '"')

                test("returns the value serialized in the expected YAML form, escaping the double-quote character") {
                    output shouldBe """"\"""""
                }
            }

            context("serializing a newline character") {
                val output = Yaml.default.encodeToString(Char.serializer(), '\n')

                test("returns the value serialized in the expected YAML form, escaping the newline character") {
                    output shouldBe """"\n""""
                }
            }
        }

        context("serializing double values") {
            val output = Yaml.default.encodeToString(Double.serializer(), 12.3)

            test("returns the value serialized in the expected YAML form") {
                output shouldBe "12.3"
            }
        }

        context("serializing floating point values") {
            val output = Yaml.default.encodeToString(Float.serializer(), 45.6f)

            test("returns the value serialized in the expected YAML form") {
                output shouldBe when (kotlinTarget) {
                    // See a bug in Kotlin/Wasm:
                    // https://youtrack.jetbrains.com/issue/KT-68948/
                    KotlinTarget.WASM -> "45.599998474121094"
                    else -> "45.6"
                }
            }
        }

        context("serializing integer values") {
            val output = Yaml.default.encodeToString(Int.serializer(), 12)

            test("returns the value serialized in the expected YAML form") {
                output shouldBe "12"
            }
        }

        context("serializing long integer values") {
            val output = Yaml.default.encodeToString(Long.serializer(), 12)

            test("returns the value serialized in the expected YAML form") {
                output shouldBe "12"
            }
        }

        context("serializing short integer values") {
            val output = Yaml.default.encodeToString(Short.serializer(), 12)

            test("returns the value serialized in the expected YAML form") {
                output shouldBe "12"
            }
        }

        context("serializing string values") {
            context("serializing a string without any special characters") {
                val output = Yaml.default.encodeToString(String.serializer(), "hello world")

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe """"hello world""""
                }
            }

            context("serializing an empty string") {
                val output = Yaml.default.encodeToString(String.serializer(), "")

                // The '---' is necessary as explained here: https://bitbucket.org/asomov/snakeyaml-engine/issues/23/emitting-only-an-empty-string-adds-to
                test("returns the value serialized in the expected YAML form") {
                    output shouldBe """--- """""
                }
            }

            context("serializing the string 'null'") {
                val output = Yaml.default.encodeToString(String.serializer(), "null")

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe """"null""""
                }
            }

            context("serializing a multi-line string") {
                val output = Yaml.default.encodeToString(String.serializer(), "This is line 1\nThis is line 2")

                test("returns the value serialized in the expected YAML form, escaping the newline character") {
                    output shouldBe """"This is line 1\nThis is line 2""""
                }
            }

            context("serializing a string containing a double-quote character") {
                val output = Yaml.default.encodeToString(String.serializer(), """They said "hello" to me""")

                test("returns the value serialized in the expected YAML form, escaping the double-quote characters") {
                    output shouldBe """"They said \"hello\" to me""""
                }
            }

            context("serializing a string longer than the maximum scalar width") {
                val output = Yaml(configuration = YamlConfiguration(breakScalarsAt = 80)).encodeToString(String.serializer(), "Hello world this is a string that is much, much, much (ok, not that much) longer than 80 characters")

                test("returns the value serialized in the expected YAML form, broken onto a new line at the maximum scalar width") {
                    output shouldBe
                        """
                        |"Hello world this is a string that is much, much, much (ok, not that much) longer\
                        |  \ than 80 characters"
                        """.trimMargin()
                }
            }

            context("serializing a string with the value of an integer using SingleLineStringStyle.PlainExceptAmbiguous") {
                val output = Yaml(configuration = YamlConfiguration(singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous)).encodeToString(String.serializer(), "12")

                test("returns the value serialized in the expected YAML form, escaping the integer") {
                    output shouldBe """"12""""
                }
            }

            context("serializing a string with the value of a boolean using SingleLineStringStyle.PlainExceptAmbiguous") {
                val output = Yaml(configuration = YamlConfiguration(singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous)).encodeToString(String.serializer(), "true")

                test("returns the value serialized in the expected YAML form, escaping the boolean") {
                    output shouldBe """"true""""
                }
            }

            context("serializing a string with the value of an float using SingleLineStringStyle.PlainExceptAmbiguous") {
                val output = Yaml(configuration = YamlConfiguration(singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous)).encodeToString(String.serializer(), "1.2")

                test("returns the value serialized in the expected YAML form, escaping the float") {
                    output shouldBe """"1.2""""
                }
            }

            context("serializing an unambiguous numerical string using SingleLineStringStyle.PlainExceptAmbiguous") {
                val output = Yaml(configuration = YamlConfiguration(singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous)).encodeToString(String.serializer(), "1.2.3")

                test("returns the value serialized in the expected YAML form, without being escaped") {
                    output shouldBe "1.2.3"
                }
            }

            context("serializing an int using SingleLineStringStyle.PlainExceptAmbiguous") {
                val output = Yaml(configuration = YamlConfiguration(singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous)).encodeToString(Int.serializer(), 123)

                test("returns the value serialized in the expected YAML form, without being escaped") {
                    output shouldBe "123"
                }
            }

            context("serializing a float using SingleLineStringStyle.PlainExceptAmbiguous") {
                val output = Yaml(configuration = YamlConfiguration(singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous)).encodeToString(Float.serializer(), 1.2f)

                test("returns the value serialized in the expected YAML form, without being escaped") {
                    output shouldBe when (kotlinTarget) {
                        // See a bug in Kotlin/Wasm:
                        // https://youtrack.jetbrains.com/issue/KT-68948/
                        KotlinTarget.WASM -> "1.2000000476837158"
                        else -> "1.2"
                    }
                }
            }

            context("serializing a boolean using SingleLineStringStyle.PlainExceptAmbiguous") {
                val output = Yaml(configuration = YamlConfiguration(singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous)).encodeToString(Boolean.serializer(), true)

                test("returns the value serialized in the expected YAML form, without being escaped") {
                    output shouldBe "true"
                }
            }

            context("serializing a string with the value of an integer using SingleLineStringStyle.PlainExceptAmbiguous, escaping with single-quotes") {
                val output = Yaml(
                    configuration = YamlConfiguration(
                        singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous,
                        ambiguousQuoteStyle = AmbiguousQuoteStyle.SingleQuoted,
                    ),
                ).encodeToString(String.serializer(), "12")

                test("returns the value serialized in the expected YAML form, escaping the integer with single-quotes") {
                    output shouldBe """'12'"""
                }
            }

            context("serializing a string with the value of a boolean using SingleLineStringStyle.PlainExceptAmbiguous, escaping with single-quotes") {
                val output = Yaml(
                    configuration = YamlConfiguration(
                        singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous,
                        ambiguousQuoteStyle = AmbiguousQuoteStyle.SingleQuoted,
                    ),
                ).encodeToString(String.serializer(), "true")

                test("returns the value serialized in the expected YAML form, escaping the boolean with single-quotes") {
                    output shouldBe """'true'"""
                }
            }

            context("serializing a string with the value of an float using SingleLineStringStyle.PlainExceptAmbiguous, escaping with single-quotes") {
                val output = Yaml(
                    configuration = YamlConfiguration(
                        singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous,
                        ambiguousQuoteStyle = AmbiguousQuoteStyle.SingleQuoted,
                    ),
                ).encodeToString(String.serializer(), "1.2")

                test("returns the value serialized in the expected YAML form, escaping the float with single-quotes") {
                    output shouldBe """'1.2'"""
                }
            }
        }

        context("serializing nested scalar node") {
            context("as scalar node") {
                val node = YamlScalar("1.2", YamlPath.root)
                val expectedOutput = """
                    text: "test"
                    node: 1.2
                """.trimIndent()
                val value = TestClassWithNestedScalar(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedScalar.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("as general node") {
                val node = YamlScalar("1.2", YamlPath.root)
                val expectedOutput = """
                    text: "test"
                    node: 1.2
                """.trimIndent()
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("of boolean") {
                val node = YamlScalar("true", YamlPath.root)
                val expectedOutput = """
                    text: "test"
                    node: true
                """.trimIndent()
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("of integer like number") {
                val node = YamlScalar("-5", YamlPath.root)
                val expectedOutput = """
                    text: "test"
                    node: -5
                """.trimIndent()
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("of floating point number") {
                val node = YamlScalar("5.34", YamlPath.root)
                val expectedOutput = """
                    text: "test"
                    node: 5.34
                """.trimIndent()
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("of character") {
                val node = YamlScalar("%", YamlPath.root)
                val expectedOutput = """
                    text: "test"
                    node: "%"
                """.trimIndent()
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("of string") {
                val node = YamlScalar("foo bar \n 42", YamlPath.root)
                val expectedOutput = """
                    text: "test"
                    node: "foo bar \n 42"
                """.trimIndent()
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }
        }

        context("serializing serial names using YamlNamingStrategies") {
            @Serializable
            data class NamingStrategyTestData(val serialName: String)

            context("serializing a serial name using YamlNamingStrategy.SnakeCase") {
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.SnakeCase),
                ).encodeToString(NamingStrategyTestData.serializer(), NamingStrategyTestData("value"))

                test("returns the serial name serialized in snake_case") {
                    output shouldBe """serial_name: "value""""
                }
            }

            context("serializing a serial name using YamlNamingStrategy.PascalCase") {
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.PascalCase),
                ).encodeToString(NamingStrategyTestData.serializer(), NamingStrategyTestData("value"))

                test("returns the serial name serialized in PascalCase") {
                    output shouldBe """SerialName: "value""""
                }
            }

            context("serializing a serial name using YamlNamingStrategy.CamelCase") {
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.CamelCase),
                ).encodeToString(NamingStrategyTestData.serializer(), NamingStrategyTestData("value"))

                test("returns the serial name serialized in camelCase") {
                    output shouldBe """serialName: "value""""
                }
            }

            context("serializing a serial name using YamlNamingStrategy.KebabCase") {
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.KebabCase),
                ).encodeToString(NamingStrategyTestData.serializer(), NamingStrategyTestData("value"))

                test("returns the serial name serialized in camelCase") {
                    output shouldBe """serial-name: "value""""
                }
            }

            context("serializing a serial name using a YamlNamingStrategy only applies it to the serial name") {
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.PascalCase),
                ).encodeToString(NamingStrategyTestData.serializer(), NamingStrategyTestData("value_with_several_words"))

                test("returns only the name serialized in PascalCase and not the value too") {
                    output shouldBe """SerialName: "value_with_several_words""""
                }
            }

            context("serializing a long serial name with multiple words using a YamlNamingStrategy") {
                @Serializable
                data class LongSerialName(val reallyLongSerialName: String)
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.SnakeCase),
                ).encodeToString(LongSerialName.serializer(), LongSerialName("value"))

                test("returns the name serialized correctly") {
                    output shouldBe """really_long_serial_name: "value""""
                }
            }

            context("serializing a serial name of only one character using a YamlNamingStrategy") {
                @Serializable
                data class OneCharacterSerialName(val a: String)
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.PascalCase),
                ).encodeToString(OneCharacterSerialName.serializer(), OneCharacterSerialName("value"))

                test("returns the name serialized correctly") {
                    output shouldBe """A: "value""""
                }
            }

            context("serializing a serial name of only one word using a YamlNamingStrategy") {
                @Serializable
                data class OneWordSerialName(val name: String)
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.PascalCase),
                ).encodeToString(OneWordSerialName.serializer(), OneWordSerialName("value"))

                test("returns the name serialized correctly") {
                    output shouldBe """Name: "value""""
                }
            }
        }

        context("serializing enumeration values") {
            val output = Yaml.default.encodeToString(TestEnum.serializer(), TestEnum.Value1)

            test("returns the value serialized in the expected YAML form") {
                output shouldBe """"Value1""""
            }
        }

        context("serializing lists") {
            context("serializing a list of integers") {
                val output = Yaml.default.encodeToString(ListSerializer(Int.serializer()), listOf(1, 2, 3))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            - 1
                            - 2
                            - 3
                        """.trimIndent()
                }
            }
            context("serializing a list of integers in flow form") {
                val output = Yaml(configuration = YamlConfiguration(sequenceStyle = SequenceStyle.Flow))
                    .encodeToString(ListSerializer(Int.serializer()), listOf(1, 2, 3))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe "[1, 2, 3]"
                }
            }

            context("serializing a list of nullable integers") {
                val output = Yaml.default.encodeToString(ListSerializer(Int.serializer().nullable), listOf(1, null, 3))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            - 1
                            - null
                            - 3
                        """.trimIndent()
                }
            }

            context("serializing a list of integers in flow form with sequence block indent") {
                val output = Yaml(configuration = YamlConfiguration(sequenceStyle = SequenceStyle.Flow, sequenceBlockIndent = 2))
                    .encodeToString(ListSerializer(Int.serializer()), listOf(1, 2, 3))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe "[1, 2, 3]"
                }
            }

            context("serializing a list of integers without sequence block indent") {
                val output = Yaml(configuration = YamlConfiguration(sequenceBlockIndent = 0))
                    .encodeToString(ListSerializer(Int.serializer()), listOf(1, 2, 3))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                        |- 1
                        |- 2
                        |- 3
                        """.trimMargin()
                }
            }

            context("serializing a list of integers with sequence block indent") {
                val output = Yaml(configuration = YamlConfiguration(sequenceBlockIndent = 2))
                    .encodeToString(ListSerializer(Int.serializer()), listOf(1, 2, 3))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                        |  - 1
                        |  - 2
                        |  - 3
                        """.trimMargin()
                }
            }

            context("serializing a list of objects with sequence block indent") {
                @Serializable
                data class Foo(val bar: String)

                val output = Yaml(configuration = YamlConfiguration(sequenceBlockIndent = 2))
                    .encodeToString(ListSerializer(Foo.serializer()), listOf(Foo("baz")))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                        |  - bar: "baz"
                        """.trimMargin()
                }
            }

            context("serializing a list of nullable integers in flow form") {
                val output = Yaml(configuration = YamlConfiguration(sequenceStyle = SequenceStyle.Flow))
                    .encodeToString(ListSerializer(Int.serializer().nullable), listOf(1, null, 3))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe "[1, null, 3]"
                }
            }

            context("serializing a list of strings") {
                val output = Yaml.default.encodeToString(ListSerializer(String.serializer()), listOf("item1", "item2"))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            - "item1"
                            - "item2"
                        """.trimIndent()
                }
            }

            context("serializing a list of strings in flow form") {
                val output = Yaml(configuration = YamlConfiguration(sequenceStyle = SequenceStyle.Flow))
                    .encodeToString(ListSerializer(String.serializer()), listOf("item1", "item2"))

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe """["item1", "item2"]"""
                }
            }

            context("serializing a list of strings with a string longer than the maximum scalar width") {
                val yamlWithScalarLimit = Yaml(configuration = YamlConfiguration(breakScalarsAt = 80))
                val output = yamlWithScalarLimit.encodeToString(ListSerializer(String.serializer()), listOf("item1", "Hello world this is a string that is much, much, much (ok, not that much) longer than 80 characters"))

                test("returns the value serialized in the expected YAML form, broken onto a new line at the maximum scalar width") {
                    output shouldBe
                        """
                        |- "item1"
                        |- "Hello world this is a string that is much, much, much (ok, not that much) longer\
                        |  \ than 80 characters"
                        """.trimMargin()
                }
            }

            context("serializing a list of strings with a string longer than the maximum scalar width in flow form") {
                val yamlWithScalarLimit = Yaml(configuration = YamlConfiguration(breakScalarsAt = 80, sequenceStyle = SequenceStyle.Flow))
                val output = yamlWithScalarLimit.encodeToString(ListSerializer(String.serializer()), listOf("item1", "Hello world this is a string that is much, much, much (ok, not that much) longer than 80 characters"))

                test("returns the value serialized in the expected YAML form, broken onto a new line at the maximum scalar width") {
                    output shouldBe
                        """
                            ["item1", "Hello world this is a string that is much, much, much (ok, not that much)\
                                \ longer than 80 characters"]
                        """.trimIndent()
                }
            }

            context("serializing a list of a list of integers") {
                val input = listOf(
                    listOf(1, 2, 3),
                    listOf(4, 5),
                )

                val output = Yaml.default.encodeToString(ListSerializer(ListSerializer(Int.serializer())), input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            - - 1
                              - 2
                              - 3
                            - - 4
                              - 5
                        """.trimIndent()
                }
            }

            context("serializing a list of a list of integers in flow form") {
                val input = listOf(
                    listOf(1, 2, 3),
                    listOf(4, 5),
                )

                val output = Yaml(configuration = YamlConfiguration(sequenceStyle = SequenceStyle.Flow))
                    .encodeToString(ListSerializer(ListSerializer(Int.serializer())), input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe """[[1, 2, 3], [4, 5]]"""
                }
            }

            context("serializing a list of maps from strings to strings") {
                val input = listOf(
                    mapOf(
                        "key1" to "value1",
                        "key2" to "value2",
                    ),
                    mapOf(
                        "key3" to "value3",
                    ),
                )

                val serializer = ListSerializer(MapSerializer(String.serializer(), String.serializer()))
                val output = Yaml.default.encodeToString(serializer, input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            - "key1": "value1"
                              "key2": "value2"
                            - "key3": "value3"
                        """.trimIndent()
                }
            }

            context("serializing a list of objects") {
                val input = listOf(
                    SimpleStructure("name1"),
                    SimpleStructure("name2"),
                )

                val output = Yaml.default.encodeToString(ListSerializer(SimpleStructure.serializer()), input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            - name: "name1"
                            - name: "name2"
                        """.trimIndent()
                }
            }
            context("serializing a list of objects in flow form") {
                val input = listOf(
                    SimpleStructure("name1"),
                    SimpleStructure("name2"),
                )

                val output = Yaml(configuration = YamlConfiguration(sequenceStyle = SequenceStyle.Flow)).encodeToString(ListSerializer(SimpleStructure.serializer()), input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            [{name: "name1"}, {name: "name2"}]
                        """.trimIndent()
                }
            }
        }

        context("serializing nested list node") {
            val node = Yaml.default.parseToYamlNode(
                """
                    - 1
                    - 2
                    - 3
                """.trimIndent(),
            ) as YamlList
            val expectedOutput = """
                    text: "test"
                    node:
                    - 1
                    - 2
                    - 3
            """.trimIndent()
            context("as list node") {
                val value = TestClassWithNestedList(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedList.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("as general node") {
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }
        }

        context("serializing maps") {
            context("serializing a map of strings to strings") {
                val input = mapOf(
                    "key1" to "value1",
                    "key2" to "value2",
                )

                val output = Yaml.default.encodeToString(MapSerializer(String.serializer(), String.serializer()), input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            "key1": "value1"
                            "key2": "value2"
                        """.trimIndent()
                }
            }

            context("serializing a nested map of strings to strings") {
                val input = mapOf(
                    "map1" to mapOf(
                        "key1" to "value1",
                        "key2" to "value2",
                    ),
                    "map2" to mapOf(
                        "key3" to "value3",
                    ),
                )

                val serializer = MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer()))
                val output = Yaml.default.encodeToString(serializer, input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            "map1":
                              "key1": "value1"
                              "key2": "value2"
                            "map2":
                              "key3": "value3"
                        """.trimIndent()
                }
            }

            context("serializing a map of strings to lists") {
                val input = mapOf(
                    "list1" to listOf(1, 2, 3),
                    "list2" to listOf(4, 5, 6),
                )

                val serializer = MapSerializer(String.serializer(), ListSerializer(Int.serializer()))
                val output = Yaml.default.encodeToString(serializer, input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            "list1":
                            - 1
                            - 2
                            - 3
                            "list2":
                            - 4
                            - 5
                            - 6
                        """.trimIndent()
                }
            }

            context("serializing a map of strings to objects") {
                val input = mapOf(
                    "item1" to SimpleStructure("name1"),
                    "item2" to SimpleStructure("name2"),
                )

                val serializer = MapSerializer(String.serializer(), SimpleStructure.serializer())
                val output = Yaml.default.encodeToString(serializer, input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            "item1":
                              name: "name1"
                            "item2":
                              name: "name2"
                        """.trimIndent()
                }
            }
        }

        context("serializing nested map node") {
            val node = Yaml.default.parseToYamlNode(
                """
                    foo: bar
                    baz: 1
                    test:
                      - 1
                      - 2
                      - 3
                """.trimIndent(),
            ) as YamlMap
            val expectedOutput = """
                    text: "test"
                    node:
                      "foo": "bar"
                      "baz": 1
                      "test":
                      - 1
                      - 2
                      - 3
            """.trimIndent()
            context("as map node") {
                val value = TestClassWithNestedMap(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedMap.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("as general node") {
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }
        }

        context("serializing objects") {
            context("serializing a simple object") {
                val input = SimpleStructure("The name")
                val output = Yaml.default.encodeToString(SimpleStructure.serializer(), input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            name: "The name"
                        """.trimIndent()
                }
            }

            context("serializing a nested object") {
                val input = NestedObjects(
                    SimpleStructure("name1"),
                    SimpleStructure("name2"),
                )

                context("with default indentation") {
                    val output = Yaml.default.encodeToString(NestedObjects.serializer(), input)

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe
                            """
                                firstPerson:
                                  name: "name1"
                                secondPerson:
                                  name: "name2"
                            """.trimIndent()
                    }
                }

                context("with customised indentation") {
                    val output = yamlWithCustomisedIndentation.encodeToString(NestedObjects.serializer(), input)

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe
                            """
                                firstPerson:
                                   name: "name1"
                                secondPerson:
                                   name: "name2"
                            """.trimIndent()
                    }
                }
            }

            context("serializing an object with a nested list") {
                val input = Team(listOf("name1", "name2"))

                context("with default indentation") {
                    val output = Yaml.default.encodeToString(Team.serializer(), input)

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe
                            """
                                members:
                                - "name1"
                                - "name2"
                            """.trimIndent()
                    }
                }

                context("with customised indentation") {
                    val output = yamlWithCustomisedIndentation.encodeToString(Team.serializer(), input)

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe
                            """
                                members:
                                - "name1"
                                - "name2"
                            """.trimIndent()
                    }
                }
            }

            context("serializing an object with a nested map") {
                val input = ThingWithMap(
                    mapOf(
                        "var1" to "value1",
                        "var2" to "value2",
                    ),
                )

                val output = Yaml.default.encodeToString(ThingWithMap.serializer(), input)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe
                        """
                            variables:
                              "var1": "value1"
                              "var2": "value2"
                        """.trimIndent()
                }
            }
        }

        context("serializing polymorphic values") {
            context("given tags are used to store the type information") {
                val polymorphicYaml = Yaml(serializersModule = polymorphicModule, configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Tag))

                context("serializing a sealed type") {
                    val input = TestSealedStructure.SimpleSealedInt(5)
                    val output = polymorphicYaml.encodeToString(TestSealedStructure.serializer(), input)
                    val expectedYaml = """
                        !<sealedInt>
                        value: 5
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing a sealed type (inline)") {
                    val input = TestSealedStructure.InlineSealedString("abc")
                    val output = polymorphicYaml.encodeToString(TestSealedStructure.serializer(), input)
                    val expectedYaml = """
                        !<inlineString> "abc"
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing an unsealed type") {
                    val input = UnsealedString("blah")
                    val output = polymorphicYaml.encodeToString(PolymorphicSerializer(UnsealedClass::class), input)
                    val expectedYaml = """
                        !<unsealedString>
                        value: "blah"
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing an unwrapped type") {
                    val input = UnwrappedString("blah")
                    val output = polymorphicYaml.encodeToString(PolymorphicSerializer(UnwrappedInterface::class), input)
                    val expectedYaml = """
                        !<simpleString> "blah"
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing a polymorphic value as a property value") {
                    val input = SealedWrapper(TestSealedStructure.SimpleSealedInt(5))
                    val output = polymorphicYaml.encodeToString(SealedWrapper.serializer(), input)
                    val expectedYaml = """
                        element: !<sealedInt>
                          value: 5
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing a polymorphic value (inline) as a property value") {
                    val input = SealedWrapper(TestSealedStructure.InlineSealedString("abc"))
                    val output = polymorphicYaml.encodeToString(SealedWrapper.serializer(), input)
                    val expectedYaml = """
                        element: !<inlineString> "abc"
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing a list of polymorphic values") {
                    val input = listOf(
                        TestSealedStructure.SimpleSealedInt(5),
                        TestSealedStructure.SimpleSealedString("some test"),
                        TestSealedStructure.SimpleSealedInt(-20),
                        TestSealedStructure.InlineSealedString("more test"),
                        TestSealedStructure.SimpleSealedString(null),
                        null,
                    )

                    val output = polymorphicYaml.encodeToString(ListSerializer(TestSealedStructure.serializer().nullable), input)

                    val expectedYaml = """
                        - !<sealedInt>
                          value: 5
                        - !<sealedString>
                          value: "some test"
                        - !<sealedInt>
                          value: -20
                        - !<inlineString> "more test"
                        - !<sealedString>
                          value: null
                        - null
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }
            }

            context("given properties are used to store the type information") {
                val polymorphicYaml = Yaml(serializersModule = polymorphicModule, configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property))

                context("serializing a sealed type") {
                    val input = TestSealedStructure.SimpleSealedInt(5)
                    val output = polymorphicYaml.encodeToString(TestSealedStructure.serializer(), input)
                    val expectedYaml = """
                        type: "sealedInt"
                        value: 5
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing an unsealed type") {
                    val input = UnsealedString("blah")
                    val output = polymorphicYaml.encodeToString(PolymorphicSerializer(UnsealedClass::class), input)
                    val expectedYaml = """
                        type: "unsealedString"
                        value: "blah"
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing an unwrapped type") {
                    val input = UnwrappedString("blah")

                    test("throws an appropriate exception") {
                        val exception = shouldThrow<IllegalStateException> { polymorphicYaml.encodeToString(PolymorphicSerializer(UnwrappedInterface::class), input) }
                        exception.message shouldBe "Cannot serialize a polymorphic value that is not a YAML object when using PolymorphismStyle.Property."
                    }
                }

                context("serializing a polymorphic value as a property value") {
                    val input = SealedWrapper(TestSealedStructure.SimpleSealedInt(5))
                    val output = polymorphicYaml.encodeToString(SealedWrapper.serializer(), input)
                    val expectedYaml = """
                        element:
                          type: "sealedInt"
                          value: 5
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing a list of polymorphic values") {
                    val input = listOf(
                        TestSealedStructure.SimpleSealedInt(5),
                        TestSealedStructure.SimpleSealedString("some test"),
                        TestSealedStructure.SimpleSealedInt(-20),
                        TestSealedStructure.SimpleSealedString(null),
                        null,
                    )

                    val output = polymorphicYaml.encodeToString(ListSerializer(TestSealedStructure.serializer().nullable), input)

                    val expectedYaml = """
                        - type: "sealedInt"
                          value: 5
                        - type: "sealedString"
                          value: "some test"
                        - type: "sealedInt"
                          value: -20
                        - type: "sealedString"
                          value: null
                        - null
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }
            }

            context("given custom property name are used to store the type information") {
                val polymorphicYaml = Yaml(serializersModule = polymorphicModule, configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property, polymorphismPropertyName = "kind"))

                context("serializing a sealed type") {
                    val input = TestSealedStructure.SimpleSealedInt(5)
                    val output = polymorphicYaml.encodeToString(TestSealedStructure.serializer(), input)
                    val expectedYaml = """
                        kind: "sealedInt"
                        value: 5
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing an unsealed type") {
                    val input = UnsealedString("blah")
                    val output = polymorphicYaml.encodeToString(PolymorphicSerializer(UnsealedClass::class), input)
                    val expectedYaml = """
                        kind: "unsealedString"
                        value: "blah"
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing an unwrapped type") {
                    val input = UnwrappedString("blah")

                    test("throws an appropriate exception") {
                        val exception = shouldThrow<IllegalStateException> { polymorphicYaml.encodeToString(PolymorphicSerializer(UnwrappedInterface::class), input) }
                        exception.message shouldBe "Cannot serialize a polymorphic value that is not a YAML object when using PolymorphismStyle.Property."
                    }
                }

                context("serializing a polymorphic value as a property value") {
                    val input = SealedWrapper(TestSealedStructure.SimpleSealedInt(5))
                    val output = polymorphicYaml.encodeToString(SealedWrapper.serializer(), input)
                    val expectedYaml = """
                        element:
                          kind: "sealedInt"
                          value: 5
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }

                context("serializing a list of polymorphic values") {
                    val input = listOf(
                        TestSealedStructure.SimpleSealedInt(5),
                        TestSealedStructure.SimpleSealedString("some test"),
                        TestSealedStructure.SimpleSealedInt(-20),
                        TestSealedStructure.SimpleSealedString(null),
                        null,
                    )

                    val output = polymorphicYaml.encodeToString(ListSerializer(TestSealedStructure.serializer().nullable), input)

                    val expectedYaml = """
                        - kind: "sealedInt"
                          value: 5
                        - kind: "sealedString"
                          value: "some test"
                        - kind: "sealedInt"
                          value: -20
                        - kind: "sealedString"
                          value: null
                        - null
                    """.trimIndent()

                    test("returns the value serialized in the expected YAML form") {
                        output shouldBe expectedYaml
                    }
                }
            }
        }

        context("serializing nested tagged node") {
            val node = Yaml.default.parseToYamlNode("!testtag 2024-01-01") as YamlTaggedNode
            val expectedOutput = """
                    text: "test"
                    node: !testtag "2024-01-01"
            """.trimIndent()
            context("as tagged node") {
                val value = TestClassWithNestedTaggedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedTaggedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }

            context("as general node") {
                val value = TestClassWithNestedNode(text = "test", node = node)
                val output = Yaml.default.encodeToString(TestClassWithNestedNode.serializer(), value)

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedOutput
                }
            }
        }

        context("handling default values") {
            context("when encoding defaults") {
                val defaultEncoder = Yaml.default

                context("given a property with no default value") {
                    val input = SimpleStructure("name1")

                    test("is always written") {
                        defaultEncoder.encodeToString(SimpleStructure.serializer(), input) shouldBe """name: "name1""""
                    }
                }

                context("given a property with a default value") {
                    val input = SimpleStructureWithDefault()

                    test("is written") {
                        defaultEncoder.encodeToString(SimpleStructureWithDefault.serializer(), input) shouldBe """name: "default""""
                    }
                }

                context("given a property with a default value has a non-default value") {
                    val input = SimpleStructureWithDefault("name1")

                    test("is written") {
                        defaultEncoder.encodeToString(SimpleStructureWithDefault.serializer(), input) shouldBe """name: "name1""""
                    }
                }
            }

            context("when not encoding defaults") {
                val noDefaultEncoder = Yaml(configuration = YamlConfiguration(encodeDefaults = false))

                context("given a property with no default value") {
                    val input = SimpleStructure("name1")

                    test("is always written") {
                        noDefaultEncoder.encodeToString(SimpleStructure.serializer(), input) shouldBe """name: "name1""""
                    }
                }

                context("given a property with a default value") {
                    val input = SimpleStructureWithDefault()

                    test("is not written") {
                        noDefaultEncoder.encodeToString(SimpleStructureWithDefault.serializer(), input) shouldBe """{}"""
                    }
                }

                context("given a property with a default value has a non-default value") {
                    val input = SimpleStructureWithDefault("name1")

                    test("is written") {
                        noDefaultEncoder.encodeToString(SimpleStructureWithDefault.serializer(), input) shouldBe """name: "name1""""
                    }
                }
            }
        }

        context("handling comments") {
            context("comments in kotlin object") {
                val input = SimpleStructureWithComments("objName", 73, "justTest")

                test("is written") {
                    Yaml.default.encodeToString(SimpleStructureWithComments.serializer(), input) shouldBe """
                        name: "objName"
                        # Cool int
                        myInt: 73
                        # Testing
                        # multiline
                        test: "justTest"
                    """.trimIndent()
                }
            }
        }
    }
})

// FIXME: ideally these would just be inline in the test cases that need them, but due to
// https://github.com/Kotlin/kotlinx.serialization/issues/1427, this is no longer possible with
// kotlinx.serialization 1.2 and above.
// See also https://github.com/Kotlin/kotlinx.serialization/issues/1468.

@Serializable
private data class SimpleStructureWithDefault(
    val name: String = "default",
)

@Serializable
private data class SimpleStructureWithComments(
    val name: String,
    @YamlComment("Cool int")
    val myInt: Int,
    @YamlComment(
        "Testing",
        "multiline",
    )
    val test: String,
)

@Serializable
private data class ThingWithMap(val variables: Map<String, String>)
