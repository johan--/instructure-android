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

import java.util.Date;

public class Assignment {
    public int id;
    public int courseId;
    public String name;
    public String description;
    public boolean published;
    private Date lockAt;

    public Assignment(int id, int courseId, String name, String description, boolean published, Date lockAt) {
        this.id = id;
        this.courseId = courseId;
        this.name = name;
        this.description = description;
        this.published = published;
        this.lockAt = lockAt;
    }

    public boolean locked() {
        if (lockAt == null) {
            return false;
        }

        Date now = new Date();
        if (lockAt.before(now)) {
            return true;
        }
        if (lockAt.equals(now)) {
            return true;
        }
        return false;
    }
}
