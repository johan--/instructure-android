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
package com.instructure.teacher.adapters

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.factory.StudentContextPresenterFactory
import com.instructure.teacher.fragments.AddMessageFragment
import com.instructure.teacher.holders.StudentContextSubmissionView
import com.instructure.teacher.interfaces.MasterDetailInteractions
import com.instructure.teacher.presenters.StudentContextPresenter
import com.instructure.teacher.router.Route
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.utils.ProfileUtils
import com.instructure.teacher.viewinterface.StudentContextView
import instructure.androidblueprint.PresenterFragment
import kotlinx.android.synthetic.main.fragment_student_context.*


class StudentContextFragment : PresenterFragment<StudentContextPresenter, StudentContextView>(), StudentContextView {

    private var mStudentId by LongArg()
    private var mCourseId by LongArg()
    private var mLaunchSubmissions by BooleanArg()

    private var mHasLoaded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mHasLoaded = false
        return inflater.inflate(R.layout.fragment_student_context, container, false)
    }

    override fun getPresenterFactory() = StudentContextPresenterFactory(mStudentId, mCourseId)

    override fun onRefreshStarted() {
        toolbar.setGone()
        contentContainer.setGone()
        loadingView.setVisible()
        loadingView.announceForAccessibility(getString(R.string.Loading))
    }

    override fun onRefreshFinished() {
        loadingView.setGone()
        toolbar.setVisible()
        contentContainer.setVisible()
    }

    override fun onPresenterPrepared(presenter: StudentContextPresenter) {}

    override fun onReadySetGo(presenter: StudentContextPresenter) {
        loadMoreButton.onClick { presenter.loadMoreSubmissions() }
        if (!mHasLoaded) {
            presenter.loadData(false)
            mHasLoaded = true
        }
    }

    override fun setData(course: Course, sections: List<Section>, student: User, summary: StudentSummary?, isStudent: Boolean) {
        // Toolbar setup
        if (activity is MasterDetailInteractions) {
            toolbar.setupBackButtonWithExpandCollapseAndBack(this) {
                toolbar.updateToolbarExpandCollapseIcon(this)
                ViewStyler.themeToolbar(activity, toolbar, course.color, Color.WHITE)
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
        } else {
            toolbar.setupBackButton(this)
        }
        toolbar.title = student.name
        toolbar.subtitle = course.name
        ViewStyler.themeToolbar(activity, toolbar, course.color, Color.WHITE)

        // Message FAB
        messageButton.setVisible()
        ViewStyler.themeFAB(messageButton, ThemePrefs.buttonColor)
        messageButton.setOnClickListener {
            val args = AddMessageFragment.createBundle(arrayListOf(BasicUser.userToBasicUser(student)), "", course.contextId, true)
            RouteMatcher.route(context, Route(AddMessageFragment::class.java, null, args))
        }

        // Student name and email
        studentNameView.text = student.name
        val email = student.primaryEmail ?: student.email
        studentEmailView.setVisible(email.isValid()).text = email

        // Avatar
        ProfileUtils.loadAvatarForUser(context, avatarView, student.name, student.avatarUrl)

        // Course and section names
        courseNameView.text = course.name
        sectionNameView.text = if (isStudent) {
            getString(R.string.sectionFormatted, sections.map { it.name }.joinToString())
        } else {
            val getEnrollmentType = { section: Section -> student.enrollments.first { it.courseSectionId == section.id }.displayType }
            getString(R.string.sectionFormatted, sections.map { "${it.name} (${getEnrollmentType(it)})" }.joinToString())
        }

        // Latest activity
        student.enrollments.firstOrNull()?.lastActivityAt?.let {
            val dateString = DateHelper.getFormattedDate(context, student.enrollments.first().lastActivityAt)
            val timeString = DateHelper.getFormattedTime(context, student.enrollments.first().lastActivityAt)
            lastActivityView.text = getString(R.string.latestStudentActivityAtFormatted, dateString, timeString)
        } ?: lastActivityView.setGone()

        if (isStudent) {
            // Grade
            gradeView.text = student.enrollments.firstOrNull()?.grades?.let { it.currentGrade ?: it.currentScore.toString() } ?: "-"

            // Missing
            missingCountView.text = summary?.tardinessBreakdown?.missing?.toString() ?: "-"

            // Late
            lateCountView.text = summary?.tardinessBreakdown?.late?.toString() ?: "-"
        } else {
            messageButton.setGone()
            val lastIdx = scrollContent.indexOfChild(additionalInfoContainer)
            scrollContent.children.forEachIndexed { idx, v -> if (idx > lastIdx) v.setGone() }
        }
    }

    override fun addSubmissions(submissions: List<Submission>, course: Course, student: User) {
        submissions.forEach { submission ->
            val view = StudentContextSubmissionView(context, submission, course.color)
            if (mLaunchSubmissions) view.onClick {
                val studentSubmission = GradeableStudentSubmission(StudentAssignee(student), submission)
                val bundle = SpeedGraderActivity.makeBundle(course, submission.assignment!!, listOf(studentSubmission), 0)
                RouteMatcher.route(context, Route(bundle, Route.RouteContext.SPEED_GRADER))
            }
            submissionListContainer.addView(view)
        }
    }

    override fun showLoadMoreButton(show: Boolean) {
        loadMoreButton.setVisible(show)
    }

    override fun onErrorLoading() {
        toast(R.string.errorLoadingStudentContextCard)
        activity.onBackPressed()
    }


    companion object {
        @JvmStatic
        fun makeBundle(studentId: Long, courseId: Long, launchSubmissions: Boolean = false) = StudentContextFragment().apply {
            mStudentId = studentId
            mCourseId = courseId
            mLaunchSubmissions = launchSubmissions
        }.nonNullArgs

        @JvmStatic
        fun newInstance(bundle: Bundle) = StudentContextFragment().apply { arguments = bundle }
    }

}
