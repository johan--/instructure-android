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
import android.support.annotation.Nullable;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.ScheduleItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


public class CalendarEventAPI {

    public static final int EVENT_TYPE = 0;
    public static final int ASSIGNMENT_TYPE = 1;

    interface CalendarEventInterface {

        @GET("users/{userId}/calendar_events/")
        Call<List<ScheduleItem>> getCalendarEventsForUser(
                @Path("userId") long userId,
                @Query("type") String type,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @Query(value = "context_codes[]", encoded = true) List<String> contextCodes);

        @GET("calendar_events/")
        Call<List<ScheduleItem>>  getCalendarEvents(
                @Query("all_events") boolean allEvents,
                @Query("type") String type,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @Query(value = "context_codes[]", encoded = true) List<String> contextCodes);

        @GET
        Call<List<ScheduleItem>> next(@Url String url);

        @GET("calendar_events/{eventId}")
        Call<ScheduleItem> getCalendarEvent(@Path("eventId") long eventId);

        @DELETE("calendar_events/{eventId}")
        Call<ScheduleItem> deleteCalendarEvent(@Path("eventId") long eventId, @Query("cancel_reason") String cancelReason);

        @GET("users/self/upcoming_events")
        Call<List<ScheduleItem>> getUpcomingEvents();

        @POST("calendar_events/")
        Call<ScheduleItem> createCalendarEvent(
                @Query("calendar_event[context_code]") String contextCode,
                @Query("calendar_event[title]") String title,
                @Query("calendar_event[description]") String description,
                @Query("calendar_event[start_at]") String startDate,
                @Query("calendar_event[end_at]") String endDate,
                @Query("calendar_event[location_name]") String locationName,
                @Body String body);

        //region Airwolf

        @GET("canvas/{parentId}/{studentId}/calendar_events/{eventId}")
        Call<ScheduleItem> getCalendarEventAirwolf(
                @Path("parentId") String parentId,
                @Path("studentId") String studentId,
                @Path("eventId") String eventId);

        @GET("canvas/{parent_id}/{student_id}/calendar_events?include[]=submission")
        Call<List<ScheduleItem>> getCalendarEventsWithSubmissionsAirwolf(
                @Path("parent_id") String parentId,
                @Path("student_id") String studentId,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @Query(value = "context_codes[]", encoded = true) List<String> contextCodes);

        //endregion
    }

    public enum CalendarEventType {
        CALENDAR("event"),
        ASSIGNMENT("assignment");

        private String apiName;

        CalendarEventType(String apiName) {
            this.apiName = apiName;
        }

        public String getApiName() {
            return apiName;
        }
    }

    public static void getCalendarEvent(
            long eventId,
            @NonNull final RestBuilder adapter,
            @NonNull final RestParams params,
            @NonNull final StatusCallback<ScheduleItem> callback) {
        callback.addCall(adapter.build(CalendarEventInterface.class, params).getCalendarEvent(eventId)).enqueue(callback);
    }

    public static void getCalendarEvents(
            boolean allEvents,
            @NonNull CalendarEventType type,
            @Nullable final String startDate,
            @Nullable final String endDate,
            @NonNull final List<String> canvasContexts,
            @NonNull final RestBuilder adapter,
            @NonNull StatusCallback<List<ScheduleItem>> callback,
            @NonNull final RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(CalendarEventInterface.class, params)
                    .getCalendarEvents(allEvents, type.apiName, startDate, endDate, canvasContexts)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(CalendarEventInterface.class, params)
                    .next(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void getUpcomingEvents(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull StatusCallback<List<ScheduleItem>> callback) {
        callback.addCall(adapter.build(CalendarEventInterface.class, params).getUpcomingEvents()).enqueue(callback);
    }

    public static Response<List<ScheduleItem>> getUpcomingEventsSynchronous(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params) throws IOException {
        return adapter.build(CalendarEventInterface.class, params).getUpcomingEvents().execute();
    }

    public static void deleteCalendarEvent(
            long eventId,
            String cancelReason,
            @NonNull final RestBuilder adapter,
            @NonNull final RestParams params,
            @NonNull final StatusCallback<ScheduleItem> callback) {
        callback.addCall(adapter.build(CalendarEventInterface.class, params).deleteCalendarEvent(eventId, cancelReason)).enqueue(callback);
    }

    public static void createCalendarEvent(
            @NonNull String contextCode,
            @NonNull String title,
            @NonNull String description,
            @NonNull String startDate,
            @NonNull String endDate,
            @NonNull String location,
            @NonNull final RestBuilder adapter,
            @NonNull final RestParams params,
            @NonNull StatusCallback<ScheduleItem> callback){
        Call<ScheduleItem> call = adapter.build(CalendarEventInterface.class, params).createCalendarEvent(contextCode, title, description, startDate, endDate, location, "");
        callback.addCall(call).enqueue(callback);
    }

    //region Airwolf

    public static void getCalendarEventAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String eventId,
            @NonNull final RestBuilder adapter,
            @NonNull StatusCallback<ScheduleItem> callback,
            @NonNull final RestParams params) {
        callback.addCall(adapter.build(CalendarEventInterface.class, params)
                .getCalendarEventAirwolf(parentId, studentId, eventId)).enqueue(callback);
    }

    public static void getAllCalendarEventsWithSubmissionAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String startDate,
            @NonNull String endDate,
            @NonNull ArrayList<String> contextCodes,
            @NonNull final RestBuilder adapter,
            @NonNull StatusCallback<List<ScheduleItem>> callback,
            @NonNull final RestParams params) {

        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(CalendarEventInterface.class, params)
                    .getCalendarEventsWithSubmissionsAirwolf(parentId, studentId, startDate, endDate, contextCodes)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(CalendarEventInterface.class, params)
                    .next(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    //endregion
}

