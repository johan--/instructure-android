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
