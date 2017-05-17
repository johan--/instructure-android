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
import com.instructure.parentapp.ui.models.Parent;

import org.junit.Test;

public class ParentLoginPageTest extends ParentTest {

    @Test
    public void displaysPageObjects() {
        parentloginPage.assertPageObjects();
    }

    @Test
    public void routesToAddStudentPage() {
        parentloginPage.loginAs(Data.getNextParent());
        noStudentsPage.assertPageObjects();
    }

    @Test
    public void routesToDashboardPage() {
        parentloginPage.loginAs(Data.getNextParent());
        dashboardPage.assertPageObjects();
    }

    @Test
    public void routesToCreateAccountPage() {
        parentloginPage.clickCreateAccountButton();
        createAccountPage.assertPageObjects();
    }

    @Test
    public void routesToForgotPasswordPage() {
        parentloginPage.clickForgotPasswordPage();
        forgotPasswordPage.assertPageObjects();
    }

    @Test
    public void loginButtonNotEnabled_emptyEmailField() {
        parentloginPage.enterPassword(Data.getNextParent().password);
        parentloginPage.assertLoginButtonNotEnabled();
    }

    @Test
    public void loginButtonNotEnabled_emptyPasswordField() {
        parentloginPage.enterEmail(Data.getNextParent().username);
        parentloginPage.assertLoginButtonNotEnabled();
    }

    @Test
    public void loginButtonEnabled() {
        Parent parent = Data.getNextParent();
        parentloginPage.enterEmail(parent.username);
        parentloginPage.enterPassword(parent.password);
        parentloginPage.assertLoginButtonEnabled();
    }

    @Test
    public void displaysInvalidLoginNotification() {
        Parent parent = Data.getNextParent();
        parentloginPage.enterEmail(parent.username);
        parentloginPage.enterPassword(parent.username);
        parentloginPage.clickLoginButton();
        parentloginPage.assertInvalidLoginNotifcationDisplayed();
    }
}
