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

public class Quiz {
    public int id;
    public String title;
    public String description;
    public String quizType;
    public int timeLimit;
    public boolean showCorrectAnswers;
    public int allowedAttempts;
    public int pointsPossible;
    public String dueAt;
    public boolean published;

    public Quiz(int id, String title, String description, String quizType, int timeLimit, boolean showCorrectAnswers, int allowedAttempts, int pointsPossible, String dueAt, boolean published) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.quizType = quizType;
        this.timeLimit = timeLimit;
        this.showCorrectAnswers = showCorrectAnswers;
        this.allowedAttempts = allowedAttempts;
        this.pointsPossible = pointsPossible;
        this.dueAt = dueAt;
        this.published = published;
    }
}
