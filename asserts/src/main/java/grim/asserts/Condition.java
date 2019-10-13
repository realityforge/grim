package grim.asserts;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * The condition under which a rule is applied.
 * A condition reads the value of a compile-time property compares it to a value with the result of the
 * comparison expected to match the equals flag if the condition is to resolve to match.
 */
public final class Condition
{
  /**
   * The compile-time property.
   */
  @Nonnull
  private final String _property;
  /**
   * The value of the compile-time property.
   */
  @Nonnull
  private final String _value;
  /**
   * True if the condition is checking that the compile-time property matches the value, false if the condition
   * is checking that the compile time property does not match the value.
   */
  private final boolean _equals;

  public Condition( @Nonnull final String property, @Nonnull final String value, final boolean equals )
  {
    _property = Objects.requireNonNull( property );
    _value = Objects.requireNonNull( value );
    _equals = equals;
  }

  @Nonnull
  public String getProperty()
  {
    return _property;
  }

  @Nonnull
  public String getValue()
  {
    return _value;
  }

  public boolean isEquals()
  {
    return _equals;
  }

  public boolean matches( @Nonnull final Map<String, String> compileTimeProperties )
  {
    return _equals == _value.equals( compileTimeProperties.get( _property ) );
  }
}
