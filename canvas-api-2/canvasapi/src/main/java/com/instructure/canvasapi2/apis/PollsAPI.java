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
import com.instructure.canvasapi2.models.PollResponse;

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

public class PollsAPI {

    interface PollsInterface {

        @GET("polls")
        Call<PollResponse> getPollsList();

        @GET("{next}")
        Call<PollResponse> next(@Path(value = "next", encoded = false) String nextURL);

        @GET("polls/{pollId}")
        Call<PollResponse> getSinglePoll(@Path("pollId") long pollId);

        @POST("polls")
        Call<PollResponse> createPoll(@Query("polls[][question]") String pollTitle, @Body String body);

        @PUT("polls/{pollId}")
        Call<PollResponse> updatePoll(@Path("pollId") long pollId, @Query("polls[][question]") String pollTitle, @Body String body);

        @DELETE("polls/{pollId}")
        Call<ResponseBody> deletePoll(@Path("pollId") long pollId);
    }

    public static void getFirstPagePolls(RestBuilder adapter, RestParams params, StatusCallback<PollResponse> callback) {
        callback.addCall(adapter.build(PollsInterface.class, params).getPollsList()).enqueue(callback);
    }

    public static void getNextPagePolls(String nextURL, RestBuilder adapter, RestParams params, StatusCallback<PollResponse> callback){
        callback.addCall(adapter.build(PollsInterface.class, params).next(nextURL)).enqueue(callback);
    }

    public static void getSinglePoll(long pollId, RestBuilder adapter, RestParams params, StatusCallback<PollResponse> callback) {
        callback.addCall(adapter.build(PollsInterface.class, params).getSinglePoll(pollId)).enqueue(callback);
    }

    public static void createPoll(String title, RestBuilder adapter, RestParams params, StatusCallback<PollResponse> callback) {
        callback.addCall(adapter.build(PollsInterface.class, params).createPoll(title, "")).enqueue(callback);
    }

    public static void updatePoll(long pollId, String title, RestBuilder adapter, RestParams params, StatusCallback<PollResponse> callback) {
        callback.addCall(adapter.build(PollsInterface.class, params).updatePoll(pollId, title, "")).enqueue(callback);
    }

    public static void deletePoll(long pollId, RestBuilder adapter, RestParams params, StatusCallback<ResponseBody> callback) {
        callback.addCall(adapter.build(PollsInterface.class, params).deletePoll(pollId)).enqueue(callback);
    }
}
