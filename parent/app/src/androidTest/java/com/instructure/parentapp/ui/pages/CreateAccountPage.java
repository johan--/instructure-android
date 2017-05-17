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
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;

import com.instructure.espresso.ActivityHelper;
import com.instructure.parentapp.R;
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class CreateAccountPage {

    //region UI Element Locator Methods

    private ViewInteraction appLogo() {
        return onView(withId(R.id.logo));
    }

    private ViewInteraction emailField() {
        return onView(withId(R.id.email));
    }

    private ViewInteraction firstNameField() {
        return onView(withId(R.id.first_name));
    }

    private ViewInteraction lastNameField() {
        return onView(withId(R.id.last_name));
    }

    private ViewInteraction passwordField() {
        return onView(withId(R.id.createAccountPassword));
    }

    private ViewInteraction confirmPasswordField() {
        return onView(withId(R.id.createAccountPasswordConfirm));
    }

    private ViewInteraction nextButton() {
        return onView(withId(R.id.next));
    }

    private ViewInteraction emailAlreadyExistsNotification() {
        Activity activity = ActivityHelper.currentActivity();
        return onView(withText(R.string.email_already_exists))
                .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        appLogo().check(matches(isDisplayed()));
        emailField().check(matches(isDisplayed()));
        firstNameField().check(matches(isDisplayed()));
        lastNameField().check(matches(isDisplayed()));
        passwordField().check(matches(isDisplayed()));
        confirmPasswordField().check(matches(isDisplayed()));
        nextButton().check(matches(isDisplayed()));
    }

    public void assertEmailAlreadyExistsNotification() {
        emailAlreadyExistsNotification().check(matches(isDisplayed()));
    }

    public void assertNextButtonEnabled() {
        nextButton().check(matches(isEnabled()));
    }

    public void assertNextButtonNotEnabled() {
        nextButton().check(matches(not(isEnabled())));
    }

    //endregion

    //region UI Action Helpers

    public void clickBackButton() {
        Espresso.pressBack();
    }

    public void clickNextButton() {
        nextButton().perform(click());
    }

    public void enterEmail(String email) {
        emailField().perform(replaceText(email));
    }

    public void enterFirstName(String firstName) {
        firstNameField().perform(replaceText(firstName));
    }

    public void enterLastName(String lastName) {
        lastNameField().perform(replaceText(lastName));
    }

    public void enterPassword(String password) {
        passwordField().perform(replaceText(password));
    }

    public void enterConfirmPassword(String password) {
        confirmPasswordField().perform(replaceText(password));
    }

    public void enterFormFields(Parent parent) {
        enterEmail(parent.username);
        enterFirstName(parent.firstName);
        enterLastName(parent.lastName);
        enterPassword(parent.password);
        enterConfirmPassword(parent.password);
    }

    public void createAccount(Parent parent) {
        enterFormFields(parent);
        clickNextButton();
    }

    //endregion
}
