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

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.QuizAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Quiz;
import com.instructure.canvasapi2.models.QuizQuestion;
import com.instructure.canvasapi2.models.QuizSubmission;
import com.instructure.canvasapi2.models.QuizSubmissionQuestionResponse;
import com.instructure.canvasapi2.models.QuizSubmissionResponse;
import com.instructure.canvasapi2.models.QuizSubmissionTime;
import com.instructure.canvasapi2.models.post_models.QuizPostBody;
import com.instructure.canvasapi2.models.post_models.QuizPostBodyWrapper;
import com.instructure.canvasapi2.tests.QuizManager_Test;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ExhaustiveCallback;
import com.instructure.canvasapi2.utils.ExhaustiveListCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;


public class QuizManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getQuizQuestions(long courseId, long quizId, StatusCallback<List<QuizQuestion>> callback) {

        if(isTesting() || mTesting) {
            QuizManager_Test.getQuizQuestions(courseId, quizId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();

            QuizAPI.getQuizQuestions(courseId, quizId, adapter, callback, params);
        }
    }

    public static void startQuizPreview(long courseId, long quizId, boolean forceNetwork, StatusCallback<QuizSubmissionResponse> callback) {

        if(isTesting() || mTesting) {
            QuizManager_Test.startQuizPreview(courseId, quizId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            QuizAPI.startQuizPreview(courseId, quizId, adapter, callback, params);
        }
    }

    public static void getQuizSubmissionQuestions(long quizSubmissionId, StatusCallback<QuizSubmissionQuestionResponse> callback) {

        if(isTesting() || mTesting) {
            QuizManager_Test.getQuizSubmissionQuestions(quizSubmissionId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();

            QuizAPI.getQuizSubmissionQuestions(quizSubmissionId, adapter, callback, params);
        }
    }

    public static void getQuizzes(long courseId, final boolean forceNetwork, StatusCallback<List<Quiz>> callback) {
        if (isTesting() || mTesting) {
            QuizManager_Test.getQuizesQuestions(callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            QuizAPI.getQuizzes(courseId, adapter, callback, params);
        }
    }

    public static void getAllQuizzes(long courseId, final boolean forceNetwork, StatusCallback<List<Quiz>> callback) {
        if (isTesting() || mTesting) {
            QuizManager_Test.getQuizesQuestions(callback);
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            StatusCallback<List<Quiz>> depaginatedCallback = new ExhaustiveListCallback<Quiz>(callback) {
                @Override
                public void getNextPage(@NonNull StatusCallback<List<Quiz>> callback, @NonNull String nextUrl, boolean isCached) {
                    QuizAPI.getNextPageQuizzes(forceNetwork, nextUrl, adapter, callback);
                }
            };
            adapter.setStatusCallback(depaginatedCallback);
            QuizAPI.getFirstPageQuizzes(courseId, forceNetwork, adapter, depaginatedCallback);
        }
    }

    public static void getQuiz(long courseId, long quizId, final boolean forceNetwork, StatusCallback<Quiz> callback) {
        if (isTesting() || mTesting) {
            QuizManager_Test.getQuiz(callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            QuizAPI.getQuiz(courseId, quizId, adapter, callback, params);
        }
    }


    public static void editQuiz(long courseId, long quizId, QuizPostBody body, final StatusCallback<Quiz> callback){

        if (isTesting() || mTesting) {
            QuizManager_Test.editQuiz(body, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .build();


            QuizPostBodyWrapper bodyWrapper = new QuizPostBodyWrapper();
            bodyWrapper.setQuiz(body);
            QuizAPI.editQuiz(courseId, quizId, bodyWrapper, adapter, callback, params);
        }
    }

    public static void getQuizSubmissions(CanvasContext canvasContext, long quizId, final boolean forceNetwork, StatusCallback<QuizSubmissionResponse> callback) {
        if(isTesting() || mTesting) {
            QuizManager_Test.getQuizSubmissions(callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            QuizAPI.getQuizSubmissions(canvasContext, quizId, adapter, callback, params);
        }
    }

    public static void getAllQuizSubmissions(final CanvasContext canvasContext, final long quizId, final boolean forceNetwork, final StatusCallback<List<QuizSubmission>> callback) {
        if (isTesting() || mTesting) {
            QuizManager_Test.getAllQuizSubmissions(callback);
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            final ExhaustiveCallback<QuizSubmissionResponse, QuizSubmission> paginatedCallback = new ExhaustiveCallback<QuizSubmissionResponse, QuizSubmission>(callback) {
                @Override
                public void getNextPage(@NotNull StatusCallback<QuizSubmissionResponse> callback, @NotNull String nextUrl, boolean isCached) {
                    QuizAPI.getQuizSubmissions(canvasContext, quizId, adapter, callback, params);
                }

                @NotNull
                @Override
                public List<QuizSubmission> extractItems(QuizSubmissionResponse response) {
                    return response.getQuizSubmissions();
                }
            };

            adapter.setStatusCallback(paginatedCallback);
            QuizAPI.getQuizSubmissions(canvasContext, quizId, adapter, paginatedCallback, params);
        }
    }

    public static void getFirstPageQuizList(final CanvasContext canvasContext, final boolean forceNetwork, final StatusCallback<List<Quiz>> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.getFirstPageQuizList(canvasContext, adapter, params, callback);
    }

    public static void getNextPageQuizList(String nextPage, final boolean forceNetwork, final StatusCallback<List<Quiz>> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.getNextPageQuizList(nextPage, adapter, params, callback);
    }

    public static void getDetailedQuiz(final CanvasContext canvasContext, long quizId, final boolean forceNetwork, final StatusCallback<Quiz> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.getDetailedQuiz(canvasContext, quizId, adapter, params, callback);
    }

    public static void getDetailedQuizByUrl(String quizUrl, final boolean forceNetwork, final StatusCallback<Quiz> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.getDetailedQuizByUrl(quizUrl, adapter, params, callback);
    }

    public static void submitQuiz(CanvasContext canvasContext, QuizSubmission quizSubmission, final boolean forceNetwork, final StatusCallback<QuizSubmissionResponse> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.submitQuiz(canvasContext, quizSubmission, adapter, params, callback);
    }

    public static void getFirstPageQuizSubmissions(CanvasContext canvasContext, long quizId, final boolean forceNetwork, final StatusCallback<QuizSubmissionResponse> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.getFirstPageQuizSubmissions(canvasContext, quizId, adapter, params, callback);
    }

    public static void postQuizStartedEvent(CanvasContext canvasContext, long quizId, long submissionId, boolean forceNetwork, final StatusCallback<ResponseBody> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.postQuizStartedEvent(canvasContext, quizId, submissionId, ApiPrefs.getUserAgent(), adapter, params, callback);
    }

    public static void startQuiz(CanvasContext canvasContext, long quizId, boolean forceNetwork, final StatusCallback<QuizSubmissionResponse> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.startQuiz(canvasContext, quizId, adapter, params, callback);
    }

    public static void getQuizSubmissionTime(CanvasContext canvasContext, QuizSubmission quizSubmission, boolean forceNetwork, final StatusCallback<QuizSubmissionTime> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.getQuizSubmissionTime(canvasContext, quizSubmission.getQuizId(), quizSubmission.getId(), adapter, params, callback);
    }

    public static void postQuizSubmit(CanvasContext canvasContext, QuizSubmission quizSubmission, boolean forceNetwork, final StatusCallback<QuizSubmissionResponse> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.postQuizSubmit(canvasContext, quizSubmission, adapter, params, callback);
    }

    public static void getFirstPageSubmissionQuestions(long quizSubmissionId, boolean forceNetwork, @NonNull StatusCallback<QuizSubmissionQuestionResponse> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.getFirstPageSubmissionQuestions(quizSubmissionId, adapter, params, callback);
    }

    public static void getNextPageSubmissionQuestions(String nextUrl, boolean forceNetwork, @NonNull StatusCallback<QuizSubmissionQuestionResponse> callback) {
        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.getNextPageSubmissionQuestions(nextUrl, adapter, params, callback);
    }

    public static void postQuizQuestionFileUpload(
            CanvasContext canvasContext,
            QuizSubmission quizSubmission,
            long answer,
            long questionId,
            boolean forceNetwork,
            StatusCallback<QuizSubmissionQuestionResponse> callback){

        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.postQuizQuestionFileUpload(canvasContext, quizSubmission, answer, questionId, adapter, params, callback);
    }

    public static void postQuizQuestionMatching(
            QuizSubmission quizSubmission,
            long questionId,
            HashMap<Long, Integer> answers,
            boolean forceNetwork,
            @NonNull StatusCallback<QuizSubmissionQuestionResponse> callback) {

        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.postQuizQuestionMatching(quizSubmission, questionId, answers, adapter, params, callback);
    }

    public static void postQuizQuestionMultipleDropdown(
            QuizSubmission quizSubmission,
            long questionId,
            HashMap<String, Long> answers,
            boolean forceNetwork,
            @NonNull StatusCallback<QuizSubmissionQuestionResponse> callback) {

        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.postQuizQuestionMultipleDropdown(quizSubmission, questionId, answers, adapter, params, callback);
    }

    public static void postQuizQuestionMultiAnswer(
            QuizSubmission quizSubmission,
            long questionId,
            ArrayList<Long> answers,
            boolean forceNetwork,
            @NonNull StatusCallback<QuizSubmissionQuestionResponse> callback) {

        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.postQuizQuestionMultiAnswers(quizSubmission, questionId, answers, adapter, params, callback);
    }

    public static void postQuizQuestionEssay(
            QuizSubmission quizSubmission,
            String answer,
            long questionId,
            boolean forceNetwork,
            @NonNull StatusCallback<QuizSubmissionQuestionResponse> callback) {

        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.postQuizQuestionEssay(quizSubmission, questionId, answer, adapter, params, callback);
    }

    public static void postQuizQuestionMultiChoice(
            QuizSubmission quizSubmission,
            long answer,
            long questionId,
            boolean forceNetwork,
            @NonNull StatusCallback<QuizSubmissionQuestionResponse> callback) {

        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.postQuizQuestionMultiChoice(quizSubmission.getId(), quizSubmission.getAttempt(), questionId, answer, quizSubmission.getValidationToken(), adapter, params, callback);
    }

    public static void putFlagQuizQuestion(
            QuizSubmission quizSubmission,
            long questionId,
            boolean forceNetwork,
            boolean flagQuestion,
            @NonNull StatusCallback<ResponseBody> callback) {

        final RestBuilder adapter = new RestBuilder(callback);
        final RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .withForceReadFromNetwork(forceNetwork)
                .build();

        QuizAPI.putFlagQuizQuestion(quizSubmission, questionId, flagQuestion, adapter, params, callback);
    }
}
