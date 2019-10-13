package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation containing rules under which symbol is to be omitted from the generated javascript.
 */
@Documented
@Target( { ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR } )
public @interface OmitSymbols
{
  /**
   * The rules under which the symbol is to be omitted from the generated javascript.
   *
   * @return the rules under which the symbol is to be omitted from the generated javascript.
   */
  OmitSymbol[] value();
}
