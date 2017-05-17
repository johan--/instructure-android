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
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class ThresholdValuePage extends BasePage {

    private int mTitle;

    private ThresholdValuePage() {
    }

    private ThresholdValuePage(int title) {
        mTitle = title;
    }

    public static ThresholdValuePage courseBelow() {
        return new ThresholdValuePage(R.string.gradeBelow);
    }

    public static ThresholdValuePage courseAbove() {
        return new ThresholdValuePage(R.string.gradeAbove);
    }

    public static ThresholdValuePage assignmentBelow() {
        return new ThresholdValuePage(R.string.assignmentGradeBelow);
    }

    public static ThresholdValuePage assignmentAbove() {
        return new ThresholdValuePage(R.string.assignmentGradeAbove);
    }

    //region UI Element Locator Methods

    private ViewInteraction thresholdTitle() {
        return onView(withId(R.id.title));
    }

    private ViewInteraction percentageField() {
        // TODO: needs resource-id or contentDescription
        return onView(withHint(R.string.enterThreshold));
    }

    private ViewInteraction minMaxText() {
        return onView(withId(R.id.minMax));
    }

    private ViewInteraction cancelButton() {
        return onView(withId(R.id.buttonDefaultNegative));
    }

    private ViewInteraction neverButton() {
        return onView(withId(R.id.buttonDefaultNeutral));
    }

    private ViewInteraction saveButton() {
        return onView(withId(R.id.buttonDefaultPositive));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        thresholdTitle().check(matches(isDisplayed()));
        thresholdTitle().check(matches(withText(mTitle)));
        percentageField().check(matches(isDisplayed()));
        minMaxText().check(matches(isDisplayed()));
        cancelButton().check(matches(isDisplayed()));
        neverButton().check(matches(isDisplayed()));
        saveButton().check(matches(isDisplayed()));
    }

    //endregion

    //region UI Action Helpers

    public void clickCancelButton() {
        cancelButton().perform(click());
    }

    public void clickNeverButton() {
        neverButton().perform(click());
    }

    public void clickSaveButton() {
        saveButton().perform(click());
    }

    public void enterPercentage(String value) {
        percentageField().perform(replaceText(value));
    }

    //endregion
}
