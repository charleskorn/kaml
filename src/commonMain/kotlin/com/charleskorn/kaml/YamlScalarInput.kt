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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class YamlScalarInput(val scalar: YamlScalar, yaml: Yaml, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(scalar, yaml, context, configuration) {
    override fun decodeString(): String = scalar.content
    override fun decodeInt(): Int = scalar.toInt()
    override fun decodeLong(): Long = scalar.toLong()
    override fun decodeShort(): Short = scalar.toShort()
    override fun decodeByte(): Byte = scalar.toByte()
    override fun decodeDouble(): Double = scalar.toDouble()
    override fun decodeFloat(): Float = scalar.toFloat()
    override fun decodeBoolean(): Boolean = scalar.toBoolean()
    override fun decodeChar(): Char = scalar.toChar()

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val index = enumDescriptor.getElementIndex(scalar.content)

        if (index != CompositeDecoder.UNKNOWN_NAME) {
            return index
        }

        val choices = (0..<enumDescriptor.elementsCount)
            .asSequence()
            .map { enumDescriptor.getElementName(it) }

        if (configuration.decodeEnumCaseInsensitive) {
            val idx = choices.indexOfFirst { it.equals(scalar.content, ignoreCase = true) }

            if (idx != -1) {
                return idx
            }
        }

        throw YamlScalarFormatException(
            "Value ${scalar.contentToString()} is not a valid option, permitted choices are: ${choices.sorted().joinToString(", ")}",
            scalar.path,
            scalar.content,
        )
    }

    override fun getCurrentLocation(): Location = scalar.location
    override fun getCurrentPath(): YamlPath = scalar.path

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = 0
}
