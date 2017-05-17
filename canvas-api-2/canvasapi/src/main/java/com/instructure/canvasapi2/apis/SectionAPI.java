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
import com.instructure.canvasapi2.models.Section;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public class SectionAPI {

    interface SectionsInterface {

        @GET("courses/{courseId}/sections")
        Call<List<Section>> getFirstPageSectionsList(@Path("courseId") long courseID);

        @GET
        Call<List<Section>> getNextPageSectionsList(@Url String nextUrl);

        @GET("courses/{courseId}/sections/{sectionId}")
        Call<Section> getSingleSection(@Path("courseId") long courseID, @Path("sectionId") long sectionID);
    }

    public static void getFirstSectionsForCourse(long courseId, RestBuilder adapter, StatusCallback<List<Section>> callback, RestParams params) {
        callback.addCall(adapter.build(SectionsInterface.class, params).getFirstPageSectionsList(courseId)).enqueue(callback);
    }

    public static void getNextPageSections(String nextUrl, RestBuilder adapter, StatusCallback<List<Section>> callback, RestParams params) {
        callback.addCall(adapter.build(SectionsInterface.class, params).getNextPageSectionsList(nextUrl)).enqueue(callback);
    }
}
