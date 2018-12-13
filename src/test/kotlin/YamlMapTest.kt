import ch.tutteli.atrium.api.cc.en_GB.message
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.domain.builders.AssertImpl
import ch.tutteli.atrium.domain.creating.throwable.thrown.ThrowableThrown
import ch.tutteli.atrium.reporting.translating.Untranslatable
import ch.tutteli.atrium.verbs.assert
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object YamlMapTest : Spek({
    describe("a YAML map") {
        describe("creating an instance") {
            on("creating an empty map") {
                it("does not throw an exception") {
                    assert({ YamlMap(emptyMap(), Location(1, 1)) }).toNotThrow()
                }
            }

            on("creating a map with a single entry") {
                it("does not throw an exception") {
                    assert({
                        YamlMap(
                            mapOf(YamlScalar("key", Location(1, 1)) to YamlScalar("value", Location(2, 1))),
                            Location(1, 1)
                        )
                    }).toNotThrow()
                }
            }

            on("creating a map with two entries, each with unique keys") {
                it("does not throw an exception") {
                    assert({
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 1)) to YamlScalar("value", Location(2, 1)),
                                YamlScalar("key2", Location(3, 1)) to YamlScalar("value", Location(4, 1))
                            ),
                            Location(1, 1)
                        )
                    }).toNotThrow()
                }
            }

            on("creating a map with two entries with the same key") {
                it("throws an appropriate exception") {
                    assert({
                        YamlMap(
                            mapOf(
                                YamlScalar("key1", Location(1, 1)) to YamlScalar("value", Location(2, 1)),
                                YamlScalar("key1", Location(3, 1)) to YamlScalar("value", Location(4, 1))
                            ),
                            Location(1, 1)
                        )
                    }).toThrow<YamlException> {
                        message { toBe("Duplicate key 'key1'. It was previously given at line 1, column 1.") }
                        line { toBe(3) }
                        column { toBe(1) }
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

            on("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    assert(map.equivalentContentTo(map)).toBe(true)
                }
            }

            on("comparing it to another map with the same items in the same order in a different location") {
                it("indicates that they are equivalent") {
                    assert(map.equivalentContentTo(YamlMap(map.entries, Location(5, 6)))).toBe(true)
                }
            }

            on("comparing it to another map with the same items in a different order in the same location") {
                it("indicates that they are equivalent") {
                    assert(
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

            on("comparing it to another map with different keys in the same location") {
                it("indicates that they are not equivalent") {
                    assert(
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

            on("comparing it to another map with different values in the same location") {
                it("indicates that they are not equivalent") {
                    assert(
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

            on("comparing it to another map with different items in the same location") {
                it("indicates that they are not equivalent") {
                    assert(map.equivalentContentTo(YamlMap(emptyMap(), Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a scalar value") {
                it("indicates that they are not equivalent") {
                    assert(map.equivalentContentTo(YamlScalar("some content", Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a null value") {
                it("indicates that they are not equivalent") {
                    assert(map.equivalentContentTo(YamlNull(Location(2, 3)))).toBe(false)
                }
            }

            on("comparing it to a list") {
                it("indicates that they are not equivalent") {
                    assert(map.equivalentContentTo(YamlList(emptyList(), Location(2, 3)))).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            given("an empty map") {
                val map = YamlMap(emptyMap(), Location(1, 1))

                it("returns empty curly brackets") {
                    assert(map.contentToString()).toBe("{}")
                }
            }

            given("a map with a single entry") {
                val map = YamlMap(mapOf(
                    YamlScalar("hello", Location(1, 1)) to YamlScalar("world", Location(2, 1))
                ), Location(1, 1))

                it("returns that item surrounded by curly brackets") {
                    assert(map.contentToString()).toBe("{'hello': 'world'}")
                }
            }

            given("a map with multiple entries") {
                val map = YamlMap(mapOf(
                    YamlScalar("hello", Location(1, 1)) to YamlScalar("world", Location(2, 1)),
                    YamlScalar("also", Location(1, 1)) to YamlScalar("thanks", Location(2, 1))
                ), Location(1, 1))

                it("returns all items separated by commas and surrounded by curly brackets") {
                    assert(map.contentToString()).toBe("{'hello': 'world', 'also': 'thanks'}")
                }
            }
        }
    }
})

// FIXME: This is a hack, pending further discussion at https://kotlinlang.slack.com/archives/C887ZKGCQ/p1544305394000500
fun ThrowableThrown.Builder.toNotThrow() = act()

