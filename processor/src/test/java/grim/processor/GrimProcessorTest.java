package grim.processor;

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
        new Object[]{ "com.example.OmitClinitExample" },
        new Object[]{ "com.example.OmitConstructorExample" },
        new Object[]{ "com.example.OmitFieldExample" },
        new Object[]{ "com.example.OmitMethodExample" },
        new Object[]{ "com.example.OmitOnNestedClassExample" },
        new Object[]{ "com.example.OmitPatternExample" },
        new Object[]{ "com.example.OmitTypeExample" }
      };
  }

  @Test( dataProvider = "successfulCompiles" )
  public void processSuccessfulCompile( @Nonnull final String classname )
    throws Exception
  {
    assertGrimRulesGenerated( classname );
  }
}
