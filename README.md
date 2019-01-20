# kaml

[![Build Status](https://img.shields.io/travis/charleskorn/kaml/master.svg)](https://travis-ci.org/charleskorn/kaml)
[![Coverage](https://img.shields.io/codecov/c/github/charleskorn/kaml.svg)](https://codecov.io/gh/charleskorn/kaml)
[![License](https://img.shields.io/github/license/charleskorn/kaml.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.charleskorn.kaml/kaml.svg?label=maven%20central)](https://search.maven.org/search?q=g:%22com.charleskorn.kaml%22%20AND%20a:%22kaml%22)

## What is this?

This library adds YAML support to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/).

YAML version 1.2 is supported.

This is a very rough initial version:

* Many operations are not yet optimised for performance.
* Only the JVM is supported, Kotlin/Native support will be added in a future version.

## Usage samples

### Parsing from YAML to a Kotlin object

```kotlin
@Serializable
data class Team(
    val leader: String,
    val members: List<String>
)

val input = """
        leader: Amy
        members:
          - Bob
          - Cindy
          - Dan
    """.trimIndent()

val result = Yaml.default.parse(Team.serializer(), input)

println(result)
```

### Serializing from a Kotlin object to YAML

```kotlin
@Serializable
data class Team(
    val leader: String,
    val members: List<String>
)

val input = Team("Amy", listOf("Bob", "Cindy", "Dan"))

val result = Yaml.default.stringify(Team.serializer(), input)

println(result)
```

## Referencing kaml

Add the following to your Gradle build script:

```kotlin
implementation("com.charleskorn.kaml:kaml:<version number here>")
```

Check the [releases page](https://github.com/charleskorn/kaml/releases) for the latest release information,
and the [Maven Central page](https://search.maven.org/artifact/com.charleskorn.kaml/kaml) for examples of how
to reference the library in other build systems.

## Contributing to kaml

Pull requests and bug reports are always welcome!

kaml uses [batect](https://batect.charleskorn.com) to simplify development environment setup:

* To build the library: `./batect build`
* To run the tests and static analysis tools: `./batect check`
* To run the tests and static analysis tools continuously: `./batect continuousCheck`

Other commands are available by running `./batect --list-tasks`

## Reference links

* [YAML 1.2 Specification](http://yaml.org/spec/1.2/spec.html)
* [snakeyaml-engine](https://bitbucket.org/asomov/snakeyaml-engine), the YAML parser this library is based on
