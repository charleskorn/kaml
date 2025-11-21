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

package com.charleskorn.kaml

import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.events.AliasEvent
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.events.MappingStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.NodeEvent
import it.krzeminski.snakeyaml.engine.kmp.events.ScalarEvent
import it.krzeminski.snakeyaml.engine.kmp.events.SequenceStartEvent

internal class YamlNodeReader(
    private val parser: YamlParser,
    private val extensionDefinitionPrefix: String? = null,
    private val maxAliasCount: UInt? = 0u,
) {
    private val aliases = mutableMapOf<Anchor, WeightedNode>()
    private var aliasCount = 0u

    fun read(): YamlNode = readNode(YamlPath.root).node

    private fun readNode(path: YamlPath): WeightedNode = readNodeAndAnchor(path).first

    private fun readNodeAndAnchor(path: YamlPath): Pair<WeightedNode, Anchor?> {
        val event = parser.consumeEvent(path)
        val (node, weight) = readFromEvent(event, path)

        if (event is NodeEvent) {
            if (event !is AliasEvent) {
                event.anchor?.let {
                    if (maxAliasCount == 0u) {
                        throw ForbiddenAnchorOrAliasException("Parsing anchors and aliases is disabled.", path)
                    }

                    val anchor = node.withPath(YamlPath.forAliasDefinition(it.value, event.location))
                    aliases[it] = WeightedNode(anchor, weight)
                }
            }

            return WeightedNode(node, weight) to event.anchor
        }

        return WeightedNode(node, weight = 0u) to null
    }

    private fun readFromEvent(event: Event, path: YamlPath): WeightedNode = when (event) {
        is ScalarEvent -> WeightedNode(readScalarOrNull(event, path).maybeToTaggedNode(event.tag), weight = 0u)
        is SequenceStartEvent -> readSequence(path).let { it.copy(node = it.node.maybeToTaggedNode(event.tag)) }
        is MappingStartEvent -> readMapping(path).let { it.copy(node = it.node.maybeToTaggedNode(event.tag)) }
        is AliasEvent -> readAlias(event, path)
        else -> throw MalformedYamlException("Unexpected ${event.eventId}", path.withError(event.location))
    }

    private fun readScalarOrNull(event: ScalarEvent, path: YamlPath): YamlNode {
        if ((event.value == "null" || event.value == "" || event.value == "~") && event.plain) {
            return YamlNull(path)
        } else {
            return YamlScalar(event.value, path, YamlNodeScalarStyle.fromScalarStyle(event.scalarStyle))
        }
    }

    private fun readSequence(path: YamlPath): WeightedNode {
        val items = mutableListOf<YamlNode>()
        var sequenceWeight = 0u

        while (true) {
            val event = parser.peekEvent(path)

            when (event.eventId) {
                Event.ID.SequenceEnd -> {
                    parser.consumeEventOfType(Event.ID.SequenceEnd, path)
                    return WeightedNode(YamlList(items, path), sequenceWeight)
                }

                else -> {
                    val (node, weight) = readNode(path.withListEntry(items.size, event.location))
                    sequenceWeight += weight
                    items += node
                }
            }
        }
    }

    private fun readMapping(path: YamlPath): WeightedNode {
        val items = mutableMapOf<YamlScalar, YamlNode>()
        var mapWeight = 0u

        while (true) {
            val event = parser.peekEvent(path)

            when (event.eventId) {
                Event.ID.MappingEnd -> {
                    parser.consumeEventOfType(Event.ID.MappingEnd, path)
                    return WeightedNode(YamlMap(doMerges(items), path), mapWeight)
                }

                else -> {
                    val keyLocation = parser.peekEvent(path).location
                    val key = readMapKey(path)
                    val keyNode = YamlScalar(key, path.withMapElementKey(key, keyLocation), YamlNodeScalarStyle.PLAIN)

                    val valueLocation = parser.peekEvent(keyNode.path).location
                    val valuePath = if (isMerge(keyNode)) path.withMerge(valueLocation) else keyNode.path.withMapElementValue(valueLocation)
                    val (weightedNode, anchor) = readNodeAndAnchor(valuePath)
                    mapWeight += weightedNode.weight

                    if (path == YamlPath.root && extensionDefinitionPrefix != null && key.startsWith(extensionDefinitionPrefix)) {
                        if (anchor == null) {
                            throw NoAnchorForExtensionException(key, extensionDefinitionPrefix, path.withError(event.location))
                        }
                    } else {
                        items += (keyNode to weightedNode.node)
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
                val isNullKey = (scalarEvent.value == "null" || scalarEvent.value == "~") && scalarEvent.plain

                if (scalarEvent.tag != null || isNullKey) {
                    throw nonScalarMapKeyException(path, event)
                }

                return scalarEvent.value
            }
            else -> throw nonScalarMapKeyException(path, event)
        }
    }

    private fun nonScalarMapKeyException(path: YamlPath, event: Event) = MalformedYamlException("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes.)", path.withError(event.location))

    private fun YamlNode.maybeToTaggedNode(tag: String?): YamlNode =
        tag?.let { YamlTaggedNode(it, this) } ?: this

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

    private fun readAlias(event: AliasEvent, path: YamlPath): WeightedNode {
        if (maxAliasCount == 0u) {
            throw ForbiddenAnchorOrAliasException("Parsing anchors and aliases is disabled.", path)
        }

        val anchor = event.alias

        val (resolvedNode, resolvedNodeWeight) = aliases.getOrElse(anchor) {
            throw UnknownAnchorException(anchor.value, path.withError(event.location))
        }

        val resultWeight = resolvedNodeWeight + 1u
        aliasCount += resultWeight

        if ((maxAliasCount != null) && (aliasCount > maxAliasCount)) {
            throw ForbiddenAnchorOrAliasException(
                "Maximum number of aliases has been reached.",
                path,
            )
        }

        return WeightedNode(
            node = resolvedNode.withPath(
                path.withAliasReference(anchor.value, event.location)
                    .withAliasDefinition(anchor.value, resolvedNode.location),
            ),
            weight = resultWeight,
        )
    }

    private fun <T> Iterable<T>.second(): T = this.drop(1).first()

    private val Event.location: Location
        get() = Location(startMark!!.line + 1, startMark!!.column + 1)
}

private data class WeightedNode(
    val node: YamlNode,
    val weight: UInt,
)
