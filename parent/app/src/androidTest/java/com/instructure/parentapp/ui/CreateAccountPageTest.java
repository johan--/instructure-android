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

public class CreateAccountPageTest extends ParentTest {
    @Before
    public void setup() {
        parentloginPage.clickCreateAccountButton();
    }

    @Test
    public void displaysPageObjects() {
        createAccountPage.assertPageObjects();
    }

    @Test
    public void routesToParentLoginPage() {
        createAccountPage.clickBackButton();
        parentloginPage.assertPageObjects();
    }

    // MBL-7047 [Parent] Create account does not display toast notification on error
    @Test
    public void displaysEmailAlreadyExistsNotification() {
        createAccountPage.createAccount(Data.getNextParent());
        createAccountPage.assertEmailAlreadyExistsNotification();
    }

    @Test
    public void nextButtonNotEnabled_emptyEmail() {
        createAccountPage.enterFormFields(Data.getNextParent());
        createAccountPage.enterEmail("");
        createAccountPage.assertNextButtonNotEnabled();
    }

    @Test
    public void nextButtonNotEnabled_emptyFirstName() {
        createAccountPage.enterFormFields(Data.getNextParent());
        createAccountPage.enterFirstName("");
        createAccountPage.assertNextButtonNotEnabled();
    }

    @Test
    public void nextButtonNotEnabled_emptyLastName() {
        createAccountPage.enterFormFields(Data.getNextParent());
        createAccountPage.enterLastName("");
        createAccountPage.assertNextButtonNotEnabled();
    }

    @Test
    public void nextButtonNotEnabled_emptyPassword() {
        createAccountPage.enterFormFields(Data.getNextParent());
        createAccountPage.enterPassword("");
        createAccountPage.assertNextButtonNotEnabled();
    }

    @Test
    public void nextButtonNotEnabled_emptyConfirmPassword() {
        createAccountPage.enterFormFields(Data.getNextParent());
        createAccountPage.enterConfirmPassword("");
        createAccountPage.assertNextButtonNotEnabled();
    }

    @Test
    public void nextButtonNotEnabled_passwordMismatch() {
        Parent parent = Data.getNextParent();
        createAccountPage.enterFormFields(parent);
        createAccountPage.enterPassword(parent.username);
        createAccountPage.assertNextButtonNotEnabled();
    }

    @Test
    public void nextButtonEnabled() {
        createAccountPage.enterFormFields(Data.getNextParent());
        createAccountPage.assertNextButtonEnabled();
    }
}
