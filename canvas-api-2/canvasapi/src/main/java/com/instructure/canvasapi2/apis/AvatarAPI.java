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
import com.instructure.canvasapi2.models.Avatar;
import com.instructure.canvasapi2.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public class AvatarAPI {

    interface AvatarsInterface{
        @GET("users/self/avatars")
        Call<List<Avatar>> getAvatarList();

        @PUT("users/self")
        Call<User> updateAvatar(@Query("user[avatar][url]") String avatarUrl);
    }

    public static void getAvatarList(@NonNull RestBuilder adapter, @NonNull RestParams params, StatusCallback<List<Avatar>> callback){
        callback.addCall(adapter.build(AvatarsInterface.class, params).getAvatarList()).enqueue(callback);
    }

    public static void updateAvatar(@NonNull RestBuilder adapter, @NonNull RestParams params, String avatarUrl, StatusCallback<User> callback){
        callback.addCall(adapter.build(AvatarsInterface.class, params).updateAvatar(avatarUrl)).enqueue(callback);
    }
}
