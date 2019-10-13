package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation containing rules under which the type is to be omitted from the generated javascript.
 */
@Documented
@Target( ElementType.TYPE )
public @interface OmitTypes
{
  /**
   * The rules under which the type is to be omitted from the generated javascript.
   *
   * @return the rules under which the type is to be omitted from the generated javascript.
   */
  OmitType[] value();
}
