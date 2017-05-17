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

public class SettingsPageTest extends ParentTest {
    @Test
    public void displaysPageObjects() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.assertPageObjects();
    }

    @Test
    public void routesToDashboardPage_backButton() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.clickBackButton();
        dashboardPage.assertPageObjects();
    }

    @Test
    public void routesToDashBoardPage_closeButton() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.clickCloseButton();
        dashboardPage.assertPageObjects();
    }

    @Test
    public void routesToAddStudentDomainPage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.openAddStudent();
        addStudentDomainPage.assertPageObjects();
    }

    @Test
    public void routesToHelpPage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.openHelp();
        helpPage.assertPageObjects();
    }

    // This test will fail until MBL-7019 is resolved.
    @Test
    public void routesToParentLoginPage_confirmLogout() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.confirmLogout();
        parentloginPage.assertPageObjects();
    }

    @Test
    public void routesToSettingsPage_cancelLogout() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.cancelLogout();
        settingsPage.assertPageObjects();
    }

    @Test
    public void routesToThresholdsPage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.clickStudentNameText();
        thresholdsPage.assertPageObjects();
    }
}
