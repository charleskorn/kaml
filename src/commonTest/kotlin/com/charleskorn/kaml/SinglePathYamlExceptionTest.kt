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

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SinglePathYamlExceptionTest : Spek({
    describe("a YAML exception") {
        describe("formatting it as a string") {
            val path = YamlPath.root.withMapElementKey("colours", Location(3, 4)).withMapElementValue(Location(4, 1)).withListEntry(2, Location(123, 456))
            val exception = SinglePathYamlException("Something went wrong", path)

            it("includes the class name, location information and message") {
                expect(exception.toString()).toBe("com.charleskorn.kaml.SinglePathYamlException at colours[2] on line 123, column 456: Something went wrong")
            }
        }
    }
})
