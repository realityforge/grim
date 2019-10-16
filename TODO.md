# TODO

This document is essentially a list of shorthand notes describing work yet to completed.
Unfortunately it is not complete enough for other people to pick work off the list and
complete as there is too much un-said.

* Figure out a way how to support J2CL based applications.

* Add mechanisms for merging multiple ruleSets into a single RuleSet.

* Add a set of `Keep*` patterns that are a higher priority than `Omit*` style rules. That way we can declare
  `@OmitPattern(pattern="^\\$clinit$")` but add a `@KeepClinit` on the few classes where clinits are valid.
  This makes it much easier in projects like Arez where we can very precisely control where it is valid to
  retain java code elements.
  i.e.
  - Allow `type=".*\\.React4j_[^\\.]+\\$Factory", member="^\\$clinit$"` but disallow all other `$clinit` in `React_*` generated classes.
  - Allow `type="arez.ArezContextHolder", member="$clinit"` if zones disabled
  - Allow `type="arez.ZoneHolder", member="$clinit"` if zones enabled
  - Disallow `type="arez.*", member="$clinit"`

* Apply ruleset to output using gwt-symbolmap

* Add a separate module containing a GWT Linker that emits the compile time properties for each permutation.
  This would be used to help determine what tests need to be run for each application.

* Simplify infrastructure so that it is trivial for each application to test it's own output
  applying rules from all included libraries.

* Add Grim metadata to the toolkits.
  - `arez-dom`
  - `arez-promise`
  - `arez-spytools`
  - `react4j`
  _ `spritz`
  - `galdr`
  - `replicant`

* Add Grim metadata to the sample applications.
  - `react4j-todomvc`
  _ `react4j-flux-challenge`
  - `react4j-drumloop`

* Add some basic documentation to README
