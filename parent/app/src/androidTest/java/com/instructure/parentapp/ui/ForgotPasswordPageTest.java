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

import org.junit.Before;
import org.junit.Test;

public class ForgotPasswordPageTest extends ParentTest {
    @Before
    public void setup() {
        parentloginPage.clickForgotPasswordPage();
    }

    @Test
    public void displaysPageObjects() {
        forgotPasswordPage.assertPageObjects();
    }

    @Test
    public void routesToParentLoginPage() {
        forgotPasswordPage.clickBackButton();
        parentloginPage.assertPageObjects();
    }

    @Test
    public void displaysEmailNotFoundNotification() {
        Parent forgotPasswordParent = Data.getNextParent();
        String noUser = forgotPasswordParent.username + forgotPasswordParent.lastName;
        forgotPasswordPage.enterEmail(noUser);
        forgotPasswordPage.clickRequestPasswordButton();
        forgotPasswordPage.assertEmailNotFoundNotification();
    }
}
