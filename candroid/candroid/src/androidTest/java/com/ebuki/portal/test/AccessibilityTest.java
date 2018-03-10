/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ebuki.portal.test;

import android.support.test.runner.AndroidJUnit4;

import com.ebuki.portal.activity.LoginActivity;
import com.ebuki.portal.test.page.PageObjects;
import com.instructure.espresso.ScreenshotActivityTestRule;

import static com.ebuki.portal.test.utils.UserProfile.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.instructure.espresso.AccessibilityChecker.runChecks;

@RunWith(AndroidJUnit4.class)
public class AccessibilityTest extends PageObjects {
    @Rule
    public ScreenshotActivityTestRule<LoginActivity> mActivityRule =
            new ScreenshotActivityTestRule<>(LoginActivity.class);

    @Test
    public void checkDomainPickerPage() {
        runChecks(); // We're on the domain picker page by default
    }

    @Test
    public void checkLoginPage() {
        domainPickerPage.loadDefaultSchool();
        loginPage.login(INVALID_USER); // Login so we're on the login page before running checks
        runChecks();
    }
}
