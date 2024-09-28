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

import com.charleskorn.kaml.testobjects.TestEnum
import com.charleskorn.kaml.testobjects.TestEnumWithExplicitNames
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

class YamlScalarReadingTest : FlatFunSpec({
    context("a YAML parser parsing scalars") {
        context("given the input 'hello'") {
            val input = "hello"

            context("parsing that input as a string") {
                val result = Yaml.default.decodeFromString(String.serializer(), input)

                test("deserializes it to the expected string value") {
                    result shouldBe "hello"
                }
            }

            context("parsing that input as a nullable string") {
                val result = Yaml.default.decodeFromString(String.serializer().nullable, input)

                test("deserializes it to the expected string value") {
                    result shouldBe "hello"
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

            context("parsing that input as a value type") {
                val result = Yaml.default.decodeFromString(StringValue.serializer(), input)

                test("deserializes it to the expected object") {
                    result shouldBe StringValue("hello")
                }
            }
        }

        context("given the input '123'") {
            val input = "123"

            context("parsing that input as an integer") {
                val result = Yaml.default.decodeFromString(Int.serializer(), input)

                test("deserializes it to the expected integer") {
                    result shouldBe 123
                }
            }

            context("parsing that input as a long") {
                val result = Yaml.default.decodeFromString(Long.serializer(), input)

                test("deserializes it to the expected long") {
                    result shouldBe 123
                }
            }

            context("parsing that input as a short") {
                val result = Yaml.default.decodeFromString(Short.serializer(), input)

                test("deserializes it to the expected short") {
                    result shouldBe 123
                }
            }

            context("parsing that input as a byte") {
                val result = Yaml.default.decodeFromString(Byte.serializer(), input)

                test("deserializes it to the expected byte") {
                    result shouldBe 123
                }
            }

            context("parsing that input as a double") {
                val result = Yaml.default.decodeFromString(Double.serializer(), input)

                test("deserializes it to the expected double") {
                    result shouldBe 123.0
                }
            }

            context("parsing that input as a float") {
                val result = Yaml.default.decodeFromString(Float.serializer(), input)

                test("deserializes it to the expected float") {
                    result shouldBe 123.0f
                }
            }

            context("parsing that input as a nullable integer") {
                val result = Yaml.default.decodeFromString(Int.serializer().nullable, input)

                test("deserializes it to the expected integer") {
                    result shouldBe 123
                }
            }

            context("parsing that input as a nullable long") {
                val result = Yaml.default.decodeFromString(Long.serializer().nullable, input)

                test("deserializes it to the expected long") {
                    result shouldBe 123
                }
            }

            context("parsing that input as a nullable short") {
                val result = Yaml.default.decodeFromString(Short.serializer().nullable, input)

                test("deserializes it to the expected short") {
                    result shouldBe 123
                }
            }

            context("parsing that input as a nullable byte") {
                val result = Yaml.default.decodeFromString(Byte.serializer().nullable, input)

                test("deserializes it to the expected byte") {
                    result shouldBe 123
                }
            }

            context("parsing that input as a nullable double") {
                val result = Yaml.default.decodeFromString(Double.serializer().nullable, input)

                test("deserializes it to the expected double") {
                    result shouldBe 123.0
                }
            }

            context("parsing that input as a nullable float") {
                val result = Yaml.default.decodeFromString(Float.serializer().nullable, input)

                test("deserializes it to the expected float") {
                    result shouldBe 123.0f
                }
            }
        }

        context("given the input 'true'") {
            val input = "true"

            context("parsing that input as a boolean") {
                val result = Yaml.default.decodeFromString(Boolean.serializer(), input)

                test("deserializes it to the expected boolean value") {
                    result shouldBe true
                }
            }

            context("parsing that input as a nullable boolean") {
                val result = Yaml.default.decodeFromString(Boolean.serializer().nullable, input)

                test("deserializes it to the expected boolean value") {
                    result shouldBe true
                }
            }
        }

        context("given the input 'c'") {
            val input = "c"

            context("parsing that input as a character") {
                val result = Yaml.default.decodeFromString(Char.serializer(), input)

                test("deserializes it to the expected character value") {
                    result shouldBe 'c'
                }
            }

            context("parsing that input as a nullable character") {
                val result = Yaml.default.decodeFromString(Char.serializer().nullable, input)

                test("deserializes it to the expected character value") {
                    result shouldBe 'c'
                }
            }
        }

        data class EnumFixture(val input: String, val serializer: KSerializer<*>)

        mapOf(
            EnumFixture("Value1", TestEnum.serializer()) to TestEnum.Value1,
            EnumFixture("Value2", TestEnum.serializer()) to TestEnum.Value2,
            EnumFixture("A", TestEnumWithExplicitNames.serializer()) to TestEnumWithExplicitNames.Alpha,
            EnumFixture("B", TestEnumWithExplicitNames.serializer()) to TestEnumWithExplicitNames.Beta,
            EnumFixture("With space", TestEnumWithExplicitNames.serializer()) to TestEnumWithExplicitNames.WithSpace,
        ).forEach { (fixture, expectedValue) ->
            val (input, serializer) = fixture
            context("given the input '$input'") {
                context("parsing that input as an enumeration value") {
                    val result = Yaml.default.decodeFromString(serializer, input)

                    test("deserializes it to the expected enumeration value") {
                        result shouldBe expectedValue
                    }
                }
            }
        }

        context("parsing an invalid enumeration value") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<YamlScalarFormatException> { Yaml.default.decodeFromString(TestEnum.serializer(), "nonsense") }

                exception.asClue {
                    it.message shouldBe "Value 'nonsense' is not a valid option, permitted choices are: Value1, Value2"
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing case insensitive enumeration value") {
            val yaml = Yaml(configuration = YamlConfiguration(decodeEnumCaseInsensitive = true))

            test("deserializes it to the expected enumeration value") {
                val result = yaml.decodeFromString(TestEnum.serializer(), "value1")

                result shouldBe TestEnum.Value1
            }

            test("deserializes explicit names to the expected enumeration value") {
                val result = yaml.decodeFromString(TestEnumWithExplicitNames.serializer(), "with SPACE")

                result shouldBe TestEnumWithExplicitNames.WithSpace
            }

            test("throws exception with case sensitive configuration") {
                val exception = shouldThrow<YamlScalarFormatException> { Yaml.default.decodeFromString(TestEnum.serializer(), "value1") }

                exception.asClue {
                    it.message shouldBe "Value 'value1' is not a valid option, permitted choices are: Value1, Value2"
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }
    }
})
