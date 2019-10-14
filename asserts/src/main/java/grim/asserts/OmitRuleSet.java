package grim.asserts;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

/**
 * A collection of OmitRules.
 */
public final class OmitRuleSet
{
  @Nonnull
  private final Collection<OmitRule> _rules;

  private OmitRuleSet( @Nonnull final Collection<OmitRule> rules )
  {
    _rules = Objects.requireNonNull( rules );
  }

  @Nonnull
  public Collection<OmitRule> getRules()
  {
    return _rules;
  }

  /**
   * Load all grim rules from the specified classloader.
   * The rules are stored files with the suffix ".grim.json" in the standard path "META-INF/grim/*".
   *
   * @param classLoader the classloader.
   * @return the collection of rules loaded from the Classloader.
   */
  @Nonnull
  public static OmitRuleSet loadFromClassLoader( @Nonnull final ClassLoader classLoader )
  {
    return new OmitRuleSet( OmitRuleLoader.loadFromClassLoader( classLoader, null ) );
  }

  /**
   * Load all grim rules from the specified classloader that have classnames that match the specified filter.
   * The rules are stored files with the suffix ".grim.json" in the standard path "META-INF/grim/*".
   *
   * @param classLoader the classloader.
   * @param filter      the filter.
   * @return the collection of rules loaded from the Classloader.
   */
  @Nonnull
  public static OmitRuleSet loadFromClassLoader( @Nonnull final ClassLoader classLoader,
                                                 @Nonnull final Predicate<String> filter )
  {
    return new OmitRuleSet( OmitRuleLoader.loadFromClassLoader( classLoader, Objects.requireNonNull( filter ) ) );
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
    return _rules.stream().anyMatch( r -> r.matches( compileTimeProperties, type, member ) );
  }
}
