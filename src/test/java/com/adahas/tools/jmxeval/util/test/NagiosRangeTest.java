package com.adahas.tools.jmxeval.util.test;

import com.adahas.tools.jmxeval.util.NagiosRange;

import org.testng.annotations.Test;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

@Test
public class NagiosRangeTest {

  @Test(dataProviderClass = NagiosRangeDataProvider.class, dataProvider = "validRangeSpecifications")
  public void testNagiosRange(String rangeSpec)
  {
    new NagiosRange(rangeSpec);
    new NagiosRange("@" + rangeSpec);
  }

  // @todo Overhaul this test, 28.12.2014
  public void badNagiosRangeAreDetected()
  {
    String [] range_specs = { "20:10", "x", "@x"};
    for (String range_spec:range_specs)
    {
      try {
        new NagiosRange(range_spec);
        fail("no exception generated for bad range="+range_spec);
      } catch (Exception e) {
        // ignored
      }
      try {
        new NagiosRange("@"+range_spec);
        fail("no exception generated for bad inverted range="+range_spec);
      } catch (Exception e) {
        // ignored
      }
    }
  }

  @Test(dataProviderClass = NagiosRangeDataProvider.class, dataProvider = "valuesInRange10U20")
  public void valuesInExclusiveNagiosRangeAreAccepted(Double value) {
    NagiosRange range = new NagiosRange("10:20");

    assertTrue(range.isValueOK(value));
  }

  @Test(dataProviderClass = NagiosRangeDataProvider.class, dataProvider = "valuesOutsideRange10U20")
  public void valuesInInclusiveNagiosRangeAreAccepted(Double value) {
    NagiosRange range = new NagiosRange("@10:20");

    assertTrue(range.isValueOK(value));
  }

  @Test(dataProviderClass = NagiosRangeDataProvider.class, dataProvider = "valuesOutsideRange10U20")
  public void valuesOutsideExclusiveNagiosRangeAreNotAccepted(Double value) {
    NagiosRange range = new NagiosRange("10:20");

    assertFalse(range.isValueOK(value));
  }

  @Test(dataProviderClass = NagiosRangeDataProvider.class, dataProvider = "valuesInRange10U20")
  public void valuesOutsideInclusiveNagiosRangeAreNotAccepted(Double value) {
    NagiosRange range = new NagiosRange("@10:20");

    assertFalse(range.isValueOK(value));
  }

  // @todo Overhaul this test, 28.12.2014
  public void isValueOK_NI() {
    String range_spec = "~:0";
    double[] good_values = {Double.NEGATIVE_INFINITY, -2000.1, 0, -0};
    double[] bad_values  = {Double.POSITIVE_INFINITY, 0.1, 200.2134};
    testRangeValues(range_spec, good_values, bad_values);
    testRangeValues("@"+range_spec, bad_values, good_values);    
  }

  // @todo Overhaul this test, 28.12.2014
  public void isValueOK_PI() {
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
