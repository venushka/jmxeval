package com.adahas.tools.jmxeval.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.adahas.tools.jmxeval.exception.JMXEvalException;

/**
 * Implements standard nagios-plugin range checks.
 *
 * @author Christopher B. Liebman
 * @see http://nagiosplug.sourceforge.net/developer-guidelines.html#THRESHOLDFORMAT
 */
public class NagiosRange {

  private static final Pattern RANGE_PATTERN = Pattern.compile("^(?<inside>@?)(((?<start>[-\\.0-9]+|~|):)|)(?<end>[-\\.0-9]+|~|)");

  private double start;
  private double end;
  private final boolean inside;

  /**
   * Construct based on range string
   *
   * @param range
   * @throws JMXEvalException if the range definition is invalid
   */
  public NagiosRange(final String range) throws JMXEvalException {
    final Matcher matcher = RANGE_PATTERN.matcher(range);

    //
    // Check if the range is in the correct syntax
    //
    if (!matcher.matches()) {
      throw new JMXEvalException("Invalid range definition: [" + range + "]");
    }

    final String insideStr = StringUtils.trimToEmpty(matcher.group("inside"));
    final String startStr = StringUtils.trimToEmpty(matcher.group("start"));
    final String endStr = StringUtils.trimToEmpty(matcher.group("end"));

    //
    // If the range starts with @ then the alert is if the
    // value is inside the range instead of the default outside.
    //
    inside = "@".equals(insideStr);

    //
    // set the start value.  default is 0.0 and ~ means negative infinity
    //
    try {
      start = toValue(startStr, 0.0, Double.NEGATIVE_INFINITY);
    } catch (NumberFormatException e) {
      throw new JMXEvalException("Bad start value [" + startStr + "] in range [" + range + "]", e);
    }

    //
    // set the end value default is infinity and ~ also means infinity
    //
    try {
      end = toValue(endStr, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    } catch (NumberFormatException e) {
      throw new JMXEvalException("Bad end value [" + endStr + "] in range: [" + range + "]", e);
    }

    //
    // Start MUST NOT be greater than end!
    //
    if (start > end) {
      throw new JMXEvalException("Range start (" + start + ") MUST NOT be greater than end (" + end + ")!");
    }
  }

  /**
   * Convert the string to double value.
   *
   * @param valueStr String to convert
   * @param undef Value to use if the string is empty
   * @param infinity Value to use if the string represents infinity
   * @return double value
   */
  private double toValue(final String valueStr, final double undef, final double infinity) {
    if (valueStr.isEmpty()) {
      return undef;
    } else if ("~".equals(valueStr)) {
      return infinity;
    } else {
      return Double.parseDouble(valueStr);
    }
  }

  /**
   * Tests if value is OK with respect to the nagios range.
   *
   * @param value value to test
   * @return true if value is OK and false f an alert should be raised.
   */
  public boolean isInRange(double value) {
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
}
