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

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assert
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlListTest : Spek({
    describe("a YAML list") {
        describe("testing equivalence") {
            val list = YamlList(
                listOf(
                    YamlScalar("item 1", Location(4, 5)),
                    YamlScalar("item 2", Location(6, 7))
                ), Location(2, 3)
            )

            describe("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    assert(list.equivalentContentTo(list)).toBe(true)
                }
            }

            describe("comparing it to another list with the same items in the same order in a different location") {
                it("indicates that they are equivalent") {
                    assert(list.equivalentContentTo(YamlList(list.items, Location(5, 6)))).toBe(true)
                }
            }

            describe("comparing it to another list with the same items in a different order in the same location") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlList(list.items.reversed(), Location(2, 3)))).toBe(false)
                }
            }

            describe("comparing it to another list with different items in the same location") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlList(emptyList(), Location(2, 3)))).toBe(false)
                }
            }

            describe("comparing it to a scalar value") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlScalar("some content", Location(2, 3)))).toBe(false)
                }
            }

            describe("comparing it to a null value") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlNull(Location(2, 3)))).toBe(false)
                }
            }

            describe("comparing it to a map") {
                it("indicates that they are not equivalent") {
                    assert(list.equivalentContentTo(YamlMap(emptyMap(), Location(2, 3)))).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            context("an empty list") {
                val list = YamlList(emptyList(), Location(1, 1))

                it("returns empty square brackets") {
                    assert(list.contentToString()).toBe("[]")
                }
            }

            context("a list with a single entry") {
                val list = YamlList(listOf(YamlScalar("hello", Location(1, 1))), Location(1, 1))

                it("returns that item surrounded by square brackets") {
                    assert(list.contentToString()).toBe("['hello']")
                }
            }

            context("a list with multiple entries") {
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
