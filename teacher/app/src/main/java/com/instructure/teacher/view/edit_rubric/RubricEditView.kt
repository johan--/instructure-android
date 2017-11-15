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
@file:Suppress("unused")

package com.instructure.teacher.view.edit_rubric

import android.content.Context
import android.content.res.ColorStateList
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.SubmissionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import kotlinx.android.synthetic.main.view_edit_rubric.view.*
import kotlinx.coroutines.experimental.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class RubricEditView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * A reference to the original assessment, if any. Will be compared to the working
     * assessment [mAssessment] when checking for unsaved changes.
     */
    private var mOriginalAssessment: MutableMap<String, RubricCriterionAssessment> = hashMapOf()

    /** The working assessment, should always match what the UI shows */
    private var mAssessment: MutableMap<String, RubricCriterionAssessment> = hashMapOf()

    /** Max points possible for the current criterion */
    private var mPointsPossible = 0.0

    /** Coroutine job for saving the rubric assessment. Should be canceled on view detached */
    private var mSaveJob: Job? = null

    /** Whether the teacher is grading anonymously */
    private var mAnonymousGrading: Boolean = false

    /** Reference to the assignment */
    lateinit private var mAssignment: Assignment

    /** Reference to the current submission */
    private var mSubmission: Submission? = null

    /** Reference to the current gradeable assignee */
    lateinit private var mAssignee: Assignee

    /** Set this to listen for working score changes. */
    var onScorePreviewUpdated: (score: Double) -> Unit = {}

    /** Set this to be to know when the assessment is saved */
    var onAssessmentSaved: (newSubmission: Submission) -> Unit = {}

    init {
        // Inflate
        View.inflate(context, R.layout.view_edit_rubric, this)

        // Set save button colors
        saveRubricButton.setTextColor(ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
                intArrayOf(ContextCompat.getColor(context, R.color.defaultTextGray), ThemePrefs.buttonColor)
        ))

        saveRubricButton.onClickWithRequireNetwork { saveRubricAssessment() }
    }

    /** Must be called in order to populate this view with rubric information */
    fun setData(assignment: Assignment, submission: Submission?, assignee: Assignee, anonymousGrading: Boolean) {
        mAssignment = assignment
        mSubmission = submission
        mAssignee = assignee
        mAnonymousGrading = anonymousGrading

        // Hide this view if there is no rubric
        if (assignment.rubric == null || assignment.rubric.isEmpty()) {
            setGone()
            return
        }

        // Otherwise, make this view visible
        setVisible()

        // Set the assignee ID on the tooltip view so it can correctly filter rubric events
        tooltipView.assigneeId = assignee.id

        // Get points possible
        mPointsPossible = assignment.rubricSettings?.pointsPossible
                ?: assignment.rubric.sumByDouble { it.points }

        // Clone original assessment to hold unsaved changes
        mOriginalAssessment = submission?.rubricAssessment ?: hashMapOf()
        mAssessment = mOriginalAssessment.mapValues { it.value.copy() }.toMutableMap()

        // Set up criteria views
        criteriaItemsContainer.removeAllViews()
        for ((index, criterion) in assignment.rubric.withIndex()) {
            val critItem = RubricCriterionItemView(context)
            critItem.setCriterion(criterion, mAssignee.id, mAssignee.name, index, assignment.rubricSettings?.freeFormCriterionComments ?: false)
            critItem.setAssessment(mAssessment[criterion.id] ?: RubricCriterionAssessment())
            critItem.gradeAnonymously = mAnonymousGrading
            criteriaItemsContainer.addView(critItem)
        }

        // Disable save button until there are changes
        saveRubricButton.isEnabled = false

        // Update score
        refreshScore()
    }

    /** Attempts to save the rubric assessment to the API */
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun saveRubricAssessment() {
        mSaveJob = weave {
            // Hide save button, show progressbar
            saveRubricButton.setGone()
            errorTextView.setGone()
            saveProgressBar.announceForAccessibility(getContext().getString(R.string.saving))
            saveProgressBar.setVisible()

            try {
                val updatedSubmission = awaitApi<Submission> {
                    SubmissionManager.updateRubricAssessment(mAssignment.courseId, mAssignment.id, mAssignee.id, mAssessment, it)
                }

                // API response does not include rubric assessment, so we need to add it
                updatedSubmission.rubricAssessment.putAll(mAssessment)

                // Use the updated submission
                mSubmission = updatedSubmission

                // Reset the original and working assessments
                mOriginalAssessment = mAssessment
                mAssessment = mOriginalAssessment.mapValues { it.value.copy() }.toMutableMap()

                // Notify listener of updated submission
                onAssessmentSaved(updatedSubmission)

                // Post update event
                AssignmentGradedEvent(mAssignment.id).post()
                SubmissionUpdatedEvent(updatedSubmission).post()

                // Show success toast
                this@RubricEditView.toast(R.string.success_saving_rubric_assessment)

                // Disable save button
                saveRubricButton.isEnabled = false

            } catch (e: Throwable) {
                // Show error text
                errorTextView.setVisible().apply { announceForAccessibility(text) }
            }

            // Show save button, hide progressbar
            saveProgressBar.setGone()
            saveRubricButton.setVisible().apply {
                requestFocus()
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }
        }
    }

    /** Calculates the working score and updates the UI */
    private fun refreshScore(): Double {
        val sum = mAssessment.values.sumByDouble { it.points ?: 0.0 }
        rubricScoreView.text = context.getString(R.string.rubric_assessment_score_out_of_total,
                NumberHelper.formatDecimal(sum, 2, true),
                NumberHelper.formatDecimal(mPointsPossible, 2, true))
        return sum
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    /** Subscription to RatingSelectedEvent. This should update the working assessment and update the UI */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRatingSelected(event: RatingSelectedEvent) {
        if (event.studentId == mAssignee.id) {
            mAssessment.getOrPut(event.criterionId) { RubricCriterionAssessment() }.points = event.points
            val score = refreshScore()
            onScorePreviewUpdated(score)
            checkForChanges()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentUpdated(event: RubricCommentEditedEvent) {
        if (event.studentId == mAssignee.id) {
            mAssessment.getOrPut(event.criterionId) { RubricCriterionAssessment() }.comments = event.text
            checkForChanges()
        }
    }

    /** Filters out assessments that have no data */
    private val mHasData = { (points, comments): RubricCriterionAssessment -> points != null || comments != null }

    val hasUnsavedChanges: Boolean get() = mOriginalAssessment.filterValues(mHasData) != mAssessment.filterValues(mHasData)

    fun getCurrentAssessment() = mAssessment

    /** Checks for differences between the original assessment and the working assessment */
    private fun checkForChanges() {
        saveRubricButton.isEnabled = hasUnsavedChanges
    }

}
