package grim.asserts;

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ConditionTest
{
  @Test
  public void equalsCondition()
  {
    final Condition condition = new Condition( "arez.environment", "production", true );
    assertEquals( condition.getProperty(), "arez.environment" );
    assertEquals( condition.getValue(), "production" );
    assertTrue( condition.isEquals() );

    final Map<String, String> compileTimeProperties = new HashMap<>();

    assertFalse( condition.matches( compileTimeProperties ) );

    compileTimeProperties.put( "arez.other", "true" );

    assertFalse( condition.matches( compileTimeProperties ) );

    compileTimeProperties.put( "arez.environment", "production" );

    assertTrue( condition.matches( compileTimeProperties ) );

    compileTimeProperties.put( "arez.environment", "development" );

    assertFalse( condition.matches( compileTimeProperties ) );
  }

  @Test
  public void notEqualsCondition()
  {
    final Condition condition = new Condition( "arez.environment", "production", false );
    assertEquals( condition.getProperty(), "arez.environment" );
    assertEquals( condition.getValue(), "production" );
    assertFalse( condition.isEquals() );

    final Map<String, String> compileTimeProperties = new HashMap<>();

    assertTrue( condition.matches( compileTimeProperties ) );

    compileTimeProperties.put( "arez.environment", "production" );

    assertFalse( condition.matches( compileTimeProperties ) );

    compileTimeProperties.put( "arez.environment", "development" );

    assertTrue( condition.matches( compileTimeProperties ) );
  }
}
