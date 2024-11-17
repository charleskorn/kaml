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

import com.charleskorn.kaml.testobjects.NestedObjects
import com.charleskorn.kaml.testobjects.PolymorphicWrapper
import com.charleskorn.kaml.testobjects.SealedWrapper
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.Team
import com.charleskorn.kaml.testobjects.TestClassWithNestedList
import com.charleskorn.kaml.testobjects.TestClassWithNestedMap
import com.charleskorn.kaml.testobjects.TestClassWithNestedNode
import com.charleskorn.kaml.testobjects.TestClassWithNestedTaggedNode
import com.charleskorn.kaml.testobjects.TestEnum
import com.charleskorn.kaml.testobjects.TestSealedStructure
import com.charleskorn.kaml.testobjects.UnsealedClass
import com.charleskorn.kaml.testobjects.UnsealedString
import com.charleskorn.kaml.testobjects.UnwrappedInt
import com.charleskorn.kaml.testobjects.UnwrappedInterface
import com.charleskorn.kaml.testobjects.UnwrappedString
import com.charleskorn.kaml.testobjects.polymorphicModule
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.serializersModuleOf
import kotlin.jvm.JvmInline

class YamlReadingTest : FlatFunSpec({
    context("a YAML parser") {

        context("deserializing serial names using YamlNamingStrategies") {
            @Serializable
            data class NamingStrategyTestData(val serialName: String)

            context("deserializing a snake_case serial name using YamlNamingStrategy.SnakeCase") {
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.SnakeCase),
                ).decodeFromString(NamingStrategyTestData.serializer(), "serial_name: value")

                test("correctly serializes into the data class") {
                    output shouldBe NamingStrategyTestData("value")
                }
            }

            context("deserializing a PascalCase serial name using YamlNamingStrategy.PascalCase") {
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.PascalCase),
                ).decodeFromString(NamingStrategyTestData.serializer(), "SerialName: value")

                test("correctly serializes into the data class") {
                    output shouldBe NamingStrategyTestData("value")
                }
            }

            context("deserializing a camelCase serial name using YamlNamingStrategy.CamelCase") {
                val output = Yaml(
                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.CamelCase),
                ).decodeFromString(NamingStrategyTestData.serializer(), "serialName: value")

                test("correctly serializes into the data class") {
                    output shouldBe NamingStrategyTestData("value")
                }
            }
        }

        context("parsing lists") {
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
                        val exception = shouldThrow<LocationInformationException> { Yaml.default.decodeFromString(ListSerializer(LocationThrowingSerializer), input) }

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

        context("parsing nested list node") {
            val input = """
                text: "OK"
                node:
                    - 1.2
                    - 3
                    - .Inf
                    - null
            """.trimIndent()

            context("parsing that input as a list node") {
                val result = Yaml.default.decodeFromString(TestClassWithNestedList.serializer(), input)
                val resultList = result.node.items.map { if (it is YamlNull) null else it.yamlScalar.toDouble() }

                test("deserializes list") {
                    resultList shouldBe listOf(1.2, 3.0, Double.POSITIVE_INFINITY, null)
                }
            }

            context("parsing that input as a node") {
                val result = Yaml.default.decodeFromString(TestClassWithNestedNode.serializer(), input)

                test("deserializes node to list") {
                    result.node.shouldBeInstanceOf<YamlList>()
                }

                test("deserializes node to double list") {
                    val resultList = result.node.yamlList.items.map { if (it is YamlNull) null else it.yamlScalar.toDouble() }
                    resultList shouldBe listOf(1.2, 3.0, Double.POSITIVE_INFINITY, null)
                }
            }
        }

        context("parsing objects") {
            context("given some input representing an object with an optional value specified") {
                val input = """
                    string: Alex
                    byte: 12
                    short: 1234
                    int: 123456
                    long: 1234567
                    float: 1.2
                    double: 2.4
                    enum: Value1
                    boolean: true
                    char: A
                    nullable: present
                """.trimIndent()

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(ComplexStructure.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe
                            ComplexStructure(
                                "Alex",
                                12,
                                1234,
                                123456,
                                1234567,
                                1.2f,
                                2.4,
                                TestEnum.Value1,
                                true,
                                'A',
                                "present",
                            )
                    }
                }
            }

            context("given some input representing an object with an optional value specified as null") {
                val input = """
                    string: Alex
                    byte: 12
                    short: 1234
                    int: 123456
                    long: 1234567
                    float: 1.2
                    double: 2.4
                    enum: Value1
                    boolean: true
                    char: A
                    nullable: null
                """.trimIndent()

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(ComplexStructure.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe
                            ComplexStructure(
                                "Alex",
                                12,
                                1234,
                                123456,
                                1234567,
                                1.2f,
                                2.4,
                                TestEnum.Value1,
                                true,
                                'A',
                                null,
                            )
                    }
                }
            }

            context("given some input representing an object with an optional value not specified") {
                val input = """
                    string: Alex
                    byte: 12
                    short: 1234
                    int: 123456
                    long: 1234567
                    float: 1.2
                    double: 2.4
                    enum: Value1
                    boolean: true
                    char: A
                """.trimIndent()

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(ComplexStructure.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe
                            ComplexStructure(
                                "Alex",
                                12,
                                1234,
                                123456,
                                1234567,
                                1.2f,
                                2.4,
                                TestEnum.Value1,
                                true,
                                'A',
                                null,
                            )
                    }
                }
            }

            context("given some input representing an object with an embedded list") {
                val input = """
                    members:
                        - Alex
                        - Jamie
                """.trimIndent()

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(Team.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe Team(listOf("Alex", "Jamie"))
                    }
                }
            }

            context("given some input representing an object with an embedded object") {
                val input = """
                    firstPerson:
                        name: Alex
                    secondPerson:
                        name: Jamie
                """.trimIndent()

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(NestedObjects.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe NestedObjects(SimpleStructure("Alex"), SimpleStructure("Jamie"))
                    }
                }
            }

            context("given some input representing an object where the keys are in a different order to the object definition") {
                val input = """
                    secondPerson:
                        name: Jamie
                    firstPerson:
                        name: Alex
                """.trimIndent()

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(NestedObjects.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe NestedObjects(SimpleStructure("Alex"), SimpleStructure("Jamie"))
                    }
                }
            }

            context("given some tagged input representing an arbitrary list") {
                val input = """
                    !!list
                        - 5
                        - 3
                """.trimIndent()

                context("parsing that input as list") {
                    val result = Yaml.default.decodeFromString(ListSerializer(Int.serializer()), input)
                    test("deserializes it to a list ignoring the tag") {
                        result shouldBe listOf(5, 3)
                    }
                }

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<LocationInformationException> { Yaml.default.decodeFromString(LocationThrowingSerializer, input) }

                        exception.asClue {
                            it.message shouldBe "Serializer called with location (1, 1) and path: <root>"
                        }
                    }
                }
            }

            context("given some tagged input representing an arbitrary map") {
                val input = """
                    !!map
                    foo: bar
                """.trimIndent()

                context("parsing that input as map") {
                    val result = Yaml.default.decodeFromString(
                        MapSerializer(String.serializer(), String.serializer()),
                        input,
                    )
                    test("deserializes it to a Map ignoring the tag") {
                        result shouldBe mapOf("foo" to "bar")
                    }
                }

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<LocationInformationException> { Yaml.default.decodeFromString(LocationThrowingMapSerializer, input) }

                        exception.asClue {
                            it.message shouldBe "Serializer called with location (1, 1) and path: <root>"
                        }
                    }
                }
            }

            context("given some input representing an object with a missing key") {
                val input = """
                    byte: 12
                    short: 1234
                    int: 123456
                    long: 1234567
                    float: 1.2
                    double: 2.4
                    enum: Value1
                    boolean: true
                    char: A
                """.trimIndent()

                context("parsing that input") {
                    test("throws an appropriate exception") {
                        val exception = shouldThrow<MissingRequiredPropertyException> { Yaml.default.decodeFromString(ComplexStructure.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Property 'string' is required but it is missing."
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.propertyName shouldBe "string"
                            it.path shouldBe YamlPath.root
                        }
                    }
                }
            }

            context("given some input representing an object with an unknown key") {
                val input = """
                    abc123: something
                """.trimIndent()

                context("parsing that input") {
                    test("throws an appropriate exception") {
                        val exception = shouldThrow<UnknownPropertyException> { Yaml.default.decodeFromString(ComplexStructure.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Unknown property 'abc123'. Known properties are: boolean, byte, char, double, enum, float, int, long, nullable, short, string"
                            it.line shouldBe 1
                            it.column shouldBe 1
                            it.propertyName shouldBe "abc123"
                            it.validPropertyNames shouldBe setOf("boolean", "byte", "char", "double", "enum", "float", "int", "long", "nullable", "short", "string")
                            it.path shouldBe YamlPath.root.withMapElementKey("abc123", Location(1, 1))
                        }
                    }
                }
            }

            context("given some input representing an object with an unknown key, using a naming strategy") {
                context("given the unknown key does not match any of the known field names") {
                    val input = "oneTwoThree: something".trimIndent()

                    context("parsing that input") {
                        test("throws an exception") {
                            val exception = shouldThrow<UnknownPropertyException> {
                                Yaml(
                                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.SnakeCase),
                                ).decodeFromString(NestedObjects.serializer(), input)
                            }

                            exception.asClue {
                                it.message shouldBe "Unknown property 'oneTwoThree'. Known properties are: first_person, second_person"
                                it.line shouldBe 1
                                it.column shouldBe 1
                                it.propertyName shouldBe "oneTwoThree"
                                it.validPropertyNames shouldBe setOf("first_person", "second_person")
                                it.path shouldBe YamlPath.root.withMapElementKey("oneTwoThree", Location(1, 1))
                            }
                        }
                    }
                }

                context("given the unknown key uses the Kotlin name of the field, rather than the name from the naming strategy") {
                    val input = "firstPerson: something".trimIndent()

                    context("parsing that input") {
                        test("throws an exception") {
                            val exception = shouldThrow<UnknownPropertyException> {
                                Yaml(
                                    configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.SnakeCase),
                                ).decodeFromString(NestedObjects.serializer(), input)
                            }

                            exception.asClue {
                                it.message shouldBe "Unknown property 'firstPerson'. Known properties are: first_person, second_person"
                                it.line shouldBe 1
                                it.column shouldBe 1
                                it.propertyName shouldBe "firstPerson"
                                it.validPropertyNames shouldBe setOf("first_person", "second_person")
                                it.path shouldBe YamlPath.root.withMapElementKey("firstPerson", Location(1, 1))
                            }
                        }
                    }
                }
            }

            context("given some input representing an object with an invalid value for a field") {
                mapOf(
                    "byte" to "Value 'xxx' is not a valid byte value.",
                    "short" to "Value 'xxx' is not a valid short value.",
                    "int" to "Value 'xxx' is not a valid integer value.",
                    "long" to "Value 'xxx' is not a valid long value.",
                    "float" to "Value 'xxx' is not a valid floating point value.",
                    "double" to "Value 'xxx' is not a valid floating point value.",
                    "enum" to "Value 'xxx' is not a valid option, permitted choices are: Value1, Value2",
                    "boolean" to "Value 'xxx' is not a valid boolean, permitted choices are: true or false",
                    "char" to "Value 'xxx' is not a valid character value.",
                ).forEach { (fieldName, errorMessage) ->
                    context("given the invalid field represents a $fieldName") {
                        val input = "$fieldName: xxx"

                        context("parsing that input") {
                            test("throws an appropriate exception") {
                                val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(ComplexStructure.serializer(), input) }

                                exception.asClue {
                                    it.message shouldBe "Value for '$fieldName' is invalid: $errorMessage"
                                    it.line shouldBe 1
                                    it.column shouldBe fieldName.length + 3
                                    it.propertyName shouldBe fieldName
                                    it.reason shouldBe errorMessage
                                    it.path shouldBe YamlPath.root.withMapElementKey(fieldName, Location(1, 1)).withMapElementValue(Location(1, fieldName.length + 3))
                                }
                            }
                        }
                    }
                }
            }

            context("given some input representing an object with a null value for a non-nullable scalar field") {
                val input = "name: null"

                context("parsing that input") {
                    test("throws an appropriate exception") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(SimpleStructure.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'name' is invalid: Unexpected null or empty value for non-null field."
                            it.line shouldBe 1
                            it.column shouldBe 7
                            it.propertyName shouldBe "name"
                            it.reason shouldBe "Unexpected null or empty value for non-null field."
                            it.path shouldBe YamlPath.root.withMapElementKey("name", Location(1, 1)).withMapElementValue(Location(1, 7))
                        }
                    }
                }
            }

            context("given some input representing an object with a null value for a non-nullable nested object field") {
                val input = "firstPerson: null"

                context("parsing that input") {
                    test("throws an appropriate exception") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(NestedObjects.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'firstPerson' is invalid: Unexpected null or empty value for non-null field."
                            it.line shouldBe 1
                            it.column shouldBe 14
                            it.propertyName shouldBe "firstPerson"
                            it.reason shouldBe "Unexpected null or empty value for non-null field."
                            it.path shouldBe YamlPath.root.withMapElementKey("firstPerson", Location(1, 1)).withMapElementValue(Location(1, 14))
                        }
                    }
                }
            }

            context("given some input representing an object with a null value for a nullable nested object field") {

                val input = "firstPerson: null"

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(NullableNestedObject.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe NullableNestedObject(null)
                    }
                }
            }

            context("given some input representing an object with a null value for a non-nullable nested list field") {
                val input = "members: null"

                context("parsing that input") {
                    test("throws an appropriate exception") {
                        val exception = shouldThrow<InvalidPropertyValueException> { Yaml.default.decodeFromString(Team.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Value for 'members' is invalid: Unexpected null or empty value for non-null field."
                            it.line shouldBe 1
                            it.column shouldBe 10
                            it.propertyName shouldBe "members"
                            it.reason shouldBe "Unexpected null or empty value for non-null field."
                            it.path shouldBe YamlPath.root.withMapElementKey("members", Location(1, 1)).withMapElementValue(Location(1, 10))
                        }
                    }
                }
            }

            context("given some input representing an object with a null value for a nullable nested list field") {
                val input = "members: null"

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(NullableNestedList.serializer(), input)

                    test("deserializes it to a Kotlin object") {
                        result shouldBe NullableNestedList(null)
                    }
                }
            }

            context("given some input representing an object with a custom serializer for one of its values") {
                val input = "value: something"

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<LocationInformationException> { Yaml.default.decodeFromString(StructureWithLocationThrowingSerializer.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Serializer called with location (1, 8) and path: value"
                        }
                    }
                }
            }

            context("given some input representing a generic map") {
                val input = """
                    SOME_ENV_VAR: somevalue
                    SOME_OTHER_ENV_VAR: someothervalue
                """.trimIndent()

                context("parsing that input") {
                    val result = Yaml.default.decodeFromString(MapSerializer(String.serializer(), String.serializer()), input)

                    test("deserializes it to a Kotlin map") {
                        result shouldBe
                            mapOf(
                                "SOME_ENV_VAR" to "somevalue",
                                "SOME_OTHER_ENV_VAR" to "someothervalue",
                            )
                    }
                }

                context("parsing that input with a serializer for the key that uses YAML location information when throwing exceptions") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<LocationInformationException> { Yaml.default.decodeFromString(MapSerializer(LocationThrowingSerializer, String.serializer()), input) }

                        exception.asClue {
                            it.message shouldBe "Serializer called with location (1, 1) and path: SOME_ENV_VAR"
                        }
                    }
                }

                context("parsing that input with a serializer for the value that uses YAML location information when throwing exceptions") {
                    test("throws an exception with the correct location information") {
                        val exception = shouldThrow<LocationInformationException> { Yaml.default.decodeFromString(MapSerializer(String.serializer(), LocationThrowingSerializer), input) }

                        exception.asClue {
                            it.message shouldBe "Serializer called with location (1, 15) and path: SOME_ENV_VAR"
                        }
                    }
                }
            }

            context("given some input with some extensions") {
                val input = """
                    .some-extension: &name Jamie

                    name: *name
                """.trimIndent()

                context("parsing anchors and aliases is disabled") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", anchorsAndAliases = AnchorsAndAliases.Forbidden)
                    val yaml = Yaml(configuration = configuration)

                    test("throws an appropriate exception") {
                        val exception = shouldThrow<ForbiddenAnchorOrAliasException> { yaml.decodeFromString(SimpleStructure.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Parsing anchors and aliases is disabled."
                            it.line shouldBe 1
                            it.column shouldBe 18
                        }
                    }
                }

                context("parsing anchors and aliases is enabled") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", anchorsAndAliases = AnchorsAndAliases.Permitted())
                    val yaml = Yaml(configuration = configuration)
                    val result = yaml.decodeFromString(SimpleStructure.serializer(), input)

                    test("deserializes it to a Kotlin object, replacing the reference to the extension with the extension") {
                        result shouldBe SimpleStructure("Jamie")
                    }
                }
            }

            context("restricting max alias count") {
                val input = """
                    .some-extension: &name Jamie

                    members: [*name, *name]
                """.trimIndent()

                context("parsing anchors and aliases is disabled") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", anchorsAndAliases = AnchorsAndAliases.Forbidden)
                    val yaml = Yaml(configuration = configuration)

                    test("throws an appropriate exception") {
                        val exception = shouldThrow<ForbiddenAnchorOrAliasException> { yaml.decodeFromString(Team.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Parsing anchors and aliases is disabled."
                            it.line shouldBe 1
                            it.column shouldBe 18
                        }
                    }
                }

                context("parsing anchors and aliases is enabled, max aliases count 0") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", anchorsAndAliases = AnchorsAndAliases.Permitted(maxAliasCount = 0u))
                    val yaml = Yaml(configuration = configuration)

                    test("throws an appropriate exception") {
                        val exception = shouldThrow<ForbiddenAnchorOrAliasException> { yaml.decodeFromString(Team.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Parsing anchors and aliases is disabled."
                            it.line shouldBe 1
                            it.column shouldBe 18
                        }
                    }
                }

                context("parsing anchors and aliases is enabled, max aliases count 1") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", anchorsAndAliases = AnchorsAndAliases.Permitted(maxAliasCount = 1u))
                    val yaml = Yaml(configuration = configuration)

                    test("throws an appropriate exception") {
                        val exception = shouldThrow<ForbiddenAnchorOrAliasException> { yaml.decodeFromString(Team.serializer(), input) }

                        exception.asClue {
                            it.message shouldBe "Maximum number of aliases has been reached."
                            it.line shouldBe 3
                            it.column shouldBe 18
                        }
                    }
                }

                context("parsing anchors and aliases is enabled, max aliases count 2") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", anchorsAndAliases = AnchorsAndAliases.Permitted(maxAliasCount = 2u))
                    val yaml = Yaml(configuration = configuration)
                    val result = yaml.decodeFromString(Team.serializer(), input)

                    test("deserializes it to a Kotlin object, replacing the reference to the extension with the extension") {
                        result shouldBe Team(listOf("Jamie", "Jamie"))
                    }
                }

                context("parsing anchors and aliases is enabled, max aliases count null") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", anchorsAndAliases = AnchorsAndAliases.Permitted(maxAliasCount = null))
                    val yaml = Yaml(configuration = configuration)
                    val result = yaml.decodeFromString(Team.serializer(), input)

                    test("deserializes it to a Kotlin object, replacing the reference to the extension with the extension") {
                        result shouldBe Team(listOf("Jamie", "Jamie"))
                    }
                }

                context("parsing anchors and aliases is enabled, billion laughs is prevented by default") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", anchorsAndAliases = AnchorsAndAliases.Permitted())
                    val yaml = Yaml(configuration = configuration)

                    test("throws an appropriate exception") {
                        val exception = shouldThrow<ForbiddenAnchorOrAliasException> {
                            yaml.decodeFromString(
                                Team.serializer(),
                                """
                                    a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
                                    b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
                                    c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
                                    d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
                                    e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
                                    f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
                                    g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
                                    h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
                                    i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]
                                """.trimIndent(),
                            )
                        }

                        exception.asClue {
                            it.message shouldBe "Maximum number of aliases has been reached."
                            it.line shouldBe 4
                            it.column shouldBe 8
                        }
                    }
                }
            }

            context("given some input with an additional unknown field") {
                val input = """
                    name: Blah Blahson
                    extra-field: Hello
                """.trimIndent()

                context("given strict mode is enabled") {
                    val configuration = YamlConfiguration(strictMode = true)
                    val yaml = Yaml(configuration = configuration)

                    context("parsing that input") {
                        test("throws an appropriate exception") {
                            val exception = shouldThrow<UnknownPropertyException> { yaml.decodeFromString(SimpleStructure.serializer(), input) }

                            exception.asClue {
                                it.message shouldBe "Unknown property 'extra-field'. Known properties are: name"
                                it.line shouldBe 2
                                it.column shouldBe 1
                                it.path shouldBe YamlPath.root.withMapElementKey("extra-field", Location(2, 1))
                            }
                        }
                    }
                }

                context("given strict mode is disabled") {
                    val configuration = YamlConfiguration(strictMode = false)
                    val yaml = Yaml(configuration = configuration)

                    context("parsing that input") {
                        test("ignores the extra field and returns a deserialised object") {
                            yaml.decodeFromString(SimpleStructure.serializer(), input) shouldBe SimpleStructure("Blah Blahson")
                        }
                    }
                }
            }

            context("given a nullable object") {
                val input = """
                    host: "db.test.com"
                """.trimIndent()

                val result = Yaml.default.decodeFromString(Database.serializer().nullable, input)

                test("deserializes it to the expected object") {
                    result shouldBe Database("db.test.com")
                }
            }
        }

        context("parsing nested map node") {
            val input = """
                text: "OK"
                node:
                    foo1: "bar"
                    foo2: null
                    foo3: 3.14
                    foo4:
                        - 1
                        - 2
                        - 3
                    foo5:
                        element1: 1
                        element2: 2
            """.trimIndent()

            context("parsing that input as a map node") {
                val result = Yaml.default.decodeFromString(TestClassWithNestedMap.serializer(), input)

                test("deserializes map") {
                    result.node.entries shouldHaveSize 5
                    result.node.get<YamlScalar>("foo1")!!.content shouldBe "bar"
                    result.node.get<YamlNull>("foo2").shouldBeInstanceOf<YamlNull>()
                    result.node.get<YamlScalar>("foo3")!!.toDouble() shouldBe 3.14
                    result.node.get<YamlList>("foo4")!!.items.map { it.yamlScalar.toInt() } shouldBe listOf(1, 2, 3)
                    result.node.get<YamlMap>("foo5")!!.get<YamlScalar>("element1")!!.toInt() shouldBe 1
                    result.node.get<YamlMap>("foo5")!!.get<YamlScalar>("element2")!!.toInt() shouldBe 2
                }
            }

            context("parsing that input as a node") {
                val result = Yaml.default.decodeFromString(TestClassWithNestedNode.serializer(), input)

                test("deserializes node to map") {
                    result.node.shouldBeInstanceOf<YamlMap>()
                }

                test("deserializes node to double list") {
                    val node = result.node.yamlMap
                    node.entries shouldHaveSize 5
                    node.get<YamlScalar>("foo1")!!.content shouldBe "bar"
                    node.get<YamlNull>("foo2").shouldBeInstanceOf<YamlNull>()
                    node.get<YamlScalar>("foo3")!!.toDouble() shouldBe 3.14
                    node.get<YamlList>("foo4")!!.items.map { it.yamlScalar.toInt() } shouldBe listOf(1, 2, 3)
                    node.get<YamlMap>("foo5")!!.get<YamlScalar>("element1")!!.toInt() shouldBe 1
                    node.get<YamlMap>("foo5")!!.get<YamlScalar>("element2")!!.toInt() shouldBe 2
                }
            }
        }

        context("parsing polymorphic values") {
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
                            val exception = shouldThrow<MissingRequiredPropertyException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

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

                context("given some input for an object where the property value should be a sealed class (inline)") {
                    val input = """
                        element: !<inlineString> "abcdef"
                    """.trimIndent()

                    context("parsing that input") {
                        val result = polymorphicYaml.decodeFromString(SealedWrapper.serializer(), input)

                        test("deserializes it to a Kotlin object") {
                            result shouldBe SealedWrapper(TestSealedStructure.InlineSealedString("abcdef"))
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
                        - !<inlineString> "testing"
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
                                    TestSealedStructure.InlineSealedString("testing"),
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
                            val exception = shouldThrow<InvalidPropertyValueException> { polymorphicYaml.decodeFromString(SealedWrapper.serializer(), input) }

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
                            val exception = shouldThrow<InvalidPropertyValueException> { polymorphicYaml.decodeFromString(PolymorphicWrapper.serializer(), input) }

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
                            val exception = shouldThrow<InvalidPropertyValueException> { polymorphicYaml.decodeFromString(PolymorphicWrapper.serializer(), input) }

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
                            val exception = shouldThrow<UnknownPolymorphicTypeException> { polymorphicYaml.decodeFromString(PolymorphicSerializer(UnsealedClass::class), input) }

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
                            val exception = shouldThrow<UnknownPolymorphicTypeException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

                            exception.asClue {
                                it.message shouldBe "Unknown type 'someOtherType'. Known types are: inlineString, sealedInt, sealedString"
                                it.line shouldBe 1
                                it.column shouldBe 1
                                it.typeName shouldBe "someOtherType"
                                it.validTypeNames shouldBe setOf("inlineString", "sealedInt", "sealedString")
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
                            val exception = shouldThrow<UnknownPolymorphicTypeException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

                            exception.asClue {
                                it.message shouldBe "Unknown type 'someOtherType'. Known types are: inlineString, sealedInt, sealedString"
                                it.line shouldBe 1
                                it.column shouldBe 1
                                it.typeName shouldBe "someOtherType"
                                it.validTypeNames shouldBe setOf("inlineString", "sealedInt", "sealedString")
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
                            val exception = shouldThrow<MissingRequiredPropertyException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

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
                                val exception = shouldThrow<InvalidPropertyValueException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

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
                            val exception = shouldThrow<UnknownPolymorphicTypeException> { polymorphicYaml.decodeFromString(PolymorphicSerializer(UnsealedClass::class), input) }

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
                            val exception = shouldThrow<UnknownPolymorphicTypeException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

                            exception.asClue {
                                it.message shouldBe "Unknown type 'someOtherType'. Known types are: inlineString, sealedInt, sealedString"
                                it.line shouldBe 1
                                it.column shouldBe 7
                                it.typeName shouldBe "someOtherType"
                                it.validTypeNames shouldBe setOf("inlineString", "sealedInt", "sealedString")
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
                            val exception = shouldThrow<MissingRequiredPropertyException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

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
                                val exception = shouldThrow<InvalidPropertyValueException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

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
                            val exception = shouldThrow<UnknownPolymorphicTypeException> { polymorphicYaml.decodeFromString(PolymorphicSerializer(UnsealedClass::class), input) }

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
                            val exception = shouldThrow<UnknownPolymorphicTypeException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

                            exception.asClue {
                                it.message shouldBe "Unknown type 'someOtherType'. Known types are: inlineString, sealedInt, sealedString"
                                it.line shouldBe 1
                                it.column shouldBe 7
                                it.typeName shouldBe "someOtherType"
                                it.validTypeNames shouldBe setOf("inlineString", "sealedInt", "sealedString")
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
                            val exception = shouldThrow<IncorrectTypeException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

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
                            val exception = shouldThrow<IncorrectTypeException> { polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input) }

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

        context("parsing nested tagged node") {
            val input = """
                text: "OK"
                node: !testtag 2024-01-01
            """.trimIndent()

            context("parsing that input as a tagged node") {
                val result = Yaml.default.decodeFromString(TestClassWithNestedTaggedNode.serializer(), input)

                test("deserializes tagged node") {
                    result.node.tag shouldBe "!testtag"
                    result.node.innerNode.shouldBeInstanceOf<YamlScalar>()
                    result.node.innerNode.yamlScalar.content shouldBe "2024-01-01"
                }
            }

            context("parsing that input as a node") {
                val result = Yaml.default.decodeFromString(TestClassWithNestedNode.serializer(), input)

                test("deserializes node to tagged node") {
                    result.node.shouldBeInstanceOf<YamlTaggedNode>()
                }

                test("deserializes node to tagged node content") {
                    val node = result.node.yamlTaggedNode
                    node.tag shouldBe "!testtag"
                    node.innerNode.shouldBeInstanceOf<YamlScalar>()
                    node.innerNode.yamlScalar.content shouldBe "2024-01-01"
                }
            }
        }

        context("parsing values with a dynamically installed serializer") {
            context("parsing a literal with a contextual serializer") {
                val contextSerializer = object : KSerializer<Inner> {
                    override val descriptor: SerialDescriptor
                        get() = String.serializer().descriptor

                    override fun deserialize(decoder: Decoder): Inner = Inner("from context serializer: ${decoder.decodeString()}")
                    override fun serialize(encoder: Encoder, value: Inner) = throw UnsupportedOperationException()
                }

                val module = serializersModuleOf(Inner::class, contextSerializer)
                val parser = Yaml(serializersModule = module)

                val input = """
                    inner: this is the input
                """.trimIndent()

                val result = parser.decodeFromString(Container.serializer(), input)

                test("deserializes it using the dynamically installed serializer") {
                    result shouldBe Container(Inner("from context serializer: this is the input"))
                }
            }

            context("parsing a class with a contextual serializer") {
                val contextSerializer = object : KSerializer<Inner> {
                    override val descriptor = buildClassSerialDescriptor("Inner") {
                        element("thing", String.serializer().descriptor)
                    }

                    override fun deserialize(decoder: Decoder): Inner {
                        val objectDecoder = decoder.beginStructure(descriptor)
                        val index = objectDecoder.decodeElementIndex(descriptor)
                        val name = objectDecoder.decodeStringElement(descriptor, index)
                        objectDecoder.endStructure(descriptor)

                        return Inner("$name, from context serializer")
                    }

                    override fun serialize(encoder: Encoder, value: Inner) = throw UnsupportedOperationException()
                }

                val module = serializersModuleOf(Inner::class, contextSerializer)
                val parser = Yaml(serializersModule = module)

                val input = """
                    inner:
                        thing: this is the input
                """.trimIndent()

                val result = parser.decodeFromString(Container.serializer(), input)

                test("deserializes it using the dynamically installed serializer") {
                    result shouldBe Container(Inner("this is the input, from context serializer"))
                }
            }

            context("parsing a map with a contextual serializer") {
                val contextSerializer = object : KSerializer<Inner> {
                    override val descriptor = buildSerialDescriptor("Inner", StructureKind.MAP) {
                        element("key", String.serializer().descriptor)
                        element("value", String.serializer().descriptor)
                    }

                    override fun deserialize(decoder: Decoder): Inner {
                        val objectDecoder = decoder.beginStructure(descriptor)
                        val keyIndex = objectDecoder.decodeElementIndex(descriptor)
                        val key = objectDecoder.decodeStringElement(descriptor, keyIndex)
                        val valueIndex = objectDecoder.decodeElementIndex(descriptor)
                        val value = objectDecoder.decodeStringElement(descriptor, valueIndex)

                        objectDecoder.endStructure(descriptor)

                        return Inner("$key: $value, from context serializer")
                    }

                    override fun serialize(encoder: Encoder, value: Inner) = throw UnsupportedOperationException()
                }

                val module = serializersModuleOf(Inner::class, contextSerializer)
                val parser = Yaml(serializersModule = module)

                val input = """
                    inner:
                        thing: this is the input
                """.trimIndent()

                val result = parser.decodeFromString(Container.serializer(), input)

                test("deserializes it using the dynamically installed serializer") {
                    result shouldBe Container(Inner("thing: this is the input, from context serializer"))
                }
            }
        }

        context("parsing values with mismatched types") {
            data class Scenario(
                val description: String,
                val serializer: KSerializer<out Any?>,
                val expectedErrorMessage: String = description,
            )

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

        context("parsing values with a contextual serializer") {
            mapOf(
                "scalar" to "2",
                "list" to "[ thing ]",
                "map" to "{ key: value }",
            ).forEach { (description, input) ->
                context("given some input representing a $description") {
                    context("parsing that input using a contextual serializer at the top level") {
                        val result = Yaml.default.decodeFromString(ContextualSerializer, input)

                        test("the serializer receives the top-level object") {
                            result shouldBe description
                        }
                    }

                    context("parsing that input using a contextual serializer nested within an object") {
                        val result = Yaml.default.decodeFromString(ObjectWithNestedContextualSerializer.serializer(), "thing: $input")

                        test("the serializer receives the correct object") {
                            result shouldBe ObjectWithNestedContextualSerializer(description)
                        }
                    }
                }
            }

            context("given the contextual serializer attempts to begin a structure that does not match the input") {
                context("given the input is a map") {
                    val input = "a: b"

                    mapOf(
                        PrimitiveKind.STRING to "a string",
                        StructureKind.LIST to "a list",
                    ).forEach { (kind, description) ->
                        context("attempting to begin $description") {
                            test("throws an exception with the correct location information") {
                                val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(ContextualSerializerThatAttemptsToDeserializeIncorrectType(kind), input) }

                                exception.asClue {
                                    it.message shouldBe "Expected $description, but got a map"
                                    it.line shouldBe 1
                                    it.column shouldBe 1
                                    it.path shouldBe YamlPath.root
                                }
                            }
                        }
                    }
                }

                context("given the input is a list") {
                    val input = "- a"

                    mapOf(
                        StructureKind.OBJECT to "an object",
                        StructureKind.CLASS to "an object",
                        StructureKind.MAP to "a map",
                        PrimitiveKind.STRING to "a string",
                    ).forEach { (kind, description) ->
                        context("attempting to begin $kind") {
                            test("throws an exception with the correct location information") {
                                val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(ContextualSerializerThatAttemptsToDeserializeIncorrectType(kind), input) }

                                exception.asClue {
                                    it.message shouldBe "Expected $description, but got a list"
                                    it.line shouldBe 1
                                    it.column shouldBe 1
                                    it.path shouldBe YamlPath.root
                                }
                            }
                        }
                    }
                }

                context("given the input is a scalar") {
                    val input = "2"

                    mapOf(
                        StructureKind.OBJECT to "an object",
                        StructureKind.CLASS to "an object",
                        StructureKind.MAP to "a map",
                        StructureKind.LIST to "a list",
                    ).forEach { (kind, description) ->
                        context("attempting to begin $kind") {
                            test("throws an exception with the correct location information") {
                                val exception = shouldThrow<IncorrectTypeException> { Yaml.default.decodeFromString(ContextualSerializerThatAttemptsToDeserializeIncorrectType(kind), input) }

                                exception.asClue {
                                    it.message shouldBe "Expected $description, but got a scalar value"
                                    it.line shouldBe 1
                                    it.column shouldBe 1
                                    it.path shouldBe YamlPath.root
                                }
                            }
                        }
                    }
                }
            }

            context("decoding from a YamlNode") {
                val input = """
                    keyA:
                        host: A
                    keyB:
                        host: B
                """.trimIndent()

                val mapAsListSerializer = object : KSerializer<List<Database>> {
                    override val descriptor = buildSerialDescriptor("DatabaseList", StructureKind.MAP) {
                    }

                    override fun deserialize(decoder: Decoder): List<Database> {
                        check(decoder is YamlInput)
                        return decoder.node.yamlMap.entries.map { (_, value) ->
                            decoder.yaml.decodeFromYamlNode(Database.serializer(), value)
                        }
                    }

                    override fun serialize(encoder: Encoder, value: List<Database>) = throw UnsupportedOperationException()
                }

                val parser = Yaml.default
                val result = parser.decodeFromString(mapAsListSerializer, input)

                test("decodes the map value as a list using the YamlNode") {
                    result shouldBe listOf(Database("A"), Database("B"))
                }
            }

            context("decoding from a YamlNode at a non-root node") {
                val input = """
                    databaseListing:
                        keyA:
                            host: A
                        keyB:
                            host: B
                """.trimIndent()

                val parser = Yaml.default
                val result = parser.decodeFromString(ServerConfig.serializer(), input)

                test("decodes the map value as a list using the YamlNode") {
                    result shouldBe ServerConfig(DatabaseListing(listOf(Database("A"), Database("B"))))
                }
            }

            context("decoding with a custom serializer for a non-root node") {
                val theInput = """
                    objectWithCustomSerializer:
                        - cats
                        - dogs
                        - birds
                """.trimIndent()

                val result = Yaml.default.decodeFromString(ObjectContainingObjectWithCustomSerializer.serializer(), theInput)

                test("decodes the Yaml as an ObjectContainingObjectWithCustomSerializer") {
                    result shouldBe ObjectContainingObjectWithCustomSerializer(
                        ObjectWithCustomSerializer("cats;dogs;birds"),
                    )
                }
            }
        }
    }
})

@Serializable
private data class ComplexStructure(
    val string: String,
    val byte: Byte,
    val short: Short,
    val int: Int,
    val long: Long,
    val float: Float,
    val double: Double,
    val enum: TestEnum,
    val boolean: Boolean,
    val char: Char,
    val nullable: String? = null,
)

@Serializable
private data class StructureWithLocationThrowingSerializer(
    @Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
    @Serializable(with = LocationThrowingSerializer::class)
    val value: CustomSerializedValue,
)

private data class CustomSerializedValue(val thing: String)

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
object ContextualSerializer : KSerializer<String> {
    override val descriptor = buildSerialDescriptor("ContextualSerializer", SerialKind.CONTEXTUAL) {
        element("string", PrimitiveSerialDescriptor("value", PrimitiveKind.STRING))
        element("object", buildSerialDescriptor("thing", StructureKind.OBJECT))
    }

    override fun deserialize(decoder: Decoder): String {
        val input = decoder.beginStructure(descriptor) as YamlInput
        val type = input.node::class.simpleName!!
        input.endStructure(descriptor)

        return type.removePrefix("Yaml").lowercase()
    }

    override fun serialize(encoder: Encoder, value: String): Unit = throw UnsupportedOperationException()
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class ContextualSerializerThatAttemptsToDeserializeIncorrectType(private val kind: SerialKind) : KSerializer<String> {
    private val innerDescriptor = if (kind == StructureKind.CLASS) buildClassSerialDescriptor("thing") else buildSerialDescriptor("thing", kind)

    override val descriptor = buildSerialDescriptor("ContextualSerializer", SerialKind.CONTEXTUAL) {
        element("string", PrimitiveSerialDescriptor("value", PrimitiveKind.STRING))
        element("object", innerDescriptor)
    }

    override fun deserialize(decoder: Decoder): String {
        val input = decoder.beginStructure(descriptor) as YamlInput

        input.beginStructure(innerDescriptor)

        return "Should never get to this point"
    }

    override fun serialize(encoder: Encoder, value: String): Unit = throw UnsupportedOperationException()
}

// FIXME: ideally these would just be inline in the test cases that need them, but due to
// https://github.com/Kotlin/kotlinx.serialization/issues/1427, this is no longer possible with
// kotlinx.serialization 1.2 and above.
// See also https://github.com/Kotlin/kotlinx.serialization/issues/1468.

@Serializable
private data class NullableNestedObject(val firstPerson: SimpleStructure?)

@Serializable
data class NullableNestedList(val members: List<String>?)

@Serializable
private data class Database(val host: String)

@Serializable(with = DecodingFromYamlNodeSerializer::class)
private data class DatabaseListing(val databases: List<Database>)

@Serializable
private data class ServerConfig(val databaseListing: DatabaseListing)

private data class Inner(val name: String)

@Serializable
private data class Container(@Contextual val inner: Inner)

@Serializable
private data class ObjectWithNestedContextualSerializer(@Serializable(with = ContextualSerializer::class) val thing: String)

@Serializable
@JvmInline
value class StringValue(val value: String)

private object DecodingFromYamlNodeSerializer : KSerializer<DatabaseListing> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("DecodingFromYamlNodeSerializer", StructureKind.MAP)

    override fun deserialize(decoder: Decoder): DatabaseListing {
        check(decoder is YamlInput)

        val list = decoder.node.yamlMap.entries.map { (_, value) ->
            decoder.yaml.decodeFromYamlNode(Database.serializer(), value)
        }

        return DatabaseListing(list)
    }

    override fun serialize(encoder: Encoder, value: DatabaseListing) = throw UnsupportedOperationException()
}

@Serializable
private data class ObjectContainingObjectWithCustomSerializer(
    val objectWithCustomSerializer: ObjectWithCustomSerializer,
)

@Serializable(with = SerializerForObjectWithCustomSerializer::class)
private data class ObjectWithCustomSerializer(
    val combinedValues: String,
)

private object SerializerForObjectWithCustomSerializer : KSerializer<ObjectWithCustomSerializer> {
    private const val ValuesSeparator = ";"

    private val listSerializer = ListSerializer(String.serializer())
    override val descriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ObjectWithCustomSerializer) {
        encoder.encodeSerializableValue(listSerializer, value.combinedValues.split(ValuesSeparator))
    }

    override fun deserialize(decoder: Decoder): ObjectWithCustomSerializer {
        check(decoder is YamlInput)

        // Intentionally parse the values from the current yaml node
        val values = decoder.node.yamlList.items
            .map { it.yamlScalar.content }

        return ObjectWithCustomSerializer(values.joinToString(ValuesSeparator))
    }
}
