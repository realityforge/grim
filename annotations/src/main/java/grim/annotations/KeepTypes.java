package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation containing rules under which a type may appear in the generated javascript.
 */
@Documented
@Target( ElementType.TYPE )
public @interface KeepTypes
{
  /**
   * The rules under which a type may appear in the generated javascript.
   *
   * @return the rules under which a type may appear in the generated javascript.
   */
  KeepType[] value();
}
