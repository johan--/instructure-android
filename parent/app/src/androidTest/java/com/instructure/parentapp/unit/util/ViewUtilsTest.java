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
package com.instructure.parentapp.unit.util;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.test.InstrumentationRegistry;
import android.text.Spannable;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.pandautils.utils.AssignmentUtils2;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.ViewUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.instructure.canvasapi2.models.Assignment.submissionTypeToAPIString;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ViewUtilsTest {

    private Context mMockContext;

    @Before
    public void setUp() throws Exception {
        mMockContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void applyKerning() throws Exception {
        final String str = "George was a curious monkey";
        Spannable spannable = ViewUtils.applyKerning(str, 1F);
        assertNotEquals(str, spannable.toString());
    }

    @Test
    public void getAssignmentIcon_ONLINE_QUIZ() throws Exception {
        Assignment assignment = new Assignment();
        List<String> types = new ArrayList<>();

        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ));
        assignment.setSubmissionTypes(types);
        @DrawableRes int iconRes = ViewUtils.getAssignmentIcon(assignment);
        assertTrue(iconRes == R.drawable.ic_cv_quizzes_fill);
    }

    @Test
    public void getAssignmentIcon_DISCUSSION_TOPIC() throws Exception {
        Assignment assignment = new Assignment();
        List<String> types = new ArrayList<>();

        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.DISCUSSION_TOPIC));
        assignment.setSubmissionTypes(types);
        @DrawableRes int iconRes = ViewUtils.getAssignmentIcon(assignment);
        assertTrue(iconRes == R.drawable.ic_cv_discussions_fill);
    }

    @Test
    public void getAssignmentIcon_OTHERS() throws Exception {
        Assignment assignment = new Assignment();
        List<String> types = new ArrayList<>();

        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.NONE));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ON_PAPER));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.EXTERNAL_TOOL));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_UPLOAD));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_TEXT_ENTRY));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_URL));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.MEDIA_RECORDING));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ATTENDANCE));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.NOT_GRADED));
        assignment.setSubmissionTypes(types);
        @DrawableRes int iconRes = ViewUtils.getAssignmentIcon(assignment);
        assertTrue(iconRes == R.drawable.ic_cv_assignments_fill);
    }

    @Test
    public void getAssignmentIconOrder() throws Exception {
        /**
         * Tests the order that the icons are retrieved.
         * Quizzes, Discussion Topics, Assignment icons in that order
         *
         * To properly test the order we need to test the method 3 times for each icon type
         */

        Assignment assignment = new Assignment();
        List<String> types = new ArrayList<>();
        @DrawableRes int iconRes = 0;

        //Add all other submission types
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.NONE));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ON_PAPER));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.EXTERNAL_TOOL));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_UPLOAD));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_TEXT_ENTRY));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_URL));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.MEDIA_RECORDING));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ATTENDANCE));
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.NOT_GRADED));
        assignment.setSubmissionTypes(types);
        iconRes = ViewUtils.getAssignmentIcon(assignment);
        assertTrue(iconRes == R.drawable.ic_cv_assignments_fill);

        //Add the one we want to test
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.DISCUSSION_TOPIC));
        assignment.setSubmissionTypes(types);
        iconRes = ViewUtils.getAssignmentIcon(assignment);
        assertTrue(iconRes == R.drawable.ic_cv_discussions_fill);

        //Add the next one we want to test
        types.add(submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ));
        assignment.setSubmissionTypes(types);
        iconRes = ViewUtils.getAssignmentIcon(assignment);
        assertTrue(iconRes == R.drawable.ic_cv_quizzes_fill);
    }

    @Test
    public void getGradeText() throws Exception {
        double score = 2.0;
        int pointsPossible = 100;
        final String graded = mMockContext.getResources().getString(R.string.submitted);

        //Test Graded
        String gradedText = ViewUtils.getGradeText(AssignmentUtils2.ASSIGNMENT_STATE_GRADED, score, pointsPossible, mMockContext);
        assertTrue(gradedText.contains(graded));
    }

    @Test
    public void getLateText() throws Exception {
        double score = 2.0;
        int pointsPossible = 100;
        final String late = mMockContext.getResources().getString(R.string.late);

        ArrayList<Integer> lateAssignmentStates = new ArrayList<>();
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_DROPPED);
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_DUE);
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED);
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE);
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_IN_CLASS);
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_MISSING);
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED);
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE);
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_UNKNOWN);

        //Test Late
        for (Integer state : lateAssignmentStates) {
            assertTrue(ViewUtils.getGradeText(state, score, pointsPossible, mMockContext).contains(late));
        }
    }
}