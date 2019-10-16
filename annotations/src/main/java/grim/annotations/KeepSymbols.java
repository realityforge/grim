package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation containing rules under which symbol may appear in the generated javascript.
 */
@Documented
@Target( { ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR } )
public @interface KeepSymbols
{
  /**
   * The rules under which symbol may appear in the generated javascript.
   *
   * @return the rules under which symbol may appear in the generated javascript.
   */
  KeepSymbol[] value();
}
