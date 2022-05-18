/*

   Copyright 2018-2021 Charles Korn.

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

import com.charleskorn.kaml.testobjects.SimpleStructure
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer
import java.io.ByteArrayInputStream

class JvmYamlReadingTest : DescribeSpec({
    describe("JVM-specific extensions for YAML reading") {
        describe("parsing from a stream") {
            val input = "123"
            val result = Yaml.default.decodeFromStream(Int.serializer(), ByteArrayInputStream(input.toByteArray(Charsets.UTF_8)))

            it("successfully deserializes values from a stream") {
                result shouldBe 123
            }
        }

        describe("parsing from a stream via generic extension function") {
            val input = "123"
            val result = Yaml.default.decodeFromStream<Int>(ByteArrayInputStream(input.toByteArray(Charsets.UTF_8)))

            it("successfully deserializes values from a stream") {
                result shouldBe 123
            }
        }

        describe("reading an empty yaml without throwing EmptyYamlDocumentException") {
            val input = ""
            val bytes = ByteArrayInputStream(input.toByteArray(Charsets.UTF_8))
            val emptyAllowedYaml = Yaml(configuration = YamlConfiguration(allowReadingEmptyDocument = true))

            context("empty string reading") {
                it("expect throwing an error because it's an empty document") {
                    shouldThrowExactly<EmptyYamlDocumentException> { Yaml.default.decodeFromStream<String>(bytes) }
                }

                it("expect ignoring empty document because of configuration") {
                    shouldNotThrowAny { emptyAllowedYaml.decodeFromStream<String>(bytes) }
                }
            }

            it("reading list as empty string") {
                shouldNotThrowAny { emptyAllowedYaml.decodeFromStream<List<String>>(bytes) }
            }

            it("reading map as empty string") {
                shouldNotThrowAny { emptyAllowedYaml.decodeFromStream<Map<String, String>>(bytes) }
            }

            it("reading an kotlin class as empty string") {
                shouldNotThrowAny { emptyAllowedYaml.decodeFromStream<SimpleStructure>(bytes) }
            }
        }
    }
})
