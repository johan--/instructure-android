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
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class ReportProblemPage extends BasePage {

    //region UI Element Locator Methods

    private ViewInteraction reportProblemTitle() {
        return onView(withId(R.id.dialog_custom_title));
    }

    private ViewInteraction subjectText() {
        return onView(withId(R.id.subject));
    }

    private ViewInteraction subjectEditField() {
        return onView(withId(R.id.subjectEditText));
    }

    private ViewInteraction descriptionText() {
        return onView(withId(R.id.description));
    }

    private ViewInteraction descriptionEditField() {
        return onView(withId(R.id.descriptionEditText));
    }

    private ViewInteraction severityPromptText() {
        return onView(withId(R.id.severityPrompt));
    }

    private ViewInteraction severitySpinnerText() {
        return onView(withId(R.id.severitySpinner));
    }

    private ViewInteraction sendButton() {
        return onView(withId(R.id.dialog_custom_confirm));
    }

    private ViewInteraction cancelButton() {
        return onView(withId(R.id.dialog_custom_cancel));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        reportProblemTitle().check(matches(isDisplayed()));
        subjectText().check(matches(isDisplayed()));
        subjectEditField().check(matches(isDisplayed()));
        descriptionText().check(matches(isDisplayed()));
        descriptionEditField().check(matches(isDisplayed()));
        severityPromptText().check(matches(isDisplayed()));
        severitySpinnerText().check(matches(isDisplayed()));
        sendButton().check(matches(isDisplayed()));
        cancelButton().check(matches(isDisplayed()));
    }

    //endregion

    //region UI Action Helpers

    public void clickSendButton() {
        sendButton().perform(click());
    }

    public void clickCancelButton() {
        cancelButton().perform(click());
    }

    //endregion
}

