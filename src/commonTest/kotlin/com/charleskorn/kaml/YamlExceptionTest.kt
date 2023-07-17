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

class YamlExceptionTest : FunSpec({
    test("Formatting a YAML exception as a string") {
        val path = YamlPath.root
            .withMapElementKey("colours", Location(3, 4))
            .withMapElementValue(Location(4, 1))
            .withListEntry(2, Location(123, 456))
        val exception = YamlException("Something went wrong", path)


        exception.toString() shouldBe "YamlException at colours[2] on line 123, column 456: Something went wrong"

    }
})
