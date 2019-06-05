/*

   Copyright 2018-2019 Charles Korn.

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

import ch.tutteli.atrium.api.cc.en_GB.notToBe
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assert
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlinx.serialization.Serializable

object YamlNoDefaults : Spek({
    describe("a YAML default value") {
        describe("testing equivalence") {
            @Serializable
            data class LocationLayerDefault(val line: Int, val column: Int, val layer: Int? = null)

            @Serializable
            data class LocationNoDefault(val line: Int, val column: Int)

            val noDefaultEncoder = Yaml(configuration = YamlConfiguration(encodeDefaults = false))
            val defaultEncoder = Yaml.default

            val locationLayerDefaultNoEncodeDefault = noDefaultEncoder.stringify(
                LocationLayerDefault.serializer(),
                LocationLayerDefault(1, 1, 42)
            )

            val locationLayerNoEncodeDefault = noDefaultEncoder.stringify(
                LocationLayerDefault.serializer(),
                LocationLayerDefault(1, 1)
            )

            val locationLayerDefault = defaultEncoder.stringify(
                LocationLayerDefault.serializer(),
                LocationLayerDefault(1, 1)
            )

            val location = defaultEncoder.stringify(
                LocationNoDefault.serializer(),
                LocationNoDefault(1, 1)
            )

            context("compare an object with default with and without encodeDefaults") {
                it("indicates that they are not equivalent") {
                    assert(locationLayerDefaultNoEncodeDefault).notToBe(locationLayerNoEncodeDefault)
                }
            }

            context("with encoding defaults: comparing an object with default overridden and object without that element") {
                it("indicates that they are not equivalent") {
                    assert(locationLayerDefaultNoEncodeDefault).notToBe(location)
                }
            }

            context("with encoding defaults: comparing an object with null default and object without that element") {
                it("indicates that they are equivalent") {
                    assert(locationLayerNoEncodeDefault).toBe(location)
                }
            }

            context("without encoding defaults: comparing an object with null default and object without that element") {
                it("indicates that they are equivalent") {
                    assert(locationLayerDefault).notToBe(locationLayerDefaultNoEncodeDefault)
                }
            }
        }
    }
})
