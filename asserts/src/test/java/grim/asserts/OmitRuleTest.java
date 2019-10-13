package grim.asserts;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class OmitRuleTest
{
  @Test
  public void basicOperation()
  {
    final Pattern typePattern = Pattern.compile( "^\\Qarez.ArezContext\\E$" );
    final Pattern memberPattern = Pattern.compile( "^\\QgetName\\E$" );
    final Condition condition = new Condition( "arez.enable_names", "true", false );

    final OmitRule rule = new OmitRule( typePattern, memberPattern, condition );
    assertEquals( rule.getType(), typePattern );
    assertEquals( rule.getMember(), memberPattern );
    assertEquals( rule.getCondition(), condition );

    final Map<String, String> compileTimeProperties = new HashMap<>();
    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getName" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.ArezContext", "getNextNodeId" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.Node", "getName" ) );

    compileTimeProperties.put( "arez.enable_names", "true" );

    assertFalse( rule.matches( compileTimeProperties, "arez.ArezContext", "getName" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.ArezContext", "getNextNodeId" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.Node", "getName" ) );

    compileTimeProperties.put( "arez.enable_names", "false" );

    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getName" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.ArezContext", "getNextNodeId" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.Node", "getName" ) );
  }

  @Test
  public void basicOperation_noCondition()
  {
    final Pattern typePattern = Pattern.compile( "^\\Qarez.ArezContext\\E$" );
    final Pattern memberPattern = Pattern.compile( "^\\QgetName\\E$" );

    final OmitRule rule = new OmitRule( typePattern, memberPattern, null );
    assertEquals( rule.getType(), typePattern );
    assertEquals( rule.getMember(), memberPattern );
    assertNull( rule.getCondition() );

    final Map<String, String> compileTimeProperties = new HashMap<>();
    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getName" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.ArezContext", "getNextNodeId" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.Node", "getName" ) );
  }

  @Test
  public void basicOperation_noCondition_noMember()
  {
    final Pattern typePattern = Pattern.compile( "^\\Qarez.ArezContext\\E$" );

    final OmitRule rule = new OmitRule( typePattern, null, null );
    assertEquals( rule.getType(), typePattern );
    assertNull( rule.getMember() );
    assertNull( rule.getCondition() );

    final Map<String, String> compileTimeProperties = new HashMap<>();
    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getName" ) );
    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getNextNodeId" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.Node", "getName" ) );
  }

  @Test
  public void basicOperation_noMember()
  {
    final Pattern typePattern = Pattern.compile( "^\\Qarez.ArezContext\\E$" );
    final Condition condition = new Condition( "arez.enable_names", "true", false );

    final OmitRule rule = new OmitRule( typePattern, null, condition );
    assertEquals( rule.getType(), typePattern );
    assertNull( rule.getMember() );
    assertEquals( rule.getCondition(), condition );

    final Map<String, String> compileTimeProperties = new HashMap<>();
    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getName" ) );
    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getNextNodeId" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.Node", "getName" ) );

    compileTimeProperties.put( "arez.enable_names", "true" );

    assertFalse( rule.matches( compileTimeProperties, "arez.ArezContext", "getName" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.ArezContext", "getNextNodeId" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.Node", "getName" ) );

    compileTimeProperties.put( "arez.enable_names", "false" );

    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getName" ) );
    assertTrue( rule.matches( compileTimeProperties, "arez.ArezContext", "getNextNodeId" ) );
    assertFalse( rule.matches( compileTimeProperties, "arez.Node", "getName" ) );
  }
}
