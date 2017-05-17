/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.canvasapi2.utils;

import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Logger {

    public static final String LOG_TAG = "canvasLog";

    public static void d(String s) {
        Log.d(LOG_TAG, s);
    }

    public static void i(String s) {
        Log.i(LOG_TAG, s);
    }

    public static void e(String s) {
        Log.e(LOG_TAG, s);
    }

    public static void v(String s) {
        Log.v(LOG_TAG, s);
    }

    public static void date(String s, GregorianCalendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss zzz", Locale.US);
        Log.d(LOG_TAG, s + ": " + sdf.format(new Date(date.getTimeInMillis())));
    }

    public static void logBundle(final Bundle extras) {
        if(extras != null) {
            d("---====---LOGGING BUNDLE---====---");
            if(extras.size() == 0) {
                d("- Bundle was empty.");
            }
            for (String key: extras.keySet()){
                d("- Bundle: " + key);

                if("bundledExtras".equals(key)) {
                    Bundle innerExtras = extras.getBundle("bundledExtras");
                    if(innerExtras != null) {
                        for (String innerKey: innerExtras.keySet()) {
                            d("   -> Inner Bundle: " + innerKey);
                        }
                    }
                }
            }
        } else {
            d("Bundle was null.");
        }
    }
}
