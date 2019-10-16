package grim.asserts;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Representation of a rule declaring symbols that should be omitted possible under a condition.
 */
public final class Rule
{
  /**
   * Does the rule indicate symbol should be omitted or kept.
   */
  private final boolean _omit;
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

  public Rule( final boolean omit,
               @Nonnull final Pattern type,
               @Nullable final Pattern member,
               @Nullable final Condition condition )
  {
    _omit = omit;
    _type = Objects.requireNonNull( type );
    _member = member;
    _condition = condition;
  }

  /**
   * Return true if the rule indicates a symbol to omit.
   *
   * @return true if the rule indicates a symbol to omit.
   */
  public boolean isOmitRule()
  {
    return _omit;
  }

  /**
   * Return true if the rule indicates a symbol to keep.
   *
   * @return true if the rule indicates a symbol to keep.
   */
  public boolean isKeepRule()
  {
    return !_omit;
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
