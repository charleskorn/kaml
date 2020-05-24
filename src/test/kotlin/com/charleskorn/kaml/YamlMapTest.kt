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

import ch.tutteli.atrium.api.fluent.en_GB.message
import ch.tutteli.atrium.api.fluent.en_GB.notToThrow
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlMapTest : Spek({
    describe("a YAML map") {
        describe("creating an instance") {
            context("creating an empty map") {
                it("does not throw an exception") {
                    expect({ YamlMap(emptyMap(), Location(1, 1)) }).notToThrow()
                }
            }

            context("creating a map with a single entry") {
                it("does not throw an exception") {
                    expect({
                        YamlMap(
                            mapOf(YamlScalar("key", Location(1, 1)) to YamlScalar("value", Location(2, 1))),
                            Location(1, 1)
                        )
                    }).notToThrow()
                }
            }

            context("creating a map with two entries, each with unique keys") {
                it("does not throw an exception") {
                    expect({
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 1)) to YamlScalar("value", Location(2, 1)),
                                YamlScalar("key2", Location(3, 1)) to YamlScalar("value", Location(4, 1))
                            ),
                            Location(1, 1)
                        )
                    }).notToThrow()
                }
            }

            context("creating a map with two entries with the same key") {
                it("throws an appropriate exception") {
                    expect({
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 1)) to YamlScalar("value", Location(2, 1)),
                                YamlScalar("key1", Location(3, 1)) to YamlScalar("value", Location(4, 1))
                            ),
                            Location(1, 1)
                        )
                    }).toThrow<DuplicateKeyException> {
                        message { toBe("Duplicate key 'key1'. It was previously given at line 1, column 1.") }
                        line { toBe(3) }
                        column { toBe(1) }
                        originalLocation { toBe(Location(1, 1)) }
                        duplicateLocation { toBe(Location(3, 1)) }
                        key { toBe("'key1'") }
                    }
                }
            }
        }

        describe("testing equivalence") {
            val map = YamlMap(
                mapOf(
                    YamlScalar("key1", Location(4, 1)) to YamlScalar("item 1", Location(4, 5)),
                    YamlScalar("key2", Location(6, 1)) to YamlScalar("item 2", Location(6, 7))
                ), Location(2, 3)
            )

            context("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    expect(map.equivalentContentTo(map)).toBe(true)
                }
            }

            context("comparing it to another map with the same items in the same order in a different location") {
                it("indicates that they are equivalent") {
                    expect(map.equivalentContentTo(YamlMap(map.entries, Location(5, 6)))).toBe(true)
                }
            }

            context("comparing it to another map with the same items in a different order in the same location") {
                it("indicates that they are equivalent") {
                    expect(
                        map.equivalentContentTo(
                            YamlMap(
                                mapOf(
                                    YamlScalar("key2", Location(4, 1)) to YamlScalar("item 2", Location(4, 5)),
                                    YamlScalar("key1", Location(6, 1)) to YamlScalar("item 1", Location(6, 7))
                                ), Location(2, 3)
                            )
                        )
                    ).toBe(true)
                }
            }

            context("comparing it to another map with different keys in the same location") {
                it("indicates that they are not equivalent") {
                    expect(
                        map.equivalentContentTo(
                            YamlMap(
                                mapOf(
                                    YamlScalar("key1", Location(4, 1)) to YamlScalar("item 1", Location(4, 5)),
                                    YamlScalar("key3", Location(6, 1)) to YamlScalar("item 2", Location(6, 7))
                                ), Location(2, 3)
                            )
                        )
                    ).toBe(false)
                }
            }

            context("comparing it to another map with different values in the same location") {
                it("indicates that they are not equivalent") {
                    expect(
                        map.equivalentContentTo(
                            YamlMap(
                                mapOf(
                                    YamlScalar("key1", Location(4, 1)) to YamlScalar("item 1", Location(4, 5)),
                                    YamlScalar("key2", Location(6, 1)) to YamlScalar("item 3", Location(6, 7))
                                ), Location(2, 3)
                            )
                        )
                    ).toBe(false)
                }
            }

            context("comparing it to another map with different items in the same location") {
                it("indicates that they are not equivalent") {
                    expect(map.equivalentContentTo(YamlMap(emptyMap(), Location(2, 3)))).toBe(false)
                }
            }

            context("comparing it to a scalar value") {
                it("indicates that they are not equivalent") {
                    expect(map.equivalentContentTo(YamlScalar("some content", Location(2, 3)))).toBe(false)
                }
            }

            context("comparing it to a null value") {
                it("indicates that they are not equivalent") {
                    expect(map.equivalentContentTo(YamlNull(Location(2, 3)))).toBe(false)
                }
            }

            context("comparing it to a list") {
                it("indicates that they are not equivalent") {
                    expect(map.equivalentContentTo(YamlList(emptyList(), Location(2, 3)))).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            context("an empty map") {
                val map = YamlMap(emptyMap(), Location(1, 1))

                it("returns empty curly brackets") {
                    expect(map.contentToString()).toBe("{}")
                }
            }

            context("a map with a single entry") {
                val map = YamlMap(mapOf(
                    YamlScalar("hello", Location(1, 1)) to YamlScalar("world", Location(2, 1))
                ), Location(1, 1))

                it("returns that item surrounded by curly brackets") {
                    expect(map.contentToString()).toBe("{'hello': 'world'}")
                }
            }

            context("a map with multiple entries") {
                val map = YamlMap(mapOf(
                    YamlScalar("hello", Location(1, 1)) to YamlScalar("world", Location(2, 1)),
                    YamlScalar("also", Location(1, 1)) to YamlScalar("thanks", Location(2, 1))
                ), Location(1, 1))

                it("returns all items separated by commas and surrounded by curly brackets") {
                    expect(map.contentToString()).toBe("{'hello': 'world', 'also': 'thanks'}")
                }
            }
        }
    }
})
