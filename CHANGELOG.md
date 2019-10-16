# Change Log

### Unreleased

* Fixed a bug where the `@OmitSymbol` annotation on methods that are devirtualized by the GWT compiler were not being matched. The devirtualization process creates a new method using the name of the old method prefixed with the `$` character. Update the annotation processor to follow this convention.
* Fixed a bug where the `@OmitSymbol` annotation on a constructor would fail to match the constructor as the GWT compiler names constructors using a different convention from javac. Javac uses `<init>` while GWT uses the simple name of the enclosing type.

### [v0.01](https://github.com/realityforge/grim/tree/v0.01) (2019-10-15) · [Full Changelog](https://github.com/realityforge/grim/compare/v0.00...v0.01)

 ‎🎉	Initial super-alpha release ‎🎉.
