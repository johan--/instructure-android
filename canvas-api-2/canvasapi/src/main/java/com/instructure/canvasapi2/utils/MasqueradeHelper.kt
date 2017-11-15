/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.canvasapi2.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.User
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.IOException

object MasqueradeHelper {

    @JvmStatic
    @JvmOverloads
    fun <ACTIVITY : Activity> stopMasquerading(startingClass: Class<ACTIVITY>? = null) {
        ApiPrefs.isMasquerading = false
        ApiPrefs.masqueradeId = -1L
        ApiPrefs.domain = ApiPrefs.originalDomain
        ApiPrefs.masqueradeDomain = ""
        ApiPrefs.masqueradeUser = null
        cleanupMasquerading(ContextKeeper.appContext)
        if (startingClass != null) restartApplication(startingClass)
    }

    @JvmStatic
    fun <ACTIVITY : Activity> startMasquerading(masqueradingUserId: Long, masqueradingDomain: String?, startingClass: Class<ACTIVITY>) {
        //Check to see if they're trying to switch domain as site admin
        if (!masqueradingDomain.isNullOrBlank()) {
            // If we don't set isMasquerading to true here the original domain will be set to the masquerading domain, even if trying to
            // masquerade fails
            ApiPrefs.isMasquerading = true
            ApiPrefs.masqueradeId = masqueradingUserId
            // because isMasquerading is set to true this will also set the masqueradingDomain
            ApiPrefs.domain = masqueradingDomain!!
        }

        try {
            UserManager.getUser(masqueradingUserId, object : StatusCallback<User>() {
                override fun onResponse(response: Response<User>?, linkHeaders: LinkHeaders?, type: ApiType?) {
                    if (response?.body() != null) {
                        cleanupMasquerading(ContextKeeper.appContext)
                        ApiPrefs.masqueradeUser = response.body()
                        ApiPrefs.domain = ApiPrefs.originalDomain
                        restartApplication(startingClass)
                    }
                }

                override fun onFail(callResponse: Call<User>?, error: Throwable?, response: Response<*>?) {
                    stopMasquerading(startingClass)
                }
            }, true)
        } catch (e: Exception) {
            Logger.e("Error masquerading: " + e)
            stopMasquerading(startingClass)
        }
    }

    private fun <ACTIVITY : Activity> restartApplication(startingClass: Class<ACTIVITY>) {
        //totally restart the app so the masquerading will apply
        val initActivity = Intent(ContextKeeper.appContext, startingClass)
        val pendingIntent = PendingIntent.getActivity(ContextKeeper.appContext, 6660, initActivity, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = ContextKeeper.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 200, pendingIntent)
        System.exit(0)
    }

    /** Appends the masquerade ID to the provided URL (if currently masquerading) */
    @JvmStatic
    fun addMasqueradeId(url: String): String {
        if (!ApiPrefs.isMasquerading) return url
        val queryChar = if ('?' in url) '&' else '?'
        return "$url${queryChar}as_user_id=${ApiPrefs.masqueradeId}"
    }

    @Suppress("DEPRECATION")
    private fun cleanupMasquerading(context: Context) {
        CookieSyncManager.createInstance(ContextKeeper.appContext)
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookie()

        val client = CanvasRestAdapter.getClient()
        if (client != null) {
            try {
                client.cache().evictAll()
            } catch (e: IOException) {/* Do Nothing */ }
        }

        val file: File
        if (context.externalCacheDir != null) {
            file = File(context.externalCacheDir, "attachments")
        } else {
            file = context.filesDir
        }

        FileUtils.deleteAllFilesInDirectory(file)
        RestBuilder.clearCacheDirectory()
    }
}
