/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.holders

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.utils.getAssignmentIcon
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.getGradeText
import com.instructure.teacher.utils.getResForSubmission
import kotlinx.android.synthetic.main.adapter_student_context_submission.view.*


@SuppressLint("ViewConstructor")
class StudentContextSubmissionView(context: Context, submission: Submission, courseColor: Int) : FrameLayout(context) {

    val assignment = requireNotNull(submission.assignment)

    init {
        View.inflate(context, R.layout.adapter_student_context_submission, this)

        // Title, icon, and publish status
        assignmentTitle.text = assignment.name
        assignmentIcon.setIcon(assignment.getAssignmentIcon(), courseColor)
        assignmentIcon.setPublishedStatus(assignment.isPublished)
        publishedBar.visibility = if (assignment.isPublished) View.VISIBLE else View.INVISIBLE

        // Submission status
        val (stringRes, colorRes) = assignment.getResForSubmission(submission)
        if (stringRes == -1 || colorRes == -1) {
            submissionStatus.setGone()
        } else {
            submissionStatus.setText(stringRes)
            submissionStatus.setTextColor(context.getColorCompat(colorRes))
        }

        // Submission grade
        if (submission.isGraded || submission.isExcused) {
            val grade = assignment.getGradeText(submission, context, false)
            submissionGradeView.text = grade.takeUnless { it == "null" } ?: ""
            scoreBar.progress = (submission.score / assignment.pointsPossible).toFloat()
        } else {
            submissionGradeContainer.setGone()
            if (submission.workflowState != "unsubmitted") {
                val submissionGradeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_generic_pill)
                val strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics)
                (submissionGradeDrawable as GradientDrawable).setStroke(strokeWidth.toInt(), ThemePrefs.brandColor)
                needsGradingPill.background = submissionGradeDrawable
                needsGradingPill.setTextColor(ThemePrefs.brandColor)
                needsGradingPill.setVisible()
            }
        }

    }

}
