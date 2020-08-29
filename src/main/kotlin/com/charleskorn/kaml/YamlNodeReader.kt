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

import java.util.Optional
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.events.AliasEvent
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.NodeEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.SequenceStartEvent

class YamlNodeReader(
    private val parser: YamlParser,
    private val extensionDefinitionPrefix: String? = null
) {
    private val aliases = mutableMapOf<Anchor, YamlNode>()

    fun read(): YamlNode = readNode(true)

    private fun readNode(isTopLevel: Boolean = false): YamlNode = readNodeAndAnchor(isTopLevel).first

    private fun readNodeAndAnchor(isTopLevel: Boolean = false): Pair<YamlNode, Anchor?> {
        val event = parser.consumeEvent()
        val node = readFromEvent(event, isTopLevel)

        if (event is NodeEvent) {
            event.anchor.ifPresent {
                aliases.put(it, node)
            }

            return node to event.anchor.orElse(null)
        }

        return node to null
    }

    private fun readFromEvent(event: Event, isTopLevel: Boolean): YamlNode = when (event) {
        is ScalarEvent -> readScalarOrNull(event).maybeToTaggedNode(event.tag)
        is SequenceStartEvent -> readSequence(event.location).maybeToTaggedNode(event.tag)
        is MappingStartEvent -> readMapping(event.location, isTopLevel).maybeToTaggedNode(event.tag)
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

                else -> items += readNode()
            }
        }
    }

    private fun readMapping(location: Location, isTopLevel: Boolean): YamlMap {
        val items = mutableMapOf<YamlNode, YamlNode>()

        while (true) {
            val event = parser.peekEvent()

            when (event.eventId) {
                Event.ID.MappingEnd -> {
                    parser.consumeEventOfType(Event.ID.MappingEnd)
                    return YamlMap(doMerges(items), location)
                }

                else -> {
                    val key = readNode()
                    val (value, anchor) = readNodeAndAnchor()

                    if (isTopLevel && extensionDefinitionPrefix != null && key.isScalarAndStartsWith(extensionDefinitionPrefix)) {
                        if (anchor == null) {
                            throw NoAnchorForExtensionException(key.contentToString(), extensionDefinitionPrefix, event.location)
                        }
                    } else {
                        items += (key to value)
                    }
                }
            }
        }
    }

    private fun YamlNode.maybeToTaggedNode(tag: Optional<String>): YamlNode =
        tag.map<YamlNode> { YamlTaggedNode(it, this) }.orElse(this)

    private fun YamlNode.isScalarAndStartsWith(prefix: String): Boolean = this is YamlScalar && this.content.startsWith(prefix)

    private fun doMerges(items: Map<YamlNode, YamlNode>): Map<YamlNode, YamlNode> {
        val mergeEntries = items.entries.filter { (key, _) -> isMerge(key) }

        when (mergeEntries.count()) {
            0 -> return items
            1 -> when (val mappingsToMerge = mergeEntries.single().value) {
                is YamlList -> return doMerges(items, mappingsToMerge.items)
                else -> return doMerges(items, listOf(mappingsToMerge))
            }
            else -> throw MalformedYamlException("Cannot perform multiple merges into a map.", mergeEntries.second().key.location)
        }
    }

    private fun isMerge(key: YamlNode): Boolean = key is YamlScalar && key.content == "<<"

    private fun doMerges(original: Map<YamlNode, YamlNode>, others: List<YamlNode>): Map<YamlNode, YamlNode> {
        val merged = mutableMapOf<YamlNode, YamlNode>()

        original
            .filterNot { (key, _) -> isMerge(key) }
            .forEach { (key, value) -> merged.put(key, value) }

        others
            .forEach { other ->
                when (other) {
                    is YamlNull -> throw MalformedYamlException("Cannot merge a null value into a map.", other.location)
                    is YamlScalar -> throw MalformedYamlException("Cannot merge a scalar value into a map.", other.location)
                    is YamlList -> throw MalformedYamlException("Cannot merge a list value into a map.", other.location)
                    is YamlMap ->
                        other.entries.forEach { (key, value) ->
                            val existingEntry = merged.entries.singleOrNull { it.key.equivalentContentTo(key) }

                            if (existingEntry == null) {
                                merged.put(key, value)
                            }
                        }
                }
            }

        return merged
    }

    private fun readAlias(event: AliasEvent): YamlNode {
        val anchor = event.anchor.get()

        return aliases.getOrElse(anchor) {
            throw UnknownAnchorException(anchor.value, event.location)
        }
    }

    private fun <T> Iterable<T>.second(): T = this.drop(1).first()
}
