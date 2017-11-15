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

package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Enrollment;
import com.instructure.canvasapi2.models.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UserTest {

    private User user1;
    private User user2;

    @Before
    public void setup() {
        user1 = new User();
        user2 = new User();
    }

    //region equals
    @Test
    public void equals_TestSelfTrue() {
        assertEquals(true, user1.equals(user1));
    }

    @Test
    public void equals_TestFalse1() {
        assertEquals(false, user1.equals(null));
    }

    @Test
    public void equals_TestFalse2() {
        Course course = new Course();

        assertEquals(false, user1.equals(course));
    }

    @Test
    public void equals_TestFalse3() {
        user1.setId(1234);
        user2.setId(4321);

        assertEquals(false, user1.equals(user2));
    }

    @Test
    public void equals_TestTrueIdMatch() {
        user1.setId(1234);
        user2.setId(1234);

        assertEquals(true, user1.equals(user2));
    }
    //endregion


    @Test
    public void getEnrollmentsHash_Test1() {
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourseId(1234);
        enrollment1.setRole("student");

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCourseId(1234);
        enrollment2.setRole("teacher");

        List<Enrollment> enrollmentList = new ArrayList<>();
        enrollmentList.add(enrollment1);
        enrollmentList.add(enrollment2);
        user1.setEnrollments(enrollmentList);
        user1.getEnrollmentsHash();

        assertEquals("student", user1.getEnrollmentsHash().get("1234")[0]);
    }

    @Test
    public void getEnrollmentsHash_Test2() {
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourseId(1234);
        enrollment1.setRole("student");

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCourseId(1234);
        enrollment2.setRole("teacher");

        List<Enrollment> enrollmentList = new ArrayList<>();
        enrollmentList.add(enrollment1);
        enrollmentList.add(enrollment2);
        user1.setEnrollments(enrollmentList);
        user1.getEnrollmentsHash();

        assertEquals("teacher", user1.getEnrollmentsHash().get("1234")[1]);
    }
}
