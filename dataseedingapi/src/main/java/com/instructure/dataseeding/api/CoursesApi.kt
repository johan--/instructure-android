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
 */
package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.Course
import com.instructure.dataseeding.model.CreateCourse
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class CoursesApi {

    interface CoursesService {

        @POST("courses")
        fun createCourse(@Body createCourse: CreateCourse): Call<Course>
    }

    companion object {
        private val coursesService: CoursesService by lazy {
            CanvasRestAdapter.retrofit.create(CoursesService::class.java)
        }

        fun createRandomCourse(): Response<Course> {
            val randomCourseName = Randomizer.randomCourseName()
            val course = CreateCourse(
                    Course(-1, randomCourseName,
                        randomCourseName.substring(0, 2)
                    )
            )
            return coursesService.createCourse(course).execute()
        }
    }
}
