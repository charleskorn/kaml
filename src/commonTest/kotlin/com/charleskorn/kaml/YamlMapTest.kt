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

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class YamlMapTest : FlatFunSpec({
    context("a YAML map") {
        context("creating an instance") {
            val mapPath = YamlPath.root
            val key1Path = mapPath.withMapElementKey("key1", Location(4, 1))
            val key1ValuePath = key1Path.withMapElementValue(Location(4, 5))
            val key2Path = mapPath.withMapElementKey("key2", Location(6, 1))
            val key2ValuePath = key2Path.withMapElementValue(Location(6, 7))

            context("creating an empty map") {
                test("does not throw an exception") {
                    shouldNotThrowAny { YamlMap(emptyMap(), mapPath) }
                }
            }

            context("creating a map with a single entry") {
                test("does not throw an exception") {
                    shouldNotThrowAny {
                        YamlMap(
                            mapOf(YamlScalar("key", key1Path) to YamlScalar("value", key1ValuePath)),
                            mapPath,
                        )
                    }
                }
            }

            context("creating a map with two entries, each with unique keys") {
                test("does not throw an exception") {
                    shouldNotThrowAny {
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value", key1ValuePath),
                                YamlScalar("key2", key2Path) to YamlScalar("value", key2ValuePath),
                            ),
                            mapPath,
                        )
                    }
                }
            }

            context("creating a map with two entries with the same key") {
                test("throws an appropriate exception") {
                    val exception = shouldThrow<DuplicateKeyException> {
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("value", key1ValuePath),
                                YamlScalar("key1", key2Path) to YamlScalar("value", key2ValuePath),
                            ),
                            mapPath,
                        )
                    }

                    exception.asClue {
                        it.message shouldBe "Duplicate key 'key1'. It was previously given at line 4, column 1."
                        it.line shouldBe 6
                        it.column shouldBe 1
                        it.path shouldBe key2Path
                        it.originalLocation shouldBe Location(4, 1)
                        it.originalPath shouldBe key1Path
                        it.duplicateLocation shouldBe Location(6, 1)
                        it.duplicatePath shouldBe key2Path
                        it.key shouldBe "'key1'"
                    }
                }
            }
        }

        context("testing equivalence") {
            val mapPath = YamlPath.root
            val key1Path = mapPath.withMapElementKey("key1", Location(4, 1))
            val key1ValuePath = key1Path.withMapElementValue(Location(4, 5))
            val key2Path = mapPath.withMapElementKey("key2", Location(6, 1))
            val key2ValuePath = key2Path.withMapElementValue(Location(6, 7))

            val map = YamlMap(
                mapOf(
                    YamlScalar("key1", key1Path) to YamlScalar("item 1", key1ValuePath),
                    YamlScalar("key2", key2Path) to YamlScalar("item 2", key2ValuePath),
                ),
                mapPath,
            )

            context("comparing it to the same instance") {
                test("indicates that they are equivalent") {
                    map.equivalentContentTo(map) shouldBe true
                }
            }

            context("comparing it to another map with the same items in the same order with a different path") {
                test("indicates that they are equivalent") {
                    map.equivalentContentTo(YamlMap(map.entries, YamlPath.root.withListEntry(0, Location(3, 4)))) shouldBe true
                }
            }

            context("comparing it to another map with the same items in a different order with the same path") {
                test("indicates that they are equivalent") {
                    map.equivalentContentTo(
                        YamlMap(
                            mapOf(
                                YamlScalar("key2", key1Path) to YamlScalar("item 2", key1ValuePath),
                                YamlScalar("key1", key2Path) to YamlScalar("item 1", key2ValuePath),
                            ),
                            mapPath,
                        ),
                    ) shouldBe true
                }
            }

            context("comparing it to another map with different keys with the same path") {
                test("indicates that they are not equivalent") {
                    map.equivalentContentTo(
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("item 1", key1ValuePath),
                                YamlScalar("key3", key2Path) to YamlScalar("item 2", key2ValuePath),
                            ),
                            mapPath,
                        ),
                    ) shouldBe false
                }
            }

            context("comparing it to another map with different values with the same path") {
                test("indicates that they are not equivalent") {
                    map.equivalentContentTo(
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", key1Path) to YamlScalar("item 1", key1ValuePath),
                                YamlScalar("key2", key2Path) to YamlScalar("item 3", key2ValuePath),
                            ),
                            mapPath,
                        ),
                    ) shouldBe false
                }
            }

            context("comparing it to another map with different items with the same path") {
                test("indicates that they are not equivalent") {
                    map.equivalentContentTo(YamlMap(emptyMap(), map.path)) shouldBe false
                }
            }

            context("comparing it to a scalar value") {
                test("indicates that they are not equivalent") {
                    map.equivalentContentTo(YamlScalar("some content", map.path)) shouldBe false
                }
            }

            context("comparing it to a null value") {
                test("indicates that they are not equivalent") {
                    map.equivalentContentTo(YamlNull(map.path)) shouldBe false
                }
            }

            context("comparing it to a list") {
                test("indicates that they are not equivalent") {
                    map.equivalentContentTo(YamlList(emptyList(), map.path)) shouldBe false
                }
            }
        }

        context("converting the content to a human-readable string") {
            val helloKeyPath = YamlPath.root.withMapElementKey("hello", Location(1, 1))
            val helloValuePath = helloKeyPath.withMapElementValue(Location(2, 1))
            val alsoKeyPath = YamlPath.root.withMapElementKey("also", Location(3, 1))
            val alsoValuePath = alsoKeyPath.withMapElementValue(Location(4, 1))

            context("an empty map") {
                val map = YamlMap(emptyMap(), YamlPath.root)

                test("returns empty curly brackets") {
                    map.contentToString() shouldBe "{}"
                }
            }

            context("a map with a single entry") {
                val map = YamlMap(
                    mapOf(
                        YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath),
                    ),
                    YamlPath.root,
                )

                test("returns that item surrounded by curly brackets") {
                    map.contentToString() shouldBe "{'hello': 'world'}"
                }
            }

            context("a map with multiple entries") {
                val map = YamlMap(
                    mapOf(
                        YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath),
                        YamlScalar("also", alsoKeyPath) to YamlScalar("thanks", alsoValuePath),
                    ),
                    YamlPath.root,
                )

                test("returns all items separated by commas and surrounded by curly brackets") {
                    map.contentToString() shouldBe "{'hello': 'world', 'also': 'thanks'}"
                }
            }
        }

        context("getting elements of the map") {
            val helloKeyPath = YamlPath.root.withMapElementKey("hello", Location(1, 1))
            val helloValuePath = helloKeyPath.withMapElementValue(Location(2, 1))
            val alsoKeyPath = YamlPath.root.withMapElementKey("also", Location(3, 1))
            val alsoValuePath = alsoKeyPath.withMapElementValue(Location(4, 1))

            val map = YamlMap(
                mapOf(
                    YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath),
                    YamlScalar("also", alsoKeyPath) to YamlScalar("something", alsoValuePath),
                ),
                YamlPath.root,
            )

            context("the key is not in the map") {
                test("returns null") {
                    map.get<YamlScalar>("something else") shouldBe null
                }
            }

            context("the key is in the map") {
                test("returns the value for that key") {
                    map.get<YamlScalar>("hello") shouldBe YamlScalar("world", helloValuePath)
                }
                test("throws IncorrectTypeException with clear message when the type mismatches") {
                    val exception = shouldThrow<IncorrectTypeException> {
                        map.get<YamlList>("hello")
                    }

                    exception.asClue {
                        it.message shouldBe "Expected element to be YamlList but is YamlScalar"
                        it.line shouldBe 2
                        it.column shouldBe 1
                        it.path shouldBe helloValuePath
                    }
                }
            }
        }

        context("getting scalar elements of the map") {
            val helloKeyPath = YamlPath.root.withMapElementKey("hello", Location(1, 1))
            val helloValuePath = helloKeyPath.withMapElementValue(Location(2, 1))
            val alsoKeyPath = YamlPath.root.withMapElementKey("also", Location(3, 1))
            val alsoValuePath = alsoKeyPath.withMapElementValue(Location(4, 1))
            val alsoValueListEntryPath = alsoValuePath.withListEntry(0, Location(5, 1))

            val map = YamlMap(
                mapOf(
                    YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath),
                    YamlScalar("also", alsoKeyPath) to YamlList(listOf(YamlScalar("something", alsoValueListEntryPath)), alsoValuePath),
                ),
                YamlPath.root,
            )

            context("the key is not in the map") {
                test("returns null") {
                    map.getScalar("something else") shouldBe null
                }
            }

            context("the key is in the map and has a scalar value") {
                test("returns the value for that key") {
                    map.getScalar("hello") shouldBe YamlScalar("world", helloValuePath)
                }
            }

            context("the key is in the map but does not have a scalar value") {
                test("returns the value for that key") {
                    val exception = shouldThrow<IncorrectTypeException> { map.getScalar("also") }

                    exception.asClue {
                        it.message shouldBe "Value for 'also' is not a scalar."
                        it.line shouldBe 4
                        it.column shouldBe 1
                        it.path shouldBe alsoValuePath
                    }
                }
            }
        }

        context("getting keys of the map") {
            val helloKeyPath = YamlPath.root.withMapElementKey("hello", Location(1, 1))
            val helloValuePath = helloKeyPath.withMapElementValue(Location(2, 1))
            val alsoKeyPath = YamlPath.root.withMapElementKey("also", Location(3, 1))
            val alsoValuePath = alsoKeyPath.withMapElementValue(Location(4, 1))

            val map = YamlMap(
                mapOf(
                    YamlScalar("hello", helloKeyPath) to YamlScalar("world", helloValuePath),
                    YamlScalar("also", alsoKeyPath) to YamlScalar("something", alsoValuePath),
                ),
                YamlPath.root,
            )

            context("the key is not in the map") {
                test("returns null") {
                    map.getKey("something else") shouldBe null
                }
            }

            context("the key is in the map") {
                test("returns the node for that key") {
                    map.getKey("hello") shouldBe YamlScalar("hello", helloKeyPath)
                }
            }
        }

        context("replacing its path") {
            val originalPath = YamlPath.root
            val originalKey1Path = originalPath.withMapElementKey("key1", Location(4, 1))
            val originalKey1ValuePath = originalKey1Path.withMapElementValue(Location(4, 5))
            val originalKey2Path = originalPath.withMapElementKey("key2", Location(6, 1))
            val originalKey2ValuePath = originalKey2Path.withMapElementValue(Location(6, 7))

            val original = YamlMap(
                mapOf(
                    YamlScalar("key1", originalKey1Path) to YamlScalar("value", originalKey1ValuePath),
                    YamlScalar("key2", originalKey2Path) to YamlScalar("value", originalKey2ValuePath),
                ),
                originalPath,
            )

            val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))
            val newKey1Path = newPath.withMapElementKey("key1", Location(4, 1))
            val newKey1ValuePath = newKey1Path.withMapElementValue(Location(4, 5))
            val newKey2Path = newPath.withMapElementKey("key2", Location(6, 1))
            val newKey2ValuePath = newKey2Path.withMapElementValue(Location(6, 7))

            val expected = YamlMap(
                mapOf(
                    YamlScalar("key1", newKey1Path) to YamlScalar("value", newKey1ValuePath),
                    YamlScalar("key2", newKey2Path) to YamlScalar("value", newKey2ValuePath),
                ),
                newPath,
            )

            test("returns a new map node with the path for the node and its keys and values updated to the new path") {
                original.withPath(newPath) shouldBe expected
            }
        }

        context("converting it to a string") {
            val path = YamlPath.root.withMapElementKey("test", Location(2, 1)).withMapElementValue(Location(2, 7))
            val keyPath = path.withMapElementKey("something", Location(3, 3))
            val valuePath = keyPath.withMapElementValue(Location(3, 7))
            val value = YamlMap(
                mapOf(
                    YamlScalar("something", keyPath) to YamlScalar("some value", valuePath),
                ),
                path,
            )

            test("returns a human-readable description of itself") {
                value.toString() shouldBe
                    """
                        map @ $path (size: 1)
                        - key:
                            scalar @ $keyPath : something
                          value:
                            scalar @ $valuePath : some value
                    """.trimIndent()
            }
        }
    }
})
