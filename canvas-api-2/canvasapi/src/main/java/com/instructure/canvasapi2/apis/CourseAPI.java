/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Favorite;
import com.instructure.canvasapi2.models.GradingPeriodResponse;
import com.instructure.canvasapi2.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;


public class CourseAPI {

    static final String ACTIVE_ENROLLMENT_STATE = "active";

    interface CoursesInterface {

        @GET("courses/{courseId}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=course_image")
        Call<Course> getCourse(@Path("courseId") long courseId);

        @GET("courses/{courseId}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=total_scores&include[]=current_grading_period_scores&include[]=course_image")
        Call<Course> getCourseWithGrade(@Path("courseId") long courseId);

        @GET("users/self/favorites/courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=current_grading_period_scores&include[]=course_image&include[]=favorites")
        Call<List<Course>> getFavoriteCourses();

        @GET("courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=permissions&include[]=favorites&include[]=current_grading_period_scores&include[]=course_image")
        Call<List<Course>> getFirstPageCourses();

        @GET
        Call<List<Course>> next(@Url String nextURL);

        @GET("courses")
        Call<List<Course>> getCoursesByEnrollmentType(@Query("enrollment_type") String type);

        // TODO: Set up pagination when API is fixed and remove per_page query parameterø
        @GET("courses/{courseId}/grading_periods?per_page=100")
        Call<GradingPeriodResponse> getGradingPeriodsForCourse(
                @Path("courseId") long courseId);

        @GET("courses/{courseId}/students")
        Call<List<User>> getCourseStudents(@Path("courseId") long courseId);

        @GET("courses/{courseId}/users/{studentId}")
        Call<User> getCourseStudent(@Path("courseId") long courseId, @Path("studentId") long studentId);

        @GET
        Call<List<User>> getNextPageStudents(@Url String nextUrl);

        @POST("users/self/favorites/courses/{courseId}")
        Call<Favorite> addCourseToFavorites(@Path("courseId") long courseId);

        @DELETE("users/self/favorites/courses/{courseId}")
        Call<Favorite> removeCourseFromFavorites(@Path("courseId") long courseId);

        @PUT("courses/{course_id}")
        Call<Course> updateCourse(@Path("course_id") long courseId, @QueryMap Map<String, String> params);

        //region Airwolf

        @GET("canvas/{parentId}/{studentId}/courses?include[]=total_scores&include[]=syllabus_body&include[]=current_grading_period_scores")
        Call<List<Course>> getCoursesForUserAirwolf(
                @Path("parentId") String parentId,
                @Path("studentId") String studentId,
                @Query("enrollment_state") String enrollmentState);

        @GET("canvas/{parentId}/{studentId}/courses/{courseId}?include[]=syllabus_body&include[]=term&include[]=license&include[]=is_public&include[]=permissions")
        Call<Course> getCourseWithSyllabusAirwolf(
                @Path("parentId") String parentId,
                @Path("studentId") String studentId,
                @Path("courseId") long courseId);

        @GET("canvas/{parentId}/{studentId}/courses/{courseId}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=total_scores&include[]=current_grading_period_scores")
        Call<Course> getCourseWithGradeAirwolf(
                @Path("parentId") String parentId,
                @Path("studentId") String studentId,
                @Path("courseId") long courseId);

        //endregion
    }

    public static void getFavoriteCourses(@NonNull RestBuilder adapter, @NonNull StatusCallback<List<Course>> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(CoursesInterface.class, params).getFavoriteCourses()).enqueue(callback);
    }

    public static void getCourses(@NonNull RestBuilder adapter, @NonNull StatusCallback<List<Course>> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(CoursesInterface.class, params).getFirstPageCourses()).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(CoursesInterface.class, params).next(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    // TODO: Set up pagination when API is fixed. API currently sends pagination data in body instead of headers
    public static void getGradingPeriodsForCourse(@NonNull RestBuilder adapter, @NonNull StatusCallback<GradingPeriodResponse> callback, @NonNull RestParams params, long courseId) {
        callback.addCall(adapter.build(CoursesInterface.class, params).getGradingPeriodsForCourse(courseId)).enqueue(callback);
    }

    public static void getCourse(long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<Course> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(CoursesInterface.class, params).getCourse(courseId)).enqueue(callback);
    }

    public static void getCoursesByEnrollmentType(@NonNull RestBuilder adapter, @NonNull StatusCallback<List<Course>> callback, @NonNull RestParams params, String type) {
        callback.addCall(adapter.build(CoursesInterface.class, params).getCoursesByEnrollmentType(type)).enqueue(callback);
    }

    public static void getCourseWithGrade(long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<Course> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(CoursesInterface.class, params).getCourseWithGrade(courseId)).enqueue(callback);
    }

    public static void getCourseStudents(long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<User>> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(CoursesInterface.class, params).getCourseStudents(courseId)).enqueue(callback);
        } else if (callback.getLinkHeaders() != null && StatusCallback.moreCallsExist(callback.getLinkHeaders())){
            callback.addCall(adapter.build(CoursesInterface.class, params).getNextPageStudents(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void getCourseStudent(long courseId, long studentId, @NonNull RestBuilder adapter, @NonNull StatusCallback<User> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(CoursesInterface.class, params).getCourseStudent(courseId, studentId)).enqueue(callback);
    }

    public static void addCourseToFavorites(final long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<Favorite> callback, @NonNull RestParams params) {

        callback.addCall(adapter.build(CoursesInterface.class, params).addCourseToFavorites(courseId)).enqueue(callback);
    }

    public static void removeCourseFromFavorites(final long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<Favorite> callback, @NonNull RestParams params) {

        callback.addCall(adapter.build(CoursesInterface.class, params).removeCourseFromFavorites(courseId)).enqueue(callback);
    }

    /**
     * Updates a course
     * @param courseId The id for the course
     * @param params A map of the fields to change and the values they will change to
     */
    public static void updateCourse(long courseId, Map<String, String> queryParams, @NonNull RestBuilder adapter, @NonNull StatusCallback<Course> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(CoursesInterface.class, params).updateCourse(courseId, queryParams)).enqueue(callback);
    }

    //region airwolf

    public static void getCoursesForUserAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull RestBuilder adapter,
            @NonNull StatusCallback<List<Course>> callback,
            @NonNull RestParams params) {

        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(CoursesInterface.class, params)
                    .getCoursesForUserAirwolf(parentId, studentId, ACTIVE_ENROLLMENT_STATE)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(CoursesInterface.class, params)
                    .next(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void getCourseWithSyllabusAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            long courseId,
            @NonNull RestBuilder adapter,
            @NonNull StatusCallback<Course> callback,
            @NonNull RestParams params) {

        callback.addCall(adapter.build(CoursesInterface.class, params)
                .getCourseWithSyllabusAirwolf(parentId, studentId, courseId)).enqueue(callback);
    }

    public static void getCourseWithGradeAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            long courseId,
            @NonNull RestBuilder adapter,
            @NonNull StatusCallback<Course> callback,
            @NonNull RestParams params) {

        callback.addCall(adapter.build(CoursesInterface.class, params)
                .getCourseWithGradeAirwolf(parentId, studentId, courseId)).enqueue(callback);
    }

    //endregion
}
