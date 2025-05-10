package com.charleskorn.kaml

import com.charleskorn.kaml.testobjects.Shape
import com.charleskorn.kaml.testobjects.Shapes
import com.charleskorn.kaml.testobjects.SimpleStructure
import com.charleskorn.kaml.testobjects.UppercasedValueSimpleStructure
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class YamlTransformingSerializerTest : FunSpec({
    val yaml = Yaml(
        configuration = YamlConfiguration(
            polymorphismStyle = PolymorphismStyle.None
        )
    )

    context("no-op default implementation") {
        val objToSerialize = SimpleStructure(
            name = "kAml"
        )

        test("serialize with no transformation") {
            val output = yaml.encodeToString(objToSerialize)
            output shouldBe "name: \"kAml\""
        }

        test("deserialize with no transformation") {
            val inputYaml = "name: \"kaml\""
            val result = yaml.decodeFromString<SimpleStructure>(inputYaml)
            result.name shouldBe "kaml"
        }
    }

    context("simple value transformer") {
        val objToSerialize = UppercasedValueSimpleStructure(
            name = "kAml"
        )

        test("serialize should uppercase the string in YAML") {
            val output = yaml.encodeToString(objToSerialize)
            output shouldBe "name: \"KAML\""
        }

        test("deserialize should return uppercased string") {
            val inputYaml = "name: \"kaml\""
            val result = yaml.decodeFromString<UppercasedValueSimpleStructure>(inputYaml)
            result.name shouldBe "KAML"
        }
    }

    context("structure transformer") {
        val shapes = Shapes(
            first = Shape.Rectangle(0.5, 1.0),
            rest =
                listOf(
                    Shape.Circle(1.0),
                    Shape.Circle(2.0),
                    Shape.Rectangle(1.5, 1.0),
                )
        )

        test("serialize should wrap all shapes into list") {
            val output = yaml.encodeToString(shapes)
            val expectedYaml =
                """- "a": 0.5
  "b": 1.0
- "diameter": 1.0
- "diameter": 2.0
- "a": 1.5
  "b": 1.0""".trimIndent()
            output shouldBe expectedYaml
        }
    }
})
