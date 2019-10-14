package grim.asserts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.lang.model.SourceVersion;

final class OmitRuleLoader
{
  @Nonnull
  private static final String BASE_PATH = "META-INF/grim";
  @Nonnull
  private static final String FILE_SUFFIX = ".grim.json";
  @Nonnull
  private static final Predicate<String> GRIM_FILE_MATCHER = r -> r.endsWith( FILE_SUFFIX );

  private OmitRuleLoader()
  {
  }

  @Nonnull
  static Collection<OmitRule> loadFromClassLoader( @Nonnull final ClassLoader classLoader,
                                                   @Nullable final Predicate<String> filterFn )
  {
    final List<String> resourceNames = new ArrayList<>();
    collectResourceNames( classLoader, BASE_PATH, asFilter( filterFn ), resourceNames );

    final List<OmitRule> rules = new ArrayList<>();
    for ( final String resourceName : resourceNames )
    {
      try
      {
        rules.addAll( loadOmitRules( classLoader, resourceName ) );
      }
      catch ( final IOException ioe )
      {
        throw new IllegalStateException( "Failed to load Grim Omit rules from " + resourceName, ioe );
      }
    }
    return rules;
  }

  @Nonnull
  private static List<OmitRule> loadOmitRules( @Nonnull final ClassLoader classLoader,
                                               @Nonnull final String resourceFilename )
    throws IOException
  {
    final InputStream resourceStream = classLoader.getResourceAsStream( resourceFilename );
    if ( null == resourceStream )
    {
      throw new IOException( "Failed to locate grim rules for resource " + resourceFilename );
    }
    final List<OmitRule> rules = new ArrayList<>();
    try ( final JsonReader reader = Json.createReader( resourceStream ) )
    {
      final JsonArray ruleArray = reader.readArray();
      final int size = ruleArray.size();
      for ( int i = 0; i < size; i++ )
      {
        rules.add( parseOmitRule( i, ruleArray.getJsonObject( i ) ) );
      }
    }
    return rules;
  }

  @Nonnull
  private static OmitRule parseOmitRule( final int ruleIndex,
                                         @Nonnull final JsonObject ruleObject )
    throws IOException
  {
    final String type = ruleObject.getString( "type" );
    final String member = ruleObject.getString( "member", null );
    final String property = ruleObject.getString( "property", null );
    final String value = ruleObject.getString( "value", null );
    final String operator = ruleObject.getString( "operator", null );
    if ( ( null != property || null != value || null != operator ) &&
         ( null == property || null == value || null == operator ) )
    {
      throw new IOException( "Grim rule at index " + ruleIndex + " contains a partially defined operator" );
    }
    try
    {
      return new OmitRule( Pattern.compile( type ),
                           null == member ? null : Pattern.compile( member ),
                           null == property ?
                           null :
                           new Condition( property, value, operator.equals( "EQ" ) ) );
    }
    catch ( final PatternSyntaxException pse )
    {
      throw new IOException( "Grim rule at index " + ruleIndex + " contains a pattern with a syntax error", pse );
    }
  }

  @Nonnull
  private static Predicate<String> asFilter( @Nullable final Predicate<String> filterFn )
  {
    return null == filterFn ?
           GRIM_FILE_MATCHER :
           GRIM_FILE_MATCHER.and( resource -> {
             final int beginIndex = BASE_PATH.length() + 1;
             final int endIndex = resource.length() - FILE_SUFFIX.length();
             return filterFn.test( resource.substring( beginIndex, endIndex ).replace( "/", "." ) );
           } );
  }

  private static void collectResourceNames( @Nonnull final ClassLoader classLoader,
                                            @Nonnull final String base,
                                            @Nonnull final Predicate<String> acceptResource,
                                            @Nonnull final List<String> resources )
  {
    final InputStream resourceStream = classLoader.getResourceAsStream( base );
    if ( null != resourceStream )
    {
      try ( final BufferedReader reader = toReader( resourceStream ) )
      {
        String line;
        while ( null != ( line = reader.readLine() ) )
        {
          final String resource = base + "/" + line;
          if ( acceptResource.test( resource ) )
          {
            resources.add( resource );
          }
          else if ( SourceVersion.isIdentifier( line ) )
          {
            collectResourceNames( classLoader, resource, acceptResource, resources );
          }
        }
      }
      catch ( final IOException ioe )
      {
        throw new IllegalStateException( "Failed to read grim metadata directory", ioe );
      }
    }
  }

  @Nonnull
  private static BufferedReader toReader( @Nonnull final InputStream resourceStream )
  {
    return new BufferedReader( new InputStreamReader( resourceStream, StandardCharsets.UTF_8 ) );
  }
}
