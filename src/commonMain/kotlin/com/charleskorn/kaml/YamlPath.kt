/*

   Copyright 2018-2020 Charles Korn.

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

public data class YamlPath(val segments: List<YamlPathSegment>) {
    public constructor(vararg segments: YamlPathSegment) : this(segments.toList())

    init {
        if (segments.isEmpty()) {
            throw IllegalArgumentException("Path must contain at least one segment.")
        }

        if (segments.first() !is YamlPathSegment.Root && segments.first() !is YamlPathSegment.AliasDefinition) {
            throw IllegalArgumentException("First element of path must be root segment or alias definition.")
        }

        if (segments.drop(1).contains(YamlPathSegment.Root)) {
            throw IllegalArgumentException("Root segment can only be first element of path.")
        }
    }

    val endLocation: Location = segments.last().location

    public fun withError(location: Location): YamlPath = withSegment(YamlPathSegment.Error(location))
    public fun withListEntry(index: Int, location: Location): YamlPath = withSegment(YamlPathSegment.ListEntry(index, location))
    public fun withMapElementKey(key: String, location: Location): YamlPath = withSegment(YamlPathSegment.MapElementKey(key, location))
    public fun withMapElementValue(location: Location): YamlPath = withSegment(YamlPathSegment.MapElementValue(location))
    public fun withAliasReference(name: String, location: Location): YamlPath = withSegment(YamlPathSegment.AliasReference(name, location))
    public fun withAliasDefinition(name: String, location: Location): YamlPath = withSegment(YamlPathSegment.AliasDefinition(name, location))
    public fun withMerge(location: Location): YamlPath = withSegment(YamlPathSegment.Merge(location))
    private fun withSegment(segment: YamlPathSegment): YamlPath = YamlPath(segments + segment)

    public fun toHumanReadableString(): String {
        val builder = StringBuilder()

        var nextSegmentIndex = 1

        while (nextSegmentIndex <= segments.lastIndex) {
            val segmentIndex = nextSegmentIndex
            nextSegmentIndex++

            when (val segment = segments[segmentIndex]) {
                is YamlPathSegment.ListEntry -> {
                    builder.append('[')
                    builder.append(segment.index)
                    builder.append(']')
                }
                is YamlPathSegment.MapElementKey -> {
                    if (builder.isNotEmpty()) {
                        builder.append('.')
                    }

                    builder.append(segment.key)
                }
                is YamlPathSegment.AliasReference -> {
                    builder.append("->&")
                    builder.append(segment.name)
                }
                is YamlPathSegment.Merge -> {
                    builder.append(">>(merged")

                    if (nextSegmentIndex <= segments.lastIndex && segments[nextSegmentIndex] is YamlPathSegment.ListEntry) {
                        builder.append(" entry ")
                        builder.append((segments[nextSegmentIndex] as YamlPathSegment.ListEntry).index)
                        nextSegmentIndex++
                    }

                    if (nextSegmentIndex <= segments.lastIndex && segments[nextSegmentIndex] is YamlPathSegment.AliasReference) {
                        builder.append(" &")
                        builder.append((segments[nextSegmentIndex] as YamlPathSegment.AliasReference).name)
                        nextSegmentIndex++
                    }

                    builder.append(")")
                }
                is YamlPathSegment.Root, is YamlPathSegment.Error, is YamlPathSegment.MapElementValue, is YamlPathSegment.AliasDefinition -> {
                    // Nothing to do.
                }
            }
        }

        if (builder.isNotEmpty()) {
            return builder.toString()
        }

        return "<root>"
    }

    public companion object {
        public val root: YamlPath = YamlPath(YamlPathSegment.Root)
        public fun forAliasDefinition(name: String, location: Location): YamlPath = YamlPath(YamlPathSegment.AliasDefinition(name, location))
    }
}

public sealed class YamlPathSegment(public open val location: Location) {
    public object Root : YamlPathSegment(Location(1, 1))
    public data class ListEntry(val index: Int, override val location: Location) : YamlPathSegment(location)
    public data class MapElementKey(val key: String, override val location: Location) : YamlPathSegment(location)
    public data class MapElementValue(override val location: Location) : YamlPathSegment(location)
    public data class AliasReference(val name: String, override val location: Location) : YamlPathSegment(location)
    public data class AliasDefinition(val name: String, override val location: Location) : YamlPathSegment(location)
    public data class Merge(override val location: Location) : YamlPathSegment(location)
    public data class Error(override val location: Location) : YamlPathSegment(location)
}
