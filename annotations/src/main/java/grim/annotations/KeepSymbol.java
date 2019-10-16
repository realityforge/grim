package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Annotation indicating when a java element should be omitted from generated javascript.
 */
@Documented
@Repeatable( KeepSymbols.class )
@Target( { ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR } )
public @interface KeepSymbol
{
  /**
   * The symbol may be kept unless this expression is true.
   * Supplying a value for this parameter is incompatible with supplying a value to the {@link #when()} parameter.
   *
   * @return the conditional expression.
   */
  String unless() default "";

  /**
   * The symbol may be kept if this expression is true.
   * Supplying a value for this parameter is incompatible with supplying a value to the {@link #unless()} parameter.
   *
   * @return the conditional expression.
   */
  String when() default "";
}
