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

import com.charleskorn.kaml.testobjects.TestInline
import com.charleskorn.kaml.testobjects.TestSealedImpl
import com.charleskorn.kaml.testobjects.TestSealedInterface
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

class TestInlineTest : FlatFunSpec({
    context("given a TestInline value class") {
        context("with a string value") {
            val value = TestInline("hello")
            val yaml = "\"hello\""

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInline.serializer(String.serializer()), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInline.serializer(String.serializer()), yaml)
                result shouldBe value
            }
        }

        context("with an integer value") {
            val value = TestInline(123)
            val yaml = "123"

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInline.serializer(Int.serializer()), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInline.serializer(Int.serializer()), yaml)
                result shouldBe value
            }
        }

        context("with a boolean value") {
            val value = TestInline(true)
            val yaml = "true"

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInline.serializer(Boolean.serializer()), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInline.serializer(Boolean.serializer()), yaml)
                result shouldBe value
            }
        }

        context("with a double value") {
            val value = TestInline(3.14)
            val yaml = "3.14"

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInline.serializer(Double.serializer()), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInline.serializer(Double.serializer()), yaml)
                result shouldBe value
            }
        }

        context("with a list value") {
            @Serializable
            data class TestList(val items: List<Int>)

            val testList = TestList(listOf(1, 2))
            val value = TestInline(testList)
            val yaml = """
                items:
                - 1
                - 2
            """.trimIndent()

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInline.serializer(TestList.serializer()), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInline.serializer(TestList.serializer()), yaml)
                result shouldBe value
            }
        }

        context("with a map value") {
            @Serializable
            data class TestMap(val map: Map<String, Int>)

            val testMap = TestMap(mapOf("key1" to 1, "key2" to 2))
            val value = TestInline(testMap)
            val yaml = """
                map:
                  "key1": 1
                  "key2": 2
            """.trimIndent()

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInline.serializer(TestMap.serializer()), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInline.serializer(TestMap.serializer()), yaml)
                result shouldBe value
            }
        }

        context("with a tagged node") {
            val value = TestInline(TestSealedImpl("hello"))
            val yaml = """
                !<com.charleskorn.kaml.testobjects.TestSealedImpl>
                value: "hello"
            """.trimIndent()
            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInline.serializer(TestSealedInterface.serializer()), value)
                result shouldBe yaml
            }
            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInline.serializer(TestSealedInterface.serializer()), yaml)
                result shouldBe value
            }
        }
    }
})
