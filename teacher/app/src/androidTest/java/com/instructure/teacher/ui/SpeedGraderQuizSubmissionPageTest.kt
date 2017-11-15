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

class SpeedGraderQuizSubmissionPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToQuizSubmissionPage()
        speedGraderQuizSubmissionPage.assertPageObjects()
    }

    @Test
    fun displaysNoSubmission() {
        getToQuizSubmissionPage()
        speedGraderQuizSubmissionPage.assertShowsNoSubmissionState()
    }

    @Test
    fun displaysPendingReviewState() {
        getToQuizSubmissionPage()
        speedGraderQuizSubmissionPage.assertShowsPendingReviewState()
    }

    @Test
    fun displaysViewQuizState() {
        getToQuizSubmissionPage()
        speedGraderQuizSubmissionPage.assertShowsViewQuizState()
    }

    private fun getToQuizSubmissionPage(): CanvasUser {
        val teacher = logIn()
        val course = getNextCourse()
        coursesListPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()
        quizListPage.clickQuiz(getNextQuiz())
        quizDetailsPage.openSubmissionsPage()
        quizSubmissionListPage.clickSubmission(getNextStudent(course))
        return teacher
    }

}
