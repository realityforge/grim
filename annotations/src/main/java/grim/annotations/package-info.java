/**
 * This package contains the Grim annotations used by the annotation processor.
 * Grim annotations declare the symbols that should be omitted from the output javascript. The
 * symbols are often often omitted based on a condition which uses a simple expression language.
 * The symbols omitted may be designated by a name or a pattern matching multiple names.
 *
 * <p>The expression language used in conditions is based on compile time settings. The expression either
 * reference a compile time setting (in which case the expression is true if the expression is true) or
 * use "[compile-time-setting][operator][string literal]" where "[operator]" can either be "==" or "!=".</p>
 */
package grim.annotations;
