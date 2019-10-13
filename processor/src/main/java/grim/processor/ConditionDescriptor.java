package grim.processor;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.json.stream.JsonGenerator;

final class ConditionDescriptor
{
  enum Operator
  {
    EQUALS,
    NOT_EQUALS
  }

  @Nonnull
  private final String _property;
  @Nonnull
  private final String _value;
  @Nonnull
  private final Operator _operator;

  ConditionDescriptor( @Nonnull final String property,
                       @Nonnull final String value,
                       @Nonnull final Operator operator )
  {
    _property = Objects.requireNonNull( property );
    _value = Objects.requireNonNull( value );
    _operator = Objects.requireNonNull( operator );
  }

  void generate( @Nonnull final JsonGenerator g )
  {
    g.write( "property", _property );
    g.write( "operator", _operator == Operator.EQUALS ? "EQ" : "NEQ" );
    g.write( "value", _value );
  }
}
