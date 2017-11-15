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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.managers.AnalyticsManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.inParallel
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.teacher.viewinterface.StudentContextView
import instructure.androidblueprint.FragmentPresenter
import kotlinx.coroutines.experimental.Job
import java.util.*


class StudentContextPresenter(
        private val mStudentId: Long,
        private val mCourseId: Long
) : FragmentPresenter<StudentContextView>() {

    private val SUBMISSION_PAGE_SIZE = 10
    private val permissions = listOf(
            CanvasContextPermission.MANAGE_GRADES,
            CanvasContextPermission.SEND_MESSAGES,
            CanvasContextPermission.VIEW_ALL_GRADES,
            CanvasContextPermission.VIEW_ANALYTICS,
            CanvasContextPermission.BECOME_USER
    )

    private lateinit var mStudent: User
    private lateinit var mCourse: Course
    private lateinit var mSections: List<Section>
    private lateinit var mSubmissions: List<Submission>
    private lateinit var mPermissions: CanvasContextPermission
    private var mStudentSummary: StudentSummary? = null
    private var mIsStudent = false

    private var mIsLoaded = false
    private var mApiJob: Job? = null
    private var mAddedSubmissionCount = 0

    override fun loadData(forceNetwork: Boolean) {
        if (mIsLoaded) {
            viewCallback?.onRefreshFinished()
            viewCallback?.setData(mCourse, mSections, mStudent, mStudentSummary, mIsStudent)
            loadMoreSubmissions(0)
            return
        }
        if (mApiJob?.isActive == true) return
        viewCallback?.onRefreshStarted()
        mApiJob = tryWeave {

            var sections: List<Section> = emptyList()
            inParallel {
                // Get course
                await<Course>({ CourseManager.getCourse(mCourseId, it, forceNetwork) }) {
                    mCourse = it
                }

                // Get course sections
                await<List<Section>>({ SectionManager.getAllSectionsForCourse(mCourseId, it, forceNetwork) }) {
                    sections = it
                }

                // Get student in the context of this course
                await<User>({ CourseManager.getCourseStudent(mCourseId, mStudentId, it, forceNetwork) }) {
                    mStudent = it
                    mIsStudent = it.enrollments.any { it.isStudent }
                }

                // Get student summary analytics
                await<List<StudentSummary>>(
                        managerCall = { AnalyticsManager.getStudentSummaryForCourse(mStudentId, mCourseId, it, forceNetwork) },
                        onComplete = { mStudentSummary = it.firstOrNull() },
                        errorCall = {
                            /* Student summary API may return a 404. This is expected in some cases
                               and we don't want it throw an exception which would cancel the other
                               API calls, so we return true for 404s. */
                            it.response?.code() == 404
                        }
                )

                // Get student's submissions
                await<List<Submission>>(
                        managerCall = { SubmissionManager.getAllStudentSubmissionsForCourse(mStudentId, mCourseId, it, forceNetwork) },
                        onComplete = {
                            val defaultDate = Date(0)
                            mSubmissions = it.sortedByDescending { it.submittedAt ?: defaultDate }
                        },
                        errorCall = {
                            /* The submissions API will return a 401 if the specified user is not a
                            student in the course. We'll handle this case manually instead of
                            displaying a generic error. */
                            mSubmissions = emptyList()
                            it.response?.code() == 401
                        }
                )

                // Get course permissions
                await<CanvasContextPermission>({ CourseManager.getCoursePermissions(mCourseId, permissions, it, forceNetwork) }) {
                    mPermissions = it
                }
            }

            mSections = sections.filter { section -> mStudent.enrollments.any { it.courseSectionId == section.id } }
            viewCallback?.onRefreshFinished()
            viewCallback?.setData(mCourse, mSections, mStudent, mStudentSummary, mIsStudent)
            loadMoreSubmissions()
            mIsLoaded = true
        } catch {
            viewCallback?.onErrorLoading()
        }
    }

    fun loadMoreSubmissions(additionCount: Int = SUBMISSION_PAGE_SIZE) {
        when (additionCount) {
            0 -> viewCallback?.addSubmissions(
                    mSubmissions.take(mAddedSubmissionCount),
                    mCourse,
                    mStudent
            )
            else -> viewCallback?.addSubmissions(
                    mSubmissions.subList(mAddedSubmissionCount, (mAddedSubmissionCount + additionCount).coerceAtMost(mSubmissions.size)),
                    mCourse,
                    mStudent
            )
        }
        mAddedSubmissionCount += additionCount
        if (mAddedSubmissionCount >= mSubmissions.size) viewCallback?.showLoadMoreButton(false)

    }

    override fun refresh(forceNetwork: Boolean) {}

    override fun onDestroyed() {
        super.onDestroyed()
        mApiJob?.cancel()
    }
}
