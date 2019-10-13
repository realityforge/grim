package grim.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;

/**
 * Annotation indicating that the class should not be present in output javascript.
 * This is typically used when it is expected that all contained members are optimized away.
 */
@Documented
@Repeatable( OmitTypes.class )
public @interface OmitType
{
}
