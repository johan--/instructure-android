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

package com.instructure.teacher.ui.utils

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import com.instructure.canvasapi2.models.User
import com.instructure.soseedy.Teacher
import com.instructure.teacher.ui.data.Data
import com.instructure.teacher.ui.models.*
import java.util.*

fun TeacherTest.enterDomain(): CanvasUser {
    val teacher = Data.getNextTeacher()
    loginFindSchoolPage.enterDomain(teacher.domain)
    return teacher
}

fun TeacherTest.enterStudentDomain(): CanvasUser {
    val student = Data.getNextStudent()
    loginFindSchoolPage.enterDomain(student.domain)
    return student
}

fun TeacherTest.slowLogIn(): CanvasUser {
    loginLandingPage.clickFindMySchoolButton()
    val teacher = enterDomain()
    loginFindSchoolPage.clickToolbarNextMenuItem()
    loginSignInPage.loginAs(teacher)
    return teacher
}


fun TeacherTest.logIn(skipSplash: Boolean = true): CanvasUser {
    val teacher = Data.getNextTeacher()
    activityRule.runOnUiThread {
        activityRule.activity.loginWithToken(
                teacher.token,
                teacher.domain,
                User().apply {
                    id = teacher.id.toLong()
                    name = teacher.name
                    shortName = teacher.shortName
                    avatarUrl = teacher.avatarUrl
                },
                skipSplash
        )
    }
    return teacher
}

fun TeacherTest.tokenLogin(teacher: Teacher, skipSplash: Boolean = true) {
    activityRule.runOnUiThread {
        activityRule.activity.loginWithToken(
                teacher.token,
                teacher.domain,
                User().apply {
                    id = teacher.id.toLong()
                    name = teacher.name
                    shortName = teacher.shortName
                    avatarUrl = teacher.avatarUrl
                },
                skipSplash
        )
    }
}

fun TeacherTest.openOverflowMenu() {
    Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
}

fun TeacherTest.logInAsStudent(): CanvasUser {
    loginLandingPage.clickFindMySchoolButton()
    val student = enterStudentDomain()
    loginFindSchoolPage.clickToolbarNextMenuItem()
    loginSignInPage.loginAs(student)
    return student
}

fun TeacherTest.getNextCourse(): Course {
    return Data.getNextCourse()
}

fun TeacherTest.getAllCourses(): ArrayList<Course> {
    return Data.getAllCourses()
}

fun TeacherTest.getNextAssignment(): Assignment {
    return Data.getNextAssignment()
}

fun TeacherTest.getNextSubmission(): Submission {
    return Data.getNextSubmission()
}

fun TeacherTest.getNextStudent(course: Course): CanvasUser {
    return Data.getNextStudent(course)
}

fun TeacherTest.getNextQuiz(): Quiz {
    return Data.getNextQuiz()
}

fun TeacherTest.getNextConversation(): Conversation {
    return Data.getNextConversation()
}
