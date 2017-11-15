/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberHelper {

    /**
     * Formats the a double as a String percentage, limited to two decimal places
     * @param number The number to be formatted
     * @return The formatted String
     */
    public static String doubleToPercentage(Double number) {
        return doubleToPercentage(number, 2);
    }

    /**
     * Formats the a double as a String percentage, limiting decimal places to the specified length
     * @param number The number to be formatted.
     * @param maxFractionDigits The maximum number of decimal places
     * @return The formatted String
     */
    public static String doubleToPercentage(Double number, int maxFractionDigits) {
        NumberFormat f = NumberFormat.getPercentInstance(Locale.getDefault());
        f.setMaximumFractionDigits(maxFractionDigits);
        return f.format(number/100);
    }

    public static String formatInt(long number) {
        return NumberFormat.getIntegerInstance().format(number);
    }

    /**
     * Formats a double value using the current locale settings
     * @param number The number to be formatted
     * @param decimalPlaces The number of decimal places to be printed
     * @param trimZero Whether to include decimal places if everything after the decimal would be zero.
     * @return The formatted string
     */
    public static String formatDecimal(double number, int decimalPlaces, boolean trimZero) {
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(decimalPlaces);
        if (!trimZero) {
            format.setMinimumFractionDigits(decimalPlaces);
            format.setDecimalSeparatorAlwaysShown(false);
        }
        if (decimalPlaces <= 0) {
            format.setRoundingMode(RoundingMode.FLOOR);
        }
        return format.format(number);
    }
}
