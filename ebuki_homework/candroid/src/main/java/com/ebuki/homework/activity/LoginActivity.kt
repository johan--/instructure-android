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
package com.ebuki.homework.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.webkit.CookieManager
import com.ebuki.homework.R
import com.ebuki.homework.service.PushRegistrationService
import com.instructure.loginapi.login.activities.BaseLoginInitActivity

class LoginActivity : BaseLoginInitActivity() {

    override fun launchApplicationMainActivityIntent(): Intent {
        if (!PushRegistrationService.hasTokenBeenSentToServer(this)) {
            startService(Intent(this, PushRegistrationService::class.java))//Register Push Notifications
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush()
        }

        val intent = Intent(this, NavigationActivity.getStartActivityClass())
        if (getIntent() != null && getIntent().extras != null) {
            intent.putExtras(getIntent().extras)
        }
        return intent
    }

    override fun beginLoginFlowIntent(): Intent {
        return LoginLandingPageActivity.createIntent(this);
    }

    override fun themeColor(): Int {
        return ContextCompat.getColor(this, R.color.login_studentAppTheme)
    }

    override fun userAgent(): String {
        return "candroid"
    }

    override fun finish() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.finish()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }
    }
}
