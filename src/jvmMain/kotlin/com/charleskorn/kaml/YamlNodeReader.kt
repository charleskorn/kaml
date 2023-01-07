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

import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.events.AliasEvent
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.NodeEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.SequenceStartEvent
import java.util.Optional

internal actual class YamlNodeReader(
    private val parser: YamlParser,
    private val extensionDefinitionPrefix: String? = null,
) {
    private val aliases = mutableMapOf<Anchor, YamlNode>()

    actual fun read(): YamlNode = readNode(YamlPath.root)

    private fun readNode(path: YamlPath): YamlNode = readNodeAndAnchor(path).first

    private fun readNodeAndAnchor(path: YamlPath): Pair<YamlNode, Anchor?> {
        val event = parser.consumeEvent(path)
        val node = readFromEvent(event, path)

        if (event is NodeEvent) {
            event.anchor.ifPresent {
                aliases.put(it, node.withPath(YamlPath.forAliasDefinition(it.value, event.location)))
            }

            return node to event.anchor.orElse(null)
        }

        return node to null
    }

    private fun readFromEvent(event: Event, path: YamlPath): YamlNode = when (event) {
        is ScalarEvent -> readScalarOrNull(event, path).maybeToTaggedNode(event.tag)
        is SequenceStartEvent -> readSequence(path).maybeToTaggedNode(event.tag)
        is MappingStartEvent -> readMapping(path).maybeToTaggedNode(event.tag)
        is AliasEvent -> readAlias(event, path)
        else -> throw MalformedYamlException("Unexpected ${event.eventId}", path.withError(event.location))
    }

    private fun readScalarOrNull(event: ScalarEvent, path: YamlPath): YamlNode {
        if ((event.value == "null" || event.value == "" || event.value == "~") && event.isPlain) {
            return YamlNull(path)
        } else {
            return YamlScalar(event.value, path)
        }
    }

    private fun readSequence(path: YamlPath): YamlList {
        val items = mutableListOf<YamlNode>()

        while (true) {
            val event = parser.peekEvent(path)

            when (event.eventId) {
                Event.ID.SequenceEnd -> {
                    parser.consumeEventOfType(Event.ID.SequenceEnd, path)
                    return YamlList(items, path)
                }

                else -> items += readNode(path.withListEntry(items.size, event.location))
            }
        }
    }

    private fun readMapping(path: YamlPath): YamlMap {
        val items = mutableMapOf<YamlScalar, YamlNode>()

        while (true) {
            val event = parser.peekEvent(path)

            when (event.eventId) {
                Event.ID.MappingEnd -> {
                    parser.consumeEventOfType(Event.ID.MappingEnd, path)
                    return YamlMap(doMerges(items), path)
                }

                else -> {
                    val keyLocation = parser.peekEvent(path).location
                    val key = readMapKey(path)
                    val keyNode = YamlScalar(key, path.withMapElementKey(key, keyLocation))

                    val valueLocation = parser.peekEvent(keyNode.path).location
                    val valuePath = if (isMerge(keyNode)) path.withMerge(valueLocation) else keyNode.path.withMapElementValue(valueLocation)
                    val (value, anchor) = readNodeAndAnchor(valuePath)

                    if (path == YamlPath.root && extensionDefinitionPrefix != null && key.startsWith(extensionDefinitionPrefix)) {
                        if (anchor == null) {
                            throw NoAnchorForExtensionException(key, extensionDefinitionPrefix, path.withError(event.location))
                        }
                    } else {
                        items += (keyNode to value)
                    }
                }
            }
        }
    }

    private fun readMapKey(path: YamlPath): String {
        val event = parser.peekEvent(path)

        when (event.eventId) {
            Event.ID.Scalar -> {
                parser.consumeEventOfType(Event.ID.Scalar, path)
                val scalarEvent = event as ScalarEvent
                val isNullKey = (scalarEvent.value == "null" || scalarEvent.value == "~") && scalarEvent.isPlain

                if (scalarEvent.tag.isPresent || isNullKey) {
                    throw nonScalarMapKeyException(path, event)
                }

                return scalarEvent.value
            }
            else -> throw nonScalarMapKeyException(path, event)
        }
    }

    private fun nonScalarMapKeyException(path: YamlPath, event: Event) = MalformedYamlException("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)", path.withError(event.location))

    private fun YamlNode.maybeToTaggedNode(tag: Optional<String>): YamlNode =
        tag.map<YamlNode> { YamlTaggedNode(it, this) }.orElse(this)

    private fun doMerges(items: Map<YamlScalar, YamlNode>): Map<YamlScalar, YamlNode> {
        val mergeEntries = items.entries.filter { (key, _) -> isMerge(key) }

        when (mergeEntries.count()) {
            0 -> return items
            1 -> when (val mappingsToMerge = mergeEntries.single().value) {
                is YamlList -> return doMerges(items, mappingsToMerge.items)
                else -> return doMerges(items, listOf(mappingsToMerge))
            }
            else -> throw MalformedYamlException("Cannot perform multiple '<<' merges into a map. Instead, combine all merges into a single '<<' entry.", mergeEntries.second().key.path)
        }
    }

    private fun isMerge(key: YamlNode): Boolean = key is YamlScalar && key.content == "<<"

    private fun doMerges(original: Map<YamlScalar, YamlNode>, others: List<YamlNode>): Map<YamlScalar, YamlNode> {
        val merged = mutableMapOf<YamlScalar, YamlNode>()

        original
            .filterNot { (key, _) -> isMerge(key) }
            .forEach { (key, value) -> merged.put(key, value) }

        others
            .forEach { other ->
                when (other) {
                    is YamlNull -> throw MalformedYamlException("Cannot merge a null value into a map.", other.path)
                    is YamlScalar -> throw MalformedYamlException("Cannot merge a scalar value into a map.", other.path)
                    is YamlList -> throw MalformedYamlException("Cannot merge a list value into a map.", other.path)
                    is YamlTaggedNode -> throw MalformedYamlException("Cannot merge a tagged value into a map.", other.path)
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

    private fun readAlias(event: AliasEvent, path: YamlPath): YamlNode {
        val anchor = event.anchor.get()

        val resolvedNode = aliases.getOrElse(anchor) {
            throw UnknownAnchorException(anchor.value, path.withError(event.location))
        }

        return resolvedNode.withPath(path.withAliasReference(anchor.value, event.location).withAliasDefinition(anchor.value, resolvedNode.location))
    }

    private fun <T> Iterable<T>.second(): T = this.drop(1).first()

    private val Event.location: Location
        get() = Location(startMark.get().line + 1, startMark.get().column + 1)
}
