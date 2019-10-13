package grim.asserts;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Representation of a rule declaring symbols that should be omitted possible under a condition.
 */
public final class OmitRule
{
  /**
   * A regular expression to match the type.
   */
  @Nonnull
  private final Pattern _type;
  /**
   * An optional regular expression to match the member.
   */
  @Nullable
  private final Pattern _member;
  /**
   * An optional condition that determines the scenarios in which the omit rule applies.
   */
  @Nullable
  private final Condition _condition;

  public OmitRule( @Nonnull final Pattern type, @Nullable final Pattern member, @Nullable final Condition condition )
  {
    _type = Objects.requireNonNull( type );
    _member = member;
    _condition = condition;
  }

  @Nonnull
  public Pattern getType()
  {
    return _type;
  }

  @Nullable
  public Pattern getMember()
  {
    return _member;
  }

  @Nullable
  public Condition getCondition()
  {
    return _condition;
  }

  public boolean matches( @Nonnull final Map<String, String> compileTimeProperties,
                          @Nonnull final String type,
                          @Nonnull final String member )
  {
    return _type.matcher( type ).matches() &&
           ( null == _member || _member.matcher( member ).matches() ) &&
           ( null == _condition || _condition.matches( compileTimeProperties ) );
  }
}
