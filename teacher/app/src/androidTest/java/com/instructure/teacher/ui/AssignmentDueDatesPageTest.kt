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

import com.instructure.teacher.ui.models.Assignment
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class AssignmentDueDatesPageTest : TeacherTest() {

    @Test
    @TestRail(ID = "C3134131")
    override fun displaysPageObjects() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3134484")
    fun displaysNoDueDate() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertDisplaysNoDueDate()
    }

    @Test
    @TestRail(ID = "C3134485")
    fun displaysSingleDueDate() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertDisplaysSingleDueDate()
    }

    @Test
    @TestRail(ID = "C3134486")
    fun displaysAvailabilityDates() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertDisplaysAvailabilityDates()
    }

    private fun getToDueDatesPage(): Assignment {
        logIn()
        val assignment = getNextAssignment()
        coursesListPage.openCourse(getNextCourse())
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openAllDatesPage()
        return assignment
    }
}
