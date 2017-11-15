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
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageView
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.R
import com.instructure.pandautils.utils.dismissExisting
import kotlin.properties.Delegates

class ColorPickerDialog: AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var mCallback: (Int) -> Unit by Delegates.notNull()

    companion object {
        @JvmStatic
        fun newInstance(manager: FragmentManager, course: Course, callback: (Int) -> Unit): ColorPickerDialog {
            manager.dismissExisting<ColorPickerDialog>()
            val dialog = ColorPickerDialog()
            val args = Bundle()
            args.putParcelable(Const.COURSE, course)
            dialog.arguments = args
            dialog.mCallback = callback
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val view = View.inflate(ContextThemeWrapper(activity, 0), R.layout.dialog_color_picker, null)
        setupViews(view)
        builder.setView(view)
        builder.setTitle(R.string.colorPickerDialogTitle)
        builder.setCancelable(true)
        return builder.create()
    }

    fun setupViews(view: View) {

        val comp_colorCottonCandy = view.findViewById<ImageView>(R.id.colorCottonCandy)
        val comp_colorBarbie = view.findViewById<ImageView>(R.id.colorBarbie)
        val comp_colorBarneyPurple = view.findViewById<ImageView>(R.id.colorBarneyPurple)
        val comp_colorEggplant = view.findViewById<ImageView>(R.id.colorEggplant)
        val comp_colorUltramarine = view.findViewById<ImageView>(R.id.colorUltramarine)

        val comp_colorOcean11 = view.findViewById<ImageView>(R.id.colorOcean11)
        val comp_colorCyan = view.findViewById<ImageView>(R.id.colorCyan)
        val comp_colorAquaMarine = view.findViewById<ImageView>(R.id.colorAquaMarine)
        val comp_colorEmeraldGreen = view.findViewById<ImageView>(R.id.colorEmeraldGreen)
        val comp_colorFreshCutLawn = view.findViewById<ImageView>(R.id.colorFreshCutLawn)

        val comp_colorChartreuse = view.findViewById<ImageView>(R.id.colorChartreuse)
        val comp_colorSunFlower = view.findViewById<ImageView>(R.id.colorSunFlower)
        val comp_colorTangerine = view.findViewById<ImageView>(R.id.colorTangerine)
        val comp_colorBloodOrange = view.findViewById<ImageView>(R.id.colorBloodOrange)
        val comp_colorSriracha = view.findViewById<ImageView>(R.id.colorSriracha)

        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorCottonCandy), comp_colorCottonCandy)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorBarbie), comp_colorBarbie)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorBarneyPurple), comp_colorBarneyPurple)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorEggplant), comp_colorEggplant)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorUltramarine), comp_colorUltramarine)

        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorOcean11), comp_colorOcean11)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorCyan), comp_colorCyan)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorAquaMarine), comp_colorAquaMarine)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorEmeraldGreen), comp_colorEmeraldGreen)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorFreshCutLawn), comp_colorFreshCutLawn)

        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorChartreuse), comp_colorChartreuse)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorSunFlower), comp_colorSunFlower)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorTangerine), comp_colorTangerine)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorBloodOrange), comp_colorBloodOrange)
        ColorUtils.colorIt(ContextCompat.getColor(context, R.color.colorSriracha), comp_colorSriracha)

        comp_colorCottonCandy.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorCottonCandy))
            dismiss()
        }
        comp_colorBarbie.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorBarbie))
            dismiss()
        }
        comp_colorBarneyPurple.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorBarneyPurple))
            dismiss()
        }
        comp_colorEggplant.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorEggplant))
            dismiss()
        }
        comp_colorUltramarine.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorUltramarine))
            dismiss()
        }
        comp_colorOcean11.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorOcean11))
            dismiss()
        }
        comp_colorCyan.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorCyan))
            dismiss()
        }
        comp_colorAquaMarine.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorAquaMarine))
            dismiss()
        }
        comp_colorEmeraldGreen.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorEmeraldGreen))
            dismiss()
        }
        comp_colorFreshCutLawn.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorFreshCutLawn))
            dismiss()
        }
        comp_colorChartreuse.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorChartreuse))
            dismiss()
        }
        comp_colorSunFlower.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorSunFlower))
            dismiss()
        }
        comp_colorTangerine.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorTangerine))
            dismiss()
        }
        comp_colorBloodOrange.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorBloodOrange))
            dismiss()
        }
        comp_colorSriracha.setOnClickListener {
            mCallback(ContextCompat.getColor(context, R.color.colorSriracha))
            dismiss()
        }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
