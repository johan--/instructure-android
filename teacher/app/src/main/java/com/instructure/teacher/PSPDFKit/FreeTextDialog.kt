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
package com.instructure.teacher.PSPDFKit

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.AppCompatEditText
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.teacher.R
import com.instructure.pandautils.utils.dismissExisting
import kotlin.properties.Delegates


class FreeTextDialog : AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var mFreeTextCallback: (cancelled: Boolean, text: String) -> Unit by Delegates.notNull()
    private var mCurrentText = ""

    @Suppress("unused")
    private fun FreeTextDialog() { }

    companion object {
        @JvmStatic
        fun getInstance(manager: FragmentManager, currentText: String = "", callback: (Boolean, String) -> Unit) : FreeTextDialog {
            manager.dismissExisting<FreeTextDialog>()
            val dialog = FreeTextDialog()
            dialog.mFreeTextCallback = callback
            dialog.mCurrentText = currentText
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val view = View.inflate(activity, R.layout.dialog_free_text, null)
        val freeTextEditText = view.findViewById<AppCompatEditText>(R.id.freeTextInput)
        freeTextEditText.setText(mCurrentText)
        ViewStyler.themeEditText(context, freeTextEditText, ThemePrefs.brandColor)
        freeTextEditText.inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES

        val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.input_text))
                .setView(view)
                .setPositiveButton(activity.getString(android.R.string.ok), { _, _ ->
                    mFreeTextCallback(false, freeTextEditText.text.toString())
                })
                .setNegativeButton(activity.getString(R.string.cancel), { _, _ ->
                    mFreeTextCallback(true, "")

                })
                .create()

        //Adjust the dialog to the top so keyboard does not cover it up, issue happens on tablets in landscape
        val params = dialog.window.attributes
        params.gravity = Gravity.CENTER or Gravity.TOP
        params.y = 120
        dialog.window.attributes = params
        dialog.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        return dialog
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
