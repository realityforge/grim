package grim.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.tools.JavaFileObject;
import static com.google.common.truth.Truth.*;
import static org.testng.Assert.*;

@SuppressWarnings( "Duplicates" )
abstract class AbstractGrimProcessorTest
{
  void assertGrimRulesGenerated( @Nonnull final String classname )
    throws Exception
  {
    final String[] elements = classname.contains( "." ) ? classname.split( "\\." ) : new String[]{ classname };
    final StringBuilder input = new StringBuilder();
    final StringBuilder config = new StringBuilder();
    input.append( "input" );
    config.append( "expected/META-INF/grim" );
    for ( final String element : elements )
    {
      input.append( '/' );
      input.append( element );
      config.append( '/' );
      config.append( element );
    }
    input.append( ".java" );
    config.append( ".grim.json" );
    assertGrimRulesGenerated( Collections.singletonList( fixture( input.toString() ) ),
                              Collections.singletonList( config.toString() ) );
  }

  void assertGrimRulesGenerated( @Nonnull final List<JavaFileObject> inputs, @Nonnull final List<String> outputs )
    throws Exception
  {
    if ( outputFiles() )
    {
      final Compilation compilation =
        Compiler.javac()
          .withProcessors( new GrimProcessor() )
          .withOptions( "-Xlint:all,-processing", "-implicit:none" )
          .compile( inputs );

      final Compilation.Status status = compilation.status();
      if ( Compilation.Status.SUCCESS != status )
      {
        /*
         * Ugly hackery that marks the compile as successful so we can emit output onto filesystem. This could
         * result in java code that is not compilable emitted to filesystem. This re-running determining problems
         * a little easier even if it does make re-running tests from IDE a little harder
         */
        final Field field = compilation.getClass().getDeclaredField( "status" );
        field.setAccessible( true );
        field.set( compilation, Compilation.Status.SUCCESS );
      }

      for ( final JavaFileObject fileObject : compilation.generatedFiles() )
      {
        if ( JavaFileObject.Kind.OTHER == fileObject.getKind() )
        {
          final Path target =
            fixtureDir().resolve( "expected/" + fileObject.getName().replace( "/CLASS_OUTPUT/", "" ) );

          final File dir = target.getParent().toFile();
          if ( !dir.exists() )
          {
            assertTrue( dir.mkdirs() );
          }
          if ( Files.exists( target ) )
          {
            final byte[] existing = Files.readAllBytes( target );
            final InputStream generated = fileObject.openInputStream();
            final byte[] data = new byte[ generated.available() ];
            assertEquals( generated.read( data ), data.length );
            if ( Arrays.equals( existing, data ) )
            {
              /*
               * If the data on the filesystem is identical to data generated then do not write
               * to filesystem. The writing can be slow and it can also trigger the IDE or other
               * tools to reload file which is problematic.
               */
              continue;
            }
            Files.delete( target );
          }
          Files.copy( fileObject.openInputStream(), target );
        }
      }

      if ( Compilation.Status.SUCCESS != status )
      {
        // Restore old status
        final Field field = compilation.getClass().getDeclaredField( "status" );
        field.setAccessible( true );
        field.set( compilation, status );

        // This next line will generate an error
        try
        {
          //noinspection ResultOfMethodCallIgnored
          compilation.generatedSourceFiles();
        }
        catch ( final Exception ignored )
        {
        }
      }
    }
    final JavaFileObject firstExpected = fixture( outputs.get( 0 ) );
    final JavaFileObject[] restExpected =
      outputs.stream().skip( 1 ).map( this::fixture ).toArray( JavaFileObject[]::new );
    assert_().about( JavaSourcesSubjectFactory.javaSources() ).
      that( inputs ).
      processedWith( new GrimProcessor() ).
      compilesWithoutError().
      and().
      generatesFiles( firstExpected, restExpected );
  }

  @Nonnull
  final JavaFileObject fixture( @Nonnull final String path )
  {
    try
    {
      return JavaFileObjects.forResource( fixtureDir().resolve( path ).toUri().toURL() );
    }
    catch ( final MalformedURLException e )
    {
      throw new IllegalStateException( e );
    }
  }

  @Nonnull
  private Path fixtureDir()
  {
    final String fixtureDir = System.getProperty( "grim.fixture_dir" );
    assertNotNull( fixtureDir, "Expected System.getProperty( \"grim.fixture_dir\" ) to return fixture directory" );
    return new File( fixtureDir ).toPath();
  }

  private boolean outputFiles()
  {
    return System.getProperty( "grim.output_fixture_data", "false" ).equals( "true" );
  }
}
