package com.charleskorn.kaml.testobjects

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlTransformingSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

object UppercaseTransformingSerializer : YamlTransformingSerializer<String>(String.serializer()) {
    override fun transformSerialize(element: YamlNode): YamlNode = when (element) {
        is YamlScalar -> YamlScalar(element.content.uppercase(), element.path)
        else -> element
    }

    override fun transformDeserialize(element: YamlNode): YamlNode = when (element) {
        is YamlScalar -> YamlScalar(element.content.uppercase(), element.path)
        else -> element
    }
}

@Serializable
data class UppercasedValueSimpleStructure(
    @Serializable(with = UppercaseTransformingSerializer::class)
    val name: String,
)

object ShapeWrappingSerializer : YamlTransformingSerializer<Shapes>(Shapes.generatedSerializer()) {
    override fun transformSerialize(element: YamlNode): YamlNode {
        require(element is YamlMap)

        val firstNode = checkNotNull(element.get<YamlMap>("first"))
        val restNode = checkNotNull(element.get<YamlList>("rest"))

        val combined = listOf(firstNode) + restNode.items
        return YamlList(combined, element.path)
    }
}

@Serializable(with = ShapeWrappingSerializer::class)
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
data class Shapes(
    val first: Shape,
    val rest: List<Shape>,
)

@Serializable
sealed interface Shape {
    @Serializable
    data class Circle(val diameter: Double) : Shape

    @Serializable
    data class Rectangle(val a: Double, val b: Double) : Shape
}
