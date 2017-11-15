/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.utils

import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.models.Submission
import com.instructure.teacher.R
import java.util.*

/**
 * Determines if this quiz has an associated assignment.
 * Used to determine if we need to save any overrides for the quiz.
 */
fun Quiz.isGradeable() : Boolean {
    return (this.quizType == Quiz.TYPE_ASSIGNMENT || this.quizType == Quiz.TYPE_GRADED_SURVEY)
}

/**
 *
 * @return Pair(stringRes: Int, colorRes: Int)
 */
fun Quiz.getResForSubmission(quizSubmission: QuizSubmission?): Pair<Int, Int> {
    if(quizSubmission == null || quizSubmission.workflowState == QuizSubmission.WORKFLOW_STATE.SETTINGS_ONLY) {
        if(this.dueAt != null && (this.dueAt as Date).before(Date())) {
            return Pair(R.string.submission_status_missing, R.color.submission_status_color_missing)
        } else {
            return Pair(R.string.quizStatusNotStarted, R.color.defaultTextGray)
        }
    }
    when(quizSubmission.workflowState) {
        QuizSubmission.WORKFLOW_STATE.UNTAKEN -> {
            return Pair(R.string.quizStatusInProgress, R.color.defaultTextGray)
        }
        QuizSubmission.WORKFLOW_STATE.COMPLETE -> {
            if(isSubmissionLate(quizSubmission)) {
                return Pair(R.string.submission_status_late, R.color.submission_status_color_late)
            } else {
                return Pair(R.string.quizStatusComplete, R.color.submission_status_color_submitted)
            }
        }
        QuizSubmission.WORKFLOW_STATE.PENDING_REVIEW -> {
            return Pair(R.string.quizStatusPendingReview, R.color.defaultTextGray)
        }
        else -> {
            if(quizSubmission.isOverDueAndNeedsSubmission) {
                return Pair(R.string.submission_status_late, R.color.submission_status_color_late)
            }
            return Pair(-1, -1)
        }

    }
}

fun Quiz.isSubmissionLate(quizSubmission: QuizSubmission?) : Boolean {
    if(this.assignment == null && this.dueAt?.before(quizSubmission?.finishedAt) ?: false) {
        return true
    } else if(this.assignment != null && this.assignment.allDates.size > 0) {
        //logic to check for multiple due date issues since there isn't an isLate flag on a quiz submission
        if(this.assignment.allDates.size == 1) {
            if(this.assignment.allDates[0].dueAt?.before(quizSubmission?.finishedAt) ?: false) {
                return true
            }
        } else {
            for (group in this.assignment.groupedDueDates) {
                if (group.studentIds.contains(quizSubmission?.userId)) {
                    if (group.coreDates.dueDate?.before(quizSubmission?.finishedAt) ?: false) {
                        return true
                    }
                }
            }
        }
    }
    return false
}

fun Quiz.quizTypeDisplayable(): Int = when (this.quizType) {
    Quiz.TYPE_PRACTICE -> R.string.practice_quiz
    Quiz.TYPE_ASSIGNMENT -> R.string.graded_quiz
    Quiz.TYPE_GRADED_SURVEY -> R.string.graded_survey
    Quiz.TYPE_SURVEY -> R.string.ungraded_survey
    // Else shouldn't happen; just here to satisfy the expression
    else -> 0
}

fun Quiz.shuffleAnswersDisplayable(): Int = if (this.shuffleAnswers) R.string.yes else R.string.no
/**
 * Anonymous Submissions only shows if the quiz type is
 * one of the survey types.
 */
fun Quiz.anonymousSubmissionsDisplayable(): Boolean {
    return this.quizType == Quiz.TYPE_SURVEY || this.quizType == Quiz.TYPE_GRADED_SURVEY
}

fun Quiz.isPracticeOrUngraded(): Boolean {
    return this.quizType == Quiz.TYPE_SURVEY || this.quizType == Quiz.TYPE_PRACTICE
}

fun Quiz.isUngradedSurvey(): Boolean = this.quizType == Quiz.TYPE_SURVEY
fun Submission.transformForQuizGrading() {
    submissionHistory.filterNotNull().forEach {
        it.id = id
        it.previewUrl = it.previewUrl?.replace("version=\\d+".toRegex(), "version=${it.attempt}")
    }
}
