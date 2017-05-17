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
import com.instructure.parentapp.ui.utils.BasePage;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class SettingsPage extends BasePage {

    //region UI Element Locator Methods

    private ViewInteraction closeButton() {
        return onView(withContentDescription(R.string.close));
    }

    private ViewInteraction settingsTitle() {
        // TODO: needs resource-id or contentDescription
        return onView(withText(R.string.action_settings));
    }

    private ViewInteraction moreOptionsButton() {
        return onView(withContentDescription(android.support.v7.appcompat.R.string.abc_action_menu_overflow_description));
    }

    private ViewInteraction addStudentButton() {
        return onView(withText(R.string.addStudent));
    }

    private ViewInteraction helpButton() {
        return onView(withText(R.string.help));
    }

    private ViewInteraction logoutButton() {
        return onView(withText(R.string.logout));
    }

    private ViewInteraction confirmLogoutTitle() {
        return onView(withId(R.id.dialog_custom_title));
    }

    private ViewInteraction cancelLogoutButton() {
        return onView(withId(R.id.dialog_custom_cancel));
    }

    private ViewInteraction confirmLogoutButton() {
        return onView(withId(R.id.dialog_custom_confirm));
    }

    private ViewInteraction studentAvatar() {
        return studentAvatarAtRow(0);
    }

    private ViewInteraction studentAvatarAtRow(int withIndex) {
        return onView(withContentDescription("avatar_" + withIndex));
    }

    private ViewInteraction studentName() {
        return studentNameAtRow(0);
    }

    private ViewInteraction studentNameAtRow(int withIndex) {
        return onView(withContentDescription("name_text_" + withIndex));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        closeButton().check(matches(isDisplayed()));
        settingsTitle().check(matches(isDisplayed()));
        moreOptionsButton().check(matches(isDisplayed()));
    }

    //endregion

    //region UI Action Helpers

    public void clickCloseButton() {
        closeButton().perform(click());
    }

    public void clickMoreOptionsButton() {
        moreOptionsButton().perform(click());
    }

    public void clickAddStudentButton() {
        addStudentButton().perform(click());
    }

    public void clickHelpButton() {
        helpButton().perform(click());
    }

    public void clickLogoutButton() {
        logoutButton().perform(click());
    }

    public void openAddStudent() {
        clickMoreOptionsButton();
        clickAddStudentButton();
    }

    public void openHelp() {
        clickMoreOptionsButton();
        clickHelpButton();
    }

    public void openLogout() {
        clickMoreOptionsButton();
        clickLogoutButton();
    }

    public void cancelLogout() {
        openLogout();
        cancelLogoutButton().perform(click());
    }

    public void confirmLogout() {
        openLogout();
        confirmLogoutButton().perform(click());
    }

    public void clickStudentNameText() {
        studentName().perform(click());
    }

    //endregion
}
