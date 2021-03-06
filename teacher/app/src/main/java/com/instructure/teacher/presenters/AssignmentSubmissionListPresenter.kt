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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.post_models.AssignmentPostBody
import com.instructure.canvasapi2.utils.intersectBy
import com.instructure.canvasapi2.utils.shuffled
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.teacher.events.AssignmentUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.getState
import com.instructure.teacher.viewinterface.AssignmentSubmissionListView
import instructure.androidblueprint.SyncPresenter
import kotlinx.coroutines.experimental.Job
import java.util.*

class AssignmentSubmissionListPresenter(val mAssignment: Assignment, private var mFilter: SubmissionListFilter) : SyncPresenter<GradeableStudentSubmission, AssignmentSubmissionListView>(GradeableStudentSubmission::class.java) {

    enum class SubmissionListFilter {
        ALL,
        LATE,
        MISSING,
        NOT_GRADED,
        GRADED,
        BELOW_VALUE,
        ABOVE_VALUE
    }

    private var apiCalls: Job? = null

    private var mUnfilteredSubmissions: List<GradeableStudentSubmission> = emptyList()
    private var mFilteredSubmissions: List<GradeableStudentSubmission> = emptyList()

    private var mFilterValue: Double = 0.0

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun loadData(forceNetwork: Boolean) {
        // Skip if API call are already in progress
        if (apiCalls?.isActive == true) return

        // Use existing data if we already have it. Unfiltered submissions should be cleared on refresh
        if (!forceNetwork && mUnfilteredSubmissions.isNotEmpty()) {
            setFilteredData()
            return
        }

        // Get data from network
        apiCalls = weave {
            try {
                viewCallback?.onRefreshStarted()
                val (gradeableStudents, enrollments, submissions) = awaitApis<List<GradeableStudent>, List<Enrollment>, List<Submission>>(
                        { AssignmentManager.getAllGradeableStudentsForAssignment(mAssignment.courseId, mAssignment.id, forceNetwork, it) },
                        { EnrollmentManager.getAllEnrollmentsForCourse(mAssignment.courseId, null, forceNetwork, it) },
                        { AssignmentManager.getAllSubmissionsForAssignment(mAssignment.courseId, mAssignment.id, forceNetwork, it) }
                )
                val enrollmentMap = enrollments.associateBy { it.user.id }
                val students = gradeableStudents.distinctBy { it.id }.map { enrollmentMap[it.id]?.user }.filterNotNull()
                if (mAssignment.groupCategoryId > 0 && !mAssignment.isGradeGroupsIndividually) {
                    val groups = awaitApi<List<Group>> { CourseManager.getGroupsForCourse(mAssignment.courseId, it, false) }
                            .filter { it.groupCategoryId == mAssignment.groupCategoryId }
                    mUnfilteredSubmissions = makeGroupSubmissions(students, groups, submissions)
                } else {
                    val submissionMap = submissions.associateBy { it.userId }
                    mUnfilteredSubmissions = students.map {
                        GradeableStudentSubmission(StudentAssignee(it), submissionMap[it.id])
                    }
                }

                //see if anonymous grading is set by the institution
                val features = awaitApi<List<String>> { FeaturesManager.getEnabledFeaturesForCourse(mAssignment.courseId, forceNetwork, it) }
                if (features.contains(FeaturesAPI.ANONYMOUS_GRADING)) {
                    TeacherPrefs.enforceGradeAnonymously = true
                    viewCallback?.removeAnonymousGradingOption(true)
                } else {
                    TeacherPrefs.enforceGradeAnonymously = false
                    viewCallback?.removeAnonymousGradingOption(false)
                }

                setFilteredData()
            } catch (ignore: Throwable) {
            }
        }
    }

    fun makeGroupSubmissions(students: List<User>, groups: List<Group>, submissions: List<Submission>): List<GradeableStudentSubmission> {
        // Set up group assignees
        val userMap = students.associateBy { it.id }
        val groupAssignees = groups.map { GroupAssignee(it, it.users.map { userMap[it.id] ?: it }) }

        // Set up individual student assignees, if any
        val individualIds = students.map { it.id } - groupAssignees.flatMap { it.students.map { it.id } }
        val individualAssignees = individualIds
                .map { userMap[it] }
                .filterNotNull()
                .map { StudentAssignee(it) }

        // Divide submissions into group and individual submissions
        val (groupedSubmissions, individualSubmissions) = submissions.partition { it.group?.id ?: 0L != 0L }
        val studentSubmissionMap = individualSubmissions.associateBy { it.userId }
        val groupSubmissionMap = groupedSubmissions
                .groupBy { it.group!!.id }
                .mapValues {
                    it.value.reduce { acc, submission ->
                        acc.submissionComments = acc.submissionComments.intersectBy(submission.submissionComments) {
                            "${it.authorId}|${it.comment}|${it.createdAt?.time}"
                        }
                        acc
                    }
                }
                .toMap()

        // Set up GradeableStudentSubmissions
        val groupSubs = groupAssignees.map { GradeableStudentSubmission(it, groupSubmissionMap[it.group.id]) }
        val individualSubs = individualAssignees.map { GradeableStudentSubmission(it, studentSubmissionMap[it.student.id]) }

        return groupSubs + individualSubs
    }

    private fun setFilteredData() {
        mFilteredSubmissions = mUnfilteredSubmissions.filter {
            when (mFilter) {
                SubmissionListFilter.ALL -> true
                SubmissionListFilter.LATE -> it.submission?.let { mAssignment.getState(it) == AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE } ?: false
                SubmissionListFilter.NOT_GRADED -> it.submission?.let { mAssignment.getState(it) == AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED ||  mAssignment.getState(it) == AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE || !it.isGradeMatchesCurrentSubmission } ?: false
                SubmissionListFilter.GRADED -> it.submission?.let { mAssignment.getState(it) == AssignmentUtils2.ASSIGNMENT_STATE_GRADED  && it.isGradeMatchesCurrentSubmission} ?: false
                SubmissionListFilter.ABOVE_VALUE -> it.submission?.let { it.isGraded && it.score >= mFilterValue } ?: false
                SubmissionListFilter.BELOW_VALUE -> it.submission?.let { it.isGraded && it.score < mFilterValue } ?: false
                // Filtering by ASSIGNMENT_STATE_MISSING here doesn't work because it assumes that the due date has already passed, which isn't necessarily the case when the teacher wants to see
                // which students haven't submitted yet
                SubmissionListFilter.MISSING -> it.submission?.workflowState == "unsubmitted" || it.submission == null
            }
        }

        // Shuffle if grading anonymously
        if (TeacherPrefs.shouldGradeAnonymously(mAssignment.courseId, mAssignment.id)) mFilteredSubmissions = mFilteredSubmissions.shuffled(Random(1234))

        data.clear()
        data.addOrUpdate(mFilteredSubmissions)
        viewCallback?.onRefreshFinished()
        viewCallback?.checkIfEmpty()
    }


    override fun onDestroyed() {
        apiCalls?.cancel()
        super.onDestroyed()
    }

    override fun refresh(forceNetwork: Boolean) {
        clearData()
        mUnfilteredSubmissions = emptyList()
        loadData(forceNetwork)
    }

    fun setFilter(filter: SubmissionListFilter, value: Double = 0.0) {
        //In case this filter hasn't been shown yet, we'll need to show the loader so the user
        //doesn't see "no items" view first
        viewCallback?.onRefreshStarted()
        mFilter = filter
        mFilterValue = value
        setFilteredData()
    }

    fun getFilter() : SubmissionListFilter = mFilter

    fun getFilterPoints() : Double = mFilterValue

    fun getStudents() : ArrayList<BasicUser> {
        return mFilteredSubmissions.map { BasicUser.userToBasicUser((it.assignee as StudentAssignee).student) } as ArrayList<BasicUser>
    }

    override fun compare(item1: GradeableStudentSubmission?, item2: GradeableStudentSubmission?): Int {
        //turns out we do need to sort them by sortable name, but not when anonymous grading is on
        if(item1?.assignee is StudentAssignee && item2?.assignee is StudentAssignee && !TeacherPrefs.shouldGradeAnonymously(mAssignment.courseId, mAssignment.id)) {
            return (item1.assignee as StudentAssignee).student.sortableName.toLowerCase().compareTo((item2.assignee as StudentAssignee).student.sortableName.toLowerCase())
        }
        return -1
    }

    override fun areContentsTheSame(item1: GradeableStudentSubmission?, item2: GradeableStudentSubmission?) = false

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun toggleMuted() {
        tryWeave {
            val postData = AssignmentPostBody().apply { isMuted = !mAssignment.isMuted }
            awaitApi<Assignment> { AssignmentManager.editAssignment(mAssignment.courseId, mAssignment.id, postData, it, false) }
            mAssignment.isMuted = !mAssignment.isMuted
            AssignmentUpdatedEvent(mAssignment.id).post() // Post bus event
            viewCallback?.onMuteUpdated(success = true, isMuted = mAssignment.isMuted)
        } catch {
            viewCallback?.onMuteUpdated(success = false, isMuted = mAssignment.isMuted)
        }
    }
}
