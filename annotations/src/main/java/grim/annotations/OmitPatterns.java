package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation containing rules under which symbols matching a pattern are to be omitted from the generated javascript.
 * This annotation is applied to the class type and applies to all nested members.
 */
@Documented
@Target( ElementType.TYPE )
public @interface OmitPatterns
{
  /**
   * The rules under which symbols matching a pattern are to be omitted from the generated javascript.
   *
   * @return the rules under which symbols matching a pattern are to be omitted from the generated javascript.
   */
  OmitPattern[] value();
}
