/*

   Copyright 2018-2019 Charles Korn.

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

import ch.tutteli.atrium.api.cc.en_GB.message
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.assert
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlNodeReaderTest : Spek({
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
            """"null"""" to "null"
        ).forEach { input, expectedResult ->
            context("given the string '$input'") {
                describe("parsing that input") {
                    val parser = YamlParser(input)
                    val result = YamlNodeReader(parser).read()

                    it("returns the expected scalar value") {
                        assert(result).toBe(YamlScalar(expectedResult, Location(1, 1)))
                    }
                }
            }
        }

        // https://yaml.org/spec/1.2/spec.html#id2793979 is useful reference here, as is
        // https://yaml-multiline.info/
        mapOf(
            """
                thing: |
                  line 1
                  line 2
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "line 1\nline 2\n",
            """
                thing: >
                  some
                  text
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "some text\n",

            // Preserve consecutive blank lines when literal
            """
                thing: |
                  some

                  text
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "some\n\ntext\n",

            // Don't preserve consecutive blank lines when folded
            """
                thing: >
                  some

                  text
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "some\ntext\n",

            // No chomping indicator - default behaviour is to clip, so retain trailing new line but not blank lines
            """
                thing: |
                  line 1
                  line 2

                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "line 1\nline 2\n",
            """
                thing: >
                  some
                  text

                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "some text\n",

            // Indentation indicator
            """
                thing: |1
                  line 1
                  line 2
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to " line 1\n line 2\n",
            """
                thing: >1
                  some
                  text
                 here
                 there
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to " some\n text\nhere there\n",

            // 'Strip' chomping indicator - remove all trailing new lines
            """
                thing: |-
                  line 1
                  line 2
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "line 1\nline 2",
            """
                thing: >-
                  some
                  text
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "some text",

            // 'Keep' chomping indicator - keep all trailing new lines
            """
                thing: |+
                  line 1
                  line 2

                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "line 1\nline 2\n\n",
            """
                thing: >+
                  some
                  text

                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to "some text\n\n",

            // Chomping indicator with indentation indicator
            """
                thing: |-1
                  line 1
                  line 2
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to " line 1\n line 2",
            """
                thing: >-1
                  some
                  text
                 here
                 there
                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to " some\n text\nhere there",
            """
                thing: |+1
                  line 1
                  line 2

                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to " line 1\n line 2\n\n",
            """
                thing: >+1
                  some
                  text
                 here
                 there

                # This comment is a hack to workaround https://github.com/kareez/dahgan/issues/31
            """.trimIndent() to " some\n text\nhere there\n\n"
        ).forEach { input, text ->
            context("given the block scalar '$input'") {
                describe("parsing that input") {
                    val parser = YamlParser(input)
                    val result = YamlNodeReader(parser).read()

                    it("returns the expected multi-line text value") {
                        assert(result).toBe(
                            YamlMap(
                                mapOf(
                                    YamlScalar("thing", Location(1, 1)) to YamlScalar(text, Location(1, 8))
                                ), Location(1, 1)
                            )
                        )
                    }
                }
            }
        }

        mapOf(
            "given a double-quoted string without a trailing double quote" to """"hello""",
            "given a single-quoted string without a trailing single quote" to "'hello"
        ).forEach { description, input ->
            context(description) {
                describe("parsing that input") {
                    it("throws an appropriate exception") {
                        assert({
                            val parser = YamlParser(input)
                            YamlNodeReader(parser).read()
                        }).toThrow<MalformedYamlException> {
                            message {
                                toBe(
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
                                )
                            }
                            line { toBe(1) }
                            column { toBe(7) }
                        }
                    }
                }
            }
        }

        context("given a flow-style list without a trailing closing bracket") {
            val input = "[thing"

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message {
                            toBe(
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
                            )
                        }
                        line { toBe(1) }
                        column { toBe(7) }
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
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlScalar("thing1", Location(1, 3)),
                                YamlScalar("thing2", Location(2, 3)),
                                YamlScalar("thing3", Location(3, 3)),
                                YamlScalar("thing4", Location(4, 3)),
                                YamlScalar("thing\"5", Location(5, 3))
                            ), Location(1, 1)
                        )
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
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlScalar("thing1", Location(1, 3)),
                                YamlScalar("thing2", Location(2, 3)),
                                YamlScalar("thing1", Location(1, 3))
                            ), Location(1, 1)
                        )
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
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlScalar("thing1", Location(1, 3)),
                                YamlScalar("thing2", Location(2, 3)),
                                YamlScalar("thing1", Location(1, 3)),
                                YamlScalar("thing3", Location(4, 3)),
                                YamlScalar("thing3", Location(4, 3))
                            ), Location(1, 1)
                        )
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<UnknownAnchorException> {
                        message {
                            toBe("Unknown anchor 'thing'.")
                        }
                        line { toBe(2) }
                        column { toBe(3) }
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
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlScalar("thing1", Location(1, 2)),
                                YamlScalar("thing2", Location(1, 10)),
                                YamlScalar("thing3", Location(1, 18)),
                                YamlScalar("thing4", Location(1, 28)),
                                YamlScalar("thing\"5", Location(1, 38))
                            ), Location(1, 1)
                        )
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
                    assert(result).toBe(YamlList(emptyList(), Location(1, 1)))
                }
            }
        }

        context("given a nested list given with both the inner and outer lists given in flow style with no elements") {
            val input = "[[], []]"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns the expected list") {
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlList(emptyList(), Location(1, 2)),
                                YamlList(emptyList(), Location(1, 6))
                            ), Location(1, 1)
                        )
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
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlList(emptyList(), Location(1, 3)),
                                YamlList(emptyList(), Location(2, 3))
                            ), Location(1, 1)
                        )
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

                it("returns the expected list") {
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlList(
                                    listOf(
                                        YamlScalar("thing1", Location(1, 4)),
                                        YamlScalar("thing2", Location(1, 12))
                                    ), Location(1, 3)
                                ),
                                YamlList(
                                    listOf(
                                        YamlScalar("thing3", Location(3, 7)),
                                        YamlScalar("thing4", Location(4, 7))
                                    ), Location(3, 5)
                                )
                            ), Location(1, 1)
                        )
                    )
                }
            }
        }

        listOf(
            "-",
            "- "
        ).forEach { input ->
            context("given a list with a single null entry in the format '$input'") {
                describe("parsing that input") {
                    val parser = YamlParser(input)
                    val result = YamlNodeReader(parser).read()

                    it("returns a list with a single null entry") {
                        assert(result).toBe(
                            YamlList(
                                listOf(
                                    YamlNull(Location(1, 2))
                                ), Location(1, 1)
                            )
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
                    assert(result).toBe(YamlNull(Location(1, 1)))
                }
            }
        }

        context("given a single key-value pair") {
            val input = "key: value"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns a map with a single key-value pair") {
                    assert(result).toBe(
                        YamlMap(
                            mapOf(
                                YamlScalar("key", Location(1, 1)) to YamlScalar("value", Location(1, 6))
                            ), Location(1, 1)
                        )
                    )
                }
            }
        }

        context("given a single key-value pair with a null value") {
            val input = "key:"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns a map with a single key-value pair with a null value") {
                    assert(result).toBe(
                        YamlMap(
                            mapOf(
                                YamlScalar("key", Location(1, 1)) to YamlNull(Location(1, 5))
                            ), Location(1, 1)
                        )
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

                it("returns a map with two key-value pairs") {
                    assert(result).toBe(
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 1)) to YamlScalar("value1", Location(1, 7)),
                                YamlScalar("key2", Location(2, 1)) to YamlScalar("value2", Location(2, 7))
                            ), Location(1, 1)
                        )
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

                it("returns a map with two key-value pairs") {
                    assert(result).toBe(
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 1)) to YamlScalar("value1", Location(1, 7)),
                                YamlScalar("key2", Location(2, 1)) to YamlNull(Location(2, 6))
                            ), Location(1, 1)
                        )
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

                it("returns a map with two key-value pairs") {
                    assert(result).toBe(
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 1)) to YamlScalar("value1", Location(1, 7)),
                                YamlScalar("key2", Location(2, 1)) to YamlScalar("value1", Location(1, 7))
                            ), Location(1, 1)
                        )
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

                it("returns a map with all expected key-value pairs") {
                    assert(result).toBe(
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 1)) to YamlScalar("value1", Location(1, 7)),
                                YamlScalar("key2", Location(2, 1)) to YamlScalar("value2", Location(2, 7)),
                                YamlScalar("key3", Location(3, 1)) to YamlList(
                                    listOf(
                                        YamlScalar("listitem1", Location(4, 5)),
                                        YamlScalar("listitem2", Location(5, 5)),
                                        YamlMap(
                                            mapOf(
                                                YamlScalar("thing", Location(6, 5)) to YamlScalar(
                                                    "value",
                                                    Location(6, 12)
                                                )
                                            ), Location(6, 5)
                                        )
                                    ), Location(4, 3)
                                ),
                                YamlScalar("key4", Location(7, 1)) to YamlList(
                                    listOf(
                                        YamlScalar("something", Location(7, 8))
                                    ), Location(7, 7)
                                ),
                                YamlScalar("key5", Location(8, 1)) to YamlMap(
                                    mapOf(
                                        YamlScalar("inner", Location(9, 3)) to YamlScalar("othervalue", Location(9, 10))
                                    ), Location(9, 3)
                                )
                            ), Location(1, 1)
                        )
                    )
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message {
                            toBe(
                                """
                                mapping values are not allowed here (is the indentation level of this line or a line nearby incorrect?)
                                 at line 3, column 8:
                                       key2: value2
                                           ^
                                """.trimIndent()
                            )
                        }
                        line { toBe(3) }
                        column { toBe(8) }
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message {
                            toBe(
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
                            )
                        }
                        line { toBe(3) }
                        column { toBe(2) }
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message {
                            toBe(
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
                            )
                        }
                        line { toBe(3) }
                        column { toBe(2) }
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
                    assert(result).toBe(
                        YamlMap(emptyMap(), Location(1, 1))
                    )
                }
            }
        }

        context("given a single opening curly brace") {
            val input = "{"

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message {
                            toBe(
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
                            )
                        }
                        line { toBe(1) }
                        column { toBe(2) }
                    }
                }
            }
        }

        context("given a single key-value pair in flow style") {
            val input = "{key: value}"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns a map with a single key-value pair") {
                    assert(result).toBe(
                        YamlMap(
                            mapOf(
                                YamlScalar("key", Location(1, 2)) to YamlScalar("value", Location(1, 7))
                            ), Location(1, 1)
                        )
                    )
                }
            }
        }

        context("given a single key-value pair in flow style with a missing closing curly brace") {
            val input = "{key: value"

            describe("parsing that input") {
                it("throws an appropriate exception") {
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message {
                            toBe(
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
                            )
                        }
                        line { toBe(1) }
                        column { toBe(12) }
                    }
                }
            }
        }

        context("given two key-value pairs in flow style") {
            val input = "{key1: value1, key2: value2}"

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns a map with a single key-value pair") {
                    assert(result).toBe(
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 2)) to YamlScalar("value1", Location(1, 8)),
                                YamlScalar("key2", Location(1, 16)) to YamlScalar("value2", Location(1, 22))
                            ), Location(1, 1)
                        )
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
                    assert(result).toBe(
                        YamlScalar("somevalue", Location(2, 1))
                    )
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
                    assert(result).toBe(
                        YamlScalar("somevalue", Location(1, 1))
                    )
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
                    assert(result).toBe(
                        YamlScalar("somevalue", Location(3, 1))
                    )
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
                    assert(result).toBe(
                        YamlScalar("somevalue", Location(1, 1))
                    )
                }
            }
        }

        mapOf(
            "!thing" to "tags",
            "!!str 'some string'" to "tags"
        ).forEach { input, featureName ->
            context("given the input '$input' which contains an unsupported YAML feature") {
                describe("parsing that input") {
                    it("throws an appropriate exception stating that the YAML feature being used is not supported") {
                        assert({
                            val parser = YamlParser(input)
                            YamlNodeReader(parser).read()
                        }).toThrow<UnsupportedYamlFeatureException> {
                            message { toBe("Unsupported YAML feature: $featureName") }
                            line { toBe(1) }
                            column { toBe(1) }
                            featureName { toBe(featureName) }
                        }
                    }
                }
            }
        }

        context("given an empty document") {
            val input = ""

            describe("parsing that input") {
                it("throws an appropriate exception stating that the document is empty") {
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<EmptyYamlDocumentException> {
                        message { toBe("The YAML document is empty.") }
                        line { toBe(1) }
                        column { toBe(1) }
                    }
                }
            }
        }

        context("given a document with just a comment") {
            val input = "# this is a comment"

            describe("parsing that input") {
                it("throws an appropriate exception stating that the document is empty") {
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<EmptyYamlDocumentException> {
                        message { toBe("The YAML document is empty.") }
                        line { toBe(1) }
                        column { toBe(1) }
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

                it("returns that map with the values from the source map merged into it") {
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", Location(1, 13)) to YamlScalar("1", Location(1, 16)),
                                        YamlScalar("y", Location(1, 19)) to YamlScalar("2", Location(1, 22))
                                    ), Location(1, 3)
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", Location(1, 13)) to YamlScalar("1", Location(1, 16)),
                                        YamlScalar("y", Location(1, 19)) to YamlScalar("2", Location(1, 22)),
                                        YamlScalar("r", Location(4, 3)) to YamlScalar("10", Location(4, 6)),
                                        YamlScalar("label", Location(5, 3)) to YamlScalar("center/big", Location(5, 10))
                                    ), Location(3, 3)
                                )
                            ), Location(1, 1)
                        )
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

                it("returns that map with the values from the source map merged into it, with the local values taking precedence") {
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", Location(1, 13)) to YamlScalar("1", Location(1, 16)),
                                        YamlScalar("y", Location(1, 19)) to YamlScalar("2", Location(1, 22))
                                    ), Location(1, 3)
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", Location(4, 3)) to YamlScalar("10", Location(4, 6)),
                                        YamlScalar("y", Location(1, 19)) to YamlScalar("2", Location(1, 22)),
                                        YamlScalar("label", Location(5, 3)) to YamlScalar("center/big", Location(5, 10))
                                    ), Location(3, 3)
                                )
                            ), Location(1, 1)
                        )
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message { toBe("Cannot merge a null value into a map.") }
                        line { toBe(1) }
                        column { toBe(8) }
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message { toBe("Cannot merge a scalar value into a map.") }
                        line { toBe(1) }
                        column { toBe(8) }
                    }
                }
            }
        }

        context("given a map with multiple maps to merge into it") {
            val input = """
                - &CENTER { x: 1, y: 2 }
                - &BIG { r: 10 }

                - << : [ *CENTER, *BIG ]
                  label: center/big
            """.trimIndent()

            describe("parsing that input") {
                val parser = YamlParser(input)
                val result = YamlNodeReader(parser).read()

                it("returns that map with the values from the source maps merged into it") {
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", Location(1, 13)) to YamlScalar("1", Location(1, 16)),
                                        YamlScalar("y", Location(1, 19)) to YamlScalar("2", Location(1, 22))
                                    ), Location(1, 3)
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("r", Location(2, 10)) to YamlScalar("10", Location(2, 13))
                                    ), Location(2, 3)
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", Location(1, 13)) to YamlScalar("1", Location(1, 16)),
                                        YamlScalar("y", Location(1, 19)) to YamlScalar("2", Location(1, 22)),
                                        YamlScalar("r", Location(2, 10)) to YamlScalar("10", Location(2, 13)),
                                        YamlScalar("label", Location(5, 3)) to YamlScalar("center/big", Location(5, 10))
                                    ), Location(4, 3)
                                )
                            ), Location(1, 1)
                        )
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

                it("returns that map with the values from the source maps merged into it, with local values taking precedence over earlier source values, and with earlier source values taking precedence over later source values") {
                    assert(result).toBe(
                        YamlList(
                            listOf(
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", Location(1, 11)) to YamlScalar("0", Location(1, 14)),
                                        YamlScalar("y", Location(1, 17)) to YamlScalar("2", Location(1, 20))
                                    ), Location(1, 3)
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("r", Location(2, 10)) to YamlScalar("10", Location(2, 13))
                                    ), Location(2, 3)
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("r", Location(3, 12)) to YamlScalar("1", Location(3, 15))
                                    ), Location(3, 3)
                                ),
                                YamlMap(
                                    mapOf(
                                        YamlScalar("x", Location(6, 3)) to YamlScalar("1", Location(6, 6)),
                                        YamlScalar("y", Location(1, 17)) to YamlScalar("2", Location(1, 20)),
                                        YamlScalar("r", Location(2, 10)) to YamlScalar("10", Location(2, 13)),
                                        YamlScalar("label", Location(7, 3)) to YamlScalar("center/big", Location(7, 10))
                                    ), Location(5, 3)
                                )
                            ), Location(1, 1)
                        )
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message { toBe("Cannot merge a null value into a map.") }
                        line { toBe(1) }
                        column { toBe(9) }
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message { toBe("Cannot merge a scalar value into a map.") }
                        line { toBe(1) }
                        column { toBe(9) }
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message { toBe("Cannot merge a list value into a map.") }
                        line { toBe(1) }
                        column { toBe(10) }
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
                    assert({
                        val parser = YamlParser(input)
                        YamlNodeReader(parser).read()
                    }).toThrow<MalformedYamlException> {
                        message { toBe("Cannot perform multiple merges into a map.") }
                        line { toBe(2) }
                        column { toBe(3) }
                    }
                }
            }
        }
    }
})
