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

package com.charleskorn.kaml.internal

import okio.Buffer
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8

/**
 * Convert a [String] to a [BufferedSource].
 *
 * The string _must_ be encoded with UTF-8.
 */
// https://github.com/square/okio/issues/774#issuecomment-703315013
internal fun String.bufferedSource(): BufferedSource = Buffer().write(encodeUtf8())
