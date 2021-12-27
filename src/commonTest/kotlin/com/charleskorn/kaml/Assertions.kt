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

import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.logic._logic
import kotlin.jvm.JvmName

fun <T : YamlException> Expect<T>.path(assertionCreator: Expect<YamlPath>.() -> Unit) {
    feature(YamlException::path)._logic.appendAsGroup(assertionCreator)
}

fun <T : YamlException> Expect<T>.line(assertionCreator: Expect<Int>.() -> Unit) {
    feature(YamlException::line)._logic.appendAsGroup(assertionCreator)
}

fun <T : YamlException> Expect<T>.column(assertionCreator: Expect<Int>.() -> Unit) {
    feature(YamlException::column)._logic.appendAsGroup(assertionCreator)
}

fun Expect<DuplicateKeyException>.originalLocation(assertionCreator: Expect<Location>.() -> Unit) {
    feature(DuplicateKeyException::originalLocation)._logic.appendAsGroup(assertionCreator)
}

fun Expect<DuplicateKeyException>.duplicateLocation(assertionCreator: Expect<Location>.() -> Unit) {
    feature(DuplicateKeyException::duplicateLocation)._logic.appendAsGroup(assertionCreator)
}

fun Expect<DuplicateKeyException>.originalPath(assertionCreator: Expect<YamlPath>.() -> Unit) {
    feature(DuplicateKeyException::originalPath)._logic.appendAsGroup(assertionCreator)
}

fun Expect<DuplicateKeyException>.duplicatePath(assertionCreator: Expect<YamlPath>.() -> Unit) {
    feature(DuplicateKeyException::duplicatePath)._logic.appendAsGroup(assertionCreator)
}

fun Expect<DuplicateKeyException>.key(assertionCreator: Expect<String>.() -> Unit) {
    feature(DuplicateKeyException::key)._logic.appendAsGroup(assertionCreator)
}

@JvmName("MissingRequiredPropertyExceptionPropertyName")
fun Expect<MissingRequiredPropertyException>.propertyName(assertionCreator: Expect<String>.() -> Unit) {
    feature(MissingRequiredPropertyException::propertyName)._logic.appendAsGroup(assertionCreator)
}

@JvmName("InvalidPropertyValueExceptionPropertyName")
fun Expect<InvalidPropertyValueException>.propertyName(assertionCreator: Expect<String>.() -> Unit) {
    feature(InvalidPropertyValueException::propertyName)._logic.appendAsGroup(assertionCreator)
}

fun Expect<InvalidPropertyValueException>.reason(assertionCreator: Expect<String>.() -> Unit) {
    feature(InvalidPropertyValueException::reason)._logic.appendAsGroup(assertionCreator)
}

@JvmName("UnknownPropertyExceptionPropertyName")
fun Expect<UnknownPropertyException>.propertyName(assertionCreator: Expect<String>.() -> Unit) {
    feature(UnknownPropertyException::propertyName)._logic.appendAsGroup(assertionCreator)
}

fun Expect<UnknownPropertyException>.validPropertyNames(assertionCreator: Expect<Set<String>>.() -> Unit) {
    feature(UnknownPropertyException::validPropertyNames)._logic.appendAsGroup(assertionCreator)
}

fun Expect<YamlScalarFormatException>.originalValue(assertionCreator: Expect<String>.() -> Unit) {
    feature(YamlScalarFormatException::originalValue)._logic.appendAsGroup(assertionCreator)
}

fun Expect<UnknownPolymorphicTypeException>.typeName(assertionCreator: Expect<String>.() -> Unit) {
    feature(UnknownPolymorphicTypeException::typeName)._logic.appendAsGroup(assertionCreator)
}

fun Expect<UnknownPolymorphicTypeException>.validTypeNames(assertionCreator: Expect<Set<String>>.() -> Unit) {
    feature(UnknownPolymorphicTypeException::validTypeNames)._logic.appendAsGroup(assertionCreator)
}
