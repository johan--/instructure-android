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

package com.instructure.pandautils.unit;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.pandautils.utils.AssignmentUtils;
import com.instructure.pandautils.utils.AssignmentUtils2;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class AssignmentUtils2Test {

    @Test
    public void getAssignmentState_unknownStateNullAssignment() throws Exception {
        Assignment assignment = null;
        Submission submission = new Submission();

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_UNKNOWN);
    }

    @Test
    public void getAssignmentState_stateDue() throws Exception {
        Assignment assignment = new Assignment();
        Submission submission = null;

        long time = Calendar.getInstance().getTimeInMillis() + 100000;
        Date date = new Date(time);

        assignment.setSubmission(submission);
        assignment.setDueAt(APIHelper.dateToString(date));

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_DUE);
    }

    @Test
    public void getAssignmentState_stateInClass() throws Exception {
        Assignment assignment = new Assignment();
        Submission submission = null;

        long time = Calendar.getInstance().getTimeInMillis() + 100000;
        Date date = new Date(time);
        ArrayList<String> submissionTypes = new ArrayList<>();
        submissionTypes.add(Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ON_PAPER));

        assignment.setSubmission(submission);
        assignment.setDueAt(APIHelper.dateToString(date));
        assignment.setSubmissionTypes(submissionTypes);

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_IN_CLASS);
    }

    @Test
    public void getAssignmentState_stateMissingNullDueDate() throws Exception {
        Assignment assignment = new Assignment();
        Submission submission = null;
        assignment.setSubmission(submission);
        assignment.setDueAt(null);

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_MISSING);
    }

    @Test
    public void getAssignmentState_stateSubmitted() throws Exception {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();

        long time = Calendar.getInstance().getTimeInMillis() + 100000;
        Date date = new Date(time);

        submission.setAttempt(1);
        assignment.setSubmission(submission);
        assignment.setDueAt(APIHelper.dateToString(date));

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED);
    }

    @Test
    public void getAssignmentState_stateGraded() throws Exception {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();

        long time = Calendar.getInstance().getTimeInMillis() + 100000;
        Date date = new Date(time);

        submission.setAttempt(1);
        submission.setGrade("A");
        assignment.setSubmission(submission);
        assignment.setDueAt(APIHelper.dateToString(date));

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_GRADED);
    }

    @Test
    public void getAssignmentState_stateSubmittedLate() throws Exception {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();

        long time = Calendar.getInstance().getTimeInMillis() + 100000;
        Date date = new Date(time);

        submission.setAttempt(1);
        submission.setLate(true);
        assignment.setSubmission(submission);
        assignment.setDueAt(APIHelper.dateToString(date));

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE);
    }

    @Test
    public void getAssignmentState_stateGradedLate() throws Exception {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();

        long time = Calendar.getInstance().getTimeInMillis() + 100000;
        Date date = new Date(time);

        submission.setAttempt(1);
        submission.setGrade("A");
        submission.setLate(true);
        assignment.setSubmission(submission);
        assignment.setDueAt(APIHelper.dateToString(date));

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE);
    }

    @Test
    public void getAssignmentState_stateExcused() throws Exception {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();

        submission.setExcused(true);
        assignment.setSubmission(submission);

        int testValue = AssignmentUtils2.getAssignmentState(assignment, submission);

        assertEquals("", testValue, AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED);
    }

}