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

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatDialog
import android.support.v7.app.AppCompatDialogFragment
import android.widget.DatePicker
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.dismissExisting
import java.util.*
import kotlin.properties.Delegates

class DatePickerDialogFragment : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {

    var mCallback: (year: Int, month: Int, dayOfMonth: Int) -> Unit by Delegates.notNull()
    var mDefaultDate by SerializableArg(Date())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Setup default date
        val c = Calendar.getInstance()
        c.time = mDefaultDate
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dialog = DatePickerDialog(activity, this, year, month, day)

        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        return dialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mCallback(year, month, dayOfMonth)
    }

    companion object {
        @JvmStatic
        fun getInstance(manager: FragmentManager, defaultDate: Date? = null, callback: (Int, Int, Int) -> Unit) : DatePickerDialogFragment {
            manager.dismissExisting<DatePickerDialogFragment>()
            val dialog = DatePickerDialogFragment()
            dialog.mCallback = callback
            defaultDate?.let { dialog.mDefaultDate = it }
            return dialog
        }
    }
}
