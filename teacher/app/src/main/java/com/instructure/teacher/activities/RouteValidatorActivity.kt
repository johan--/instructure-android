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
 *
 */
package com.instructure.teacher.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Window
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.teacher.R
import com.instructure.teacher.router.Route
import com.instructure.teacher.router.RouteMatcher

class RouteValidatorActivity : Activity() {

    internal var host = ""

    public override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_validator)

        val data: Uri? = intent.data
        val url: String? = data?.toString()

        if (data == null || url.isNullOrBlank()) {
            finish()
            return
        }

        host = data.host // "mobiledev.instructure.com"

        val isSignedIn = ApiPrefs.token.isNotEmpty()
        val domain = ApiPrefs.domain

        if (!isSignedIn) {
            startActivity(InitLoginActivity.createIntent(this))
            finish()
        } else if (host !in domain) {
            val intent = InitLoginActivity.createLaunchApplicationMainActivityIntent(this)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            //Allow the UI to show.
            Handler().postDelayed({
                RouteMatcher.routeUrl(this@RouteValidatorActivity, url, domain, Route.RouteContext.EXTERNAL)
                finish()
            }, 1000)
        }
    }

    companion object {

        fun createIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, RouteValidatorActivity::class.java)
            intent.data = uri
            return intent
        }
    }
}