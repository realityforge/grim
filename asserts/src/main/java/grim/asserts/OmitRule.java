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

  /**
   * Return a regular expression to match the type.
   *
   * @return a regular expression to match the type.
   */
  @Nonnull
  public Pattern getType()
  {
    return _type;
  }

  /**
   * Return an optional regular expression to match the member.
   *
   * @return an optional regular expression to match the member.
   */
  @Nullable
  public Pattern getMember()
  {
    return _member;
  }

  /**
   * Return an optional condition that determines the scenarios in which the omit rule applies.
   *
   * @return an optional condition that determines the scenarios in which the omit rule applies.
   */
  @Nullable
  public Condition getCondition()
  {
    return _condition;
  }

  /**
   * Return true if the specified type+member combination should have been omitted given the context of the compileTimeProperties.
   *
   * @param compileTimeProperties the static compile time properties.
   * @param type                  the name of the java type.
   * @param member                the name of the member if any else the empty string.
   * @return true if the symbol should be omitted.
   */
  public boolean matches( @Nonnull final Map<String, String> compileTimeProperties,
                          @Nonnull final String type,
                          @Nonnull final String member )
  {
    return _type.matcher( type ).matches() &&
           ( null == _member || _member.matcher( member ).matches() ) &&
           ( null == _condition || _condition.matches( compileTimeProperties ) );
  }
}
