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

import com.charleskorn.kaml.YamlPathSegment.MapElementKey
import com.charleskorn.kaml.YamlPathSegment.MapElementValue
import com.charleskorn.kaml.YamlPathSegment.Root
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.scopes.DescribeSpecContainerScope
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Serializable
data class YamlNodeWrapper(val yaml: YamlNode)

@Serializable
data class YamlMapWrapper(val yaml: YamlMap)

@Serializable
data class YamlListWrapper(val yaml: YamlList)

@Serializable
data class YamlTaggedNodeWrapper(val yaml: YamlTaggedNode)

@Serializable
data class YamlScalarWrapper(val yaml: YamlScalar)

@Serializable
data class YamlNullWrapper(val yaml: YamlNull)

private suspend inline fun <reified T : Any> DescribeSpecContainerScope.roundTrip(name: String, input: T, expectedEncoding: String) {
    describe(name) {
        val encoded = Yaml.default.encodeToString(input)
        val decoded = Yaml.default.decodeFromString<T>(encoded)

        it("correctly serializes") {
            encoded shouldBe expectedEncoding
        }

        it("correctly deserializes") {
            decoded shouldBe input
        }
    }
}

private val path = YamlPath(
    Root,
    MapElementKey("yaml", Location(1, 1)),
    MapElementValue(Location(1, 7)),
)

class JvmYamlNodeSerializationTest : DescribeSpec({
    describe("yaml node serializers") {
        roundTrip(
            name = "wrapped object",
            input = YamlMapWrapper(YamlMap(emptyMap(), path)),
            expectedEncoding = "yaml: {}",
        )
        roundTrip(
            name = "wrapped list",
            input = YamlListWrapper(YamlList(emptyList(), path)),
            expectedEncoding = "yaml: []",
        )
        roundTrip(
            name = "wrapped scalar",
            input = YamlScalarWrapper(YamlScalar("1", path)),
            expectedEncoding = "yaml: 1",
        )
        roundTrip(
            name = "wrapped null",
            input = YamlNullWrapper(YamlNull(path)),
            expectedEncoding = "yaml: null",
        )
        roundTrip(
            name = "wrapped object as node",
            input = YamlNodeWrapper(YamlMap(emptyMap(), path)),
            expectedEncoding = "yaml: {}",
        )
        roundTrip(
            name = "wrapped list as node",
            input = YamlNodeWrapper(YamlList(emptyList(), path)),
            expectedEncoding = "yaml: []",
        )
        roundTrip(
            name = "wrapped scalar as node",
            input = YamlNodeWrapper(YamlScalar("1", path)),
            expectedEncoding = "yaml: 1",
        )
        roundTrip(
            name = "wrapped null as node",
            input = YamlNodeWrapper(YamlNull(path)),
            expectedEncoding = "yaml: null",
        )
    }
})
