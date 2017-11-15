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

package com.instructure.canvasapi2.managers;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.PollSubmissionAPI;
import com.instructure.canvasapi2.apis.PollsAPI;
import com.instructure.canvasapi2.apis.PollsChoiceAPI;
import com.instructure.canvasapi2.apis.PollsSessionAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.PollChoiceResponse;
import com.instructure.canvasapi2.models.PollResponse;
import com.instructure.canvasapi2.models.PollSessionResponse;
import com.instructure.canvasapi2.models.PollSubmissionResponse;

import okhttp3.ResponseBody;

public class PollsManager extends BaseManager {

    public static boolean mTesting = false;

    public static void getFirstPagePolls(StatusCallback<PollResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsAPI.getFirstPagePolls(adapter, params, callback);
        }
    }

    public static void getNextPagePolls(String nextUrl, StatusCallback<PollResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsAPI.getNextPagePolls(nextUrl, adapter, params, callback);
        }
    }

    public static void createPollSession(long pollId, long courseId, long sectionId, StatusCallback<PollSessionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.createPollSession(pollId, courseId, sectionId, adapter, params, callback);
        }
    }

    public static void openPollSession(long pollId, long sectionId, StatusCallback<ResponseBody> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.openPollSession(pollId, sectionId, adapter, params, callback);
        }
    }

    public static void getFirstPagePollSessions(long pollId, StatusCallback<PollSessionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.getFirstPagePollSessions(pollId, adapter, params, callback);
        }
    }

    public static void getNextPagePollSessions(String nextUrl, StatusCallback<PollSessionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.getNextPagePollSessions(nextUrl, adapter, params, callback);
        }
    }

    public static void updatePoll(long pollId, String title, StatusCallback<PollResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsAPI.updatePoll(pollId, title, adapter, params, callback);
        }
    }

    public static void deletePollChoice(long pollId, long pollChoiceId, StatusCallback<ResponseBody> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsChoiceAPI.deletePollChoice(pollId, pollChoiceId, adapter, params, callback);
        }
    }

    public static void deletePollSession(long pollId, long pollSessionId, StatusCallback<ResponseBody> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.deletePollSession(pollId, pollSessionId, adapter, params, callback);
        }
    }

    public static void createPoll(String title, StatusCallback<PollResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsAPI.createPoll(title, adapter, params, callback);
        }
    }

    public static void createPollChoice(long pollId, String text, boolean isCorrect, int position, StatusCallback<PollChoiceResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsChoiceAPI.createPollChoice(pollId, text, isCorrect, position, adapter, params, callback);
        }
    }

    public static void updatePollChoice(long pollId, long pollChoiceId, String text, boolean isCorrect, int position, StatusCallback<PollChoiceResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsChoiceAPI.updatePollChoice(pollId, pollChoiceId, text, isCorrect, position, adapter, params, callback);
        }
    }

    public static void getClosedSessions(StatusCallback<PollSessionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.getClosedSessions(adapter, params, callback);
        }
    }

    public static void getOpenSessions(StatusCallback<PollSessionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.getOpenSessions(adapter, params, callback);
        }
    }

    public static void getSinglePoll(long pollId, StatusCallback<PollResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsAPI.getSinglePoll(pollId, adapter, params, callback);
        }
    }

    public static void getSinglePollSession(long pollId, long pollSessionId, StatusCallback<PollSessionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.getSinglePollSession(pollId, pollSessionId, adapter, params, callback);
        }
    }


    public static void closePollSession(long pollId, long pollSessionId, StatusCallback<ResponseBody> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.closePollSession(pollId, pollSessionId, adapter, params, callback);
        }
    }

    public static void updatePollSession(long pollId, long pollSessionId, long courseId, long sectionId, boolean hasPublicResults, StatusCallback<PollSessionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsSessionAPI.updatePollSession(pollId, pollSessionId, courseId, sectionId, hasPublicResults, adapter, params, callback);
        }
    }

    public static void getFirstPagePollChoices(long pollId, StatusCallback<PollChoiceResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsChoiceAPI.getFirstPagePollChoices(pollId, adapter, params, callback);
        }
    }

    public static void getNextPagePollChoices(String nextUrl, StatusCallback<PollChoiceResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsChoiceAPI.getNextPagePollChoices(nextUrl, adapter, params, callback);
        }
    }

    public static void deletePoll(long pollId, StatusCallback<ResponseBody> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollsAPI.deletePoll(pollId, adapter, params, callback);
        }
    }

    public static void createPollSubmission(long pollId, long pollSessionId, long pollChoiceId, StatusCallback<PollSubmissionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollSubmissionAPI.createPollSubmission(pollId, pollSessionId, pollChoiceId, adapter, params, callback);
        }
    }

    public static void getPollSubmission(long pollId, long pollSessionId, long pollSubmissionId, StatusCallback<PollSubmissionResponse> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            PollSubmissionAPI.getPollSubmission(pollId, pollSessionId, pollSubmissionId, adapter, params, callback);
        }
    }
}
