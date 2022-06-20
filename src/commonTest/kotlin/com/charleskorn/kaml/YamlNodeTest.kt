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

        val testScalar = YamlScalar("test", path)
        val testNull = YamlNull(path)
        val testList = YamlList(emptyList(), path)
        val testMap = YamlMap(emptyMap(), path)
        val testTaggedNode = YamlTaggedNode("tag", YamlScalar("tagged_scalar", path))

        listOf(
            Triple("YamlScalar", YamlNode::yamlScalar, testScalar),
            Triple("YamlNull", YamlNode::yamlNull, testNull),
            Triple("YamlList", YamlNode::yamlList, testList),
            Triple("YamlMap", YamlNode::yamlMap, testMap),
            Triple("YamlTaggedNode", YamlNode::yamlTaggedNode, testTaggedNode),
        ).forEach { (type, method, value) ->
            it("successfully converts to $type") {
                shouldNotThrowAny { method(value) }
                method(value) shouldBe value
            }
        }

        listOf(
            Triple("YamlScalar", YamlNode::yamlScalar, testNull),
            Triple("YamlScalar", YamlNode::yamlScalar, testList),
            Triple("YamlScalar", YamlNode::yamlScalar, testMap),
            Triple("YamlScalar", YamlNode::yamlScalar, testTaggedNode),
            Triple("YamlNull", YamlNode::yamlNull, testScalar),
            Triple("YamlNull", YamlNode::yamlNull, testList),
            Triple("YamlNull", YamlNode::yamlNull, testMap),
            Triple("YamlNull", YamlNode::yamlNull, testTaggedNode),
            Triple("YamlList", YamlNode::yamlList, testScalar),
            Triple("YamlList", YamlNode::yamlList, testNull),
            Triple("YamlList", YamlNode::yamlList, testMap),
            Triple("YamlList", YamlNode::yamlList, testTaggedNode),
            Triple("YamlMap", YamlNode::yamlMap, testScalar),
            Triple("YamlMap", YamlNode::yamlMap, testNull),
            Triple("YamlMap", YamlNode::yamlMap, testList),
            Triple("YamlMap", YamlNode::yamlMap, testTaggedNode),
            Triple("YamlTaggedNode", YamlNode::yamlTaggedNode, testScalar),
            Triple("YamlTaggedNode", YamlNode::yamlTaggedNode, testNull),
            Triple("YamlTaggedNode", YamlNode::yamlTaggedNode, testList),
            Triple("YamlTaggedNode", YamlNode::yamlTaggedNode, testMap),
        ).forEach { (type, method, value) ->
            it("throws when converting from ${value::class} to $type") {
                val exception = shouldThrow<IncorrectTypeException> { method(value) }
                exception.asClue {
                    it.message shouldBe "Element is not $type"
                    it.path shouldBe path
                }
            }
        }
    }
})
