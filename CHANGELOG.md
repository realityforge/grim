# Change Log

### Unreleased

* Fix bug where `@OmitSymbol` on methods that are devirtualized by the GWT compiler were not being matched. The devirtualization process creates a new method using the name of the old method prefixed with the `$` character. Update the annotation processor to follow this convention.

### [v0.01](https://github.com/realityforge/grim/tree/v0.01) (2019-10-15) · [Full Changelog](https://github.com/realityforge/grim/compare/v0.00...v0.01)

 ‎🎉	Initial super-alpha release ‎🎉.
