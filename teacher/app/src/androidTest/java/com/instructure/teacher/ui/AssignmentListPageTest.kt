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

import com.instructure.teacher.ui.models.CanvasUser
import com.instructure.teacher.ui.utils.*
import org.junit.Test
class AssignmentListPageTest : TeacherTest() {

    @Test
    @TestRail(ID = "C3109578")
    override fun displaysPageObjects() {
        getToAssignmentsPage()
        assignmentListPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3134487")
    fun displaysNoAssignmentsView() {
        getToAssignmentsPage()
        assignmentListPage.assertDisplaysNoAssignmentsView()
    }

    @Test
    @TestRail(ID = "C3109578")
    fun displaysAssignment() {
        getToAssignmentsPage()
        assignmentListPage.assertHasAssignment(getNextAssignment())
    }

    @Test
    @TestRail(ID = "C3134488")
    fun displaysGradingPeriods() {
        getToAssignmentsPage()
        assignmentListPage.assertHasGradingPeriods()
    }

    private fun getToAssignmentsPage(): CanvasUser {
        val teacher = logIn()
        coursesListPage.openCourse(getNextCourse())
        courseBrowserPage.openAssignmentsTab()
        return teacher
    }
}

