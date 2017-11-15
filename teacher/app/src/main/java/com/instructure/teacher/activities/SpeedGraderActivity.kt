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
package com.instructure.teacher.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.TypedValue
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.coerceAtLeast
import com.instructure.canvasapi2.utils.rangeWithin
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.activities.BasePresenterActivity
import com.instructure.pandautils.utils.*
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.adapters.SubmissionContentAdapter
import com.instructure.teacher.dialog.UnsavedChangesContinueDialog
import com.instructure.teacher.factory.SpeedGraderPresenterFactory
import com.instructure.teacher.presenters.SpeedGraderPresenter
import com.instructure.teacher.router.Route
import com.instructure.teacher.utils.ExoAgent
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.isTalkbackEnabled
import com.instructure.teacher.utils.toast
import com.instructure.teacher.view.AudioPermissionGrantedEvent
import com.instructure.teacher.view.TabSelectedEvent
import com.instructure.teacher.view.VideoPermissionGrantedEvent
import com.instructure.teacher.viewinterface.SpeedGraderView
import com.pspdfkit.preferences.PSPDFKitPreferences
import kotlinx.android.synthetic.main.activity_speedgrader.*
import kotlinx.coroutines.experimental.delay
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class SpeedGraderActivity : BasePresenterActivity<SpeedGraderPresenter, SpeedGraderView>(), SpeedGraderView {

    /* These should be passed to the presenter factory and should not be directly referenced otherwise */
    private val mCourse: Course by lazy { intent.extras.getParcelable<Course>(Const.COURSE) }
    private val mAssignment: Assignment by lazy { intent.extras.getParcelable(Const.ASSIGNMENT) ?: Assignment() }
    private val mSubmissions: ArrayList<GradeableStudentSubmission> by lazy { intent.extras.getParcelableArrayList<GradeableStudentSubmission>(Const.SUBMISSION) ?: arrayListOf() }
    private val mDiscussionTopicHeader: DiscussionTopicHeader? by lazy { intent.extras.getParcelable<DiscussionTopicHeader>(Const.DISCUSSION_HEADER) }

    private val mInitialSelection: Int by lazy { intent.extras.getInt(Const.SELECTED_ITEM, 0) }
    private var mCurrentSelection = 0
    private var mPreviousSelection = 0

    // Used for keeping track of the page that is asking for media permissions from SubmissionContentView
    private var assigneeId: Long = -1L

    // Used in the SubmissionViewFragments in the ViewPager to handle issues with sliding panel
    var mIsCurrentlyAnnotating = false

    private lateinit var mAdapter: SubmissionContentAdapter

    override fun unBundle(extras: Bundle) = Unit

    override fun onPresenterPrepared(presenter: SpeedGraderPresenter?) = Unit

    override fun onReadySetGo(presenter: SpeedGraderPresenter?) {
        presenter?.setupData()
    }

    override fun getPresenterFactory() = SpeedGraderPresenterFactory(
            mCourse,
            mAssignment,
            mSubmissions,
            mDiscussionTopicHeader
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the PDF author, but only if it hasn't been set yet
        if (!PSPDFKitPreferences.get(this).isAnnotationCreatorSet) {
            PSPDFKitPreferences.get(this).setAnnotationCreator(ApiPrefs.user?.name)
        }
        setContentView(R.layout.activity_speedgrader)
    }

    override fun onDataSet(assignment: Assignment, submissions: List<GradeableStudentSubmission>) {
        mAdapter = SubmissionContentAdapter(assignment, presenter.course, submissions)
        submissionContentPager.offscreenPageLimit = 1
        submissionContentPager.pageMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics))
        submissionContentPager.setPageMarginDrawable(R.color.dividerColor)
        submissionContentPager.adapter = mAdapter
        submissionContentPager.setCurrentItem(mInitialSelection, false)
        submissionContentPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                mPreviousSelection = mCurrentSelection
                mCurrentSelection = position
                if (mAdapter.hasUnsavedChanges(mPreviousSelection)) {
                    UnsavedChangesContinueDialog.show(supportFragmentManager) {
                        submissionContentPager.setCurrentItem(mPreviousSelection, true)
                    }
                }
            }
        })
        setupTutorialView()
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun setupTutorialView() = weave {
        if (BuildConfig.IS_TESTING || TeacherPrefs.hasViewedSwipeTutorial || mAdapter.count < 2 || isTalkbackEnabled()) return@weave

        delay(TUTORIAL_DELAY)
        swipeTutorialView.setVisible().onClick {
            if (it.alpha != 1f) return@onClick
            ObjectAnimator.ofFloat(it, "alpha", 1f, 0f).apply {
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) = Unit
                    override fun onAnimationCancel(animation: Animator?) = Unit
                    override fun onAnimationStart(animation: Animator?) = Unit
                    override fun onAnimationEnd(animation: Animator?) {
                        it.setGone()
                        TeacherPrefs.hasViewedSwipeTutorial = true
                    }
                })
                duration = TUTORIAL_DELAY
            }.start()
        }
        ObjectAnimator.ofFloat(swipeTutorialView, "alpha", 0f, 1f).apply {
            duration = TUTORIAL_DELAY
        }.start()
    }

    override fun onErrorSettingData() {
        toast(R.string.errorOccurred)
        finish()
    }

    fun enableViewPager() {
        submissionContentPager.isPagingEnabled = true
    }

    fun disableViewPager() {
        submissionContentPager.isPagingEnabled = false
    }

    fun lockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    fun unlockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ExoAgent.releaseAllAgents() // Stop any media playback
    }

    @Subscribe
    fun onTabSelected(event: TabSelectedEvent) {
        mAdapter.initialTabIdx = event.selectedTabIdx
    }

    fun requestAudioPermissions(assigneeId: Long) {
        if (checkAudioPermission()) {
            // We have the permission
            EventBus.getDefault().post(AudioPermissionGrantedEvent(assigneeId))
        } else {
            this.assigneeId = assigneeId
            ActivityCompat.requestPermissions(this, arrayOf(PermissionUtils.RECORD_AUDIO), PermissionUtils.PERMISSION_REQUEST_CODE)
        }
    }

    fun requestVideoPermissions(assigneeId: Long) {
        if (checkVideoPermission()) {
            // We have the permissions
            EventBus.getDefault().post(VideoPermissionGrantedEvent(assigneeId))
        } else {
            this.assigneeId = assigneeId
            ActivityCompat.requestPermissions(this, arrayOf(PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO), PermissionUtils.PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkAudioPermission(): Boolean = ContextCompat.checkSelfPermission(this, PermissionUtils.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    private fun checkVideoPermission(): Boolean = ContextCompat.checkSelfPermission(this, PermissionUtils.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE && grantResults.size >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when {
                permissions.contains(PermissionUtils.CAMERA) && permissions.contains(PermissionUtils.RECORD_AUDIO) -> {
                    if(grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                        EventBus.getDefault().post(VideoPermissionGrantedEvent(assigneeId))
                    }
                }
                permissions.contains(PermissionUtils.RECORD_AUDIO) ->  EventBus.getDefault().post(AudioPermissionGrantedEvent(assigneeId))
            }
        }
    }

    companion object {

        private val TUTORIAL_DELAY = 400L

        /**
         * The number of submissions to be bundled (pre-cached) to either side of the selected
         * submission. If this value is too high it may result in Android throwing a
         * TransactionTooLargeException when creating SpeedGraderActivity.
         */
        private val MAX_CACHED_ADJACENT = 6

        /**
         * The maximum submission history depth allowed for a submission to be eligible for
         * pre-caching. If this value is too high it may result in Android throwing a
         * TransactionTooLargeException when creating SpeedGraderActivity.
         */
        private val MAX_HISTORY_THRESHOLD = 8

        @JvmStatic
        fun makeBundle(course: Course, assignment: Assignment, submissions: List<GradeableStudentSubmission>, selectedIdx: Int): Bundle {
            return Bundle().apply {
                putParcelable(Const.COURSE, course as Parcelable)
                putParcelable(Const.ASSIGNMENT, assignment as Parcelable)

                // Avoid TransactionTooLargeException by only bundling submissions in the cached range with shallow submission histories
                val cachedRange = selectedIdx.rangeWithin(MAX_CACHED_ADJACENT).coerceAtLeast(0)
                val compactSubmissions = submissions.mapIndexed { index, submission ->
                    val inRange = index in cachedRange
                    val smallHistory = submission.submission?.submissionHistory?.size ?: 0 <= MAX_HISTORY_THRESHOLD
                    val smallBodies = submission.submission?.submissionHistory?.none { it?.body?.length ?: 0 > 2048 } ?: true
                    if (inRange && smallHistory && smallBodies) {
                        submission.copy(isCached = true)
                    } else {
                        submission.copy(submission = null, isCached = false)
                    }
                }

                putParcelableArrayList(Const.SUBMISSION, ArrayList(compactSubmissions))
                putInt(Const.SELECTED_ITEM, selectedIdx)
            }
        }

        @JvmStatic
        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, SpeedGraderActivity::class.java)
            intent.putExtras(route.arguments)
            return intent
        }
    }
}
