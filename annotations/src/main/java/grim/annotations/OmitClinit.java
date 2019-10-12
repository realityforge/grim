package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation indicating that the class should not generate a clinit when compiled to javascript.
 */
@Documented
@Target( ElementType.TYPE )
public @interface OmitClinit
{
}
