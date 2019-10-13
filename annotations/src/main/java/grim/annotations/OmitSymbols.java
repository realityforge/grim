package grim.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Contain annotation so symbols can be omitted from in different contexts.
 */
@Target( { ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR } )
public @interface OmitSymbols
{
  /**
   * The conditions under which a symbol is omitted from the type.
   *
   * @return the conditions under which a symbol is omitted from the type.
   */
  OmitSymbol[] value();
}
