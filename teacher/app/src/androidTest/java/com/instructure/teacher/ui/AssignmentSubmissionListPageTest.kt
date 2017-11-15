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
import com.instructure.teacher.ui.models.CanvasUser
import com.instructure.teacher.ui.models.Course
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class AssignmentSubmissionListPageTest : TeacherTest() {

    private var mCourse: Course? = null

    @Test
    override fun displaysPageObjects() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertPageObjects()
    }

    @Test
    fun displaysNoSubmissionsView() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertDisplaysNoSubmissionsView()
    }

    @Test
    fun filterLateSubmissions() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.filterSubmittedLate()
        assignmentSubmissionListPage.assertDisplaysClearFilter()
        assignmentSubmissionListPage.assertFilterLabelText(R.string.submitted_late)
        assignmentSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun filterUngradedSubmissions() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.filterUngraded()
        assignmentSubmissionListPage.assertDisplaysClearFilter()
        assignmentSubmissionListPage.assertFilterLabelText(R.string.havent_been_graded)
        assignmentSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun displaysAssignmentStatusSubmitted() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertSubmissionStatusSubmitted()
    }

    @Test
    fun displaysAssignmentStatusMissing() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertSubmissionStatusMissing()
    }

    @Test
    fun displaysAssignmentStatusNotSubmitted() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertSubmissionStatusNotSubmitted()
    }

    @Test
    fun displaysAssignmentStatusLate() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertSubmissionStatusLate()
    }

    @Test
    fun messageStudentsWho() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.clickAddMessage()
        addMessagePage.assertPageObjects()
        addMessagePage.assertHasStudentRecipient(getNextStudent(mCourse as Course))
    }

    @Test
    fun togglesMute() {
        goToAssignmentSubmissionListPage()
        openOverflowMenu()
        assignmentSubmissionListPage.assertDisplaysMuteOption()
        assignmentSubmissionListPage.clickMuteOption()
        assignmentSubmissionListPage.assertDisplaysMutedStatus()
        openOverflowMenu()
        assignmentSubmissionListPage.assertDisplaysUnmuteOption()
    }

    @Test
    fun toggleAnonymousGrading() {
        goToAssignmentSubmissionListPage()
        openOverflowMenu()
        assignmentSubmissionListPage.assertDisplaysEnableAnonymousOption()
        assignmentSubmissionListPage.clickAnonymousOption()
        assignmentSubmissionListPage.assertDisplaysAnonymousGradingStatus()
        assignmentSubmissionListPage.assertDisplaysAnonymousName()
        openOverflowMenu()
        assignmentSubmissionListPage.assertDisplaysDisableAnonymousOption()
    }

    private fun goToAssignmentSubmissionListPage(): CanvasUser {
        val teacher = logIn()
        mCourse = getNextCourse()
        coursesListPage.openCourse(mCourse as Course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(getNextAssignment())
        assignmentDetailsPage.openSubmissionsPage()
        return teacher
    }
}