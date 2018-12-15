# kaml

## What is this?

This library adds YAML support to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/).

YAML version 1.2 is supported.

This is a very rough initial version:

* Currently, only parsing YAML is supported. Emitting YAML will be added in a future version.
* Many operations are not yet optimised for performance.

## Contributing to kaml

Pull requests and bug reports are always welcome!

kaml uses [batect](https://batect.charleskorn.com) to simplify development environment setup:

* To build the library: `./batect build`
* To run the tests and static analysis tools: `./batect check`
* To run the tests and static analysis tools continuously: `./batect continuousCheck`

Other commands are available by running `./batect --list-tasks`

## Reference links

* [YAML 1.2 Specification](http://yaml.org/spec/1.2/spec.html)
* [Dahgan](https://github.com/kareez/dahgan/), the YAML parser this library is based on
