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

package com.instructure.canvasapi2.managers;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.SubmissionAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.RubricCriterionAssessment;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.tests.SubmissionManager_Test;

import java.util.List;
import java.util.Map;

public class SubmissionManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getStudentSubmissionsForCourse(long studentId, long courseId, @NonNull StatusCallback<List<Submission>> callback, boolean forceNetwork) {
        if (isTesting() | mTesting) {
            SubmissionManager_Test.getStudentSubmissionsForCourse(studentId, courseId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();

            SubmissionAPI.getStudentSubmissionsForCourse(courseId, studentId, adapter, callback, params);
        }
    }

    public static void updateRubricAssessment(long courseId, long assignmentId, long studentId, Map<String, RubricCriterionAssessment> assessmentMap, @NonNull StatusCallback<Submission> callback) {
        if (isTesting() || mTesting) {
            SubmissionManager_Test.updateRubricAssessment(courseId, assignmentId, studentId, assessmentMap, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder().build();
            SubmissionAPI.updateRubricAssessment(courseId, assignmentId, studentId, assessmentMap, adapter, callback, params);
        }
    }

    public static void postSubmissionGrade(long courseId, long assignmentId, long userId, String score, boolean isExcused, boolean forceNetwork, @NonNull StatusCallback<Submission> callback) {
        if (isTesting() | mTesting) {
//            SubmissionManager_Test.postSubmissionGrade(courseId, assignmentId, userId, score, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .build();

            SubmissionAPI.postSubmissionGrade(courseId, assignmentId, userId, score, isExcused, adapter, callback, params);
        }
    }

    public static void postSubmissionExcusedStatus(long courseId, long assignmentId, long userId, boolean isExcused, boolean forceNetwork, @NonNull StatusCallback<Submission> callback) {
        if (isTesting() | mTesting) {
//            SubmissionManager_Test.postSubmissionGrade(courseId, assignmentId, userId, score, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .build();

            SubmissionAPI.postSubmissionExcusedStatus(courseId, assignmentId, userId, isExcused, adapter, callback, params);
        }
    }
}
