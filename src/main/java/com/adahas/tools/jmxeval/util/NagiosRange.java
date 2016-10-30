package com.adahas.tools.jmxeval.util;

import com.adahas.tools.jmxeval.exception.BadNagiosRangeException;

/**
 * Implements standard nagios-plugin range checks.
 *
 * @author Christopher B. Liebman
 * @see http://nagiosplug.sourceforge.net/developer-guidelines.html#THRESHOLDFORMAT
 */
public class NagiosRange {
  private double start;
  private double end;
  private boolean inside;

  /**
   * Construct based on range string
   *
   * @param range
   * @throws com.adahas.tools.jmxeval.exception.ConfigurationException if start value is greater than end value
   */
  public NagiosRange(final String range) {
    //
    // If the range starts with @ then the alert is if the
    // value is inside the range instead of the default outside.
    // And we remove the leading @ from the range string.
    //
    int rangeStart = 0;
    if (range.startsWith("@")) {
      inside = true;
      rangeStart = 1;
    }

    //
    // start string is optional and will only be set if
    // a colon is found seperating start from end specs.
    //
    String startStr = null;
    String endStr = range.substring(rangeStart);

    //
    // a colon separates start from end optionally
    //
    int colon = range.indexOf(':');

    if (colon >= rangeStart) {
      startStr = range.substring(rangeStart, colon);
      endStr = range.substring(colon + 1);
    }

    //
    // set the start value.  default is 0.0 and ~ means negative infinity
    //
    if (startStr == null || startStr.isEmpty()) {
      start = 0.0;
    } else if ("~".equals(startStr)) {
      start = Double.NEGATIVE_INFINITY;
    } else {
      try {
        start = Double.parseDouble(startStr);
      } catch (NumberFormatException nfe) {
        throw new BadNagiosRangeException("Bad start value '" + startStr + "' in range: '" + range + "'", nfe);
      }
    }

    //
    // set the end value default is infinity and ~ also means infinity
    //

    if (endStr == null || endStr.isEmpty() || "~".equals(endStr)) {
      end = Double.POSITIVE_INFINITY;
    } else {
      try {
        end = Double.parseDouble(endStr);
      } catch (NumberFormatException nfe) {
        throw new BadNagiosRangeException("Bad end value '" + endStr + "' in range: '" + range + "'", nfe);
      }
    }

    //
    // Start MUST NOT be greater than end!
    //
    if (start > end) {
      throw new BadNagiosRangeException("Range start (" + start + ") MUST NOT be greater than end (" + end + ")!");
    }
  }

  /**
   * Tests if value is OK with respect to the nagios range.
   *
   * @param value value to test
   * @return true if value is OK and false f an alert should be raised.
   */
  public boolean isValueOK(double value) {
    boolean result = false;

    if (inside) {
      if (value < start || value > end) {
        result = true;
      }
    } else {
      if (value >= start && value <= end) {
        result = true;
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return "start:" + start + " end:" + end;
  }
}
