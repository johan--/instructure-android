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

package com.instructure.pandautils.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.instructure.canvasapi2.CanvasRestAdapter;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;

import static android.content.Context.MODE_PRIVATE;

public abstract class AppResetter {

    protected abstract void beforeReset(Context context, boolean isTesting);

    protected abstract void onReset(Context context, boolean isTesting);

    public void performReset(Context context, boolean isTesting) {
        beforeReset(context, isTesting);
        clearPrefs(context);
        clearFiles(context);
        clearHttpClientCache();
        onReset(context, isTesting);
    }

    private void clearPrefs(Context context) {
        //Clear all Shared Preferences.
        ApiPrefs.clearAllData();
    }

    private void clearFiles(Context context) {
        File cacheDir = new File(context.getFilesDir(), "cache");
        File exCacheDir = Utils.getAttachmentsDirectory(context);
        //remove the cached stuff for masqueraded user
        File masqueradeCacheDir = new File(context.getFilesDir(), "cache_masquerade");
        //need to delete the contents of the external cache folder so previous user's results don't show up on incorrect user
        FileUtils.deleteAllFilesInDirectory(masqueradeCacheDir);
        FileUtils.deleteAllFilesInDirectory(cacheDir);
        FileUtils.deleteAllFilesInDirectory(exCacheDir);
    }

    private void clearHttpClientCache() {
        OkHttpClient client = CanvasRestAdapter.getClient();
        if(client != null) {
            try {
                client.cache().evictAll();
            } catch (IOException e) {
                //Do Nothing
            }
        }
        RestBuilder.clearCacheDirectory();
    }

    protected SharedPreferences.Editor getEditor(Context context, String prefName) {
        SharedPreferences preferences = getSharedPreferences(context, prefName);
        return preferences.edit();
    }

    protected SharedPreferences getSharedPreferences(Context context, String prefName) {
        return context.getSharedPreferences(prefName, MODE_PRIVATE);
    }
}
