import kotlinx.serialization.AbstractSerialFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decode

object YAML : AbstractSerialFormat(), StringFormat {
    override fun <T> parse(serializer: DeserializationStrategy<T>, string: String): T {
        val parser = YamlParser(string)
        val rootNode = YamlNode.fromParser(parser)
        val input = YamlInput.createFor(rootNode)
        return input.decode(serializer)
    }

    override fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String {
        TODO("not implemented")
    }
}
