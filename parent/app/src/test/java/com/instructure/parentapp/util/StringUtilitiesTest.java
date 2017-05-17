/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.parentapp.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtilitiesTest {

    //region isEmpty
    @Test
    public void isEmpty_TestEmptyNoSpace() {
        assertTrue(StringUtilities.isEmpty(""));
    }

    @Test
    public void isEmpty_TestEmptySpace() {
        assertFalse(StringUtilities.isEmpty(" "));
    }

    @Test
    public void isEmpty_TestNotEmptySingle() {
        assertFalse(StringUtilities.isEmpty("Not Empty"));
    }

    @Test
    public void isEmpty_TestNotEmptyVarArgs() {
        assertFalse(StringUtilities.isEmpty("Not", "Empty"));
    }

    @Test
    public void isEmpty_TestNoParams() {
        assertFalse(StringUtilities.isEmpty());
    }

    @Test
    public void isEmpty_TestNullParam() {
        assertTrue(StringUtilities.isEmpty((String) null));
    }
    //endregion

    //region isStringNumeric
    @Test
    public void isStringNumeric_TestEmptySpaceString() {
        assertFalse(StringUtilities.isStringNumeric(" "));
    }

    @Test
    public void isStringNumeric_TestEmptyString() {
        assertFalse(StringUtilities.isStringNumeric(""));
    }

    @Test
    public void isStringNumeric_TestNullString() {
        assertFalse(StringUtilities.isStringNumeric(null));
    }

    @Test
    public void isStringNumeric_TestAlphaNumeric() {
        assertFalse(StringUtilities.isStringNumeric("123ABC"));
    }

    @Test
    public void isStringNumeric_TestNumeric() {
        assertTrue(StringUtilities.isStringNumeric("234"));
    }

    @Test
    public void isStringNumeric_TestMinusSignFront() {
        assertTrue(StringUtilities.isStringNumeric("-234"));
    }

    @Test
    public void isStringNumeric_TestMinusSignBack() {
        assertFalse(StringUtilities.isStringNumeric("234-"));
    }

    @Test
    public void isStringNumeric_TestDecimal() {
        assertTrue(StringUtilities.isStringNumeric("23.123"));
    }
    //endregion

    //region simplifyHTML
    @Test
    public void simplifyHTML_TestNullString() {
        assertEquals("", StringUtilities.simplifyHTML(null));
    }

    @Test
    public void simplifyHTML_TestEmptyString() {
        assertEquals("", StringUtilities.simplifyHTML(" "));
    }

    @Test
    public void simplifyHTML_TestObjChar() {
        assertEquals("Should be", StringUtilities.simplifyHTML("Should\uFFFCbe"));
    }

    @Test
    public void simplifyHTML_TestRegularString() {
        assertEquals("http://whatever", StringUtilities.simplifyHTML("http://whatever"));
    }
    //endregion
}