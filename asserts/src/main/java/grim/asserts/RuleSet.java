package grim.asserts;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A collection of Rules.
 */
public final class RuleSet
{
  /**
   * Rules for symbols to omit.
   */
  @Nonnull
  private final Collection<Rule> _omitRules;
  /**
   * Rules for symbols to keep.
   */
  @Nonnull
  private final Collection<Rule> _keepRules;

  private RuleSet( @Nonnull final Collection<Rule> rules )
  {
    _omitRules = rules.stream().filter( Rule::isOmitRule ).collect( Collectors.toList() );
    _keepRules = rules.stream().filter( Rule::isKeepRule ).collect( Collectors.toList() );
  }

  @Nonnull
  Collection<Rule> getOmitRules()
  {
    return _omitRules;
  }

  @Nonnull
  Collection<Rule> getKeepRules()
  {
    return _keepRules;
  }

  /**
   * Load all grim rules from the specified classloader.
   * The rules are stored files with the suffix ".grim.json" in the standard path "META-INF/grim/*".
   *
   * @param classLoader the classloader.
   * @return the collection of rules loaded from the Classloader.
   * @see #loadFromClassLoader(ClassLoader, Predicate)
   */
  @Nonnull
  public static RuleSet loadFromClassLoader( @Nonnull final ClassLoader classLoader )
  {
    return loadFromClassLoader( classLoader, null );
  }

  /**
   * Load all grim rules from the specified classloader that have classnames that match the specified filter.
   * The rules are stored files with the suffix ".grim.json" in the standard path "META-INF/grim/*".
   *
   * @param classLoader the classloader.
   * @param filter      the filter if any.
   * @return the collection of rules loaded from the Classloader.
   * @see #loadFromClassLoader(ClassLoader)
   */
  @Nonnull
  public static RuleSet loadFromClassLoader( @Nonnull final ClassLoader classLoader,
                                             @Nullable final Predicate<String> filter )
  {
    return new RuleSet( RuleLoader.loadFromClassLoader( classLoader, filter ) );
  }

  /**
   * Load all grim rules from the archive specified.
   * The rules are stored files with the suffix ".grim.json" in the standard path "META-INF/grim/*".
   * The archive is expected to be a jar file.
   *
   * @param archivePath the path to the archive.
   * @return the collection of rules loaded from the Archive.
   * @see #loadFromArchive(Path, Predicate)
   */
  @Nonnull
  public static RuleSet loadFromArchive( @Nonnull final Path archivePath )
  {
    return loadFromArchive( archivePath, null );
  }

  /**
   * Load all grim rules from the archive specified that have classnames that match the specified filter.
   * The rules are stored files with the suffix ".grim.json" in the standard path "META-INF/grim/*".
   * The archive is expected to be a jar file.
   *
   * @param archivePath the path to the archive.
   * @param filter      the filter if any.
   * @return the collection of rules loaded from the Archive.
   * @see #loadFromArchive(Path)
   */
  @Nonnull
  public static RuleSet loadFromArchive( @Nonnull final Path archivePath, @Nullable final Predicate<String> filter )
  {
    return new RuleSet( RuleLoader.loadFromArchive( archivePath, filter ) );
  }

  /**
   * Return true if the specified type+member combination should have been omitted given the context of the compileTimeProperties.
   *
   * @param compileTimeProperties the static compile time properties.
   * @param type                  the name of the java type.
   * @param member                the name of the member if any else the empty string.
   * @return true if the symbol should be omitted.
   */
  public boolean shouldOmitSymbol( @Nonnull final Map<String, String> compileTimeProperties,
                                   @Nonnull final String type,
                                   @Nonnull final String member )
  {
    return _omitRules.stream().anyMatch( r1 -> r1.matches( compileTimeProperties, type, member ) ) &&
           _keepRules.stream().noneMatch( r -> r.matches( compileTimeProperties, type, member ) );
  }
}
