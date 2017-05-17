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
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.instructure.espresso.WaitForCheckMatcher.waitFor;
import static com.instructure.espresso.WaitForViewMatcher.waitForView;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class ParentLoginPage extends BasePage {

    //region UI Element Locator Methods

    private ViewInteraction emailField() {
        return waitForView(withId(R.id.userName));
    }

    private ViewInteraction passwordField() {
        return onView(withId(R.id.password));
    }

    private ViewInteraction loginButton() {
        return onView(withId(R.id.next));
    }

    private ViewInteraction orText() {
        return onView(withId(R.id.or));
    }

    private ViewInteraction logInWithCanvasButton() {
        return onView(withId(R.id.canvasLogin));
    }

    private ViewInteraction createAccountButton() {
        return onView(withId(R.id.createAccount));
    }

    private ViewInteraction forgotPasswordButton() {
        return onView(withId(R.id.forgotPassword));
    }

    private ViewInteraction invalidLoginNotification() {
        Activity activity = ActivityHelper.currentActivity();
        return onView(withText(R.string.invalid_username_password))
                .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        emailField().check(matches(waitFor(isDisplayed())));
        passwordField().check(matches(isDisplayed()));
        loginButton().check(matches(isDisplayed()));
        orText().check(matches(isDisplayed()));
        logInWithCanvasButton().check(matches(isDisplayed()));
        createAccountButton().check(matches(isDisplayed()));
        forgotPasswordButton().check(matches(isDisplayed()));
    }

    public void assertLoginButtonEnabled() {
        loginButton().check(matches(isEnabled()));
    }

    public void assertLoginButtonNotEnabled() {
        loginButton().check(matches(not(isEnabled())));
    }

    public void assertInvalidLoginNotifcationDisplayed() {
        invalidLoginNotification().check(matches(isDisplayed()));
    }

    //endregion

    //region UI Action Helpers

    public void clickLogInWithCanvasButton() {
        logInWithCanvasButton().perform(click());
    }

    public void clickCreateAccountButton() {
        createAccountButton().perform(click());
    }

    public void clickForgotPasswordPage() {
        forgotPasswordButton().perform(click());
    }

    public void clickLoginButton() {
        loginButton().perform(click());
    }

    public void enterEmail(String email) {
        emailField().perform(replaceText(email));
    }

    public void enterPassword(String password) {
        passwordField().perform(replaceText(password));
    }

    public void loginAs(Parent parent) {
        enterEmail(parent.username);
        enterPassword(parent.password);
        clickLoginButton();
    }

    //endregion
}
