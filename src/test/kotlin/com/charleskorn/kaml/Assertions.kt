/*

   Copyright 2018-2019 Charles Korn.

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

fun <T : YamlException> Expect<T>.line(assertionCreator: Expect<Int>.() -> Unit) {
    feature { f(it::line) }.addAssertionsCreatedBy(assertionCreator)
}

fun <T : YamlException> Expect<T>.column(assertionCreator: Expect<Int>.() -> Unit) {
    feature { f(it::column) }.addAssertionsCreatedBy(assertionCreator)
}

fun Expect<DuplicateKeyException>.originalLocation(assertionCreator: Expect<Location>.() -> Unit) {
    feature { f(it::originalLocation) }.addAssertionsCreatedBy(assertionCreator)
}

fun Expect<DuplicateKeyException>.duplicateLocation(assertionCreator: Expect<Location>.() -> Unit) {
    feature { f(it::duplicateLocation) }.addAssertionsCreatedBy(assertionCreator)
}

fun Expect<DuplicateKeyException>.key(assertionCreator: Expect<String>.() -> Unit) {
    feature { f(it::key) }.addAssertionsCreatedBy(assertionCreator)
}

@JvmName("InvalidPropertyValueExceptionPropertyName")
fun Expect<InvalidPropertyValueException>.propertyName(assertionCreator: Expect<String>.() -> Unit) {
    feature { f(it::propertyName) }.addAssertionsCreatedBy(assertionCreator)
}

fun Expect<InvalidPropertyValueException>.reason(assertionCreator: Expect<String>.() -> Unit) {
    feature { f(it::reason) }.addAssertionsCreatedBy(assertionCreator)
}

@JvmName("UnknownPropertyExceptionPropertyName")
fun Expect<UnknownPropertyException>.propertyName(assertionCreator: Expect<String>.() -> Unit) {
    feature { f(it::propertyName) }.addAssertionsCreatedBy(assertionCreator)
}

fun Expect<UnknownPropertyException>.validPropertyNames(assertionCreator: Expect<Set<String>>.() -> Unit) {
    feature { f(it::validPropertyNames) }.addAssertionsCreatedBy(assertionCreator)
}

fun Expect<YamlScalarFormatException>.originalValue(assertionCreator: Expect<String>.() -> Unit) {
    feature { f(it::originalValue) }.addAssertionsCreatedBy(assertionCreator)
}
