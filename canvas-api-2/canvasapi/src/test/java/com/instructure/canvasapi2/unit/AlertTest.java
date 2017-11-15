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

import com.instructure.canvasapi2.models.Alert;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class AlertTest {

    @Test
    public void getAlertTypeFromString_COURSE_ANNOUNCEMENT() {
        assertEquals(Alert.ALERT_TYPE.COURSE_ANNOUNCEMENT, Alert.getAlertTypeFromString("course_announcement"));
    }

    @Test
    public void getAlertTypeFromString_INSTITUTION_ANNOUNCEMENT() {
        assertEquals(Alert.ALERT_TYPE.INSTITUTION_ANNOUNCEMENT, Alert.getAlertTypeFromString("institution_announcement"));
    }

    @Test
    public void getAlertTypeFromString_ASSIGNMENT_GRADE_HIGH() {
        assertEquals(Alert.ALERT_TYPE.ASSIGNMENT_GRADE_HIGH, Alert.getAlertTypeFromString("assignment_grade_high"));
    }

    @Test
    public void getAlertTypeFromString_ASSIGNMENT_GRADE_LOW() {
        assertEquals(Alert.ALERT_TYPE.ASSIGNMENT_GRADE_LOW, Alert.getAlertTypeFromString("assignment_grade_low"));
    }

    @Test
    public void getAlertTypeFromString_ASSIGNMENT_MISSING() {
        assertEquals(Alert.ALERT_TYPE.ASSIGNMENT_MISSING, Alert.getAlertTypeFromString("assignment_missing"));
    }

    @Test
    public void getAlertTypeFromString_COURSE_GRADE_HIGH() {
        assertEquals(Alert.ALERT_TYPE.COURSE_GRADE_HIGH, Alert.getAlertTypeFromString("course_grade_high"));
    }

    @Test
    public void getAlertTypeFromString_COURSE_GRADE_LOW() {
        assertEquals(Alert.ALERT_TYPE.COURSE_GRADE_LOW, Alert.getAlertTypeFromString("course_grade_low"));
    }

    @Test
    public void getAlertTypeFromString_empty() {
        assertEquals(null, Alert.getAlertTypeFromString(""));
    }

    @Test
    public void getAlertTypeFromString_null() {
        assertEquals(null, Alert.getAlertTypeFromString(null));
    }

    @Test
    public void alertTypeToAPIString_COURSE_ANNOUNCEMENT() {
        assertEquals("course_announcement", Alert.alertTypeToAPIString(Alert.ALERT_TYPE.COURSE_ANNOUNCEMENT));
    }

    @Test
    public void alertTypeToAPIString_INSTITUTION_ANNOUNCEMENT() {
        assertEquals("institution_announcement", Alert.alertTypeToAPIString(Alert.ALERT_TYPE.INSTITUTION_ANNOUNCEMENT));
    }

    @Test
    public void alertTypeToAPIString_ASSIGNMENT_GRADE_HIGH() {
        assertEquals("assignment_grade_high", Alert.alertTypeToAPIString(Alert.ALERT_TYPE.ASSIGNMENT_GRADE_HIGH));
    }

    @Test
    public void alertTypeToAPIString_ASSIGNMENT_GRADE_LOW() {
        assertEquals("assignment_grade_low", Alert.alertTypeToAPIString(Alert.ALERT_TYPE.ASSIGNMENT_GRADE_LOW));
    }

    @Test
    public void alertTypeToAPIString_ASSIGNMENT_MISSING() {
        assertEquals("assignment_missing", Alert.alertTypeToAPIString(Alert.ALERT_TYPE.ASSIGNMENT_MISSING));
    }

    @Test
    public void alertTypeToAPIString_COURSE_GRADE_HIGH() {
        assertEquals("course_grade_high", Alert.alertTypeToAPIString(Alert.ALERT_TYPE.COURSE_GRADE_HIGH));
    }


    @Test
    public void alertTypeToAPIString_COURSE_GRADE_LOW() {
        assertEquals("course_grade_low", Alert.alertTypeToAPIString(Alert.ALERT_TYPE.COURSE_GRADE_LOW));
    }

    @Test
    public void alertTypeToAPIString_null() {
        assertEquals(null, Alert.alertTypeToAPIString(null));
    }

    @Test
    public void alertTypeToAPIString_all() {
        for (Alert.ALERT_TYPE type : Alert.ALERT_TYPE.values()) {
            assertNotEquals("Expected non-null API string value for ALERT_TYPE." + type.name(), null, Alert.alertTypeToAPIString(type));
        }
    }

    @Test
    public void getAlertTypeFromString_all() {
        for (Alert.ALERT_TYPE type : Alert.ALERT_TYPE.values()) {
            String apiString = Alert.alertTypeToAPIString(type);
            assertEquals("Expected ALERT_TYPE." + type.name() + " for apiString '" + apiString + "'", type, Alert.getAlertTypeFromString(apiString));
        }
    }

}
