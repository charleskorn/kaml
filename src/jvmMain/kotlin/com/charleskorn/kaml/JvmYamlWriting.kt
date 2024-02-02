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

import kotlinx.serialization.SerializationStrategy
import java.io.OutputStream
import java.nio.charset.Charset

public fun <T> Yaml.encodeToStream(serializer: SerializationStrategy<T>, value: T, stream: OutputStream, charset: Charset = Charsets.UTF_8): Nothing =
    TODO()

public inline fun <reified T> Yaml.encodeToStream(value: T, stream: OutputStream): Nothing =
    TODO()
