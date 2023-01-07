/*

   Copyright 2018-2021 Charles Korn.

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

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class YamlTaggedNodeTest : DescribeSpec({
    describe("a YAML tagged node") {
        describe("testing equivalence") {
            val tagged = YamlTaggedNode(
                "tag",
                YamlScalar("test", YamlPath.root),
            )

            context("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    tagged.equivalentContentTo(tagged) shouldBe true
                }
            }

            context("comparing it to another non-tagged node") {
                it("indicates that they are not equivalent") {
                    tagged.equivalentContentTo(
                        YamlScalar("test", YamlPath.root),
                    ) shouldBe false
                }
            }

            context("comparing it to another tagged node with a different tag") {
                it("indicates that they are not equivalent") {
                    tagged.equivalentContentTo(
                        YamlTaggedNode(
                            "tag2",
                            YamlScalar("test", YamlPath.root),
                        ),
                    ) shouldBe false
                }
            }

            context("comparing it to another tagged node with different child node") {
                it("indicates that they are not equivalent") {
                    tagged.equivalentContentTo(
                        YamlTaggedNode(
                            "tag",
                            YamlScalar("test2", YamlPath.root),
                        ),
                    ) shouldBe false
                }
            }
        }

        describe("converting the content to a human-readable string") {
            context("a tagged scalar") {
                val map = YamlTaggedNode("tag", YamlScalar("test", YamlPath.root))

                it("returns tag and child") {
                    map.contentToString() shouldBe "!tag 'test'"
                }
            }
        }

        describe("replacing its path") {
            val original = YamlTaggedNode("tag", YamlScalar("value", YamlPath.root))
            val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))

            it("returns a tagged node with the inner node updated with the provided path") {
                original.withPath(newPath) shouldBe YamlTaggedNode("tag", YamlScalar("value", newPath))
            }
        }

        describe("converting it to a string") {
            val path = YamlPath.root.withListEntry(2, Location(3, 4))
            val value = YamlTaggedNode("some tag", YamlScalar("some value", path))

            it("returns a human-readable description of itself") {
                value.toString() shouldBe "tagged 'some tag': scalar @ $path : some value"
            }
        }
    }
})
