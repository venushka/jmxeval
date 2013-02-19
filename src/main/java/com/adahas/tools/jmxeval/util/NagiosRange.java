package com.adahas.tools.jmxeval.util;

import com.adahas.tools.jmxeval.exception.BadNagiosRangeException;

/**
 * Implements standard nagios-plugin range checks.
 * 
 * @author Christopher B. Liebman
 * @see http://nagiosplug.sourceforge.net/developer-guidelines.html#THRESHOLDFORMAT
 */
public class NagiosRange {
  private double  start;
  private double  end;
  private boolean inside;

  /**
   * Construct based on range string
   * 
   * @param range
   * @throws ConfigurationException if start value is greater than end value
   */
  public NagiosRange(String range) {
    //
    // If the range starts with @ then the alert is if the
    // value is inside the range instead of the default outside.
    // And we remove the leading @ from the range string.
    //
    int range_start = 0;
    if (range.startsWith("@")) {
      inside = true;
      range_start = 1;
    }

    //
    // start string is optional and will only be set if
    // a colon is found seperating start from end specs.
    //
    
    String start_str = null;
    String end_str = range.substring(range_start);

    //
    // a colon separates start from end optionally
    //
    
    int colon = range.indexOf(":");

    if (colon >= range_start) {
      start_str = range.substring(range_start, colon);
      end_str = range.substring(colon + 1);
    }

    //
    // set the start value.  default is 0.0 and ~ means negative infinity
    //
    
    if (start_str == null || start_str.isEmpty()) {
      start = 0.0;
    } else if (start_str.equals("~")) {
      start = Double.NEGATIVE_INFINITY;
    } else {
      try {
        start = Double.parseDouble(start_str);
      } catch (NumberFormatException nfe) {
        throw new BadNagiosRangeException("bad start value:'"+start_str+"' in range:'"+range, nfe);
      }
    }

    //
    // set the end value default is infinity and ~ also means infinity
    //
    
    if (end_str == null || end_str.isEmpty() || end_str.equals("~")) {
      end = Double.POSITIVE_INFINITY;
    } else {
      try {
        end = Double.parseDouble(end_str);
      } catch (NumberFormatException nfe) {
        throw new BadNagiosRangeException("bad end value:'"+end_str+"' in range:'"+range, nfe);
      }
    }
    
    //
    // Start MUST NOT be greater than end!
    //
    if (start > end)
    {
      throw new BadNagiosRangeException("start ("+start+") MUST NOT be greater than end ("+end+")!");
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
      if (value < start) {
        result = true;
      } else if (value > end) {
        result = true;
      }
    } else {
      if (value >= start && value <= end) {
        result = true;
      }
    }

    return result;
  }
  
  public String toString() {
    return "start:"+start+" end:"+end;
  }
}
