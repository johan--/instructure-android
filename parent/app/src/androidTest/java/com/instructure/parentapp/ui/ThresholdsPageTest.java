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

package com.instructure.parentapp.ui;

import com.instructure.parentapp.ui.data.Data;
import com.instructure.parentapp.ui.utils.ParentTest;

import org.junit.Test;

public class ThresholdsPageTest extends ParentTest {

    @Test
    public void displaysPageObjects() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.assertPageObjects();
    }

    @Test
    public void routesToSettingsPage_backButton() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.clickBackButton();
        settingsPage.assertPageObjects();
    }

    @Test
    public void routesToSettingsPage_closeButton() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.clickCloseButton();
        settingsPage.assertPageObjects();
    }

    @Test
    public void routesToThresholdCourseBelowPage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.clickCourseBelowValue();
        thresholdCourseBelowPage.assertPageObjects();
    }

    @Test
    public void routesToThresholdCourseAbovePage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.clickCourseAboveValue();
        thresholdCourseAbovePage.assertPageObjects();
    }

    @Test
    public void routesToThresholdAssignmentBelowPage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.clickAssignmentBelowValue();
        thresholdAssignmentBelowPage.assertPageObjects();
    }

    @Test
    public void routesToThresholdAssignmentAbovePage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.clickAssignmentAboveValue();
        thresholdAssignmentAbovePage.assertPageObjects();
    }

    @Test
    public void removeOnlyStudent() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.removeStudent();
        noStudentsPage.assertPageObjects();
    }

    @Test
    public void removeOneStudent() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.openThresholds();
        thresholdsPage.removeStudent();
        settingsPage.assertPageObjects();
    }
}
