package com.adahas.tools.jmxeval.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NagiosRangeValueTest {

  private final String rangeSpec;
  private final Double value;
  private final boolean verifyValid;

  public NagiosRangeValueTest(final String rangeSpec, final Double value, final boolean verifyValid) {
    this.rangeSpec = rangeSpec;
    this.value = value;
    this.verifyValid = verifyValid;
  }

  @Parameters(name = "{index}: for range [{0}] value [{1}] should be valid [{2}]")
  public static Iterable<Object[]> values() {
    return Arrays.asList(new Object[][] {
      // values in range
      { "10:20", 10d, true },
      { "10:20", 20d, true },
      { "10:20", 15d, true },

      // values outside range
      { "10:20", 9.99d, false },
      { "10:20", 20.001d, false },
      { "10:20", -1d, false },
      { "10:20", 0d, false },

      // is value OK for negative infinity
      { "~:0", Double.NEGATIVE_INFINITY, true },
      { "~:0", -2000.1, true },
      { "~:0", 0.0, true },
      { "~:0", -0.0, true },

      // is value not OK for negative infinity
      { "~:0", Double.POSITIVE_INFINITY, false },
      { "~:0", 0.1, false },
      { "~:0", 200.2134, false },

      // is value OK for positive infinity
      { "0:~", Double.POSITIVE_INFINITY, true },
      { "0:~", 0.1, true },
      { "0:~", 200.2134, true },

      // is value not OK for positive infinity
      { "0:~", Double.NEGATIVE_INFINITY, false },
      { "0:~", -2000.1, false },
      { "0:~", -0.1, false }
    });
  }

  @Test
  public void testValue() throws Exception {
    final NagiosRange range = new NagiosRange(rangeSpec);
    if (verifyValid) {
      assertTrue("range=" + rangeSpec + " value=" + value, range.isInRange(value));
    } else {
      assertFalse("range=" + rangeSpec + " value=" + value, range.isInRange(value));
    }

    final NagiosRange rangeExcl = new NagiosRange("@" + rangeSpec);
    if (verifyValid) {
      assertFalse("range=" + rangeSpec + " value=" + value, rangeExcl.isInRange(value));
    } else {
      assertTrue("range=" + rangeSpec + " value=" + value, rangeExcl.isInRange(value));
    }
  }
}
