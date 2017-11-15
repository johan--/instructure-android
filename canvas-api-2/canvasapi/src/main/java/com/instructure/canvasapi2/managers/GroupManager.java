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
import com.instructure.canvasapi2.apis.GroupAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Favorite;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.utils.ExhaustiveListCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getFavoriteGroups(StatusCallback<List<Group>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();
            final RestBuilder adapter = new RestBuilder(callback);
            StatusCallback<List<Group>> depaginatedCallback = new ExhaustiveListCallback<Group>(callback) {
                @Override
                public void getNextPage(@NonNull StatusCallback<List<Group>> callback, @NonNull String nextUrl, boolean isCached) {
                    GroupAPI.getNextPageGroups(nextUrl, adapter, callback, params);
                }
            };
            adapter.setStatusCallback(depaginatedCallback);
            GroupAPI.getFavoriteGroups(adapter, depaginatedCallback, params);
        }
    }

    public static void getAllGroups(StatusCallback<List<Group>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(true)
                    .build();
            final RestBuilder adapter = new RestBuilder(callback);
            StatusCallback<List<Group>> depaginatedCallback = new ExhaustiveListCallback<Group>(callback) {
                @Override
                public void getNextPage(@NonNull StatusCallback<List<Group>> callback, @NonNull String nextUrl, boolean isCached) {
                    GroupAPI.getNextPageGroups(nextUrl, adapter, callback, params);
                }
            };
            adapter.setStatusCallback(depaginatedCallback);
            GroupAPI.getFirstPageGroups(adapter, depaginatedCallback, params);
        }
    }

    public static void getDetailedGroup(long groupId, StatusCallback<Group> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withShouldIgnoreToken(false)
                    .build();
            final RestBuilder adapter = new RestBuilder(callback);

            GroupAPI.getDetailedGroup(adapter, callback, params, groupId);
        }
    }

    public static void addGroupToFavorites(long groupId, StatusCallback<Favorite> callback) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .build();
            final RestBuilder adapter = new RestBuilder(callback);

            GroupAPI.addGroupToFavorites(adapter, callback, params, groupId);
        }
    }

    public static void removeGroupFromFavorites(long groupId, StatusCallback<Favorite> callback) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .build();
            final RestBuilder adapter = new RestBuilder(callback);

            GroupAPI.removeGroupFromFavorites(adapter, callback, params, groupId);
        }
    }

    public static List<Group> getGroupsSynchronous(final boolean forceNetwork) {
        if(isTesting() || mTesting) {
                //TODO
                return null;
            } else {
                final RestBuilder adapter = new RestBuilder();
                RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();


                return GroupAPI.getAllGroupsSynchronous(adapter, params);
            }
    }

    public static Map<Long, Group> createGroupMap(List<Group> groups) {
        Map<Long, Group> groupMap = new HashMap<Long, Group>();
        for (Group group : groups) {
            groupMap.put(group.getId(), group);
        }
        return groupMap;
    }
}
