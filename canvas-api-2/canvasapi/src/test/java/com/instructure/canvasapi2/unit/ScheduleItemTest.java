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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.instructure.canvasapi2.models.AssignmentOverride;
import com.instructure.canvasapi2.models.ScheduleItem;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

public class ScheduleItemTest {
    @Test
    public void getIdTest_Number() throws Exception {
        ScheduleItem scheduleItem = new ScheduleItem();
        scheduleItem.setId(43243);

        assertEquals(43243, scheduleItem.getId());
    }

    @Test
    public void getIdTest_Assignment() throws Exception {
        //can't set a schedule item's id to be a string by any exposed method, so use JSON parsing
        //to test it
        String scheduleItemJSON =
                "{\n\"all_day\": true,\n\"all_day_date\": \"2012-10-17\",\n\"created_at\": \"2012-10-06T01:09:52Z\",\n\"end_at\": \"2012-10-17T06:00:00Z\",\n\"id\": \"assignment_673956\",\n\"location_address\": null,\n\"location_name\": null,\n\"start_at\": \"2012-10-17T06:00:00Z\",\n\"title\": \"No Class\",\n\"updated_at\": \"2012-10-06T01:09:52Z\",\n\"workflow_state\": \"active\",\n\"description\": null,\n\"context_code\": \"course_833052\",\n\"child_events_count\": 0,\n\"parent_event_id\": null,\n\"hidden\": false,\n\"child_events\": [],\n\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/673956\",\n\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=673956&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n}";

        GsonBuilder builder = new GsonBuilder();

        Gson gson = builder.create();
        ScheduleItem scheduleItem = gson.fromJson(scheduleItemJSON, ScheduleItem.class);

        assertEquals(673956, scheduleItem.getId());
    }

    @Test
    public void getIdTest_AssignmentOverrides() throws Exception {
        //can't set a schedule item's id to be a string by any exposed method, so use JSON parsing
        //to test it
        String scheduleItemJSON =
                "{\n\"all_day\": true,\n\"all_day_date\": \"2012-10-17\",\n\"created_at\": \"2012-10-06T01:09:52Z\",\n\"end_at\": \"2012-10-17T06:00:00Z\",\n\"id\": \"assignment_673956\",\n\"location_address\": null,\n\"location_name\": null,\n\"start_at\": \"2012-10-17T06:00:00Z\",\n\"title\": \"No Class\",\n\"updated_at\": \"2012-10-06T01:09:52Z\",\n\"workflow_state\": \"active\",\n\"description\": null,\n\"context_code\": \"course_833052\",\n\"child_events_count\": 0,\n\"parent_event_id\": null,\n\"hidden\": false,\n\"child_events\": [],\n\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/673956\",\n\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=673956&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n}";

        GsonBuilder builder = new GsonBuilder();

        Gson gson = builder.create();
        ScheduleItem scheduleItem = gson.fromJson(scheduleItemJSON, ScheduleItem.class);

        AssignmentOverride assignmentOverride = new AssignmentOverride();
        assignmentOverride.setId(1234567);

        ArrayList<AssignmentOverride> assignmentOverrides = new ArrayList<>();
        assignmentOverrides.add(assignmentOverride);

        scheduleItem.setAssignmentOverrides(assignmentOverrides);
        assertEquals(1234567, scheduleItem.getId());
    }


    @Test
    public void getContextIdTest_User() throws Exception {
        ScheduleItem scheduleItem = new ScheduleItem();
        scheduleItem.setContextCode("user_12345");

        assertEquals(12345, scheduleItem.getContextId());
    }

    @Test
    public void getContextIdTest_Course() throws Exception {
        ScheduleItem scheduleItem = new ScheduleItem();
        scheduleItem.setContextCode("course_12345");

        assertEquals(12345, scheduleItem.getContextId());
    }

    @Test
    public void getContextIdTest_Group() throws Exception {
        ScheduleItem scheduleItem = new ScheduleItem();
        scheduleItem.setContextCode("group_12345");

        assertEquals(12345, scheduleItem.getContextId());
    }

    @Test
    public void getUserIdTest() throws Exception {
        ScheduleItem scheduleItem = new ScheduleItem();
        scheduleItem.setContextCode("user_12345");

        assertEquals(12345, scheduleItem.getUserId());
    }

    @Test
    public void getCourseIdTest() throws Exception {
        ScheduleItem scheduleItem = new ScheduleItem();
        scheduleItem.setContextCode("course_12345");

        assertEquals(12345, scheduleItem.getCourseId());
    }
}
