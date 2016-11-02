package com.adahas.tools.jmxeval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.adahas.tools.jmxeval.exception.JMXEvalException;

public class ContextTest {

  @Test
  public void testSetVarString() throws Exception {
    final Context context = new Context();

    final String var1 = "val1";
    final Double var2 = 5.2d;
    final Integer var3 = new Integer(10);
    final boolean var4 = true;

    context.setVar("var1=" + var1);
    context.setVar("var2=" + var2);
    context.setVar("var3=" + var3);
    context.setVar("var4=" + var4);
    context.setVar("var5=");

    assertEquals(String.valueOf(var1), context.getVar("var1"));
    assertEquals(String.valueOf(var2), context.getVar("var2"));
    assertEquals(String.valueOf(var3), context.getVar("var3"));
    assertEquals(String.valueOf(var4), context.getVar("var4"));
    assertEquals("", context.getVar("var5"));

    try {
      context.setVar("var4=duplicate");
      fail("Should not allow setting already set variables");
    } catch (JMXEvalException e) {
      // expected
    }

    try {
      context.setVar("var6");
      fail("Should not allow variables without a value");
    } catch (JMXEvalException e) {
      // expected
    }
  }

  @Test
  public void testSetVarStringObject() throws Exception {
    final Context context = new Context();

    final String var1 = "val1";
    final Double var2 = 5.2d;
    final Integer var3 = new Integer(10);
    final boolean var4 = true;

    context.setVar("var1", var1);
    context.setVar("var2", var2);
    context.setVar("var3", var3);
    context.setVar("var4", var4);

    assertEquals(var1, context.getVar("var1"));
    assertEquals(var2, context.getVar("var2"));
    assertEquals(var3, context.getVar("var3"));
    assertEquals(var4, context.getVar("var4"));

    try {
      context.setVar("var4", "duplicate");
      fail("Should not allow setting already set variables");
    } catch (JMXEvalException e) {
      // expected
    }
  }


  @Test
  public void testGetVar() throws Exception {
    final Context context = new Context();

    context.setVar("var1", "val1");
    System.setProperty("ContextTest.var4", "val4");

    assertEquals("Incorrect value", "val1", context.getVar("var1"));
    assertEquals("Should return default", "testDefault", context.getVar("var2:testDefault"));
    assertNull("Should return null for undefined variables", context.getVar("var3"));
    assertEquals("Incorrect value", "val4", context.getVar("ContextTest.var4"));
  }
}

