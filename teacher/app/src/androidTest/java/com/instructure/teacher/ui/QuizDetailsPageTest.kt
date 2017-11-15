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

import com.instructure.teacher.ui.models.Quiz
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class QuizDetailsPageTest: TeacherTest() {

    @Test
    @TestRail(ID = "C3109579")
    override fun displaysPageObjects() {
        getToQuizDetailsPage()
        quizDetailsPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3109579")
    fun displaysCorrectDetails() {
        val assignment = getToQuizDetailsPage()
        quizDetailsPage.assertQuizDetails(assignment)
    }

    @Test
    @TestRail(ID = "C3109579")
    fun displaysInstructions() {
        getToQuizDetailsPage()
        quizDetailsPage.assertDisplaysInstructions()
    }

    @Test
    @TestRail(ID = "C3134480")
    fun displaysNoInstructionsMessage() {
        getToQuizDetailsPage()
        quizDetailsPage.assertDisplaysNoInstructionsView()
    }

    @Test
    @TestRail(ID = "C3134481")
    fun displaysClosedAvailability() {
        getToQuizDetailsPage()
        quizDetailsPage.assertQuizClosed()
    }

    @Test
    @TestRail(ID = "C3134482")
    fun displaysNoFromDate() {
        getToQuizDetailsPage()
        quizDetailsPage.assertToFilledAndFromEmpty()
    }

    @Test
    @TestRail(ID = "C3134483")
    fun displaysNoToDate() {
        getToQuizDetailsPage()
        quizDetailsPage.assertFromFilledAndToEmpty()
    }

//    @Test
//    fun displaysSubmittedDonut() {
//        getToQuizDetailsPage()
//        quizDetailsPage.assertHasSubmitted()
//    }

//    @Test
//    fun displaysNotSubmittedDonut() {
//        getToQuizDetailsPage()
//        quizDetailsPage.assertNotSubmitted()
//    }

    private fun getToQuizDetailsPage(): Quiz {
        logIn()
        val quiz = getNextQuiz()
        coursesListPage.openCourse(getNextCourse())
        courseBrowserPage.openQuizzesTab()
        quizListPage.clickQuiz(quiz)
        return quiz
    }
}