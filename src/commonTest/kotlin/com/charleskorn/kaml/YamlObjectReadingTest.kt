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
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.Team
import com.charleskorn.kaml.testobjects.TestEnum
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

class YamlObjectReadingTest : FlatFunSpec({
    context("a YAML parser parsing objects") {
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
                    val exception = shouldThrow<InvalidPropertyValueException> {
                        Yaml.default.decodeFromString(
                            SimpleStructure.serializer(),
                            input,
                        )
                    }

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
                    val exception = shouldThrow<InvalidPropertyValueException> {
                        Yaml.default.decodeFromString(
                            NestedObjects.serializer(),
                            input,
                        )
                    }

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
                    val exception = shouldThrow<LocationInformationException> {
                        Yaml.default.decodeFromString(
                            MapSerializer(LocationThrowingSerializer, String.serializer()),
                            input,
                        )
                    }

                    exception.asClue {
                        it.message shouldBe "Serializer called with location (1, 1) and path: SOME_ENV_VAR"
                    }
                }
            }

            context("parsing that input with a serializer for the value that uses YAML location information when throwing exceptions") {
                test("throws an exception with the correct location information") {
                    val exception = shouldThrow<LocationInformationException> {
                        Yaml.default.decodeFromString(
                            MapSerializer(String.serializer(), LocationThrowingSerializer),
                            input,
                        )
                    }

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
                val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", allowAnchorsAndAliases = false)
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
                val configuration = YamlConfiguration(extensionDefinitionPrefix = ".", allowAnchorsAndAliases = true)
                val yaml = Yaml(configuration = configuration)
                val result = yaml.decodeFromString(SimpleStructure.serializer(), input)

                test("deserializes it to a Kotlin object, replacing the reference to the extension with the extension") {
                    result shouldBe SimpleStructure("Jamie")
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
            @Serializable
            data class Database(val host: String)

            val input = """
                    host: "db.test.com"
            """.trimIndent()

            val result = Yaml.default.decodeFromString(Database.serializer().nullable, input)

            test("deserializes it to the expected object") {
                result shouldBe Database("db.test.com")
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

// FIXME: ideally these would just be inline in the test cases that need them, but due to
// https://github.com/Kotlin/kotlinx.serialization/issues/1427, this is no longer possible with
// kotlinx.serialization 1.2 and above.
// See also https://github.com/Kotlin/kotlinx.serialization/issues/1468.

@Serializable
private data class NullableNestedObject(val firstPerson: SimpleStructure?)

@Serializable
private data class NullableNestedList(val members: List<String>?)
