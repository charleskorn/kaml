/*

   Copyright 2018-2020 Charles Korn.

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

fun <T : YamlException> Expect<T>.path(assertionCreator: Expect<YamlPath>.() -> Unit) {
    feature(YamlException::path).addAssertionsCreatedBy(assertionCreator)
}

fun <T : YamlException> Expect<T>.line(assertionCreator: Expect<Int>.() -> Unit) {
    feature(YamlException::line).addAssertionsCreatedBy(assertionCreator)
}

fun <T : YamlException> Expect<T>.column(assertionCreator: Expect<Int>.() -> Unit) {
    feature(YamlException::column).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<DuplicateKeyException>.originalLocation(assertionCreator: Expect<Location>.() -> Unit) {
    feature(DuplicateKeyException::originalLocation).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<DuplicateKeyException>.duplicateLocation(assertionCreator: Expect<Location>.() -> Unit) {
    feature(DuplicateKeyException::duplicateLocation).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<DuplicateKeyException>.originalPath(assertionCreator: Expect<YamlPath>.() -> Unit) {
    feature(DuplicateKeyException::originalPath).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<DuplicateKeyException>.duplicatePath(assertionCreator: Expect<YamlPath>.() -> Unit) {
    feature(DuplicateKeyException::duplicatePath).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<DuplicateKeyException>.key(assertionCreator: Expect<String>.() -> Unit) {
    feature(DuplicateKeyException::key).addAssertionsCreatedBy(assertionCreator)
}

@JvmName("MissingRequiredPropertyExceptionPropertyName")
fun Expect<MissingRequiredPropertyException>.propertyName(assertionCreator: Expect<String>.() -> Unit) {
    feature(MissingRequiredPropertyException::propertyName).addAssertionsCreatedBy(assertionCreator)
}

@JvmName("InvalidPropertyValueExceptionPropertyName")
fun Expect<InvalidPropertyValueException>.propertyName(assertionCreator: Expect<String>.() -> Unit) {
    feature(InvalidPropertyValueException::propertyName).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<InvalidPropertyValueException>.reason(assertionCreator: Expect<String>.() -> Unit) {
    feature(InvalidPropertyValueException::reason).addAssertionsCreatedBy(assertionCreator)
}

@JvmName("UnknownPropertyExceptionPropertyName")
fun Expect<UnknownPropertyException>.propertyName(assertionCreator: Expect<String>.() -> Unit) {
    feature(UnknownPropertyException::propertyName).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<UnknownPropertyException>.validPropertyNames(assertionCreator: Expect<Set<String>>.() -> Unit) {
    feature(UnknownPropertyException::validPropertyNames).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<YamlScalarFormatException>.originalValue(assertionCreator: Expect<String>.() -> Unit) {
    feature(YamlScalarFormatException::originalValue).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<UnknownPolymorphicTypeException>.typeName(assertionCreator: Expect<String>.() -> Unit) {
    feature(UnknownPolymorphicTypeException::typeName).addAssertionsCreatedBy(assertionCreator)
}

fun Expect<UnknownPolymorphicTypeException>.validTypeNames(assertionCreator: Expect<Set<String>>.() -> Unit) {
    feature(UnknownPolymorphicTypeException::validTypeNames).addAssertionsCreatedBy(assertionCreator)
}
