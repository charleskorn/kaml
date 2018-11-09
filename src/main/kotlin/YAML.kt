import kotlinx.serialization.AbstractSerialFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decode
import io.dahgan.yaml

object YAML : AbstractSerialFormat(), StringFormat {
    override fun <T> parse(serializer: DeserializationStrategy<T>, string: String): T {
        val tokens = yaml().tokenize("some-name-that-should-probably-be-the-filename", string.toByteArray(), true /* TODO: what's the correct value for this? */)
        val input = YAMLInput(tokens)
        val result = input.decode(serializer)
        // TODO: check parser reached end of file
        return result
    }

    override fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String {
        TODO("not implemented")
    }
}
