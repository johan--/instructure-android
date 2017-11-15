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
import com.instructure.canvasapi2.models.Grades;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CourseTest {

    @Test
    public void isStudent_hasStudentEnrollment() {
        Course course = new Course();
        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(true, course.isStudent());
    }

    @Test
    public void isStudent_noStudentEnrollment() {
        Course course = new Course();
        Enrollment enrollment = new Enrollment();
        enrollment.setType("teacher");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(false, course.isStudent());
    }

    @Test
    public void isStudent_noEnrollments() {
        Course course = new Course();
        course.setEnrollments(new ArrayList<Enrollment>());

        assertEquals(false, course.isStudent());
    }

    @Test
    public void isStudent_nullEnrollments() {
        Course course = new Course();
        course.setEnrollments(null);

        assertEquals(false, course.isStudent());
    }

    @Test
    public void isTeacher_hasTeacherEnrollment() {
        Course course = new Course();
        Enrollment enrollment = new Enrollment();
        enrollment.setType("teacher");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(true, course.isTeacher());
    }

    @Test
    public void isTeacher_noTeacherEnrollment() {
        Course course = new Course();
        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(false, course.isTeacher());
    }

    @Test
    public void isTeacher_noEnrollments() {
        Course course = new Course();
        course.setEnrollments(new ArrayList<Enrollment>());

        assertEquals(false, course.isTeacher());
    }

    @Test
    public void isTeacher_nullEnrollments() {
        Course course = new Course();
        course.setEnrollments(null);

        assertEquals(false, course.isTeacher());
    }

    @Test
    public void isTA_hasTaEnrollment() {
        Course course = new Course();
        Enrollment enrollment = new Enrollment();
        enrollment.setType("ta");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(true, course.isTA());
    }

    @Test
    public void isTA_noTaEnrollment() {
        Course course = new Course();
        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(false, course.isTA());
    }

    @Test
    public void isTA_noEnrollments() {
        Course course = new Course();
        course.setEnrollments(new ArrayList<Enrollment>());

        assertEquals(false, course.isTA());
    }

    @Test
    public void isTA_nullEnrollments() {
        Course course = new Course();
        course.setEnrollments(null);

        assertEquals(false, course.isTA());
    }

    @Test
    public void isObserver_hasObserverEnrollment() {
        Course course = new Course();
        Enrollment enrollment = new Enrollment();
        enrollment.setType("observer");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(true, course.isObserver());
    }

    @Test
    public void isObserver_noObserverEnrollment() {
        Course course = new Course();
        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(false, course.isObserver());
    }

    @Test
    public void isObserver_noEnrollments() {
        Course course = new Course();
        course.setEnrollments(new ArrayList<Enrollment>());

        assertEquals(false, course.isObserver());
    }

    @Test
    public void isObserver_nullEnrollments() {
        Course course = new Course();
        course.setEnrollments(null);

        assertEquals(false, course.isObserver());
    }

    @Test
    public void getCurrentScore_withStudentEnrollment() {
        Course course = new Course();

        Grades grades = new Grades();
        grades.setCurrentScore(95.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setGrades(grades);

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(95.0, course.getCurrentScore(), 0.0001);
    }

    @Test
    public void getCurrentScore_withoutStudentEnrollment() {
        Course course = new Course();

        assertEquals(0, course.getCurrentScore(), 0.0001);
    }

    @Test
    public void getCurrentScore_withStudentEnrollmentNoGrades() {
        Course course = new Course();

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(0, course.getCurrentScore(), 0.0001);
    }

    @Test
    public void getCurrentScore_withStudentEnrollmentMultipleGradingPeriods() {
        Course course = new Course();

        Grades grades = new Grades();
        grades.setCurrentScore(50.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setGrades(grades);
        enrollment.setMultipleGradingPeriodsEnabled(true);
        enrollment.setCurrentPeriodComputedCurrentScore(95.0);

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(95.0, course.getCurrentScore(), 0.0001);
    }

    @Test
    public void getCurrentGrade_withStudentEnrollment() {
        Course course = new Course();

        Grades grades = new Grades();
        grades.setCurrentGrade("A");

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setGrades(grades);

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals("A", course.getCurrentGrade());
    }

    @Test
    public void getCurrentGrade_withoutStudentEnrollment() {
        Course course = new Course();

        assertEquals(null, course.getCurrentGrade());
    }

    @Test
    public void getCurrentGrade_withStudentEnrollmentNoGrades() {
        Course course = new Course();

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setComputedCurrentGrade("A");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals("A", course.getCurrentGrade());
    }

    @Test
    public void getCurrentGrade_withStudentEnrollmentMultipleGradingPeriods() {
        Course course = new Course();

        Grades grades = new Grades();
        grades.setCurrentGrade("A");

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setGrades(grades);
        enrollment.setMultipleGradingPeriodsEnabled(true);
        enrollment.setCurrentPeriodComputedCurrentGrade("B+");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals("B+", course.getCurrentGrade());
    }

    @Test
    public void getFinalScore_withStudentEnrollment() {
        Course course = new Course();

        Grades grades = new Grades();
        grades.setFinalScore(95.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setGrades(grades);

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(95.0, course.getFinalScore(), 0.0001);
    }

    @Test
    public void getFinalScore_withoutStudentEnrollment() {
        Course course = new Course();

        assertEquals(0, course.getFinalScore(), 0.0001);
    }

    @Test
    public void getFinalScore_withStudentEnrollmentNoGrades() {
        Course course = new Course();

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(0, course.getFinalScore(), 0.0001);
    }

    @Test
    public void getFinalScore_withStudentEnrollmentMultipleGradingPeriods() {
        Course course = new Course();

        Grades grades = new Grades();
        grades.setFinalScore(50.0);

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setGrades(grades);
        enrollment.setMultipleGradingPeriodsEnabled(true);
        enrollment.setCurrentPeriodComputedFinalScore(95.0);

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals(95.0, course.getFinalScore(), 0.0001);
    }

    @Test
    public void getFinalGrade_withStudentEnrollment() {
        Course course = new Course();

        Grades grades = new Grades();
        grades.setFinalGrade("A");

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setGrades(grades);

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals("A", course.getFinalGrade());
    }

    @Test
    public void getFinalGrade_withoutStudentEnrollment() {
        Course course = new Course();

        assertEquals(null, course.getFinalGrade());
    }

    @Test
    public void getFinalGrade_withStudentEnrollmentNoGrades() {
        Course course = new Course();

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setComputedFinalGrade("A");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals("A", course.getFinalGrade());
    }

    @Test
    public void getFinalGrade_withStudentEnrollmentMultipleGradingPeriods() {
        Course course = new Course();

        Grades grades = new Grades();
        grades.setFinalGrade("A");

        Enrollment enrollment = new Enrollment();
        enrollment.setType("student");
        enrollment.setGrades(grades);
        enrollment.setMultipleGradingPeriodsEnabled(true);
        enrollment.setCurrentPeriodComputedFinalGrade("B+");

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        enrollments.add(enrollment);

        course.setEnrollments(enrollments);

        assertEquals("B+", course.getFinalGrade());
    }

    @Test
    public void addEnrollment() {
        Course course = new Course();
        course.setEnrollments(null);

        Enrollment enrollment = new Enrollment();
        course.addEnrollment(enrollment);

        assertEquals(true, course.getEnrollments().contains(enrollment));
    }

    @Test
    public void licenseToAPIString_all() {
        for (Course.LICENSE license : Course.LICENSE.values()) {
            assertNotEquals("Expected valid API license string for Course.LICENSE." + license.name(), "", Course.licenseToAPIString(license));
        }
    }

    @Test
    public void licenseToAPIString_PRIVATE_COPYRIGHTED() {
        assertEquals("private", Course.licenseToAPIString(Course.LICENSE.PRIVATE_COPYRIGHTED));
    }

    @Test
    public void licenseToAPIString_CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE() {
        assertEquals("cc_by_nc_nd", Course.licenseToAPIString(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE));
    }

    @Test
    public void licenseToAPIString_CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE() {
        assertEquals("c_by_nc_sa", Course.licenseToAPIString(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE));
    }

    @Test
    public void licenseToAPIString_CC_ATTRIBUTION_NON_COMMERCIAL() {
        assertEquals("cc_by_nc", Course.licenseToAPIString(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL));
    }

    @Test
    public void licenseToAPIString_CC_ATTRIBUTION_NO_DERIVATIVE() {
        assertEquals("cc_by_nd", Course.licenseToAPIString(Course.LICENSE.CC_ATTRIBUTION_NO_DERIVATIVE));
    }

    @Test
    public void licenseToAPIString_CC_ATTRIBUTION_SHARE_ALIKE() {
        assertEquals("cc_by_sa", Course.licenseToAPIString(Course.LICENSE.CC_ATTRIBUTION_SHARE_ALIKE));
    }

    @Test
    public void licenseToAPIString_CC_ATTRIBUTION() {
        assertEquals("cc_by", Course.licenseToAPIString(Course.LICENSE.CC_ATTRIBUTION));
    }

    @Test
    public void licenseToAPIString_PUBLIC_DOMAIN() {
        assertEquals("public_domain", Course.licenseToAPIString(Course.LICENSE.PUBLIC_DOMAIN));
    }

    @Test
    public void licenseToAPIString_nullInput() {
        assertEquals(null, Course.licenseToAPIString(null));
    }

    @Test
    public void licenseToPrettyPrint_all() {
        for (Course.LICENSE license : Course.LICENSE.values()) {
            assertNotEquals("Expected valid pretty print string for Course.LICENSE." + license.name(), "", Course.licenseToPrettyPrint(license));
        }
    }

    @Test
    public void licenseToPrettyPrint_PRIVATE_COPYRIGHTED() {
        assertEquals("Private (Copyrighted)", Course.licenseToPrettyPrint(Course.LICENSE.PRIVATE_COPYRIGHTED));
    }

    @Test
    public void licenseToPrettyPrint_CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE() {
        assertEquals("CC Attribution Non-Commercial No Derivatives", Course.licenseToPrettyPrint(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE));
    }

    @Test
    public void licenseToPrettyPrint_CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE() {
        assertEquals("CC Attribution Non-Commercial Share Alike", Course.licenseToPrettyPrint(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE));
    }

    @Test
    public void licenseToPrettyPrint_CC_ATTRIBUTION_NON_COMMERCIAL() {
        assertEquals("CC Attribution Non-Commercial", Course.licenseToPrettyPrint(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL));
    }

    @Test
    public void licenseToPrettyPrint_CC_ATTRIBUTION_NO_DERIVATIVE() {
        assertEquals("CC Attribution No Derivatives", Course.licenseToPrettyPrint(Course.LICENSE.CC_ATTRIBUTION_NO_DERIVATIVE));
    }

    @Test
    public void licenseToPrettyPrint_CC_ATTRIBUTION_SHARE_ALIKE() {
        assertEquals("CC Attribution Share Alike", Course.licenseToPrettyPrint(Course.LICENSE.CC_ATTRIBUTION_SHARE_ALIKE));
    }

    @Test
    public void licenseToPrettyPrint_CC_ATTRIBUTION() {
        assertEquals("CC Attribution", Course.licenseToPrettyPrint(Course.LICENSE.CC_ATTRIBUTION));
    }

    @Test
    public void licenseToPrettyPrint_PUBLIC_DOMAIN() {
        assertEquals("Public Domain", Course.licenseToPrettyPrint(Course.LICENSE.PUBLIC_DOMAIN));
    }

    @Test
    public void getLicense_PRIVATE_COPYRIGHTED() {
        Course course = new Course();
        course.setLicense("private");
        assertEquals(Course.LICENSE.PRIVATE_COPYRIGHTED, course.getLicense());
    }

    @Test
    public void getLicense_CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE() {
        Course course = new Course();
        course.setLicense("cc_by_nc_nd");
        assertEquals(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE, course.getLicense());
    }

    @Test
    public void getLicense_CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE() {
        Course course = new Course();
        course.setLicense("c_by_nc_sa");
        assertEquals(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE, course.getLicense());
    }

    @Test
    public void getLicense_CC_ATTRIBUTION_NON_COMMERCIAL() {
        Course course = new Course();
        course.setLicense("cc_by_nc");
        assertEquals(Course.LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL, course.getLicense());
    }

    @Test
    public void getLicense_CC_ATTRIBUTION_NO_DERIVATIVE() {
        Course course = new Course();
        course.setLicense("cc_by_nd");
        assertEquals(Course.LICENSE.CC_ATTRIBUTION_NO_DERIVATIVE, course.getLicense());
    }

    @Test
    public void getLicense_CC_ATTRIBUTION_SHARE_ALIKE() {
        Course course = new Course();
        course.setLicense("cc_by_sa");
        assertEquals(Course.LICENSE.CC_ATTRIBUTION_SHARE_ALIKE, course.getLicense());
    }

    @Test
    public void getLicense_CC_ATTRIBUTION() {
        Course course = new Course();
        course.setLicense("cc_by");
        assertEquals(Course.LICENSE.CC_ATTRIBUTION, course.getLicense());
    }

    @Test
    public void getLicense_PUBLIC_DOMAIN() {
        Course course = new Course();
        course.setLicense("public_domain");
        assertEquals(Course.LICENSE.PUBLIC_DOMAIN, course.getLicense());
    }

    @Test
    public void getLicense_empty() {
        Course course = new Course();
        course.setLicense("");
        assertEquals(Course.LICENSE.PRIVATE_COPYRIGHTED, course.getLicense());
    }

    @Test
    public void getLicense_all() {
        for (Course.LICENSE license : Course.LICENSE.values()) {
            Course course = new Course();
            course.setLicense(Course.licenseToAPIString(license));
            assertEquals(license, course.getLicense());
        }
    }

}