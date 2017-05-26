package com.instructure.pandautils.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.DimenRes
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatSpinner
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ScaleXSpan
import android.view.View
import com.instructure.pandautils.R

object ViewStyler {

    @JvmStatic
    fun setToolbarElevationSmall(context: Context, toolbar: Toolbar) {
        ViewCompat.setElevation(toolbar, context.resources.getDimension(R.dimen.utils_toolbar_elevation_small))
    }

    @JvmStatic
    fun setToolbarElevation(context: Context, toolbar: Toolbar) {
        ViewCompat.setElevation(toolbar, context.resources.getDimension(R.dimen.utils_toolbar_elevation))
    }

    @JvmStatic
    fun setToolbarElevation(context: Context, toolbar: Toolbar, @DimenRes elevation: Int) {
        ViewCompat.setElevation(toolbar, context.resources.getDimension(elevation))
    }

    @JvmStatic
    fun colorToolbarIconsAndText(activity: Activity, toolbar: Toolbar, @ColorInt color: Int) {
        toolbar.setTitleTextAppearance(activity, R.style.ToolbarStyle)
        toolbar.setSubtitleTextAppearance(activity, R.style.ToolbarStyle_Subtitle)
        ToolbarColorizeHelper.colorizeToolbar(toolbar, color, activity)
    }

    @JvmStatic
    fun themeEditText(context: Context, editText: AppCompatEditText, @ColorInt brand: Int) {
        val defaultColor = ContextCompat.getColor(context, R.color.utils_editTextColor)
        editText.supportBackgroundTintList = makeColorStateList(defaultColor, brand)
        editText.highlightColor = ThemePrefs.increaseAlpha(brand)
    }

    @JvmStatic
    fun themeSpinner(context: Context, spinner: AppCompatSpinner, @ColorInt brand: Int) {
        val defaultColor = ContextCompat.getColor(context, R.color.utils_editTextColor)
        spinner.supportBackgroundTintList = makeColorStateList(defaultColor, brand)
    }

    @JvmStatic
    fun themeInputTextLayout(textInputLayout: TextInputLayout, @ColorInt color: Int) {
        try {
            val fDefaultTextColor = TextInputLayout::class.java.getDeclaredField("mDefaultTextColor")
            fDefaultTextColor.isAccessible = true
            fDefaultTextColor.set(textInputLayout, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(color)))

            val fFocusedTextColor = TextInputLayout::class.java.getDeclaredField("mFocusedTextColor")
            fFocusedTextColor.isAccessible = true
            fFocusedTextColor.set(textInputLayout, ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(color)))
        } catch (e: Exception) { }
    }

    @JvmStatic
    fun themeToolbar(activity: Activity, toolbar: Toolbar, @ColorInt backgroundColor: Int, @ColorInt contentColor: Int) {
        themeToolbar(activity, toolbar, backgroundColor, contentColor, true)
    }

    @JvmStatic
    fun themeToolbar(activity: Activity, toolbar: Toolbar, @ColorInt backgroundColor: Int, @ColorInt contentColor: Int, darkStatusBar: Boolean) {
        toolbar.setBackgroundColor(backgroundColor)
        toolbar.setTitleTextAppearance(activity, R.style.ToolbarStyle)
        toolbar.setSubtitleTextAppearance(activity, R.style.ToolbarStyle_Subtitle)
        colorToolbarIconsAndText(activity, toolbar, contentColor)
        if(darkStatusBar) {
            setStatusBarDark(activity, backgroundColor)
        } else {
            setStatusBarLight(activity)
        }
    }

    @JvmStatic
    fun themeToolbarBottomSheet(activity: Activity, isTablet: Boolean, toolbar: Toolbar, @ColorInt contentColor: Int, darkStatusBar: Boolean) {
        toolbar.setBackgroundColor(Color.WHITE)
        toolbar.setTitleTextAppearance(activity, R.style.ToolbarStyle)
        toolbar.setSubtitleTextAppearance(activity, R.style.ToolbarStyle_Subtitle)
        colorToolbarIconsAndText(activity, toolbar, contentColor)
        if(!isTablet) {
            if (darkStatusBar) {
                setStatusBarDark(activity, Color.WHITE)
            } else {
                setStatusBarLight(activity)
            }
        }
    }

    @JvmStatic
    fun themeCheckBox(context: Context, checkBox: AppCompatCheckBox, @ColorInt brand: Int) {
        val defaultColor = ContextCompat.getColor(context, R.color.utils_editTextColor)
        checkBox.supportButtonTintList = makeColorStateList(defaultColor, brand)
        checkBox.highlightColor = ThemePrefs.increaseAlpha(defaultColor)
    }

            @JvmStatic
    fun setStatusBarDark(activity: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.statusBarColor = ThemePrefs.darker(color)
            var flags = activity.window.decorView.systemUiVisibility
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            activity.window.decorView.systemUiVisibility = flags
        }
    }

    @JvmStatic
    fun setStatusBarLight(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.dim_lighter_gray)
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun makeColorStateList(defaultColor: Int, brand: Int) = generateColorStateList(
            intArrayOf(-android.R.attr.state_enabled) to defaultColor,
            intArrayOf(android.R.attr.state_focused, -android.R.attr.state_pressed) to brand,
            intArrayOf(android.R.attr.state_focused, android.R.attr.state_pressed) to brand,
            intArrayOf(-android.R.attr.state_focused, android.R.attr.state_pressed) to brand,
            intArrayOf(android.R.attr.state_checked) to brand,
            intArrayOf() to defaultColor
    )

    @JvmStatic
    fun makeColorStateListForButton() = generateColorStateList(
            intArrayOf() to ThemePrefs.buttonColor
    )

    @JvmStatic
    fun makeColorStateListForRadioGroup(uncheckedColor: Int, checkedColor: Int) = generateColorStateList(
            intArrayOf(-android.R.attr.state_checked) to uncheckedColor,
            intArrayOf(android.R.attr.state_checked) to checkedColor,
            intArrayOf() to uncheckedColor
    )

    @JvmStatic
    fun generateColorStateList(vararg stateColors: Pair<IntArray, Int>) = ColorStateList(
            Array(stateColors.size) { stateColors[it].first },
            Array(stateColors.size) { stateColors[it].second }.toIntArray()
    )

    @JvmStatic
    fun applyKerning(src: CharSequence?, kerning: Float): Spannable? {
        if (src == null) return null
        val srcLength = src.length
        if (srcLength < 2)
            return src as? Spannable ?: SpannableString(src)

        val nonBreakingSpace = "\u00A0"
        val builder = src as? SpannableStringBuilder ?: SpannableStringBuilder(src)
        for (i in src.length - 1 downTo 1) {
            builder.insert(i, nonBreakingSpace)
            builder.setSpan(ScaleXSpan(kerning), i, i + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return builder
    }
}
