/**
 * This package contains the Grim annotations used by the annotation processor.
 * Grim annotations declare the symbols that should be omitted from the output javascript. The
 * symbols are often often omitted based on a condition which uses a simple expression language.
 * The symbols omitted may be designated by a name or a pattern matching multiple names.
 *
 * <p>The expression language used in conditions is based on matching the compile time settings. The expression either
 * references a compile time setting (in which case the expression is true if the value of the compile time setting
 * is <tt>true</tt>) or is of the form "[compile-time-setting]=[literal]".</p>
 */
package grim.annotations;
