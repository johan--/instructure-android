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
 *
 */
package com.instructure.teacher.ui.pages

import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.OnViewWithId
import com.instructure.teacher.ui.utils.WaitForViewWithId
import com.instructure.teacher.ui.utils.click
import com.instructure.teacher.ui.utils.pageAssert.PageAssert
import com.instructure.teacher.ui.utils.pageAssert.SimplePageAssert
import com.instructure.teacher.ui.utils.waitForViewWithText

class CourseBrowserPage : BasePage(), PageAssert by SimplePageAssert() {

    private val courseBrowserRecyclerView by WaitForViewWithId(R.id.courseBrowserRecyclerView)
    private val courseImage by OnViewWithId(R.id.courseImage)
    private val courseTitle by OnViewWithId(R.id.courseBrowserTitle)
    private val courseSubtitle by OnViewWithId(R.id.courseBrowserSubtitle)
    private val courseSettingsMenuButton by OnViewWithId(R.id.menu_course_browser_settings)

    fun openAssignmentsTab() {
        waitForViewWithText("Assignments").click()
    }

    fun openQuizzesTab() {
        waitForViewWithText("Quizzes").click()
    }

    fun openDiscussionsTab() {
        waitForViewWithText("Discussions").click()
    }

    fun openAnnouncementsTab() {
        waitForViewWithText("Announcements").click()
    }

    fun clickSettingsButton() {
        courseSettingsMenuButton.click()
    }
}