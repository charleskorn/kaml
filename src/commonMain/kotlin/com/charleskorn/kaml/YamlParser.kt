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

import okio.Buffer
import okio.Source
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader

internal class YamlParser(reader: Source) {
    private val dummyFileName = "DUMMY_FILE_NAME"
    private val loadSettings = LoadSettings.builder().setLabel(dummyFileName).build()
    private val streamReader = StreamReader(loadSettings, reader)
    private val events = ParserImpl(loadSettings, streamReader)

    internal constructor(source: String) : this(Buffer().write(source.encodeToByteArray()))

    init {
        consumeEventOfType(Event.ID.StreamStart, YamlPath.root)

        if (peekEvent(YamlPath.root).eventId == Event.ID.StreamEnd) {
            throw EmptyYamlDocumentException("The YAML document is empty.", YamlPath.root)
        }

        consumeEventOfType(Event.ID.DocumentStart, YamlPath.root)
    }

    fun ensureEndOfStreamReached() {
        consumeEventOfType(Event.ID.DocumentEnd, YamlPath.root)
        consumeEventOfType(Event.ID.StreamEnd, YamlPath.root)
    }

    fun consumeEvent(path: YamlPath): Event = checkEvent(path) { events.next() }
    fun peekEvent(path: YamlPath): Event = checkEvent(path) { events.peekEvent() }

    fun consumeEventOfType(type: Event.ID, path: YamlPath) {
        val event = consumeEvent(path)

        if (event.eventId != type) {
            throw MalformedYamlException("Unexpected ${event.eventId}, expected $type", path.withError(Location(event.startMark!!.line, event.startMark!!.column)))
        }
    }

    private fun checkEvent(path: YamlPath, retrieve: () -> Event): Event {
        try {
            return retrieve()
        } catch (e: MarkedYamlEngineException) {
            throw translateYamlEngineException(e, path)
        }
    }

    private fun translateYamlEngineException(e: MarkedYamlEngineException, path: YamlPath): MalformedYamlException {
        return MalformedYamlException(e.message ?: "No message", path)
    }
}
