package com.adahas.tools.jmxeval.response;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link EvalResult}.
 */
public class EvalResultTest {

  /**
   * Test construction.
   */
  @Test
  public void testEvalResult() {
    final EvalResult result = new EvalResult("memory", Status.CRITICAL, "used memory > 80%");
    assertEquals("Name", "memory", result.getName());
    assertEquals("Status", Status.CRITICAL, result.getStatus());
    assertEquals("Message", "used memory > 80%", result.getMessage());
  }

  /**
   * Check the output format for performance data.
   */
  @Test
  public void testToString() {
    assertEquals("memory CRITICAL - used memory > 80%", new EvalResult("memory", Status.CRITICAL, "used memory > 80%").toString());
    assertEquals("memory CRITICAL", new EvalResult("memory", Status.CRITICAL, null).toString());
  }
}

