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

public class HelpPageTest extends ParentTest {

    @Test
    public void displaysPageObjects() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.openHelp();
        helpPage.assertPageObjects();
    }

    @Test
    public void routesToSettingsPage_backButton() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.openHelp();
        helpPage.clickBackButton();
        settingsPage.assertPageObjects();
    }

    @Test
    public void routesToSettingsPage_closeButton() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.openHelp();
        helpPage.clickCloseButton();
        settingsPage.assertPageObjects();
    }

    @Test
    public void routesToReportProblemPage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.clickSettingsButton();
        settingsPage.openHelp();
        helpPage.clickReportProblemText();
        reportProblemPage.assertPageObjects();
    }
}

