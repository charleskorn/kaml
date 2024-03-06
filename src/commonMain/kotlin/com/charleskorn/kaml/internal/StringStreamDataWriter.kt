package com.charleskorn.kaml.internal

import okio.Buffer
import okio.Source
import org.snakeyaml.engine.v2.api.StreamDataWriter

internal class StringStreamDataWriter(
    private val buffer: Buffer = Buffer(),
) : StreamDataWriter, Source by buffer {
    override fun flush(): Unit = buffer.flush()

    override fun write(str: String) {
        buffer.writeUtf8(str)
    }

    override fun write(str: String, off: Int, len: Int) {
        buffer.writeUtf8(string = str, beginIndex = off, endIndex = off + len)
    }
}
