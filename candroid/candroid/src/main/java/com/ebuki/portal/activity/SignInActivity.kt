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
package com.instructure.candroid.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import com.instructure.candroid.service.PushRegistrationService
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.loginapi.login.activities.BaseLoginSignInActivity

class SignInActivity : BaseLoginSignInActivity() {

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

    override fun userAgent(): String {
        return "candroid"
    }

    override fun refreshWidgets() {
        //TODO:
    }

    companion object {
        fun createIntent(context: Context, accountDomain: AccountDomain): Intent {
            val intent = Intent(context, SignInActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(BaseLoginSignInActivity.ACCOUNT_DOMAIN, accountDomain)
            intent.putExtras(extras)
            return intent
        }
    }
}
