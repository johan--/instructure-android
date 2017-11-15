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
package com.instructure.teacher.fragments

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Html
import android.text.SpannedString
import android.text.TextUtils
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.HttpHelper
import com.instructure.teacher.R
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.StringArg

import org.json.JSONObject

class LTIWebViewFragment : InternalWebViewFragment() {

    var ltiUrl: String by StringArg()
    var ltiTab: Tab? by NullableParcelableArg()
    var sessionLessLaunch: Boolean by BooleanArg()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShouldRouteInternally(false)
        title = if(title.isNotBlank()) title else ltiUrl
    }

    override fun onResume() {
        super.onResume()
        try {
            if (ltiTab == null) {
                if (url.isNotBlank()) {
                    //modify the url
                    if (url.startsWith("canvas-courses://")) {
                        url = url.replaceFirst("canvas-courses".toRegex(), ApiPrefs.protocol)
                    }
                    val uri = Uri.parse(url).buildUpon()
                            .appendQueryParameter("display", "borderless")
                            .appendQueryParameter("platform", "android")
                            .build()
                    if (sessionLessLaunch) {
                        val sessionless_launch = ApiPrefs.fullDomain +
                                "/api/v1/accounts/self/external_tools/sessionless_launch?url=" + url
                        GetSessionlessLtiURL().execute(sessionless_launch)
                    } else {
                        loadUrl(uri.toString())
                    }
                } else if(ltiUrl.isNotBlank()) {
                    GetSessionlessLtiURL().execute(ltiUrl)
                } else {
                    val spannedString = SpannedString(getString(R.string.errorOccurred))
                    loadHtml(Html.toHtml(spannedString))
                }
            } else {
                GetLtiURL().execute(ltiTab)
            }
        } catch (e: Exception) {
            //if it gets here we're in trouble and won't know what the tab is, so just display an error message
            val spannedString = SpannedString(getString(R.string.errorOccurred))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                loadHtml(Html.toHtml(spannedString, Html.FROM_HTML_MODE_LEGACY))
            } else {
                loadHtml(Html.toHtml(spannedString))
            }
        }
    }

    private inner class GetLtiURL : AsyncTask<Tab, Void, String?>() {

        override fun doInBackground(vararg params: Tab): String? {
            return getLTIUrlForTab(context, params[0])
        }

        override fun onPostExecute(result: String?) {
            if (activity == null || result == null) {
                return
            }

            //make sure we have a non null url before we add parameters
            if (!TextUtils.isEmpty(result)) {
                val uri = Uri.parse(result).buildUpon()
                        .appendQueryParameter("display", "borderless")
                        .appendQueryParameter("platform", "android")
                        .build()
                loadUrl(uri.toString())
            } else {
                loadUrl(result)
            }
        }
    }

    private inner class GetSessionlessLtiURL : AsyncTask<String, Void, String?>() {

        override fun doInBackground(vararg params: String): String? {
            return getLTIUrl(context, params[0])
        }

        override fun onPostExecute(result: String?) {
            if (activity == null || result == null) {
                return
            }

            //make sure we have a non null url before we add parameters
            if (!TextUtils.isEmpty(result)) {
                val uri = Uri.parse(result).buildUpon()
                        .appendQueryParameter("display", "borderless")
                        .appendQueryParameter("platform", "android")
                        .build()
                loadUrl(uri.toString())
            } else {
                loadUrl(result)
            }
        }
    }

    private fun getLTIUrlForTab(context: Context, tab: Tab): String? {
        return getLTIUrl(context, tab.ltiUrl)
    }

    private fun getLTIUrl(context: Context, url: String): String? {
        try {
            val result = HttpHelper.externalHttpGet(context, url, true).responseBody
            var ltiUrl: String? = null
            if (result != null) {
                val ltiJSON = JSONObject(result)
                ltiUrl = ltiJSON.getString("url")
            }
            return ltiUrl
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        const val LTI_URL = "lti_url"
        const val TAB = "tab"
        const val SESSION_LESS = "session_less"

        @JvmStatic
        fun makeLTIBundle(ltiUrl: String): Bundle {
            val args = Bundle()
            args.putString(LTI_URL, ltiUrl)
            return args
        }

        @JvmStatic
        fun makeLTIBundle(ltiTab: Tab): Bundle {
            val args = Bundle()
            args.putParcelable(TAB, ltiTab)
            return args
        }

        @JvmStatic
        fun makeLTIBundle(ltiUrl: String, title: String, sessionLessLaunch: Boolean): Bundle {
            val args = Bundle()
            args.putString(LTI_URL, ltiUrl)
            args.putBoolean(SESSION_LESS, sessionLessLaunch)
            args.putString(TITLE, title)
            return args
        }

        @JvmStatic
        fun newInstance(args: Bundle) = LTIWebViewFragment().apply {
            ltiUrl = args.getString(LTI_URL, "")
            title = args.getString(TITLE, "")
            sessionLessLaunch = args.getBoolean(SESSION_LESS, false)
            if(args.containsKey(TAB)) {
                ltiTab = args.getParcelable(TAB)
            }
            setShouldLoadUrl(false)
        }
    }
}
