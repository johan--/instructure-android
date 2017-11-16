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

import com.instructure.dataseeding.model.CreateEnrollment
import com.instructure.dataseeding.model.Enrollment
import com.instructure.dataseeding.util.CanvasRestAdapter
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

class EnrollmentsApi {

    interface EnrollmentsService {

        @POST("courses/{courseId}/enrollments")
        fun enrollUser(@Path("courseId") courseId: Long, @Body enrollment: CreateEnrollment): Call<Enrollment>

    }

    companion object {
        private val enrollmentsService: EnrollmentsService by lazy {
            CanvasRestAdapter.enrollmentRetrofit.create(EnrollmentsService::class.java)
        }

        enum class EnrollmentType {
            StudentEnrollment,
            TeacherEnrollment,
            TaEnrollment,
            ObserverEnrollment,
            DesignerEnrollment
        }

        fun enrollTeacher(courseId: Long, userId: Long): Response<Enrollment> {
            val enrollment = Enrollment(userId, EnrollmentType.TeacherEnrollment.name)
            return enrollmentsService.enrollUser(courseId, CreateEnrollment(enrollment)).execute()
        }
    }
}