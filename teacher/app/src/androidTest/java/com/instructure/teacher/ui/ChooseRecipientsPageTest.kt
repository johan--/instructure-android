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

import com.instructure.teacher.ui.models.Course
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class ChooseRecipientsPageTest: TeacherTest() {
    private lateinit var mCourse: Course

    @Test
    override fun displaysPageObjects() {
        getToChooseRecipients()
        chooseRecipientsPage.assertPageObjects()
    }

    @Test
    fun hasStudentCategory() {
        getToChooseRecipients()
        chooseRecipientsPage.assertHasStudent()
    }

    @Test
    fun addRecipient() {
        getToChooseRecipients()
        val student = getNextStudent(mCourse)
        chooseRecipientsPage.clickStudentCategory()
        chooseRecipientsPage.clickStudent(student)
        chooseRecipientsPage.clickDone()
        addMessagePage.assertHasStudentRecipient(student)
    }

    private fun getToChooseRecipients() {
        logIn()
        mCourse = getNextCourse()
        coursesListPage.clickInboxTab()
        inboxPage.clickAddMessageFAB()
        addMessagePage.clickCourseSpinner()
        addMessagePage.selectCourseFromSpinner(mCourse)
        addMessagePage.clickAddContacts()
    }
}