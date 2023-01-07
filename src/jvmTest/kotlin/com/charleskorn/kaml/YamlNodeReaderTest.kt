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

@file:Suppress("MoveLambdaOutsideParentheses")

package com.charleskorn.kaml

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class YamlNodeReaderTest : DescribeSpec({
    describe("a YAML node reader") {
        mapOf(
            "hello" to "hello",
            "12" to "12",
            """"hello"""" to "hello",
            "'hello'" to "hello",
            "'he''llo'" to "he'llo",
            """"hello\""""" to """hello"""",
            """"\"hello"""" to """"hello""",
            """"he\"llo"""" to """he"llo""",
            """"he\"\"llo"""" to """he""llo""",
            """"hello\n"""" to "hello\n",
            // Sample from http://yaml.org/spec/1.2/spec.html#escaping/in%20double-quoted%20scalars/
            """"Fun with \\ \" \a \b \e \f \n \r \t \v \0 \  \_ \N \L \P \x41 \u0041 \U00000041"""" to "Fun with \u005C \u0022 \u0007 \u0008 \u001B \u000C \u000A \u000D \u0009 \u000B \u0000 \u0020 \u00A0 \u0085 \u2028 \u2029 A A A",
            "''" to "",
            """""""" to "",
            "'null'" to "null",
            """"null"""" to "null",
            "'~'" to "~",
            """"~"""" to "~",
        ).forEach { (input, expectedResult) ->
            context("given the string '$input'") {
                describe("parsing that input") {
                    val parser = YamlParser(input)
                    val result = YamlNodeReader(parser).read()

                    it("returns the expected scalar value") {
                        result shouldBe YamlScalar(expectedResult, YamlPath.root)
                    }
                }
            }
        }

        // https://yaml.org/spec/1.2/spec.html#id2793979 is useful reference here, as is
        // https://yaml-multiline.info/
        mapOf(
            """
                |thing: |
                |  line 1
                |  line 2
                |
            """.trimMargin() to "line 1\nline 2\n",
            """
                |thing: >
                |  some
                |  text
                |
            """.trimMargin() to "some text\n",

            // Preserve consecutive blank lines when literal
            """
                |thing: |
                |  some
                |
                |  text
                |
            """.trimMargin() to "some\n\ntext\n",

            // Don't preserve consecutive blank lines when folded
            """
                |thing: >
                |  some
                |
                |  text
                |
            """.trimMargin() to "some\ntext\n",

            // No chomping indicator - default behaviour is to clip, so retain trailing new line but not blank lines
            """
                |thing: |
                |  line 1
                |  line 2
                |
                |
            """.trimMargin() to "line 1\nline 2\n",
            """
                |thing: >
                |  some
                |  text
                |
                |
            """.trimMargin() to "some text\n",

            // Indentation indicator
            """
                |thing: |1
                |  line 1
                |  line 2
                |
            """.trimMargin() to " line 1\n line 2\n",
            """
                |thing: >1
                |  some
                |  text
                | here
                | there
                |
            """.trimMargin() to " some\n text\nhere there\n",

            // 'Strip' chomping indicator - remove all trailing new lines
            """
                |thing: |-
                |  line 1
                |  line 2
                |
            """.trimMargin() to "line 1\nline 2",
            """
                |thing: >-
                |  some
                |  text
                |
            """.trimMargin() to "some text",

            // 'Keep' chomping indicator - keep all trailing new lines
            """
                |thing: |+
                |  line 1
                |  line 2
                |
                |
            """.trimMargin() to "line 1\nline 2\n\n",
            """
                |thing: >+
                |  some
                |  text
                |
                |
            """.trimMargin() to "some text\n\n",

            // Chomping indicator with indentation indicator
            """
                |thing: |-1
                |  line 1
                |  line 2
                |
            """.trimMargin() to " line 1\n line 2",
            """
                |thing: >-1
                |  some
                |  text
                | here
                | there
                |
            """.trimMargin() to " some\n text\nhere there",
            """
                |thing: |+1
                |  line 1
                |  line 2
                |
                |
            """.trimMargin() to " line 1\n line 2\n\n",
            """
                |thing: >+1
                |  some
                |  text
                | here
                | there
                |
                |
            """.trimMargin() to " some\n text\nhere there\n\n",
        ).forEach { (input, text) ->
            context("given the block scalar '${input.replace("\n", "\\n")}'") {
                describe("parsing that input") {
                    val parser = YamlParser(input)
                    val result = YamlNodeReader(parser).read()

                    val keyPath = YamlPath.root.withMapElementKey("thing", Location(1, 1))
                    val valuePath = keyPath.withMapElementValue(Location(1, 8))

                    it("returns the expected multi-line text value") {
                        result shouldBe
                            YamlMap(
                                mapOf(
                                    YamlScalar("thing", keyPath) to YamlScalar(text, valuePath),
                                ),
                                YamlPath.root,
                            )
                    }
                }
            }
        }

        mapOf(
            "given a double-quoted string without a trailing double quote" to """"hello""",
            "given a single-quoted string without a trailing single quote" to "'hello",
        ).forEach { (description, input) ->
            context(description) {
                describe("parsing that input") {
                    it("throws an appropriate exception") {
                        val exception = shouldThrow<MalformedYamlException> {
                            val parser = YamlParser(input)
                            YamlNodeReader(parser).read()
                        }

                        exception.asClue {
                            it.message shouldBe
                                """
                                        while scanning a quoted scalar
                                         at line 1, column 1:
                                            $input
                                            ^
                                        found unexpected end of stream
                                         at line 1, column 7:
                                            $input
                                                  ^
                                """.trimIndent()

                            it.line shouldBe 1
                            it.column shouldBe 7
                            it.path shouldBe YamlPath.root.withError(Location(1, 7))
                        }
                    }
                }
            }
        }

        context("given a flow-style list without a trailing closing bracket") {
            val input = "[thing"

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe
                            """
                                    while parsing a flow sequence
                                     at line 1, column 1:
                                        [thing
                                        ^
                                    expected ',' or ']', but got <stream end>
                                     at line 1, column 7:
                                        [thing
                                              ^
                            """.trimIndent()

                        it.line shouldBe 1
                        it.column shouldBe 7
                        it.path shouldBe YamlPath.root.withError(Location(1, 7))
                    }
                }
            }
        }

        context("given some input representing a list of strings") {
            val input = """
                - thing1
                - thing2
                - "thing3"
                - 'thing4'
                - "thing\"5"
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns the expected list") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlScalar("thing1", YamlPath.root.withListEntry(0, Location(1, 3))),
                                YamlScalar("thing2", YamlPath.root.withListEntry(1, Location(2, 3))),
                                YamlScalar("thing3", YamlPath.root.withListEntry(2, Location(3, 3))),
                                YamlScalar("thing4", YamlPath.root.withListEntry(3, Location(4, 3))),
                                YamlScalar("thing\"5", YamlPath.root.withListEntry(4, Location(5, 3))),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given some input representing a list of strings with an alias and anchor present") {
            val input = """
                - &thing thing1
                - thing2
                - *thing
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns the expected list") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlScalar("thing1", YamlPath.root.withListEntry(0, Location(1, 3))),
                                YamlScalar("thing2", YamlPath.root.withListEntry(1, Location(2, 3))),
                                YamlScalar("thing1", YamlPath.root.withListEntry(2, Location(3, 3)).withAliasReference("thing", Location(3, 3)).withAliasDefinition("thing", Location(1, 3))),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given some input representing a list of strings with an alias that is redefined") {
            val input = """
                - &thing thing1
                - thing2
                - *thing
                - &thing thing3
                - *thing
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns the expected list, using the most-recently defined value each time the alias is referenced") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlScalar("thing1", YamlPath.root.withListEntry(0, Location(1, 3))),
                                YamlScalar("thing2", YamlPath.root.withListEntry(1, Location(2, 3))),
                                YamlScalar("thing1", YamlPath.root.withListEntry(2, Location(3, 3)).withAliasReference("thing", Location(3, 3)).withAliasDefinition("thing", Location(1, 3))),
                                YamlScalar("thing3", YamlPath.root.withListEntry(3, Location(4, 3))),
                                YamlScalar("thing3", YamlPath.root.withListEntry(4, Location(5, 3)).withAliasReference("thing", Location(5, 3)).withAliasDefinition("thing", Location(4, 3))),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given some input representing a list of strings with a reference to a non-existent anchor") {
            val input = """
                - thing2
                - *thing
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    val exception = shouldThrow<UnknownAnchorException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "Unknown anchor 'thing'."
                        it.line shouldBe 2
                        it.column shouldBe 3
                        it.path shouldBe YamlPath.root.withListEntry(1, Location(2, 3)).withError(Location(2, 3))
                    }
                }
            }
        }

        context("given some input representing a list of strings in flow style") {
            val input = """[thing1, thing2, "thing3", 'thing4', "thing\"5"]"""

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns the expected list") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlScalar("thing1", YamlPath.root.withListEntry(0, Location(1, 2))),
                                YamlScalar("thing2", YamlPath.root.withListEntry(1, Location(1, 10))),
                                YamlScalar("thing3", YamlPath.root.withListEntry(2, Location(1, 18))),
                                YamlScalar("thing4", YamlPath.root.withListEntry(3, Location(1, 28))),
                                YamlScalar("thing\"5", YamlPath.root.withListEntry(4, Location(1, 38))),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given some input representing an empty list of strings in flow style") {
            val input = "[]"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns an empty list") {
                    result shouldBe YamlList(emptyList(), YamlPath.root)
                }
            }
        }

        context("given a nested list given with both the inner and outer lists given in flow style with no elements") {
            val input = "[[], []]"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns the expected list") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlList(emptyList(), YamlPath.root.withListEntry(0, Location(1, 2))),
                                YamlList(emptyList(), YamlPath.root.withListEntry(1, Location(1, 6))),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a nested list given with the outer list in non-flow style and the inner lists in flow style with no elements") {
            val input = """
                - []
                - []
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns the expected list") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlList(emptyList(), YamlPath.root.withListEntry(0, Location(1, 3))),
                                YamlList(emptyList(), YamlPath.root.withListEntry(1, Location(2, 3))),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a nested list given with the outer list in non-flow style and the inner lists in both non-flow and flow styles with some elements") {
            val input = """
                - [thing1, thing2]
                -
                    - thing3
                    - thing4
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                val firstListPath = YamlPath.root.withListEntry(0, Location(1, 3))
                val secondListPath = YamlPath.root.withListEntry(1, Location(3, 5))

                it("returns the expected list") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlList(
                                    listOf(
                                        YamlScalar("thing1", firstListPath.withListEntry(0, Location(1, 4))),
                                        YamlScalar("thing2", firstListPath.withListEntry(1, Location(1, 12))),
                                    ),
                                    firstListPath,
                                ),
                                YamlList(
                                    listOf(
                                        YamlScalar("thing3", secondListPath.withListEntry(0, Location(3, 7))),
                                        YamlScalar("thing4", secondListPath.withListEntry(1, Location(4, 7))),
                                    ),
                                    secondListPath,
                                ),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        listOf(
            "-",
            "- ",
        ).forEach { input ->
            context("given a list with a single null entry in the format '$input'") {
                describe("parsing that input") {
                    val parser = YamlParser(input)
                    val result = YamlNodeReader(parser).read()

                    it("returns a list with a single null entry") {
                        result shouldBe
                            YamlList(
                                listOf(
                                    YamlNull(YamlPath.root.withListEntry(0, Location(1, 2))),
                                ),
                                YamlPath.root,
                            )
                    }
                }
            }
        }

        context("given the string 'null'") {
            val input = "null"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns a single null entry") {
                    result shouldBe YamlNull(YamlPath.root)
                }
            }
        }

        // See https://github.com/charleskorn/kaml/issues/149.
        context("given the string '~'") {
            val input = "~"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns a single null entry") {
                    result shouldBe YamlNull(YamlPath.root)
                }
            }
        }

        context("given a single key-value pair") {
            val input = "key: value"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()
                val keyPath = YamlPath.root.withMapElementKey("key", Location(1, 1))
                val valuePath = keyPath.withMapElementValue(Location(1, 6))

                it("returns a map with a single key-value pair") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("key", keyPath) to YamlScalar("value", valuePath),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a single key-value pair with a null value") {
            val input = "key:"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()
                val keyPath = YamlPath.root.withMapElementKey("key", Location(1, 1))
                val valuePath = keyPath.withMapElementValue(Location(1, 5))

                it("returns a map with a single key-value pair with a null value") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("key", keyPath) to YamlNull(valuePath),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a map with two key-value pairs") {
            val input = """
                key1: value1
                key2: value2
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()
                val key1Path = YamlPath.root.withMapElementKey("key1", Location(1, 1))
                val value1Path = key1Path.withMapElementValue(Location(1, 7))
                val key2Path = YamlPath.root.withMapElementKey("key2", Location(2, 1))
                val value2Path = key2Path.withMapElementValue(Location(2, 7))

                it("returns a map with two key-value pairs") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value1", value1Path),
                                YamlScalar("key2", key2Path) to YamlScalar("value2", value2Path),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a map with two key-value pairs, one of which has a null value") {
            val input = """
                key1: value1
                key2:
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()
                val key1Path = YamlPath.root.withMapElementKey("key1", Location(1, 1))
                val value1Path = key1Path.withMapElementValue(Location(1, 7))
                val key2Path = YamlPath.root.withMapElementKey("key2", Location(2, 1))
                val value2Path = key2Path.withMapElementValue(Location(2, 6))

                it("returns a map with two key-value pairs") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value1", value1Path),
                                YamlScalar("key2", key2Path) to YamlNull(value2Path),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a map with two key-value pairs, one of which has a reference to the other") {
            val input = """
                key1: &value value1
                key2: *value
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()
                val key1Path = YamlPath.root.withMapElementKey("key1", Location(1, 1))
                val value1Path = key1Path.withMapElementValue(Location(1, 7))
                val key2Path = YamlPath.root.withMapElementKey("key2", Location(2, 1))
                val value2Path = key2Path.withMapElementValue(Location(2, 7)).withAliasReference("value", Location(2, 7)).withAliasDefinition("value", Location(1, 7))

                it("returns a map with two key-value pairs") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value1", value1Path),
                                YamlScalar("key2", key2Path) to YamlScalar("value1", value2Path),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a map with nested values") {
            val input = """
                key1: value1
                key2: value2
                key3:
                  - listitem1
                  - listitem2
                  - thing: value
                key4: [something]
                key5:
                  inner: othervalue
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                val key1Path = YamlPath.root.withMapElementKey("key1", Location(1, 1))
                val key2Path = YamlPath.root.withMapElementKey("key2", Location(2, 1))
                val key3Path = YamlPath.root.withMapElementKey("key3", Location(3, 1))
                val value3Path = key3Path.withMapElementValue(Location(4, 3))
                val thingPath = value3Path.withListEntry(2, Location(6, 5)).withMapElementKey("thing", Location(6, 5))
                val key4Path = YamlPath.root.withMapElementKey("key4", Location(7, 1))
                val value4Path = key4Path.withMapElementValue(Location(7, 7))
                val key5Path = YamlPath.root.withMapElementKey("key5", Location(8, 1))
                val value5Path = key5Path.withMapElementValue(Location(9, 3))
                val innerPath = value5Path.withMapElementKey("inner", Location(9, 3))

                it("returns a map with all expected key-value pairs") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value1", key1Path.withMapElementValue(Location(1, 7))),
                                YamlScalar("key2", key2Path) to YamlScalar("value2", key2Path.withMapElementValue(Location(2, 7))),
                                YamlScalar("key3", key3Path) to YamlList(
                                    listOf(
                                        YamlScalar("listitem1", value3Path.withListEntry(0, Location(4, 5))),
                                        YamlScalar("listitem2", value3Path.withListEntry(1, Location(5, 5))),
                                        YamlMap(
                                            mapOf(
                                                YamlScalar("thing", thingPath) to YamlScalar("value", thingPath.withMapElementValue(Location(6, 12))),
                                            ),
                                            value3Path.withListEntry(2, Location(6, 5)),
                                        ),
                                    ),
                                    value3Path,
                                ),
                                YamlScalar("key4", key4Path) to YamlList(
                                    listOf(
                                        YamlScalar("something", value4Path.withListEntry(0, Location(7, 8))),
                                    ),
                                    value4Path,
                                ),
                                YamlScalar("key5", key5Path) to YamlMap(
                                    mapOf(
                                        YamlScalar("inner", innerPath) to YamlScalar("othervalue", innerPath.withMapElementValue(Location(9, 10))),
                                    ),
                                    value5Path,
                                ),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        mapOf(
            "null" to "null value",
            "~" to "shorthand null value",
            "[]" to "list",
            "{}" to "map",
            "!thing hello" to "tagged value",
        ).forEach { (value, description) ->
            context("given a map with a $description for a key") {
                val input = """
                    key: value
                    $value: something
                """.trimIndent()

                describe("parsing that input") {
                    it("throws an appropriate exception") {
                        val exception = shouldThrow<MalformedYamlException> {
                            val parser = YamlParser(input)
                            YamlNodeReader(parser).read()
                        }

                        exception.asClue {
                            it.message shouldBe "Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)"
                            it.line shouldBe 2
                            it.column shouldBe 1
                            it.path shouldBe YamlPath.root.withError(Location(2, 1))
                        }
                    }
                }
            }
        }

        context("given a map with the the word 'null' as a key in quotes") {
            val input = """
                key: value
                "null": something
            """.trimIndent()

            describe("parsing that input") {
                it("does not throw an exception") {
                    shouldNotThrowAny {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }
                }
            }
        }

        context("given a map with the the value '~' as a key in quotes") {
            val input = """
                key: value
                "~": something
            """.trimIndent()

            describe("parsing that input") {
                it("does not throw an exception") {
                    shouldNotThrowAny {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }
                }
            }
        }

        context("given a key-value pair with extra indentation") {
            val input = """
                thing:
                  key1: value1
                   key2: value2
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe
                            """
                                mapping values are not allowed here (is the indentation level of this line or a line nearby incorrect?)
                                 at line 3, column 8:
                                       key2: value2
                                           ^
                            """.trimIndent()

                        it.line shouldBe 3
                        it.column shouldBe 8
                        it.path shouldBe YamlPath.root.withMapElementKey("thing", Location(1, 1)).withMapElementValue(Location(2, 3)).withError(Location(3, 8))
                    }
                }
            }
        }

        context("given a key-value pair with not enough indentation") {
            val input = """
                thing:
                  key1: value1
                 key2: value2
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe
                            """
                                while parsing a block mapping
                                 at line 1, column 1:
                                    thing:
                                    ^
                                expected <block end>, but found '<block mapping start>' (is the indentation level of this line or a line nearby incorrect?)
                                 at line 3, column 2:
                                     key2: value2
                                     ^
                            """.trimIndent()

                        it.line shouldBe 3
                        it.column shouldBe 2
                        it.path shouldBe YamlPath.root.withError(Location(3, 2))
                    }
                }
            }
        }

        context("given a list item in a map value with not enough indentation") {
            val input = """
                thing:
                  - value1
                 - value2
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe
                            """
                                while parsing a block mapping
                                 at line 1, column 1:
                                    thing:
                                    ^
                                expected <block end>, but found '<block sequence start>' (is the indentation level of this line or a line nearby incorrect?)
                                 at line 3, column 2:
                                     - value2
                                     ^
                            """.trimIndent()

                        it.line shouldBe 3
                        it.column shouldBe 2
                        it.path shouldBe YamlPath.root.withError(Location(3, 2))
                    }
                }
            }
        }

        context("given an empty map in flow style") {
            val input = "{}"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns an empty map") {
                    result shouldBe
                        YamlMap(emptyMap(), YamlPath.root)
                }
            }
        }

        context("given a single opening curly brace") {
            val input = "{"

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe
                            """
                                while parsing a flow node
                                 at line 1, column 2:
                                    {
                                     ^
                                expected the node content, but found '<stream end>'
                                 at line 1, column 2:
                                    {
                                     ^
                            """.trimIndent()

                        it.line shouldBe 1
                        it.column shouldBe 2
                        it.path shouldBe YamlPath.root.withError(Location(1, 2))
                    }
                }
            }
        }

        context("given a single key-value pair in flow style") {
            val input = "{key: value}"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()
                val keyPath = YamlPath.root.withMapElementKey("key", Location(1, 2))
                val valuePath = keyPath.withMapElementValue(Location(1, 7))

                it("returns a map with a single key-value pair") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("key", keyPath) to YamlScalar("value", valuePath),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a single key-value pair in flow style with a missing closing curly brace") {
            val input = "{key: value"

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe
                            """
                                    while parsing a flow mapping
                                     at line 1, column 1:
                                        {key: value
                                        ^
                                    expected ',' or '}', but got <stream end>
                                     at line 1, column 12:
                                        {key: value
                                                   ^
                            """.trimIndent()

                        it.line shouldBe 1
                        it.column shouldBe 12
                        it.path shouldBe YamlPath.root.withError(Location(1, 12))
                    }
                }
            }
        }

        context("given two key-value pairs in flow style") {
            val input = "{key1: value1, key2: value2}"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()
                val key1Path = YamlPath.root.withMapElementKey("key1", Location(1, 2))
                val value1Path = key1Path.withMapElementValue(Location(1, 8))
                val key2Path = YamlPath.root.withMapElementKey("key2", Location(1, 16))
                val value2Path = key2Path.withMapElementValue(Location(1, 22))

                it("returns a map with a single key-value pair") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value1", value1Path),
                                YamlScalar("key2", key2Path) to YamlScalar("value2", value2Path),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a scalar with a preceding comment") {
            val input = """
                # this is a comment
                somevalue
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns that scalar, ignoring the comment") {
                    // FIXME: ideally we'd return a path with the location (2, 1)
                    result shouldBe YamlScalar("somevalue", YamlPath.root)
                }
            }
        }

        context("given a scalar with a following comment") {
            val input = """
                somevalue
                # this is a comment
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns that scalar, ignoring the comment") {
                    result shouldBe
                        YamlScalar("somevalue", YamlPath.root)
                }
            }
        }

        context("given a scalar with a multiple lines of preceding and following comments") {
            val input = """
                # this is a comment
                # also a comment
                somevalue
                # still a comment
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns that scalar, ignoring the comments") {
                    // FIXME: ideally we'd return a path with the location (3, 1)
                    result shouldBe YamlScalar("somevalue", YamlPath.root)
                }
            }
        }

        context("given a scalar with a following inline comment") {
            val input = """
                somevalue # this is a comment
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns that scalar, ignoring the comment") {
                    result shouldBe
                        YamlScalar("somevalue", YamlPath.root)
                }
            }
        }

        mapOf(
            "!thing" to YamlTaggedNode("!thing", YamlNull(YamlPath.root)),
            "!!str 'some string'" to YamlTaggedNode("tag:yaml.org,2002:str", YamlScalar("some string", YamlPath.root)),
        ).forEach { (input, featureName) ->
            context("given the input '$input' which contains a tagged node") {
                describe("parsing that input") {
                    it("returns the expected node") {
                        YamlNodeReader(YamlParser(input)).read() shouldBe featureName
                    }
                }
            }
        }

        context("given an empty document") {
            val input = ""

            describe("parsing that input") {
                it("throws an appropriate exception stating that the document is empty") {
                    val exception = shouldThrow<EmptyYamlDocumentException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "The YAML document is empty."
                        it.line shouldBe 1
                        it.column shouldBe 1
                        it.path shouldBe YamlPath.root
                    }
                }
            }
        }

        context("given a document with just a comment") {
            val input = "# this is a comment"

            describe("parsing that input") {
                it("throws an appropriate exception stating that the document is empty") {
                    val exception = shouldThrow<EmptyYamlDocumentException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "The YAML document is empty."
                        it.line shouldBe 1
                        it.column shouldBe 1
                        it.path shouldBe YamlPath.root
                    }
                }
            }
        }

        // The following examples are taken from https://yaml.org/type/merge.html
        context("given a map with a single map to merge into it") {
            val input = """
                - &CENTER { x: 1, y: 2 }

                - << : *CENTER
                  r: 10
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                val firstItemPath = YamlPath.root.withListEntry(0, Location(1, 3))
                val firstXPath = firstItemPath.withMapElementKey("x", Location(1, 13))
                val firstYPath = firstItemPath.withMapElementKey("y", Location(1, 19))
                val secondItemPath = YamlPath.root.withListEntry(1, Location(3, 3))
                val mergeResolutionPath = secondItemPath.withMerge(Location(3, 8)).withAliasReference("CENTER", Location(3, 8)).withAliasDefinition("CENTER", Location(1, 3))
                val secondXPath = mergeResolutionPath.withMapElementKey("x", Location(1, 13))
                val secondYPath = mergeResolutionPath.withMapElementKey("y", Location(1, 19))
                val labelPath = secondItemPath.withMapElementKey("label", Location(5, 3))
                val rPath = secondItemPath.withMapElementKey("r", Location(4, 3))

                it("returns that map with the values from the source map merged into it") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", firstXPath) to YamlScalar("1", firstXPath.withMapElementValue(Location(1, 16))),
                                        YamlScalar("y", firstYPath) to YamlScalar("2", firstYPath.withMapElementValue(Location(1, 22))),
                                    ),
                                    firstItemPath,
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", secondXPath) to YamlScalar("1", secondXPath.withMapElementValue(Location(1, 16))),
                                        YamlScalar("y", secondYPath) to YamlScalar("2", secondYPath.withMapElementValue(Location(1, 22))),
                                        YamlScalar("r", rPath) to YamlScalar("10", rPath.withMapElementValue(Location(4, 6))),
                                        YamlScalar("label", labelPath) to YamlScalar("center/big", labelPath.withMapElementValue(Location(5, 10))),
                                    ),
                                    secondItemPath,
                                ),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a map with a single map to merge into it, with both containing the same key") {
            val input = """
                - &CENTER { x: 1, y: 2 }

                - << : *CENTER
                  x: 10
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                val firstItemPath = YamlPath.root.withListEntry(0, Location(1, 3))
                val firstXPath = firstItemPath.withMapElementKey("x", Location(1, 13))
                val firstYPath = firstItemPath.withMapElementKey("y", Location(1, 19))
                val secondItemPath = YamlPath.root.withListEntry(1, Location(3, 3))
                val mergeResolutionPath = secondItemPath.withMerge(Location(3, 8)).withAliasReference("CENTER", Location(3, 8)).withAliasDefinition("CENTER", Location(1, 3))
                val secondXPath = secondItemPath.withMapElementKey("x", Location(4, 3))
                val secondYPath = mergeResolutionPath.withMapElementKey("y", Location(1, 19))
                val labelPath = secondItemPath.withMapElementKey("label", Location(5, 3))

                it("returns that map with the values from the source map merged into it, with the local values taking precedence") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", firstXPath) to YamlScalar("1", firstXPath.withMapElementValue(Location(1, 16))),
                                        YamlScalar("y", firstYPath) to YamlScalar("2", firstYPath.withMapElementValue(Location(1, 22))),
                                    ),
                                    firstItemPath,
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", secondXPath) to YamlScalar("10", secondXPath.withMapElementValue(Location(4, 6))),
                                        YamlScalar("y", secondYPath) to YamlScalar("2", secondYPath.withMapElementValue(Location(1, 22))),
                                        YamlScalar("label", labelPath) to YamlScalar("center/big", labelPath.withMapElementValue(Location(5, 10))),
                                    ),
                                    secondItemPath,
                                ),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a map with a single null value to merge into it") {
            val input = """
                - << : null
                  r: 10
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception stating that merging a null value is not valid") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "Cannot merge a null value into a map."
                        it.line shouldBe 1
                        it.column shouldBe 8
                        it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3)).withMerge(Location(1, 8))
                    }
                }
            }
        }

        context("given a map with a single scalar value to merge into it") {
            val input = """
                - << : abc123
                  r: 10
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception stating that merging a scalar value is not valid") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "Cannot merge a scalar value into a map."
                        it.line shouldBe 1
                        it.column shouldBe 8
                        it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3)).withMerge(Location(1, 8))
                    }
                }
            }
        }

        context("given a map with multiple maps to merge into it") {
            val input = """
                - &CENTER { x: 1, y: 2 }
                - &RADIUS { r: 10 }

                - << : [ *CENTER, *RADIUS ]
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                val firstItemPath = YamlPath.root.withListEntry(0, Location(1, 3))
                val firstXPath = firstItemPath.withMapElementKey("x", Location(1, 13))
                val firstYPath = firstItemPath.withMapElementKey("y", Location(1, 19))

                val secondItemPath = YamlPath.root.withListEntry(1, Location(2, 3))
                val secondRPath = secondItemPath.withMapElementKey("r", Location(2, 13))

                val thirdItemPath = YamlPath.root.withListEntry(2, Location(4, 3))
                val centerMergeResolutionPath = thirdItemPath.withMerge(Location(4, 8)).withListEntry(0, Location(4, 10)).withAliasReference("CENTER", Location(4, 10)).withAliasDefinition("CENTER", Location(1, 3))
                val thirdXPath = centerMergeResolutionPath.withMapElementKey("x", Location(1, 13))
                val thirdYPath = centerMergeResolutionPath.withMapElementKey("y", Location(1, 19))
                val radiusMergeResolutionPath = thirdItemPath.withMerge(Location(4, 8)).withListEntry(1, Location(4, 19)).withAliasReference("RADIUS", Location(4, 19)).withAliasDefinition("RADIUS", Location(2, 3))
                val thirdRPath = radiusMergeResolutionPath.withMapElementKey("r", Location(2, 13))
                val labelPath = thirdItemPath.withMapElementKey("label", Location(5, 3))

                it("returns that map with the values from the source maps merged into it") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", firstXPath) to YamlScalar("1", firstXPath.withMapElementValue(Location(1, 16))),
                                        YamlScalar("y", firstYPath) to YamlScalar("2", firstYPath.withMapElementValue(Location(1, 22))),
                                    ),
                                    firstItemPath,
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("r", secondRPath) to YamlScalar("10", secondRPath.withMapElementValue(Location(2, 16))),
                                    ),
                                    secondItemPath,
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", thirdXPath) to YamlScalar("1", thirdXPath.withMapElementValue(Location(1, 16))),
                                        YamlScalar("y", thirdYPath) to YamlScalar("2", thirdYPath.withMapElementValue(Location(1, 22))),
                                        YamlScalar("r", thirdRPath) to YamlScalar("10", thirdRPath.withMapElementValue(Location(2, 16))),
                                        YamlScalar("label", labelPath) to YamlScalar("center/big", labelPath.withMapElementValue(Location(5, 10))),
                                    ),
                                    thirdItemPath,
                                ),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a map with multiple maps to merge into it, with both source and destination maps containing the same keys") {
            val input = """
                - &LEFT { x: 0, y: 2 }
                - &BIG { r: 10 }
                - &SMALL { r: 1 }

                - << : [ *BIG, *LEFT, *SMALL ]
                  x: 1
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                val firstItemPath = YamlPath.root.withListEntry(0, Location(1, 3))
                val firstXPath = firstItemPath.withMapElementKey("x", Location(1, 11))
                val firstYPath = firstItemPath.withMapElementKey("y", Location(1, 17))

                val secondItemPath = YamlPath.root.withListEntry(1, Location(2, 3))
                val secondRPath = secondItemPath.withMapElementKey("r", Location(2, 10))

                val thirdItemPath = YamlPath.root.withListEntry(2, Location(3, 3))
                val thirdRPath = thirdItemPath.withMapElementKey("r", Location(3, 12))

                val fourthItemPath = YamlPath.root.withListEntry(3, Location(5, 3))
                val fourthXPath = fourthItemPath.withMapElementKey("x", Location(6, 3))
                val leftMergeResolutionPath = fourthItemPath.withMerge(Location(5, 8)).withListEntry(1, Location(5, 16)).withAliasReference("LEFT", Location(5, 16)).withAliasDefinition("LEFT", Location(1, 3))
                val fourthYPath = leftMergeResolutionPath.withMapElementKey("y", Location(1, 17))
                val bigMergeResolutionPath = fourthItemPath.withMerge(Location(5, 8)).withListEntry(0, Location(5, 10)).withAliasReference("BIG", Location(5, 10)).withAliasDefinition("BIG", Location(2, 3))
                val fourthRPath = bigMergeResolutionPath.withMapElementKey("r", Location(2, 10))
                val labelPath = fourthItemPath.withMapElementKey("label", Location(7, 3))

                it("returns that map with the values from the source maps merged into it, with local values taking precedence over earlier source values, and with earlier source values taking precedence over later source values") {
                    result shouldBe
                        YamlList(
                            listOf(
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", firstXPath) to YamlScalar("0", firstXPath.withMapElementValue(Location(1, 14))),
                                        YamlScalar("y", firstYPath) to YamlScalar("2", firstYPath.withMapElementValue(Location(1, 20))),
                                    ),
                                    firstItemPath,
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("r", secondRPath) to YamlScalar("10", secondRPath.withMapElementValue(Location(2, 13))),
                                    ),
                                    secondItemPath,
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("r", thirdRPath) to YamlScalar("1", thirdRPath.withMapElementValue(Location(3, 15))),
                                    ),
                                    thirdItemPath,
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", fourthXPath) to YamlScalar("1", fourthXPath.withMapElementValue(Location(6, 6))),
                                        YamlScalar("y", fourthYPath) to YamlScalar("2", fourthYPath.withMapElementValue(Location(1, 20))),
                                        YamlScalar("r", fourthRPath) to YamlScalar("10", fourthRPath.withMapElementValue(Location(2, 13))),
                                        YamlScalar("label", labelPath) to YamlScalar("center/big", labelPath.withMapElementValue(Location(7, 10))),
                                    ),
                                    fourthItemPath,
                                ),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a map with a null value in a list of values to merge into it") {
            val input = """
                - << : [null]
                  r: 10
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception stating that merging a null value is not valid") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "Cannot merge a null value into a map."
                        it.line shouldBe 1
                        it.column shouldBe 9
                        it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3)).withMerge(Location(1, 8)).withListEntry(0, Location(1, 9))
                    }
                }
            }
        }

        context("given a map with a scalar value in a list of values to merge into it") {
            val input = """
                - << : [abc123]
                  r: 10
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception stating that merging a scalar value is not valid") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "Cannot merge a scalar value into a map."
                        it.line shouldBe 1
                        it.column shouldBe 9
                        it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3)).withMerge(Location(1, 8)).withListEntry(0, Location(1, 9))
                    }
                }
            }
        }

        context("given a map with a list value in a list of values to merge into it") {
            val input = """
                - << : [ [] ]
                  r: 10
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception stating that merging a list value is not valid") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "Cannot merge a list value into a map."
                        it.line shouldBe 1
                        it.column shouldBe 10
                        it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3)).withMerge(Location(1, 8)).withListEntry(0, Location(1, 10))
                    }
                }
            }
        }

        context("given a map with multiple lists of items to merge into it") {
            val input = """
                - << : []
                  << : []
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception stating that multiple merges are not possible") {
                    val exception = shouldThrow<MalformedYamlException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }

                    exception.asClue {
                        it.message shouldBe "Cannot perform multiple '<<' merges into a map. Instead, combine all merges into a single '<<' entry."
                        it.line shouldBe 2
                        it.column shouldBe 3
                        it.path shouldBe YamlPath.root.withListEntry(0, Location(1, 3)).withMapElementKey("<<", Location(2, 3))
                    }
                }
            }
        }

        context("given a top-level map with an entry matching the extension definition prefix") {
            val input = """
                .extension: &extension extension-value

                foo:
                    bar: value
                    baz: *extension
            """.trimIndent()

            describe("parsing that input with an extension definition prefix defined") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser, extensionDefinitionPrefix = ".").read()

                val fooKeyPath = YamlPath.root.withMapElementKey("foo", Location(3, 1))
                val fooValuePath = fooKeyPath.withMapElementValue(Location(4, 5))
                val barKeyPath = fooValuePath.withMapElementKey("bar", Location(4, 5))
                val bazKeyPath = fooValuePath.withMapElementKey("baz", Location(5, 5))
                val bazValuePath = bazKeyPath.withMapElementValue(Location(5, 10)).withAliasReference("extension", Location(5, 10)).withAliasDefinition("extension", Location(1, 13))

                it("returns the map, merging the alias where it is referenced and removing it from the top-level entry") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("foo", fooKeyPath) to YamlMap(
                                    mapOf(
                                        YamlScalar("bar", barKeyPath) to YamlScalar("value", barKeyPath.withMapElementValue(Location(4, 10))),
                                        YamlScalar("baz", bazKeyPath) to YamlScalar("extension-value", bazValuePath),
                                    ),
                                    fooValuePath,
                                ),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a non-top-level map with an entry matching the extension definition prefix") {
            val input = """
                foo:
                    .bar: value
            """.trimIndent()

            describe("parsing that input with an extension definition prefix defined") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser, extensionDefinitionPrefix = ".").read()

                val fooKeyPath = YamlPath.root.withMapElementKey("foo", Location(1, 1))
                val fooValuePath = fooKeyPath.withMapElementValue(Location(2, 5))
                val barKeyPath = fooValuePath.withMapElementKey(".bar", Location(2, 5))

                it("returns the map, retaining the key matching the extension definition prefix") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar("foo", fooKeyPath) to YamlMap(
                                    mapOf(
                                        YamlScalar(".bar", barKeyPath) to YamlScalar("value", barKeyPath.withMapElementValue(Location(2, 11))),
                                    ),
                                    fooValuePath,
                                ),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a top-level map that has another map merged into it with an entry with a key matching the extension definition prefix") {
            val input = """
                .extension: &extension
                    .some-key: some-value

                << : [ *extension ]
            """.trimIndent()

            describe("parsing that input with an extension definition prefix defined") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser, extensionDefinitionPrefix = ".").read()

                val keyPath = YamlPath.root
                    .withMerge(Location(4, 6))
                    .withListEntry(0, Location(4, 8))
                    .withAliasReference("extension", Location(4, 8))
                    .withAliasDefinition("extension", Location(1, 13))
                    .withMapElementKey(".some-key", Location(2, 5))

                val valuePath = keyPath.withMapElementValue(Location(2, 16))

                it("returns the map, merging the other map into it and preserving its keys") {
                    result shouldBe
                        YamlMap(
                            mapOf(
                                YamlScalar(".some-key", keyPath) to YamlScalar("some-value", valuePath),
                            ),
                            YamlPath.root,
                        )
                }
            }
        }

        context("given a top-level map with a key matching the extension definition prefix but no anchor defined") {
            val input = """
                .invalid-extension: some-value
            """.trimIndent()

            describe("parsing that input") {
                it("throws an appropriate exception stating that an anchor is required for keys with the extension definition prefix") {
                    val exception = shouldThrow<NoAnchorForExtensionException> {
                        val parser = YamlParser(input)
                        YamlNodeReader(parser, extensionDefinitionPrefix = ".").read()
                    }

                    exception.asClue {
                        it.message shouldBe "The key '.invalid-extension' starts with the extension definition prefix '.' but does not define an anchor."
                        it.line shouldBe 1
                        it.column shouldBe 1
                        it.path shouldBe YamlPath.root.withError(Location(1, 1))
                    }
                }
            }
        }
    }
})
