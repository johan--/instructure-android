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
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatDialog
import android.support.v7.app.AppCompatDialogFragment
import android.text.format.DateFormat
import android.widget.TimePicker
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.dismissExisting
import java.util.*
import kotlin.properties.Delegates

class TimePickerDialogFragment : AppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener {

    var mCallback: (hourOfDay: Int, minute: Int) -> Unit by Delegates.notNull()
    var mDefaultDate by SerializableArg(Date())

    @Suppress("unused")
    private fun TimePickerDialog() { }

    companion object {
        @JvmStatic
        fun getInstance(manager: FragmentManager, defaultDate: Date? = null, callback: (Int, Int) -> Unit) : TimePickerDialogFragment {
            manager.dismissExisting<TimePickerDialogFragment>()
            val dialog = TimePickerDialogFragment()
            dialog.mCallback = callback
            defaultDate?.let { dialog.mDefaultDate = it }
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Default time
        val c = Calendar.getInstance()
        c.time = mDefaultDate
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val dialog = TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))

        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        return dialog
    }


    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mCallback(hourOfDay, minute)
    }

}
