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
import com.instructure.canvasapi2.models.Enrollment;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public class EnrollmentAPI {

    public static final String STUDENT_ENROLLMENT = "StudentEnrollment";
    public static final String TEACHER_ENROLLMENT = "TeacherEnrollment";
    public static final String TA_ENROLLMENT = "TaEnrollment";
    public static final String DESIGNER_ENROLLMENT = "DesignerEnrollment";
    public static final String OBSERVER_ENROLLMENT = "ObserverEnrollment";

    interface EnrollmentInterface {

        @GET("courses/{courseId}/enrollments?include[]=avatar_url")
        Call<List<Enrollment>> getEnrollmentsForCourse(
                @Path("courseId") long courseId,
                @Query("type[]") String enrollmentType);

        @GET
        Call<List<Enrollment>> getNextPage(@Url String nextUrl);

    }

    public static void getEnrollmentsForCourse(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            long courseId,
            @NonNull String enrollmentType,
            @NonNull StatusCallback<List<Enrollment>> callback){

        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(EnrollmentInterface.class, params).getEnrollmentsForCourse(courseId, enrollmentType)).enqueue(callback);
        } else if (callback.getLinkHeaders() != null && StatusCallback.moreCallsExist(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(EnrollmentInterface.class, params).getNextPage(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }
}
