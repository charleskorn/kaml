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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class YamlTaggedNodeTest : FunSpec({
    val tagged = YamlTaggedNode("tag", YamlScalar("test", YamlPath.root))
    val original = YamlTaggedNode("tag", YamlScalar("value", YamlPath.root))
    val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))
    val value = YamlTaggedNode("some tag", YamlScalar("some value", YamlPath.root.withListEntry(2, Location(3, 4))))
    val map = YamlTaggedNode("tag", YamlScalar("test", YamlPath.root))

    test("Tagged node should be equivalent to the same instance") {
        tagged.equivalentContentTo(tagged) shouldBe true
    }

    test("Tagged node should not be equivalent to a non-tagged node") {
        tagged.equivalentContentTo(YamlScalar("test", YamlPath.root)) shouldBe false
    }

    test("Tagged node should not be equivalent to a tagged node with different tag") {
        tagged.equivalentContentTo(YamlTaggedNode("tag2", YamlScalar("test", YamlPath.root))) shouldBe false
    }

    test("Tagged node should not be equivalent to a tagged node with different child node") {
        tagged.equivalentContentTo(YamlTaggedNode("tag", YamlScalar("test2", YamlPath.root))) shouldBe false
    }

    test("Converting tagged scalar content to human-readable string should return tag and child") {
        map.contentToString() shouldBe "!tag 'test'"
    }

    test("Replacing its path should return a tagged node with the inner node updated with the provided path") {
        original.withPath(newPath) shouldBe YamlTaggedNode("tag", YamlScalar("value", newPath))
    }

    test("Converting to string should return a human-readable description") {
        value.toString() shouldBe "tagged 'some tag': scalar @ ${value.path} : some value"
    }
})
