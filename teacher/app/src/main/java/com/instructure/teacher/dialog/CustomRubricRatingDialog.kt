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
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.AppCompatEditText
import android.text.InputType
import android.view.WindowManager
import android.widget.FrameLayout
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.teacher.R
import com.instructure.pandautils.utils.DoubleArg
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.teacher.view.edit_rubric.RatingSelectedEvent
import org.greenrobot.eventbus.EventBus


class CustomRubricRatingDialog : AppCompatDialogFragment() {

    var mCriterionId by StringArg()
    var mStudentId by LongArg(-1L)
    var mMaxValue by DoubleArg()
    var mOldValue by DoubleArg()

    init {
        retainInstance = true
    }

    companion object {
        @JvmStatic
        fun show(manager: FragmentManager, criterionId: String, studentId: Long, oldValue: Double, maxValue: Double) = CustomRubricRatingDialog().apply {
            manager.dismissExisting<CustomRubricRatingDialog>()
            mCriterionId = criterionId
            mStudentId = studentId
            mMaxValue = maxValue
            mOldValue = oldValue
            show(manager, javaClass.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val editText = AppCompatEditText(context).apply {
            ViewStyler.themeEditText(context, this, ThemePrefs.brandColor)
            setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            setText(NumberHelper.formatDecimal(mOldValue, 4, true))
            selectAll()
        }

        val container = FrameLayout(context).apply {
            val padding: Int = context.DP(16f).toInt()
            setPaddingRelative(padding, 0, padding, 0)
            addView(editText)
        }

        val onSave = { _: DialogInterface, _: Int ->
            val newScore = editText.text.toString().toDoubleOrNull()?.coerceAtMost(mMaxValue)
            EventBus.getDefault().post(RatingSelectedEvent(newScore, mCriterionId, mStudentId))
        }

        return AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(context.getString(R.string.criterion_rating_customize_score))
                .setView(container)
                .setPositiveButton(context.getString(android.R.string.ok).toUpperCase(), onSave)
                .setNegativeButton(context.getString(android.R.string.cancel).toUpperCase(), null)
                .create()
                .apply {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    setOnShowListener {
                        getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
                        getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
                    }
                }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

}
