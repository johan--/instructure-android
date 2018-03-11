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

package com.ebuki.portal.test.adapter;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.ebuki.portal.adapter.CalendarListRecyclerAdapter;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.ScheduleItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class CalendarListRecyclerAdapterTest extends InstrumentationTestCase {
    private CalendarListRecyclerAdapter mAdapter;

    /**
     * Make it so the protected constructor can be called
     */
    public static class CalendarListRecyclerAdapterWrapper extends CalendarListRecyclerAdapter {
        protected CalendarListRecyclerAdapterWrapper(Context context) {
            super(context);
        }
    }

    @Before
    public void setup(){
        mAdapter = new CalendarListRecyclerAdapterWrapper(RuntimeEnvironment.application.getApplicationContext());
    }

    @Test
    public void testAreContentsTheSame_noAssignmentSame(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1");
        scheduleItem1.setStartDate(new Date());
        assertTrue(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem1));
    }

    @Test
    public void testAreContentsTheSame_noAssignmentDifferentName(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1a");
        Date date = new Date();
        scheduleItem1.setStartDate(date);
        ScheduleItem scheduleItem2 = new ScheduleItem();
        scheduleItem2.setTitle("ScheduleItem1b");
        scheduleItem2.setStartDate(date);
        assertFalse(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2));
    }

    @Test
    public void testAreContentsTheSame_noAssignmentDifferentDate(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1a");
        scheduleItem1.setStartDate(new Date(Calendar.getInstance().getTimeInMillis() - 1000));
        ScheduleItem scheduleItem2 = new ScheduleItem();
        scheduleItem2.setTitle("ScheduleItem1a");
        scheduleItem2.setStartDate(new Date(Calendar.getInstance().getTimeInMillis() + 1000));
        assertFalse(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2));
    }

    @Test
    public void testAreContentsTheSame_sameAssignment(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1");
        scheduleItem1.setStartDate(new Date());
        Assignment assignment1 = new Assignment();
        assignment1.setDueDate(new Date());
        scheduleItem1.setAssignment(assignment1);
        assertTrue(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem1));
    }

    @Test
    public void testAreContentsTheSame_differentAssignment(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1");
        Date date = new Date();
        scheduleItem1.setStartDate(date);
        Assignment assignment1 = new Assignment();
        assignment1.setDueDate(new Date(Calendar.getInstance().getTimeInMillis() - 1000));
        scheduleItem1.setAssignment(assignment1);

        ScheduleItem scheduleItem2 = new ScheduleItem();
        scheduleItem2.setTitle("ScheduleItem1");
        scheduleItem2.setStartDate(date);
        Assignment assignment2 = new Assignment();
        assignment2.setDueDate(new Date(Calendar.getInstance().getTimeInMillis() + 1000));
        scheduleItem2.setAssignment(assignment2);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2));
    }

    @Test
    public void testAreContentsTheSame_nullAssignment() {
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1");
        Date date = new Date();
        scheduleItem1.setStartDate(date);
        Assignment assignment1 = new Assignment();
        assignment1.setDueDate(date);
        scheduleItem1.setAssignment(assignment1);

        ScheduleItem scheduleItem2 = new ScheduleItem();
        scheduleItem2.setTitle("ScheduleItem1");
        scheduleItem2.setStartDate(date);
        Assignment assignment2 = null;
        scheduleItem2.setAssignment(assignment2);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2));
    }
}
