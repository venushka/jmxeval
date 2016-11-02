package com.adahas.tools.jmxeval.response;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link PerfDataResult}.
 */
public class PerfDataResultTest {

  /**
   * Check the output format for performance data.
   */
  @Test
  public void testToString() {
    assertEquals("memory=233m;200;250;0;256", new PerfDataResult("memory", 233, "m", "200", "250", "0", "256").toString());
    assertEquals("'Service up'=1", new PerfDataResult("Service up", "true", null, null, null, null, null).toString());
    assertEquals("'Orphan processes'=0;unknown;true", new PerfDataResult("Orphan processes", "false", null, "unknown", "true", null, null).toString());
  }
}

