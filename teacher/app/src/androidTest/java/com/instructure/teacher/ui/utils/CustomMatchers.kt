/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 */
package com.instructure.teacher.ui.utils

import android.support.design.widget.TextInputLayout
import android.support.test.espresso.ViewAssertion
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.util.HumanReadables
import android.support.test.espresso.util.TreeIterables.breadthFirstViewTraversal
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.RadioButton
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


fun checked(checked: Boolean = true, index: Int = 0, getText: (String) -> Unit = {}): BoundedMatcher<View, RadioButton> {
    return object : BoundedMatcher<View, RadioButton>(RadioButton::class.java) {
        var currentIndex = 0
        override fun describeTo(description: Description) {
            description.appendText("selected radio button ")
        }

        override fun matchesSafely(item: RadioButton): Boolean {
            if (index == currentIndex++ && item.isChecked == checked) {
                getText(item.text.toString())
                return true
            }
            currentIndex--
            return false
        }
    }
}


/**
 * @param matchTitle True if matching with title, false if matching with subtitle
 */
fun matchToolbarText(matchText: Matcher<String>, matchTitle: Boolean = true): BoundedMatcher<View, Toolbar> {
    return object : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with toolbar ${if (matchTitle) "title" else "subtitle"} ")
            matchText.describeTo(description)
        }

        override fun matchesSafely(view: Toolbar): Boolean {
            return matchText.matches(if (matchTitle) view.title else view.subtitle)
        }
    }
}

fun has(expectedCount: Int, selector: Matcher<View>): ViewAssertion {
    return ViewAssertion { view, noViewFoundException ->
        val rootView = view

        val descendantViews = breadthFirstViewTraversal(rootView)
        val selectedViews = ArrayList<View>()
        descendantViews.forEach {
            if (selector.matches(it)) {
                selectedViews.add(it)
            }
        }

        if (selectedViews.size != expectedCount) {
            val errorMessage = HumanReadables.getViewHierarchyErrorMessage(rootView,
                    selectedViews,
                    String.format("Found %d views instead of %d matching: %s", selectedViews.size, expectedCount, selector),
                    "****MATCHES****")
            throw AssertionFailedError(errorMessage);
        }
    }
}

fun hasTextInputLayoutErrorText(stringResId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {

        override fun matchesSafely(view: View): Boolean {
            if (view !is TextInputLayout) {
                return false
            }

            val error = view.error ?: return false

            val hint = error.toString()

            val actualErrorMsg = view.resources.getString(stringResId)

            return actualErrorMsg == hint
        }

        override fun describeTo(description: Description) {}
    }
}

fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        internal var currentIndex = 0

        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}