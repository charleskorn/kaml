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

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer
import java.io.ByteArrayOutputStream

class JvmYamlWritingTest : DescribeSpec({
    describe("JVM-specific extensions for YAML writing") {
        describe("writing to a stream") {
            it("returns the value serialized in the expected YAML form") {
                val output = ByteArrayOutputStream()
                Yaml.default.encodeToStream(String.serializer(), "hello world", output)

                output.toString(Charsets.UTF_8) shouldBe "\"hello world\"\n"
            }

            it("should support block literal style output for multiline strings when configured") {
                with(
                    Yaml(
                        configuration = YamlConfiguration(
                            multiLineStringStyle = MultiLineStringStyle.Literal,
                        ),
                    ),
                ) {
                    val output = ByteArrayOutputStream()
                    encodeToStream(String.serializer(), "hello\nworld\nhow are | you?\n", output)

                    output.toString(Charsets.UTF_8) shouldBe
                        """
                           |
                             hello
                             world
                             how are | you?

                        """.trimIndent()
                }

                with(
                    Yaml(
                        configuration = YamlConfiguration(
                            multiLineStringStyle = MultiLineStringStyle.DoubleQuoted,
                        ),
                    ),
                ) {
                    val output = ByteArrayOutputStream()
                    encodeToStream(String.serializer(), "hello\nworld\nhow are | you?\n", output)
                    output.toString(Charsets.UTF_8) shouldBe
                        """|"hello\nworld\nhow are | you?\n"
                           |
                        """.trimMargin()
                }
            }

            it("should support configurable scalar quoting") {
                with(
                    Yaml(
                        configuration = YamlConfiguration(
                            singleLineStringStyle = SingleLineStringStyle.SingleQuoted,
                        ),
                    ),
                ) {
                    val output = ByteArrayOutputStream()
                    encodeToStream(String.serializer(), "hello, world", output)

                    output.toString(Charsets.UTF_8) shouldBe
                        """|'hello, world'
                           |
                        """.trimMargin()
                }

                with(
                    Yaml(
                        configuration = YamlConfiguration(
                            singleLineStringStyle = SingleLineStringStyle.DoubleQuoted,
                        ),
                    ),
                ) {
                    val output = ByteArrayOutputStream()
                    encodeToStream(String.serializer(), "hello, world", output)

                    output.toString(Charsets.UTF_8) shouldBe
                        """|"hello, world"
                           |
                        """.trimMargin()
                }
            }
        }

        describe("writing to a stream via generic extension function") {
            val output = ByteArrayOutputStream()
            Yaml.default.encodeToStream<String>("hello world", output)

            it("returns the value serialized in the expected YAML form") {
                output.toString(Charsets.UTF_8) shouldBe "\"hello world\"\n"
            }
        }
    }
})
