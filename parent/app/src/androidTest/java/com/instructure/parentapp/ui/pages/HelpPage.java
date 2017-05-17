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

public class HelpPage extends BasePage {

    //region UI Element Locator Methods

    private ViewInteraction closeButton() {
        return onView(withContentDescription(R.string.close));
    }

    private ViewInteraction helpTitle() {
        // TODO: needs resource-id or contentDescription
        return onView(withText(R.string.help));
    }
    private ViewInteraction searchCanvasGuidesText() {
        return onView(withId(R.id.search_canvas_guides_text));
    }

    private ViewInteraction findAnswersText() {
        return onView(withId(R.id.find_answers_text));
    }

    private ViewInteraction reportProblemText() {
        return onView(withId(R.id.report_problem_text));
    }

    private ViewInteraction letUsKnowText() {
        return onView(withId(R.id.let_us_know_text));
    }

    private ViewInteraction requestFeatureText() {
        return onView(withId(R.id.feature_request_text));
    }

    private ViewInteraction ideaText() {
        return onView(withId(R.id.idea_text));
    }

    private ViewInteraction shareLoveText() {
        return onView(withId(R.id.share_love_text));
    }

    private ViewInteraction favoritePartText() {
        return onView(withId(R.id.favorite_part_text));
    }

    private ViewInteraction openSourceText() {
        return onView(withId(R.id.open_source_text));
    }

    private ViewInteraction openSourceLicensesText() {
        return onView(withId(R.id.open_source_licenses_text));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        closeButton().check(matches(isDisplayed()));
        helpTitle().check(matches(isDisplayed()));
        searchCanvasGuidesText().check(matches(isDisplayed()));
        findAnswersText().check(matches(isDisplayed()));
        reportProblemText().check(matches(isDisplayed()));
        letUsKnowText().check(matches(isDisplayed()));
        requestFeatureText().check(matches(isDisplayed()));
        ideaText().check(matches(isDisplayed()));
        shareLoveText().check(matches(isDisplayed()));
        favoritePartText().check(matches(isDisplayed()));
        openSourceText().check(matches(isDisplayed()));
        openSourceLicensesText().check(matches(isDisplayed()));
    }

    //endregion

    //region UI Action Helpers

    public void clickCloseButton() {
        closeButton().perform(click());
    }

    public void clickReportProblemText() {
        reportProblemText().perform(click());
    }

    //endregion
}
