package com.instructure.canvasapi2.apis;

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
import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CourseNickname;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class CourseNicknameAPI {

    interface NicknameInterface {

        @GET("users/self/course_nicknames/")
        Call<List<CourseNickname>> getAllNicknames();

        @GET("users/self/course_nicknames/{course_id}")
        Call<CourseNickname> getNickname(@Path("course_id") long courseId);

        @PUT("users/self/course_nicknames/{course_id}")
        Call<CourseNickname> setNickname(@Path("course_id") long courseId, @Query("nickname") String nickname);

        @DELETE("users/self/course_nicknames/{course_id}")
        Call<CourseNickname> deleteNickname(@Path("course_id") long courseId);

        @DELETE("users/self/course_nicknames/")
        Call<CourseNickname> deleteAllNicknames();
    }

    public static void getAllNicknames(@NonNull RestBuilder adapter, @NonNull StatusCallback<List<CourseNickname>> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(NicknameInterface.class, params).getAllNicknames()).enqueue(callback);
    }

    public static void getNickname(long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<CourseNickname> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(NicknameInterface.class, params).getNickname(courseId)).enqueue(callback);
    }

    public static void setNickname(long courseId, String nickname, @NonNull RestBuilder adapter, @NonNull StatusCallback<CourseNickname> callback, @NonNull RestParams params) {
        //Reduces the nickname to only 60 max chars per the api docs.
        nickname = nickname.substring(0, Math.min(nickname.length(), 60));

        callback.addCall(adapter.build(NicknameInterface.class, params).setNickname(courseId, nickname)).enqueue(callback);
    }

    public static void deleteNickname(long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<CourseNickname> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(NicknameInterface.class, params).deleteNickname(courseId)).enqueue(callback);

    }

    public static void deleteAllNicknames(@NonNull RestBuilder adapter, @NonNull StatusCallback<CourseNickname> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(NicknameInterface.class, params).deleteAllNicknames()).enqueue(callback);
    }
}
