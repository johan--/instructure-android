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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class AddStudentDomainPage extends BasePage {

    //region UI Element Locator Methods

    private ViewInteraction addStudentImage() {
        return onView(withId(R.id.addStudentIcon));
    }

    private ViewInteraction domainField() {
        return onView(withId(R.id.school));
    }

    private ViewInteraction nextButton() {
        return onView(withId(R.id.finish));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        addStudentImage().check(matches(isDisplayed()));
        domainField().check(matches(isDisplayed()));
        nextButton().check(matches(isDisplayed()));
    }

    //endregion

    //region UI Action Helpers

    //endregion
}
