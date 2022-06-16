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

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class YamlNodeTest : DescribeSpec({
    describe("converting from YamlNode") {
        val path = YamlPath.root

        val cases = listOf(
            Triple("YamlScalar", YamlNode::yamlScalar, YamlScalar("test", path)),
            Triple("YamlNull", YamlNode::yamlNull, YamlNull(path)),
            Triple("YamlList", YamlNode::yamlList, YamlList(emptyList(), path)),
            Triple("YamlMap", YamlNode::yamlMap, YamlMap(emptyMap(), path)),
            Triple(
                "YamlTaggedNode", YamlNode::yamlTaggedNode,
                YamlTaggedNode("tag", YamlScalar("tagged_scalar", path))
            ),
        )

        cases.forEach { (fromType, _, value) ->
            cases.forEach { (toType, method, _) ->
                if (fromType == toType) {
                    it("successfully converts to $toType") {
                        shouldNotThrowAny { method(value) }
                    }
                } else {
                    it("throws when converting to $toType") {
                        val exception = shouldThrow<IncorrectTypeException> { method(value) }
                        exception.asClue {
                            it.message shouldBe "Element is not $toType"
                            it.path shouldBe path
                        }
                    }
                }
            }
        }
    }
})
