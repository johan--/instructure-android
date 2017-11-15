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

package com.instructure.teacher.ui

import com.instructure.teacher.ui.utils.*
import org.junit.Test

class EditCoursesPageTest : TeacherTest() {

    @Test
    @TestRail(ID = "C3109572")
    override fun displaysPageObjects() {
        logIn()
        coursesListPage.openEditFavorites()
        editCoursesListPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3109572")
    fun displaysCourseList() {
        logIn()
        coursesListPage.openEditFavorites()
        editCoursesListPage.assertHasCourses(getAllCourses())
    }

    @Test
    @TestRail(ID = "C3109574")
    fun favoriteCourse() {
        logIn()
        val courses = getAllCourses()
        coursesListPage.openEditFavorites()
        editCoursesListPage.toggleFavoritingCourse(courses[0])
        editCoursesListPage.assertCourseFavorited(courses[0])
    }

    @Test
    @TestRail(ID = "C3109575")
    fun unfavoriteCourse() {
        logIn()
        val courses = getAllCourses()
        coursesListPage.openEditFavorites()
        editCoursesListPage.toggleFavoritingCourse(courses[0])
        editCoursesListPage.assertCourseUnfavorited(courses[0])
    }
}
