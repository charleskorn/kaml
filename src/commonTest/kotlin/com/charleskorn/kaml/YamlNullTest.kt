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

class YamlNullTest : FunSpec({
    val nullValue = YamlNull(YamlPath.root)
    val path = YamlPath.root.withListEntry(0, Location(2, 4))
    val original = YamlNull(YamlPath.root)
    val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))
    val value = YamlNull(path)

    test("Null value should be equivalent to same instance") {
        nullValue.equivalentContentTo(nullValue) shouldBe true
    }

    test("Null value should be equivalent to another null value with same path") {
        nullValue.equivalentContentTo(YamlNull(nullValue.path)) shouldBe true
    }

    test("Null value should be equivalent to another null value with different path") {
        nullValue.equivalentContentTo(YamlNull(path)) shouldBe true
    }

    test("Null value should not be equivalent to a scalar value") {
        nullValue.equivalentContentTo(YamlScalar("some content", nullValue.path)) shouldBe false
    }

    test("Null value should not be equivalent to a list") {
        nullValue.equivalentContentTo(YamlList(emptyList(), nullValue.path)) shouldBe false
    }

    test("Null value should not be equivalent to a map") {
        nullValue.equivalentContentTo(YamlMap(emptyMap(), nullValue.path)) shouldBe false
    }

    test("Converting content to human-readable string should return 'null'") {
        YamlNull(YamlPath.root).contentToString() shouldBe "null"
    }

    test("Replacing its path should return null value with the provided path") {
        original.withPath(newPath) shouldBe YamlNull(newPath)
    }

    test("Converting to string should return a human-readable description") {
        value.toString() shouldBe "null @ $path"
    }
})
