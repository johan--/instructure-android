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

package com.instructure.canvasapi2.managers;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.SectionAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Section;
import com.instructure.canvasapi2.utils.ExhaustiveListCallback;

import java.util.List;


public class SectionManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getAllSectionsForCourse(long courseId, StatusCallback<List<Section>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();
            final RestBuilder adapter = new RestBuilder(callback);
            StatusCallback<List<Section>> depaginatedCallback = new ExhaustiveListCallback<Section>(callback) {
                @Override
                public void getNextPage(@NonNull StatusCallback<List<Section>> callback, @NonNull String nextUrl, boolean isCached) {
                    SectionAPI.getNextPageSections(nextUrl, adapter, callback, params);
                }
            };
            adapter.setStatusCallback(depaginatedCallback);
            SectionAPI.getFirstSectionsForCourse(courseId, adapter, depaginatedCallback, params);
        }
    }

    public static void getSection(long courseId, long sectionId, StatusCallback<Section> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            SectionAPI.getSection(courseId, sectionId, adapter, callback, params);
        }
    }
}
