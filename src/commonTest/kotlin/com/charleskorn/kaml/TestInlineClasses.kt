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

import com.charleskorn.kaml.testobjects.*
import io.kotest.matchers.shouldBe

class TestInlineClasses : FlatFunSpec({
    context("given a TestInline value class") {
        context("with a string value") {
            val value = TestInlineString("hello")
            val yaml = "\"hello\""

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInlineString.serializer(), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInlineString.serializer(), yaml)
                result shouldBe value
            }
        }

        context("with an integer value") {
            val value = TestInlineInt(123)
            val yaml = "123"

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInlineInt.serializer(), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInlineInt.serializer(), yaml)
                result shouldBe value
            }
        }

        context("with a boolean value") {
            val value = TestInlineBoolean(true)
            val yaml = "true"

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInlineBoolean.serializer(), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInlineBoolean.serializer(), yaml)
                result shouldBe value
            }
        }

        context("with a double value") {
            val value = TestInlineDouble(3.14)
            val yaml = "3.14"

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInlineDouble.serializer(), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInlineDouble.serializer(), yaml)
                result shouldBe value
            }
        }

        context("with a list value") {
            val testList = TestList(listOf(1, 2))
            val value = TestInlineList(testList)
            val yaml = """
                items:
                - 1
                - 2
            """.trimIndent()

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInlineList.serializer(), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInlineList.serializer(), yaml)
                result shouldBe value
            }
        }

        context("with a map value") {
            val testMap = TestMap(mapOf("key1" to 1, "key2" to 2))
            val value = TestInlineMap(testMap)
            val yaml = """
                map:
                  "key1": 1
                  "key2": 2
            """.trimIndent()

            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInlineMap.serializer(), value)
                result shouldBe yaml
            }

            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInlineMap.serializer(), yaml)
                result shouldBe value
            }
        }

        context("with a tagged node") {
            val value = TestInlineSealedInterface(TestSealedImpl("hello"))
            val yaml = """
                !<com.charleskorn.kaml.testobjects.TestSealedImpl>
                value: "hello"
            """.trimIndent()
            test("serializing it to YAML produces the expected output") {
                val result = Yaml.default.encodeToString(TestInlineSealedInterface.serializer(), value)
                result shouldBe yaml
            }
            test("deserializing it from YAML produces the expected object") {
                val result = Yaml.default.decodeFromString(TestInlineSealedInterface.serializer(), yaml)
                result shouldBe value
            }
        }
    }
})
