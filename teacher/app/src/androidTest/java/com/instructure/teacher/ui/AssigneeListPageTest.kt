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

import com.instructure.teacher.R
import com.instructure.teacher.ui.data.Data.getAllStudents
import com.instructure.teacher.ui.models.CanvasUser
import com.instructure.teacher.ui.models.Course
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class AssigneeListPageTest : TeacherTest() {

    @Test override fun displaysPageObjects() {
        getToAssigneeListPage()
        assigneeListPage.assertPageObjects()
    }

    @Test fun displaysEveryoneItem() {
        getToAssigneeListPage()
        assigneeListPage.assertDisplaysAssigneeOptions(sectionNames = listOf("Everyone"))
    }

    @Test fun displaysStudentItems() {
        val (_, course) = getToAssigneeListPage()
        val students = getAllStudents(course)
        assigneeListPage.assertDisplaysAssigneeOptions(
                sectionNames = listOf("Everyone"),
                studentNames = students.map { it.name }
        )
    }

    @Test fun selectsStudents() {
        val (_, course) = getToAssigneeListPage()
        val studentNames = getAllStudents(course).map { it.name }
        assigneeListPage.assertDisplaysAssigneeOptions(
                sectionNames = listOf("Everyone"),
                studentNames = studentNames
        )
        assigneeListPage.assertAssigneesSelected(listOf("Everyone"))
        assigneeListPage.toggleAssignees(studentNames)
        val expectedAssignees = studentNames + "Everyone else"
        assigneeListPage.assertAssigneesSelected(expectedAssignees)
        assigneeListPage.saveAndClose()
        val assignText = editAssignmentDetailsPage.onViewWithId(R.id.assignTo)
        for (assignee in expectedAssignees) assignText.assertContainsText(assignee)
    }

    private fun getToAssigneeListPage(): Pair<CanvasUser, Course> {
        val teacher = logIn()
        val assignment = getNextAssignment()
        val course = getNextCourse()
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.editAssignees()
        return Pair(teacher, course)
    }

}