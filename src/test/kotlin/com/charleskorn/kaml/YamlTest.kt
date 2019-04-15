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
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.context.SimpleModule
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
import kotlinx.serialization.serializer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlTest : Spek({
    describe("a YAML parser and serializer") {
        describe("parsing YAML") {
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
                        val result = Yaml(extensionDefinitionPrefix = ".").parse(SimpleStructure.serializer(), input)

                        it("deserializes it to a Kotlin object, replacing the reference to the extension with the extension") {
                            assert(result).toBe(SimpleStructure("Jamie"))
                        }
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

                val module = SimpleModule(Inner::class, contextSerializer)
                val parser = Yaml()
                parser.install(module)

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
        }

        describe("serializing to YAML") {
            describe("serializing null values") {
                val input = null as String?

                context("serializing a null string value") {
                    val output = Yaml.default.stringify(makeNullable(StringSerializer), input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("null")
                    }
                }
            }

            describe("serializing boolean values") {
                context("serializing a true value") {
                    val output = Yaml.default.stringify(BooleanSerializer, true)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("true")
                    }
                }

                context("serializing a false value") {
                    val output = Yaml.default.stringify(BooleanSerializer, false)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("false")
                    }
                }
            }

            describe("serializing byte values") {
                val output = Yaml.default.stringify(ByteSerializer, 12)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("12")
                }
            }

            describe("serializing character values") {
                context("serializing a alphanumeric character") {
                    val output = Yaml.default.stringify(CharSerializer, 'A')

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe(""""A"""")
                    }
                }

                context("serializing a double-quote character") {
                    val output = Yaml.default.stringify(CharSerializer, '"')

                    it("returns the value serialized in the expected YAML form, escaping the double-quote character") {
                        assert(output).toBe(""""\""""")
                    }
                }

                context("serializing a newline character") {
                    val output = Yaml.default.stringify(CharSerializer, '\n')

                    it("returns the value serialized in the expected YAML form, escaping the newline character") {
                        assert(output).toBe(""""\n"""")
                    }
                }
            }

            describe("serializing double values") {
                val output = Yaml.default.stringify(DoubleSerializer, 12.3)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("12.3")
                }
            }

            describe("serializing floating point values") {
                val output = Yaml.default.stringify(FloatSerializer, 45.6f)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("45.6")
                }
            }

            describe("serializing integer values") {
                val output = Yaml.default.stringify(IntSerializer, 12)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("12")
                }
            }

            describe("serializing long integer values") {
                val output = Yaml.default.stringify(LongSerializer, 12)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("12")
                }
            }

            describe("serializing short integer values") {
                val output = Yaml.default.stringify(ShortSerializer, 12)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("12")
                }
            }

            describe("serializing string values") {
                context("serializing a string without any special characters") {
                    val output = Yaml.default.stringify(StringSerializer, "hello world")

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe(""""hello world"""")
                    }
                }

                context("serializing an empty string") {
                    val output = Yaml.default.stringify(StringSerializer, "")

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""""""")
                    }
                }

                context("serializing the string 'null'") {
                    val output = Yaml.default.stringify(StringSerializer, "null")

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe(""""null"""")
                    }
                }

                context("serializing a multi-line string") {
                    val output = Yaml.default.stringify(StringSerializer, "This is line 1\nThis is line 2")

                    it("returns the value serialized in the expected YAML form, escaping the newline character") {
                        assert(output).toBe(""""This is line 1\nThis is line 2"""")
                    }
                }

                context("serializing a string containing a double-quote character") {
                    val output = Yaml.default.stringify(StringSerializer, """They said "hello" to me""")

                    it("returns the value serialized in the expected YAML form, escaping the double-quote characters") {
                        assert(output).toBe(""""They said \"hello\" to me"""")
                    }
                }
            }

            describe("serializing enumeration values") {
                val output = Yaml.default.stringify(EnumSerializer(TestEnum::class), TestEnum.Value1)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(""""Value1"""")
                }
            }

            describe("serializing lists") {
                context("serializing a list of integers") {
                    val output = Yaml.default.stringify(IntSerializer.list, listOf(1, 2, 3))

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            - 1
                            - 2
                            - 3
                        """.trimIndent())
                    }
                }

                context("serializing a list of nullable integers") {
                    val output = Yaml.default.stringify(makeNullable(IntSerializer).list, listOf(1, null, 3))

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            - 1
                            - null
                            - 3
                        """.trimIndent())
                    }
                }

                context("serializing a list of strings") {
                    val output = Yaml.default.stringify(StringSerializer.list, listOf("item1", "item2"))

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            - "item1"
                            - "item2"
                        """.trimIndent())
                    }
                }

                context("serializing a list of a list of integers") {
                    val input = listOf(
                        listOf(1, 2, 3),
                        listOf(4, 5)
                    )

                    val output = Yaml.default.stringify(IntSerializer.list.list, input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            - - 1
                              - 2
                              - 3
                            - - 4
                              - 5
                        """.trimIndent())
                    }
                }

                context("serializing a list of maps from strings to strings") {
                    val input = listOf(
                        mapOf(
                            "key1" to "value1",
                            "key2" to "value2"
                        ),
                        mapOf(
                            "key3" to "value3"
                        )
                    )

                    val serializer = (StringSerializer to StringSerializer).map.list
                    val output = Yaml.default.stringify(serializer, input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            - "key1": "value1"
                              "key2": "value2"
                            - "key3": "value3"
                        """.trimIndent())
                    }
                }

                context("serializing a list of objects") {
                    val input = listOf(
                        SimpleStructure("name1"),
                        SimpleStructure("name2")
                    )

                    val output = Yaml.default.stringify(SimpleStructure.serializer().list, input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            - name: "name1"
                            - name: "name2"
                        """.trimIndent())
                    }
                }
            }

            describe("serializing maps") {
                context("serializing a map of strings to strings") {
                    val input = mapOf(
                        "key1" to "value1",
                        "key2" to "value2"
                    )

                    val output = Yaml.default.stringify((StringSerializer to StringSerializer).map, input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            "key1": "value1"
                            "key2": "value2"
                        """.trimIndent())
                    }
                }

                context("serializing a nested map of strings to strings") {
                    val input = mapOf(
                        "map1" to mapOf(
                            "key1" to "value1",
                            "key2" to "value2"
                        ),
                        "map2" to mapOf(
                            "key3" to "value3"
                        )
                    )

                    val serializer = (StringSerializer to (StringSerializer to StringSerializer).map).map
                    val output = Yaml.default.stringify(serializer, input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            "map1":
                              "key1": "value1"
                              "key2": "value2"
                            "map2":
                              "key3": "value3"
                        """.trimIndent())
                    }
                }

                context("serializing a map of strings to lists") {
                    val input = mapOf(
                        "list1" to listOf(1, 2, 3),
                        "list2" to listOf(4, 5, 6)
                    )

                    val serializer = (StringSerializer to IntSerializer.list).map
                    val output = Yaml.default.stringify(serializer, input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            "list1":
                            - 1
                            - 2
                            - 3
                            "list2":
                            - 4
                            - 5
                            - 6
                        """.trimIndent())
                    }
                }

                context("serializing a map of strings to objects") {
                    val input = mapOf(
                        "item1" to SimpleStructure("name1"),
                        "item2" to SimpleStructure("name2")
                    )

                    val serializer = (StringSerializer to SimpleStructure.serializer()).map
                    val output = Yaml.default.stringify(serializer, input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            "item1":
                              name: "name1"
                            "item2":
                              name: "name2"
                        """.trimIndent())
                    }
                }
            }

            describe("serializing objects") {
                context("serializing a simple object") {
                    val input = SimpleStructure("The name")
                    val output = Yaml.default.stringify(SimpleStructure.serializer(), input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            name: "The name"
                        """.trimIndent())
                    }
                }

                context("serializing a nested object") {
                    val input = NestedObjects(
                        SimpleStructure("name1"),
                        SimpleStructure("name2")
                    )

                    val output = Yaml.default.stringify(NestedObjects.serializer(), input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            firstPerson:
                              name: "name1"
                            secondPerson:
                              name: "name2"
                        """.trimIndent())
                    }
                }

                context("serializing an object with a nested list") {
                    val input = Team(listOf("name1", "name2"))
                    val output = Yaml.default.stringify(Team.serializer(), input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            members:
                            - "name1"
                            - "name2"
                        """.trimIndent())
                    }
                }

                context("serializing an object with a nested map") {
                    @Serializable
                    data class ThingWithMap(val variables: Map<String, String>)

                    val input = ThingWithMap(mapOf(
                        "var1" to "value1",
                        "var2" to "value2"
                    ))

                    val output = Yaml.default.stringify(ThingWithMap.serializer(), input)

                    it("returns the value serialized in the expected YAML form") {
                        assert(output).toBe("""
                            variables:
                              "var1": "value1"
                              "var2": "value2"
                        """.trimIndent())
                    }
                }
            }
        }
    }
})

@Serializable
data class SimpleStructure(
    val name: String
)

@Serializable
data class ComplexStructure(
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
    @Optional val nullable: String? = null
)

@Serializable
data class Team(
    val members: List<String>
)

@Serializable
data class NestedObjects(
    val firstPerson: SimpleStructure,
    val secondPerson: SimpleStructure
)

@Serializable
data class StructureWithLocationThrowingSerializer(
    @Serializable(with = LocationThrowingSerializer::class) val value: CustomSerializedValue
)

data class CustomSerializedValue(val thing: String)

enum class TestEnum {
    Value1,
    Value2
}

@Serializer(forClass = CustomSerializedValue::class)
object LocationThrowingSerializer : KSerializer<CustomSerializedValue> {
    override val descriptor: SerialDescriptor
        get() = StringDescriptor

    override fun deserialize(decoder: Decoder): CustomSerializedValue {
        val location = (decoder as YamlInput).getCurrentLocation()

        throw LocationInformationException("Serializer called with location: ${location.line}, ${location.column}")
    }

    override fun serialize(encoder: Encoder, obj: CustomSerializedValue) = throw UnsupportedOperationException()
}

class LocationInformationException(message: String) : RuntimeException(message)
