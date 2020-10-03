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
import ch.tutteli.atrium.api.fluent.en_GB.notToThrow
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlMapTest : Spek({
    describe("a YAML map") {
        describe("creating an instance") {
            val mapPath = YamlPath.root
            val key1Path = mapPath.withMapElementKey("key1", Location(4, 1))
            val key1ValuePath = key1Path.withMapElementValue(Location(4, 5))
            val key2Path = mapPath.withMapElementKey("key2", Location(6, 1))
            val key2ValuePath = key2Path.withMapElementValue(Location(6, 7))

            context("creating an empty map") {
                it("does not throw an exception") {
                    expect({ YamlMap(emptyMap(), mapPath) }).notToThrow()
                }
            }

            context("creating a map with a single entry") {
                it("does not throw an exception") {
                    expect({
                        YamlMap(
                            mapOf(YamlScalar("key", key1Path) to YamlScalar("value", key1ValuePath)),
                            mapPath
                        )
                    }).notToThrow()
                }
            }

            context("creating a map with two entries, each with unique keys") {
                it("does not throw an exception") {
                    expect({
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value", key1ValuePath),
                                YamlScalar("key2", key2Path) to YamlScalar("value", key2ValuePath)
                            ),
                            mapPath
                        )
                    }).notToThrow()
                }
            }

            context("creating a map with two entries with the same key") {
                it("throws an appropriate exception") {
                    expect({
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value", key1ValuePath),
                                YamlScalar("key1", key2Path) to YamlScalar("value", key2ValuePath)
                            ),
                            mapPath
                        )
                    }).toThrow<DuplicateKeyException> {
                        message { toBe("Duplicate key 'key1'. It was previously given at line 4, column 1.") }
                        line { toBe(6) }
                        column { toBe(1) }
                        path { toBe(key2Path) }
                        originalLocation { toBe(Location(4, 1)) }
                        originalPath { toBe(key1Path) }
                        duplicateLocation { toBe(Location(6, 1)) }
                        duplicatePath { toBe(key2Path) }
                        key { toBe("'key1'") }
                    }
                }
            }
        }

        describe("testing equivalence") {
            val mapPath = YamlPath.root
            val key1Path = mapPath.withMapElementKey("key1", Location(4, 1))
            val key1ValuePath = key1Path.withMapElementValue(Location(4, 5))
            val key2Path = mapPath.withMapElementKey("key2", Location(6, 1))
            val key2ValuePath = key2Path.withMapElementValue(Location(6, 7))

            val map = YamlMap(
                mapOf(
                    YamlScalar("key1", key1Path) to YamlScalar("item 1", key1ValuePath),
                    YamlScalar("key2", key2Path) to YamlScalar("item 2", key2ValuePath)
                ),
                mapPath
            )

            context("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    expect(map.equivalentContentTo(map)).toBe(true)
                }
            }

            context("comparing it to another map with the same items in the same order with a different path") {
                it("indicates that they are equivalent") {
                    expect(map.equivalentContentTo(YamlMap(map.entries, YamlPath.root.withListEntry(0, Location(3, 4))))).toBe(true)
                }
            }

            context("comparing it to another map with the same items in a different order with the same path") {
                it("indicates that they are equivalent") {
                    expect(
                        map.equivalentContentTo(
                            YamlMap(
                                mapOf(
                                    YamlScalar("key2", key1Path) to YamlScalar("item 2", key1ValuePath),
                                    YamlScalar("key1", key2Path) to YamlScalar("item 1", key2ValuePath)
                                ),
                                mapPath
                            )
                        )
                    ).toBe(true)
                }
            }

            context("comparing it to another map with different keys with the same path") {
                it("indicates that they are not equivalent") {
                    expect(
                        map.equivalentContentTo(
                            YamlMap(
                                mapOf(
                                    YamlScalar("key1", key1Path) to YamlScalar("item 1", key1ValuePath),
                                    YamlScalar("key3", key2Path) to YamlScalar("item 2", key2ValuePath)
                                ),
                                mapPath
                            )
                        )
                    ).toBe(false)
                }
            }

            context("comparing it to another map with different values with the same path") {
                it("indicates that they are not equivalent") {
                    expect(
                        map.equivalentContentTo(
                            YamlMap(
                                mapOf(
                                    YamlScalar("key1", key1Path) to YamlScalar("item 1", key1ValuePath),
                                    YamlScalar("key2", key2Path) to YamlScalar("item 3", key2ValuePath)
                                ),
                                mapPath
                            )
                        )
                    ).toBe(false)
                }
            }

            context("comparing it to another map with different items with the same path") {
                it("indicates that they are not equivalent") {
                    expect(map.equivalentContentTo(YamlMap(emptyMap(), map.path))).toBe(false)
                }
            }

            context("comparing it to a scalar value") {
                it("indicates that they are not equivalent") {
                    expect(map.equivalentContentTo(YamlScalar("some content", map.path))).toBe(false)
                }
            }

            context("comparing it to a null value") {
                it("indicates that they are not equivalent") {
                    expect(map.equivalentContentTo(YamlNull(map.path))).toBe(false)
                }
            }

            context("comparing it to a list") {
                it("indicates that they are not equivalent") {
                    expect(map.equivalentContentTo(YamlList(emptyList(), map.path))).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            val helloKeyPath = YamlPath.root.withMapElementKey("hello", Location(1, 1))
            val helloValuePath = helloKeyPath.withMapElementValue(Location(2, 1))
            val alsoKeyPath = YamlPath.root.withMapElementKey("also", Location(3, 1))
            val alsoValuePath = alsoKeyPath.withMapElementValue(Location(4, 1))

            context("an empty map") {
                val map = YamlMap(emptyMap(), YamlPath.root)

                it("returns empty curly brackets") {
                    expect(map.contentToString()).toBe("{}")
                }
            }

            context("a map with a single entry") {
                val map = YamlMap(
                    mapOf(
                        YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath)
                    ),
                    YamlPath.root
                )

                it("returns that item surrounded by curly brackets") {
                    expect(map.contentToString()).toBe("{'hello': 'world'}")
                }
            }

            context("a map with multiple entries") {
                val map = YamlMap(
                    mapOf(
                        YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath),
                        YamlScalar("also", alsoKeyPath) to YamlScalar("thanks", alsoValuePath)
                    ),
                    YamlPath.root
                )

                it("returns all items separated by commas and surrounded by curly brackets") {
                    expect(map.contentToString()).toBe("{'hello': 'world', 'also': 'thanks'}")
                }
            }
        }

        describe("getting elements of the map") {
            val helloKeyPath = YamlPath.root.withMapElementKey("hello", Location(1, 1))
            val helloValuePath = helloKeyPath.withMapElementValue(Location(2, 1))
            val alsoKeyPath = YamlPath.root.withMapElementKey("also", Location(3, 1))
            val alsoValuePath = alsoKeyPath.withMapElementValue(Location(4, 1))

            val map = YamlMap(
                mapOf(
                    YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath),
                    YamlScalar("also", alsoKeyPath) to YamlScalar("something", alsoValuePath)
                ),
                YamlPath.root
            )

            context("the key is not in the map") {
                it("returns null") {
                    expect(map.get<YamlScalar>("something else")).toBe(null)
                }
            }

            context("the key is in the map") {
                it("returns the value for that key") {
                    expect(map.get<YamlScalar>("hello")).toBe(YamlScalar("world", helloValuePath))
                }
            }
        }

        describe("getting scalar elements of the map") {
            val helloKeyPath = YamlPath.root.withMapElementKey("hello", Location(1, 1))
            val helloValuePath = helloKeyPath.withMapElementValue(Location(2, 1))
            val alsoKeyPath = YamlPath.root.withMapElementKey("also", Location(3, 1))
            val alsoValuePath = alsoKeyPath.withMapElementValue(Location(4, 1))
            val alsoValueListEntryPath = alsoValuePath.withListEntry(0, Location(5, 1))

            val map = YamlMap(
                mapOf(
                    YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath),
                    YamlScalar("also", alsoKeyPath) to YamlList(listOf(YamlScalar("something", alsoValueListEntryPath)), alsoValuePath)
                ),
                YamlPath.root
            )

            context("the key is not in the map") {
                it("returns null") {
                    expect(map.getScalar("something else")).toBe(null)
                }
            }

            context("the key is in the map and has a scalar value") {
                it("returns the value for that key") {
                    expect(map.getScalar("hello")).toBe(YamlScalar("world", helloValuePath))
                }
            }

            context("the key is in the map but does not have a scalar value") {
                it("returns the value for that key") {
                    expect { map.getScalar("also") }.toThrow<IncorrectTypeException> {
                        message { toBe("Value for 'also' is not a scalar.") }
                        line { toBe(4) }
                        column { toBe(1) }
                        path { toBe(alsoValuePath) }
                    }
                }
            }
        }

        describe("replacing its path") {
            val originalPath = YamlPath.root
            val originalKey1Path = originalPath.withMapElementKey("key1", Location(4, 1))
            val originalKey1ValuePath = originalKey1Path.withMapElementValue(Location(4, 5))
            val originalKey2Path = originalPath.withMapElementKey("key2", Location(6, 1))
            val originalKey2ValuePath = originalKey2Path.withMapElementValue(Location(6, 7))

            val original = YamlMap(
                mapOf(
                    YamlScalar("key1", originalKey1Path) to YamlScalar("value", originalKey1ValuePath),
                    YamlScalar("key2", originalKey2Path) to YamlScalar("value", originalKey2ValuePath)
                ),
                originalPath
            )

            val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))
            val newKey1Path = newPath.withMapElementKey("key1", Location(4, 1))
            val newKey1ValuePath = newKey1Path.withMapElementValue(Location(4, 5))
            val newKey2Path = newPath.withMapElementKey("key2", Location(6, 1))
            val newKey2ValuePath = newKey2Path.withMapElementValue(Location(6, 7))

            val expected = YamlMap(
                mapOf(
                    YamlScalar("key1", newKey1Path) to YamlScalar("value", newKey1ValuePath),
                    YamlScalar("key2", newKey2Path) to YamlScalar("value", newKey2ValuePath)
                ),
                newPath
            )

            it("returns a new map node with the path for the node and its keys and values updated to the new path") {
                expect(original.withPath(newPath)).toBe(expected)
            }
        }

        describe("converting it to a string") {
            val path = YamlPath.root.withMapElementKey("test", Location(2, 1)).withMapElementValue(Location(2, 7))
            val keyPath = path.withMapElementKey("something", Location(3, 3))
            val valuePath = keyPath.withMapElementValue(Location(3, 7))
            val value = YamlMap(
                mapOf(
                    YamlScalar("something", keyPath) to YamlScalar("some value", valuePath)
                ),
                path
            )

            it("returns a human-readable description of itself") {
                expect(value.toString()).toBe(
                    """
                        map @ $path (size: 1)
                        - key:
                            scalar @ $keyPath : something
                          value:
                            scalar @ $valuePath : some value
                    """.trimIndent()
                )
            }
        }
    }
})
