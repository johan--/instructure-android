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

package com.instructure.parentapp.ui;

import com.instructure.parentapp.ui.utils.ParentTest;

import org.junit.Before;
import org.junit.Test;

public class CanvasDomainPickerTest extends ParentTest {

    private static final String TEST_DOMAIN = "canvas.instructure.com";

    @Before
    public void setup() {
        parentloginPage.clickLogInWithCanvasButton();
    }

    @Test
    public void displaysPageObjects() {
        canvasDomainPickerPage.assertPageObjects();
    }

    @Test
    public void routesToCanvasLoginPage() {
        canvasDomainPickerPage.enterDomain(TEST_DOMAIN);
        canvasLoginPage.assertPageObjects();
    }

    @Test
    public void routesToParentLoginPage() {
        canvasDomainPickerPage.clickBackButton();
        parentloginPage.assertPageObjects();
    }

    @Test
    public void nextButtonNotEnabled_emptyDomainField() {
        canvasDomainPickerPage.assertNextButtonNotEnabled();
    }

    @Test
    public void nextButtonEnabled() {
        canvasDomainPickerPage.enterDomainFieldText(TEST_DOMAIN);
        canvasDomainPickerPage.assertNextButtonEnabled();
    }

    @Test
    public void searchResultsNotDisplayed_emptyDomainField() {
        canvasDomainPickerPage.assertSearchResultsNotDispalyed();
    }

    @Test
    public void searchResultsDisplayed() {
        canvasDomainPickerPage.enterDomainFieldText(TEST_DOMAIN);
        canvasDomainPickerPage.assertSearchResultsDisplayed();
    }
}
