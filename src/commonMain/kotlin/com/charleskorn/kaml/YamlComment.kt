package com.charleskorn.kaml

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Adds a comment block before property on serialization
 * @param comment comment to add. Multiline commment indent will be trimmed automatically
 */
@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@SerialInfo
public annotation class YamlComment(
    val comment: String
)
