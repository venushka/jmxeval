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

    @DataProvider(name = "valuesInRange10U20")
    public static Object[][] valuesInRange10U20() {
        return new Object[][] {
            {10d}, {20d}, {15d}
        };
    }

    @DataProvider(name = "valuesOutsideRange10U20")
    public static Object[][] valuesOutsideRange10U20() {
        return new Object[][] {
            {9.99d}, {20.001d}, {-1d}, {0d}
        };
    }
}
