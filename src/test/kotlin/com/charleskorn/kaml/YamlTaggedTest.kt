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

object YamlTaggedTest : Spek({
    describe("a YAML tagged node") {
        describe("testing equivalence") {
            val tagged = YamlTaggedNode(
                "tag",
                YamlScalar("test", Location(4, 1))
            )

            context("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    assert(tagged.equivalentContentTo(tagged)).toBe(true)
                }
            }

            context("comparing it to another non-tagged node") {
                it("indicates that they are not equivalent") {
                    assert(
                        tagged.equivalentContentTo(
                            YamlScalar("test", Location(4, 1))
                        )
                    ).toBe(false)
                }
            }

            context("comparing it to another tagged node with a different tag") {
                it("indicates that they are not equivalent") {
                    assert(
                        tagged.equivalentContentTo(
                            YamlTaggedNode(
                                "tag2",
                                YamlScalar("test", Location(4, 1))
                            )
                        )
                    ).toBe(false)
                }
            }

            context("comparing it to another tagged node with different child node") {
                it("indicates that they are not equivalent") {
                    assert(
                        tagged.equivalentContentTo(
                            YamlTaggedNode(
                                "tag",
                                YamlScalar("test2", Location(4, 1))
                            )
                        )
                    ).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            context("a tagged scalar") {
                val map = YamlTaggedNode("tag", YamlScalar("test", Location(4, 1)))

                it("returns tag and child") {
                    assert(map.contentToString()).toBe("!tag 'test'")
                }
            }
        }
    }
})
