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

import com.instructure.teacher.R
import com.instructure.teacher.ui.models.CanvasUser
import com.instructure.teacher.ui.models.Course
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class QuizSubmissionListPageTest : TeacherTest() {

    var mCourse: Course? = null

    @Test
    override fun displaysPageObjects() {
        goToQuizSubmissionListPage()
        quizSubmissionListPage.assertPageObjects()
    }

    @Test
    fun displaysNoSubmissionsView() {
        goToQuizSubmissionListPage()
        quizSubmissionListPage.assertDisplaysNoSubmissionsView()
    }

    @Test
    fun filterLateSubmissions() {
        goToQuizSubmissionListPage()
        quizSubmissionListPage.clickFilterButton()
        quizSubmissionListPage.filterSubmittedLate()
        quizSubmissionListPage.assertDisplaysClearFilter()
        quizSubmissionListPage.assertFilterLabelText(R.string.submitted_late)
        quizSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun filterPendingReviewSubmissions() {
        goToQuizSubmissionListPage()
        quizSubmissionListPage.clickFilterButton()
        quizSubmissionListPage.filterPendingReview()
        quizSubmissionListPage.assertDisplaysClearFilter()
        quizSubmissionListPage.assertFilterLabelText(R.string.quizStatusPendingReview)
        quizSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun displaysQuizStatusComplete() {
        goToQuizSubmissionListPage()
        quizSubmissionListPage.assertSubmissionStatusSubmitted()
    }

    @Test
    fun displaysQuizStatusMissing() {
        goToQuizSubmissionListPage()
        quizSubmissionListPage.assertSubmissionStatusMissing()
    }

    @Test
    fun messageStudentsWho() {
        goToQuizSubmissionListPage()
        quizSubmissionListPage.clickAddMessage()
        addMessagePage.assertPageObjects()
        addMessagePage.assertHasStudentRecipient(getNextStudent(mCourse as Course))
    }

    private fun goToQuizSubmissionListPage(): CanvasUser {
        val teacher = logIn()
        mCourse = getNextCourse()
        coursesListPage.openCourse(mCourse as Course)
        courseBrowserPage.openQuizzesTab()
        quizListPage.clickQuiz(getNextQuiz())
        quizDetailsPage.openSubmissionsPage()
        return teacher
    }
}