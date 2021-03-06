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
package com.instructure.teacher.ui

import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.TestRail
import com.instructure.teacher.ui.utils.logIn
import org.junit.Test

class CourseSettingsPageTest : TeacherTest() {

    @Test
    @TestRail(ID = "C3108914")
    override fun displaysPageObjects() {
        logIn()
        navigateToCourseSettings()
        courseSettingsPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3108915")
    fun editCourseName() {
        logIn()
        navigateToCourseSettings()
        courseSettingsPage.clickCourseName()
        val newCourseName: String = courseSettingsPage.editCourseName()
        courseSettingsPage.assertCourseNameChanged(newCourseName)
    }

    @Test
    @TestRail(ID = "C3108916")
    fun editCourseHomePage() {
        logIn()
        navigateToCourseSettings()
        courseSettingsPage.clickSetHomePage()
        val newCourseHomePage: String = courseSettingsPage.selectNewHomePage()
        courseSettingsPage.assertHomePageChanged(newCourseHomePage)
    }

    private fun navigateToCourseSettings() {
        coursesListPage.openCourseAtPosition(0)
        courseBrowserPage.clickSettingsButton()
    }
}