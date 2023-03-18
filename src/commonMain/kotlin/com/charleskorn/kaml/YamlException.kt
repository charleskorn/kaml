/*

   Copyright 2018-2021 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package com.charleskorn.kaml

import kotlinx.serialization.SerializationException

public open class YamlException(
    override val message: String,
    public val path: YamlPath,
    override val cause: Throwable? = null,
) : SerializationException(message, cause) {
    public val location: Location = path.endLocation
    public val line: Int = location.line
    public val column: Int = location.column

    override fun toString(): String = "${this::class.qualifiedName} at ${path.toHumanReadableString()} on line $line, column $column: $message"
}

public class DuplicateKeyException(
    public val originalPath: YamlPath,
    public val duplicatePath: YamlPath,
    public val key: String,
) :
    YamlException("Duplicate key $key. It was previously given at line ${originalPath.endLocation.line}, column ${originalPath.endLocation.column}.", duplicatePath) {

    public val originalLocation: Location = originalPath.endLocation
    public val duplicateLocation: Location = duplicatePath.endLocation
}

public class EmptyYamlDocumentException(message: String, path: YamlPath) : YamlException(message, path)

public class InvalidPropertyValueException(
    public val propertyName: String,
    public val reason: String,
    path: YamlPath,
    cause: Throwable? = null,
) : YamlException("Value for '$propertyName' is invalid: $reason", path, cause)

public class MalformedYamlException(message: String, path: YamlPath) : YamlException(message, path)

public class UnexpectedNullValueException(path: YamlPath) : YamlException("Unexpected null or empty value for non-null field.", path)

public class MissingRequiredPropertyException(
    public val propertyName: String,
    path: YamlPath,
    cause: Throwable? = null,
) :
    YamlException("Property '$propertyName' is required but it is missing.", path, cause)

public class UnknownPropertyException(
    public val propertyName: String,
    public val validPropertyNames: Set<String>,
    path: YamlPath,
) :
    YamlException("Unknown property '$propertyName'. Known properties are: ${validPropertyNames.sorted().joinToString(", ")}", path)

public class UnknownPolymorphicTypeException(
    public val typeName: String,
    public val validTypeNames: Set<String>,
    path: YamlPath,
    cause: Throwable? = null,
) :
    YamlException("Unknown type '$typeName'. Known types are: ${validTypeNames.sorted().joinToString(", ")}", path, cause)

public class YamlScalarFormatException(
    message: String,
    path: YamlPath,
    public val originalValue: String,
) : YamlException(message, path)

public open class IncorrectTypeException(message: String, path: YamlPath) : YamlException(message, path)

public class MissingTypeTagException(path: YamlPath) :
    IncorrectTypeException("Value is missing a type tag (eg. !<type>)", path)

public class UnknownAnchorException(public val anchorName: String, path: YamlPath) :
    YamlException("Unknown anchor '$anchorName'.", path)

public class NoAnchorForExtensionException(
    public val key: String,
    public val extensionDefinitionPrefix: String,
    path: YamlPath,
) :
    YamlException("The key '$key' starts with the extension definition prefix '$extensionDefinitionPrefix' but does not define an anchor.", path)

public class ForbiddenAnchorOrAliasException(message: String, path: YamlPath) : YamlException(message, path)
