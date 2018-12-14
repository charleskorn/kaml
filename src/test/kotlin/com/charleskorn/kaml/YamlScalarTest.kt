/*

   Copyright 2018 Charles Korn.

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
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

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
            given("a scalar with the content '$content'") {
                val scalar = YamlScalar(content, Location(2, 4))

                on("retrieving the value as an integer") {
                    val result = scalar.toInt()

                    it("converts it to the expected integer") {
                        assert(result).toBe(expectedValue)
                    }
                }

                on("retrieving the value as a long") {
                    val result = scalar.toLong()

                    it("converts it to the expected long") {
                        assert(result).toBe(expectedValue.toLong())
                    }
                }

                on("retrieving the value as a short") {
                    val result = scalar.toShort()

                    it("converts it to the expected short") {
                        assert(result).toBe(expectedValue.toShort())
                    }
                }

                on("retrieving the value as a byte") {
                    val result = scalar.toByte()

                    it("converts it to the expected byte") {
                        assert(result).toBe(expectedValue.toByte())
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
            given("a scalar with the content '$content'") {
                val scalar = YamlScalar(content, Location(2, 4))

                on("retrieving the value as an integer") {
                    it("throws an appropriate exception") {
                        assert({ scalar.toInt() }).toThrow<YamlException> {
                            message { toBe("Value '$content' is not a valid integer value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                        }
                    }
                }

                on("retrieving the value as a long") {
                    it("throws an appropriate exception") {
                        assert({ scalar.toLong() }).toThrow<YamlException> {
                            message { toBe("Value '$content' is not a valid long value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                        }
                    }
                }

                on("retrieving the value as a short") {
                    it("throws an appropriate exception") {
                        assert({ scalar.toShort() }).toThrow<YamlException> {
                            message { toBe("Value '$content' is not a valid short value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                        }
                    }
                }

                on("retrieving the value as a byte") {
                    it("throws an appropriate exception") {
                        assert({ scalar.toByte() }).toThrow<YamlException> {
                            message { toBe("Value '$content' is not a valid byte value.") }
                            line { toBe(2) }
                            column { toBe(4) }
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
            given("a scalar with the content '$content'") {
                val scalar = YamlScalar(content, Location(2, 4))

                on("retrieving the value as a double") {
                    val result = scalar.toDouble()

                    it("converts it to the expected double") {
                        assert(result).toBe(expectedResult)
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
            given("a scalar with the content '$content'") {
                val scalar = YamlScalar(content, Location(2, 4))

                on("retrieving the value as a float") {
                    val result = scalar.toFloat()

                    it("converts it to the expected float") {
                        assert(result).toBe(expectedResult)
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
            given("a scalar with the content '$content'") {
                val scalar = YamlScalar(content, Location(2, 4))

                on("retrieving the value as a float") {
                    it("throws an appropriate exception") {
                        assert({ scalar.toFloat() }).toThrow<YamlException> {
                            message { toBe("Value '$content' is not a valid floating point value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                        }
                    }
                }

                on("retrieving the value as a double") {
                    it("throws an appropriate exception") {
                        assert({ scalar.toDouble() }).toThrow<YamlException> {
                            message { toBe("Value '$content' is not a valid floating point value.") }
                            line { toBe(2) }
                            column { toBe(4) }
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
            given("a scalar with the content '$content'") {
                val scalar = YamlScalar(content, Location(2, 4))

                on("retrieving the value as a boolean") {
                    val result = scalar.toBoolean()

                    it("converts it to the expected value") {
                        assert(result).toBe(expectedValue)
                    }
                }
            }
        }

        given("a scalar with the content 'nonsense'") {
            val scalar = YamlScalar("nonsense", Location(2, 4))

            on("retrieving the value as a boolean") {
                it("throws an appropriate exception") {
                    assert({ scalar.toBoolean() }).toThrow<YamlException> {
                        message { toBe("Value 'nonsense' is not a valid boolean, permitted choices are: true or false") }
                        line { toBe(2) }
                        column { toBe(4) }
                    }
                }
            }
        }

        given("a scalar with the content 'b'") {
            val scalar = YamlScalar("b", Location(2, 4))

            on("retrieving the value as a character value") {
                val result = scalar.toChar()

                it("converts it to the expected value") {
                    assert(result).toBe('b')
                }
            }
        }

        listOf(
            "aa",
            ""
        ).forEach { content ->
            given("a scalar with the content '$content'") {
                val scalar = YamlScalar(content, Location(2, 4))

                on("retrieving the value as a character value") {
                    it("throws an appropriate exception") {
                        assert({ scalar.toChar() }).toThrow<YamlException> {
                            message { toBe("Value '$content' is not a valid character value.") }
                            line { toBe(2) }
                            column { toBe(4) }
                        }
                    }
                }
            }
        }

        describe("testing equivalence") {
            val scalar = YamlScalar("some content", Location(2, 3))

            on("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    assert(scalar.equivalentContentTo(scalar)).toBe(true)
                }
            }

            on("comparing it to another scalar with the same content and location") {
                it("indicates that they are equivalent") {
                    assert(scalar.equivalentContentTo(YamlScalar("some content", Location(2, 3)))).toBe(true)
                }
            }

            on("comparing it to another scalar with the same content but a different location") {
                it("indicates that they are equivalent") {
                    assert(scalar.equivalentContentTo(YamlScalar("some content", Location(2, 4)))).toBe(true)
                }
            }

            on("comparing it to another scalar with the same location but different content") {
                it("indicates that they are not equivalent") {
                    assert(scalar.equivalentContentTo(YamlScalar("some other content", Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a null value") {
                it("indicates that they are not equivalent") {
                    assert(scalar.equivalentContentTo(YamlNull(Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a list") {
                it("indicates that they are not equivalent") {
                    assert(scalar.equivalentContentTo(YamlList(emptyList(), Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a map") {
                it("indicates that they are not equivalent") {
                    assert(scalar.equivalentContentTo(YamlMap(emptyMap(), Location(2, 3)))).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            it("returns the content surrounded by single quotes") {
                assert(YamlScalar("thing", Location(1, 1)).contentToString()).toBe("'thing'")
            }
        }
    }
})
