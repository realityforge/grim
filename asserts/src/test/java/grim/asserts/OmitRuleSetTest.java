package grim.asserts;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class OmitRuleSetTest
{
  private Path _baseDirectory;

  @AfterMethod
  final void afterMethod()
    throws IOException
  {
    if ( null != _baseDirectory )
    {
      //noinspection ResultOfMethodCallIgnored
      Files.walk( _baseDirectory ).sorted( Comparator.reverseOrder() ).map( Path::toFile ).forEach( File::delete );
      _baseDirectory = null;
    }
  }

  @Test
  public void loadFromClassLoader_noRules()
    throws Exception
  {
    final OmitRuleSet rules = buildRuleSet();
    assertEquals( rules.getRules().size(), 0 );
  }

  @Test
  public void loadFromClassLoader_rulesFromSingleFile()
    throws Exception
  {
    createRuleFile( "arez/ArezContext.grim.json",
                    "[\n" +
                    "  {\n" +
                    "    \"type\": \"^\\\\Qarez.ArezContext\\\\E$\",\n" +
                    "    \"member\": \"^\\\\Q$clinit\\\\E$\"\n" +
                    "  }\n" +
                    "]\n" );

    // This is named incorrectly and thus not picked up
    createRuleFile( "arez/Other.gwt.json",
                    "[\n" +
                    "  {\n" +
                    "    \"type\": \"^\\\\Qarez.Other\\\\E$\"\n" +
                    "  }\n" +
                    "]\n" );

    final OmitRuleSet rules = buildRuleSet();
    assertEquals( rules.getRules().size(), 1 );

    final OmitRule rule = rules.getRules().iterator().next();
    assertEquals( rule.getType().toString(), "^\\Qarez.ArezContext\\E$" );
    final Pattern member = rule.getMember();
    assertNotNull( member );
    assertEquals( member.toString(), "^\\Q$clinit\\E$" );
    assertNull( rule.getCondition() );
  }

  @Test
  public void loadFromClassLoader_Condition()
    throws Exception
  {
    createRuleFile( "arez/ArezContext.grim.json",
                    "[\n" +
                    "  {\n" +
                    "    \"type\": \"^\\\\Qarez.ArezContext\\\\E$\",\n" +
                    "    \"property\": \"galdr.enable_names\",\n" +
                    "    \"operator\": \"NEQ\",\n" +
                    "    \"value\": \"true\"\n" +
                    "  }\n" +
                    "]\n" );

    final OmitRuleSet rules = buildRuleSet();
    assertEquals( rules.getRules().size(), 1 );

    final OmitRule rule = rules.getRules().iterator().next();
    assertEquals( rule.getType().toString(), "^\\Qarez.ArezContext\\E$" );
    assertNull( rule.getMember() );
    final Condition condition = rule.getCondition();
    assertNotNull( condition );
    assertEquals( condition.getProperty(), "galdr.enable_names" );
    assertEquals( condition.getValue(), "true" );
    assertFalse( condition.isEquals() );

    assertTrue( rules.matches( new HashMap<>(), "arez.ArezContext", "Foo" ) );
    final Map<String, String> compileTimeProperties = new HashMap<>();
    compileTimeProperties.put( "galdr.enable_names", "true" );
    assertFalse( rules.matches( compileTimeProperties, "arez.ArezContext", "Foo" ) );
  }

  @Test
  public void loadFromClassLoader_FilteringOfResources()
    throws Exception
  {
    createRuleFile( "arez/ArezContext.grim.json",
                    "[\n" +
                    "  {\n" +
                    "    \"type\": \"^\\\\Qarez.ArezContext\\\\E$\",\n" +
                    "    \"member\": \"^\\\\Q$clinit\\\\E$\"\n" +
                    "  }\n" +
                    "]\n" );

    createRuleFile( "arez/spy/Spy.grim.json",
                    "[\n" +
                    "  {\n" +
                    "    \"type\": \"^\\\\Qarez.spy.Spy\\\\E$\"\n" +
                    "  }\n" +
                    "]\n" );

    assertEquals( buildRuleSet().getRules().size(), 2 );
    // This next one filters out the spy rules
    assertEquals( OmitRuleSet.loadFromClassLoader( newClassLoader(), r -> !r.startsWith( "arez.spy." ) )
                    .getRules()
                    .size(), 1 );
  }

  @Test
  public void loadFromClassLoader_malformedPattern()
    throws Exception
  {
    createRuleFile( "arez/ArezContext.grim.json",
                    "[\n" +
                    "  {\n" +
                    "    \"type\": \"\\\\x\"\n" +
                    "  }\n" +
                    "]\n" );

    final IllegalStateException exception = expectThrows( IllegalStateException.class, this::buildRuleSet );
    assertEquals( exception.getMessage(),
                  "Failed to load Grim Omit rules from META-INF/grim/arez/ArezContext.grim.json" );
    assertEquals( exception.getCause().getMessage(),
                  "Grim rule at index 0 contains a pattern with a syntax error" );
  }

  @Test
  public void loadFromClassLoader_badCondition()
    throws Exception
  {
    createRuleFile( "arez/ArezContext.grim.json",
                    "[\n" +
                    "  {\n" +
                    "    \"type\": \".\",\n" +
                    "    \"property\": \"galdr.enable_names\",\n" +
                    "    \"value\": \"true\"\n" +
                    "  }\n" +
                    "]\n" );

    final IllegalStateException exception = expectThrows( IllegalStateException.class, this::buildRuleSet );
    assertEquals( exception.getMessage(),
                  "Failed to load Grim Omit rules from META-INF/grim/arez/ArezContext.grim.json" );
    assertEquals( exception.getCause().getMessage(),
                  "Grim rule at index 0 contains a partially defined operator" );
  }

  @Nonnull
  private OmitRuleSet buildRuleSet()
    throws IOException
  {
    return OmitRuleSet.loadFromClassLoader( newClassLoader() );
  }

  @Nonnull
  private URLClassLoader newClassLoader()
    throws IOException
  {
    return new URLClassLoader( new URL[]{ getBaseDirectory().toUri().toURL() }, null );
  }

  private void createRuleFile( @Nonnull final String path, @Nonnull final String content )
    throws IOException
  {
    final Path file = getBaseDirectory().resolve( "META-INF" ).resolve( "grim" ).resolve( Paths.get( path ) );
    Files.createDirectories( file.getParent() );
    Files.write( file, content.getBytes( StandardCharsets.UTF_8 ) );
  }

  private Path getBaseDirectory()
    throws IOException
  {
    if ( null == _baseDirectory )
    {
      _baseDirectory = Files.createTempDirectory( "grim" );
    }
    return _baseDirectory;
  }
}
