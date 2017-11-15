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
package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.AppCompatCheckBox
import android.widget.FrameLayout
import com.instructure.canvasapi2.utils.NetworkUtils
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.teacher.R
import com.instructure.pandautils.utils.BlindSerializableArg
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.pandautils.utils.dismissExisting

class MobileDataWarningDialog : AppCompatDialogFragment() {

    private var mOnProceed: (() -> Unit)? by BlindSerializableArg()

    init { retainInstance = true }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val checkBox = AppCompatCheckBox(context).apply {
            isChecked = false
            setText(R.string.doNotShowMessageAgain)
            textSize = 12f
        }

        val checkBoxContainer = FrameLayout(context).apply {
            val pad = context.DP(16).toInt()
            setPadding(pad, pad, pad, 0)
            addView(checkBox)
        }

        val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.dataUsageWarningTitle)
                .setMessage(R.string.dataUsageWarningMessage)
                .setView(checkBoxContainer)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    TeacherPrefs.warnForMobileData = !checkBox.isChecked
                    mOnProceed?.invoke()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    TeacherPrefs.warnForMobileData = !checkBox.isChecked
                }
                .create()
        return dialog.apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
            }
        }
    }

    override fun onDestroyView() {
        mOnProceed = null
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

    companion object {
        fun showIfNeeded(manager: FragmentManager, onProceed: () -> Unit) {
            if (NetworkUtils.isUsingMobileData && TeacherPrefs.warnForMobileData) {
                MobileDataWarningDialog().apply {
                    manager.dismissExisting<MobileDataWarningDialog>()
                    mOnProceed = onProceed
                    show(manager, javaClass.simpleName)
                }
            } else {
                onProceed()
            }
        }
    }
}
