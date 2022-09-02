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

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule

internal class YamlContextualInput(node: YamlNode, context: SerializersModule, configuration: YamlConfiguration) : YamlInput(node, context, configuration) {
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = throw IllegalStateException("Must call beginStructure() and use returned Decoder")
    override fun decodeValue(): Any = throw IllegalStateException("Must call beginStructure() and use returned Decoder")

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        createFor(node, serializersModule, configuration, descriptor)

    override fun getCurrentLocation(): Location = node.location
    override fun getCurrentPath(): YamlPath = node.path
}
