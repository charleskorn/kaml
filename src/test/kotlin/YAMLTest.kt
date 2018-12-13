import ch.tutteli.atrium.api.cc.en_GB.message
import ch.tutteli.atrium.api.cc.en_GB.notToBeNullBut
import ch.tutteli.atrium.api.cc.en_GB.property
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.verbs.assert
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.BooleanSerializer
import kotlinx.serialization.internal.ByteSerializer
import kotlinx.serialization.internal.CharSerializer
import kotlinx.serialization.internal.DoubleSerializer
import kotlinx.serialization.internal.EnumSerializer
import kotlinx.serialization.internal.FloatSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.LongSerializer
import kotlinx.serialization.internal.ShortSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.internal.makeNullable
import kotlinx.serialization.list
import kotlinx.serialization.map
import kotlinx.serialization.serializer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object YAMLTest : Spek({
    describe("a YAML parser") {
        describe("parsing scalars") {
            given("the input 'hello'") {
                val input = "hello"

                on("parsing that input as a string") {
                    val result = YAML.parse(StringSerializer, input)

                    it("deserializes it to the expected string value") {
                        assert(result).toBe("hello")
                    }
                }

                on("parsing that input as a nullable string") {
                    val result = YAML.parse(makeNullable(StringSerializer), input)

                    it("deserializes it to the expected string value") {
                        assert(result).notToBeNullBut("hello")
                    }
                }
            }

            given("the input '123'") {
                val input = "123"

                on("parsing that input as an integer") {
                    val result = YAML.parse(Int.serializer(), input)

                    it("deserializes it to the expected integer") {
                        assert(result).toBe(123)
                    }
                }

                on("parsing that input as a long") {
                    val result = YAML.parse(Long.serializer(), input)

                    it("deserializes it to the expected long") {
                        assert(result).toBe(123)
                    }
                }

                on("parsing that input as a short") {
                    val result = YAML.parse(Short.serializer(), input)

                    it("deserializes it to the expected short") {
                        assert(result).toBe(123)
                    }
                }

                on("parsing that input as a byte") {
                    val result = YAML.parse(Byte.serializer(), input)

                    it("deserializes it to the expected byte") {
                        assert(result).toBe(123)
                    }
                }

                on("parsing that input as a double") {
                    val result = YAML.parse(Double.serializer(), input)

                    it("deserializes it to the expected double") {
                        assert(result).toBe(123.0)
                    }
                }

                on("parsing that input as a float") {
                    val result = YAML.parse(FloatSerializer, input)

                    it("deserializes it to the expected float") {
                        assert(result).toBe(123.0f)
                    }
                }

                on("parsing that input as a nullable integer") {
                    val result = YAML.parse(makeNullable(Int.serializer()), input)

                    it("deserializes it to the expected integer") {
                        assert(result).notToBeNullBut(123)
                    }
                }

                on("parsing that input as a nullable long") {
                    val result = YAML.parse(makeNullable(Long.serializer()), input)

                    it("deserializes it to the expected long") {
                        assert(result).notToBeNullBut(123)
                    }
                }

                on("parsing that input as a nullable short") {
                    val result = YAML.parse(makeNullable(Short.serializer()), input)

                    it("deserializes it to the expected short") {
                        assert(result).notToBeNullBut(123)
                    }
                }

                on("parsing that input as a nullable byte") {
                    val result = YAML.parse(makeNullable(Byte.serializer()), input)

                    it("deserializes it to the expected byte") {
                        assert(result).notToBeNullBut(123)
                    }
                }

                on("parsing that input as a nullable double") {
                    val result = YAML.parse(makeNullable(Double.serializer()), input)

                    it("deserializes it to the expected double") {
                        assert(result).notToBeNullBut(123.0)
                    }
                }

                on("parsing that input as a nullable float") {
                    val result = YAML.parse(makeNullable(FloatSerializer), input)

                    it("deserializes it to the expected float") {
                        assert(result).notToBeNullBut(123.0f)
                    }
                }
            }

            given("the input 'true'") {
                val input = "true"

                on("parsing that input as a boolean") {
                    val result = YAML.parse(BooleanSerializer, input)

                    it("deserializes it to the expected boolean value") {
                        assert(result).toBe(true)
                    }
                }

                on("parsing that input as a nullable boolean") {
                    val result = YAML.parse(makeNullable(BooleanSerializer), input)

                    it("deserializes it to the expected boolean value") {
                        assert(result).notToBeNullBut(true)
                    }
                }
            }

            given("the input 'c'") {
                val input = "c"

                on("parsing that input as a character") {
                    val result = YAML.parse(CharSerializer, input)

                    it("deserializes it to the expected character value") {
                        assert(result).toBe('c')
                    }
                }

                on("parsing that input as a nullable character") {
                    val result = YAML.parse(makeNullable(CharSerializer), input)

                    it("deserializes it to the expected character value") {
                        assert(result).notToBeNullBut('c')
                    }
                }
            }

            mapOf(
                "Value1" to TestEnum.Value1,
                "Value2" to TestEnum.Value2
            ).forEach { input, expectedValue ->
                given("the input '$input'") {
                    on("parsing that input as an enumeration value") {
                        val result = YAML.parse(EnumSerializer(TestEnum::class), input)

                        it("deserializes it to the expected enumeration value") {
                            assert(result).toBe(expectedValue)
                        }
                    }
                }
            }

            on("parsing an invalid enumeration value") {
                it("throws an appropriate exception") {
                    assert({ YAML.parse(EnumSerializer(TestEnum::class), "nonsense") }).toThrow<YamlException> {
                        message { toBe("Value 'nonsense' is not a valid option, permitted choices are: Value1, Value2") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }
        }

        describe("parsing null values") {
            val input = "null"

            on("parsing a null value as a string") {
                val result = YAML.parse(makeNullable(StringSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a integer") {
                val result = YAML.parse(makeNullable(IntSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a long") {
                val result = YAML.parse(makeNullable(LongSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a short") {
                val result = YAML.parse(makeNullable(ShortSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a byte") {
                val result = YAML.parse(makeNullable(ByteSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a double") {
                val result = YAML.parse(makeNullable(DoubleSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a float") {
                val result = YAML.parse(makeNullable(FloatSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a boolean") {
                val result = YAML.parse(makeNullable(BooleanSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a character") {
                val result = YAML.parse(makeNullable(CharSerializer), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a enum") {
                val result = YAML.parse(makeNullable(EnumSerializer(TestEnum::class)), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a list") {
                val result = YAML.parse(makeNullable(StringSerializer.list), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }

            on("parsing a null value as a object") {
                val result = YAML.parse(makeNullable(ComplexStructure.serializer()), input)

                it("returns a null value") {
                    assert(result).toBe(null)
                }
            }
        }

        describe("parsing lists") {
            given("a list of strings") {
                val input = """
                    - thing1
                    - thing2
                    - thing3
                """.trimIndent()

                on("parsing that input as a list") {
                    val result = YAML.parse(String.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf("thing1", "thing2", "thing3"))
                    }
                }
            }

            given("a list of numbers") {
                val input = """
                    - 123
                    - 45
                    - 6
                """.trimIndent()

                on("parsing that input as a list of integers") {
                    val result = YAML.parse(Int.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123, 45, 6))
                    }
                }

                on("parsing that input as a list of longs") {
                    val result = YAML.parse(Long.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123L, 45, 6))
                    }
                }

                on("parsing that input as a list of shorts") {
                    val result = YAML.parse(Short.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123.toShort(), 45, 6))
                    }
                }

                on("parsing that input as a list of bytes") {
                    val result = YAML.parse(Byte.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123.toByte(), 45, 6))
                    }
                }

                on("parsing that input as a list of doubles") {
                    val result = YAML.parse(Double.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123.0, 45.0, 6.0))
                    }
                }

                on("parsing that input as a list of floats") {
                    val result = YAML.parse(FloatSerializer.list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(123.0f, 45.0f, 6.0f))
                    }
                }
            }

            given("a list of booleans") {
                val input = """
                    - true
                    - false
                """.trimIndent()

                on("parsing that input as a list") {
                    val result = YAML.parse(Boolean.serializer().list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(true, false))
                    }
                }
            }

            given("a list of enum values") {
                val input = """
                    - Value1
                    - Value2
                """.trimIndent()

                on("parsing that input as a list") {
                    val result = YAML.parse(EnumSerializer(TestEnum::class).list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf(TestEnum.Value1, TestEnum.Value2))
                    }
                }
            }

            given("a list of characters") {
                val input = """
                    - a
                    - b
                """.trimIndent()

                on("parsing that input as a list") {
                    val result = YAML.parse(CharSerializer.list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf('a', 'b'))
                    }
                }
            }

            given("a list of nullable strings") {
                val input = """
                    - thing1
                    - null
                """.trimIndent()

                on("parsing that input as a list") {
                    val result = YAML.parse(makeNullable(String.serializer()).list, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(listOf("thing1", null))
                    }
                }
            }

            given("a list of lists") {
                val input = """
                    - [thing1, thing2]
                    - [thing3]
                """.trimIndent()

                on("parsing that input as a list") {
                    val result = YAML.parse(String.serializer().list.list, input)

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

            given("a list of objects") {
                val input = """
                    - name: thing1
                    - name: thing2
                """.trimIndent()

                on("parsing that input as a list") {
                    val result = YAML.parse(SimpleStructure.serializer().list, input)

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
            given("some input representing an object with an optional value specified") {
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

                on("parsing that input") {
                    val result = YAML.parse(ComplexStructure.serializer(), input)

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

            given("some input representing an object with an optional value specified as null") {
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

                on("parsing that input") {
                    val result = YAML.parse(ComplexStructure.serializer(), input)

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

            given("some input representing an object with an optional value not specified") {
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

                on("parsing that input") {
                    val result = YAML.parse(ComplexStructure.serializer(), input)

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

            given("some input representing an object with an embedded list") {
                val input = """
                        members:
                            - Alex
                            - Jamie
                    """.trimIndent()

                on("parsing that input") {
                    val result = YAML.parse(Team.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(Team(listOf("Alex", "Jamie")))
                    }
                }
            }

            given("some input representing an object with an embedded object") {
                val input = """
                        firstPerson:
                            name: Alex
                        secondPerson:
                            name: Jamie
                    """.trimIndent()

                on("parsing that input") {
                    val result = YAML.parse(NestedObjects.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(NestedObjects(SimpleStructure("Alex"), SimpleStructure("Jamie")))
                    }
                }
            }

            given("some input representing an object where the keys are in a different order to the object definition") {
                val input = """
                        secondPerson:
                            name: Jamie
                        firstPerson:
                            name: Alex
                    """.trimIndent()

                on("parsing that input") {
                    val result = YAML.parse(NestedObjects.serializer(), input)

                    it("deserializes it to a Kotlin object") {
                        assert(result).toBe(NestedObjects(SimpleStructure("Alex"), SimpleStructure("Jamie")))
                    }
                }
            }

            given("some input representing an object with an unknown key") {
                val input = """
                        abc123: something
                    """.trimIndent()

                on("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(ComplexStructure.serializer(), input) }).toThrow<YamlException> {
                            message { toBe("Unknown property 'abc123'. Known properties are: boolean, byte, char, double, enum, float, int, long, nullable, short, string") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }
            }

            given("some input representing an object with a list as a key") {
                val input = """
                        []: something
                    """.trimIndent()

                on("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(ComplexStructure.serializer(), input) }).toThrow<YamlException> {
                            message { toBe("Property name must not be a list, map or null value. (To use 'null' as a property name, enclose it in quotes.)") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }
            }

            given("some input representing a generic map") {
                val input = """
                    SOME_ENV_VAR: somevalue
                    SOME_OTHER_ENV_VAR: someothervalue
                """.trimIndent()

                on("parsing that input") {
                    val result = YAML.parse((StringSerializer to StringSerializer).map, input)

                    it("deserializes it to a Kotlin map") {
                        assert(result).toBe(mapOf(
                            "SOME_ENV_VAR" to "somevalue",
                            "SOME_OTHER_ENV_VAR" to "someothervalue"
                        ))
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

enum class TestEnum {
    Value1,
    Value2
}

fun Assert<YamlException>.line(assertionCreator: Assert<Int>.() -> Unit) {
    property(subject::line).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<YamlException>.column(assertionCreator: Assert<Int>.() -> Unit) {
    property(subject::column).addAssertionsCreatedBy(assertionCreator)
}
