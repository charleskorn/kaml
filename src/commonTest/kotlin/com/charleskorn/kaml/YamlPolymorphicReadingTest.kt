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

import com.charleskorn.kaml.testobjects.PolymorphicWrapper
import com.charleskorn.kaml.testobjects.SealedWrapper
import com.charleskorn.kaml.testobjects.TestSealedStructure
import com.charleskorn.kaml.testobjects.UnsealedClass
import com.charleskorn.kaml.testobjects.UnsealedString
import com.charleskorn.kaml.testobjects.UnwrappedInt
import com.charleskorn.kaml.testobjects.UnwrappedInterface
import com.charleskorn.kaml.testobjects.UnwrappedString
import com.charleskorn.kaml.testobjects.polymorphicModule
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

class YamlPolymorphicReadingTest : FlatFunSpec({
    context("a YAML parser parsing polymorphic values") {
        context("given tags are used to store the type information") {
            val polymorphicYaml = Yaml(serializersModule = polymorphicModule, configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Tag))

            context("given some input where the value should be a sealed class") {
                val input = """
                        !<sealedString>
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe TestSealedStructure.SimpleSealedString("asdfg")
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input)

                    test("deserializes it to a map ignoring the tag") {
                        result shouldBe mapOf("value" to "asdfg")
                    }
                }
            }

            // See https://github.com/charleskorn/kaml/issues/179.
            context("given some input where a tag is provided but no value is provided") {
                val input = """
                        !<sealedString>
                """.trimIndent()

                context("parsing that input") {
                    test("throws an appropriate exception") {
                        val exception = shouldThrow<MissingRequiredPropertyException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Property 'value' is required but it is missing."
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.propertyName shouldBe "value"
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }

            context("given some input where the value is a literal") {
                val input = """
                        !<simpleString> "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(PolymorphicSerializer(UnwrappedInterface::class), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe UnwrappedString("asdfg")
                    }
                }

                context("parsing that input as a string") {
                    val result = polymorphicYaml.decodeFromString(String.serializer(), input)

                    test("deserializes it to a string ignoring the tag") {
                        result shouldBe "asdfg"
                    }
                }
            }

            context("given some input where the value should be an unsealed class") {
                val input = """
                        !<unsealedString>
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(PolymorphicSerializer(UnsealedClass::class), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe UnsealedString("asdfg")
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input)

                    test("deserializes it to a map ignoring the tag") {
                        result shouldBe mapOf("value" to "asdfg")
                    }
                }
            }

            context("given some input for an object where the property value should be a sealed class") {
                val input = """
                        element: !<sealedString>
                            value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(SealedWrapper.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe SealedWrapper(TestSealedStructure.SimpleSealedString("asdfg"))
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer())), input)

                    test("deserializes it to a map ignoring the tag") {
                        result shouldBe mapOf("element" to mapOf("value" to "asdfg"))
                    }
                }
            }

            context("given some input for an object where the property value is a literal") {
                val input = """
                        test: !<simpleInt> 42
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(PolymorphicWrapper.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe PolymorphicWrapper(UnwrappedInt(42))
                    }
                }
            }

            context("given some tagged input representing a list of polymorphic objects") {
                val input = """
                        - !<sealedString>
                          value: null
                        - !<sealedInt>
                          value: -987
                        - !<sealedInt>
                          value: 654
                        - !<sealedString>
                          value: "tests"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(ListSerializer(TestSealedStructure.serializer()), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe
                            listOf(
                                TestSealedStructure.SimpleSealedString(null),
                                TestSealedStructure.SimpleSealedInt(-987),
                                TestSealedStructure.SimpleSealedInt(654),
                                TestSealedStructure.SimpleSealedString("tests"),
                            )
                    }
                }
            }

            context("given some untagged input for a polymorphic class") {
                val input = """
                        element:
                            value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> {
                            polymorphicYaml.decodeFromString(
                                SealedWrapper.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Value for 'element' is invalid: Value is missing a type tag (eg. !<type>)"
                            it.line shouldBe 2
                            it.column shouldBe 5
                            it.cause.shouldBeInstanceOf<MissingTypeTagException>()
                            it.path shouldBe YamlPath.root.withMapElementKey("element", Location(1, 1)).withMapElementValue(Location(2, 5))
                        }
                    }
                }
            }

            context("given some untagged input for a polymorphic value") {
                val input = """
                        test: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> {
                            polymorphicYaml.decodeFromString(
                                PolymorphicWrapper.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Value for 'test' is invalid: Value is missing a type tag (eg. !<type>)"
                            it.line shouldBe 1
                            it.column shouldBe 7
                            it.cause.shouldBeInstanceOf<MissingTypeTagException>()
                            it.path shouldBe YamlPath.root.withMapElementKey("test", Location(1, 1)).withMapElementValue(Location(1, 7))
                        }
                    }
                }
            }

            context("given some untagged null input for a polymorphic value") {
                val input = """
                        test: null
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<InvalidPropertyValueException> {
                            polymorphicYaml.decodeFromString(
                                PolymorphicWrapper.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Value for 'test' is invalid: Value is missing a type tag (eg. !<type>)"
                            it.line shouldBe 1
                            it.column shouldBe 7
                            it.cause.shouldBeInstanceOf<MissingTypeTagException>()
                            it.path shouldBe YamlPath.root.withMapElementKey("test", Location(1, 1)).withMapElementValue(Location(1, 7))
                        }
                    }
                }
            }

            context("given a polymorphic value for a property from an unsealed type with an unknown type tag") {
                val input = """
                        !<someOtherType> 42
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<UnknownPolymorphicTypeException> {
                            polymorphicYaml.decodeFromString(
                                PolymorphicSerializer(UnsealedClass::class),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Unknown type 'someOtherType'. Known types are: unsealedBoolean, unsealedString"
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.typeName shouldBe "someOtherType"
                            it.validTypeNames shouldBe setOf("unsealedBoolean", "unsealedString")
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }

            context("given a polymorphic value for a property from a sealed type with an unknown type tag") {
                val input = """
                        !<someOtherType> 42
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<UnknownPolymorphicTypeException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Unknown type 'someOtherType'. Known types are: sealedInt, sealedString"
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.typeName shouldBe "someOtherType"
                            it.validTypeNames shouldBe setOf("sealedInt", "sealedString")
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }

            context("given a polymorphic value from a literal with an unknown type tag") {
                val input = """
                        !<someOtherType> 42
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<UnknownPolymorphicTypeException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Unknown type 'someOtherType'. Known types are: sealedInt, sealedString"
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.typeName shouldBe "someOtherType"
                            it.validTypeNames shouldBe setOf("sealedInt", "sealedString")
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }
        }

        context("given a property is used to store the type information") {
            val polymorphicYaml = Yaml(serializersModule = polymorphicModule, configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property))

            context("given some input where the value should be a sealed class") {
                val input = """
                        type: sealedString
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe TestSealedStructure.SimpleSealedString("asdfg")
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input)

                    test("deserializes it to a map including the type") {
                        result shouldBe mapOf("type" to "sealedString", "value" to "asdfg")
                    }
                }
            }

            context("given some input where the value should be an unsealed class") {
                val input = """
                        type: unsealedString
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(PolymorphicSerializer(UnsealedClass::class), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe UnsealedString("asdfg")
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input)

                    test("deserializes it to a map ignoring the tag") {
                        result shouldBe mapOf("type" to "unsealedString", "value" to "asdfg")
                    }
                }
            }

            context("given some input for an object where the property value should be a sealed class") {
                val input = """
                        element:
                            type: sealedString
                            value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(SealedWrapper.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe SealedWrapper(TestSealedStructure.SimpleSealedString("asdfg"))
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer())), input)

                    test("deserializes it to a map ignoring the tag") {
                        result shouldBe mapOf("element" to mapOf("type" to "sealedString", "value" to "asdfg"))
                    }
                }
            }

            context("given some input missing a type property") {
                val input = """
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<MissingRequiredPropertyException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Property 'type' is required but it is missing."
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.propertyName shouldBe "type"
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }

            mapOf(
                "a list" to "[]",
                "a map" to "{}",
                "a null value" to "null",
                "a tagged value" to "!<tag> sealedString",
            ).forEach { (description, value) ->
                context("given some input with a type property that is $description") {
                    val input = """
                            type: $value
                            value: "asdfg"
                    """.trimIndent()

                    context("parsing that input") {
                        test("throws an exception with the correct location information") {
                            val exception = shouldThrow<InvalidPropertyValueException> {
                                polymorphicYaml.decodeFromString(
                                    TestSealedStructure.serializer(),
                                    input,
                                )
                            }

                            exception.asClue {
                                it.message shouldBe "Value for 'type' is invalid: expected a string, but got $description"
                                it.line shouldBe 1
                                it.column shouldBe 7
                                it.propertyName shouldBe "type"
                                it.reason shouldBe "expected a string, but got $description"
                                it.path shouldBe YamlPath.root.withMapElementKey("type", Location(1, 1)).withMapElementValue(Location(1, 7))
                            }
                        }
                    }
                }
            }

            context("given some tagged input representing a list of polymorphic objects") {
                val input = """
                        - type: sealedString
                          value: null
                        - type: sealedInt
                          value: -987
                        - type: sealedInt
                          value: 654
                        - type: sealedString
                          value: "tests"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(ListSerializer(TestSealedStructure.serializer()), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe
                            listOf(
                                TestSealedStructure.SimpleSealedString(null),
                                TestSealedStructure.SimpleSealedInt(-987),
                                TestSealedStructure.SimpleSealedInt(654),
                                TestSealedStructure.SimpleSealedString("tests"),
                            )
                    }
                }
            }

            context("given a polymorphic value for a property from an unsealed type with an unknown type tag") {
                val input = """
                        type: someOtherType
                        value: 123
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<UnknownPolymorphicTypeException> {
                            polymorphicYaml.decodeFromString(
                                PolymorphicSerializer(UnsealedClass::class),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Unknown type 'someOtherType'. Known types are: unsealedBoolean, unsealedString"
                            it.line shouldBe 1
                            it.column shouldBe 7
                            it.typeName shouldBe "someOtherType"
                            it.validTypeNames shouldBe setOf("unsealedBoolean", "unsealedString")
                            it.path shouldBe YamlPath.root.withMapElementKey("type", Location(1, 1)).withMapElementValue(Location(1, 7))
                        }
                    }
                }
            }

            context("given a polymorphic value for a property from a sealed type with an unknown type tag") {
                val input = """
                        type: someOtherType
                        value: 123
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<UnknownPolymorphicTypeException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Unknown type 'someOtherType'. Known types are: sealedInt, sealedString"
                            it.line shouldBe 1
                            it.column shouldBe 7
                            it.typeName shouldBe "someOtherType"
                            it.validTypeNames shouldBe setOf("sealedInt", "sealedString")
                            it.path shouldBe YamlPath.root.withMapElementKey("type", Location(1, 1)).withMapElementValue(Location(1, 7))
                        }
                    }
                }
            }

            context("given some input with a tag and a type property") {
                val input = """
                        !<sealedInt>
                        type: sealedString
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input)

                    test("uses the type from the property and ignores the tag") {
                        result shouldBe TestSealedStructure.SimpleSealedString("asdfg")
                    }
                }
            }
        }

        context("given a custom property name is used to store the type information") {
            val polymorphicYaml = Yaml(serializersModule = polymorphicModule, configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property, polymorphismPropertyName = "kind"))

            context("given some input where the value should be a sealed class") {
                val input = """
                        kind: sealedString
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe TestSealedStructure.SimpleSealedString("asdfg")
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input)

                    test("deserializes it to a map including the type") {
                        result shouldBe mapOf("kind" to "sealedString", "value" to "asdfg")
                    }
                }
            }

            context("given some input where the value should be an unsealed class") {
                val input = """
                        kind: unsealedString
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(PolymorphicSerializer(UnsealedClass::class), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe UnsealedString("asdfg")
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input)

                    test("deserializes it to a map ignoring the tag") {
                        result shouldBe mapOf("kind" to "unsealedString", "value" to "asdfg")
                    }
                }
            }

            context("given some input for an object where the property value should be a sealed class") {
                val input = """
                        element:
                            kind: sealedString
                            value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(SealedWrapper.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe SealedWrapper(TestSealedStructure.SimpleSealedString("asdfg"))
                    }
                }

                context("parsing that input as map") {
                    val result = polymorphicYaml.decodeFromString(MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer())), input)

                    test("deserializes it to a map ignoring the tag") {
                        result shouldBe mapOf("element" to mapOf("kind" to "sealedString", "value" to "asdfg"))
                    }
                }
            }

            context("given some input missing a type property") {
                val input = """
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<MissingRequiredPropertyException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Property 'kind' is required but it is missing."
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.propertyName shouldBe "kind"
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }

            mapOf(
                "a list" to "[]",
                "a map" to "{}",
                "a null value" to "null",
                "a tagged value" to "!<tag> sealedString",
            ).forEach { (description, value) ->
                context("given some input with a type property that is $description") {
                    val input = """
                            kind: $value
                            value: "asdfg"
                    """.trimIndent()

                    context("parsing that input") {
                        test("throws an exception with the correct location information") {
                            val exception = shouldThrow<InvalidPropertyValueException> {
                                polymorphicYaml.decodeFromString(
                                    TestSealedStructure.serializer(),
                                    input,
                                )
                            }

                            exception.asClue {
                                it.message shouldBe "Value for 'kind' is invalid: expected a string, but got $description"
                                it.line shouldBe 1
                                it.column shouldBe 7
                                it.propertyName shouldBe "kind"
                                it.reason shouldBe "expected a string, but got $description"
                                it.path shouldBe YamlPath.root.withMapElementKey("kind", Location(1, 1)).withMapElementValue(Location(1, 7))
                            }
                        }
                    }
                }
            }

            context("given some tagged input representing a list of polymorphic objects") {
                val input = """
                        - kind: sealedString
                          value: null
                        - kind: sealedInt
                          value: -987
                        - kind: sealedInt
                          value: 654
                        - kind: sealedString
                          value: "tests"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(ListSerializer(TestSealedStructure.serializer()), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe
                            listOf(
                                TestSealedStructure.SimpleSealedString(null),
                                TestSealedStructure.SimpleSealedInt(-987),
                                TestSealedStructure.SimpleSealedInt(654),
                                TestSealedStructure.SimpleSealedString("tests"),
                            )
                    }
                }
            }

            context("given a polymorphic value for a property from an unsealed type with an unknown type tag") {
                val input = """
                        kind: someOtherType
                        value: 123
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<UnknownPolymorphicTypeException> {
                            polymorphicYaml.decodeFromString(
                                PolymorphicSerializer(UnsealedClass::class),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Unknown type 'someOtherType'. Known types are: unsealedBoolean, unsealedString"
                            it.line shouldBe 1
                            it.column shouldBe 7
                            it.typeName shouldBe "someOtherType"
                            it.validTypeNames shouldBe setOf("unsealedBoolean", "unsealedString")
                            it.path shouldBe YamlPath.root.withMapElementKey("kind", Location(1, 1)).withMapElementValue(Location(1, 7))
                        }
                    }
                }
            }

            context("given a polymorphic value for a property from a sealed type with an unknown type tag") {
                val input = """
                        kind: someOtherType
                        value: 123
                """.trimIndent()

                context("parsing that input") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<UnknownPolymorphicTypeException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Unknown type 'someOtherType'. Known types are: sealedInt, sealedString"
                            it.line shouldBe 1
                            it.column shouldBe 7
                            it.typeName shouldBe "someOtherType"
                            it.validTypeNames shouldBe setOf("sealedInt", "sealedString")
                            it.path shouldBe YamlPath.root.withMapElementKey("kind", Location(1, 1)).withMapElementValue(Location(1, 7))
                        }
                    }
                }
            }

            context("given some input with a tag and a type property") {
                val input = """
                        !<sealedInt>
                        kind: sealedString
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    val result = polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input)

                    test("uses the type from the property and ignores the tag") {
                        result shouldBe TestSealedStructure.SimpleSealedString("asdfg")
                    }
                }
            }
        }

        context("given polymorphic inputs when PolymorphismStyle.None is used") {
            val polymorphicYaml = Yaml(serializersModule = polymorphicModule, configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.None))

            context("given tagged input") {
                val input = """
                        !<sealedString>
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    test("throws an appropriate exception") {
                        val exception = shouldThrow<IncorrectTypeException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Encountered a tagged polymorphic descriptor but PolymorphismStyle is 'None'"
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }
            context("given property polymorphism input") {
                val input = """
                        type: sealedString
                        value: "asdfg"
                """.trimIndent()

                context("parsing that input") {
                    test("throws an appropriate exception") {
                        val exception = shouldThrow<IncorrectTypeException> {
                            polymorphicYaml.decodeFromString(
                                TestSealedStructure.serializer(),
                                input,
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Encountered a polymorphic map descriptor but PolymorphismStyle is 'None'"
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }
        }
    }
})
