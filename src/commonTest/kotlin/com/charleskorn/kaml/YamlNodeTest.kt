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
import kotlin.reflect.KProperty1

class YamlNodeTest : FunSpec({
    data class TestCase(val name: String, val method: KProperty1<YamlNode, YamlNode>, val value: YamlNode)

    val path = YamlPath.root
    val testScalar = YamlScalar("test", path)
    val testNull = YamlNull(path)
    val testList = YamlList(emptyList(), path)
    val testMap = YamlMap(emptyMap(), path)
    val testTaggedNode = YamlTaggedNode("tag", YamlScalar("tagged_scalar", path))

    withData<TestCase>(
        { it.name },
        TestCase("scalar", YamlNode::yamlScalar, testScalar),
        TestCase("null", YamlNode::yamlNull, testNull),
        TestCase("list", YamlNode::yamlList, testList),
        TestCase("map", YamlNode::yamlMap, testMap),
        TestCase("tagged node", YamlNode::yamlTaggedNode, testTaggedNode),
    ) { testCase ->
        shouldNotThrowAny { testCase.method(testCase.value) }
        testCase.method(testCase.value) shouldBe testCase.value
    }

    withData<TestCase>(
        { it.name },
        TestCase("retrieving a scalar from a null", YamlNode::yamlScalar, testNull),
        TestCase("retrieving a scalar from a list", YamlNode::yamlScalar, testList),
        TestCase("retrieving a scalar from a map", YamlNode::yamlScalar, testMap),
        TestCase("retrieving a scalar from a tagged node", YamlNode::yamlScalar, testTaggedNode),
        TestCase("retrieving a null from a scalar", YamlNode::yamlNull, testScalar),
        TestCase("retrieving a null from a list", YamlNode::yamlNull, testList),
        TestCase("retrieving a null from a map", YamlNode::yamlNull, testMap),
        TestCase("retrieving a null from a tagged node", YamlNode::yamlNull, testTaggedNode),
        TestCase("retrieving a list from a scalar", YamlNode::yamlList, testScalar),
        TestCase("retrieving a list from a null", YamlNode::yamlList, testNull),
        TestCase("retrieving a list from a map", YamlNode::yamlList, testMap),
        TestCase("retrieving a list from a tagged node", YamlNode::yamlList, testTaggedNode),
        TestCase("retrieving a map from a scalar", YamlNode::yamlMap, testScalar),
        TestCase("retrieving a map from a null", YamlNode::yamlMap, testNull),
        TestCase("retrieving a map from a list", YamlNode::yamlMap, testList),
        TestCase("retrieving a map from a tagged node", YamlNode::yamlMap, testTaggedNode),
        TestCase("retrieving a tagged node from a scalar", YamlNode::yamlTaggedNode, testScalar),
        TestCase("retrieving a tagged node from a null", YamlNode::yamlTaggedNode, testNull),
        TestCase("retrieving a tagged node from a list", YamlNode::yamlTaggedNode, testList),
        TestCase("retrieving a tagged node from a map", YamlNode::yamlTaggedNode, testMap),
    ) { testCase ->
        val type = testCase.method.name.replaceFirstChar(Char::titlecase)
        val fromType = testCase.value::class.simpleName
        val exception = shouldThrow<IncorrectTypeException> { testCase.method(testCase.value) }
        exception.asClue {
            it.message shouldBe "Expected element to be $type but is $fromType"
            it.path shouldBe path
        }
    }
})
