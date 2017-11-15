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

package com.instructure.teacher.utils

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.*
import retrofit2.Call
import retrofit2.Response
import java.util.*

private const val PREFERENCE_FILE_NAME = "color_keeper_prefs"

@Suppress("unused")
object ColorKeeper : PrefManager(PREFERENCE_FILE_NAME) {

    /** The default color **/
    var defaultColor: Int = 0

    /** The currently cached colors **/
    var cachedColors: Map<String, Int> by NonNullGsonPref(HashMap())

    /** Whether or not colors have been synced from the API before **/
    var hasPreviouslySynced by BooleanPref()

    /** Gets the color associated with the given key if it exists, otherwise gets the [defaultColor] **/
    fun getColorOrDefault(keyName: String)
            = cachedColors[keyName] ?: defaultColor

    /** Gets the color associated with the given [CanvasContext] if it exists, otherwise generates a new color **/
    fun getOrGenerateColor(canvasContext: CanvasContext)
            = cachedColors.getOrElse(canvasContext.contextId) { generateColor(canvasContext) }

    /** Gets the color associated with the given contextId if it exists, otherwise generates a new color **/
    fun getOrGenerateColor(contextId: String)
            = cachedColors.getOrElse(contextId) { generateColor(Course()) }

    /** Adds all colors in the given [CanvasColor] object to the color cache **/
    fun addToCache(canvasColor: CanvasColor?) {
        canvasColor?.colors?.let { colors -> cachedColors += colors.mapValues { parseColor(it.value) } }
    }

    /** Associates a new color with a contextId **/
    fun addToCache(contextId: String, color: Int) {
        cachedColors += contextId to color
    }

    /**
     * Generates a colored drawable
     * @param context An Android Context
     * @param resource The resource ID of the drawable to be colored
     * @param color The color that will be used to tint the drawable
     * @return The colored drawable
     */
    fun getColoredDrawable(context: Context, @DrawableRes resource: Int, @ColorInt color: Int): Drawable
            = ContextCompat
            .getDrawable(context, resource)
            .mutate()
            .apply { setColorFilter(color, PorterDuff.Mode.SRC_ATOP) }

    /**
     * Generates a colored drawable
     * @param context An Android Context
     * @param resource The resource ID of the drawable to be colored
     * @param canvasContext A CanvasContext whose associated color will be used to tint the drawable
     * @return The colored drawable
     */
    fun getColoredDrawable(context: Context, @DrawableRes resource: Int, canvasContext: CanvasContext)
            = getColoredDrawable(context, resource, getOrGenerateColor(canvasContext))

    /**
     * Attempts to parse a color from a hex string.
     * @param hexColor A hex color string. May optionally be prefixed with '#'
     * @return The parsed color, or [defaultColor] if the string could not be parsed
     */
    private fun parseColor(hexColor: String): Int {
        try {
            return Color.parseColor("#${hexColor.trimMargin("#")}")
        } catch (e: IllegalArgumentException) {
            return defaultColor
        }
    }

    /**
     * Generates a generic color based on the canvas context id, this will produce consistent colors for a given course
     * @param canvasContext a valid canvas context
     * @return the generated colors
     */
    private fun generateColor(canvasContext: CanvasContext): Int {
        if (canvasContext.type == CanvasContext.Type.USER || canvasContext.name.isNullOrBlank()) {
            return defaultColor
        }

        val colorRes = when (Math.abs(canvasContext.name.hashCode() % 13)) {
            0 -> com.instructure.pandautils.R.color.courseOrange
            1 -> com.instructure.pandautils.R.color.courseBlue
            2 -> com.instructure.pandautils.R.color.courseGreen
            3 -> com.instructure.pandautils.R.color.coursePurple
            4 -> com.instructure.pandautils.R.color.courseGold
            5 -> com.instructure.pandautils.R.color.courseRed
            6 -> com.instructure.pandautils.R.color.courseChartreuse
            7 -> com.instructure.pandautils.R.color.courseCyan
            8 -> com.instructure.pandautils.R.color.courseSlate
            9 -> com.instructure.pandautils.R.color.coursePink
            10 -> com.instructure.pandautils.R.color.courseViolet
            11 -> com.instructure.pandautils.R.color.courseGrey
            12 -> com.instructure.pandautils.R.color.courseYellow
            13 -> com.instructure.pandautils.R.color.courseLavender
            else -> com.instructure.pandautils.R.color.courseHotPink
        }

        val color = ContextCompat.getColor(ContextKeeper.appContext, colorRes)
        addToCache(canvasContext.contextId, color)
        return color
    }

    override fun onClearPrefs() {}
}

@Suppress("unused", "DEPRECATION")
object ColorApiHelper {

    /**
     * Returns a color via a callback, if no color is in the cache it will pull from canvas via the api
     * If nothing is found in the cache or api a color is generated
     * @param canvasContext canvasContext
     * @param gotColor
     */
    fun getColor(canvasContext: CanvasContext, gotColor: (color: Int) -> Unit) {
        if (canvasContext.contextId in ColorKeeper.cachedColors) {
            gotColor(ColorKeeper.cachedColors[canvasContext.contextId]!!)
        } else {
            performSync { gotColor(ColorKeeper.getOrGenerateColor(canvasContext)) }
        }
    }

    /**
     * Sets a new color to the api and caches the result
     * @param canvasContext canvasContext
     * @param newColor the new color to set
     */
    fun setNewColor(canvasContext: CanvasContext, newColor: Int, onColorSet: (color: Int, success: Boolean) -> Unit) {
        UserManager.setColors(object : StatusCallback<CanvasColor>() {
            override fun onResponse(response: Response<CanvasColor>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type.isAPI) {
                    ColorKeeper.addToCache(canvasContext.contextId, newColor)
                    onColorSet(newColor, true)
                }
            }

            override fun onFail(response: Call<CanvasColor>?, error: Throwable?) = onColorSet(newColor, false)
        }, canvasContext.contextId, newColor)
    }

    /**
     * Attempts to pull and cache colors from the API.
     */
    fun performSync(onSynced: (success: Boolean) -> Unit) {
        UserManager.getColors(object : StatusCallback<CanvasColor>() {
            override fun onResponse(response: Response<CanvasColor>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type == ApiType.API) {
                    ColorKeeper.addToCache(response.body())
                    onSynced(true)
                }
            }

            override fun onFail(response: Call<CanvasColor>?, error: Throwable?) = onSynced(false)
        }, true)
    }
}