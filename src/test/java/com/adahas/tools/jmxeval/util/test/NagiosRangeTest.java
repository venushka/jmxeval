package com.adahas.tools.jmxeval.util.test;

import com.adahas.tools.jmxeval.util.NagiosRange;
import static org.junit.Assert.*;

import org.junit.Test;

public class NagiosRangeTest {

  @Test
  public void testNagiosRange() {
    String [] range_specs = {
        "10",
        "10:",
        "~:10",
        "10:20",
        ":10",
        "10:~",
        "~:~",
        ":",
        ":~",
        "~:",
        "",
        };
    for (String range_spec:range_specs)
    {
      try {
        NagiosRange range  = new NagiosRange(range_spec);
      } catch (Exception e) {
        fail("exception for range:"+range_spec+" message: "+e.getMessage());
      }
    }
    for (String range_spec:range_specs)
    {
      range_spec = "@"+range_spec;
      try {
        NagiosRange range  = new NagiosRange(range_spec);
      } catch (Exception e) {
        fail("exception for range="+range_spec+" message: "+e.getMessage());
      }
    }
  }

  @Test
  public void testBadNagiosRange()
  {
    String [] range_specs = { "20:10", "x", "@x"};
    for (String range_spec:range_specs)
    {
      try {
        NagiosRange range = new NagiosRange(range_spec);
        fail("no exception generated for bad range="+range_spec);
      } catch (Exception e) {
        // ignored
      }
      try {
        NagiosRange irange = new NagiosRange("@"+range_spec);
        fail("no exception generated for bad inverted range="+range_spec);
      } catch (Exception e) {
        // ignored
      }
    }
  }
  
  @Test
  public void testIsValueOK() {
    String range_spec = "10:20";
    double[] good_values = {10, 20, 15};
    double[] bad_values  = {9.99, 20.001, -1, 0};
    testRangeValues(range_spec, good_values, bad_values);
    testRangeValues("@"+range_spec, bad_values, good_values);
  }
  
  @Test
  public void testIsValueOK_NI() {
    String range_spec = "~:0";
    double[] good_values = {Double.NEGATIVE_INFINITY, -2000.1, 0, -0};
    double[] bad_values  = {Double.POSITIVE_INFINITY, 0.1, 200.2134};
    testRangeValues(range_spec, good_values, bad_values);
    testRangeValues("@"+range_spec, bad_values, good_values);    
  }
  
  @Test
  public void testIsValueOK_PI() {
    String range_spec = "0:~";
    double[] good_values = {Double.POSITIVE_INFINITY, 0, 200.2134};
    double[] bad_values  = {Double.NEGATIVE_INFINITY, -2000.1, -0.1};
    testRangeValues(range_spec, good_values, bad_values);
    testRangeValues("@"+range_spec, bad_values, good_values);    
  }

  private void testRangeValues(String range_spec, double[] good_values, double[] bad_values) {
    NagiosRange range = new NagiosRange(range_spec);
    for (double value:good_values)
    {
      assertTrue("range="+range_spec+" value="+value, range.isValueOK(value));
    }
    for (double value:bad_values)
    {
      assertFalse("range="+range_spec+" value="+value, range.isValueOK(value));
    }
    
  }
  
}
