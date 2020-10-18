# Grim: Ensure dead code is eliminated

[![Build Status](https://api.travis-ci.com/realityforge/grim.svg?branch=master)](http://travis-ci.com/realityforge/grim)
[<img src="https://img.shields.io/maven-central/v/org.realityforge.grim/grim.svg?label=latest%20release"/>](https://search.maven.org/search?q=g:org.realityforge.grim%20a:grim-annotations)
[![codecov](https://codecov.io/gh/realityforge/grim/branch/master/graph/badge.svg)](https://codecov.io/gh/realityforge/grim)

## What is Grim?

Grim is a tool that helps enforce dead code is eliminated from GWT libraries. Grim consists of a set of annotations
that developers use to declare which methods or fields are expected to be eliminated from the javascript output
under different conditions. Grim also contains an annotation processor that processes the annotations to generate
a set of output rules that indicates the members that will be eliminated with specific values for compile time
settings. Grim also contains an assertion library that takes one or more output rules and verifies that the results
of a GWT compilation complied with the rules.

### Getting Started

The tool is released to Maven Central and can be downloaded using normal dependency download mechanisms.
The Maven dependency is:

```xml
<dependency>
  <groupId>org.realityforge.grim</groupId>
  <artifactId>grim-annotations</artifactId>
  <version>0.04</version>
</dependency>
<dependency>
  <groupId>org.realityforge.grim</groupId>
  <artifactId>grim-processor</artifactId>
  <version>0.04</version>
</dependency>
<dependency>
  <groupId>org.realityforge.grim</groupId>
  <artifactId>grim-asserts</artifactId>
  <version>0.04</version>
</dependency>
```

# Contributing

The project was released as open source so others could benefit from the project. We are thankful for any
contributions from the community. A [Code of Conduct](CODE_OF_CONDUCT.md) has been put in place and
a [Contributing](CONTRIBUTING.md) document is under development.

# License

The project is licensed under [Apache License, Version 2.0](LICENSE).

# Credit

* [Stock Software](http://www.stocksoftware.com.au/) for providing significant support in building and maintaining
  the library, particularly at it's inception.
