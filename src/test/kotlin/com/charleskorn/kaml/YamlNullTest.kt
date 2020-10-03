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

object YamlNullTest : Spek({
    describe("a YAML null value") {
        describe("testing equivalence") {
            val nullValue = YamlNull(YamlPath.root)

            context("comparing it to the same instance") {
                it("indicates that they are equivalent") {
                    expect(nullValue.equivalentContentTo(nullValue)).toBe(true)
                }
            }

            context("comparing it to another null value with the same path") {
                it("indicates that they are equivalent") {
                    expect(nullValue.equivalentContentTo(YamlNull(nullValue.path))).toBe(true)
                }
            }

            context("comparing it to another null with a different path") {
                val path = YamlPath.root.withListEntry(0, Location(2, 4))

                it("indicates that they are equivalent") {
                    expect(nullValue.equivalentContentTo(YamlNull(path))).toBe(true)
                }
            }

            context("comparing it to a scalar value") {
                it("indicates that they are not equivalent") {
                    expect(nullValue.equivalentContentTo(YamlScalar("some content", nullValue.path))).toBe(false)
                }
            }

            context("comparing it to a list") {
                it("indicates that they are not equivalent") {
                    expect(nullValue.equivalentContentTo(YamlList(emptyList(), nullValue.path))).toBe(false)
                }
            }

            context("comparing it to a map") {
                it("indicates that they are not equivalent") {
                    expect(nullValue.equivalentContentTo(YamlMap(emptyMap(), nullValue.path))).toBe(false)
                }
            }
        }

        describe("converting the content to a human-readable string") {
            it("always returns the value 'null'") {
                expect(YamlNull(YamlPath.root).contentToString()).toBe("null")
            }
        }

        describe("replacing its path") {
            val original = YamlNull(YamlPath.root)
            val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))

            it("returns a null value with the provided path") {
                expect(original.withPath(newPath)).toBe(YamlNull(newPath))
            }
        }

        describe("converting it to a string") {
            val path = YamlPath.root.withListEntry(2, Location(3, 4))
            val value = YamlNull(path)

            it("returns a human-readable description of itself") {
                expect(value.toString()).toBe("null @ $path")
            }
        }
    }
})
