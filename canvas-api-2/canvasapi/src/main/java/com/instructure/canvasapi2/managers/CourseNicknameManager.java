package com.instructure.canvasapi2.managers;

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

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.CourseNicknameAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CourseNickname;
import com.instructure.canvasapi2.tests.CourseNicknameManager_Test;

import java.util.List;

public class CourseNicknameManager extends BaseManager {

    public static boolean mTesting = false;

    public static void getAllNicknames(StatusCallback<List<CourseNickname>> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            CourseNicknameManager_Test.getAllNicknames(callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseNicknameAPI.getAllNicknames(adapter, callback, params);
        }
    }

    public static void getCourseNickname(long courseId, StatusCallback<CourseNickname> callback, boolean forceNetwork) {
        if(isTesting() || mTesting) {
            CourseNicknameManager_Test.getNickname(courseId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseNicknameAPI.getNickname(courseId, adapter, callback, params);
        }
    }

    public static void setCourseNickname(long courseId, String nickname, StatusCallback<CourseNickname> callback) {
        if(isTesting() || mTesting) {
            CourseNicknameManager_Test.setNickname(courseId, nickname, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(true)
                    .build();

            CourseNicknameAPI.setNickname(courseId, nickname, adapter, callback, params);
        }
    }

    public static void deleteNickname(long courseId, final StatusCallback<CourseNickname> callback){

        if (isTesting() || mTesting) {
            CourseNicknameManager_Test.deleteNickname(courseId, callback);
        } else {

            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();

            CourseNicknameAPI.deleteNickname(courseId, adapter, callback, params);
        }
    }

    public static void deleteAllNicknames(final StatusCallback<CourseNickname> callback){

        if (isTesting() || mTesting) {
            CourseNicknameManager_Test.deleteAllNicknames(callback);
        } else {

            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();

            CourseNicknameAPI.deleteAllNicknames(adapter, callback, params);
        }
    }
}
