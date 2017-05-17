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
import com.instructure.canvasapi2.apis.GroupCategoriesAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.utils.DepaginatedCallback;

import java.util.List;


public class GroupCategoriesManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getAllGroupsForCategory(long categoryId, StatusCallback<List<Group>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();
            final RestBuilder adapter = new RestBuilder(callback);
            StatusCallback<List<Group>> depaginatedCallback = new DepaginatedCallback<>(callback, new DepaginatedCallback.PageRequestCallback<Group>() {
                @Override
                public void getNextPage(DepaginatedCallback<Group> callback, String nextUrl, boolean isCached) {
                    GroupCategoriesAPI.getNextPageGroups(nextUrl, adapter, callback, params);
                }
            });
            adapter.setStatusCallback(depaginatedCallback);
            GroupCategoriesAPI.getFirstPageGroupsInCategory(categoryId, adapter, depaginatedCallback, params);
        }
    }
}
