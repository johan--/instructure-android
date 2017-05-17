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

public class ThresholdsPage extends BasePage {

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

    private ViewInteraction removeStudentButton() {
        return onView(withText(R.string.removeStudent));
    }

    private ViewInteraction studentAvatar() {
        return scrollToView(onView(withId(R.id.avatar)));
    }

    private ViewInteraction studentNameText() {
        return scrollToView(onView(withId(R.id.studentName)));
    }

    private ViewInteraction alertMeWhenText() {
        // TODO: needs resource-id or contentDescription
        return scrollToView(onView(withId(R.id.notify_when_text)));
    }

    private ViewInteraction courseGradeBelowText() {
        return scrollToView(onView(withId(R.id.gradeBelowText)));
    }

    private ViewInteraction courseGradeBelowValue() {
        return scrollToView(onView(withId(R.id.gradeBelowValue)));
    }

    private ViewInteraction courseGradeAboveText() {
        return scrollToView(onView(withId(R.id.gradeAboveText)));
    }

    private ViewInteraction courseGradeAboveValue() {
        return scrollToView(onView(withId(R.id.gradeAboveValue)));
    }

    private ViewInteraction assignmentMissingText() {
        return scrollToView(onView(withId(R.id.assignmentMissingText)));
    }

    private ViewInteraction assignmentMissingSwitch() {
        return scrollToView(onView(withId(R.id.assignmentMissingSwitch)));
    }

    private ViewInteraction assignmentGradeBelowText() {
        return scrollToView(onView(withId(R.id.assignmentGradeBelowText)));
    }

    private ViewInteraction assignmentGradeBelowValue() {
        return scrollToView(onView(withId(R.id.assignmentGradeBelowValue)));
    }

    private ViewInteraction assignmentGradeAboveText() {
        return scrollToView(onView(withId(R.id.assignmentGradeAboveText)));
    }

    private ViewInteraction assignmentGradeAboveValue() {
        return scrollToView(onView(withId(R.id.assignmentGradeAboveValue)));
    }

    private ViewInteraction courseAnnouncementsText() {
        return scrollToView(onView(withId(R.id.teacherAnnouncementsText)));
    }

    private ViewInteraction courseAnnouncementsSwitch() {
        return scrollToView(onView(withId(R.id.teacherAnnouncementsSwitch)));
    }

    //endregion

    //region Assertion Helpers

    public void assertPageObjects() {
        closeButton().check(matches(isDisplayed()));
        settingsTitle().check(matches(isDisplayed()));
        moreOptionsButton().check(matches(isDisplayed()));
        studentAvatar().check(matches(isDisplayed()));
        studentNameText().check(matches(isDisplayed()));
        alertMeWhenText().check(matches(isDisplayed()));
        courseGradeBelowText().check(matches(isDisplayed()));
        courseGradeBelowValue().check(matches(isDisplayed()));
        courseGradeAboveText().check(matches(isDisplayed()));
        courseGradeAboveValue().check(matches(isDisplayed()));
        assignmentMissingText().check(matches(isDisplayed()));
        assignmentMissingSwitch().check(matches(isDisplayed()));
        assignmentGradeBelowText().check(matches(isDisplayed()));
        assignmentGradeBelowValue().check(matches(isDisplayed()));
        assignmentGradeAboveText().check(matches(isDisplayed()));
        assignmentGradeAboveValue().check(matches(isDisplayed()));
        courseAnnouncementsText().check(matches(isDisplayed()));
        courseAnnouncementsSwitch().check(matches(isDisplayed()));
    }

    //endregion

    //region UI Action Helpers

    public void clickCloseButton() {
        closeButton().perform(click());
    }

    public void clickCourseBelowValue() {
        courseGradeBelowValue().perform(click());
    }

    public void clickCourseAboveValue() {
        courseGradeAboveValue().perform(click());
    }

    public void clickAssignmentBelowValue() {
        assignmentGradeBelowValue().perform(click());
    }

    public void clickAssignmentAboveValue() {
        assignmentGradeAboveValue().perform(click());
    }

    public void clickMoreOptionsButton () {
        moreOptionsButton().perform(click());
    }

    public void clickRemoveStudentButton() {
        removeStudentButton().perform(click());
    }

    public void removeStudent() {
        clickMoreOptionsButton();
        clickRemoveStudentButton();
    }

    //endregion
}
