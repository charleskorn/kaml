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

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlTaggedNodeTest : Spek({
    describe("a YAML tagged node") {
        describe("testing equivalence") {
            val tagged = YamlTaggedNode(
                "tag",
                YamlScalar("test", YamlPath.root)
            )

            context("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    expect(tagged.equivalentContentTo(tagged)).toBe(true)
                }
            }

            context("comparing it to another non-tagged node") {
                it("indicates that they are not equivalent") {
                    expect(
                        tagged.equivalentContentTo(
                            YamlScalar("test", YamlPath.root)
                        )
                    ).toBe(false)
                }
            }

            context("comparing it to another tagged node with a different tag") {
                it("indicates that they are not equivalent") {
                    expect(
                        tagged.equivalentContentTo(
                            YamlTaggedNode(
                                "tag2",
                                YamlScalar("test", YamlPath.root)
                            )
                        )
                    ).toBe(false)
                }
            }

            context("comparing it to another tagged node with different child node") {
                it("indicates that they are not equivalent") {
                    expect(
                        tagged.equivalentContentTo(
                            YamlTaggedNode(
                                "tag",
                                YamlScalar("test2", YamlPath.root)
                            )
                        )
                    ).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            context("a tagged scalar") {
                val map = YamlTaggedNode("tag", YamlScalar("test", YamlPath.root))

                it("returns tag and child") {
                    expect(map.contentToString()).toBe("!tag 'test'")
                }
            }
        }

        describe("replacing its path") {
            val original = YamlTaggedNode("tag", YamlScalar("value", YamlPath.root))
            val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))

            it("returns a tagged node with the inner node updated with the provided path") {
                expect(original.withPath(newPath)).toBe(YamlTaggedNode("tag", YamlScalar("value", newPath)))
            }
        }

        describe("converting it to a string") {
            val path = YamlPath.root.withListEntry(2, Location(3, 4))
            val value = YamlTaggedNode("some tag", YamlScalar("some value", path))

            it("returns a human-readable description of itself") {
                expect(value.toString()).toBe("tagged 'some tag': scalar @ $path : some value")
            }
        }
    }
})
