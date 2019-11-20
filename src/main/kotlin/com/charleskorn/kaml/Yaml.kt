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

import kotlinx.serialization.AbstractSerialFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decode
import kotlinx.serialization.encode
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import org.snakeyaml.engine.v2.api.StreamDataWriter
import java.io.StringWriter

class Yaml(
    override val context: SerialModule = EmptyModule,
    val configuration: YamlConfiguration = YamlConfiguration()
) : AbstractSerialFormat(context), StringFormat {
    override fun <T> parse(deserializer: DeserializationStrategy<T>, string: String): T {
        val parser = YamlParser(string)
        val reader = YamlNodeReader(parser, configuration.extensionDefinitionPrefix)
        val rootNode = reader.read()
        parser.ensureEndOfStreamReached()

        val input = YamlInput.createFor(rootNode, context, configuration)
        return input.decode(deserializer)
    }

    override fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String {
        val writer = object : StringWriter(), StreamDataWriter {
            override fun flush() { }
        }

        val output = YamlOutput(writer, context, configuration)
        output.encode(serializer, obj)

        return writer.toString()
    }

    companion object {
        val default = Yaml()
    }
}
