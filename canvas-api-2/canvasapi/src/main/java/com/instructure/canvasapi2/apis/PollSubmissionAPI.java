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
import com.instructure.canvasapi2.models.PollSubmissionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class PollSubmissionAPI {

    interface PollSubmissionInterface {

        @GET("polls/{pollId}/poll_sessions/{pollSessionId}/poll_submissions/{pollSubmissionId}")
        Call<PollSubmissionResponse> getPollSubmission(
                @Path("pollId") long pollId,
                @Path("pollSessionId") long pollSessionId,
                @Path("pollSubmissionId") long pollSubmissionId);

        @POST("polls/{pollId}/poll_sessions/{pollSessionId}/poll_submissions")
        Call<PollSubmissionResponse> createPollSubmission(
                @Path("pollId") long pollId,
                @Path("pollSessionId") long pollSessionId,
                @Query("poll_submissions[][poll_choice_id]") long pollChoiceId,
                @Body String body);
    }

    public static void getPollSubmission(long pollId, long pollSessionId, long pollSubmissionId, RestBuilder adapter, RestParams params, StatusCallback<PollSubmissionResponse> callback) {
        callback.addCall(adapter.build(PollSubmissionInterface.class, params).getPollSubmission(pollId, pollSessionId, pollSubmissionId)).enqueue(callback);
    }

    public static void createPollSubmission(long pollId, long pollSessionId, long pollChoiceId, RestBuilder adapter, RestParams params, StatusCallback<PollSubmissionResponse> callback) {
        callback.addCall(adapter.build(PollSubmissionInterface.class, params).createPollSubmission(pollId, pollSessionId, pollChoiceId, "")).enqueue(callback);
    }
}
