package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation containing rules under which symbols matching a pattern are to be omitted from the generated javascript.
 */
@Documented
@Target( ElementType.PACKAGE )
public @interface OmitPatterns
{
  /**
   * The rules under which symbols matching a pattern are to be omitted from the generated javascript.
   *
   * @return the rules under which symbols matching a pattern are to be omitted from the generated javascript.
   */
  OmitPattern[] value();
}
