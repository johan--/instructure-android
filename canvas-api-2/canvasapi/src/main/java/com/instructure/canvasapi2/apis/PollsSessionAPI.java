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
import com.instructure.canvasapi2.models.PollSessionResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class PollsSessionAPI {

    interface PollSessionInterface {

        @GET("polls/{pollId}/poll_sessions")
        Call<PollSessionResponse> getFirstPagePollSessionsList(@Path("pollId") long pollId);

        @GET("{next}")
        Call<PollSessionResponse> getNextPagePollSessionsList(@Path(value = "next", encoded = false) String nextURL);

        @GET("polls/{pollId}/poll_sessions/{pollSessionId}")
        Call<PollSessionResponse> getSinglePollSession(@Path("pollId") long pollId, @Path("pollSessionId") long poll_sessionId);

        @POST("polls/{pollId}/poll_sessions")
        Call<PollSessionResponse> createPollSession(
                @Path("pollId") long pollId,
                @Query("poll_sessions[][course_id]") long courseId,
                @Query("poll_sessions[][course_section_id]") long courseSectionId,
                @Body String body);

        @PUT("polls/{pollId}/poll_sessions/{pollSessionId}")
        Call<PollSessionResponse> updatePollSession(
                @Path("pollId") long pollId,
                @Path("pollSessionId") long pollSessionId,
                @Query("poll_sessions[][course_id]") long courseId,
                @Query("poll_sessions[][course_section_id]") long courseSectionId,
                @Query("poll_sessions[][has_public_results]") boolean hasPublicResults,
                @Body String body);

        @DELETE("polls/{pollId}/poll_sessions/{pollSessionId}")
        Call<ResponseBody> deletePollSession(
                @Path("pollId") long pollId,
                @Path("pollSessionId") long pollSessionId);

        @GET("polls/{pollId}/poll_sessions/{pollSessionId}/open")
        Call<ResponseBody> openPollSession(@Path("pollId") long pollId, @Path("pollSessionId") long pollSessionId);

        @GET("polls/{pollId}/poll_sessions/{pollSessionId}/close")
        Call<ResponseBody> closePollSession(@Path("pollId") long pollId, @Path("pollSessionId") long pollSessionId);

        @GET("poll_sessions/opened")
        Call<PollSessionResponse> getOpenSessions();

        @GET("poll_sessions/closed")
        Call<PollSessionResponse> getClosedSessions();
    }

    public static void getFirstPagePollSessions(long pollId, RestBuilder adapter, RestParams params, StatusCallback<PollSessionResponse> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).getFirstPagePollSessionsList(pollId)).enqueue(callback);
    }

    public static void getNextPagePollSessions(String nextURL, RestBuilder adapter, RestParams params, StatusCallback<PollSessionResponse> callback){
        callback.addCall(adapter.build(PollSessionInterface.class, params).getNextPagePollSessionsList(nextURL)).enqueue(callback);
    }

    public static void getSinglePollSession(long pollId, long pollSessionId, RestBuilder adapter, RestParams params, StatusCallback<PollSessionResponse> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).getSinglePollSession(pollId, pollSessionId)).enqueue(callback);
    }

    public static void createPollSession(long pollId, long courseId, long courseSectionId, RestBuilder adapter, RestParams params, StatusCallback<PollSessionResponse> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).createPollSession(pollId, courseId, courseSectionId, "")).enqueue(callback);
    }

    public static void updatePollSession(long pollId, long pollSessionId, long courseId, long courseSectionId, boolean hasPublicResults, RestBuilder adapter, RestParams params, StatusCallback<PollSessionResponse> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).updatePollSession(pollId, pollSessionId, courseId, courseSectionId, hasPublicResults, "")).enqueue(callback);
    }

    public static void deletePollSession(long pollId, long pollSessionId, RestBuilder adapter, RestParams params, StatusCallback<ResponseBody> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).deletePollSession(pollId, pollSessionId)).enqueue(callback);
    }

    public static void openPollSession(long pollId, long pollSessionId, RestBuilder adapter, RestParams params, StatusCallback<ResponseBody> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).openPollSession(pollId, pollSessionId)).enqueue(callback);
    }

    public static void closePollSession(long pollId, long pollSessionId, RestBuilder adapter, RestParams params, StatusCallback<ResponseBody> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).closePollSession(pollId, pollSessionId)).enqueue(callback);
    }

    public static void getOpenSessions(RestBuilder adapter, RestParams params, StatusCallback<PollSessionResponse> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).getOpenSessions()).enqueue(callback);
    }

    public static void getClosedSessions(RestBuilder adapter, RestParams params, StatusCallback<PollSessionResponse> callback) {
        callback.addCall(adapter.build(PollSessionInterface.class, params).getClosedSessions()).enqueue(callback);
    }
}
