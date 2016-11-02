package com.adahas.tools.jmxeval.response;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link Response}.
 */
public class ResponseTest {

  /**
   * Test simple check output format.
   */
  @Test
  public void testToStringSimpleResponse() {
    final Response response = new Response();
    response.addEvalResult(new EvalResult("memory", Status.CRITICAL, "used memory > 80%"));

    assertEquals("JMXEval memory CRITICAL - used memory > 80%", response.toString());
  }

  /**
   * Test simple check output format with performance data.
   */
  @Test
  public void testToStringSimpleResponseWithPerfData() {
    final Response response = new Response();
    response.addEvalResult(new EvalResult("memory", Status.CRITICAL, "used memory > 80%"));
    response.addPerfData(new PerfDataResult("memory", 221, "m", "150", "200", "0", "256"));

    assertEquals("JMXEval memory CRITICAL - used memory > 80% | memory=221m;150;200;0;256", response.toString());
  }

  /**
   * Test multiple check output format.
   */
  @Test
  public void testToStringMultipleCheckResponse() {
    final Response response = new Response();
    response.addEvalResult(new EvalResult("memory", Status.CRITICAL, "used memory > 80%"));
    response.addEvalResult(new EvalResult("cpu", Status.OK, null));

    assertEquals(
      "JMXEval CRITICAL - 2 checks, 1 critical [memory], 1 ok" + System.getProperty("line.separator") +
      "[1] memory CRITICAL - used memory > 80%" + System.getProperty("line.separator") +
      "[2] cpu OK", response.toString());
  }

  /**
   * Test multiple check output format with performance data
   */
  @Test
  public void testToStringMultipleCheckResponseWithPerfData() {
    final Response response = new Response();
    response.addEvalResult(new EvalResult("memory", Status.CRITICAL, "used memory > 80%"));
    response.addEvalResult(new EvalResult("cpu", Status.OK, null));
    response.addPerfData(new PerfDataResult("memory", 221, "m", "150", "200", "0", "256"));
    response.addPerfData(new PerfDataResult("cpu", 95, "%", "60", "80", null, null));

    assertEquals(
      "JMXEval CRITICAL - 2 checks, 1 critical [memory], 1 ok" + System.getProperty("line.separator") +
      "[1] memory CRITICAL - used memory > 80%" + System.getProperty("line.separator") +
      "[2] cpu OK | memory=221m;150;200;0;256 cpu=95%;60;80", response.toString());
  }
}