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
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.Team
import com.charleskorn.kaml.testobjects.TestEnum
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.BooleanSerializer
import kotlinx.serialization.internal.ByteSerializer
import kotlinx.serialization.internal.CharSerializer
import kotlinx.serialization.internal.DoubleSerializer
import kotlinx.serialization.internal.EnumSerializer
import kotlinx.serialization.internal.FloatSerializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.LongSerializer
import kotlinx.serialization.internal.ShortSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.internal.makeNullable
import kotlinx.serialization.list
import kotlinx.serialization.map
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object YamlWritingTest : Spek({
    describe("a YAML serializer") {
        describe("serializing null values") {
            val input = null as String?

            context("serializing a null string value") {
                val output = Yaml.default.stringify(makeNullable(StringSerializer), input)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("null")
                }
            }
        }

        describe("serializing boolean values") {
            context("serializing a true value") {
                val output = Yaml.default.stringify(BooleanSerializer, true)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("true")
                }
            }

            context("serializing a false value") {
                val output = Yaml.default.stringify(BooleanSerializer, false)

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("false")
                }
            }
        }

        describe("serializing byte values") {
            val output = Yaml.default.stringify(ByteSerializer, 12)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12")
            }
        }

        describe("serializing character values") {
            context("serializing a alphanumeric character") {
                val output = Yaml.default.stringify(CharSerializer, 'A')

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(""""A"""")
                }
            }

            context("serializing a double-quote character") {
                val output = Yaml.default.stringify(CharSerializer, '"')

                it("returns the value serialized in the expected YAML form, escaping the double-quote character") {
                    assert(output).toBe(""""\""""")
                }
            }

            context("serializing a newline character") {
                val output = Yaml.default.stringify(CharSerializer, '\n')

                it("returns the value serialized in the expected YAML form, escaping the newline character") {
                    assert(output).toBe(""""\n"""")
                }
            }
        }

        describe("serializing double values") {
            val output = Yaml.default.stringify(DoubleSerializer, 12.3)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12.3")
            }
        }

        describe("serializing floating point values") {
            val output = Yaml.default.stringify(FloatSerializer, 45.6f)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("45.6")
            }
        }

        describe("serializing integer values") {
            val output = Yaml.default.stringify(IntSerializer, 12)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12")
            }
        }

        describe("serializing long integer values") {
            val output = Yaml.default.stringify(LongSerializer, 12)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12")
            }
        }

        describe("serializing short integer values") {
            val output = Yaml.default.stringify(ShortSerializer, 12)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe("12")
            }
        }

        describe("serializing string values") {
            context("serializing a string without any special characters") {
                val output = Yaml.default.stringify(StringSerializer, "hello world")

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(""""hello world"""")
                }
            }

            context("serializing an empty string") {
                val output = Yaml.default.stringify(StringSerializer, "")

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe("""""""")
                }
            }

            context("serializing the string 'null'") {
                val output = Yaml.default.stringify(StringSerializer, "null")

                it("returns the value serialized in the expected YAML form") {
                    assert(output).toBe(""""null"""")
                }
            }

            context("serializing a multi-line string") {
                val output = Yaml.default.stringify(StringSerializer, "This is line 1\nThis is line 2")

                it("returns the value serialized in the expected YAML form, escaping the newline character") {
                    assert(output).toBe(""""This is line 1\nThis is line 2"""")
                }
            }

            context("serializing a string containing a double-quote character") {
                val output = Yaml.default.stringify(StringSerializer, """They said "hello" to me""")

                it("returns the value serialized in the expected YAML form, escaping the double-quote characters") {
                    assert(output).toBe(""""They said \"hello\" to me"""")
                }
            }
        }

        describe("serializing enumeration values") {
            val output = Yaml.default.stringify(EnumSerializer(TestEnum::class), TestEnum.Value1)

            it("returns the value serialized in the expected YAML form") {
                assert(output).toBe(""""Value1"""")
            }
        }

        describe("serializing lists") {
            context("serializing a list of integers") {
                val output = Yaml.default.stringify(IntSerializer.list, listOf(1, 2, 3))

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
                val output = Yaml.default.stringify(makeNullable(IntSerializer).list, listOf(1, null, 3))

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
                val output = Yaml.default.stringify(StringSerializer.list, listOf("item1", "item2"))

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

                val output = Yaml.default.stringify(IntSerializer.list.list, input)

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

                val serializer = (StringSerializer to StringSerializer).map.list
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

                val output = Yaml.default.stringify((StringSerializer to StringSerializer).map, input)

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

                val serializer = (StringSerializer to (StringSerializer to StringSerializer).map).map
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

                val serializer = (StringSerializer to IntSerializer.list).map
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

                val serializer = (StringSerializer to SimpleStructure.serializer()).map
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
