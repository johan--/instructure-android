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
package com.instructure.loginapi.login.tasks;

import android.os.AsyncTask;

public abstract class SwitchUsersTask extends AsyncTask<Void, Void, Boolean> {

    protected abstract void clearCookies();
    protected abstract void clearCache();
    protected abstract void cleanupMasquerading();
    protected abstract void refreshWidgets();
    protected abstract void clearTheme();
    protected abstract void startLoginFlow();

    @Override
    protected Boolean doInBackground(Void... params) {
        cleanupMasquerading();
        clearCache();
        clearCookies();
        refreshWidgets();
        clearTheme();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        startLoginFlow();
    }
}
