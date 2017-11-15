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

package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.utils.DateHelper;

import junit.framework.Assert;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class DateHelperTest {
    @Test
    public void stringToDate() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.US);
        Calendar calendar = Calendar.getInstance();
        //clear out milliseconds because we're not displaying that in the simple date format
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();
        String nowAsString = df.format(date);

        //add a 'Z' at the end. Discussion dates (where stringToDate is used) has a Z at the end of the string ("2037-07-28T19:38:31Z")
        //so we parse that out in the function
        Assert.assertEquals(date, DateHelper.stringToDate(nowAsString + "Z"));
    }

    @Test
    public void isSameDay_date() throws Exception {
        Date date = new Date();
        Date otherDate = new Date();
        otherDate.setTime(date.getTime());

        assertEquals(true, DateHelper.isSameDay(date, otherDate));
    }

    @Test
    public void isSameDay_dateDifferent() throws Exception {
        Date date = new Date();
        Date otherDate = new Date();
        long oneDay = 24 * 60 * 60 * 1000;
        otherDate.setTime(date.getTime() + oneDay);

        assertEquals(false, DateHelper.isSameDay(date, otherDate));
    }

    @Test
    public void isSameDay_calendar() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(calendar.getTimeInMillis());

        assertEquals(true, DateHelper.isSameDay(calendar, otherCalendar));
    }

    @Test
    public void isSameDay_calendarDifferentTimeOfDay() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(calendar.getTimeInMillis());
        otherCalendar.set(Calendar.HOUR_OF_DAY, 5);

        assertEquals(true, DateHelper.isSameDay(calendar, otherCalendar));
    }

    @Test
    public void isSameDay_calendarDifferentDay() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(calendar.getTimeInMillis());
        otherCalendar.add(Calendar.DAY_OF_MONTH, 1);

        assertEquals(false, DateHelper.isSameDay(calendar, otherCalendar));
    }

    @Test
    public void compareDays_equal() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(calendar.getTimeInMillis());

        assertEquals(0, DateHelper.compareDays(calendar, otherCalendar));
    }

    @Test
    public void compareDays_firstCalendarOlder() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(calendar.getTimeInMillis());
        otherCalendar.add(Calendar.DAY_OF_MONTH, 1);

        assertTrue(DateHelper.compareDays(calendar, otherCalendar) < 0);
    }

    @Test
    public void compareDays_secondCalendarOlder() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(calendar.getTimeInMillis());
        otherCalendar.add(Calendar.DAY_OF_MONTH, -1);

        assertTrue(DateHelper.compareDays(calendar, otherCalendar) > 0);
    }
}