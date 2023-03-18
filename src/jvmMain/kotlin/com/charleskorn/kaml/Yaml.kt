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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.api.YamlOutputStreamWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import java.nio.charset.Charset

public actual class Yaml(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    public actual val configuration: YamlConfiguration = YamlConfiguration(),
) : StringFormat {
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return decodeFromReader(deserializer, StringReader(string))
    }

    public fun <T> decodeFromStream(deserializer: DeserializationStrategy<T>, source: InputStream, charset: Charset = Charsets.UTF_8): T {
        return decodeFromReader(deserializer, InputStreamReader(source, charset))
    }

    private fun <T> decodeFromReader(deserializer: DeserializationStrategy<T>, source: Reader): T {
        val rootNode = parseToYamlNodeFromReader(source)

        val input = YamlInput.createFor(rootNode, this, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    public fun parseToYamlNode(string: String): YamlNode = parseToYamlNodeFromReader(StringReader(string))

    public fun parseToYamlNode(source: InputStream): YamlNode = parseToYamlNodeFromReader(InputStreamReader(source))

    private fun parseToYamlNodeFromReader(source: Reader): YamlNode {
        val parser = YamlParser(source)
        val reader = YamlNodeReader(parser, configuration.extensionDefinitionPrefix, configuration.allowAnchorsAndAliases)
        val node = reader.read()
        parser.ensureEndOfStreamReached()
        return node
    }

    public actual fun <T> decodeFromYamlNode(deserializer: DeserializationStrategy<T>, node: YamlNode): T {
        val input = YamlInput.createFor(node, this, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        val writer = object : StringWriter(), StreamDataWriter {
            override fun flush() { }
        }

        encodeToStreamDataWriter(serializer, value, writer)

        return writer.toString().trimEnd()
    }

    public fun <T> encodeToStream(serializer: SerializationStrategy<T>, value: T, stream: OutputStream, charset: Charset = Charsets.UTF_8) {
        val writer = object : YamlOutputStreamWriter(stream, charset) {
            override fun processIOException(e: IOException?) {
                if (e != null) {
                    throw e
                }
            }
        }

        encodeToStreamDataWriter(serializer, value, writer)
    }

    private fun <T> encodeToStreamDataWriter(serializer: SerializationStrategy<T>, value: T, writer: StreamDataWriter) {
        YamlOutput(writer, serializersModule, configuration).use { output ->
            output.encodeSerializableValue(serializer, value)
        }
    }

    public actual companion object {
        public actual val default: Yaml = Yaml()
    }
}

/**
 * Decodes and deserializes from the given [stream] to the value of type [T] using the
 * deserializer retrieved from the reified type parameter.
 */
public inline fun <reified T> Yaml.decodeFromStream(stream: InputStream): T =
    decodeFromStream(serializersModule.serializer(), stream)

/**
 * Serializes and encodes the given [value] to the given [stream] using the serializer
 * retrieved from the reified type parameter.
 */
public inline fun <reified T> Yaml.encodeToStream(value: T, stream: OutputStream) {
    encodeToStream(serializersModule.serializer(), value, stream)
}
