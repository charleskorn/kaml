package com.charleskorn.kaml

import ch.tutteli.atrium.api.fluent.en_GB.message
import ch.tutteli.atrium.api.fluent.en_GB.notToThrow
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import kotlinx.serialization.decodeFromString
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class YamlReadingDuplicatesTest : Spek({
    describe("reading YAML objects") {
        val parser = Yaml.default

        context("without duplicates") {
            val yaml = """
                key1: {}
                key2: {}
            """.trimIndent()

            it("does not throw exceptions") {
                expect({ parser.decodeFromString<MapStructure>(yaml) }).notToThrow()
            }
        }

        context("with a single duplicate of a single key") {
            val yaml = """
                key1: {}
                key1: {}
            """.trimIndent()

            it("throws an exception with a correct message") {
                expect({ parser.decodeFromString<MapStructure>(yaml) }).toThrow<DuplicateKeysException> {
                    message.toBe(
                        """
                            Found duplicates of key 'key1' defined at line 1, column 1:
                              - line 2, column 1
                        """.trimIndent()
                    )
                }
            }
        }

        context("with multiple duplicates of a single key") {
            val yaml = """
                key1: {}
                key1: {}
                key2: {}
                key1: {}
            """.trimIndent()

            it("throws an exception with a correct message") {
                expect({ parser.decodeFromString<MapStructure>(yaml) }).toThrow<DuplicateKeysException> {
                    message.toBe(
                        """
                            Found duplicates of key 'key1' defined at line 1, column 1:
                              - line 2, column 1
                              - line 4, column 1
                        """.trimIndent()
                    )
                }
            }
        }

        context("with multiple duplicates of a multiple keys") {
            val yaml = """
                key1: {}
                key2: {}
                key3: {}
                key1: {}
                key2: {}
                key3: {}
                key2: {}
                key3: {}
                key1: {}
                key3: {}
            """.trimIndent()

            it("throws an exception with a correct message") {
                expect({ parser.decodeFromString<MapStructure>(yaml) }).toThrow<DuplicateKeysException> {
                    message.toBe(
                        """
                            Found duplicates of key 'key1' defined at line 1, column 1:
                              - line 4, column 1
                              - line 9, column 1
                            Found duplicates of key 'key2' defined at line 2, column 1:
                              - line 5, column 1
                              - line 7, column 1
                            Found duplicates of key 'key3' defined at line 3, column 1:
                              - line 6, column 1
                              - line 8, column 1
                              - line 10, column 1
                        """.trimIndent()
                    )
                }
            }
        }

        context("with duplicates in nested values") {
            val yaml = """
                key1:
                  key1: {}
                  key1: {}
                key2:
                  key1: {}
                  key1: {}
            """.trimIndent()

            it("throws an exception with a correct message") {
                expect({ parser.decodeFromString<MapStructure>(yaml) }).toThrow<DuplicateKeysException> {
                    message.toBe(
                        """
                            Found duplicates of key 'key1.key1' defined at line 2, column 3:
                              - line 3, column 3
                            Found duplicates of key 'key2.key1' defined at line 5, column 3:
                              - line 6, column 3
                        """.trimIndent()
                    )
                }
            }
        }

        context("with duplicates of keys with the same structures") {
            val yaml = """
                key1:
                  key1: {}
                  key2: {}
                key1:
                  key1: {}
                  key2: {}
            """.trimIndent()

            it("throws an exception without detecting duplicates between nested structures") {
                expect({ parser.decodeFromString<MapStructure>(yaml) }).toThrow<DuplicateKeysException> {
                    message.toBe(
                        """
                            Found duplicates of key 'key1' defined at line 1, column 1:
                              - line 4, column 1
                        """.trimIndent()
                    )
                }
            }
        }

        context("with duplicates in complex structures") {
            val yaml = """
                 key1:
                   key1:
                     key1: value
                     key2: value
                   key1: {}
                   key2:
                     key1: value
                     key1: value
                     key1: value
                   key2:
                     key1: value
                     key1: value
                   key3: {}
                 key1: {}
                 key2:
                   key1:
                     key1: value
                     key2: value
                   key2: {}
                   key3:
                     key1: value
                     key2: value
                     key1: value
            """.trimIndent()

            it("throws an exception without detecting duplicates between nested structures") {
                expect({ parser.decodeFromString<MapStructure>(yaml) }).toThrow<DuplicateKeysException> {
                    message.toBe(
                        """
                            Found duplicates of key 'key1' defined at line 1, column 1:
                              - line 14, column 1
                            Found duplicates of key 'key1.key1' defined at line 2, column 3:
                              - line 5, column 3
                            Found duplicates of key 'key1.key2' defined at line 6, column 3:
                              - line 10, column 3
                            Found duplicates of key 'key1.key2.key1' defined at line 7, column 5:
                              - line 8, column 5
                              - line 9, column 5
                            Found duplicates of key 'key1.key2.key1' defined at line 11, column 5:
                              - line 12, column 5
                            Found duplicates of key 'key2.key3.key1' defined at line 21, column 5:
                              - line 23, column 5
                        """.trimIndent()
                    )
                }
            }
        }
    }
})

private typealias MapStructure = Map<String, Map<String, Map<String, String>>>
