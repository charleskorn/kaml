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

package com.charleskorn.kaml.testobjects

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlTaggedNode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class SimpleStructure(
    val name: String,
)

@Serializable
data class Team(
    val members: List<String>,
)

@Serializable
data class NestedObjects(
    val firstPerson: SimpleStructure,
    val secondPerson: SimpleStructure,
)

@Serializable
enum class TestEnum {
    Value1,
    Value2,
}

@Serializable
enum class TestEnumWithExplicitNames {
    @SerialName("A")
    Alpha,

    @SerialName("B")
    Beta,

    @SerialName("With space")
    WithSpace,
}

@Serializable
data class TestClassWithNestedNode(
    val text: String,
    val node: YamlNode,
)

@Serializable
data class TestClassWithNestedScalar(
    val text: String,
    val node: YamlScalar,
)

@Serializable
data class TestClassWithNestedNull(
    val text: String,
    val node: YamlNull,
)

@Serializable
data class TestClassWithNestedMap(
    val text: String,
    val node: YamlMap,
)

@Serializable
data class TestClassWithNestedList(
    val text: String,
    val node: YamlList,
)

@Serializable
data class TestClassWithNestedTaggedNode(
    val text: String,
    val node: YamlTaggedNode,
)

@Serializable
@JvmInline
value class TestInline<out T>(val value: T)

@Serializable
sealed interface TestSealedInterface

@Serializable
data class TestSealedImpl(val value: String) : TestSealedInterface
