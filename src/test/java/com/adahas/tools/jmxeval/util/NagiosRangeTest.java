package com.adahas.tools.jmxeval.util;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NagiosRangeTest {

  private final String rangeSpec;
  private final boolean verifyValid;

  public NagiosRangeTest(final String rangeSpec, final boolean verifyValid) {
    this.rangeSpec = rangeSpec;
    this.verifyValid = verifyValid;
  }

  @Parameters(name = "{index}: range specification [{0}] should be valid [{1}]")
  public static Iterable<Object[]> values() {
    return Arrays.asList(new Object[][] {
      { "10", true },
      { "10:", true },
      { "~:10", true },
      { "10:20", true },
      { ":10", true },
      { "10:~", true },
      { "~:~", true },
      { ":", true },
      { ":~", true },
      { "~:", true },
      { "", true },
      { "20:10", false },
      { "x", false },
      { "@x", false },
      { "x:y", false }
    });
  }

  @Test
  public void testRange() throws Exception {
    if (verifyValid) {
      new NagiosRange(rangeSpec);
      new NagiosRange("@" + rangeSpec);
    } else {
      try {
        new NagiosRange(rangeSpec);
        fail("no exception generated for bad range=" + rangeSpec);
      } catch (Exception e) {
        // ignored
      }
      try {
        new NagiosRange("@" + rangeSpec);
        fail("no exception generated for bad inverted range=" + rangeSpec);
      } catch (Exception e) {
        // ignored
      }
    }
  }
}
