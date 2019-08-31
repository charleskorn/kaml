/*

   Copyright 2018-2019 Charles Korn.

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

import ch.tutteli.atrium.api.cc.en_GB.message
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.assert
import com.charleskorn.kaml.testobjects.NestedObjects
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.Team
import com.charleskorn.kaml.testobjects.TestEnum
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.BooleanSerializer
import kotlinx.serialization.internal.ByteSerializer
import kotlinx.serialization.internal.CharSerializer
import kotlinx.serialization.internal.DoubleSerializer
import kotlinx.serialization.internal.EnumSerializer
import kotlinx.serialization.internal.FloatSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.LongSerializer
import kotlinx.serialization.internal.ShortSerializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.internal.makeNullable
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.modules.serializersModuleOf
import kotlinx.serialization.serializer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlReadingTest : Spek({
    describe("a YAML parser") {
        describe("parsing scalars") {
            context("given the input 'hello'") {
                val input = "hello"

                context("parsing that input as a string") {
                    val result = Yaml.default.parse(StringSerializer, input)

                    it("deserializes it to the expected string value") {
                        assert(result).toBe("hello")
                    }
                }

                context("parsing that input as a nullable string") {
                    val result = Yaml.default.parse(makeNullable(StringSerializer), input)

                    it("deserializes it to the expected string value") {
                        assert(result).toBe("hello")
                    }
                }

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse(LocationThrowingSerializer, input) }).toThrow<LocationInformationException> {
                            message { toBe("Serializer called with location: 1, 1") }
                        }
                    }
                }
            }

            context("given the input '123'") {
                val input = "123"

                context("parsing that input as an integer") {
                    val result = Yaml.default.parse(Int.serializer(), input)

                    it("deserializes it to the expected integer") {
                        assert(result).toBe(123)
                    }
                }

                context("parsing that input as a long") {
                    val result = Yaml.default.parse(Long.serializer(), input)

                    it("deserializes it to the expected long") {
                        assert(result).toBe(123)
                    }
                }

                context("parsing that input as a short") {
                    val result = Yaml.default.parse(Short.serializer(), input)

                    it("deserializes it to the expected short") {
                        assert(result).toBe(123)
                    }
                }

                context("parsing that input as a byte") {
                    val result = Yaml.default.parse(Byte.serializer(), input)

                    it("deserializes it to the expected byte") {
                        assert(result).toBe(123)
                    }
                }

                context("parsing that input as a double") {
                    val result = Yaml.default.parse(Double.serializer(), input)

                    it("deserializes it to the expected double") {
                        assert(result).toBe(123.0)
                    }
                }

                context("parsing that input as a float") {
                    val result = Yaml.default.parse(FloatSerializer, input)

                    it("deserializes it to the expected float") {
                        assert(result).toBe(123.0f)
                    }
                }

                context("parsing that input as a nullable integer") {
                    val result = Yaml.default.parse(makeNullable(Int.serializer()), input)

                    it("deserializes it to the expected integer") {
                        assert(result).toBe(123)
                    }
                }

                context("parsing that input as a nullable long") {
                    val result = Yaml.default.parse(makeNullable(Long.serializer()), input)

                    it("deserializes it to the expected long") {
                        assert(result).toBe(123)
                    }
                }

                context("parsing that input as a nullable short") {
                    val result = Yaml.default.parse(makeNullable(Short.serializer()), input)

                    it("deserializes it to the expected short") {
                        assert(result).toBe(123)
                    }
                }

                context("parsing that input as a nullable byte") {
                    val result = Yaml.default.parse(makeNullable(Byte.serializer()), input)

                    it("deserializes it to the expected byte") {
                        assert(result).toBe(123)
                    }
                }

                context("parsing that input as a nullable double") {
                    val result = Yaml.default.parse(makeNullable(Double.serializer()), input)

                    it("deserializes it to the expected double") {
                        assert(result).toBe(123.0)
                    }
                }

                context("parsing that input as a nullable float") {
                    val result = Yaml.default.parse(makeNullable(FloatSerializer), input)

                    it("deserializes it to the expected float") {
                        assert(result).toBe(123.0f)
                    }
                }
            }

            context("given the input 'true'") {
                val input = "true"

                context("parsing that input as a boolean") {
                    val result = Yaml.default.parse(BooleanSerializer, input)

                    it("deserializes it to the expected boolean value") {
                        assert(result).toBe(true)
                    }
                }

                context("parsing that input as a nullable boolean") {
                    val result = Yaml.default.parse(makeNullable(BooleanSerializer), input)

                    it("deserializes it to the expected boolean value") {
                        assert(result).toBe(true)
                    }
                }
            }

            context("given the input 'c'") {
                val input = "c"

                context("parsing that input as a character") {
                    val result = Yaml.default.parse(CharSerializer, input)

                    it("deserializes it to the expected character value") {
                        assert(result).toBe('c')
                    }
                }

                context("parsing that input as a nullable character") {
                    val result = Yaml.default.parse(makeNullable(CharSerializer), input)

                    it("deserializes it to the expected character value") {
                        assert(result).toBe('c')
                    }
                }
            }

            mapOf(
                "Value1" to TestEnum.Value1,
                "Value2" to TestEnum.Value2
            ).forEach { input, expectedValue ->
                context("given the input '$input'") {
                    context("parsing that input as an enumeration value") {
                        val result = Yaml.default.parse(EnumSerializer(TestEnum::class), input)

                        it("deserializes it to the expected enumeration value") {
                            assert(result).toBe(expectedValue)
                        }
                    }
                }
            }

            context("parsing an invalid enumeration value") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(EnumSerializer(TestEnum::class), "nonsense") }).toThrow<YamlScalarFormatException> {
                        message { toBe("Value 'nonsense' is not a valid option, permitted choices are: Value1, Value2") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }
        }

        describe("parsing null values") {
            val input = "null"

            context("parsing a null value as a nullable string") {
                val result = Yaml.default.parse(makeNullable(StringSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable string") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(StringSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable integer") {
                val result = Yaml.default.parse(makeNullable(IntSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable integer") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(IntSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable long") {
                val result = Yaml.default.parse(makeNullable(LongSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable long") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(LongSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable short") {
                val result = Yaml.default.parse(makeNullable(ShortSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable short") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(ShortSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable byte") {
                val result = Yaml.default.parse(makeNullable(ByteSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable byte") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(ByteSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable double") {
                val result = Yaml.default.parse(makeNullable(DoubleSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable double") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(DoubleSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable float") {
                val result = Yaml.default.parse(makeNullable(FloatSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable float") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(FloatSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable boolean") {
                val result = Yaml.default.parse(makeNullable(BooleanSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable boolean") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(BooleanSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable character") {
                val result = Yaml.default.parse(makeNullable(CharSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable character") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(CharSerializer, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable enum") {
                val result = Yaml.default.parse(makeNullable(EnumSerializer(TestEnum::class)), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable enum") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(EnumSerializer(TestEnum::class), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable list") {
                val result = Yaml.default.parse(makeNullable(StringSerializer.list), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable list") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(StringSerializer.list, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable object") {
                val result = Yaml.default.parse(makeNullable(ComplexStructure.serializer()), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable object") {
                it("throws an appropriate exception") {
                    assert({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value with a serializer that uses YAML location information when throwing exceptions") {
                it("throws an exception with the correct location information") {
                    assert({ Yaml.default.parse(LocationThrowingSerializer, input) }).toThrow<LocationInformationException> {
                        message { toBe("Serializer called with location: 1, 1") }
                    }
                }
            }
        }

        describe("parsing lists") {
            context("given a list of strings") {
                val input = """
                    - thing1
                    - thing2
                    - thing3
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(String.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf("thing1", "thing2", "thing3"))
                    }
                }

                context("parsing that input as a nullable list") {
                    val result = Yaml.default.parse(makeNullable(String.serializer().list), input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf("thing1", "thing2", "thing3"))
                    }
                }

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse(LocationThrowingSerializer.list, input) }).toThrow<LocationInformationException> {
                            message { toBe("Serializer called with location: 1, 3") }
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
                    val result = Yaml.default.parse(Int.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123, 45, 6))
                    }
                }

                context("parsing that input as a list of longs") {
                    val result = Yaml.default.parse(Long.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123L, 45, 6))
                    }
                }

                context("parsing that input as a list of shorts") {
                    val result = Yaml.default.parse(Short.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123.toShort(), 45, 6))
                    }
                }

                context("parsing that input as a list of bytes") {
                    val result = Yaml.default.parse(Byte.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123.toByte(), 45, 6))
                    }
                }

                context("parsing that input as a list of doubles") {
                    val result = Yaml.default.parse(Double.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123.0, 45.0, 6.0))
                    }
                }

                context("parsing that input as a list of floats") {
                    val result = Yaml.default.parse(FloatSerializer.list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123.0f, 45.0f, 6.0f))
                    }
                }
            }

            context("given a list of booleans") {
                val input = """
                    - true
                    - false
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(Boolean.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(true, false))
                    }
                }
            }

            context("given a list of enum values") {
                val input = """
                    - Value1
                    - Value2
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(EnumSerializer(TestEnum::class).list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(TestEnum.Value1, TestEnum.Value2))
                    }
                }
            }

            context("given a list of characters") {
                val input = """
                    - a
                    - b
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(CharSerializer.list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf('a', 'b'))
                    }
                }
            }

            context("given a list of nullable strings") {
                val input = """
                    - thing1
                    - null
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(makeNullable(String.serializer()).list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf("thing1", null))
                    }
                }
            }

            context("given a list of lists") {
                val input = """
                    - [thing1, thing2]
                    - [thing3]
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(String.serializer().list.list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(
                            listOf(
                                listOf("thing1", "thing2"),
                                listOf("thing3")
                            )
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
                    val result = Yaml.default.parse(SimpleStructure.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(
                            listOf(
                                SimpleStructure("thing1"),
                                SimpleStructure("thing2")
                            )
                        )
                    }
                }
            }
        }

        describe("parsing objects") {
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
                    val result = Yaml.default.parse(ComplexStructure.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(
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
                                "present"
                            )
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
                    val result = Yaml.default.parse(ComplexStructure.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(
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
                                null
                            )
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
                    val result = Yaml.default.parse(ComplexStructure.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(
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
                                null
                            )
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
                    val result = Yaml.default.parse(Team.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(Team(listOf("Alex", "Jamie")))
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
                    val result = Yaml.default.parse(NestedObjects.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(NestedObjects(SimpleStructure("Alex"), SimpleStructure("Jamie")))
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
                    val result = Yaml.default.parse(NestedObjects.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(NestedObjects(SimpleStructure("Alex"), SimpleStructure("Jamie")))
                    }
                }
            }

            context("given some input representing an object with an unknown key") {
                val input = """
                    abc123: something
                """.trimIndent()

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<UnknownPropertyException> {
                            message { toBe("Unknown property 'abc123'. Known properties are: boolean, byte, char, double, enum, float, int, long, nullable, short, string") }
                            line { toBe(1) }
                            column { toBe(1) }
                            propertyName { toBe("abc123") }
                            validPropertyNames { toBe(setOf("boolean", "byte", "char", "double", "enum", "float", "int", "long", "nullable", "short", "string")) }
                        }
                    }
                }
            }

            context("given some input representing an object with a list as a key") {
                val input = """
                    []: something
                """.trimIndent()

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<MalformedYamlException> {
                            message { toBe("Property name must not be a list, map or null value. (To use 'null' as a property name, enclose it in quotes.)") }
                            line { toBe(1) }
                            column { toBe(1) }
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
                    "char" to "Value 'xxx' is not a valid character value."
                ).forEach { fieldName, errorMessage ->
                    context("given the invalid field represents a $fieldName") {
                        val input = "$fieldName: xxx"

                        context("parsing that input") {
                            it("throws an appropriate exception") {
                                assert({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                                    message { toBe("Value for '$fieldName' is invalid: $errorMessage") }
                                    line { toBe(1) }
                                    column { toBe(fieldName.length + 3) }
                                    propertyName { toBe(fieldName) }
                                    reason { toBe(errorMessage) }
                                }
                            }
                        }
                    }
                }
            }

            context("given some input representing an object with a null value for a non-nullable scalar field") {
                val input = "name: null"

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({ Yaml.default.parse(SimpleStructure.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'name' is invalid: Unexpected null or empty value for non-null field.") }
                            line { toBe(1) }
                            column { toBe(7) }
                            propertyName { toBe("name") }
                            reason { toBe("Unexpected null or empty value for non-null field.") }
                        }
                    }
                }
            }

            context("given some input representing an object with a null value for a non-nullable nested object field") {
                val input = "firstPerson: null"

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({ Yaml.default.parse(NestedObjects.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'firstPerson' is invalid: Unexpected null or empty value for non-null field.") }
                            line { toBe(1) }
                            column { toBe(14) }
                            propertyName { toBe("firstPerson") }
                            reason { toBe("Unexpected null or empty value for non-null field.") }
                        }
                    }
                }
            }

            context("given some input representing an object with a null value for a nullable nested object field") {
                @Serializable
                data class NullableNestedObject(val firstPerson: SimpleStructure?)

                val input = "firstPerson: null"

                context("parsing that input") {
                    val result = Yaml.default.parse(NullableNestedObject.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(NullableNestedObject(null))
                    }
                }
            }

            context("given some input representing an object with a null value for a non-nullable nested list field") {
                val input = "members: null"

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({ Yaml.default.parse(Team.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'members' is invalid: Unexpected null or empty value for non-null field.") }
                            line { toBe(1) }
                            column { toBe(10) }
                            propertyName { toBe("members") }
                            reason { toBe("Unexpected null or empty value for non-null field.") }
                        }
                    }
                }
            }

            context("given some input representing an object with a null value for a nullable nested list field") {
                @Serializable
                data class NullableNestedList(val members: List<String>?)

                val input = "members: null"

                context("parsing that input") {
                    val result = Yaml.default.parse(NullableNestedList.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(NullableNestedList(null))
                    }
                }
            }

            context("given some input representing an object with a custom serializer for one of its values") {
                val input = "value: something"

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse(StructureWithLocationThrowingSerializer.serializer(), input) }).toThrow<LocationInformationException> {
                            message { toBe("Serializer called with location: 1, 8") }
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
                    val result = Yaml.default.parse((StringSerializer to StringSerializer).map, input)

                    it("deserializes it to a Kotlin map") {
                        assert(result).toBe(
                            mapOf(
                                "SOME_ENV_VAR" to "somevalue",
                                "SOME_OTHER_ENV_VAR" to "someothervalue"
                            )
                        )
                    }
                }

                context("parsing that input with a serializer for the key that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse((LocationThrowingSerializer to StringSerializer).map, input) }).toThrow<LocationInformationException> {
                            message { toBe("Serializer called with location: 1, 1") }
                        }
                    }
                }

                context("parsing that input with a serializer for the value that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse((StringSerializer to LocationThrowingSerializer).map, input) }).toThrow<LocationInformationException> {
                            message { toBe("Serializer called with location: 1, 15") }
                        }
                    }
                }
            }

            context("given some input with some extensions") {
                val input = """
                    .some-extension: &name Jamie

                    name: *name
                """.trimIndent()

                context("parsing that input") {
                    val configuration = YamlConfiguration(extensionDefinitionPrefix = ".")
                    val yaml = Yaml(configuration = configuration)
                    val result = yaml.parse(SimpleStructure.serializer(), input)

                    it("deserializes it to a Kotlin object, replacing the reference to the extension with the extension") {
                        assert(result).toBe(SimpleStructure("Jamie"))
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
                        it("throws an appropriate exception") {
                            assert({ yaml.parse(SimpleStructure.serializer(), input) }).toThrow<UnknownPropertyException> {
                                message { toBe("Unknown property 'extra-field'. Known properties are: name") }
                                line { toBe(2) }
                                column { toBe(1) }
                            }
                        }
                    }
                }

                context("given strict mode is disabled") {
                    val configuration = YamlConfiguration(strictMode = false)
                    val yaml = Yaml(configuration = configuration)

                    context("parsing that input") {
                        it("ignores the extra field and returns a deserialised object") {
                            assert(yaml.parse(SimpleStructure.serializer(), input)).toBe(SimpleStructure("Blah Blahson"))
                        }
                    }
                }
            }

            context("given an object where the first property is nullable") {
                val input = """
                    mariaDb:
                      host: "db.test.com"
                """.trimIndent()

                @Serializable
                data class MariaDb(val host: String)

                @Serializable
                data class Server(val mariaDb: MariaDb? = null)

                val result = Yaml.default.parse(Server.serializer(), input)

                it("deserializes it to the expected object") {
                    assert(result).toBe(Server(MariaDb("db.test.com")))
                }
            }
        }

        describe("parsing values with a dynamically installed serializer") {
            data class Inner(val name: String)

            @Serializable
            data class Container(@ContextualSerialization val inner: Inner)

            val contextSerializer = object : KSerializer<Inner> {
                override val descriptor: SerialDescriptor
                    get() = StringDescriptor

                override fun deserialize(decoder: Decoder): Inner = Inner("from context serializer")
                override fun serialize(encoder: Encoder, obj: Inner) = throw UnsupportedOperationException()
            }

            val module = serializersModuleOf(Inner::class, contextSerializer)
            val parser = Yaml(context = module)

            context("given some input that should be parsed with a dynamically installed serializer") {
                val input = """
                    inner: this is the input
                """.trimIndent()

                val result = parser.parse(Container.serializer(), input)

                it("deserializes it using the dynamically installed serializer") {
                    assert(result).toBe(Container(Inner("from context serializer")))
                }
            }
        }

        describe("parsing values with mismatched types") {
            context("given a list") {
                listOf(
                    "a string" to StringSerializer,
                    "an integer" to IntSerializer,
                    "a long" to LongSerializer,
                    "a short" to ShortSerializer,
                    "a byte" to ByteSerializer,
                    "a double" to DoubleSerializer,
                    "a float" to FloatSerializer,
                    "a boolean" to BooleanSerializer,
                    "a character" to CharSerializer,
                    "an enumeration value" to EnumSerializer(TestEnum::class),
                    "a map" to (StringSerializer to StringSerializer).map,
                    "an object" to ComplexStructure.serializer(),
                    "a string" to makeNullable(StringSerializer)
                ).forEach { (description, serializer) ->
                    val input = "- thing"

                    context("parsing that input as $description") {
                        it("throws an exception with the correct location information") {
                            assert({ Yaml.default.parse(serializer, input) }).toThrow<IncorrectTypeException> {
                                message { toBe("Expected $description, but got a list") }
                                line { toBe(1) }
                                column { toBe(1) }
                            }
                        }
                    }
                }

                context("parsing that input as the value in a map") {
                    val input = """
                        key:
                            - some_value
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse((StringSerializer to StringSerializer).map, input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'key' is invalid: Expected a string, but got a list") }
                            line { toBe(2) }
                            column { toBe(5) }
                        }
                    }
                }

                context("parsing that input as the value in an object") {
                    val input = """
                        string:
                            - some_value
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'string' is invalid: Expected a string, but got a list") }
                            line { toBe(2) }
                            column { toBe(5) }
                        }
                    }
                }

                context("parsing that input as the value in a list") {
                    val input = """
                        - [ some_value ]
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse(StringSerializer.list, input) }).toThrow<IncorrectTypeException> {
                            message { toBe("Expected a string, but got a list") }
                            line { toBe(1) }
                            column { toBe(3) }
                        }
                    }
                }
            }

            context("given a map") {
                listOf(
                    "a string" to StringSerializer,
                    "an integer" to IntSerializer,
                    "a long" to LongSerializer,
                    "a short" to ShortSerializer,
                    "a byte" to ByteSerializer,
                    "a double" to DoubleSerializer,
                    "a float" to FloatSerializer,
                    "a boolean" to BooleanSerializer,
                    "a character" to CharSerializer,
                    "an enumeration value" to EnumSerializer(TestEnum::class),
                    "a list" to StringSerializer.list,
                    "a string" to makeNullable(StringSerializer)
                ).forEach { (description, serializer) ->
                    val input = "key: value"

                    context("parsing that input as $description") {
                        it("throws an exception with the correct location information") {
                            assert({ Yaml.default.parse(serializer, input) }).toThrow<IncorrectTypeException> {
                                message { toBe("Expected $description, but got a map") }
                                line { toBe(1) }
                                column { toBe(1) }
                            }
                        }
                    }
                }

                context("parsing that input as the value in a map") {
                    val input = """
                        key:
                            some_key: some_value
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse((StringSerializer to StringSerializer).map, input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'key' is invalid: Expected a string, but got a map") }
                            line { toBe(2) }
                            column { toBe(5) }
                        }
                    }
                }

                context("parsing that input as the value in an object") {
                    val input = """
                        string:
                            some_key: some_value
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'string' is invalid: Expected a string, but got a map") }
                            line { toBe(2) }
                            column { toBe(5) }
                        }
                    }
                }

                context("parsing that input as the value in a list") {
                    val input = """
                        - some_key: some_value
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse(StringSerializer.list, input) }).toThrow<IncorrectTypeException> {
                            message { toBe("Expected a string, but got a map") }
                            line { toBe(1) }
                            column { toBe(3) }
                        }
                    }
                }
            }

            context("given a scalar value") {
                mapOf(
                    "a list" to StringSerializer.list,
                    "a map" to (StringSerializer to StringSerializer).map,
                    "an object" to ComplexStructure.serializer()
                ).forEach { description, serializer ->
                    val input = "blah"

                    context("parsing that input as $description") {
                        it("throws an exception with the correct location information") {
                            assert({ Yaml.default.parse(serializer, input) }).toThrow<IncorrectTypeException> {
                                message { toBe("Expected $description, but got a scalar value") }
                                line { toBe(1) }
                                column { toBe(1) }
                            }
                        }
                    }
                }

                context("parsing that input as the value in a map") {
                    val input = """
                        key: some_value
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse((StringSerializer to StringSerializer.list).map, input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'key' is invalid: Expected a list, but got a scalar value") }
                            line { toBe(1) }
                            column { toBe(6) }
                        }
                    }
                }

                context("parsing that input as the value in an object") {
                    val input = """
                        members: some_value
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse(Team.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                            message { toBe("Value for 'members' is invalid: Expected a list, but got a scalar value") }
                            line { toBe(1) }
                            column { toBe(10) }
                        }
                    }
                }

                context("parsing that input as the value in a list") {
                    val input = """
                        - some_value
                    """.trimIndent()

                    it("throws an exception with the correct location information") {
                        assert({ Yaml.default.parse((StringSerializer.list).list, input) }).toThrow<IncorrectTypeException> {
                            message { toBe("Expected a list, but got a scalar value") }
                            line { toBe(1) }
                            column { toBe(3) }
                        }
                    }
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
    val nullable: String? = null
)

@Serializable
private data class StructureWithLocationThrowingSerializer(
    @Serializable(with = LocationThrowingSerializer::class) val value: CustomSerializedValue
)

private data class CustomSerializedValue(val thing: String)

@Serializer(forClass = CustomSerializedValue::class)
private object LocationThrowingSerializer : KSerializer<CustomSerializedValue> {
    override val descriptor: SerialDescriptor
        get() = StringDescriptor

    override fun deserialize(decoder: Decoder): CustomSerializedValue {
        val location = (decoder as YamlInput).getCurrentLocation()

        throw LocationInformationException("Serializer called with location: ${location.line}, ${location.column}")
    }

    override fun serialize(encoder: Encoder, obj: CustomSerializedValue) = throw UnsupportedOperationException()
}

private class LocationInformationException(message: String) : RuntimeException(message)
