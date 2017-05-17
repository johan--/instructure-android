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

import android.app.Activity;
import android.support.test.espresso.ViewInteraction;

import com.instructure.espresso.ActivityHelper;
import com.instructure.parentapp.R;
import com.instructure.parentapp.ui.utils.BasePage;
import com.instructure.parentapp.ui.models.Parent;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class ForgotPasswordPage extends BasePage {

    //region UI Element Locator Methods

    private ViewInteraction emailField() {
        return onView(withId(R.id.email));
    }

    private ViewInteraction requestPasswordButton() {
        return onView(withId(R.id.requestPassword));
    }

    private ViewInteraction emailNotFoundNotification() {
        Activity activity = ActivityHelper.currentActivity();
        return onView(withText(R.string.password_reset_no_user))
                .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        emailField().check(matches(isDisplayed()));
        requestPasswordButton().check(matches(isDisplayed()));
    }

    public void assertEmailNotFoundNotification() {
        emailNotFoundNotification().check(matches(isDisplayed()));
    }

    //endregion

    //region UI Action Helpers

    public void clickRequestPasswordButton() {
        requestPasswordButton().perform(click());
    }

    public void enterEmail(String email) {
        emailField().perform(replaceText(email));
    }

    public void requestPassword(Parent parent) {
        enterEmail(parent.username);
        clickRequestPasswordButton();
    }

    //endregion
}
