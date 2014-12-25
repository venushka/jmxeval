package com.adahas.tools.jmxeval.util.test;

import org.testng.annotations.DataProvider;

public class NagiosRangeDataProvider
{
    @DataProvider(name = "validRangeSpecifications")
    public static Object[][] validRangeSpecifications() {
        return new Object[][] {
            {"10"},
            {"10:"},
            {"~:10"},
            {"10:20"},
            {":10"},
            {"10:~"},
            {"~:~"},
            {":"},
            {":~"},
            {"~:"},
            {""},
        };
    }
}
