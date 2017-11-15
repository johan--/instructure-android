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
package com.instructure.teacher.fragments

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.teacher.R
import com.instructure.teacher.dialog.HelpDialogStyled
import com.instructure.teacher.dialog.LegalDialog
import com.instructure.teacher.factory.ProfileSettingsFragmentPresenterFactory
import com.instructure.teacher.presenters.ProfileSettingsFragmentPresenter
import com.instructure.teacher.tasks.LogoutAsyncTask
import com.instructure.teacher.tasks.SwitchUsersAsyncTask
import com.instructure.teacher.utils.isTablet
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.ProfileSettingsFragmentView
import kotlinx.android.synthetic.main.fragment_profile_settings.*

class ProfileSettingsFragment : BasePresenterFragment<
        ProfileSettingsFragmentPresenter,
        ProfileSettingsFragmentView>(), ProfileSettingsFragmentView {

    override fun layoutResId() = R.layout.fragment_profile_settings

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        displayVersionName()

        logoutTextView.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.teacher_logout_warning)
                    .setPositiveButton(R.string.teacher_yes) { dialog, _ ->
                        LogoutAsyncTask().execute()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.teacher_no) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

            dialog.setOnShowListener {
                dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
                dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
            }
            dialog.show()
        }

        changeUserTextView.setOnClickListener { SwitchUsersAsyncTask().execute() }

        ratingTextView.setOnClickListener {
            RatingDialog.showRateDialog(activity, com.instructure.pandautils.utils.AppType.TEACHER)
        }

        legalTextView.setOnClickListener {
            LegalDialog().show(fragmentManager, LegalDialog.TAG)
        }

        helpTextView.setOnClickListener {
            HelpDialogStyled.show(activity)
        }
    }

    private fun displayVersionName() {
        try {
            versionTextView.text = String.format(getString(R.string.version), activity.applicationContext
                    .packageManager.getPackageInfo(activity.applicationContext.packageName, 0).versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e("Error getting version: " + e)
        }
    }

    override fun getPresenterFactory() = ProfileSettingsFragmentPresenterFactory()

    override fun onReadySetGo(presenter: ProfileSettingsFragmentPresenter?) {
        setupToolbar()
    }

    fun setupToolbar() {
        toolbar.setupBackButton(this)
        toolbar.title = getString(R.string.settings)
        ViewStyler.themeToolbarBottomSheet(activity, isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(context, toolbar)
    }

    override fun onRefreshStarted() {}


    override fun onRefreshFinished() {}


    override fun onPresenterPrepared(presenter: ProfileSettingsFragmentPresenter?) {}


    companion object {
        @JvmStatic
        fun newInstance(args: Bundle) = ProfileSettingsFragment().apply {}
    }
}
