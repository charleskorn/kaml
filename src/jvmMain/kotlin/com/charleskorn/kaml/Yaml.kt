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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.snakeyaml.engine.v2.api.StreamDataWriter
import java.io.InputStream
import java.io.StringWriter
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_8

@OptIn(ExperimentalSerializationApi::class)
public actual class Yaml(
    override val serializersModule: SerializersModule = EmptySerializersModule,
    public actual val configuration: YamlConfiguration = YamlConfiguration()
) : StringFormat {

    public inline fun <reified T> decodeFromStream(inputStream: InputStream, charset: Charset = UTF_8): T {
        return decodeFromString(inputStream.bufferedReader(charset).readText())
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val parser = YamlParser(string)
        val reader = YamlNodeReader(parser, configuration.extensionDefinitionPrefix)
        val rootNode = reader.read()
        parser.ensureEndOfStreamReached()

        val input = YamlInput.createFor(rootNode, serializersModule, configuration, deserializer.descriptor)
        return input.decodeSerializableValue(deserializer)
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        val writer = object : StringWriter(), StreamDataWriter {
            override fun flush() { }
        }

        YamlOutput(writer, serializersModule, configuration).use { output ->
            output.encodeSerializableValue(serializer, value)
        }

        return writer.toString().trimEnd()
    }

    public actual companion object {
        public actual val default: Yaml = Yaml()
    }
}
