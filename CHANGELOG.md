# Change Log

### Unreleased

* Upgrade the `org.realityforge.proton` artifacts to version `0.28`.

### [v0.04](https://github.com/realityforge/grim/tree/v0.04) (2020-01-21) · [Full Changelog](https://github.com/realityforge/grim/compare/v0.03...v0.04)

* Upgrade the `com.google.guava` artifact to version `27.1-jre`.
* Upgrade the `com.google.truth` artifact to version `0.44`.
* Upgrade the `com.google.testing.compile` artifact to version `0.18`.
* Update the documentation of the `type` parameter in the `@OmitPattern` and `@KeepPattern` annotations to reflect the actual implementation. If unspecified then the rule applies to the annotated package and all-subpackages.
* Remove runtime dependencies on the `guava` and `autocommon` artifacts and replace with a more lightweight `proton` library. Also refactor the existing code to make use of additional facilities within `proton`.
* Correctly declare the support annotation options so that the `grim.defer.unresolved` and `grim.defer.errors` keys can be passed as parameters.

### [v0.03](https://github.com/realityforge/grim/tree/v0.03) (2019-10-16) · [Full Changelog](https://github.com/realityforge/grim/compare/v0.02...v0.03)

* Fixed a bug that caused a build failure if both a `@KeepPattern` and an `@OmitPattern` occurred on the same package.

### [v0.02](https://github.com/realityforge/grim/tree/v0.02) (2019-10-16) · [Full Changelog](https://github.com/realityforge/grim/compare/v0.01...v0.02)

* Fixed a bug where the `@OmitSymbol` annotation on methods that are devirtualized by the GWT compiler were not being matched. The devirtualization process creates a new method using the name of the old method prefixed with the `$` character. Update the annotation processor to follow this convention.
* Fixed a bug where the `@OmitSymbol` annotation on a constructor would fail to match the constructor as the GWT compiler names constructors using a different convention from javac. Javac uses `<init>` while GWT uses the simple name of the enclosing type.
* Rename `grim.asserts.OmitRuleSet` to `grim.asserts.RuleSet` and `grim.asserts.OmitRule` to `grim.asserts.Rule` in preparation for supporting "Keep" rules.
* Add `@Keep*` annotations that mirror the existing `@Omit*` annotations. The keep rules are used to override an `@Omit*` rule. This makes it easy to add a broad `@Omit*` that a large library and just add `@Keep*` in the few places where the `@Omit*` annotation does not apply.
* Add the `RuleSet.combine(RuleSet...)` method for combining multiple `RuleSet` instances into a single `RuleSet`.

### [v0.01](https://github.com/realityforge/grim/tree/v0.01) (2019-10-15) · [Full Changelog](https://github.com/realityforge/grim/compare/v0.00...v0.01)

 ‎🎉	Initial super-alpha release ‎🎉.
