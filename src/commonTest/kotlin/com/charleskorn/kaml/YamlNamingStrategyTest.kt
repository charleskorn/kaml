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

import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable

class YamlNamingStrategyTest : FlatFunSpec({
    context("a YAML parser deserializing serial names using YamlNamingStrategies") {
        @Serializable
        data class NamingStrategyTestData(val serialName: String)

        context("deserializing a snake_case serial name using YamlNamingStrategy.SnakeCase") {
            val output = Yaml(
                configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.SnakeCase),
            ).decodeFromString(NamingStrategyTestData.serializer(), "serial_name: value")

            test("correctly serializes into the data class") {
                output shouldBe NamingStrategyTestData("value")
            }
        }

        context("deserializing a PascalCase serial name using YamlNamingStrategy.PascalCase") {
            val output = Yaml(
                configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.PascalCase),
            ).decodeFromString(NamingStrategyTestData.serializer(), "SerialName: value")

            test("correctly serializes into the data class") {
                output shouldBe NamingStrategyTestData("value")
            }
        }

        context("deserializing a camelCase serial name using YamlNamingStrategy.CamelCase") {
            val output = Yaml(
                configuration = YamlConfiguration(yamlNamingStrategy = YamlNamingStrategy.CamelCase),
            ).decodeFromString(NamingStrategyTestData.serializer(), "serialName: value")

            test("correctly serializes into the data class") {
                output shouldBe NamingStrategyTestData("value")
            }
        }
    }
})
