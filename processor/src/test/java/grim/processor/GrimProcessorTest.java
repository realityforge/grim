package grim.processor;

import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.processing.Processor;
import org.realityforge.proton.qa.AbstractProcessorTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GrimProcessorTest
  extends AbstractProcessorTest
{
  @DataProvider( name = "successfulCompiles" )
  public Object[][] successfulCompiles()
  {
    return new Object[][]
      {
        new Object[]{ "com.example.KeepClinitExample" },
        new Object[]{ "com.example.KeepConstructorExample" },
        new Object[]{ "com.example.KeepFieldExample" },
        new Object[]{ "com.example.KeepMethodExample" },
        new Object[]{ "com.example.package-info" },
        new Object[]{ "com.example.KeepTypeExample" },
        new Object[]{ "com.example.OmitClinitExample" },
        new Object[]{ "com.example.OmitConstructorExample" },
        new Object[]{ "com.example.OmitFieldExample" },
        new Object[]{ "com.example.OmitMethodExample" },
        new Object[]{ "com.example.package-info" },
        new Object[]{ "com.example.OmitTypeExample" }
      };
  }

  @Test( dataProvider = "successfulCompiles" )
  public void processSuccessfulCompile( @Nonnull final String classname )
    throws Exception
  {
    final String output =
      toFilename( "expected/" + GrimProcessor.BASE_RESOURCE_PATH, classname, "", GrimProcessor.SUFFIX );
    assertSuccessfulCompile( inputs( classname ), Collections.singletonList( output ) );
  }

  @Test
  public void processSuccessfulCompileOnNestedClass()
    throws Exception
  {
    final String classname = "com.example.OmitOnNestedClassExample";
    final String outputFilename = "expected/META-INF/grim/com/example/OmitOnNestedClassExample$Foo.grim.json";
    assertSuccessfulCompile( inputs( classname ),
                             Collections.singletonList( outputFilename ) );
  }

  @Nonnull
  @Override
  protected Processor processor()
  {
    return new GrimProcessor();
  }

  @Nonnull
  @Override
  protected String getOptionPrefix()
  {
    return "grim";
  }
}
