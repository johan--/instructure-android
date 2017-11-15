/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.parentapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.crashlytics.android.Crashlytics;
import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.parentapp.BuildConfig;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;

public class ApplicationManager extends AppManager {
    public final static String PREF_NAME = "android_parent_SP";
    public final static String PREF_FILE_NAME = "android_parent_SP";
    public final static String MULTI_SIGN_IN_PREF_NAME = "multipleSignInAndroidParentSP";
    public final static String OTHER_SIGNED_IN_USERS_PREF_NAME = "otherSignedInUsersAndroidParentSP";
    public final static String PREF_NAME_PREVIOUS_DOMAINS = "android_parent_SP_previous_domains";


    @Override
    public void onCreate() {
        // Set preferences to create a pre-logged-in state. This should only be used for the 'robo' app flavor.
        if (BuildConfig.IS_ROBO_TEST) RoboTesting.setAppStatePrefs(this);

        super.onCreate();


        Fabric.with(this, new Crashlytics());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // there appears to be a bug when the user is installing/updating the android webview stuff.
            // http://code.google.com/p/android/issues/detail?id=175124
            try {
                WebView.setWebContentsDebuggingEnabled(true);
            } catch (Exception e) {
                Log.d("ParentApp", "Exception trying to setWebContentsDebuggingEnabled");
            }
        }
        SharedPreferences pref = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        //If we don't have one, generate one.
        if (!pref.contains("APID")) {
            String uuid = UUID.randomUUID().toString();

            SharedPreferences.Editor editor = pref.edit();
            editor.putString("APID", uuid);
            editor.apply();
        }
    }

    /**
     * Log out the currently signed in user. Permanently remove credential information.
     *
     * @return
     */
    public boolean logoutUser() {

        final String airwolfDomain = ApiPrefs.getAirwolfDomain();

        //Get the Shared Preferences
        SharedPreferences settings = getSharedPreferences(ApplicationManager.PREF_NAME, MODE_PRIVATE);

        //delete them all
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        //Clear all Shared Preferences.
        ApiPrefs.clearAllData();

        ApiPrefs.setAirwolfDomain(BuildConfig.IS_TESTING ? BuildConfig.GAMMA_DOMAIN : airwolfDomain);

        return true;
    }

    public static String getParentId(Context context) {
        Prefs prefs = new Prefs(context, com.instructure.parentapp.util.Const.CANVAS_PARENT_SP);
        return prefs.load(Const.ID, "");
    }
}
