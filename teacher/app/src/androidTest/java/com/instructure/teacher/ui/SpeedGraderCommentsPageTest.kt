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

class SpeedGraderCommentsPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.assertPageObjects()
    }

    @Test
    fun displaysAuthorName() {
        goToSpeedGraderCommentsPage()
        val authorName = getNextSubmission().submissionComments[0].authorName
        speedGraderCommentsPage.assertDisplaysAuthorName(authorName)
    }

    @Test
    fun displaysCommentText() {
        goToSpeedGraderCommentsPage()
        val commentText = getNextSubmission().submissionComments[0].comment
        speedGraderCommentsPage.assertDisplaysCommentText(commentText)
    }

    @Test
    fun displaysCommentAttachment() {
        goToSpeedGraderCommentsPage()
        val attachment = getNextSubmission().submissionComments[0].attachments[0]
        speedGraderCommentsPage.assertDisplaysCommentAttachment(attachment)
    }

    @Test
    fun displaysSubmissionHistory() {
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.assertDisplaysSubmission()
    }

    @Test
    fun displaysSubmissionFile() {
        goToSpeedGraderCommentsPage()
        val file = getNextSubmission().attachments[0]
        speedGraderCommentsPage.assertDisplaysSubmissionFile(file)
    }

    @Test
    fun addsNewTextComment() {
        goToSpeedGraderCommentsPage()
        val newComment = randomString(32)
        speedGraderCommentsPage.addComment(newComment)
        speedGraderCommentsPage.assertDisplaysCommentText(newComment)
    }

    @Test
    fun showsNoCommentsMessage() {
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.assertDisplaysEmptyState()
    }

    private fun goToSpeedGraderCommentsPage(): CanvasUser {
        val teacher = logIn()
        val course = getNextCourse()
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(getNextAssignment())
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(getNextStudent(course))
        speedGraderPage.selectCommentsTab()
        return teacher
    }
}