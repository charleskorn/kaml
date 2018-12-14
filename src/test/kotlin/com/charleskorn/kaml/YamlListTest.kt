package com.charleskorn.kaml

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assert
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object YamlListTest : Spek({
    describe("a YAML list") {
        describe("testing equivalence") {
            val list = YamlList(
                listOf(
                    YamlScalar("item 1", Location(4, 5)),
                    YamlScalar("item 2", Location(6, 7))
                ), Location(2, 3)
            )

            on("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    assert(list.equivalentContentTo(list)).toBe(true)
                }
            }

            on("comparing it to another list with the same items in the same order in a different location") {
                it("indicates that they are equivalent") {
                    assert(list.equivalentContentTo(YamlList(list.items, Location(5, 6)))).toBe(true)
                }
            }

            on("comparing it to another list with the same items in a different order in the same location") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlList(list.items.reversed(), Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to another list with different items in the same location") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlList(emptyList(), Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a scalar value") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlScalar("some content", Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a null value") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlNull(Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a map") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlMap(emptyMap(), Location(2, 3)))).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            given("an empty list") {
                val list = YamlList(emptyList(), Location(1, 1))

                it("returns empty square brackets") {
                    assert(list.contentToString()).toBe("[]")
                }
            }

            given("a list with a single entry") {
                val list = YamlList(listOf(YamlScalar("hello", Location(1, 1))), Location(1, 1))

                it("returns that item surrounded by square brackets") {
                    assert(list.contentToString()).toBe("['hello']")
                }
            }

            given("a list with multiple entries") {
                val list = YamlList(listOf(
                    YamlScalar("hello", Location(1, 1)),
                    YamlScalar("world", Location(2, 1))
                ), Location(1, 1))

                it("returns all items separated by commas and surrounded by square brackets") {
                    assert(list.contentToString()).toBe("['hello', 'world']")
                }
            }
        }
    }
})
