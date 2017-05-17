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

package com.instructure.parentapp.ui.pages;

import android.support.test.espresso.ViewInteraction;

import com.instructure.parentapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.instructure.espresso.WaitForCheckMatcher.waitFor;
import static com.instructure.espresso.WaitForViewMatcher.waitForView;

public class DashboardPage {

    //region UI Element Locator Methods

    private ViewInteraction settingsButton() {
        return waitForView(withId(R.id.settings));
    }

    private ViewInteraction coursesTabButton() {
        return waitForView(withContentDescription(R.string.courses));
    }

    private ViewInteraction weekTabButton() {
        return waitForView(withContentDescription(R.string.week));
    }

    private ViewInteraction alertsTabButton() {
        return waitForView(withContentDescription(R.string.alerts));
    }

    private ViewInteraction noCoursesText() {
        return onView(withText(R.string.noCourses));
    }

    private ViewInteraction firstStudentName() {
        return studentNameAtRow(0);
    }

    private ViewInteraction studentNameAtRow(int withIndex) {
        return onView(withContentDescription("name_text_" + withIndex));
    }

    private ViewInteraction studentName(String name) { return waitForView(withText(name)); }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        settingsButton().check(matches(isDisplayed()));
        coursesTabButton().check(matches(waitFor(isDisplayed())));
        weekTabButton().check(matches(waitFor(isDisplayed())));
        alertsTabButton().check(matches(waitFor(isDisplayed())));
    }

    //region UI Action Helpers

    public void clickSettingsButton() {
        settingsButton().perform(click());
    }

    public void clickFirstStudentName() {
        firstStudentName().perform(click());
    }

    public void clickStudentWithText(String name) {
        studentName(name).perform(click());
    }

    public void openThresholds() {
        clickSettingsButton();
        clickFirstStudentName();
    }

    public void openThresholds(String studentName) {
        clickSettingsButton();
        clickStudentWithText(studentName);
    }

    //endregion
}
