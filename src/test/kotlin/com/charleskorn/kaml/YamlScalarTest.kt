/*

   Copyright 2018-2020 Charles Korn.

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

import ch.tutteli.atrium.api.fluent.en_GB.message
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlScalarTest : Spek({
    describe("a YAML scalar") {
        mapOf(
            "0" to 0,
            "1" to 1,
            "-1" to -1,
            "0x11" to 17,
            "-0x11" to -17,
            "0o11" to 9,
            "-0o11" to -9
        ).forEach { content, expectedValue ->
            context("given a scalar with the content '$content'") {
                val scalar = YamlScalar(content, YamlPath.root)

                context("retrieving the value as an integer") {
                    val result = scalar.toInt()

                    it("converts it to the expected integer") {
                        expect(result).toBe(expectedValue)
                    }
                }

                context("retrieving the value as a long") {
                    val result = scalar.toLong()

                    it("converts it to the expected long") {
                        expect(result).toBe(expectedValue.toLong())
                    }
                }

                context("retrieving the value as a short") {
                    val result = scalar.toShort()

                    it("converts it to the expected short") {
                        expect(result).toBe(expectedValue.toShort())
                    }
                }

                context("retrieving the value as a byte") {
                    val result = scalar.toByte()

                    it("converts it to the expected byte") {
                        expect(result).toBe(expectedValue.toByte())
                    }
                }
            }
        }

        listOf(
            "a",
            ".",
            "1.",
            ".1",
            "1.5",
            "+",
            "0x",
            "0o",
            ""
        ).forEach { content ->
            context("given a scalar with the content '$content'") {
                val path = YamlPath.root.withListEntry(1, Location(2, 4))
                val scalar = YamlScalar(content, path)

                context("retrieving the value as an integer") {
                    it("throws an appropriate exception") {
                        expect({ scalar.toInt() }).toThrow<YamlScalarFormatException> {
                            message { toBe("Value '$content' is not a valid integer value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                            path { toBe(path) }
                            originalValue { toBe(content) }
                        }
                    }
                }

                context("retrieving the value as a long") {
                    it("throws an appropriate exception") {
                        expect({ scalar.toLong() }).toThrow<YamlScalarFormatException> {
                            message { toBe("Value '$content' is not a valid long value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                            path { toBe(path) }
                            originalValue { toBe(content) }
                        }
                    }
                }

                context("retrieving the value as a short") {
                    it("throws an appropriate exception") {
                        expect({ scalar.toShort() }).toThrow<YamlScalarFormatException> {
                            message { toBe("Value '$content' is not a valid short value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                            path { toBe(path) }
                            originalValue { toBe(content) }
                        }
                    }
                }

                context("retrieving the value as a byte") {
                    it("throws an appropriate exception") {
                        expect({ scalar.toByte() }).toThrow<YamlScalarFormatException> {
                            message { toBe("Value '$content' is not a valid byte value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                            path { toBe(path) }
                            originalValue { toBe(content) }
                        }
                    }
                }
            }
        }

        mapOf(
            "1" to 1.0,
            ".5" to 0.5,
            "1.5" to 1.5,
            "1.5e2" to 150.0,
            "1.5E2" to 150.0,
            "1.5e+2" to 150.0,
            "1.5e-2" to 0.015,
            "-1.5e2" to -150.0,
            "-1.5e+2" to -150.0,
            "-1.5e-2" to -0.015,
            ".nan" to Double.NaN,
            ".NaN" to Double.NaN,
            ".NAN" to Double.NaN,
            ".inf" to Double.POSITIVE_INFINITY,
            ".Inf" to Double.POSITIVE_INFINITY,
            ".INF" to Double.POSITIVE_INFINITY,
            "-.inf" to Double.NEGATIVE_INFINITY,
            "-.Inf" to Double.NEGATIVE_INFINITY,
            "-.INF" to Double.NEGATIVE_INFINITY
        ).forEach { content, expectedResult ->
            context("given a scalar with the content '$content'") {
                val scalar = YamlScalar(content, YamlPath.root.withListEntry(1, Location(2, 4)))

                context("retrieving the value as a double") {
                    val result = scalar.toDouble()

                    it("converts it to the expected double") {
                        expect(result).toBe(expectedResult)
                    }
                }
            }
        }

        mapOf(
            "1" to 1.0f,
            ".5" to 0.5f,
            "1.5" to 1.5f,
            "1.5e2" to 150f,
            "1.5E2" to 150f,
            "1.5e+2" to 150f,
            "1.5e-2" to 0.015f,
            "-1.5e2" to -150f,
            "-1.5e+2" to -150f,
            "-1.5e-2" to -0.015f,
            ".nan" to Float.NaN,
            ".NaN" to Float.NaN,
            ".NAN" to Float.NaN,
            ".inf" to Float.POSITIVE_INFINITY,
            ".Inf" to Float.POSITIVE_INFINITY,
            ".INF" to Float.POSITIVE_INFINITY,
            "-.inf" to Float.NEGATIVE_INFINITY,
            "-.Inf" to Float.NEGATIVE_INFINITY,
            "-.INF" to Float.NEGATIVE_INFINITY
        ).forEach { content, expectedResult ->
            context("given a scalar with the content '$content'") {
                val scalar = YamlScalar(content, YamlPath.root.withListEntry(1, Location(2, 4)))

                context("retrieving the value as a float") {
                    val result = scalar.toFloat()

                    it("converts it to the expected float") {
                        expect(result).toBe(expectedResult)
                    }
                }
            }
        }

        listOf(
            ".",
            "0x2",
            "0o2",
            "1e",
            "1e-",
            "1e+",
            "+",
            ""
        ).forEach { content ->
            context("given a scalar with the content '$content'") {
                val path = YamlPath.root.withListEntry(1, Location(2, 4))
                val scalar = YamlScalar(content, path)

                context("retrieving the value as a float") {
                    it("throws an appropriate exception") {
                        expect({ scalar.toFloat() }).toThrow<YamlScalarFormatException> {
                            message { toBe("Value '$content' is not a valid floating point value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                            path { toBe(path) }
                            originalValue { toBe(content) }
                        }
                    }
                }

                context("retrieving the value as a double") {
                    it("throws an appropriate exception") {
                        expect({ scalar.toDouble() }).toThrow<YamlScalarFormatException> {
                            message { toBe("Value '$content' is not a valid floating point value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                            path { toBe(path) }
                            originalValue { toBe(content) }
                        }
                    }
                }
            }
        }

        mapOf(
            "true" to true,
            "True" to true,
            "TRUE" to true,
            "false" to false,
            "False" to false,
            "FALSE" to false
        ).forEach { content, expectedValue ->
            context("given a scalar with the content '$content'") {
                val scalar = YamlScalar(content, YamlPath.root.withListEntry(1, Location(2, 4)))

                context("retrieving the value as a boolean") {
                    val result = scalar.toBoolean()

                    it("converts it to the expected value") {
                        expect(result).toBe(expectedValue)
                    }
                }
            }
        }

        context("given a scalar with the content 'nonsense'") {
            val path = YamlPath.root.withListEntry(1, Location(2, 4))
            val scalar = YamlScalar("nonsense", path)

            context("retrieving the value as a boolean") {
                it("throws an appropriate exception") {
                    expect({ scalar.toBoolean() }).toThrow<YamlScalarFormatException> {
                        message { toBe("Value 'nonsense' is not a valid boolean, permitted choices are: true or false") }
                        line { toBe(2) }
                        column { toBe(4) }
                        path { toBe(path) }
                        originalValue { toBe("nonsense") }
                    }
                }
            }
        }

        context("given a scalar with the content 'b'") {
            val scalar = YamlScalar("b", YamlPath.root.withListEntry(1, Location(2, 4)))

            context("retrieving the value as a character value") {
                val result = scalar.toChar()

                it("converts it to the expected value") {
                    expect(result).toBe('b')
                }
            }
        }

        listOf(
            "aa",
            ""
        ).forEach { content ->
            context("given a scalar with the content '$content'") {
                val path = YamlPath.root.withListEntry(1, Location(2, 4))
                val scalar = YamlScalar(content, path)

                context("retrieving the value as a character value") {
                    it("throws an appropriate exception") {
                        expect({ scalar.toChar() }).toThrow<YamlScalarFormatException> {
                            message { toBe("Value '$content' is not a valid character value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                            path { toBe(path) }
                            originalValue { toBe(content) }
                        }
                    }
                }
            }
        }

        describe("testing equivalence") {
            val path = YamlPath.root.withListEntry(1, Location(2, 3))
            val scalar = YamlScalar("some content", path)

            context("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    expect(scalar.equivalentContentTo(scalar)).toBe(true)
                }
            }

            context("comparing it to another scalar with the same content and path") {
                it("indicates that they are equivalent") {
                    expect(scalar.equivalentContentTo(YamlScalar("some content", path))).toBe(true)
                }
            }

            context("comparing it to another scalar with the same content but a different path") {
                val otherPath = YamlPath.root.withListEntry(1, Location(2, 4))

                it("indicates that they are equivalent") {
                    expect(scalar.equivalentContentTo(YamlScalar("some content", otherPath))).toBe(true)
                }
            }

            context("comparing it to another scalar with the same path but different content") {
                it("indicates that they are not equivalent") {
                    expect(scalar.equivalentContentTo(YamlScalar("some other content", path))).toBe(false)
                }
            }

            context("comparing it to a null value") {
                it("indicates that they are not equivalent") {
                    expect(scalar.equivalentContentTo(YamlNull(path))).toBe(false)
                }
            }

            context("comparing it to a list") {
                it("indicates that they are not equivalent") {
                    expect(scalar.equivalentContentTo(YamlList(emptyList(), path))).toBe(false)
                }
            }

            context("comparing it to a map") {
                it("indicates that they are not equivalent") {
                    expect(scalar.equivalentContentTo(YamlMap(emptyMap(), path))).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            it("returns the content surrounded by single quotes") {
                expect(YamlScalar("thing", YamlPath.root).contentToString()).toBe("'thing'")
            }
        }

        describe("replacing its path") {
            val original = YamlScalar("abc123", YamlPath.root)
            val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))

            it("returns a scalar value with the provided path") {
                expect(original.withPath(newPath)).toBe(YamlScalar("abc123", newPath))
            }
        }

        describe("converting it to a string") {
            val path = YamlPath.root.withListEntry(2, Location(3, 4))
            val value = YamlScalar("hello world", path)

            it("returns a human-readable description of itself") {
                expect(value.toString()).toBe("scalar @ $path : hello world")
            }
        }
    }
})
