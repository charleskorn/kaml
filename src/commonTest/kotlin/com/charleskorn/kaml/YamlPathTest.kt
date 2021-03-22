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

object YamlPathTest : Spek({
    describe("a YAML path") {
        describe("creating a path") {
            describe("given an empty list of segments") {
                it("throws an exception") {
                    expect { YamlPath(emptyList()) }
                        .toThrow<IllegalArgumentException> {
                            message { toBe("Path must contain at least one segment.") }
                        }
                }
            }

            describe("given a list of segments where the first element is the root element") {
                it("does not throw an exception") {
                    expect { YamlPath(YamlPathSegment.Root) }.notToThrow()
                }
            }

            describe("given a list of segments where the first element is an alias definition") {
                it("does not throw an exception") {
                    expect { YamlPath(YamlPathSegment.AliasDefinition("blah", Location(2, 3))) }.notToThrow()
                }
            }

            describe("given a list of segments where the first element is not the root element or an alias definition") {
                it("throws an exception") {
                    expect { YamlPath(YamlPathSegment.Error(Location(1, 2))) }
                        .toThrow<IllegalArgumentException> {
                            message { toBe("First element of path must be root segment or alias definition.") }
                        }
                }
            }

            describe("given a list of segments where the root element appears but not as the first element") {
                it("throws an exception") {
                    expect { YamlPath(YamlPathSegment.Error(Location(1, 2)), YamlPathSegment.Root) }
                        .toThrow<IllegalArgumentException> {
                            message { toBe("First element of path must be root segment or alias definition.") }
                        }
                }
            }

            describe("given a list of segments where the root element appears multiple times") {
                it("throws an exception") {
                    expect { YamlPath(YamlPathSegment.Root, YamlPathSegment.Error(Location(1, 2)), YamlPathSegment.Root) }
                        .toThrow<IllegalArgumentException> {
                            message { toBe("Root segment can only be first element of path.") }
                        }
                }
            }
        }

        describe("getting the end location of a path") {
            describe("given a path with just the root element") {
                val path = YamlPath(YamlPathSegment.Root)

                it("returns the first character of the document") {
                    expect(path.endLocation).toBe(Location(1, 1))
                }
            }

            describe("given a path with multiple elements") {
                val path = YamlPath(
                    YamlPathSegment.Root,
                    YamlPathSegment.ListEntry(2, Location(3, 4)),
                    YamlPathSegment.MapElementKey("something", Location(5, 6))
                )

                it("returns the location of the last element of the path") {
                    expect(path.endLocation).toBe(Location(5, 6))
                }
            }
        }

        describe("converting a path to a string suitable for display to a user") {
            describe("given a path with just the root element") {
                val path = YamlPath.root

                it("returns a description of the root element") {
                    expect(path.toHumanReadableString()).toBe("<root>")
                }
            }

            describe("given a path with an error after the root element") {
                val path = YamlPath.root
                    .withError(Location(2, 3))

                it("returns a description of the root element") {
                    expect(path.toHumanReadableString()).toBe("<root>")
                }
            }

            describe("given a path with an error on a non-root element") {
                val path = YamlPath.root
                    .withListEntry(2, Location(2, 3))
                    .withError(Location(2, 3))

                it("returns a description of the parent of the error") {
                    expect(path.toHumanReadableString()).toBe("[2]")
                }
            }

            describe("given a path with a list entry after the root element") {
                val path = YamlPath.root
                    .withListEntry(2, Location(2, 3))

                it("returns a description of the list entry") {
                    expect(path.toHumanReadableString()).toBe("[2]")
                }
            }

            describe("given a path with a map key after the root element") {
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(2, 3))

                it("returns a description of the map key") {
                    expect(path.toHumanReadableString()).toBe("colour")
                }
            }

            describe("given a path with a map key followed by its value") {
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(2, 3))
                    .withMapElementValue(Location(2, 11))

                it("returns a description of the map key") {
                    expect(path.toHumanReadableString()).toBe("colour")
                }
            }

            describe("given a path for a nested map's key") {
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(2, 3))
                    .withMapElementValue(Location(2, 11))
                    .withMapElementKey("brightness", Location(3, 5))

                it("returns a description of the nested map key") {
                    expect(path.toHumanReadableString()).toBe("colour.brightness")
                }
            }

            describe("given a path for a nested list entry") {
                val path = YamlPath.root
                    .withListEntry(1, Location(2, 3))
                    .withListEntry(4, Location(3, 5))

                it("returns a description of the nested list entry") {
                    expect(path.toHumanReadableString()).toBe("[1][4]")
                }
            }

            describe("given a path for a list nested within an object") {
                val path = YamlPath.root
                    .withMapElementKey("colours", Location(2, 3))
                    .withListEntry(4, Location(3, 5))

                it("returns a description of the nested list entry") {
                    expect(path.toHumanReadableString()).toBe("colours[4]")
                }
            }

            describe("given a path for an object key nested within a list") {
                val path = YamlPath.root
                    .withListEntry(1, Location(2, 3))
                    .withMapElementKey("colour", Location(3, 5))

                it("returns a description of the nested object key") {
                    expect(path.toHumanReadableString()).toBe("[1].colour")
                }
            }

            describe("given a path for a reference to an alias") {
                val path = YamlPath.root
                    .withAliasReference("blue", Location(2, 3))

                it("returns a description of the alias reference") {
                    expect(path.toHumanReadableString()).toBe("->&blue")
                }
            }

            describe("given a path for a reference to an alias nested within an object") {
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(2, 3))
                    .withAliasReference("blue", Location(3, 7))

                it("returns a description of the nested alias reference") {
                    expect(path.toHumanReadableString()).toBe("colour->&blue")
                }
            }

            describe("given a path for a reference to an alias nested within a list") {
                val path = YamlPath.root
                    .withMapElementKey("colours", Location(2, 3))
                    .withListEntry(4, Location(3, 5))
                    .withAliasReference("blue", Location(3, 7))

                it("returns a description of the nested alias reference") {
                    expect(path.toHumanReadableString()).toBe("colours[4]->&blue")
                }
            }

            describe("given a path for a reference to a resolved alias") {
                val path = YamlPath.root
                    .withAliasReference("blue", Location(2, 3))
                    .withAliasDefinition("blue", Location(1, 2))

                it("returns a description of the alias reference") {
                    expect(path.toHumanReadableString()).toBe("->&blue")
                }
            }

            describe("given a path for a reference to a resolved alias map's element") {
                val path = YamlPath.root
                    .withAliasReference("blue", Location(2, 3))
                    .withAliasDefinition("blue", Location(1, 2))
                    .withMapElementKey("saturation", Location(1, 5))

                it("returns a description of the element key") {
                    expect(path.toHumanReadableString()).toBe("->&blue.saturation")
                }
            }

            describe("given a path for a reference to a resolved alias list's element") {
                val path = YamlPath.root
                    .withAliasReference("blue", Location(2, 3))
                    .withAliasDefinition("blue", Location(1, 2))
                    .withListEntry(3, Location(1, 5))

                it("returns a description of the element key") {
                    expect(path.toHumanReadableString()).toBe("->&blue[3]")
                }
            }

            describe("given a path for a reference to an inline merged object") {
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(1, 3))
                    .withMerge(Location(4, 5))

                it("returns a description of the object") {
                    expect(path.toHumanReadableString()).toBe("colour>>(merged)")
                }
            }

            describe("given a path for a reference to an inline merged object that was part of a list of objects to merge") {
                // eg. << : [ { x: 123 }, { y: 456 } ]
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(1, 3))
                    .withMerge(Location(4, 5))
                    .withListEntry(1, Location(4, 10))

                it("returns a description of the object") {
                    expect(path.toHumanReadableString()).toBe("colour>>(merged entry 1)")
                }
            }

            describe("given a path for a reference to an inline merged object's key") {
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(1, 3))
                    .withMerge(Location(4, 5))
                    .withMapElementKey("saturation", Location(4, 7))

                it("returns a description of the element key") {
                    expect(path.toHumanReadableString()).toBe("colour>>(merged).saturation")
                }
            }

            describe("given a path for a reference to the key of a merged object from an alias") {
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(1, 3))
                    .withMerge(Location(4, 5))
                    .withAliasReference("blue", Location(4, 5))
                    .withAliasDefinition("blue", Location(10, 3))
                    .withMapElementKey("saturation", Location(11, 5))

                it("returns a description of the element key") {
                    expect(path.toHumanReadableString()).toBe("colour>>(merged &blue).saturation")
                }
            }

            describe("given a path for a reference to the key of a merged object from an alias that was part of a list of objects to merge") {
                // eg. << : [ &blue, &green ]
                val path = YamlPath.root
                    .withMapElementKey("colour", Location(1, 3))
                    .withMerge(Location(4, 5))
                    .withListEntry(1, Location(4, 10))
                    .withAliasReference("green", Location(4, 10))
                    .withAliasDefinition("green", Location(10, 3))
                    .withMapElementKey("saturation", Location(11, 5))

                it("returns a description of the element key") {
                    expect(path.toHumanReadableString()).toBe("colour>>(merged entry 1 &green).saturation")
                }
            }
        }
    }
})
