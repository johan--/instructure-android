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
package com.instructure.pandautils.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class FontCache {

    private static Map<String, Typeface> mFontMap = new HashMap<>();

    public static Typeface getFont(@NonNull Context context, @NonNull String fontName){
        if (mFontMap.containsKey(fontName)){
            return mFontMap.get(fontName);
        } else {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
            mFontMap.put(fontName, typeface);
            return typeface;
        }
    }
}
