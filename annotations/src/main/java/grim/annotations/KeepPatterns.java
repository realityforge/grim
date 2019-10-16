package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation containing rules indicating which symbols may appear in the generated javascript.
 */
@Documented
@Target( ElementType.PACKAGE )
public @interface KeepPatterns
{
  /**
   * The rules indicating which symbols may appear in the generated javascript.
   *
   * @return the rules indicating which symbols may appear in the generated javascript.
   */
  KeepPattern[] value();
}
