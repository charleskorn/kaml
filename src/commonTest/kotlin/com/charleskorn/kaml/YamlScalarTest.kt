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

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.floats.shouldBeNaN
import io.kotest.matchers.shouldBe

class YamlScalarTest : FunSpec({
    val index1Line2Column3Path = YamlPath.root.withListEntry(1, Location(2, 3))
    val index1Line2Column4Path = YamlPath.root.withListEntry(1, Location(2, 4))

    withData(
        nameFn = { "validNumberScalar_$it" },
        "0" to 0,
        "1" to 1,
        "-1" to -1,
        "0x11" to 17,
        "-0x11" to -17,
        "0o11" to 9,
        "-0o11" to -9,
    ) { (content, expectedValue) ->
        val scalar = YamlScalar(content, YamlPath.root)

        scalar.toInt() shouldBe expectedValue
        scalar.toLong() shouldBe expectedValue.toLong()
        scalar.toShort() shouldBe expectedValue.toShort()
        scalar.toByte() shouldBe expectedValue.toByte()
    }

    withData(
        nameFn = { "invalidNumberScalar_$it" },
        "a",
        ".",
        "1.",
        ".1",
        "1.5",
        "+",
        "0x",
        "0o",
        "",
    ) { content ->
        val scalar = YamlScalar(content, index1Line2Column4Path)

        val intException = shouldThrow<YamlScalarFormatException>(scalar::toInt)

        intException.asClue {
            it.message shouldBe "Value '$content' is not a valid integer value."
            it.line shouldBe 2
            it.column shouldBe 4
            it.path shouldBe index1Line2Column4Path
            it.originalValue shouldBe content
        }

        val longException = shouldThrow<YamlScalarFormatException>(scalar::toLong)

        longException.asClue {
            it.message shouldBe "Value '$content' is not a valid long value."
            it.line shouldBe 2
            it.column shouldBe 4
            it.path shouldBe index1Line2Column4Path
            it.originalValue shouldBe content
        }

        val shortException = shouldThrow<YamlScalarFormatException>(scalar::toShort)

        shortException.asClue {
            it.message shouldBe "Value '$content' is not a valid short value."
            it.line shouldBe 2
            it.column shouldBe 4
            it.path shouldBe index1Line2Column4Path
            it.originalValue shouldBe content
        }

        val byteException = shouldThrow<YamlScalarFormatException>(scalar::toByte)

        byteException.asClue {
            it.message shouldBe "Value '$content' is not a valid byte value."
            it.line shouldBe 2
            it.column shouldBe 4
            it.path shouldBe index1Line2Column4Path
            it.originalValue shouldBe content
        }
    }

    withData(
        nameFn = { "validDoubleScalar_$it" },
        "1" to 1.0,
        ".5" to 0.5,
        "1.5" to 1.5,
        "1.5e2" to 150.0,
        "1.5E2" to 150.0,
        "1.5e+2" to 150.0,
        "1.5e-2" to 0.015,
        "-1.5e2" to -150.0,
        "-1.5e+2" to -150.0,
        "-1.5e-2" to -0.015,
        ".inf" to Double.POSITIVE_INFINITY,
        ".Inf" to Double.POSITIVE_INFINITY,
        ".INF" to Double.POSITIVE_INFINITY,
        "-.inf" to Double.NEGATIVE_INFINITY,
        "-.Inf" to Double.NEGATIVE_INFINITY,
        "-.INF" to Double.NEGATIVE_INFINITY,
    ) { (content, expectedResult) ->
        val scalar = YamlScalar(content, index1Line2Column4Path)
        scalar.toDouble() shouldBe expectedResult
    }

    withData(
        nameFn = { "validDoubleScalar_$it" },
        ".nan",
        ".NaN",
        ".NAN",
    ) { content ->
        val scalar = YamlScalar(content, index1Line2Column4Path)
        scalar.toDouble().shouldBeNaN()
    }

    withData(
        nameFn = { "validFloatScalar_$it" },
        "1" to 1.0f,
        ".5" to 0.5f,
        "1.5" to 1.5f,
        "1.5e2" to 150f,
        "1.5E2" to 150f,
        "1.5e+2" to 150f,
        "1.5e-2" to 0.015f,
        "-1.5e2" to -150f,
        "-1.5e+2" to -150f,
        "-1.5e-2" to -0.015f,
        ".inf" to Float.POSITIVE_INFINITY,
        ".Inf" to Float.POSITIVE_INFINITY,
        ".INF" to Float.POSITIVE_INFINITY,
        "-.inf" to Float.NEGATIVE_INFINITY,
        "-.Inf" to Float.NEGATIVE_INFINITY,
        "-.INF" to Float.NEGATIVE_INFINITY,
    ) { (content, expectedResult) ->
        val scalar = YamlScalar(content, index1Line2Column4Path)
        scalar.toFloat() shouldBe expectedResult
    }

    withData(
        nameFn = { "validFloatScalar_$it" },
        ".nan",
        ".NaN",
        ".NAN",
    ) { content ->
        val scalar = YamlScalar(content, index1Line2Column4Path)
        scalar.toFloat().shouldBeNaN()
    }

    withData(
        nameFn = { "invalidFloatScalar_$it" },
        ".",
        // "0x2",
        // "0o2",
        "1e",
        "1e-",
        "1e+",
        "+",
        "",
    ) { content ->
        val scalar = YamlScalar(content, index1Line2Column4Path)

        val floatException = shouldThrow<YamlScalarFormatException>(scalar::toFloat)

        floatException.asClue {
            it.message shouldBe "Value '$content' is not a valid floating point value."
            it.line shouldBe 2
            it.column shouldBe 4
            it.path shouldBe index1Line2Column4Path
            it.originalValue shouldBe content
        }

        val doubleException = shouldThrow<YamlScalarFormatException>(scalar::toDouble)

        doubleException.asClue {
            it.message shouldBe "Value '$content' is not a valid floating point value."
            it.line shouldBe 2
            it.column shouldBe 4
            it.path shouldBe index1Line2Column4Path
            it.originalValue shouldBe content
        }
    }

    withData(
        nameFn = { "validBooleanScalar_$it" },
        "true" to true,
        "True" to true,
        "TRUE" to true,
        "false" to false,
        "False" to false,
        "FALSE" to false,
    ) { (content, expectedValue) ->
        val scalar = YamlScalar(content, index1Line2Column4Path)
        scalar.toBoolean() shouldBe expectedValue
    }

    test("Throws appropriate exception when retrieving boolean from scalar with 'nonsense' content") {
        val scalar = YamlScalar("nonsense", index1Line2Column4Path)

        val exception = shouldThrow<YamlScalarFormatException>(scalar::toBoolean)

        exception.asClue {
            it.message shouldBe "Value 'nonsense' is not a valid boolean, permitted choices are: true or false"
            it.line shouldBe 2
            it.column shouldBe 4
            it.path shouldBe index1Line2Column4Path
            it.originalValue shouldBe "nonsense"
        }
    }

    test("Successfully retrieves character value from a scalar with content 'b'") {
        val scalar = YamlScalar("b", index1Line2Column4Path)
        val result = scalar.toChar()
        result shouldBe 'b'
    }

    withData(
        nameFn = { "invalidCharacterScalar_$it" },
        "aa",
        "",
    ) { content ->
        val scalar = YamlScalar(content, index1Line2Column4Path)

        val exception = shouldThrow<YamlScalarFormatException>(scalar::toChar)

        exception.asClue {
            it.message shouldBe "Value '$content' is not a valid character value."
            it.line shouldBe 2
            it.column shouldBe 4
            it.path shouldBe index1Line2Column4Path
            it.originalValue shouldBe content
        }
    }

    test("Scalar is equivalent when compared to same instance") {
        val scalar = YamlScalar("some content", index1Line2Column3Path)
        scalar.equivalentContentTo(scalar) shouldBe true
    }

    test("Scalar is equivalent when compared to another scalar with same content and path") {
        val scalar = YamlScalar("some content", index1Line2Column3Path)
        scalar.equivalentContentTo(YamlScalar("some content", index1Line2Column3Path)) shouldBe true
    }

    test("Scalar is equivalent when compared to another scalar with same content but different path") {
        val scalar = YamlScalar("some content", index1Line2Column3Path)
        val otherPath = YamlPath.root.withListEntry(1, Location(2, 4))
        scalar.equivalentContentTo(YamlScalar("some content", otherPath)) shouldBe true
    }

    test("Scalar is not equivalent when compared to another scalar with same path but different content") {
        val scalar = YamlScalar("some content", index1Line2Column3Path)
        scalar.equivalentContentTo(YamlScalar("some other content", index1Line2Column3Path)) shouldBe false
    }

    test("Scalar is not equivalent when compared to a null value") {
        val scalar = YamlScalar("some content", index1Line2Column3Path)
        scalar.equivalentContentTo(YamlNull(index1Line2Column3Path)) shouldBe false
    }

    test("Scalar is not equivalent when compared to a list") {
        val scalar = YamlScalar("some content", index1Line2Column3Path)
        scalar.equivalentContentTo(YamlList(emptyList(), index1Line2Column3Path)) shouldBe false
    }

    test("Scalar is not equivalent when compared to a map") {
        val scalar = YamlScalar("some content", index1Line2Column3Path)
        scalar.equivalentContentTo(YamlMap(emptyMap(), index1Line2Column3Path)) shouldBe false
    }

    test("Scalar content conversion to a human-readable string returns content in single quotes") {
        YamlScalar("thing", YamlPath.root).contentToString() shouldBe "'thing'"
    }

    test("Replacing scalar path returns a scalar with the new provided path") {
        val original = YamlScalar("abc123", YamlPath.root)
        val newPath = YamlPath.forAliasDefinition("blah", Location(2, 3))
        original.withPath(newPath) shouldBe YamlScalar("abc123", newPath)
    }

    test("Scalar conversion to string returns a human-readable description of itself") {
        val index2Line3Column4Path = YamlPath.root.withListEntry(2, Location(3, 4))
        val value = YamlScalar("hello world", index2Line3Column4Path)
        value.toString() shouldBe "scalar @ $index2Line3Column4Path : hello world"
    }
})
