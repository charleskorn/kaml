# kaml

[![Pipeline](https://github.com/charleskorn/kaml/actions/workflows/build.yml/badge.svg)](https://github.com/charleskorn/kaml/actions/workflows/build.yml)
[![Coverage](https://img.shields.io/codecov/c/github/charleskorn/kaml.svg)](https://codecov.io/gh/charleskorn/kaml)
[![License](https://img.shields.io/github/license/charleskorn/kaml.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.charleskorn.kaml/kaml.svg?label=maven%20central)](https://search.maven.org/search?q=g:%22com.charleskorn.kaml%22%20AND%20a:%22kaml%22)

## What is this?

This library adds YAML support to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/).

Currently, only Kotlin/JVM is fully supported.

Kotlin/JS and Kotlin/Wasm support are considered highly experimental. It is not yet fully functional, and may be removed or modified at any time.

(Follow [this issue](https://github.com/charleskorn/kaml/issues/232) for a discussion of adding support for other targets.)

YAML version 1.2 is supported.

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

val result = Yaml.default.decodeFromString(Team.serializer(), input)

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

val result = Yaml.default.encodeToString(Team.serializer(), input)

println(result)
```

### Parsing into YamlNode

It is possible to parse a string or an InputStream directly into a YamlNode, for example
the following code prints `Cindy`.
```kotlin
val input = """
        leader: Amy
        members:
          - Bob
          - Cindy
          - Dan
    """.trimIndent()

val result = Yaml.default.parseToYamlNode(input)

println(
    result
        .yamlMap.get<YamlList>("members")!![1]
        .yamlScalar
        .content
)
```

## Referencing kaml

Add the following to your Gradle build script:

**Groovy DSL**

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.4.20'
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.4.20'
}

dependencies {
  implementation "com.charleskorn.kaml:kaml:<version number here>" // Get the latest version number from https://github.com/charleskorn/kaml/releases/latest
}
```

**Kotlin DSL**

```kotlin
plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("plugin.serialization") version "1.4.20"
}

dependencies {
  implementation("com.charleskorn.kaml:kaml:<version number here>") // Get the latest version number from https://github.com/charleskorn/kaml/releases/latest
}
```

Check the [releases page](https://github.com/charleskorn/kaml/releases) for the latest release information,
and the [Maven Central page](https://search.maven.org/artifact/com.charleskorn.kaml/kaml) for examples of how
to reference the library in other build systems.

## Features

* Supports most major YAML features:
  * Scalars, including strings, booleans, integers and floats
  * [Sequences (lists)](https://yaml.org/type/seq.html)
  * [Maps](https://yaml.org/type/map.html)
  * [Nulls](https://yaml.org/type/null.html)
  * [Aliases and anchors](https://yaml.org/spec/1.2/spec.html#id2765878), including [merging aliases to form one map](https://yaml.org/type/merge.html)

* Supports parsing YAML to Kotlin objects (deserializing) and writing Kotlin objects as YAML (serializing)

* Supports [kotlinx.serialization's polymorphism](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md) for sealed and unsealed types

  Two styles are available (set `YamlConfiguration.polymorphismStyle` when creating an instance of `Yaml`):

  * using [YAML tags](https://yaml.org/spec/1.2/spec.html#id2761292) to specify the type:

    ```yaml
    servers:
      - !<frontend>
        hostname: a.mycompany.com
      - !<backend>
        database: db-1
    ```

  * using a `type` property to specify the type:

    ```yaml
    servers:
      - type: frontend
        hostname: a.mycompany.com
      - type: backend
        database: db-1
    ```

  The fragments above could be generated with:

  ```kotlin
  @Serializable
  sealed class Server {
    @SerialName("frontend")
    @Serializable
    data class Frontend(val hostname: String) : Server()

    @SerialName("backend")
    @Serializable
    data class Backend(val database: String) : Server()
  }

  @Serializable
  data class Config(val servers: List<Server>)

  val config = Config(listOf(
    Frontend("a.mycompany.com"),
    Backend("db-1")
  ))

  val result = Yaml.default.encodeToString(Config.serializer(), config)

  println(result)
  ```

* Supports [Docker Compose-style extension fields](https://medium.com/@kinghuang/docker-compose-anchors-aliases-extensions-a1e4105d70bd)

  ```yaml
  x-common-labels: &common-labels
    labels:
      owned-by: myteam@mycompany.com
      cost-centre: myteam

  servers:
    server-a:
      <<: *common-labels
      kind: frontend

    server-b:
      <<: *common-labels
      kind: backend

    # server-b and server-c are equivalent
    server-c:
      labels:
        owned-by: myteam@mycompany.com
        cost-centre: myteam
      kind: backend
  ```

  Specify the extension prefix by setting `YamlConfiguration.extensionDefinitionPrefix` when creating an instance of `Yaml` (eg. `"x-"` for the example above).

  Extensions can only be defined at the top level of a document, and only if the top level element is a map or object. Any key starting with the extension prefix must have an anchor defined (`&...`) and will not be included in the deserialised value.

## Contributing to kaml

Pull requests and bug reports are always welcome!

kaml uses Gradle for builds and testing:

* To build the library: `./gradlew assemble`
* To run the tests and static analysis tools: `./gradlew check`
* To run the tests and static analysis tools continuously: `./gradlew --continuous check`

## Reference links

* [YAML 1.2 Specification](http://yaml.org/spec/1.2/spec.html)
* [snakeyaml-engine-kmp](https://github.com/krzema12/snakeyaml-engine-kmp), a Kotlin Multiplatform port of [snakeyaml-engine](https://bitbucket.org/snakeyaml/snakeyaml-engine/wiki/Home), the YAML parser this library is based on
