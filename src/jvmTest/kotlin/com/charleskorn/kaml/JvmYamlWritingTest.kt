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
import kotlinx.serialization.Serializable
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

            it("should support polymorphic writing with property") {
                with(
                    Yaml(
                        configuration = YamlConfiguration(
                            polymorphismStyle = PolymorphismStyle.Property,
                        ),
                    ),
                ) {
                    val output = ByteArrayOutputStream()
                    encodeToStream(Animals.serializer(), Animals(listOf(Animal.Dog("Spock"))), output)

                    output.toString(Charsets.UTF_8) shouldBe
                        """
                            animals:
                            - type: "com.charleskorn.kaml.Animal.Dog"
                              name: "Spock"

                        """.trimIndent()
                }
            }

            it("should support polymorphic writing with tag") {
                with(
                    Yaml(
                        configuration = YamlConfiguration(
                            polymorphismStyle = PolymorphismStyle.Tag,
                        ),
                    ),
                ) {
                    val output = ByteArrayOutputStream()
                    encodeToStream(Animals.serializer(), Animals(listOf(Animal.Dog("Spock"))), output)

                    output.toString(Charsets.UTF_8) shouldBe
                        """
                            animals:
                            - !<com.charleskorn.kaml.Animal.Dog>
                              name: "Spock"

                        """.trimIndent()
                }
            }

            it("should support polymorphic writing no tag or property") {
                with(
                    Yaml(
                        configuration = YamlConfiguration(
                            polymorphismStyle = PolymorphismStyle.None,
                        ),
                    ),
                ) {
                    val output = ByteArrayOutputStream()
                    encodeToStream(Animals.serializer(), Animals(listOf(Animal.Dog("Spock"))), output)

                    output.toString(Charsets.UTF_8) shouldBe
                        """
                            animals:
                            - name: "Spock"

                        """.trimIndent()
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

        describe("serializing a string as an explicitly stated ScalarStyle (Single Line - ThingSL)") {
            val output = ByteArrayOutputStream()
            val thing = ThingSL(
                "Name of Thing",
                "String",
                "Single Quoted",
                "Double Quoted",
                "Plain",
            )

            Yaml.default.encodeToStream<ThingSL>(thing, output)
            output.toString(Charsets.UTF_8) shouldBe
                """
                    name: "Name of Thing"
                    string: "String"
                    singleQuoted: 'Single Quoted'
                    doubleQuoted: "Double Quoted"
                    plain: Plain

                """.trimIndent()
        }

        describe("serializing a string as an explicitly stated ScalarStyle (Multi Line - ThingSL)") {
            val output = ByteArrayOutputStream()
            val thing = ThingSL(
                "Name of Thing",
                "String 1\nString 2\nString 3\n",
                "Single Quoted 1\nSingle Quoted 2\nSingle Quoted 3\n",
                "Double Quoted 1\nDouble Quoted 2\nDouble Quoted 3\n",
                "Plain 1\nPlain 2\nPlain 3\n",
            )

            Yaml.default.encodeToStream<ThingSL>(thing, output)
            output.toString(Charsets.UTF_8) shouldBe
                """
                    name: "Name of Thing"
                    string: "String 1\nString 2\nString 3\n"
                    singleQuoted: 'Single Quoted 1

                      Single Quoted 2

                      Single Quoted 3

                      '
                    doubleQuoted: "Double Quoted 1\nDouble Quoted 2\nDouble Quoted 3\n"
                    plain: 'Plain 1

                      Plain 2

                      Plain 3

                      '

                """.trimIndent()
        }

        describe("serializing a string as an explicitly stated ScalarStyle (Single Line - ThingML)") {
            val output = ByteArrayOutputStream()
            val thing = ThingML(
                "Name of Thing",
                "String",
                "Literal",
                "Folded",
                "Plain",
            )

            Yaml.default.encodeToStream<ThingML>(thing, output)
            output.toString(Charsets.UTF_8) shouldBe
                """
                    name: "Name of Thing"
                    string: "String"
                    literal: "Literal"
                    folded: "Folded"
                    plain: Plain

                """.trimIndent()
        }

        describe("serializing a string as an explicitly stated ScalarStyle (Multi Line - ThingML)") {
            val output = ByteArrayOutputStream()
            val thing = ThingML(
                "Name of Thing",
                "String 1\nString 2\nString 3\n",
                "Literal 1\nLiteral 2\nLiteral 3\n",
                "Folded 1\nFolded 2\nFolded 3\n",
                "Plain 1\nPlain 2\nPlain 3\n",
            )

            Yaml.default.encodeToStream<ThingML>(thing, output)
            output.toString(Charsets.UTF_8) shouldBe
                """
                    name: "Name of Thing"
                    string: "String 1\nString 2\nString 3\n"
                    literal: |
                      Literal 1
                      Literal 2
                      Literal 3
                    folded: >
                      Folded 1

                      Folded 2

                      Folded 3
                    plain: 'Plain 1

                      Plain 2

                      Plain 3

                      '

                """.trimIndent()
        }
    }
})

@Serializable
class Animals(val animals: List<Animal>)

@Serializable
sealed interface Animal {
    @Serializable
    data class Dog(val name: String) : Animal

    @Serializable
    data class Cat(val name: String) : Animal
}

@Serializable
data class ThingSL(
    val name: String,
    // Without any annotations
    val string: String,
    @YamlSingleLineStringStyle(SingleLineStringStyle.SingleQuoted)
    @YamlMultiLineStringStyle(MultiLineStringStyle.SingleQuoted)
    val singleQuoted: String,
    @YamlSingleLineStringStyle(SingleLineStringStyle.DoubleQuoted)
    @YamlMultiLineStringStyle(MultiLineStringStyle.DoubleQuoted)
    val doubleQuoted: String,
    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Plain)
    val plain: String,
)

@Serializable
data class ThingML(
    val name: String,
    // Without any annotations
    val string: String,
    @YamlSingleLineStringStyle(SingleLineStringStyle.DoubleQuoted)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Literal)
    val literal: String,
    @YamlSingleLineStringStyle(SingleLineStringStyle.DoubleQuoted)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Folded)
    val folded: String,
    @YamlSingleLineStringStyle(SingleLineStringStyle.Plain)
    @YamlMultiLineStringStyle(MultiLineStringStyle.Plain)
    val plain: String,
)
