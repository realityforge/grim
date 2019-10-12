package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Contain annotation so many patterns can be omitted from a type.
 * This annotation is applied to the class type and applies to all nested members.
 */
@Documented
@Target( ElementType.TYPE )
public @interface OmitPatterns
{
  /**
   * The patterns to omit from the type.
   *
   * @return the patterns to omit from the type.
   */
  OmitPattern[] value();
}
