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

import com.charleskorn.kaml.testobjects.TestSealedStructure
import com.charleskorn.kaml.testobjects.polymorphicModule
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable

class YamlContentPolymorphicSerializerTest : FunSpec({
    context("a YAML parser") {
        context("parsing polymorphic values with PolymorphismStyle.None") {
            val polymorphicYaml = Yaml(
                serializersModule = polymorphicModule,
                configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.None),
            )

            context("given some input where the value should be a sealed class") {
                val input = """
                    value: "asdfg"
                """.trimIndent()

                val result = polymorphicYaml.decodeFromString(TestSealedStructureBasedOnContentSerializer, input)

                test("deserializes it to a Kotlin object") {
                    result shouldBe TestSealedStructure.SimpleSealedString("asdfg")
                }
            }

            context("given some input where the value should be a sealed class (inline)") {
                val input = """
                    "abcdef"
                """.trimIndent()

                val result = polymorphicYaml.decodeFromString(TestSealedStructureBasedOnContentSerializer, input)

                test("deserializes it to a Kotlin object") {
                    result shouldBe TestSealedStructure.InlineSealedString("abcdef")
                }
            }

            context("given some input missing without the serializer") {
                val input = """
                    value: "asdfg"
                """.trimIndent()

                test("throws an exception with the correct location information") {
                    val exception = shouldThrow<IncorrectTypeException> {
                        polymorphicYaml.decodeFromString(TestSealedStructure.serializer(), input)
                    }

                    exception.asClue {
                        it.message shouldBe "Encountered a polymorphic map descriptor but PolymorphismStyle is 'None'"
                        it.line shouldBe 1
                        it.column shouldBe 1
                        it.path shouldBe YamlPath.root
                    }
                }
            }

            context("given some input representing a list of polymorphic objects") {
                val input = """
                    - value: null
                    - value: -987
                    - value: 654
                    - "testing"
                    - value: "tests"
                """.trimIndent()

                val result = polymorphicYaml.decodeFromString(
                    ListSerializer(TestSealedStructureBasedOnContentSerializer),
                    input,
                )

                test("deserializes it to a Kotlin object") {
                    result shouldBe listOf(
                        TestSealedStructure.SimpleSealedString(null),
                        TestSealedStructure.SimpleSealedInt(-987),
                        TestSealedStructure.SimpleSealedInt(654),
                        TestSealedStructure.InlineSealedString("testing"),
                        TestSealedStructure.SimpleSealedString("tests"),
                    )
                }
            }

            context("given some input with a tag and a type property") {
                val input = """
                    !<sealedInt>
                    kind: sealedString
                    value: "asdfg"
                """.trimIndent()

                test("throws an exception with the correct location information") {
                    val exception = shouldThrow<IncorrectTypeException> {
                        polymorphicYaml.decodeFromString(TestSealedStructureBasedOnContentSerializer, input)
                    }

                    exception.asClue {
                        it.message shouldBe "Encountered a tagged polymorphic descriptor but PolymorphismStyle is 'None'"
                        it.line shouldBe 1
                        it.column shouldBe 1
                        it.path shouldBe YamlPath.root
                    }
                }
            }
        }
    }
    context("a YAML serializer") {
        context("serializing polymorphic values with custom serializer") {
            val polymorphicYaml = Yaml(
                serializersModule = polymorphicModule,
                configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Tag),
            )

            context("serializing a sealed type") {
                val input = TestSealedStructure.SimpleSealedInt(5)
                val output = polymorphicYaml.encodeToString(TestSealedStructureBasedOnContentSerializer, input)
                val expectedYaml = """
                    value: 5
                """.trimIndent()

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedYaml
                }
            }

            context("serializing a list of polymorphic values") {
                val input = listOf(
                    TestSealedStructure.SimpleSealedInt(5),
                    TestSealedStructure.SimpleSealedString("some test"),
                    TestSealedStructure.SimpleSealedInt(-20),
                    TestSealedStructure.InlineSealedString("testing"),
                    TestSealedStructure.SimpleSealedString(null),
                    null,
                )

                val output = polymorphicYaml.encodeToString(
                    ListSerializer(TestSealedStructureBasedOnContentSerializer.nullable),
                    input,
                )

                val expectedYaml = """
                    - value: 5
                    - value: "some test"
                    - value: -20
                    - "testing"
                    - value: null
                    - null
                """.trimIndent()

                test("returns the value serialized in the expected YAML form") {
                    output shouldBe expectedYaml
                }
            }
        }
    }
})

object TestSealedStructureBasedOnContentSerializer : YamlContentPolymorphicSerializer<TestSealedStructure>(
    TestSealedStructure::class,
) {
    override fun selectDeserializer(node: YamlNode): DeserializationStrategy<TestSealedStructure> = when (node) {
        is YamlScalar -> TestSealedStructure.InlineSealedString.serializer()
        is YamlMap -> when (val value: YamlNode? = node["value"]) {
            is YamlScalar -> when {
                value.content.toIntOrNull() == null -> TestSealedStructure.SimpleSealedString.serializer()
                else -> TestSealedStructure.SimpleSealedInt.serializer()
            }
            is YamlNull -> TestSealedStructure.SimpleSealedString.serializer()
            else -> throw SerializationException("Unsupported property type for TestSealedStructure.value: ${value?.let { it::class.simpleName}}")
        }
        else -> throw SerializationException("Unsupported node type for TestSealedStructure: ${node::class.simpleName}")
    }
}
