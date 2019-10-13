package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;

/**
 * Annotation indicating symbols matching pattern should be omitted from generated javascript.
 * This annotation is applied to the class type and applies to all nested members.
 */
@Documented
@Repeatable( OmitPatterns.class )
public @interface OmitPattern
{
  /**
   * A regular expression used to match symbols to omit.
   *
   * @return a regular expression used to match symbols to omit.
   */
  String pattern();

  /**
   * The symbol should be omitted unless this expression is true.
   * Supplying a value for this parameter is incompatible with supplying a value to the {@link #when()} parameter.
   *
   * @return the conditional expression.
   */
  String unless() default "";

  /**
   * The symbol should be omitted if this expression is true.
   * Supplying a value for this parameter is incompatible with supplying a value to the {@link #unless()} parameter.
   *
   * @return the conditional expression.
   */
  String when() default "";
}
