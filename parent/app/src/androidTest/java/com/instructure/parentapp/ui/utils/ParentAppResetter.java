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

package com.instructure.parentapp.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.pandautils.utils.AppResetter;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.util.ApplicationManager;
import com.instructure.parentapp.util.Const;

class ParentAppResetter extends AppResetter {

    @Override
    protected void beforeReset(Context context, boolean isTesting) {
    }

    @Override
    protected void onReset(Context context, boolean isTesting) {
        //delete them all
        SharedPreferences.Editor editor = getEditor(context, ApplicationManager.PREF_NAME);
        editor.clear();
        editor.apply();

        //delete all of the others, Bandaid fix for testing, we shouldn't have two locations for prefs
        //TODO: MBL-7050
        editor = getEditor(context, Const.CANVAS_PARENT_SP);
        editor.clear();
        editor.apply();

        //Reset Airwolf Domain
        final String airwolfDomain = ApiPrefs.getAirwolfDomain();
        APIHelper.setAirwolfDomain(context, isTesting ? BuildConfig.GAMMA_DOMAIN : airwolfDomain);
    }

}
