package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Annotation indicating symbols matching pattern should be omitted from generated javascript.
 */
@Documented
@Repeatable( OmitPatterns.class )
@Target( ElementType.PACKAGE )
public @interface OmitPattern
{
  /**
   * A regular expression used to match types to omit.
   * If not specified then it will default to the package which is annotated  with the annotation.
   *
   * @return a regular expression used to match types to omit.
   */
  String type() default "<default>";

  /**
   * A regular expression used to match symbols to omit.
   *
   * @return a regular expression used to match symbols to omit.
   */
  String symbol() default "<default>";

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
