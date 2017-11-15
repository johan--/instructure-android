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
package com.instructure.teacher.ui.pages

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers.*
import com.instructure.teacher.R
import com.instructure.teacher.R.id.assigneeListPage
import com.instructure.teacher.ui.utils.*
import com.instructure.teacher.ui.utils.pageAssert.PageAssert
import com.instructure.teacher.ui.utils.pageAssert.PageWithIdAssert
import org.hamcrest.Matchers.allOf

@Suppress("unused")
class AssigneeListPage : BasePage(), PageAssert by PageWithIdAssert(R.id.assigneeListPage) {

    private val titleTextView by OnViewWithText(R.string.page_title_add_assignees)
    private val closeButton by OnViewWithContentDescription(R.string.close)
    private val saveButton by OnViewWithId(R.id.menu_save)
    private val recyclerView by WaitForViewWithId(R.id.recyclerView)

    fun assertDisplaysAssigneeOptions(
            sectionNames: List<String> = emptyList(),
            groupNames: List<String> = emptyList(),
            studentNames: List<String> = emptyList()) {
        for (assigneeName in (sectionNames + groupNames + studentNames)) {
            onView(allOf(withText(assigneeName), hasSibling(withId(R.id.assigneeSubtitleView)))).assertDisplayed()
        }
    }

    fun assertAssigneesSelected(assigneeNames: List<String>) {
        val selectedTextView = onViewWithId(R.id.selectedAssigneesTextView)
        for (name in assigneeNames) selectedTextView.assertContainsText(name)
    }

    fun toggleAssignees(assigneeNames: List<String>) {
        for (name in assigneeNames) {
            onView(allOf(withText(name), hasSibling(withId(R.id.assigneeSubtitleView)))).click()
        }
    }

    fun saveAndClose() {
        saveButton.click()
    }

}