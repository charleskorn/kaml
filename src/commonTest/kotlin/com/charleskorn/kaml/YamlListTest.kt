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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class YamlListTest : FunSpec({

    val list = YamlList(
        listOf(
            YamlScalar("item 1", YamlPath.root.withListEntry(0, Location(4, 5))),
            YamlScalar("item 2", YamlPath.root.withListEntry(1, Location(6, 7))),
        ),
        YamlPath.root,
    )

    test("list equivalence with same instance") {
        list.equivalentContentTo(list) shouldBe true
    }

    test("list equivalence with same items but different path") {
        list.equivalentContentTo(YamlList(list.items, YamlPath.root.withMapElementValue(Location(5, 6)))) shouldBe true
    }

    test("list equivalence with same items in different order") {
        list.equivalentContentTo(YamlList(list.items.reversed(), list.path)) shouldBe false
    }

    test("list equivalence with different items") {
        list.equivalentContentTo(YamlList(emptyList(), list.path)) shouldBe false
    }

    test("list equivalence with scalar value") {
        list.equivalentContentTo(YamlScalar("some content", list.path)) shouldBe false
    }

    test("list equivalence with null value") {
        list.equivalentContentTo(YamlNull(list.path)) shouldBe false
    }

    test("list equivalence with map") {
        list.equivalentContentTo(YamlMap(emptyMap(), list.path)) shouldBe false
    }

    val firstItemPath = YamlPath.root.withListEntry(0, Location(4, 5))
    val secondItemPath = YamlPath.root.withListEntry(1, Location(6, 7))

    val listElements = YamlList(
        listOf(
            YamlScalar("item 1", firstItemPath),
            YamlScalar("item 2", secondItemPath),
        ),
        YamlPath.root,
    )

    test("getting element in bounds from list") {
        listElements[0] shouldBe YamlScalar("item 1", firstItemPath)
        listElements[1] shouldBe YamlScalar("item 2", secondItemPath)
    }

    test("getting element out of bounds from list") {
        shouldThrow<IndexOutOfBoundsException> { listElements[2] }
        shouldThrow<IndexOutOfBoundsException> { listElements[10] }
    }

    test("converting content of an empty list to a human-readable string") {
        val listEmpty = YamlList(emptyList(), YamlPath.root)
        listEmpty.contentToString() shouldBe "[]"
    }

    test("converting content of a list with a single entry to a human-readable string") {
        val listSingleEntry = YamlList(listOf(YamlScalar("hello", YamlPath.root.withListEntry(0, Location(1, 1)))), YamlPath.root)
        listSingleEntry.contentToString() shouldBe "['hello']"
    }

    test("converting content of a list with multiple entries to a human-readable string") {
        val listMultipleEntries = YamlList(
            listOf(
                YamlScalar("hello", YamlPath.root.withListEntry(0, Location(1, 1))),
                YamlScalar("world", YamlPath.root.withListEntry(1, Location(2, 1))),
            ),
            YamlPath.root,
        )
        listMultipleEntries.contentToString() shouldBe "['hello', 'world']"
    }

    test("replacing list's path") {
        val original = YamlList(
            listOf(
                YamlScalar("hello", YamlPath.root.withListEntry(0, Location(1, 1))),
                YamlScalar("world", YamlPath.root.withListEntry(1, Location(2, 1))),
            ),
            YamlPath.root,
        )

        val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))

        val expected = YamlList(
            listOf(
                YamlScalar("hello", newPath.withListEntry(0, Location(1, 1))),
                YamlScalar("world", newPath.withListEntry(1, Location(2, 1))),
            ),
            newPath,
        )

        original.withPath(newPath) shouldBe expected
    }

    test("converting list to a string") {
        val path = YamlPath.root.withMapElementKey("test", Location(2, 1)).withMapElementValue(Location(2, 7))
        val elementPath = path.withListEntry(0, Location(3, 3))
        val value = YamlList(listOf(YamlScalar("hello", elementPath)), path)

        value.toString() shouldBe
            """
                list @ $path (size: 1)
                - item 0:
                  scalar @ $elementPath : hello
            """.trimIndent()
    }
})
