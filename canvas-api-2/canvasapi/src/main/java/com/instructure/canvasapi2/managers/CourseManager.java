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

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.CourseAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Favorite;
import com.instructure.canvasapi2.models.GradingPeriodResponse;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.tests.CourseManager_Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CourseManager extends BaseManager {

    public static boolean mTesting = false;

    public static void getFavoriteCourses(StatusCallback<List<Course>> callback, boolean forceNetwork) {

        if(isTesting() || mTesting) {
            CourseManager_Test.getFavoriteCourses(callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.getFavoriteCourses(adapter, callback, params);
        }
    }

    public static void getCourses(StatusCallback<List<Course>> callback, boolean forceNetwork) {
        if(isTesting() || mTesting) {
            CourseManager_Test.getCourses(callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.getCourses(adapter, callback, params);
        }
    }

    public static void getGradingPeriodsForCourse(StatusCallback<GradingPeriodResponse> callback, long courseId, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            CourseManager_Test.getGradingPeriodsForCourse(callback, courseId);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.getGradingPeriodsForCourse(adapter, callback, params, courseId);
        }
    }

    public static void getCourse(long courseId, StatusCallback<Course> callback, boolean forceNetwork) {
        if(isTesting() || mTesting) {
            CourseManager_Test.getCourse(courseId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.getCourse(courseId, adapter, callback, params);
        }
    }

    public static void getCourseWithGrade(long courseId, StatusCallback<Course> callback, boolean forceNetwork) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.getCourseWithGrade(courseId, adapter, callback, params);
        }
    }

    public static void getCourseStudents(long courseId, StatusCallback<List<User>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            CourseManager_Test.getCourseStudents(courseId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.getCourseStudents(courseId, adapter, callback, params);
        }
    }

    public static void getCourseStudent(long courseId, long studentId, StatusCallback<User> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            CourseManager_Test.getCourseStudent(courseId, studentId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.getCourseStudent(courseId, studentId, adapter, callback, params);
        }
    }

    public static void addCourseToFavorites(long courseId, StatusCallback<Favorite> callback, boolean forceNetwork) {
        if(isTesting() || mTesting) {
            CourseManager_Test.addCourseToFavorites(courseId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.addCourseToFavorites(courseId, adapter, callback, params);
        }
    }

    public static void removeCourseFromFavorites(long courseId, StatusCallback<Favorite> callback, boolean forceNetwork) {
        if(isTesting() || mTesting) {
            CourseManager_Test.removeCourseFromFavorites(courseId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.removeCourseFromFavorites(courseId, adapter, callback, params);
        }
    }

    public static void editCourseName(long courseId, String newCourseName, StatusCallback<Course> callback, boolean forceNetwork) {
        if(isTesting() || mTesting) {
            // TODO:
            // CourseManager_Test.editCourseName(courseId, callback);
        } else {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("course[name]", newCourseName);

            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.updateCourse(courseId, queryParams, adapter, callback, params);
        }
    }

    public static void editCourseHomePage(long courseId, String newHomePage, boolean forceNetwork, StatusCallback<Course> callback) {
        if(isTesting() || mTesting) {
            // TODO:
            // CourseManager_Test.editCourseName(courseId, callback);
        } else {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("course[default_view]", newHomePage);

            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CourseAPI.updateCourse(courseId, queryParams, adapter, callback, params);
        }
    }

    public static void getCoursesWithEnrollmentType(boolean forceNetwork, StatusCallback<List<Course>> callback, String type) {
        if (isTesting() || mTesting) {
            // TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();
            CourseAPI.getCoursesByEnrollmentType(adapter, callback, params, type);
        }
    }

    //region Airwolf
    public static void getCourseWithGradeAirwolf(String airwolfDomain, String parentId, String studentId, long courseId, StatusCallback<Course> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            CourseAPI.getCourseWithGradeAirwolf(parentId, studentId, courseId, adapter, callback, params);
        }
    }

    public static void getCourseWithSyllabusAirwolf(String airwolfDomain, String parentId, String studentId, long courseId, StatusCallback<Course> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            CourseAPI.getCourseWithSyllabusAirwolf(parentId, studentId, courseId, adapter, callback, params);
        }
    }

    public static void getCoursesForUserAirwolf(String airwolfDomain, String parentId, String studentId, boolean forceNetwork, StatusCallback<List<Course>> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            CourseAPI.getCoursesForUserAirwolf(parentId, studentId, adapter, callback, params);
        }
    }
    //endregion
}
