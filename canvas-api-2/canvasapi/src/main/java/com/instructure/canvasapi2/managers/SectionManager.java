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

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.SectionAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Section;
import com.instructure.canvasapi2.utils.DepaginatedCallback;

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
            StatusCallback<List<Section>> depaginatedCallback = new DepaginatedCallback<>(callback, new DepaginatedCallback.PageRequestCallback<Section>() {
                @Override
                public void getNextPage(DepaginatedCallback<Section> callback, String nextUrl, boolean isCached) {
                    SectionAPI.getNextPageSections(nextUrl, adapter, callback, params);
                }
            });
            adapter.setStatusCallback(depaginatedCallback);
            SectionAPI.getFirstSectionsForCourse(courseId, adapter, depaginatedCallback, params);
        }
    }
}
