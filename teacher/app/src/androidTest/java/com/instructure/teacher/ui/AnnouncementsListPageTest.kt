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
package com.instructure.teacher.ui

import com.instructure.teacher.ui.data.Data
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.getNextCourse
import com.instructure.teacher.ui.utils.logIn
import com.instructure.teacher.ui.models.CanvasUser
import org.junit.Test

class AnnouncementsListPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToAnnouncementsListPage()
        announcementsListPage.assertPageObjects()
    }

    @Test
    fun assertHasAnnouncement() {
        getToAnnouncementsListPage()
        val discussion = Data.getNextDiscussion()
        announcementsListPage.assertHasAnnouncement(discussion)
    }

    @Test
    fun assertDisplaysFloatingActionButton() {
        getToAnnouncementsListPage()
        val discussion = Data.getNextDiscussion()
        announcementsListPage.assertHasAnnouncement(discussion)
    }

    private fun getToAnnouncementsListPage(): CanvasUser {
        val teacher = logIn()
        val course = getNextCourse()
        coursesListPage.openCourse(course)
        courseBrowserPage.openAnnouncementsTab()
        return teacher
    }
}