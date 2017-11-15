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

package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.ThemeManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.teacher.R
import com.instructure.teacher.fragments.NotATeacherFragment
import com.instructure.teacher.utils.ColorKeeper
import com.instructure.teacher.utils.TeacherPrefs
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.experimental.Job
import retrofit2.Response

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class SplashActivity : AppCompatActivity() {

    var startUp: Job? = null

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, SplashActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        setContentView(R.layout.activity_splash)

        startUp = weave {
            // Grab user teacher status
            try {
                if (TeacherPrefs.isConfirmedTeacher) {
                    CourseManager.getCoursesWithEnrollmentType(true, mUserIsTeacherVerificationCallback, "teacher")
                } else {
                    if (awaitApi<List<Course>> { CourseManager.getCoursesWithEnrollmentType(true, it, "teacher") }.isNotEmpty() ||
                            awaitApi<List<Course>> { CourseManager.getCoursesWithEnrollmentType(true, it, "ta") }.isNotEmpty() ||
                            awaitApi<List<Course>> { CourseManager.getCoursesWithEnrollmentType(true, it, "designer") }.isNotEmpty()) {
                        TeacherPrefs.isConfirmedTeacher = true
                    } else {
                        // The user doesn't have any courses in which they are a teacher
                        // The user doesn't have any courses in which they are a TA; Show them the door
                        canvasLoadingView.setGone()
                        supportFragmentManager.beginTransaction()
                                .add(R.id.splashActivityRootView, NotATeacherFragment(), NotATeacherFragment::class.java.simpleName)
                                .commit()
                        return@weave
                    }
                }

                // Grab colors
                if (ColorKeeper.hasPreviouslySynced) {
                    UserManager.getColors(mUserColorsCallback, true)
                } else {
                    ColorKeeper.addToCache(awaitApi<CanvasColor> { UserManager.getColors(it, true) })
                    ColorKeeper.hasPreviouslySynced = true
                }

                // Grab theme
                if (ThemePrefs.isThemeApplied) {
                    ThemeManager.getTheme(mThemeCallback, true)
                } else {
                    ThemePrefs.applyCanvasTheme(awaitApi<CanvasTheme> { ThemeManager.getTheme(it, true) })
                }
            } catch (e: Throwable) {
                Logger.e(e.message)
            }

            // Set logged user details
            if (Logger.canLogUserDetails()) {
                Logger.d("User detail logging allowed. Setting values.")
                Crashlytics.setUserIdentifier(ApiPrefs.user?.id.toString())
                Crashlytics.setString("domain", ApiPrefs.domain)
            } else {
                Logger.d("User detail logging disallowed. Clearing values.")
                Crashlytics.setUserIdentifier(null)
                Crashlytics.setString("domain", null)
            }

            startActivity(InitActivity.createIntent(this@SplashActivity))
            canvasLoadingView.announceForAccessibility(getString(R.string.loading))
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        startUp?.cancel()
    }

    private val mThemeCallback = object : StatusCallback<CanvasTheme>() {
        override fun onResponse(response: Response<CanvasTheme>, linkHeaders: LinkHeaders, type: ApiType, code: Int) {
            //store the theme
            val theme = response.body()
            ThemePrefs.applyCanvasTheme(theme)
        }
    }

    private val mUserColorsCallback = object : StatusCallback<CanvasColor>() {
        override fun onResponse(response: Response<CanvasColor>, linkHeaders: LinkHeaders, type: ApiType) {
            if (type == ApiType.API) {
                ColorKeeper.addToCache(response.body())
                ColorKeeper.hasPreviouslySynced = true
            }
        }
    }

    private val mUserIsTeacherVerificationCallback = object : StatusCallback<List<Course>>() {
        override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
            if (response.body().isNotEmpty()) {
                TeacherPrefs.isConfirmedTeacher = true
            } else {
                CourseManager.getCoursesWithEnrollmentType(true, mUserIsTAVerificationCallback, "ta")
            }
        }
    }

    private val mUserIsTAVerificationCallback = object : StatusCallback<List<Course>>() {
        override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
                TeacherPrefs.isConfirmedTeacher = response.body().isNotEmpty()
        }
    }
}
