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

@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package com.charleskorn.kaml

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.serializersModuleOf

class YamlContextualReadingTest : FlatFunSpec({
    context("a YAML parser") {
        context("parsing values with a contextual serializer") {
            mapOf(
                "scalar" to "2",
                "list" to "[ thing ]",
                "map" to "{ key: value }",
            ).forEach { (description, input) ->
                context("given some input representing a $description") {
                    context("parsing that input using a contextual serializer at the top level") {
                        val result = Yaml.default.decodeFromString(ContextualSerializer, input)

                        test("the serializer receives the top-level object") {
                            result shouldBe description
                        }
                    }

                    context("parsing that input using a contextual serializer nested within an object") {
                        val result = Yaml.default.decodeFromString(
                            ObjectWithNestedContextualSerializer.serializer(),
                            "thing: $input",
                        )

                        test("the serializer receives the correct object") {
                            result shouldBe ObjectWithNestedContextualSerializer(description)
                        }
                    }
                }
            }

            context("given the contextual serializer attempts to begin a structure that does not match the input") {
                context("given the input is a map") {
                    val input = "a: b"

                    mapOf(
                        PrimitiveKind.STRING to "a string",
                        StructureKind.LIST to "a list",
                    ).forEach { (kind, description) ->
                        context("attempting to begin $description") {
                            test("throws an exception with the correct location information") {
                                val exception = shouldThrow<IncorrectTypeException> {
                                    Yaml.default.decodeFromString(
                                        ContextualSerializerThatAttemptsToDeserializeIncorrectType(kind),
                                        input,
                                    )
                                }

                                exception.asClue {
                                    it.message shouldBe "Expected $description, but got a map"
                                    it.line shouldBe 1
                                    it.column shouldBe 1
                                    it.path shouldBe YamlPath.root
                                }
                            }
                        }
                    }
                }

                context("given the input is a list") {
                    val input = "- a"

                    mapOf(
                        StructureKind.OBJECT to "an object",
                        StructureKind.CLASS to "an object",
                        StructureKind.MAP to "a map",
                        PrimitiveKind.STRING to "a string",
                    ).forEach { (kind, description) ->
                        context("attempting to begin $kind") {
                            test("throws an exception with the correct location information") {
                                val exception = shouldThrow<IncorrectTypeException> {
                                    Yaml.default.decodeFromString(
                                        ContextualSerializerThatAttemptsToDeserializeIncorrectType(kind),
                                        input,
                                    )
                                }

                                exception.asClue {
                                    it.message shouldBe "Expected $description, but got a list"
                                    it.line shouldBe 1
                                    it.column shouldBe 1
                                    it.path shouldBe YamlPath.root
                                }
                            }
                        }
                    }
                }

                context("given the input is a scalar") {
                    val input = "2"

                    mapOf(
                        StructureKind.OBJECT to "an object",
                        StructureKind.CLASS to "an object",
                        StructureKind.MAP to "a map",
                        StructureKind.LIST to "a list",
                    ).forEach { (kind, description) ->
                        context("attempting to begin $kind") {
                            test("throws an exception with the correct location information") {
                                val exception = shouldThrow<IncorrectTypeException> {
                                    Yaml.default.decodeFromString(
                                        ContextualSerializerThatAttemptsToDeserializeIncorrectType(kind),
                                        input,
                                    )
                                }

                                exception.asClue {
                                    it.message shouldBe "Expected $description, but got a scalar value"
                                    it.line shouldBe 1
                                    it.column shouldBe 1
                                    it.path shouldBe YamlPath.root
                                }
                            }
                        }
                    }
                }
            }

            context("decoding from a YamlNode") {
                val input = """
                    keyA:
                        host: A
                    keyB:
                        host: B
                """.trimIndent()

                val mapAsListSerializer = object : KSerializer<List<Database>> {
                    override val descriptor = buildSerialDescriptor("DatabaseList", StructureKind.MAP) {
                    }

                    override fun deserialize(decoder: Decoder): List<Database> {
                        check(decoder is YamlInput)
                        return decoder.node.yamlMap.entries.map { (_, value) ->
                            decoder.yaml.decodeFromYamlNode(Database.serializer(), value)
                        }
                    }

                    override fun serialize(encoder: Encoder, value: List<Database>) =
                        throw UnsupportedOperationException()
                }

                val parser = Yaml.default
                val result = parser.decodeFromString(mapAsListSerializer, input)

                test("decodes the map value as a list using the YamlNode") {
                    result shouldBe listOf(Database("A"), Database("B"))
                }
            }

            context("decoding from a YamlNode at a non-root node") {
                val input = """
                    databaseListing:
                        keyA:
                            host: A
                        keyB:
                            host: B
                """.trimIndent()

                val parser = Yaml.default
                val result = parser.decodeFromString(ServerConfig.serializer(), input)

                test("decodes the map value as a list using the YamlNode") {
                    result shouldBe ServerConfig(DatabaseListing(listOf(Database("A"), Database("B"))))
                }
            }
        }

        context("parsing values with a dynamically installed serializer") {
            context("parsing a literal with a contextual serializer") {
                val contextSerializer = object : KSerializer<Inner> {
                    override val descriptor: SerialDescriptor
                        get() = String.serializer().descriptor

                    override fun deserialize(decoder: Decoder): Inner =
                        Inner("from context serializer: ${decoder.decodeString()}")

                    override fun serialize(encoder: Encoder, value: Inner) = throw UnsupportedOperationException()
                }

                val module = serializersModuleOf(Inner::class, contextSerializer)
                val parser = Yaml(serializersModule = module)

                val input = """
                    inner: this is the input
                """.trimIndent()

                val result = parser.decodeFromString(Container.serializer(), input)

                test("deserializes it using the dynamically installed serializer") {
                    result shouldBe Container(Inner("from context serializer: this is the input"))
                }
            }

            context("parsing a class with a contextual serializer") {
                val contextSerializer = object : KSerializer<Inner> {
                    override val descriptor = buildClassSerialDescriptor("Inner") {
                        element("thing", String.serializer().descriptor)
                    }

                    override fun deserialize(decoder: Decoder): Inner {
                        val objectDecoder = decoder.beginStructure(descriptor)
                        val index = objectDecoder.decodeElementIndex(descriptor)
                        val name = objectDecoder.decodeStringElement(descriptor, index)
                        objectDecoder.endStructure(descriptor)

                        return Inner("$name, from context serializer")
                    }

                    override fun serialize(encoder: Encoder, value: Inner) = throw UnsupportedOperationException()
                }

                val module = serializersModuleOf(Inner::class, contextSerializer)
                val parser = Yaml(serializersModule = module)

                val input = """
                    inner:
                        thing: this is the input
                """.trimIndent()

                val result = parser.decodeFromString(Container.serializer(), input)

                test("deserializes it using the dynamically installed serializer") {
                    result shouldBe Container(Inner("this is the input, from context serializer"))
                }
            }

            context("parsing a map with a contextual serializer") {
                val contextSerializer = object : KSerializer<Inner> {
                    override val descriptor = buildSerialDescriptor("Inner", StructureKind.MAP) {
                        element("key", String.serializer().descriptor)
                        element("value", String.serializer().descriptor)
                    }

                    override fun deserialize(decoder: Decoder): Inner {
                        val objectDecoder = decoder.beginStructure(descriptor)
                        val keyIndex = objectDecoder.decodeElementIndex(descriptor)
                        val key = objectDecoder.decodeStringElement(descriptor, keyIndex)
                        val valueIndex = objectDecoder.decodeElementIndex(descriptor)
                        val value = objectDecoder.decodeStringElement(descriptor, valueIndex)

                        objectDecoder.endStructure(descriptor)

                        return Inner("$key: $value, from context serializer")
                    }

                    override fun serialize(encoder: Encoder, value: Inner) = throw UnsupportedOperationException()
                }

                val module = serializersModuleOf(Inner::class, contextSerializer)
                val parser = Yaml(serializersModule = module)

                val input = """
                    inner:
                        thing: this is the input
                """.trimIndent()

                val result = parser.decodeFromString(Container.serializer(), input)

                test("deserializes it using the dynamically installed serializer") {
                    result shouldBe Container(Inner("thing: this is the input, from context serializer"))
                }
            }
        }
    }
})

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class ContextualSerializerThatAttemptsToDeserializeIncorrectType(private val kind: SerialKind) : KSerializer<String> {
    private val innerDescriptor = if (kind == StructureKind.CLASS) buildClassSerialDescriptor("thing") else buildSerialDescriptor("thing", kind)

    override val descriptor = buildSerialDescriptor("ContextualSerializer", SerialKind.CONTEXTUAL) {
        element("string", PrimitiveSerialDescriptor("value", PrimitiveKind.STRING))
        element("object", innerDescriptor)
    }

    override fun deserialize(decoder: Decoder): String {
        val input = decoder.beginStructure(descriptor) as YamlInput

        input.beginStructure(innerDescriptor)

        return "Should never get to this point"
    }

    override fun serialize(encoder: Encoder, value: String): Unit = throw UnsupportedOperationException()
}

@Serializable
private data class Database(val host: String)

@Serializable(with = DecodingFromYamlNodeSerializer::class)
private data class DatabaseListing(val databases: List<Database>)

@Serializable
private data class ServerConfig(val databaseListing: DatabaseListing)

private object DecodingFromYamlNodeSerializer : KSerializer<DatabaseListing> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("DecodingFromYamlNodeSerializer", StructureKind.MAP)

    override fun deserialize(decoder: Decoder): DatabaseListing {
        check(decoder is YamlInput)

        val currentMap = decoder.node.yamlMap.get<YamlMap>("databaseListing")
        checkNotNull(currentMap)

        val list = currentMap.entries.map { (_, value) ->
            decoder.yaml.decodeFromYamlNode(Database.serializer(), value)
        }

        return DatabaseListing(list)
    }

    override fun serialize(encoder: Encoder, value: DatabaseListing) = throw UnsupportedOperationException()
}

@Serializable
private data class ObjectWithNestedContextualSerializer(@Serializable(with = ContextualSerializer::class) val thing: String)

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
object ContextualSerializer : KSerializer<String> {
    override val descriptor = buildSerialDescriptor("ContextualSerializer", SerialKind.CONTEXTUAL) {
        element("string", PrimitiveSerialDescriptor("value", PrimitiveKind.STRING))
        element("object", buildSerialDescriptor("thing", StructureKind.OBJECT))
    }

    override fun deserialize(decoder: Decoder): String {
        val input = decoder.beginStructure(descriptor) as YamlInput
        val type = input.node::class.simpleName!!
        input.endStructure(descriptor)

        return type.removePrefix("Yaml").lowercase()
    }

    override fun serialize(encoder: Encoder, value: String): Unit = throw UnsupportedOperationException()
}

private data class Inner(val name: String)

@Serializable
private data class Container(@Contextual val inner: Inner)
