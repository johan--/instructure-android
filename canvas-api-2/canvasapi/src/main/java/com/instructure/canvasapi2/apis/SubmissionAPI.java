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
import android.text.TextUtils;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.RubricCriterionAssessment;
import com.instructure.canvasapi2.models.Submission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;


public class SubmissionAPI {

    interface SubmissionInterface {

        @GET("courses/{courseId}/students/submissions")
        Call<List<Submission>> getStudentSubmissionsForCourse(@Path("courseId") long courseId, @Query("student_ids[]") long studentId);

        @GET
        Call<List<Submission>> getNextPageSubmissions(@Url String nextUrl);

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/{userId}")
        Call<Submission> postSubmissionRubricAssessmentMap(
                @Path("courseId") long courseId,
                @Path("assignmentId") long assignmentId,
                @Path("userId") long userId,
                @QueryMap Map<String, String> rubricAssessment
        );
        
        @PUT("courses/{contextId}/assignments/{assignmentId}/submissions/{userId}")
        Call<Submission> postSubmissionGrade(@Path("contextId") long contextId,
                                             @Path("assignmentId") long assignmentId, @Path("userId") long userId,
                                             @Query("submission[posted_grade]") String assignmentScore,
                                             @Query("submission[excuse]") boolean isExcused);

        @PUT("courses/{contextId}/assignments/{assignmentId}/submissions/{userId}")
        Call<Submission> postSubmissionExcusedStatus(@Path("contextId") long contextId,
                                             @Path("assignmentId") long assignmentId, @Path("userId") long userId,
                                             @Query("submission[excuse]") boolean isExcused);
    }

    public static void getStudentSubmissionsForCourse(long courseId, long studentId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<Submission>> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(SubmissionInterface.class, params).getStudentSubmissionsForCourse(courseId, studentId)).enqueue(callback);
        } else if (callback.getLinkHeaders() != null && StatusCallback.moreCallsExist(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(SubmissionInterface.class, params).getNextPageSubmissions(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void postSubmissionGrade(long courseId, long assignmentId, long userId, String assignmentScore, boolean isExcused, @NonNull RestBuilder adapter, @NonNull StatusCallback<Submission> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(SubmissionInterface.class, params).postSubmissionGrade(courseId, assignmentId, userId, assignmentScore, isExcused)).enqueue(callback);
    }

    public static void postSubmissionExcusedStatus(long courseId, long assignmentId, long userId, boolean isExcused, @NonNull RestBuilder adapter, @NonNull StatusCallback<Submission> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(SubmissionInterface.class, params).postSubmissionExcusedStatus(courseId, assignmentId, userId, isExcused)).enqueue(callback);
    }

    public static void updateRubricAssessment(long courseId, long assignmentId, long userId, Map<String, RubricCriterionAssessment> rubricAssessment, @NonNull RestBuilder adapter, @NonNull StatusCallback<Submission> callback, @NonNull RestParams params) {
        Map<String, String> assessmentParamMap = generateRubricAssessmentQueryMap(rubricAssessment);
        callback.addCall(adapter.build(SubmissionInterface.class, params).postSubmissionRubricAssessmentMap(courseId, assignmentId, userId, assessmentParamMap)).enqueue(callback);
    }

    private static final String assessmentPrefix = "rubric_assessment[";
    private static final String pointsPostFix = "][points]";
    private static final String commentsPostFix = "][comments]";

    private static Map<String, String> generateRubricAssessmentQueryMap(Map<String, RubricCriterionAssessment> rubricAssessment) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, RubricCriterionAssessment> entry : rubricAssessment.entrySet()) {
            RubricCriterionAssessment rating = entry.getValue();
            String criterionId = entry.getKey();
            if (rating.getPoints() != null) {
                map.put(assessmentPrefix + criterionId + pointsPostFix, String.valueOf(rating.getPoints()));
            }
            map.put(assessmentPrefix + criterionId + commentsPostFix, TextUtils.isEmpty(rating.getComments()) ? "" :rating.getComments());
        }
        return map;
    }
}
