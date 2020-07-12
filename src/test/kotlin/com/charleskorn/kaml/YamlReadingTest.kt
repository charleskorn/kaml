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

import ch.tutteli.atrium.api.fluent.en_GB.cause
import ch.tutteli.atrium.api.fluent.en_GB.message
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import com.charleskorn.kaml.testobjects.NestedObjects
import com.charleskorn.kaml.testobjects.SealedWrapper
import com.charleskorn.kaml.testobjects.UnwrappedBoolean
import com.charleskorn.kaml.testobjects.UnwrappedByte
import com.charleskorn.kaml.testobjects.UnwrappedChar
import com.charleskorn.kaml.testobjects.UnwrappedClass
import com.charleskorn.kaml.testobjects.UnwrappedDouble
import com.charleskorn.kaml.testobjects.UnwrappedEnum
import com.charleskorn.kaml.testobjects.UnwrappedFloat
import com.charleskorn.kaml.testobjects.UnwrappedInt
import com.charleskorn.kaml.testobjects.UnwrappedInterface
import com.charleskorn.kaml.testobjects.UnwrappedLong
import com.charleskorn.kaml.testobjects.UnwrappedNull
import com.charleskorn.kaml.testobjects.UnwrappedNullableInt
import com.charleskorn.kaml.testobjects.UnwrappedShort
import com.charleskorn.kaml.testobjects.UnwrappedString
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.UnwrappedUnit
import com.charleskorn.kaml.testobjects.PolymorphicWrapper
import com.charleskorn.kaml.testobjects.Team
import com.charleskorn.kaml.testobjects.TestEnum
import com.charleskorn.kaml.testobjects.TestSealedStructure
import com.charleskorn.kaml.testobjects.UnsealedClass
import com.charleskorn.kaml.testobjects.UnsealedString
import com.charleskorn.kaml.testobjects.polymorphicModule
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.StructureKind
import kotlinx.serialization.UnionKind
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.serializersModuleOf
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlReadingTest : Spek({
    describe("a YAML parser") {
        describe("parsing scalars") {
            context("given the input 'hello'") {
                val input = "hello"

                context("parsing that input as a string") {
                    val result = Yaml.default.parse(String.serializer(), input)

                    it("deserializes it to the expected string value") {
                        expect(result).toBe("hello")
                    }
                }

                context("parsing that input as a nullable string") {
                    val result = Yaml.default.parse(String.serializer().nullable, input)

                    it("deserializes it to the expected string value") {
                        expect(result).toBe("hello")
                    }
                }

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        expect({ Yaml.default.parse(LocationThrowingSerializer, input) }).toThrow<LocationInformationException> {
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
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a long") {
                    val result = Yaml.default.parse(Long.serializer(), input)

                    it("deserializes it to the expected long") {
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a short") {
                    val result = Yaml.default.parse(Short.serializer(), input)

                    it("deserializes it to the expected short") {
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a byte") {
                    val result = Yaml.default.parse(Byte.serializer(), input)

                    it("deserializes it to the expected byte") {
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a double") {
                    val result = Yaml.default.parse(Double.serializer(), input)

                    it("deserializes it to the expected double") {
                        expect(result).toBe(123.0)
                    }
                }

                context("parsing that input as a float") {
                    val result = Yaml.default.parse(Float.serializer(), input)

                    it("deserializes it to the expected float") {
                        expect(result).toBe(123.0f)
                    }
                }

                context("parsing that input as a nullable integer") {
                    val result = Yaml.default.parse(Int.serializer().nullable, input)

                    it("deserializes it to the expected integer") {
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a nullable long") {
                    val result = Yaml.default.parse(Long.serializer().nullable, input)

                    it("deserializes it to the expected long") {
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a nullable short") {
                    val result = Yaml.default.parse(Short.serializer().nullable, input)

                    it("deserializes it to the expected short") {
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a nullable byte") {
                    val result = Yaml.default.parse(Byte.serializer().nullable, input)

                    it("deserializes it to the expected byte") {
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a nullable double") {
                    val result = Yaml.default.parse(Double.serializer().nullable, input)

                    it("deserializes it to the expected double") {
                        expect(result).toBe(123.0)
                    }
                }

                context("parsing that input as a nullable float") {
                    val result = Yaml.default.parse(Float.serializer().nullable, input)

                    it("deserializes it to the expected float") {
                        expect(result).toBe(123.0f)
                    }
                }
            }

            context("given the input 'true'") {
                val input = "true"

                context("parsing that input as a boolean") {
                    val result = Yaml.default.parse(Boolean.serializer(), input)

                    it("deserializes it to the expected boolean value") {
                        expect(result).toBe(true)
                    }
                }

                context("parsing that input as a nullable boolean") {
                    val result = Yaml.default.parse(Boolean.serializer().nullable, input)

                    it("deserializes it to the expected boolean value") {
                        expect(result).toBe(true)
                    }
                }
            }

            context("given the input 'c'") {
                val input = "c"

                context("parsing that input as a character") {
                    val result = Yaml.default.parse(Char.serializer(), input)

                    it("deserializes it to the expected character value") {
                        expect(result).toBe('c')
                    }
                }

                context("parsing that input as a nullable character") {
                    val result = Yaml.default.parse(Char.serializer().nullable, input)

                    it("deserializes it to the expected character value") {
                        expect(result).toBe('c')
                    }
                }
            }

            mapOf(
                "Value1" to TestEnum.Value1,
                "Value2" to TestEnum.Value2
            ).forEach { input, expectedValue ->
                context("given the input '$input'") {
                    context("parsing that input as an enumeration value") {
                        val result = Yaml.default.parse(TestEnum.serializer(), input)

                        it("deserializes it to the expected enumeration value") {
                            expect(result).toBe(expectedValue)
                        }
                    }
                }
            }

            context("parsing an invalid enumeration value") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(TestEnum.serializer(), "nonsense") }).toThrow<YamlScalarFormatException> {
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
                val result = Yaml.default.parse(String.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable string") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(String.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable integer") {
                val result = Yaml.default.parse(Int.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable integer") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(Int.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable long") {
                val result = Yaml.default.parse(Long.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable long") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(Long.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable short") {
                val result = Yaml.default.parse(Short.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable short") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(Short.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable byte") {
                val result = Yaml.default.parse(Byte.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable byte") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(Byte.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable double") {
                val result = Yaml.default.parse(Double.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable double") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(Double.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable float") {
                val result = Yaml.default.parse(Float.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable float") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(Float.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable boolean") {
                val result = Yaml.default.parse(Boolean.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable boolean") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(Boolean.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable character") {
                val result = Yaml.default.parse(Char.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable character") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(Char.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable enum") {
                val result = Yaml.default.parse(TestEnum.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable enum") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(TestEnum.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable list") {
                val result = Yaml.default.parse(String.serializer().list.nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable list") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(String.serializer().list, input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value as a nullable object") {
                val result = Yaml.default.parse(ComplexStructure.serializer().nullable, input)

                it("returns a null value") {
                    expect(result).toBe(null)
                }
            }

            context("parsing a null value as a non-nullable object") {
                it("throws an appropriate exception") {
                    expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<UnexpectedNullValueException> {
                        message { toBe("Unexpected null or empty value for non-null field.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }

            context("parsing a null value with a serializer that uses YAML location information when throwing exceptions") {
                it("throws an exception with the correct location information") {
                    expect({ Yaml.default.parse(LocationThrowingSerializer, input) }).toThrow<LocationInformationException> {
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
                        expect(result).toBe(listOf("thing1", "thing2", "thing3"))
                    }
                }

                context("parsing that input as a nullable list") {
                    val result = Yaml.default.parse(String.serializer().list.nullable, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf("thing1", "thing2", "thing3"))
                    }
                }

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        expect({ Yaml.default.parse(LocationThrowingSerializer.list, input) }).toThrow<LocationInformationException> {
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
                        expect(result).toBe(listOf(123, 45, 6))
                    }
                }

                context("parsing that input as a list of longs") {
                    val result = Yaml.default.parse(Long.serializer().list, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf(123L, 45, 6))
                    }
                }

                context("parsing that input as a list of shorts") {
                    val result = Yaml.default.parse(Short.serializer().list, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf(123.toShort(), 45, 6))
                    }
                }

                context("parsing that input as a list of bytes") {
                    val result = Yaml.default.parse(Byte.serializer().list, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf(123.toByte(), 45, 6))
                    }
                }

                context("parsing that input as a list of doubles") {
                    val result = Yaml.default.parse(Double.serializer().list, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf(123.0, 45.0, 6.0))
                    }
                }

                context("parsing that input as a list of floats") {
                    val result = Yaml.default.parse(Float.serializer().list, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf(123.0f, 45.0f, 6.0f))
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
                        expect(result).toBe(listOf(true, false))
                    }
                }
            }

            context("given a list of enum values") {
                val input = """
                    - Value1
                    - Value2
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(TestEnum.serializer().list, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf(TestEnum.Value1, TestEnum.Value2))
                    }
                }
            }

            context("given a list of characters") {
                val input = """
                    - a
                    - b
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(Char.serializer().list, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf('a', 'b'))
                    }
                }
            }

            context("given a list of nullable strings") {
                val input = """
                    - thing1
                    - null
                """.trimIndent()

                context("parsing that input as a list") {
                    val result = Yaml.default.parse(String.serializer().nullable.list, input)

                    it("deserializes it to the expected value") {
                        expect(result).toBe(listOf("thing1", null))
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
                        expect(result).toBe(
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
                        expect(result).toBe(
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
                        expect(result).toBe(
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
                        expect(result).toBe(
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
                        expect(result).toBe(
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
                        expect(result).toBe(Team(listOf("Alex", "Jamie")))
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
                        expect(result).toBe(NestedObjects(SimpleStructure("Alex"), SimpleStructure("Jamie")))
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
                        expect(result).toBe(NestedObjects(SimpleStructure("Alex"), SimpleStructure("Jamie")))
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
                    val result = Yaml.default.parse(Int.serializer().list, input)
                    it("deserializes it to a list ignoring the tag") {
                        expect(result).toBe(listOf(5, 3))
                    }
                }

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        expect({ Yaml.default.parse(LocationThrowingSerializer, input) }).toThrow<LocationInformationException> {
                            message { toBe("Serializer called with location: 1, 1") }
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
                    val result = Yaml.default.parse(MapSerializer
                        (String.serializer(), String.serializer()), input)
                    it("deserializes it to a Map ignoring the tag") {
                        expect(result).toBe(mapOf("foo" to "bar"))
                    }
                }

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        expect({ Yaml.default.parse(LocationThrowingMapSerializer, input) }).toThrow<LocationInformationException> {
                            message { toBe("Serializer called with location: 1, 1") }
                        }
                    }
                }
            }

            context("given some input representing an object with an unknown key") {
                val input = """
                    abc123: something
                """.trimIndent()

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<UnknownPropertyException> {
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
                        expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<MalformedYamlException> {
                            message { toBe("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }
            }

            context("given some input representing an object with a null as a key") {
                val input = """
                    null: something
                """.trimIndent()

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<MalformedYamlException> {
                            message { toBe("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }
            }

            context("given some input representing an object with an object as a key") {
                val input = """
                    { }: something
                """.trimIndent()

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<MalformedYamlException> {
                            message { toBe("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }
            }

            context("given some input representing an object with a tagged value as a key") {
                val input = """
                    !<sealedInt> test: something
                """.trimIndent()

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<MalformedYamlException> {
                            message { toBe("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)") }
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
                                expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect({ Yaml.default.parse(SimpleStructure.serializer(), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect({ Yaml.default.parse(NestedObjects.serializer(), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect(result).toBe(NullableNestedObject(null))
                    }
                }
            }

            context("given some input representing an object with a null value for a non-nullable nested list field") {
                val input = "members: null"

                context("parsing that input") {
                    it("throws an appropriate exception") {
                        expect({ Yaml.default.parse(Team.serializer(), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect(result).toBe(NullableNestedList(null))
                    }
                }
            }

            context("given some input representing an object with a custom serializer for one of its values") {
                val input = "value: something"

                context("parsing that input with a serializer that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        expect({ Yaml.default.parse(StructureWithLocationThrowingSerializer.serializer(), input) }).toThrow<LocationInformationException> {
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
                    val result = Yaml.default.parse(MapSerializer(String.serializer(), String.serializer()), input)

                    it("deserializes it to a Kotlin map") {
                        expect(result).toBe(
                            mapOf(
                                "SOME_ENV_VAR" to "somevalue",
                                "SOME_OTHER_ENV_VAR" to "someothervalue"
                            )
                        )
                    }
                }

                context("parsing that input with a serializer for the key that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        expect({ Yaml.default.parse(MapSerializer(LocationThrowingSerializer, String.serializer()), input) }).toThrow<LocationInformationException> {
                            message { toBe("Serializer called with location: 1, 1") }
                        }
                    }
                }

                context("parsing that input with a serializer for the value that uses YAML location information when throwing exceptions") {
                    it("throws an exception with the correct location information") {
                        expect({ Yaml.default.parse(MapSerializer(String.serializer(), LocationThrowingSerializer), input) }).toThrow<LocationInformationException> {
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
                        expect(result).toBe(SimpleStructure("Jamie"))
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
                            expect({ yaml.parse(SimpleStructure.serializer(), input) }).toThrow<UnknownPropertyException> {
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
                            expect(yaml.parse(SimpleStructure.serializer(), input)).toBe(SimpleStructure("Blah Blahson"))
                        }
                    }
                }
            }

            context("given a nullable object") {
                val input = """
                    host: "db.test.com"
                """.trimIndent()

                @Serializable
                data class Database(val host: String)

                val result = Yaml.default.parse(Database.serializer().nullable, input)

                it("deserializes it to the expected object") {
                    expect(result).toBe(Database("db.test.com"))
                }
            }
        }

        describe("parsing polymorphic values") {
            describe("given tags are used to store the type information") {
                val polymorphicYaml = Yaml(context = polymorphicModule, configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Tags))

                context("given some input where the value should be a sealed class") {
                    val input = """
                        !<sealedString>
                        value: "asdfg"
                    """.trimIndent()

                    context("parsing that input") {
                        val result = polymorphicYaml.parse(TestSealedStructure.serializer(), input)

                        it("deserializes it to a Kotlin object") {
                            expect(result).toBe(TestSealedStructure.SimpleSealedString("asdfg"))
                        }
                    }

                    context("parsing that input as map") {
                        val result = polymorphicYaml.parse(MapSerializer(String.serializer(), String.serializer()), input)

                        it("deserializes it to a map ignoring the tag") {
                            expect(result).toBe(mapOf("value" to "asdfg"))
                        }
                    }
                }

                context("given some input where the value is a literal") {
                    val input = """
                        !<simpleString> "asdfg"
                    """.trimIndent()

                    context("parsing that input") {
                        val result = polymorphicYaml.parse(PolymorphicSerializer(UnwrappedInterface::class), input)

                        it("deserializes it to a Kotlin object") {
                            expect(result).toBe(UnwrappedString("asdfg"))
                        }
                    }

                    context("parsing that input as a string") {
                        val result = polymorphicYaml.parse(String.serializer(), input)

                        it("deserializes it to a string ignoring the tag") {
                            expect(result).toBe("asdfg")
                        }
                    }
                }

                context("given some input where the value should be an unsealed class") {
                    val input = """
                        !<unsealedString>
                        value: "asdfg"
                    """.trimIndent()

                    context("parsing that input") {
                        val result = polymorphicYaml.parse(PolymorphicSerializer(UnsealedClass::class), input)

                        it("deserializes it to a Kotlin object") {
                            expect(result).toBe(UnsealedString("asdfg"))
                        }
                    }

                    context("parsing that input as map") {
                        val result = polymorphicYaml.parse(MapSerializer(String.serializer(), String.serializer()), input)

                        it("deserializes it to a map ignoring the tag") {
                            expect(result).toBe(mapOf("value" to "asdfg"))
                        }
                    }
                }

                context("given some input for an object where the property value should be a sealed class") {
                    val input = """
                        element: !<sealedString>
                            value: "asdfg"
                    """.trimIndent()

                    context("parsing that input") {
                        val result = polymorphicYaml.parse(SealedWrapper.serializer(), input)

                        it("deserializes it to a Kotlin object") {
                            expect(result).toBe(SealedWrapper(TestSealedStructure.SimpleSealedString("asdfg")))
                        }
                    }

                    context("parsing that input as map") {
                        val result = polymorphicYaml.parse(MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer())), input)

                        it("deserializes it to a map ignoring the tag") {
                            expect(result).toBe(mapOf("element" to mapOf("value" to "asdfg")))
                        }
                    }
                }

                context("given some input for an object where the property value is a literal") {
                    val input = """
                        test: !<simpleInt> 42
                    """.trimIndent()

                    context("parsing that input") {
                        val result = polymorphicYaml.parse(PolymorphicWrapper.serializer(), input)

                        it("deserializes it to a Kotlin object") {
                            expect(result).toBe(PolymorphicWrapper(UnwrappedInt(42)))
                        }
                    }
                }

                context("given some tagged input representing a list of polymorphic objects from a sealed type") {
                    val input = """
                        - element: !<sealedString>
                            value: null
                        - element: !<sealedInt>
                            value: -987
                        - element: !<sealedInt>
                            value: 654
                        - element: !<sealedString>
                            value: "tests"
                        - element: null
                    """.trimIndent()

                    context("parsing that input") {
                        val result = polymorphicYaml.parse(SealedWrapper.serializer().list, input)

                        it("deserializes it to a Kotlin object") {
                            expect(result).toBe(
                                listOf(
                                    SealedWrapper(TestSealedStructure.SimpleSealedString(null)),
                                    SealedWrapper(TestSealedStructure.SimpleSealedInt(-987)),
                                    SealedWrapper(TestSealedStructure.SimpleSealedInt(654)),
                                    SealedWrapper(TestSealedStructure.SimpleSealedString("tests")),
                                    SealedWrapper(null)
                                )
                            )
                        }
                    }
                }

                context("given some tagged input representing a list of polymorphic objects from an unsealed type") {
                    val input = """
                        - test: !<simpleNull> null
                        - test: !<simpleUnit> {}
                        - test: !<simpleBoolean> 'false'
                        - test: !<simpleByte> 42
                        - test: !<simpleShort> 43
                        - test: !<simpleInt> 44
                        - test: !<simpleLong> 45
                        - test: !<simpleFloat> 4.2
                        - test: !<simpleDouble> 4.2
                        - test: !<simpleChar> 4
                        - test: !<simpleString> 42
                        - test: !<simpleEnum> TEST2
                        - test: !<simpleNullableInt> 4
                        - test: !<simpleNullableInt> null
                        - test: !<simpleClass>
                            value: thing
                            otherValue: otherThing
                    """.trimIndent()

                    context("parsing that input") {
                        val result = polymorphicYaml.parse(PolymorphicWrapper.serializer().list, input)

                        it("deserializes it to a Kotlin object") {
                            expect(result).toBe(
                                listOf(
                                    PolymorphicWrapper(UnwrappedNull),
                                    PolymorphicWrapper(UnwrappedUnit(Unit)),
                                    PolymorphicWrapper(UnwrappedBoolean(false)),
                                    PolymorphicWrapper(UnwrappedByte(42)),
                                    PolymorphicWrapper(UnwrappedShort(43)),
                                    PolymorphicWrapper(UnwrappedInt(44)),
                                    PolymorphicWrapper(UnwrappedLong(45L)),
                                    PolymorphicWrapper(UnwrappedFloat(4.2f)),
                                    PolymorphicWrapper(UnwrappedDouble(4.2)),
                                    PolymorphicWrapper(UnwrappedChar('4')),
                                    PolymorphicWrapper(UnwrappedString("42")),
                                    PolymorphicWrapper(UnwrappedEnum.TEST2),
                                    PolymorphicWrapper(UnwrappedNullableInt(4)),
                                    PolymorphicWrapper(UnwrappedNullableInt(null)),
                                    PolymorphicWrapper(UnwrappedClass("thing", "otherThing"))
                                )
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
                        it("throws an exception with the correct location information") {
                            expect({ polymorphicYaml.parse(SealedWrapper.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                                message { toBe("Value for 'element' is invalid: Value is missing a type tag (eg. !<type>)") }
                                line { toBe(2) }
                                column { toBe(5) }
                                cause<MissingTypeTagException>()
                            }
                        }
                    }
                }

                context("given some untagged input for a polymorphic value") {
                    val input = """
                        test: "asdfg"
                    """.trimIndent()

                    context("parsing that input") {
                        it("throws an exception with the correct location information") {
                            expect({ polymorphicYaml.parse(PolymorphicWrapper.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                                message { toBe("Value for 'test' is invalid: Value is missing a type tag (eg. !<type>)") }
                                line { toBe(1) }
                                column { toBe(7) }
                                cause<MissingTypeTagException>()
                            }
                        }
                    }
                }

                context("given some untagged null input for a polymorphic value") {
                    val input = """
                        test: null
                    """.trimIndent()

                    context("parsing that input") {
                        it("throws an exception with the correct location information") {
                            expect({ polymorphicYaml.parse(PolymorphicWrapper.serializer(), input) }).toThrow<InvalidPropertyValueException> {
                                message { toBe("Value for 'test' is invalid: Value is missing a type tag (eg. !<type>)") }
                                line { toBe(1) }
                                column { toBe(7) }
                                cause<MissingTypeTagException>()
                            }
                        }
                    }
                }

                context("given a polymorphic value for a property from an unsealed type with an unknown type tag") {
                    val input = """
                        test: !<someOtherType> 42
                    """.trimIndent()

                    context("parsing that input") {
                        it("throws an exception with the correct location information") {
                            expect({ polymorphicYaml.parse(PolymorphicWrapper.serializer(), input) }).toThrow<UnknownPolymorphicTypeException> {
                                message { toBe("Unknown type 'someOtherType'. Known types are: simpleBoolean, simpleByte, simpleChar, simpleClass, simpleDouble, simpleEnum, simpleFloat, simpleInt, simpleLong, simpleNull, simpleNullableInt, simpleShort, simpleString, simpleUnit") }
                                line { toBe(1) }
                                column { toBe(7) }
                                typeName { toBe("someOtherType") }
                                validTypeNames { toBe(setOf("simpleBoolean", "simpleByte", "simpleChar", "simpleClass", "simpleDouble", "simpleEnum", "simpleFloat", "simpleInt", "simpleLong", "simpleNull", "simpleNullableInt", "simpleShort", "simpleString", "simpleUnit")) }
                            }
                        }
                    }
                }

                context("given a polymorphic value for a property from a sealed type with an unknown type tag") {
                    val input = """
                        element: !<someOtherType> 42
                    """.trimIndent()

                    context("parsing that input") {
                        it("throws an exception with the correct location information") {
                            expect({ polymorphicYaml.parse(SealedWrapper.serializer(), input) }).toThrow<UnknownPolymorphicTypeException> {
                                message { toBe("Unknown type 'someOtherType'. Known types are: sealedInt, sealedString") }
                                line { toBe(1) }
                                column { toBe(10) }
                                typeName { toBe("someOtherType") }
                                validTypeNames { toBe(setOf("sealedInt", "sealedString")) }
                            }
                        }
                    }
                }

                context("given a polymorphic value from a literal with an unknown type tag") {
                    val input = """
                        !<someOtherType> 42
                    """.trimIndent()

                    context("parsing that input") {
                        it("throws an exception with the correct location information") {
                            expect({ polymorphicYaml.parse(TestSealedStructure.serializer(), input) }).toThrow<UnknownPolymorphicTypeException> {
                                message { toBe("Unknown type 'someOtherType'. Known types are: sealedInt, sealedString") }
                                line { toBe(1) }
                                column { toBe(1) }
                                typeName { toBe("someOtherType") }
                                validTypeNames { toBe(setOf("sealedInt", "sealedString")) }
                            }
                        }
                    }
                }
            }
        }

        describe("parsing values with a dynamically installed serializer") {
            describe("parsing a literal with a contextual serializer") {
                data class Inner(val name: String)

                @Serializable
                data class Container(@ContextualSerialization val inner: Inner)

                val contextSerializer = object : KSerializer<Inner> {
                    override val descriptor: SerialDescriptor
                        get() = String.serializer().descriptor

                    override fun deserialize(decoder: Decoder): Inner = Inner("from context serializer")
                    override fun serialize(encoder: Encoder, value: Inner) = throw UnsupportedOperationException()
                }

                val module = serializersModuleOf(Inner::class, contextSerializer)
                val parser = Yaml(context = module)

                val input = """
                    inner: this is the input
                """.trimIndent()

                val result = parser.parse(Container.serializer(), input)

                it("deserializes it using the dynamically installed serializer") {
                    expect(result).toBe(Container(Inner("from context serializer")))
                }
            }

            listOf(StructureKind.CLASS, StructureKind.OBJECT).forEach { kind ->
                describe("parsing a ${kind.toString().toLowerCase()} with a contextual serializer") {
                    data class Inner(val name: String)

                    @Serializable
                    data class Container(@ContextualSerialization val inner: Inner)

                    val contextSerializer = object : KSerializer<Inner> {
                        override val descriptor = SerialDescriptor("Inner", kind) {
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
                    val parser = Yaml(context = module)

                    val input = """
                        inner:
                            thing: this is the input
                    """.trimIndent()

                    val result = parser.parse(Container.serializer(), input)

                    it("deserializes it using the dynamically installed serializer") {
                        expect(result).toBe(Container(Inner("this is the input, from context serializer")))
                    }
                }
            }

            describe("parsing a map with a contextual serializer") {
                data class Inner(val name: String)

                @Serializable
                data class Container(@ContextualSerialization val inner: Inner)

                val contextSerializer = object : KSerializer<Inner> {
                    override val descriptor = SerialDescriptor("Inner", StructureKind.MAP) {
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
                val parser = Yaml(context = module)

                val input = """
                    inner:
                        thing: this is the input
                """.trimIndent()

                val result = parser.parse(Container.serializer(), input)

                it("deserializes it using the dynamically installed serializer") {
                    expect(result).toBe(Container(Inner("thing: this is the input, from context serializer")))
                }
            }
        }

        describe("parsing values with mismatched types") {
            context("given a list") {
                listOf(
                    "a string" to String.serializer(),
                    "an integer" to Int.serializer(),
                    "a long" to Long.serializer(),
                    "a short" to Short.serializer(),
                    "a byte" to Byte.serializer(),
                    "a double" to Double.serializer(),
                    "a float" to Float.serializer(),
                    "a boolean" to Boolean.serializer(),
                    "a character" to Char.serializer(),
                    "an enumeration value" to TestEnum.serializer(),
                    "a map" to MapSerializer(String.serializer(), String.serializer()),
                    "an object" to ComplexStructure.serializer(),
                    "a string" to String.serializer().nullable
                ).forEach { (description, serializer) ->
                    val input = "- thing"

                    context("parsing that input as $description") {
                        it("throws an exception with the correct location information") {
                            expect({ Yaml.default.parse(serializer, input) }).toThrow<IncorrectTypeException> {
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
                        expect({ Yaml.default.parse(MapSerializer(String.serializer(), String.serializer()), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect({ Yaml.default.parse(String.serializer().list, input) }).toThrow<IncorrectTypeException> {
                            message { toBe("Expected a string, but got a list") }
                            line { toBe(1) }
                            column { toBe(3) }
                        }
                    }
                }
            }

            context("given a map") {
                listOf(
                    "a string" to String.serializer(),
                    "an integer" to Int.serializer(),
                    "a long" to Long.serializer(),
                    "a short" to Short.serializer(),
                    "a byte" to Byte.serializer(),
                    "a double" to Double.serializer(),
                    "a float" to Float.serializer(),
                    "a boolean" to Boolean.serializer(),
                    "a character" to Char.serializer(),
                    "an enumeration value" to TestEnum.serializer(),
                    "a list" to String.serializer().list,
                    "a string" to String.serializer().nullable
                ).forEach { (description, serializer) ->
                    val input = "key: value"

                    context("parsing that input as $description") {
                        it("throws an exception with the correct location information") {
                            expect({ Yaml.default.parse(serializer, input) }).toThrow<IncorrectTypeException> {
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
                        expect({ Yaml.default.parse(MapSerializer(String.serializer(), String.serializer()), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect({ Yaml.default.parse(ComplexStructure.serializer(), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect({ Yaml.default.parse(String.serializer().list, input) }).toThrow<IncorrectTypeException> {
                            message { toBe("Expected a string, but got a map") }
                            line { toBe(1) }
                            column { toBe(3) }
                        }
                    }
                }
            }

            context("given a scalar value") {
                mapOf(
                    "a list" to String.serializer().list,
                    "a map" to MapSerializer(String.serializer(), String.serializer()),
                    "an object" to ComplexStructure.serializer()
                ).forEach { description, serializer ->
                    val input = "blah"

                    context("parsing that input as $description") {
                        it("throws an exception with the correct location information") {
                            expect({ Yaml.default.parse(serializer, input) }).toThrow<IncorrectTypeException> {
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
                        expect({ Yaml.default.parse(MapSerializer(String.serializer(), String.serializer().list), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect({ Yaml.default.parse(Team.serializer(), input) }).toThrow<InvalidPropertyValueException> {
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
                        expect({ Yaml.default.parse((String.serializer().list).list, input) }).toThrow<IncorrectTypeException> {
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

private object LocationThrowingSerializer : KSerializer<Any> {
    override val descriptor = SerialDescriptor(LocationThrowingSerializer::class.qualifiedName!!, UnionKind.CONTEXTUAL)

    override fun deserialize(decoder: Decoder): Any {
        val location = (decoder as YamlInput).getCurrentLocation()

        throw LocationInformationException("Serializer called with location: ${location.line}, ${location.column}")
    }

    override fun serialize(encoder: Encoder, value: Any) = throw UnsupportedOperationException()
}

private object LocationThrowingMapSerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = MapSerializer(String.serializer(), String.serializer()).descriptor

    override fun deserialize(decoder: Decoder): Any {
        val location = (decoder as YamlInput).getCurrentLocation()

        throw LocationInformationException("Serializer called with location: ${location.line}, ${location.column}")
    }

    override fun serialize(encoder: Encoder, value: Any) = throw UnsupportedOperationException()
}

private class LocationInformationException(message: String) : RuntimeException(message)
