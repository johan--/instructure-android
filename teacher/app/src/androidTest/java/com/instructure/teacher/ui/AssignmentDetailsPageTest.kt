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

class AssignmentDetailsPageTest : TeacherTest() {

    @Test
    @TestRail(ID = "C3109579")
    override fun displaysPageObjects() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3109579")
    fun displaysCorrectDetails() {
        val assignment = getToAssignmentDetailsPage()
        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    @Test
    @TestRail(ID = "C3109579")
    fun displaysInstructions() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertDisplaysInstructions()
    }

    @Test
    @TestRail(ID = "C3134480")
    fun displaysNoInstructionsMessage() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertDisplaysNoInstructionsView()
    }

    @Test
    @TestRail(ID = "C3134481")
    fun displaysClosedAvailability() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertAssignmentClosed()
    }

    @Test
    @TestRail(ID = "C3134482")
    fun displaysNoFromDate() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertToFilledAndFromEmpty()
    }

    @Test
    @TestRail(ID = "C3134483")
    fun displaysNoToDate() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertFromFilledAndToEmpty()
    }

    @Test
    fun displaysSubmissionTypeNone() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertSubmissionTypeNone()
    }

    @Test
    fun displaysSubmissionTypeOnPaper() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertSubmissionTypeOnPaper()
    }

    @Test
    fun displaysSubmissionTypeOnlineTextEntry() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertSubmissionTypeOnlineTextEntry()
    }

    @Test
    fun displaysSubmissionTypeOnlineUrl() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertSubmissionTypeOnlineUrl()
    }

    @Test
    fun displaysSubmissionTypeOnlineUpload() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertSubmissionTypeOnlineUpload()
    }

    @Test
    fun displaysSubmittedDonut() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertHasSubmitted()
    }

    @Test
    fun displaysNotSubmittedDonut() {
        getToAssignmentDetailsPage()
        assignmentDetailsPage.assertNotSubmitted()
    }

    private fun getToAssignmentDetailsPage(): Assignment {
        logIn()
        val assignment = getNextAssignment()
        coursesListPage.openCourse(getNextCourse())
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        return assignment
    }
}
