import ch.tutteli.atrium.api.cc.en_GB.message
import ch.tutteli.atrium.api.cc.en_GB.property
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.verbs.assert
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.EnumSerializer
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.dsl.xdescribe

// Reference: http://yaml.org/spec/1.2/spec.html
object YAMLTest : Spek({
    xdescribe("a YAML parser") {




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
                    assert(result).toBe(
                        TestStructure(
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
                    assert(result).toBe(
                        TestStructure(
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

fun Assert<YamlException>.line(assertionCreator: Assert<Int>.() -> Unit) {
    property(subject::line).addAssertionsCreatedBy(assertionCreator)
}

fun Assert<YamlException>.column(assertionCreator: Assert<Int>.() -> Unit) {
    property(subject::column).addAssertionsCreatedBy(assertionCreator)
}
