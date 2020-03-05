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

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assert
import com.charleskorn.kaml.testobjects.NestedObjects
import com.charleskorn.kaml.testobjects.SealedWrapper
import com.charleskorn.kaml.testobjects.SimpleBoolean
import com.charleskorn.kaml.testobjects.SimpleByte
import com.charleskorn.kaml.testobjects.SimpleChar
import com.charleskorn.kaml.testobjects.SimpleDouble
import com.charleskorn.kaml.testobjects.SimpleEnum
import com.charleskorn.kaml.testobjects.SimpleFloat
import com.charleskorn.kaml.testobjects.SimpleInt
import com.charleskorn.kaml.testobjects.SimpleLong
import com.charleskorn.kaml.testobjects.SimpleNull
import com.charleskorn.kaml.testobjects.SimpleNullableInt
import com.charleskorn.kaml.testobjects.SimpleShort
import com.charleskorn.kaml.testobjects.SimpleString
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.SimpleWrapper
import com.charleskorn.kaml.testobjects.Team
import com.charleskorn.kaml.testobjects.TestEnum
import com.charleskorn.kaml.testobjects.TestSealedStructure
import com.charleskorn.kaml.testobjects.simpleModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlWritingTest : Spek({
    describe("a YAML serializer") {
        describe("serializing null values") {
            val input = null as String?

            context("serializing a null string value") {
                val output = Yaml.default.stringify(String.serializer().nullable, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("null")
                }
            }
        }

        describe("serializing boolean values") {
            context("serializing a true value") {
                val output = Yaml.default.stringify(Boolean.serializer(), true)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("true")
                }
            }

            context("serializing a false value") {
                val output = Yaml.default.stringify(Boolean.serializer(), false)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("false")
                }
            }
        }

        describe("serializing byte values") {
            val output = Yaml.default.stringify(Byte.serializer(), 12)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12")
            }
        }

        describe("serializing character values") {
            context("serializing a alphanumeric character") {
                val output = Yaml.default.stringify(Char.serializer(), 'A')

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(""""A"""")
                }
            }

            context("serializing a double-quote character") {
                val output = Yaml.default.stringify(Char.serializer(), '"')

                it("returns the value serialized in the expected YAML form, escaping the double-quote character") {
                    assert(output).toBe(""""\""""")
                }
            }

            context("serializing a newline character") {
                val output = Yaml.default.stringify(Char.serializer(), '\n')

                it("returns the value serialized in the expected YAML form, escaping the newline character") {
                    assert(output).toBe(""""\n"""")
                }
            }
        }

        describe("serializing double values") {
            val output = Yaml.default.stringify(Double.serializer(), 12.3)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12.3")
            }
        }

        describe("serializing floating point values") {
            val output = Yaml.default.stringify(Float.serializer(), 45.6f)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("45.6")
            }
        }

        describe("serializing integer values") {
            val output = Yaml.default.stringify(Int.serializer(), 12)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12")
            }
        }

        describe("serializing long integer values") {
            val output = Yaml.default.stringify(Long.serializer(), 12)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12")
            }
        }

        describe("serializing short integer values") {
            val output = Yaml.default.stringify(Short.serializer(), 12)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12")
            }
        }

        describe("serializing string values") {
            context("serializing a string without any special characters") {
                val output = Yaml.default.stringify(String.serializer(), "hello world")

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(""""hello world"""")
                }
            }

            context("serializing an empty string") {
                val output = Yaml.default.stringify(String.serializer(), "")

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("""""""")
                }
            }

            context("serializing the string 'null'") {
                val output = Yaml.default.stringify(String.serializer(), "null")

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(""""null"""")
                }
            }

            context("serializing a multi-line string") {
                val output = Yaml.default.stringify(String.serializer(), "This is line 1\nThis is line 2")

                it("returns the value serialized in the expected YAML form, escaping the newline character") {
                    assert(output).toBe(""""This is line 1\nThis is line 2"""")
                }
            }

            context("serializing a string containing a double-quote character") {
                val output = Yaml.default.stringify(String.serializer(), """They said "hello" to me""")

                it("returns the value serialized in the expected YAML form, escaping the double-quote characters") {
                    assert(output).toBe(""""They said \"hello\" to me"""")
                }
            }
        }

        describe("serializing enumeration values") {
            val output = Yaml.default.stringify(TestEnum.serializer(), TestEnum.Value1)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe(""""Value1"""")
            }
        }

        describe("serializing lists") {
            context("serializing a list of integers") {
                val output = Yaml.default.stringify(Int.serializer().list, listOf(1, 2, 3))

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        - 1
                        - 2
                        - 3
                    """.trimIndent()
                    )
                }
            }

            context("serializing a list of nullable integers") {
                val output = Yaml.default.stringify(Int.serializer().nullable.list, listOf(1, null, 3))

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        - 1
                        - null
                        - 3
                    """.trimIndent()
                    )
                }
            }

            context("serializing a list of strings") {
                val output = Yaml.default.stringify(String.serializer().list, listOf("item1", "item2"))

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        - "item1"
                        - "item2"
                    """.trimIndent()
                    )
                }
            }

            context("serializing a list of a list of integers") {
                val input = listOf(
                    listOf(1, 2, 3),
                    listOf(4, 5)
                )

                val output = Yaml.default.stringify(Int.serializer().list.list, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        - - 1
                          - 2
                          - 3
                        - - 4
                          - 5
                    """.trimIndent()
                    )
                }
            }

            context("serializing a list of maps from strings to strings") {
                val input = listOf(
                    mapOf(
                        "key1" to "value1",
                        "key2" to "value2"
                    ),
                    mapOf(
                        "key3" to "value3"
                    )
                )

                val serializer = MapSerializer(String.serializer(), String.serializer()).list
                val output = Yaml.default.stringify(serializer, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        - "key1": "value1"
                          "key2": "value2"
                        - "key3": "value3"
                    """.trimIndent()
                    )
                }
            }

            context("serializing a list of objects") {
                val input = listOf(
                    SimpleStructure("name1"),
                    SimpleStructure("name2")
                )

                val output = Yaml.default.stringify(SimpleStructure.serializer().list, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        - name: "name1"
                        - name: "name2"
                    """.trimIndent()
                    )
                }
            }
        }

        describe("serializing maps") {
            context("serializing a map of strings to strings") {
                val input = mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )

                val output = Yaml.default.stringify(MapSerializer(String.serializer(), String.serializer()), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        "key1": "value1"
                        "key2": "value2"
                    """.trimIndent()
                    )
                }
            }

            context("serializing a nested map of strings to strings") {
                val input = mapOf(
                    "map1" to mapOf(
                        "key1" to "value1",
                        "key2" to "value2"
                    ),
                    "map2" to mapOf(
                        "key3" to "value3"
                    )
                )

                val serializer = MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer()))
                val output = Yaml.default.stringify(serializer, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        "map1":
                          "key1": "value1"
                          "key2": "value2"
                        "map2":
                          "key3": "value3"
                    """.trimIndent()
                    )
                }
            }

            context("serializing a map of strings to lists") {
                val input = mapOf(
                    "list1" to listOf(1, 2, 3),
                    "list2" to listOf(4, 5, 6)
                )

                val serializer = MapSerializer(String.serializer(), Int.serializer().list)
                val output = Yaml.default.stringify(serializer, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        "list1":
                        - 1
                        - 2
                        - 3
                        "list2":
                        - 4
                        - 5
                        - 6
                    """.trimIndent()
                    )
                }
            }

            context("serializing a map of strings to objects") {
                val input = mapOf(
                    "item1" to SimpleStructure("name1"),
                    "item2" to SimpleStructure("name2")
                )

                val serializer = MapSerializer(String.serializer(), SimpleStructure.serializer())
                val output = Yaml.default.stringify(serializer, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        "item1":
                          name: "name1"
                        "item2":
                          name: "name2"
                    """.trimIndent()
                    )
                }
            }
        }

        describe("serializing objects") {
            context("serializing a simple object") {
                val input = SimpleStructure("The name")
                val output = Yaml.default.stringify(SimpleStructure.serializer(), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        name: "The name"
                    """.trimIndent()
                    )
                }
            }

            context("serializing a nested object") {
                val input = NestedObjects(
                    SimpleStructure("name1"),
                    SimpleStructure("name2")
                )

                val output = Yaml.default.stringify(NestedObjects.serializer(), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        firstPerson:
                          name: "name1"
                        secondPerson:
                          name: "name2"
                    """.trimIndent()
                    )
                }
            }

            context("serializing an object with a nested list") {
                val input = Team(listOf("name1", "name2"))
                val output = Yaml.default.stringify(Team.serializer(), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        members:
                        - "name1"
                        - "name2"
                    """.trimIndent()
                    )
                }
            }

            context("serializing an object with a nested map") {
                @Serializable
                data class ThingWithMap(val variables: Map<String, String>)

                val input = ThingWithMap(
                    mapOf(
                        "var1" to "value1",
                        "var2" to "value2"
                    )
                )

                val output = Yaml.default.stringify(ThingWithMap.serializer(), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        variables:
                          "var1": "value1"
                          "var2": "value2"
                    """.trimIndent()
                    )
                }
            }
        }

        describe("handling sealed classes") {
            context("serializing int sealed class") {
                val input = SealedWrapper(TestSealedStructure.SimpleSealedInt(5))
                val output = Yaml.default.stringify(SealedWrapper.serializer(), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        element: !<sealedInt>
                          value: 5
                    """.trimIndent()
                    )
                }
            }

            context("serializing string sealed class") {
                val input = SealedWrapper(TestSealedStructure.SimpleSealedString("5"))
                val output = Yaml.default.stringify(SealedWrapper.serializer(), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        element: !<sealedString>
                          value: "5"
                    """.trimIndent()
                    )
                }
            }

            context("serializing list of sealed class structures") {
                val input = listOf(
                    TestSealedStructure.SimpleSealedInt(5),
                    TestSealedStructure.SimpleSealedString("some test"),
                    TestSealedStructure.SimpleSealedInt(-20),
                    TestSealedStructure.SimpleSealedString(null),
                    null
                ).map(::SealedWrapper)

                val output = Yaml.default.stringify(SealedWrapper.serializer().list, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        - element: !<sealedInt>
                            value: 5
                        - element: !<sealedString>
                            value: "some test"
                        - element: !<sealedInt>
                            value: -20
                        - element: !<sealedString>
                            value: null
                        - element: null
                    """.trimIndent()
                    )
                }
            }
        }

        describe("handling simple polymorphic structures") {
            val yaml = Yaml(context = simpleModule)
            context("serializing a boolean structure") {
                val input = SimpleWrapper(SimpleBoolean(true))
                val output = yaml.stringify(SimpleWrapper.serializer(), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        test: !<simpleBoolean> 'true'
                        """.trimIndent()
                    )
                }
            }

            context("serializing a list of structures") {
                val input = listOf(
                    SimpleWrapper(SimpleNull),
                    SimpleWrapper(SimpleBoolean(true)),
                    SimpleWrapper(SimpleByte(24)),
                    SimpleWrapper(SimpleShort(34)),
                    SimpleWrapper(SimpleInt(44)),
                    SimpleWrapper(SimpleLong(54L)),
                    SimpleWrapper(SimpleFloat(2.4f)),
                    SimpleWrapper(SimpleDouble(2.4)),
                    SimpleWrapper(SimpleChar('2')),
                    SimpleWrapper(SimpleString("24")),
                    SimpleWrapper(SimpleEnum.TEST),
                    SimpleWrapper(SimpleNullableInt(3)),
                    SimpleWrapper(SimpleNullableInt(null))
                )
                val output = yaml.stringify(SimpleWrapper.serializer().list, input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(
                        """
                        - test: !<simpleNull> 'null'
                        - test: !<simpleBoolean> 'true'
                        - test: !<simpleByte> '24'
                        - test: !<simpleShort> '34'
                        - test: !<simpleInt> '44'
                        - test: !<simpleLong> '54'
                        - test: !<simpleFloat> '2.4'
                        - test: !<simpleDouble> '2.4'
                        - test: !<simpleChar> "2"
                        - test: !<simpleString> "24"
                        - test: !<simpleEnum> "TEST"
                        - test: !<simpleNullableInt> '3'
                        - test: !<simpleNullableInt> 'null'
                        """.trimIndent()
                    )
                }
            }
        }

        describe("handling default values") {
            context("when encoding defaults") {
                val defaultEncoder = Yaml.default

                context("given a property with no default value") {
                    val input = SimpleStructure("name1")

                    it("is always written") {
                        assert(defaultEncoder.stringify(SimpleStructure.serializer(), input)).toBe("""name: "name1"""")
                    }
                }

                context("given a property with a default value") {
                    val input = SimpleStructureWithDefault()

                    it("is written") {
                        assert(defaultEncoder.stringify(SimpleStructureWithDefault.serializer(), input)).toBe("""name: "default"""")
                    }
                }

                context("given a property with a default value has a non-default value") {
                    val input = SimpleStructureWithDefault("name1")

                    it("is written") {
                        assert(defaultEncoder.stringify(SimpleStructureWithDefault.serializer(), input)).toBe("""name: "name1"""")
                    }
                }
            }

            context("when not encoding defaults") {
                val noDefaultEncoder = Yaml(configuration = YamlConfiguration(encodeDefaults = false))

                context("given a property with no default value") {
                    val input = SimpleStructure("name1")

                    it("is always written") {
                        assert(noDefaultEncoder.stringify(SimpleStructure.serializer(), input)).toBe("""name: "name1"""")
                    }
                }

                context("given a property with a default value") {
                    val input = SimpleStructureWithDefault()

                    it("is not written") {
                        assert(noDefaultEncoder.stringify(SimpleStructureWithDefault.serializer(), input)).toBe("""{}""")
                    }
                }

                context("given a property with a default value has a non-default value") {
                    val input = SimpleStructureWithDefault("name1")

                    it("is written") {
                        assert(noDefaultEncoder.stringify(SimpleStructureWithDefault.serializer(), input)).toBe("""name: "name1"""")
                    }
                }
            }
        }
    }
})

@Serializable
private data class SimpleStructureWithDefault(
    val name: String = "default"
)
