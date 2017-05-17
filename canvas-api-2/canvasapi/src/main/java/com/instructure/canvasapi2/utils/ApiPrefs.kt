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

import android.content.Context
import android.webkit.URLUtil
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import java.io.File

/** Preference file name **/
const val PREFERENCE_FILE_NAME = "canvas-kit-sp"

/**
 * Canvas API preferences containing data required for most core networking such as
 * the school domain, protocol, auth token, and user object.
 *
 * All public properties of ApiPrefs should be cached in memory (after any initial loading), so it is
 * safe to access these properties in hot code paths like View.OnDraw() and RecyclerView binders.
 */
@Suppress("unused", "UNUSED_PARAMETER")
object ApiPrefs : PrefManager(PREFERENCE_FILE_NAME) {

    @JvmStatic
    var token by StringPref()

    @JvmStatic
    var protocol by StringPref("https", "api_protocol")

    @JvmStatic
    var userAgent by StringPref("", "user_agent")

    @JvmStatic
    var restParams: RestParams = RestParams.Builder().build()

    @JvmStatic
    var perPageCount = 100

    @JvmStatic
    var theme: CanvasTheme? by GsonPref(CanvasTheme::class.java, null)

    /* Non-masquerading Prefs */
    private var originalDomain by StringPref("", "domain")
    private var originalUser: User? by GsonPref(User::class.java, null, "user")

    /* Masquerading Prefs */
    @JvmStatic
    var isMasquerading by BooleanPref()
    @JvmStatic
    var masqueradeId by LongPref(-1L)
    private var masqueradeDomain by StringPref()
    private var masqueradeUser: User? by GsonPref(User::class.java, null, "masq-user")

    @JvmStatic
    var domain: String
        get() = if (isMasquerading) masqueradeDomain else originalDomain
        set(newDomain) {
            val strippedDomain = newDomain.replaceFirst(Regex("https?://"), "").removeSuffix("/")
            if (isMasquerading) masqueradeDomain = strippedDomain else originalDomain = strippedDomain
        }

    @JvmStatic
    val fullDomain: String
        get() = when {
            domain.isBlank() || protocol.isBlank() -> ""
            URLUtil.isHttpUrl(domain) || URLUtil.isHttpsUrl(domain) -> domain
            else -> "$protocol://$domain"
        }

    @JvmStatic
    var user: User?
        get() = if (isMasquerading) masqueradeUser else originalUser
        set(newUser) {
            if (isMasquerading) masqueradeUser = newUser else originalUser = newUser
        }

    override fun onClearPrefs() {}

    @JvmStatic
    fun stopMasquerading(context: Context) {
        isMasquerading = false
        masqueradeId = -1L

        // TODO for masquerade
        //val cacheDir = File(context.filesDir, "cache")
        //FileUtilities.deleteAllFilesInDirectory(cacheDir)
        //CanvasRestAdapter.deleteCache()
    }

    @JvmStatic
    fun startMasquerading(masqId: Long, masqueradeUser: StatusCallback<User>, domain: String?) {
        isMasquerading = true
        masqueradeId = masqId

        //Check to see if they're trying to switch domain as site admin
        if (!domain.isNullOrBlank()) this.domain = domain!!

        // TODO for masquerade
        //val cacheDir = File(context.filesDir, "cache")
        //FileUtilities.deleteAllFilesInDirectory(cacheDir)
        //CanvasRestAdapter.deleteHttpCache()
        //UserAPI.getUserByIdNoCache(masqueradeId, masqueradeUser)
    }

    /** Appends the masquerade ID to the provided URL (if currently masquerading) */
    @JvmStatic
    fun addMasqueradeId(url: String): String {
        if (!isMasquerading) return url
        val queryChar = if ('?' in url) '&' else '?'
        return "$url${queryChar}as_user_id=$masqueradeId"
    }


    /**
     * clearAllData is required for logout.
     * Clears all data including credentials and cache.
     * @return true if caches files were deleted
     */
    @JvmStatic
    fun clearAllData(): Boolean {
        // Clear preferences
        clearPrefs()

        // Clear http cache
        RestBuilder.clearCacheDirectory()

        // Clear file cache
        val cacheDir = File(ContextKeeper.appContext.filesDir, FileUtils.FILE_DIRECTORY)
        return FileUtils.deleteAllFilesInDirectory(cacheDir)
    }

}