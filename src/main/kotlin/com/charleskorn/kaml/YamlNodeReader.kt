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

import org.snakeyaml.engine.v1.common.Anchor
import org.snakeyaml.engine.v1.events.AliasEvent
import org.snakeyaml.engine.v1.events.Event
import org.snakeyaml.engine.v1.events.MappingStartEvent
import org.snakeyaml.engine.v1.events.NodeEvent
import org.snakeyaml.engine.v1.events.ScalarEvent
import org.snakeyaml.engine.v1.events.SequenceStartEvent

class YamlNodeReader(private val parser: YamlParser) {
    private val aliases = mutableMapOf<Anchor, YamlNode>()

    fun read(): YamlNode {
        val event = parser.consumeEvent()
        val node = readFromEvent(event)

        if (event is NodeEvent) {
            event.anchor.ifPresent {
                aliases.put(it, node)
            }
        }

        return node
    }

    private fun readFromEvent(event: Event): YamlNode = when (event) {
        is ScalarEvent -> readScalarOrNull(event)
        is SequenceStartEvent -> readSequence(event.location)
        is MappingStartEvent -> readMapping(event.location)
        is AliasEvent -> readAlias(event)
        else -> throw MalformedYamlException("Unexpected ${event.eventId}", event.location)
    }

    private fun readScalarOrNull(event: ScalarEvent): YamlNode {
        if ((event.value == "null" || event.value == "") && event.isPlain) {
            return YamlNull(event.location)
        } else {
            return YamlScalar(event.value, event.location)
        }
    }

    private fun readSequence(location: Location): YamlList {
        val items = mutableListOf<YamlNode>()

        while (true) {
            val event = parser.peekEvent()

            when (event.eventId) {
                Event.ID.SequenceEnd -> {
                    parser.consumeEventOfType(Event.ID.SequenceEnd)
                    return YamlList(items, location)
                }

                else -> items += read()
            }
        }
    }

    private fun readMapping(location: Location): YamlMap {
        val items = mutableMapOf<YamlNode, YamlNode>()

        while (true) {
            val event = parser.peekEvent()

            when (event.eventId) {
                Event.ID.MappingEnd -> {
                    parser.consumeEventOfType(Event.ID.MappingEnd)
                    return YamlMap(items, location)
                }

                else -> items += (read() to read())
            }
        }
    }

    private fun readAlias(event: AliasEvent): YamlNode {
        val anchor = event.anchor.get()

        return aliases.getOrElse(anchor) {
            throw UnknownAnchorException(anchor.anchor, event.location)
        }
    }
}
