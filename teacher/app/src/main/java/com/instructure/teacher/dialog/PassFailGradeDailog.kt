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
package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.AppCompatCheckBox
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.teacher.R
import com.instructure.teacher.utils.Const
import com.instructure.pandautils.utils.dismissExisting
import kotlin.properties.Delegates

class PassFailGradeDailog : AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var mPassFailGradeCallback: (String, Boolean) -> Unit by Delegates.notNull()

    companion object {
        @JvmStatic
        fun getInstance(fragmentManager: FragmentManager, grade: String?, callback: (String, Boolean) -> Unit) : PassFailGradeDailog {
            fragmentManager.dismissExisting<PassFailGradeDailog>()
            val dialog = PassFailGradeDailog()
            val args = Bundle()
            args.putString(Const.GRADE, grade)
            dialog.arguments = args
            dialog.mPassFailGradeCallback = callback

            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(activity, R.layout.dialog_pass_fail_grade, null)
        val gradeOptions = resources.getStringArray(R.array.passFailArray)
        val grade: String? = arguments.getString(Const.GRADE)
        val passFailSpinner = view.findViewById<Spinner>(R.id.passFailSpinner)
        val excusedCheckBox = view.findViewById<AppCompatCheckBox>(R.id.excuseStudentCheckbox)
        val passFailAdapter = ArrayAdapter<String>(activity, R.layout.spinner_pass_fail_item, gradeOptions)

        //style views
        ViewStyler.themeCheckBox(context, excusedCheckBox, ThemePrefs.brandColor)

        //set spinner adapter
        passFailSpinner.adapter = passFailAdapter

        //listen for checkbox
        excusedCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            passFailSpinner.isEnabled = !isChecked
        }

        //set the spinner selection to the user's current grade, default is complete
        when(grade?.toLowerCase()) {
            resources.getString(R.string.complete_grade).toLowerCase() -> passFailSpinner.setSelection(0)
            resources.getString(R.string.incomplete_grade).toLowerCase() -> passFailSpinner.setSelection(1)
            else -> passFailSpinner.setSelection(0)
        }

        val passFailDialog = AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle(getString(R.string.customize_grade))
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok).toUpperCase(), { dialog, which ->
                    mPassFailGradeCallback(passFailSpinner.selectedItem.toString().toLowerCase(), excusedCheckBox.isChecked)
                })
                .setNegativeButton(getString(android.R.string.cancel).toUpperCase(), null)
                .create()

        passFailDialog.setOnShowListener {
            passFailDialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            passFailDialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        return passFailDialog
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
