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

import com.charleskorn.kaml.testobjects.TestClassWithNestedNode
import com.charleskorn.kaml.testobjects.TestClassWithNestedNull
import com.charleskorn.kaml.testobjects.TestEnum
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

class YamlNullReadingTest : FlatFunSpec({
    context("a YAML parser parsing null values") {
        val input = "null"

        context("parsing a null value as a nullable string") {
            val result = Yaml.default.decodeFromString(String.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable string") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(String.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable integer") {
            val result = Yaml.default.decodeFromString(Int.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable integer") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(Int.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable long") {
            val result = Yaml.default.decodeFromString(Long.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable long") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(Long.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable short") {
            val result = Yaml.default.decodeFromString(Short.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable short") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(Short.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable byte") {
            val result = Yaml.default.decodeFromString(Byte.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable byte") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(Byte.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable double") {
            val result = Yaml.default.decodeFromString(Double.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable double") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(Double.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable float") {
            val result = Yaml.default.decodeFromString(Float.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable float") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(Float.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable boolean") {
            val result = Yaml.default.decodeFromString(Boolean.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable boolean") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(Boolean.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable character") {
            val result = Yaml.default.decodeFromString(Char.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable character") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(Char.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable enum") {
            val result = Yaml.default.decodeFromString(TestEnum.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable enum") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(TestEnum.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value as a nullable list") {
            val result = Yaml.default.decodeFromString(ListSerializer(String.serializer()).nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable list") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(ListSerializer(String.serializer()), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        @Serializable
        class ComplexStructureForNull

        context("parsing a null value as a nullable object") {
            val result = Yaml.default.decodeFromString(ComplexStructureForNull.serializer().nullable, input)

            test("returns a null value") {
                result shouldBe null
            }
        }

        context("parsing a null value as a non-nullable object") {
            test("throws an appropriate exception") {
                val exception = shouldThrow<UnexpectedNullValueException> { Yaml.default.decodeFromString(ComplexStructureForNull.serializer(), input) }

                exception.asClue {
                    it.message shouldBe "Unexpected null or empty value for non-null field."
                    it.line shouldBe 1
                    it.column shouldBe 1
                    it.path shouldBe YamlPath.root
                }
            }
        }

        context("parsing a null value with a serializer that uses YAML location information when throwing exceptions") {
            test("throws an exception with the correct location information") {
                val exception = shouldThrow<LocationInformationException> { Yaml.default.decodeFromString(LocationThrowingSerializer, input) }

                exception.asClue {
                    it.message shouldBe "Serializer called with location (1, 1) and path: <root>"
                }
            }
        }
    }

    context("a YAML parser parsing nested null values") {

        context("given a nested null node") {
            val input = """
                text: "OK"
                node: null
            """.trimIndent()

            context("parsing that input as a null node") {
                val result = Yaml.default.decodeFromString(TestClassWithNestedNull.serializer(), input)

                test("deserializes scalar to double") {
                    result.node.shouldBeInstanceOf<YamlNull>()
                }
            }

            context("parsing that input as a node") {
                val result = Yaml.default.decodeFromString(TestClassWithNestedNode.serializer(), input)

                test("deserializes node to null") {
                    result.node.shouldBeInstanceOf<YamlNull>()
                }
            }
        }
    }
})
