/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */


package com.instructure.canvasapi2.utils;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class NumberHelperTest {

    @Test
    public void doubleToPercentage_TestHundred() throws Exception {
        String hundredPercent = "100%";
        double hundredDouble = 100.0;

        assertEquals(hundredPercent, NumberHelper.doubleToPercentage(hundredDouble));
    }

    @Test
    public void doubleToPercentage_TestPercent() throws Exception {
        String percent = "%";
        double percentDouble = 76.43;

        assertEquals(true, NumberHelper.doubleToPercentage(percentDouble).contains(percent));
    }

    @Test
    public void doubleToPercentage_TestTwoNumbersAfterDecimal() throws Exception {
        String percentString = "48.48%";
        double percentDouble = 48.48443;

        assertEquals(percentString, NumberHelper.doubleToPercentage(percentDouble));
    }

    @Test
    public void doubleToPercentage_TestTwoNumbersAfterDecimalRoundUp() throws Exception {
        String percentString = "86.59%";
        double percentDouble = 86.58954;

        assertEquals(percentString, NumberHelper.doubleToPercentage(percentDouble));
    }

    @Test
    public void doubleToPercentage_TestLargePercentComma() throws Exception {
        String percentString = "1,056.34%";
        double percentDouble = 1056.34;

        assertEquals(percentString, NumberHelper.doubleToPercentage(percentDouble));
    }

    @Test
    public void formatDecimal_TwoDecimalPlaces() {
        String expected = "12,345.67";
        double input = 12345.66789;
        String output = NumberHelper.formatDecimal(input, 2, false);
        assertEquals(expected, output);
    }

    @Test
    public void formatDecimal_TwoDecimalPlacesNoTrim() {
        String expected = "12,345.00";
        double input = 12345.000000;
        String output = NumberHelper.formatDecimal(input, 2, false);
        assertEquals(expected, output);
    }

    @Test
    public void formatDecimal_NoDecimalPlaces() {
        String expected = "12,345";
        double input = 12345.6789;
        String output = NumberHelper.formatDecimal(input, 0, false);
        assertEquals(expected, output);
    }

    @Test
    public void formatDecimal_TrimZeroWithZeroDigits() {
        String expected = "12,345";
        double input = 12345.000001;
        String output = NumberHelper.formatDecimal(input, 3, true);
        assertEquals(expected, output);
    }

    @Test
    public void formatDecimal_TrimZeroWithNonZeroDigits() {
        String expected = "12,345.678";
        double input = 12345.67789;
        String output = NumberHelper.formatDecimal(input, 3, true);
        assertEquals(expected, output);
    }


}