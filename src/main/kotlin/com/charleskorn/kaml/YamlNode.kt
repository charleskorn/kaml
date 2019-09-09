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

sealed class YamlNode(open val location: Location) {
    abstract fun equivalentContentTo(other: YamlNode): Boolean
    abstract fun contentToString(): String
}

data class YamlScalar(val content: String, override val location: Location) : YamlNode(location) {
    override fun equivalentContentTo(other: YamlNode): Boolean = other is YamlScalar && this.content == other.content
    override fun contentToString(): String = "'$content'"

    fun toByte() = convertToIntegerLikeValue(String::toByte, "byte")
    fun toShort() = convertToIntegerLikeValue(String::toShort, "short")
    fun toInt() = convertToIntegerLikeValue(String::toInt, "integer")
    fun toLong() = convertToIntegerLikeValue(String::toLong, "long")

    private fun <T> convertToIntegerLikeValue(converter: (String, Int) -> T, description: String): T {
        try {
            return when {
                content.startsWith("0x") -> converter(content.substring(2), 16)
                content.startsWith("-0x") -> converter("-" + content.substring(3), 16)
                content.startsWith("0o") -> converter(content.substring(2), 8)
                content.startsWith("-0o") -> converter("-" + content.substring(3), 8)
                else -> converter(content, 10)
            }
        } catch (e: NumberFormatException) {
            throw YamlScalarFormatException("Value '$content' is not a valid $description value.", location, content)
        }
    }

    fun toFloat(): Float {
        return when (content) {
            ".inf", ".Inf", ".INF" -> Float.POSITIVE_INFINITY
            "-.inf", "-.Inf", "-.INF" -> Float.NEGATIVE_INFINITY
            ".nan", ".NaN", ".NAN" -> Float.NaN
            else -> try {
                content.toFloat()
            } catch (e: NumberFormatException) {
                throw YamlScalarFormatException("Value '$content' is not a valid floating point value.", location, content)
            }
        }
    }

    fun toDouble(): Double {
        return when (content) {
            ".inf", ".Inf", ".INF" -> Double.POSITIVE_INFINITY
            "-.inf", "-.Inf", "-.INF" -> Double.NEGATIVE_INFINITY
            ".nan", ".NaN", ".NAN" -> Double.NaN
            else -> try {
                content.toDouble()
            } catch (e: NumberFormatException) {
                throw YamlScalarFormatException("Value '$content' is not a valid floating point value.", location, content)
            }
        }
    }

    fun toBoolean(): Boolean {
        return when (content) {
            "true", "True", "TRUE" -> true
            "false", "False", "FALSE" -> false
            else -> throw YamlScalarFormatException("Value '$content' is not a valid boolean, permitted choices are: true or false", location, content)
        }
    }

    fun toChar(): Char =
        content.singleOrNull() ?: throw YamlScalarFormatException("Value '$content' is not a valid character value.", location, content)
}

data class YamlNull(override val location: Location) : YamlNode(location) {
    override fun equivalentContentTo(other: YamlNode): Boolean = other is YamlNull
    override fun contentToString(): String = "null"
}

data class YamlList(val items: List<YamlNode>, override val location: Location) : YamlNode(location) {
    override fun equivalentContentTo(other: YamlNode): Boolean {
        if (other !is YamlList) {
            return false
        }

        if (this.items.size != other.items.size) {
            return false
        }

        return this.items.zip(other.items).all { (mine, theirs) -> mine.equivalentContentTo(theirs) }
    }

    override fun contentToString(): String = "[" + items.joinToString(", ") { it.contentToString() } + "]"
}

data class YamlMap(val entries: Map<YamlNode, YamlNode>, override val location: Location) : YamlNode(location) {
    init {
        val keys = entries.keys.sortedWith(Comparator { a, b ->
            val lineComparison = a.location.line.compareTo(b.location.line)

            if (lineComparison != 0) {
                lineComparison
            } else {
                a.location.column.compareTo(b.location.column)
            }
        })

        keys.forEachIndexed { index, key ->
            val duplicate = keys.subList(0, index).firstOrNull { it.equivalentContentTo(key) }

            if (duplicate != null) {
                throw DuplicateKeyException(duplicate.location, key.location, key.contentToString())
            }
        }
    }

    override fun equivalentContentTo(other: YamlNode): Boolean {
        if (other !is YamlMap) {
            return false
        }

        if (this.entries.size != other.entries.size) {
            return false
        }

        return this.entries.all { (thisKey, thisValue) ->
            other.entries.any { it.key.equivalentContentTo(thisKey) && it.value.equivalentContentTo(thisValue) }
        }
    }

    override fun contentToString(): String =
        "{" + entries.map { (key, value) -> "${key.contentToString()}: ${value.contentToString()}" }.joinToString(", ") + "}"
}

data class YamlTaggedNode(val tag: String, val node: YamlNode) : YamlNode(node.location) {
    override fun equivalentContentTo(other: YamlNode): Boolean {
        if (other !is YamlTaggedNode) {
            return false
        }

        if (tag != other.tag) {
            return false
        }

        return node.equivalentContentTo(other.node)
    }

    override fun contentToString(): String = "!$tag ${node.contentToString()}"
}
