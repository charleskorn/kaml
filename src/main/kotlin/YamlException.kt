import io.dahgan.parser.Token

data class YamlException(override val message: String, val line: Int, val column: Int) : RuntimeException(message) {
    constructor(message: String, token: Token) : this(message, token.line, token.lineChar + 1)
    constructor(message: String, location: Location) : this(message, location.line, location.column)
}
