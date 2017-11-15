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

package com.instructure.canvasapi2.apis;


import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.FileFolder;
import com.instructure.canvasapi2.utils.APIHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class FeaturesAPI {

    public static final String ANONYMOUS_GRADING = "anonymous_grading";

    interface FeaturesInterface {
        @GET("courses/{courseId}/features/enabled")
        Call<List<String>> getEnabledFeaturesForCourse(@Path("courseId") long contextId);
    }

    public static void getEnabledFeaturesForCourse(@NonNull RestBuilder adapter, long courseId, StatusCallback<List<String>> callback, @NonNull RestParams params) {
        if (APIHelper.paramIsNull(callback)) {
            return;
        }

        callback.addCall(adapter.build(FeaturesInterface.class, params).getEnabledFeaturesForCourse(courseId)).enqueue(callback);
    }
}
