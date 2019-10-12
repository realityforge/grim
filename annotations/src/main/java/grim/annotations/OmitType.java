package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation indicating that the class should not be present in output javascript.
 * This is typically used when it is expected that all contained members are optimized away.
 */
@Documented
@Target( ElementType.TYPE )
public @interface OmitType
{
}
