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

package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.models.Enrollment;
import com.instructure.canvasapi2.models.Grades;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnrollmentTest {

    @Test
    public void isStudent_Student() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        assertEquals(true, enrollment.isStudent());
    }

    @Test
    public void isStudent_StudentEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("studentenrollment");
        assertEquals(true, enrollment.isStudent());
    }

    @Test
    public void isTeacher_Teacher() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("teacher");
        assertEquals(true, enrollment.isTeacher());
    }

    @Test
    public void isTeacher_TeacherEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("teacherenrollment");
        assertEquals(true, enrollment.isTeacher());
    }

    @Test
    public void isObserver_Observer() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("observer");
        assertEquals(true, enrollment.isObserver());
    }

    @Test
    public void isObserver_ObserverEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("observerenrollment");
        assertEquals(true, enrollment.isObserver());
    }

    @Test
    public void isTa_Ta() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("ta");
        assertEquals(true, enrollment.isTA());
    }

    @Test
    public void isTa_TaEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("taenrollment");
        assertEquals(true, enrollment.isTA());
    }

    @Test
    public void getType() {
        Enrollment enrollment = new Enrollment();
        enrollment.setType("studentenrollment");
        assertEquals("student", enrollment.getType());
    }

    @Test
    public void getCurrentScore() {
        Grades grades = new Grades();
        grades.setCurrentScore(95.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setComputedCurrentScore(50.0);
        enrollment.setGrades(grades);

        assertEquals(95.0, enrollment.getCurrentScore(), 0.001);
    }

    @Test
    public void getCurrentScore_NullGrades() {
        Enrollment enrollment = new Enrollment();
        enrollment.setComputedCurrentScore(50.0);
        enrollment.setGrades(null);

        assertEquals(50.0, enrollment.getCurrentScore(), 0.001);
    }

    @Test
    public void getFinalScore() {
        Grades grades = new Grades();
        grades.setFinalScore(95.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setComputedFinalScore(50.0);
        enrollment.setGrades(grades);

        assertEquals(95.0, enrollment.getFinalScore(), 0.001);
    }

    @Test
    public void getFinalScore_NullGrades() {
        Enrollment enrollment = new Enrollment();
        enrollment.setComputedFinalScore(50.0);
        enrollment.setGrades(null);

        assertEquals(50.0, enrollment.getFinalScore(), 0.001);
    }

    @Test
    public void getCurrentGrade() {
        Grades grades = new Grades();
        grades.setCurrentGrade("B+");

        Enrollment enrollment = new Enrollment();
        enrollment.setComputedCurrentGrade("C-");
        enrollment.setGrades(grades);

        assertEquals("B+", enrollment.getCurrentGrade());
    }

    @Test
    public void getCurrentGrade_NullGrades() {
        Enrollment enrollment = new Enrollment();
        enrollment.setComputedCurrentGrade("C-");
        enrollment.setGrades(null);

        assertEquals("C-", enrollment.getCurrentGrade());
    }

    @Test
    public void getFinalGrade() {
        Grades grades = new Grades();
        grades.setFinalGrade("B+");

        Enrollment enrollment = new Enrollment();
        enrollment.setComputedFinalGrade("C-");
        enrollment.setGrades(grades);

        assertEquals("B+", enrollment.getFinalGrade());
    }

    @Test
    public void getFinalGrade_NullGrades() {
        Enrollment enrollment = new Enrollment();
        enrollment.setComputedFinalGrade("C-");
        enrollment.setGrades(null);

        assertEquals("C-", enrollment.getFinalGrade());
    }

}