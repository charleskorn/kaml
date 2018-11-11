import ch.tutteli.atrium.api.cc.en_GB.message
import ch.tutteli.atrium.api.cc.en_GB.property
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.verbs.assert
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.CharSerializer
import kotlinx.serialization.internal.EnumSerializer
import kotlinx.serialization.internal.FloatSerializer
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

// Reference: http://yaml.org/spec/1.2/spec.html
object YAMLTest : Spek({
    describe("a YAML parser") {
        mapOf(
            "hello" to "hello",
            """"hello"""" to "hello",
            "'hello'" to "hello",
            "'he''llo'" to "he'llo",
            """"hello\""""" to """hello"""",
            """"\"hello"""" to """"hello""",
            """"he\"llo"""" to """he"llo""",
            """"he\"\"llo"""" to """he""llo""",
            "''" to "",
            """""""" to ""
        ).forEach { input, expectedResult ->
            given("the string '$input'") {
                on("parsing that input") {
                    val result = YAML.parse(String.serializer(), input)

                    it("deserializes it to the expected string") {
                        assert(result).toBe(expectedResult)
                    }
                }
            }
        }

        mapOf(
            "a double-quoted string without a trailing double quote" to """"hello""",
            "a single-quoted string without a trailing single quote" to "'hello"
        ).forEach { description, input ->
            given(description) {
                on("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(String.serializer(), input) }).toThrow<YAMLException> {
                            message { toBe("Unexpected end of input") }
                            line { toBe(1) }
                            column { toBe(7) }
                        }
                    }
                }
            }
        }


        mapOf(
            "0" to 0,
            "1" to 1,
            "-1" to -1,
            "0x11" to 17,
            "-0x11" to -17,
            "0o11" to 9,
            "-0o11" to -9
        ).forEach { input, expectedValue ->
            given("the string '$input'") {
                on("parsing that input as an integer") {
                    val result = YAML.parse(Int.serializer(), input)

                    it("deserializes it to the expected integer") {
                        assert(result).toBe(expectedValue)
                    }
                }

                on("parsing that input as a long") {
                    val result = YAML.parse(Long.serializer(), input)

                    it("deserializes it to the expected long") {
                        assert(result).toBe(expectedValue.toLong())
                    }
                }

                on("parsing that input as a short") {
                    val result = YAML.parse(Short.serializer(), input)

                    it("deserializes it to the expected short") {
                        assert(result).toBe(expectedValue.toShort())
                    }
                }

                on("parsing that input as a byte") {
                    val result = YAML.parse(Byte.serializer(), input)

                    it("deserializes it to the expected byte") {
                        assert(result).toBe(expectedValue.toByte())
                    }
                }
            }
        }

        listOf(
            "a",
            ".",
            "1.",
            ".1",
            "1.5",
            "+",
            "0x",
            "0o"
        ).forEach { input ->
            given("the string '$input'") {
                on("parsing that input as an integer") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(Int.serializer(), input) }).toThrow<YAMLException> {
                            message { toBe("Value '$input' is not a valid integer value.") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }

                on("parsing that input as a long") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(Long.serializer(), input) }).toThrow<YAMLException> {
                            message { toBe("Value '$input' is not a valid long value.") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }

                on("parsing that input as a short") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(Short.serializer(), input) }).toThrow<YAMLException> {
                            message { toBe("Value '$input' is not a valid short value.") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }

                on("parsing that input as a byte") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(Byte.serializer(), input) }).toThrow<YAMLException> {
                            message { toBe("Value '$input' is not a valid byte value.") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }
            }
        }

        mapOf(
            "1" to 1.0,
            ".5" to 0.5,
            "1.5" to 1.5,
            "1.5e2" to 150.0,
            "1.5E2" to 150.0,
            "1.5e+2" to 150.0,
            "1.5e-2" to 0.015,
            "-1.5e2" to -150.0,
            "-1.5e+2" to -150.0,
            "-1.5e-2" to -0.015,
            ".nan" to Double.NaN,
            ".NaN" to Double.NaN,
            ".NAN" to Double.NaN,
            ".inf" to Double.POSITIVE_INFINITY,
            ".Inf" to Double.POSITIVE_INFINITY,
            ".INF" to Double.POSITIVE_INFINITY,
            "-.inf" to Double.NEGATIVE_INFINITY,
            "-.Inf" to Double.NEGATIVE_INFINITY,
            "-.INF" to Double.NEGATIVE_INFINITY
        ).forEach { input, expectedResult ->
            given("the string '$input'") {
                on("parsing that input as a double") {
                    val result = YAML.parse(Double.serializer(), input)

                    it("deserializes it to the expected double") {
                        assert(result).toBe(expectedResult)
                    }
                }
            }
        }

        mapOf(
            "1" to 1.0f,
            ".5" to 0.5f,
            "1.5" to 1.5f,
            "1.5e2" to 150f,
            "1.5E2" to 150f,
            "1.5e+2" to 150f,
            "1.5e-2" to 0.015f,
            "-1.5e2" to -150f,
            "-1.5e+2" to -150f,
            "-1.5e-2" to -0.015f,
            ".nan" to Float.NaN,
            ".NaN" to Float.NaN,
            ".NAN" to Float.NaN,
            ".inf" to Float.POSITIVE_INFINITY,
            ".Inf" to Float.POSITIVE_INFINITY,
            ".INF" to Float.POSITIVE_INFINITY,
            "-.inf" to Float.NEGATIVE_INFINITY,
            "-.Inf" to Float.NEGATIVE_INFINITY,
            "-.INF" to Float.NEGATIVE_INFINITY
        ).forEach { input, expectedResult ->
            given("the string '$input'") {
                on("parsing that input as a float") {
                    // FIXME: for some reason, Float.serializer() isn't defined, neither is Char.serializer()
                    // https://github.com/Kotlin/kotlinx.serialization/issues/263
                    val result = YAML.parse(FloatSerializer, input)

                    it("deserializes it to the expected float") {
                        assert(result).toBe(expectedResult)
                    }
                }
            }
        }

        listOf(
            ".",
            "0x2",
            "0o2",
            "1e",
            "1e-",
            "1e+",
            "+"
        ).forEach { input ->
            given("the input '$input'") {
                on("parsing that input as a float") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(FloatSerializer, input) }).toThrow<YAMLException> {
                            message { toBe("Value '$input' is not a valid floating point value.") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }

                on("parsing that input as a double") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(Double.serializer(), input) }).toThrow<YAMLException> {
                            message { toBe("Value '$input' is not a valid floating point value.") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
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
                assert({ YAML.parse(EnumSerializer(TestEnum::class), "nonsense") }).toThrow<YAMLException> {
                    message { toBe("Value 'nonsense' is not a valid option, permitted choices are: Value1, Value2") }
                    line { toBe(1) }
                    column { toBe(1) }
                }
            }
        }

        mapOf(
            "true" to true,
            "True" to true,
            "TRUE" to true,
            "false" to false,
            "False" to false,
            "FALSE" to false
        ).forEach { input, expectedValue ->
            given("the input '$input'") {
                on("parsing that input as a boolean value") {
                    val result = YAML.parse(Boolean.serializer(), input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(expectedValue)
                    }
                }
            }
        }

        on("parsing an invalid boolean value") {
            it("throws an appropriate exception") {
                assert({ YAML.parse(Boolean.serializer(), "nonsense") }).toThrow<YAMLException> {
                    message { toBe("Value 'nonsense' is not a valid boolean, permitted choices are: true or false") }
                    line { toBe(1) }
                    column { toBe(1) }
                }
            }
        }

        mapOf(
            "a" to 'a',
            "'a'" to 'a',
            """"a"""" to 'a'
        ).forEach { input, expectedValue ->
            given("the input '$input'") {
                on("parsing that input as a character value") {
                    // FIXME: for some reason, Float.serializer() isn't defined, neither is Char.serializer()
                    // https://github.com/Kotlin/kotlinx.serialization/issues/263
                    val result = YAML.parse(CharSerializer, input)

                    it("deserializes it to the expected value") {
                        assert(result).toBe(expectedValue)
                    }
                }
            }
        }

        mapOf(
            "aa" to "aa",
            "'aa'" to "aa",
            """"aa"""" to "aa",
            "''" to "",
            """""""" to ""
        ).forEach { input, value ->
            given("the input '$input'") {
                on("parsing that input as a character value") {
                    it("throws an appropriate exception") {
                        assert({ YAML.parse(CharSerializer, input) }).toThrow<YAMLException> {
                            message { toBe("Value '$value' is not a valid character value.") }
                            line { toBe(1) }
                            column { toBe(1) }
                        }
                    }
                }
            }
        }

        given("some input representing a list of strings") {
            val input = """
                - thing1
                - thing2
                - "thing3"
                - 'thing4'
                - "thing\"5"
            """.trimIndent()

            on("parsing that input as a list") {
                val result = YAML.parse(String.serializer().list, input)

                it("deserializes it to the expected value") {
                    assert(result).toBe(listOf("thing1", "thing2", "thing3", "thing4", "thing\"5"))
                }
            }
        }

        given("some input representing a list of strings in flow style") {
            val input = """[thing1, thing2, "thing3", 'thing4', "thing\"5"]"""

            on("parsing that input as a list") {
                val result = YAML.parse(String.serializer().list, input)

                it("deserializes it to the expected value") {
                    assert(result).toBe(listOf("thing1", "thing2", "thing3", "thing4", "thing\"5"))
                }
            }
        }

        given("some input representing a list of strings in flow style without a trailing ']'") {
            val input = """[thing1"""

            on("parsing that input as a list") {
                it("throws an appropriate exception") {
                    assert({ YAML.parse(String.serializer().list, input) }).toThrow<YAMLException> {
                        message { toBe("Unexpected end of input") }
                        line { toBe(1) }
                        column { toBe(8) }
                    }
                }
            }
        }

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
                val result = YAML.parse(TestStructure.serializer(), input)

                it("deserializes it to a Kotlin object") {
                    assert(result).toBe(TestStructure(
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
                    ))
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
                val result = YAML.parse(TestStructure.serializer(), input)

                it("deserializes it to a Kotlin object") {
                    assert(result).toBe(TestStructure(
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
                    ))
                }
            }
        }

        // Nested lists
        // Comments
        // Multiline strings
        // References etc.
        // Structures
        // - optional values
        // - nullable values
        // - lists
        // - nested structures
        // - concise format
        // Maps
    }
})

@Serializable
data class TestStructure(
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

enum class TestEnum {
    Value1,
    Value2
}

fun Assert<YAMLException>.line(assertionCreator: Assert<Int>.() -> Unit) {
    property(subject::line).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<YAMLException>.column(assertionCreator: Assert<Int>.() -> Unit) {
    property(subject::column).addAssertionsCreatedBy(assertionCreator)
}
