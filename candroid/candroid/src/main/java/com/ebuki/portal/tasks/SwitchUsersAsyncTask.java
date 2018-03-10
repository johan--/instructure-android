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
package com.instructure.candroid.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.instructure.candroid.activity.LoginActivity;
import com.instructure.candroid.fragment.ApplicationSettingsFragment;
import com.instructure.candroid.view.CanvasRecipientManager;
import com.instructure.candroid.widget.CanvasWidgetProvider;
import com.instructure.canvasapi2.CanvasRestAdapter;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ContextKeeper;
import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.canvasapi2.utils.MasqueradeHelper;
import com.instructure.loginapi.login.tasks.SwitchUsersTask;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.pandautils.utils.TutorialUtils;
import com.instructure.pandautils.utils.Utils;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;

import static android.content.Context.MODE_PRIVATE;
import static com.instructure.candroid.util.ApplicationManager.PREF_NAME;

public class SwitchUsersAsyncTask extends SwitchUsersTask {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        CanvasRecipientManager.getInstance(ContextKeeper.getAppContext()).clearCache();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void clearCookies() {
        CookieSyncManager.createInstance(ContextKeeper.appContext);
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
        safeClear(ContextKeeper.appContext);
    }

    @Override
    protected void cleanupMasquerading() {
        MasqueradeHelper.stopMasquerading();
        //remove the cached stuff for masqueraded user
        File masqueradeCacheDir = new File(ContextKeeper.getAppContext().getFilesDir(), "cache_masquerade");
        //need to delete the contents of the internal cache folder so previous user's results don't show up on incorrect user
        FileUtils.deleteAllFilesInDirectory(masqueradeCacheDir);
    }

    @Override
    protected void refreshWidgets() {
        ContextKeeper.getAppContext().sendBroadcast(new Intent(CanvasWidgetProvider.REFRESH_ALL));
    }

    @Override
    protected void clearTheme() {
        ThemePrefs.INSTANCE.clearPrefs();
    }

    @Override
    protected void startLoginFlow() {
        Intent intent = LoginActivity.Companion.createIntent(ContextKeeper.appContext);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ContextKeeper.appContext.startActivity(intent);
    }

    private void safeClear(Context context) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        //Don't make them redo tutorials
        boolean doneStreamTutorial = settings.getBoolean("stream_tutorial_v2", false);
        boolean doneConversationListTutorial = settings.getBoolean("conversation_list_tutorial_v2", false);
        boolean featureSlides = settings.getBoolean("feature_slides_shown", false);
        String lastDomain = settings.getString("last-domain", "");
        String UUID = settings.getString("APID", null);
        int landingPage = settings.getInt(ApplicationSettingsFragment.LANDING_PAGE, 0);
        boolean drawerLearned = settings.getBoolean(Const.PREF_USER_LEARNED_DRAWER, false);
        boolean tutorialViewed = settings.getBoolean(Const.TUTORIAL_VIEWED, false);
        boolean newGroupsViewed = settings.getBoolean(Const.VIEWED_NEW_FEATURE_BANNER, false);
        boolean showGrades = settings.getBoolean(Const.SHOW_GRADES_ON_CARD, true);
        boolean pandasFlying = settings.getBoolean(Const.FUN_MODE, false);

        boolean tutorial_1 = TutorialUtils.hasBeenViewed(getPrefs(context), TutorialUtils.TYPE.STAR_A_COURSE);
        boolean tutorial_2 = TutorialUtils.hasBeenViewed(getPrefs(context), TutorialUtils.TYPE.COLOR_CHANGING_DIALOG);
        boolean tutorial_3 = TutorialUtils.hasBeenViewed(getPrefs(context), TutorialUtils.TYPE.LANDING_PAGE);
        boolean tutorial_5 = TutorialUtils.hasBeenViewed(getPrefs(context), TutorialUtils.TYPE.MY_COURSES);
        boolean tutorial_6 = TutorialUtils.hasBeenViewed(getPrefs(context), TutorialUtils.TYPE.NOTIFICATION_PREFERENCES);
        boolean tutorial_9 = TutorialUtils.hasBeenViewed(getPrefs(context), TutorialUtils.TYPE.NAVIGATION_SHORTCUTS);
        boolean tutorial_10 = TutorialUtils.hasBeenViewed(getPrefs(context), TutorialUtils.TYPE.COURSE_GRADES);

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        ApiPrefs.clearAllData();
        File exCacheDir = Utils.getAttachmentsDirectory(ContextKeeper.getAppContext());
        File cacheDir = new File(ContextKeeper.getAppContext().getFilesDir(), "cache");
        //need to delete the contents of the internal/external cache folder so previous user's results don't show up on incorrect user
        FileUtils.deleteAllFilesInDirectory(cacheDir);
        FileUtils.deleteAllFilesInDirectory(exCacheDir);

        //Replace the information about tutorials/last domain
        editor.putBoolean("stream_tutorial_v2", doneStreamTutorial);
        editor.putBoolean("conversation_list_tutorial_v2", doneConversationListTutorial);
        editor.putBoolean("feature_slides_shown", featureSlides);
        editor.putString("last-domain", lastDomain);
        editor.putInt(ApplicationSettingsFragment.LANDING_PAGE, landingPage);
        editor.putBoolean(Const.PREF_USER_LEARNED_DRAWER, drawerLearned);
        editor.putBoolean(Const.TUTORIAL_VIEWED, tutorialViewed);
        editor.putBoolean(Const.VIEWED_NEW_FEATURE_BANNER, newGroupsViewed);
        editor.putBoolean(Const.SHOW_GRADES_ON_CARD, showGrades);
        editor.putBoolean(Const.FUN_MODE, pandasFlying);

        TutorialUtils.setHasBeenViewed(getPrefs(context), TutorialUtils.TYPE.STAR_A_COURSE, tutorial_1);
        TutorialUtils.setHasBeenViewed(getPrefs(context), TutorialUtils.TYPE.COLOR_CHANGING_DIALOG, tutorial_2);
        TutorialUtils.setHasBeenViewed(getPrefs(context), TutorialUtils.TYPE.LANDING_PAGE, tutorial_3);
        TutorialUtils.setHasBeenViewed(getPrefs(context), TutorialUtils.TYPE.MY_COURSES, tutorial_5);
        TutorialUtils.setHasBeenViewed(getPrefs(context), TutorialUtils.TYPE.NOTIFICATION_PREFERENCES, tutorial_6);
        TutorialUtils.setHasBeenViewed(getPrefs(context), TutorialUtils.TYPE.NAVIGATION_SHORTCUTS, tutorial_9);
        TutorialUtils.setHasBeenViewed(getPrefs(context), TutorialUtils.TYPE.COURSE_GRADES, tutorial_10);


        if (UUID != null) {
            editor.putString("APID", UUID);
        }

        editor.apply();
    }

    public static Prefs getPrefs(Context context){
        return new Prefs(context, PREF_NAME);
    }
}
