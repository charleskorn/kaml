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

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class YamlNodeTest : FunSpec({
    val path = YamlPath.root
    val testScalar = YamlScalar("test", path)
    val testNull = YamlNull(path)
    val testList = YamlList(emptyList(), path)
    val testMap = YamlMap(emptyMap(), path)
    val testTaggedNode = YamlTaggedNode("tag", YamlScalar("tagged_scalar", path))

    withData(
        YamlNode::yamlScalar to testScalar,
        YamlNode::yamlNull to testNull,
        YamlNode::yamlList to testList,
        YamlNode::yamlMap to testMap,
        YamlNode::yamlTaggedNode to testTaggedNode,
    ) { (method, value) ->
        shouldNotThrowAny { method(value) }
        method(value) shouldBe value
    }

    withData(
        YamlNode::yamlScalar to testNull,
        YamlNode::yamlScalar to testList,
        YamlNode::yamlScalar to testMap,
        YamlNode::yamlScalar to testTaggedNode,
        YamlNode::yamlNull to testScalar,
        YamlNode::yamlNull to testList,
        YamlNode::yamlNull to testMap,
        YamlNode::yamlNull to testTaggedNode,
        YamlNode::yamlList to testScalar,
        YamlNode::yamlList to testNull,
        YamlNode::yamlList to testMap,
        YamlNode::yamlList to testTaggedNode,
        YamlNode::yamlMap to testScalar,
        YamlNode::yamlMap to testNull,
        YamlNode::yamlMap to testList,
        YamlNode::yamlMap to testTaggedNode,
        YamlNode::yamlTaggedNode to testScalar,
        YamlNode::yamlTaggedNode to testNull,
        YamlNode::yamlTaggedNode to testList,
        YamlNode::yamlTaggedNode to testMap,
    ) { (method, value) ->
        val type = method.name.replaceFirstChar(Char::titlecase)
        val fromType = value::class.simpleName
        val exception = shouldThrow<IncorrectTypeException> { method(value) }
        exception.asClue {
            it.message shouldBe "Expected element to be $type but is $fromType"
            it.path shouldBe path
        }
    }
})
