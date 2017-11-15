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
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class SpeedGraderPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderPage()
        speedGraderPage.assertPageObjects()
    }

    @Test
    fun displaysSubmissionDropDown() {
        goToSpeedGraderPage()
        speedGraderPage.assertHasSubmissionDropDown()
    }

    @Test
    fun displaySubmissionPickerDialog() {
        goToSpeedGraderPage()
        speedGraderPage.openSubmissionsDialog()
        speedGraderPage.assertSubmissionDialogDisplayed()
    }

    @Test
    fun opensToCorrectSubmission() {
        logIn()
        val course = getNextCourse()
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(getNextAssignment())
        assignmentDetailsPage.openSubmissionsPage()
        List(4) { getNextStudent(course) }.forEach { student ->
            assignmentSubmissionListPage.clickSubmission(student)
            speedGraderPage.assertGradingStudent(student)
            speedGraderPage.clickBackButton()
        }
    }

    @Test
    fun hasCorrectPageCount() {
        goToSpeedGraderPage()
        speedGraderPage.assertPageCount(4)
    }

    /* TODO: Uncomment and implement if we come up with a way to create/modify submissions dates
    @Test
    fun displaysSelectedSubmissionInDropDown() {
        goToSpeedGraderPage()
        speedGraderPage.openSubmissionsDialog()
        getNextSubmission()
        val submission = getNextSubmission()
        speedGraderPage.selectSubmissionFromDialog(submission)
        speedGraderPage.assertSubmissionSelected(submission)
    }
    */

    @Test
    fun displaysTextSubmission() {
        goToSpeedGraderPage()
        speedGraderPage.assertDisplaysTextSubmissionView()
    }

    @Test
    fun displaysUnsubmittedEmptyState() {
        goToSpeedGraderPage()
        speedGraderPage.assertDisplaysEmptyState(R.string.speedgrader_student_no_submissions)
    }

    @Test
    fun displaysNoSubmissionsAllowedEmptyState() {
        goToSpeedGraderPage()
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderNoneMessage)
    }

    @Test
    fun displaysOnPaperEmptyState() {
        goToSpeedGraderPage()
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderOnPaperMessage)
    }

    @Test
    fun displaysExternalToolEmptyState() {
        goToSpeedGraderPage()
        speedGraderPage.assertDisplaysEmptyState(R.string.speedgrader_student_no_submissions)
    }

    @Test
    fun displaysUrlSubmission() {
        goToSpeedGraderPage()
        val submission = getNextSubmission()
        speedGraderPage.assertDisplaysUrlSubmissionLink(submission)
        speedGraderPage.assertDisplaysUrlWebView()
    }

    private fun goToSpeedGraderPage(): CanvasUser {
        val teacher = logIn()
        val course = getNextCourse()
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(getNextAssignment())
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(getNextStudent(course))
        return teacher
    }
}
