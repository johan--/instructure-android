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


import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Group;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public class GroupCategoriesAPI {

    interface GroupCategoriesInterface {

        @GET("group_categories/{groupCategoryId}/groups")
        Call<List<Group>> getFirstPageGroupsFromCategory(@Path("groupCategoryId") long groupCategoryId);

        @GET
        Call<List<Group>> getNextPageGroups(@Url String nextUrl);

    }

    public static void getFirstPageGroupsInCategory(long categoryId, RestBuilder adapter, StatusCallback<List<Group>> callback, RestParams params) {
        callback.addCall(adapter.build(GroupCategoriesInterface.class, params).getFirstPageGroupsFromCategory(categoryId)).enqueue(callback);
    }

    public static void getNextPageGroups(String nextUrl, RestBuilder adapter, StatusCallback<List<Group>> callback, RestParams params) {
        callback.addCall(adapter.build(GroupCategoriesInterface.class, params).getNextPageGroups(nextUrl)).enqueue(callback);
    }
}
