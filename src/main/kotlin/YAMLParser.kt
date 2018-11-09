import io.dahgan.parser.Code
import io.dahgan.parser.Token

class YAMLParser(tokens: Sequence<Token>) {
    // TODO: this isn't very efficient (we read the entire structure into memory rather than streaming it)
    // but it makes things quite a bit easier
    private val tokens = tokens
        .filterNot { it.code == Code.White }
        .filterNot { it.code == Code.Break }
        .toList()

    private var nextTokenIndex = 0

    init {
        tokens.forEach { it ->
            if (it.code == Code.Error) {
                throw YAMLException(it.text.toString(), it)
            }
        }

        readToken(Code.BeginDocument)
    }

    fun peekAnyToken(): Token = tokens[nextTokenIndex]

    fun peekToken(vararg expectedTypes: Code): Token {
        if (expectedTypes.isEmpty()) {
            throw IllegalStateException("Called peekToken() with no expected token types")
        }

        val nextToken = peekAnyToken()

        if (nextToken.code !in expectedTypes) {
            val explanation = if (expectedTypes.size == 1) {
                "expected ${expectedTypes.single().name}"
            } else {
                val types = expectedTypes.map { it.name }.joinToString(",")
                "expected one of $types"
            }

            throw YAMLException("Unexpected ${nextToken.code.name}, $explanation", nextToken)
        }

        return nextToken
    }

    fun readAnyToken(): Token = peekAnyToken().also { nextTokenIndex++ }
    fun readToken(vararg expectedTypes: Code): Token = peekToken(*expectedTypes).also { nextTokenIndex++ }
}
