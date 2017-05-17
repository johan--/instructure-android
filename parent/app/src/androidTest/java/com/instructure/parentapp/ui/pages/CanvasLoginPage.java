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
import android.support.test.espresso.web.webdriver.Locator;

import com.instructure.parentapp.R;
import com.instructure.parentapp.ui.utils.BasePage;
import com.instructure.parentapp.ui.models.Parent;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.sugar.Web.WebInteraction;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webKeys;

public class CanvasLoginPage extends BasePage {

    // to debug these selectors visit "canvas.instructure.com" and inspect the
    // webview elements using Chrome's device toolbar

    private static final String EMAIL_FIELD_CSS = "input[name=\"pseudonym_session[unique_id]\"]";
    private static final String PASSWORD_FIELD_CSS = "input[name=\"pseudonym_session[password]\"]";
    private static final String LOGIN_BUTTON_CSS = "button[type=\"submit\"]";
    private static final String FORGOT_PASSWORD_BUTTON_CSS = "a[class=\"forgot-password flip-to-back\"]";

    //region UI Element Locator Methods

    private ViewInteraction logInWithCanvasInstructions() {
        return onView(withId(R.id.instructions));
    }

    private WebInteraction emailField() {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, EMAIL_FIELD_CSS));
    }

    private WebInteraction passwordField() {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, PASSWORD_FIELD_CSS));
    }

    private WebInteraction loginButton() {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, LOGIN_BUTTON_CSS));
    }

    private WebInteraction forgotPasswordButton() {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, FORGOT_PASSWORD_BUTTON_CSS));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        logInWithCanvasInstructions().check(matches(isDisplayed()));
        emailField();
        passwordField();
        loginButton();
        forgotPasswordButton();
    }

    //endregion

    //region UI Action Helpers

    public void enterEmail(String email) {
        emailField().perform(webKeys(email));
    }

    public void enterPassword(String password) {
        passwordField().perform(webKeys(password));
    }

    public void clickLoginButton() {
        loginButton().perform(webClick());
    }

    public void clickForgotPasswordButton() {
        forgotPasswordButton().perform(webClick());
    }

    public void loginAs(Parent parent) {
        enterEmail(parent.username);
        enterPassword(parent.password);
        clickLoginButton();
    }

    //endregion
}
