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
package com.instructure.teacher.ui.pages

import com.instructure.teacher.R
import com.instructure.teacher.ui.models.CanvasUser
import com.instructure.teacher.ui.utils.*
import com.instructure.teacher.ui.utils.pageAssert.PageAssert
import com.instructure.teacher.ui.utils.pageAssert.SimplePageAssert

class QuizSubmissionListPage : BasePage(), PageAssert by SimplePageAssert() {

    private val assignmentSubmissionListToolbar by OnViewWithId(R.id.assignmentSubmissionListToolbar)

    private val assignmentSubmissionRecyclerView by OnViewWithId(R.id.submissionsRecyclerView)

    private val assignmentSubmissionListFilterLabel by OnViewWithId(R.id.filterTitle)

    private val assignmentSubmissionClearFilter by OnViewWithId(R.id.clearFilterTextView, false)

    private val assignmentSubmissionFilterButton by OnViewWithId(R.id.submissionFilter)

    private val filterAllSubmissions by OnViewWithId(R.id.allSubmissions, false)

    private val filterSubmittedLate by OnViewWithId(R.id.submittedLate, false)

    private val assignmentSubmissionStatus by OnViewWithId(R.id.submissionStatus)

    private val addMessageFAB by OnViewWithId(R.id.addMessage)

    //Only displayed when assignment list is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)

    fun assertDisplaysNoSubmissionsView() {
        emptyPandaView.assertDisplayed()
    }

    fun assertHasStudentSubmission(canvasUser: CanvasUser) {
        waitForViewWithText(canvasUser.name).assertDisplayed()
    }

    fun assertFilterLabelAllSubmissions() {
        assignmentSubmissionListFilterLabel.assertHasText(R.string.all_submissions)
    }

    fun assertDisplaysClearFilter() {
        assignmentSubmissionClearFilter.assertDisplayed()
    }

    fun assertClearFilterGone() {
        assignmentSubmissionClearFilter.assertGone()
    }

    fun clickFilterButton() {
        assignmentSubmissionFilterButton.click()
    }

    fun clickSubmission(student: CanvasUser) {
        waitForViewWithText(student.name).click()
    }

    fun filterSubmittedLate() {
        onViewWithText(R.string.submitted_late).click()
    }

    fun filterPendingReview() {
        onViewWithText(R.string.quizStatusPendingReview).click()
    }

    fun assertFilterLabelText(text: Int) {
        assignmentSubmissionListFilterLabel.assertHasText(text)
    }

    fun assertHasSubmission() {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(1))
    }

    fun assertHasNoSubmission() {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(0))
    }

    fun assertSubmissionStatusMissing() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_missing)
    }

    fun assertSubmissionStatusSubmitted() {
        assignmentSubmissionStatus.assertHasText(R.string.quizStatusComplete)
    }

    fun clickAddMessage() {
        addMessageFAB.click()
    }
}