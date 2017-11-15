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
import com.instructure.canvasapi2.models.Attendance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class AttendanceAPI {

    public static final String BASE_DOMAIN = "rollcall.instructure.com";
    public static final String BASE_TEST_DOMAIN = "rollcall-beta.instructure.com";

    interface AttendanceInterface {

        @GET("statuses")
        Call<List<Attendance>> getAttendance(
                @Query("section_id") long sectionId,
                @Query("class_date") String date,
                @Header("X-CSRF-Token") String token,
                @Header("Cookie") String cookie);

        @POST("statuses")
        Call<Attendance> postAttendance(
                @Body Attendance body,
                @Header("X-CSRF-Token") String token,
                @Header("Cookie") String cookie);

        @PUT("statuses/{statusId}")
        Call<Attendance> putAttendance(
                @Path("statusId") long statusId,
                @Body Attendance body,
                @Header("X-CSRF-Token") String token,
                @Header("Cookie") String cookie);

        @DELETE("statuses/{statusId}")
        Call<Attendance> deleteAttendance(
                @Path("statusId") long statusId,
                @Header("X-CSRF-Token") String token,
                @Header("Cookie") String cookie);
    }

    /**
     * Get attendance objects for a course
     * @param sectionId A section ID for a course. It is expected the use can view this section and that work has been done client side.
     * @param date A valid Calendar object. This is transformed into a yyyy-MM-dd format
     * @param token The CSRF token which is retrieved from the LTI Launch HTML response.
     * @param cookie The cookie which is retrieved from the LTI Launch
     * @param adapter RestBuilder
     * @param callback StatusCallback
     * @param params RestParams
     */
    public static void getAttendance(long sectionId,
                                     @NonNull Calendar date,
                                     @NonNull String token,
                                     @NonNull String cookie,
                                     @NonNull RestBuilder adapter,
                                     @NonNull StatusCallback<List<Attendance>> callback,
                                     @NonNull RestParams params) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formatter.setTimeZone(date.getTimeZone());
        callback.addCall(adapter.buildRollCall(AttendanceInterface.class, params)
                .getAttendance(sectionId, formatter.format(date.getTime()), token, cookie)).enqueue(callback);
    }

    /**
     * Marks attendance for a particular student. It is expected that the state passed in is the desired state to be posted.
     * @param attendance An attendance object with the expected state. Only exception is the status id should remain until after a DELETE is complete then be removed.
     * @param token The CSRF token which is retrieved from the LTI Launch HTML response.
     * @param cookie The cookie which is retrieved from the LTI Launch
     * @param adapter RestBuilder
     * @param callback StatusCallback
     * @param params RestParams
     */
    public static void markAttendance(Attendance attendance,
                                      @NonNull String token,
                                      @NonNull String cookie,
                                      @NonNull RestBuilder adapter,
                                      @NonNull StatusCallback<Attendance> callback,
                                      @NonNull RestParams params) {
        //if status id == null -> POST
        //if status id != null -> 1. PUT to update 2. DELETE to unmark

        if(attendance.getStatusId() == null) { //Unmarked at the moment
                // POST (initial post of attendance to an unmarked student)
                callback.addCall(adapter.buildRollCall(AttendanceInterface.class, params)
                        .postAttendance(attendance, token, cookie)).enqueue(callback);
        } else {
            if(attendance.attendanceStatus() == Attendance.Attendance.UNMARKED) {
                // DELETE (unmarking a student who has attendance marked)
                callback.addCall(adapter.buildRollCall(AttendanceInterface.class, params)
                        .deleteAttendance(attendance.getStatusId(), token, cookie)).enqueue(callback);
            } else {
                // PUT (updating attendance from current status to another status that is not unmarked)
                callback.addCall(adapter.buildRollCall(AttendanceInterface.class, params)
                        .putAttendance(attendance.getStatusId(), attendance, token, cookie)).enqueue(callback);
            }
        }
    }
}
