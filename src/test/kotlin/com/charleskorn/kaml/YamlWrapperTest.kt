/*

   Copyright 2018-2020 Charles Korn.

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

@file:OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)

package com.charleskorn.kaml.testobjects

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlReadingTest : Spek({
    describe("a YAML parser") {
        describe("parsing scalars") {
            context("given the input 'hello'") {
                val input = "hello"

                context("parsing that input as a string") {
                    val result = Yaml.decodeFromString<String>(input)

                    it("deserializes it to the expected string value") {
                        expect(result).toBe("hello")
                    }
                }
            }

            context("given the input '123'") {
                val input = "123"

                context("parsing that input as an integer") {
                    val result = Yaml.default.decodeFromString(Int.serializer(), input)

                    it("deserializes it to the expected integer") {
                        expect(result).toBe(123)
                    }
                }

                context("parsing that input as a long") {
                    val result = Yaml.default.decodeFromString(Long.serializer(), input)

                    it("deserializes it to the expected long") {
                        expect(result).toBe(123)
                    }
                }
            }
        }

        describe("parsing objects") {
            context("given some input representing an object with an optional value specified") {
                val input = """
                    string: Alex
                    byte: 12
                    short: 1234
                    int: 123456
                    long: 1234567
                    float: 1.2
                    double: 2.4
                    enum: Value1
                    boolean: true
                    char: A
                    nullable: present
                """.trimIndent()

                context("parsing that input") {
                    val result = Yaml.decodeFromString<ComplexStructure>(input)

                    it("deserializes it to a Kotlin object") {
                        expect(result).toBe(
                            ComplexStructure(
                                "Alex",
                                12,
                                1234,
                                123456,
                                1234567,
                                1.2f,
                                2.4,
                                TestEnum.Value1,
                                true,
                                'A',
                                "present"
                            )
                        )
                    }
                }
            }
        }
    }
}) {
    @Serializable
    private data class ComplexStructure(
        val string: String,
        val byte: Byte,
        val short: Short,
        val int: Int,
        val long: Long,
        val float: Float,
        val double: Double,
        val enum: TestEnum,
        val boolean: Boolean,
        val char: Char,
        val nullable: String? = null
    )
}
