package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Annotation indicating symbols matching pattern may appear in generated javascript.
 */
@Documented
@Repeatable( KeepPatterns.class )
@Target( ElementType.PACKAGE )
public @interface KeepPattern
{
  /**
   * A regular expression used to match types that may appear in generated javascript.
   * If not specified then it will default to the package which is annotated with the annotation.
   *
   * @return a regular expression used to match types that may appear in generated javascript.
   */
  String type() default "<default>";

  /**
   * A regular expression used to match symbols that may appear in generated javascript.
   *
   * @return a regular expression used to match symbols that may appear in generated javascript.
   */
  String symbol() default "<default>";

  /**
   * The symbol may be kept unless this expression is true.
   * Supplying a value for this parameter is incompatible with supplying a value to the {@link #when()} parameter.
   *
   * @return the conditional expression.
   */
  String unless() default "";

  /**
   * The symbol may be kept if this expression is true.
   * Supplying a value for this parameter is incompatible with supplying a value to the {@link #unless()} parameter.
   *
   * @return the conditional expression.
   */
  String when() default "";
}
