/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* This is an auto-generated file. */

package com.instructure.parentapp.ui.models;

public class Enrollment {
    public int id;
    public int courseId;
    public int userId;
    public String enrollmentState;
    public String type;

    public Enrollment(int id, int courseId, int userId, String enrollmentState, String type) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.enrollmentState = enrollmentState;
        this.type = type;
    }
}
