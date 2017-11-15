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
 *
 */

package com.instructure.androidpolling.app.asynctasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.activities.InitLoginActivity;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.canvasapi2.CanvasRestAdapter;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.managers.OAuthManager;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ContextKeeper;
import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.loginapi.login.tasks.LogoutTask;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;

import static com.instructure.androidpolling.app.util.ApplicationManager.PREF_FILE_NAME;

public class LogoutAsyncTask extends LogoutTask {

    @Override
    protected void onLogoutFailed() {
        Toast.makeText(ContextKeeper.getAppContext(), R.string.no_data_connection, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void clearCookies() {
        CookieSyncManager.createInstance(ContextKeeper.getAppContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    protected void clearCache() {
        OkHttpClient client = CanvasRestAdapter.getClient();
        if(client != null) {
            try {
                client.cache().evictAll();
            } catch (IOException e) {/* Do Nothing */}
        }

        RestBuilder.clearCacheDirectory();

        //Clear shared preferences,
        //Get the Shared Preferences
        SharedPreferences settings = ContextKeeper.getAppContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        //Clear all Shared Preferences.
        ApiPrefs.clearAllData();

        File cacheDir = new File(ContextKeeper.getAppContext().getFilesDir(), "cache");
        File exCacheDir = ApplicationManager.getAttachmentsDirectory(ContextKeeper.getAppContext());
        FileUtils.deleteAllFilesInDirectory(cacheDir);
        FileUtils.deleteAllFilesInDirectory(exCacheDir);
    }

    @Override
    protected void cleanupMasquerading() {
        //remove the cached stuff for masqueraded user
        File masqueradeCacheDir = new File(ContextKeeper.getAppContext().getFilesDir(), "cache_masquerade");
        //need to delete the contents of the internal cache folder so previous user's results don't show up on incorrect user
        FileUtils.deleteAllFilesInDirectory(masqueradeCacheDir);
    }

    @Override
    protected boolean logout() {
        //It is possible for multiple APIs to come back 'simultaneously' as HTTP401s causing a logout
        //if this has already ran, data is already cleared causing null pointer exceptions
        if(APIHelper.hasNetworkConnection()) {
            String token = ApiPrefs.getToken();
            if (!token.equals("")) {
                OAuthManager.deleteToken();
                return true;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void refreshWidgets() {
        //do nothing
    }

    @Override
    protected void clearTheme() {
        //do nothing
    }

    @Override
    protected void startLoginFlow() {
        Intent intent = InitLoginActivity.createIntent(ContextKeeper.appContext);
        ContextKeeper.appContext.startActivity(intent);
    }
}