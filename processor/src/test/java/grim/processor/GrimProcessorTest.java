package grim.processor;

import java.util.Collections;
import javax.annotation.Nonnull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GrimProcessorTest
  extends AbstractGrimProcessorTest
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
    assertGrimRulesGenerated( classname );
  }

  @Test
  public void processSuccessfulCompileOnNestedClass()
    throws Exception
  {
    final String inputFilename = "input/com/example/OmitOnNestedClassExample.java";
    final String outputFilename = "expected/META-INF/grim/com/example/OmitOnNestedClassExample$Foo.grim.json";
    assertGrimRulesGenerated( Collections.singletonList( fixture( inputFilename ) ),
                              Collections.singletonList( outputFilename ) );
  }
}
