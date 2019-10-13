package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;

/**
 * Annotation indicating that the class should not be present in output javascript.
 * This is typically used when it is expected that all contained members are optimized away.
 */
@Documented
@Repeatable( OmitTypes.class )
public @interface OmitType
{
  /**
   * The type should be omitted unless this expression is true.
   * Supplying a value for this parameter is incompatible with supplying a value to the {@link #when()} parameter.
   *
   * @return the conditional expression.
   */
  String unless() default "";

  /**
   * The type should be omitted if this expression is true
   * Supplying a value for this parameter is incompatible with supplying a value to the {@link #unless()} parameter.
   *
   * @return the conditional expression.
   */
  String when() default "";
}
