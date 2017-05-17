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

package com.instructure.canvasapi2.tests;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.AssignmentGroup;
import com.instructure.canvasapi2.models.GradeableStudent;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.models.post_models.AssignmentPostBody;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class AssignmentManager_Test {

    public static void getAssignmentGroupsWithAssignments(long courseId, StatusCallback<List<AssignmentGroup>> callback) {
        //TODO:
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<AssignmentGroup> assignments = new ArrayList<>();
        retrofit2.Response<List<AssignmentGroup>> response = retrofit2.Response.success(assignments, httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getAssignmentGroupsWithAssignmentsForGradingPeriod(long courseId, StatusCallback<List<AssignmentGroup>> callback, long gradingPeriodId) {
        //TODO:
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<AssignmentGroup> assignments = new ArrayList<>();
        retrofit2.Response<List<AssignmentGroup>> response = retrofit2.Response.success(assignments, httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }

    public static void editAssignment(AssignmentPostBody assignmentBody, StatusCallback<Assignment> callback) {
        //TODO:
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        retrofit2.Response<Assignment> response = retrofit2.Response.success(new Assignment(), httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }

    public static void deleteAssignment(Assignment assignment, StatusCallback<Assignment> callback) {
        //TODO:
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        retrofit2.Response<Assignment> response = retrofit2.Response.success(assignment, httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getAllGradeableStudentsForAssignment(long courseId, long assignmentId, StatusCallback<List<GradeableStudent>> callback) {
        //TODO:
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<GradeableStudent> students = new ArrayList<>();
        retrofit2.Response<List<GradeableStudent>> response = retrofit2.Response.success(students, httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getAllSubmissionsForAssignment(long courseId, long assignmentId, StatusCallback<List<Submission>> callback) {
        //TODO:
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<Submission> submissions = new ArrayList<>();
        retrofit2.Response<List<Submission>> response = retrofit2.Response.success(submissions, httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getAssignment(long assignmentId, long courseId, StatusCallback<Assignment> callback) {
        //TODO:
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        Assignment assignment = new Assignment();
        retrofit2.Response<Assignment> response = retrofit2.Response.success(assignment, httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getAllAssignments(long courseId, StatusCallback<List<Assignment>> callback) {
        //TODO:
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<Assignment> assignments = new ArrayList<>();
        retrofit2.Response<List<Assignment>> response = retrofit2.Response.success(assignments, httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }
}