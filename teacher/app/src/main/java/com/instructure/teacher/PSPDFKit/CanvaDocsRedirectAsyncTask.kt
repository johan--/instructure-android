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

import android.os.AsyncTask
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.utils.ApiPrefs
import okhttp3.Request

/**
 * Given an appropriate canvadocs url (typically the preview_url from an attachment), this async
 * task will attempt return a redirect url which can be used to start a canvadocs session
 */
class CanvaDocsRedirectAsyncTask(private val mCanvaDocsUrl: String,
                                 private val mCallback: (String) -> Unit) : AsyncTask<Unit, Unit, String>() {

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun doInBackground(vararg params: Unit?): String {
        val client = CanvasRestAdapter.getOkHttpClient()
                .newBuilder()
                .followRedirects(false)
                .cache(null)
                .build()

        val request = Request.Builder()
                .url(ApiPrefs.fullDomain + mCanvaDocsUrl)
                .build()

        val response = client.newCall(request).execute()

        val redirectUrl: String
        if(response.isRedirect) {
            redirectUrl = response.header("Location")

            //lets parse out what we don't want
            return redirectUrl.substringBefore("/view")
        } else return ""
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        mCallback(result)
    }
}