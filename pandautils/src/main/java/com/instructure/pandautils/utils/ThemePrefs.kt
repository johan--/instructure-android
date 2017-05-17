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
package com.instructure.pandautils.utils

import android.graphics.Color
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.utils.BooleanPref
import com.instructure.canvasapi2.utils.ColorPref
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.canvasapi2.utils.StringPref
import com.instructure.pandautils.R

object ThemePrefs : PrefManager("CanvasTheme") {

    const val DARK_MULTIPLIER = 0.85f
    const val ALPHA_VALUE = 0x32

    @JvmStatic
    var brandColor by ColorPref(R.color.canvas_default_primary)

    @JvmStatic
    var fontColor by ColorPref(R.color.canvas_default_primary)

    @JvmStatic
    var primaryColor by ColorPref(R.color.canvas_default_primary)

    @JvmStatic
    val darkPrimaryColor: Int
        get() = darker(primaryColor, DARK_MULTIPLIER)

    @JvmStatic
    var primaryTextColor by ColorPref(R.color.canvas_default_primary_text)

    @JvmStatic
    var accentColor by ColorPref(R.color.canvas_default_accent)

    @JvmStatic
    var buttonColor by ColorPref(R.color.canvas_default_button)

    @JvmStatic
    var buttonTextColor by ColorPref(R.color.canvas_default_button_text)

    @JvmStatic
    var logoUrl by StringPref()

    @JvmStatic
    var isThemeApplied by BooleanPref()

    override fun onClearPrefs() {
    }

    /**
     * Returns darker version of specified `color`.
     * StatusBar color example would be 0.85F
     */
    @JvmStatic
    @JvmOverloads
    fun darker(color: Int, factor: Float = DARK_MULTIPLIER): Int {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(
                a,
                Math.max((r * factor).toInt(), 0),
                Math.max((g * factor).toInt(), 0),
                Math.max((b * factor).toInt(), 0))
    }

    /**
     * Returns darker version of specified `color`.
     * StatusBar color example would be 0.85F
     */
    @JvmStatic
    @JvmOverloads
    fun increaseAlpha(color: Int, factor: Int = ALPHA_VALUE): Int {
        val a = factor
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, r, g, b)

    }

    @JvmStatic
    fun themeViewBackground(view: View, color: Int) {
        val viewTreeObserver = view.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val wrappedDrawable = DrawableCompat.wrap(view.background)
                if (wrappedDrawable != null) {
                    DrawableCompat.setTint(wrappedDrawable.mutate(), color)
                    view.background = wrappedDrawable
                }
            }
        })
    }

    @JvmStatic
    fun themeEditTextBackground(editText: EditText, color: Int) {
        editText.setTextColor(color)
        themeViewBackground(editText, color)
    }

    @JvmStatic
    fun applyCanvasTheme(theme: CanvasTheme) {
        brandColor = parseColor(theme.brand, brandColor)
        fontColor = parseColor(theme.fontColorDark, fontColor)
        primaryColor = parseColor(theme.primary, primaryColor)
        primaryTextColor = parseColor(theme.primaryText, primaryTextColor)
        accentColor = parseColor(theme.accent, accentColor)
        buttonColor = parseColor(theme.button, buttonColor)
        buttonTextColor = parseColor(theme.buttonText, buttonTextColor)
        logoUrl = theme.logoUrl ?: ""
        isThemeApplied = true
    }

    private fun parseColor(hexColor: String, defaultColor: Int): Int {
        try {
            return Color.parseColor("#${hexColor.trimMargin("#")}")
        } catch (e: IllegalArgumentException) {
            return defaultColor
        }
    }

}