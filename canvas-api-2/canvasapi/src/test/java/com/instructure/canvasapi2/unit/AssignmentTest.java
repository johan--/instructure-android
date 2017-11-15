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

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.LockInfo;
import com.instructure.canvasapi2.models.LockedModule;
import com.instructure.canvasapi2.models.RubricCriterion;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.utils.APIHelper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AssignmentTest {

    @Test
    public void getLastActualSubmission_TestNullSubmission() {
        Assignment assignment = new Assignment();
        assignment.setSubmission(null);

        assertEquals(null, assignment.getLastActualSubmission());
    }

    @Test
    public void getLastActualSubmission_TestNullWorkFlow() {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();
        submission.setWorkflowState(null);
        assignment.setSubmission(submission);

        assertEquals(null, assignment.getLastActualSubmission());
    }

    @Test
    public void getLastActualSubmission_TestWorkFlowSubmitted() {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();
        submission.setWorkflowState("submitted");
        assignment.setSubmission(submission);

        assertEquals(submission, assignment.getLastActualSubmission());
    }

    @Test
    public void isAllowedToSubmit_TestTrue() {
        Assignment assignment = new Assignment();
        List<String> submissionTypeList = new ArrayList<>();
        submissionTypeList.add(Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.DISCUSSION_TOPIC));
        submissionTypeList.add(Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_URL));
        assignment.setSubmissionTypes(submissionTypeList);
        assignment.setLockedForUser(false);

        assertEquals(true, assignment.isAllowedToSubmit());
    }

    @Test
    public void isAllowedToSubmit_TestFalse() {
        Assignment assignment = new Assignment();
        List<String> submissionTypeList = new ArrayList<>();
        submissionTypeList.add(Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ATTENDANCE));
        submissionTypeList.add(Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ));
        assignment.setSubmissionTypes(submissionTypeList);
        assignment.setLockedForUser(true);

        assertEquals(false, assignment.isAllowedToSubmit());
    }

    @Test
    public void isWithoutGradedSubmission_TestTrueNotNull() {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();
        submission.setWorkflowState("submitted");
        submission.setGrade(null);
        submission.setSubmissionType(null);
        assignment.setSubmission(submission);

        assertEquals(true, assignment.isWithoutGradedSubmission());
    }

    @Test
    public void isWithoutGradedSubmission_TestTrueNull() {
        Assignment assignment = new Assignment();
        assignment.setSubmission(null);

        assertEquals(true, assignment.isWithoutGradedSubmission());
    }

    @Test
    public void isWithoutGradedSubmission_TestFalse() {
        Assignment assignment = new Assignment();
        Submission submission = new Submission();
        submission.setWorkflowState("submitted");
        submission.setGrade("A");
        submission.setSubmissionType("Online_Quiz");
        assignment.setSubmission(submission);

        assertEquals(false, assignment.isWithoutGradedSubmission());
    }

    //region isLocked
    @Test
    public void isLocked_TestNullLockInfo() {
        Assignment assignment = new Assignment();
        assignment.setLockInfo(null);

        assertEquals(false, assignment.isLocked());
    }

    @Test
    public void isLocked_TestEmptyLockInfo() {
        Assignment assignment = new Assignment();
        LockInfo lockInfo = new LockInfo();
        assignment.setLockInfo(lockInfo);

        assertEquals(false, assignment.isLocked());
    }

    @Test
    public void isLocked_TestLockedModuleName() {
        Assignment assignment = new Assignment();
        LockInfo lockInfo = new LockInfo();
        LockedModule lockedModule = new LockedModule();
        lockedModule.setName("Hodor");
        lockInfo.setContextModule(lockedModule);
        assignment.setLockInfo(lockInfo);

        assertEquals(true, assignment.isLocked());
    }

    @Test
    public void isLocked_TestUnlockAfterCurrentDate() {
        Assignment assignment = new Assignment();
        LockInfo lockInfo = new LockInfo();
        long time = Calendar.getInstance().getTimeInMillis() + 100000;
        Date date = new Date(time);
        lockInfo.setUnlockAt(APIHelper.dateToString(date));
        assignment.setLockInfo(lockInfo);

        assertEquals(true, assignment.isLocked());
    }
    //endregion

    @Test
    public void hasRubric_TestTrue() {
        Assignment assignment = new Assignment();
        List<RubricCriterion> rubricCriterionList = new ArrayList<>();
        rubricCriterionList.add(new RubricCriterion());
        assignment.setRubric(rubricCriterionList);

        assertEquals(true, assignment.hasRubric());
    }

    @Test
    public void hasRubric_TestFalse() {
        Assignment assignment = new Assignment();
        assignment.setRubric(null);

        assertEquals(false, assignment.hasRubric());
    }

    //region submissionTypeToAPIString
    @Test
    public void submissionTypeToAPIString_TestOnlineQuiz() {
        assertEquals("online_quiz",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ));
    }

    @Test
    public void submissionTypeToAPIString_TestNone() {
        assertEquals("none",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.NONE));
    }

    @Test
    public void submissionTypeToAPIString_TestOnPaper() {
        assertEquals("on_paper",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ON_PAPER));
    }

    @Test
    public void submissionTypeToAPIString_TestDiscussionTopic() {
        assertEquals("discussion_topic",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.DISCUSSION_TOPIC));
    }

    @Test
    public void submissionTypeToAPIString_TestExternalTool() {
        assertEquals("external_tool",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.EXTERNAL_TOOL));
    }

    @Test
    public void submissionTypeToAPIString_TestOnlineUpload() {
        assertEquals("online_upload",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_UPLOAD));
    }

    @Test
    public void submissionTypeToAPIString_TestOnlineTextEntry() {
        assertEquals("online_text_entry",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_TEXT_ENTRY));
    }

    @Test
    public void submissionTypeToAPIString_TestOnlineUrl() {
        assertEquals("online_url",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ONLINE_URL));
    }

    @Test
    public void submissionTypeToAPIString_TestMediaRecording() {
        assertEquals("media_recording",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.MEDIA_RECORDING));
    }

    @Test
    public void submissionTypeToAPIString_TestAttendance() {
        assertEquals("attendance",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.ATTENDANCE));
    }

    @Test
    public void submissionTypeToAPIString_TestNotGraded() {
        assertEquals("not_graded",
                Assignment.submissionTypeToAPIString(Assignment.SUBMISSION_TYPE.NOT_GRADED));
    }
    //endregion

    //region getGradingTypeFromAPIString
    @Test
    public void getGradingTypeFromAPIString_TestPassFail() {
        assertEquals(Assignment.GRADING_TYPE.PASS_FAIL,
                Assignment.getGradingTypeFromAPIString("pass_fail"));
    }

    @Test
    public void getGradingTypeFromAPIString_TestPercent() {
        assertEquals(Assignment.GRADING_TYPE.PERCENT,
                Assignment.getGradingTypeFromAPIString("percent"));
    }

    @Test
    public void getGradingTypeFromAPIString_TestLetterGrade() {
        assertEquals(Assignment.GRADING_TYPE.LETTER_GRADE,
                Assignment.getGradingTypeFromAPIString("letter_grade"));
    }

    @Test
    public void getGradingTypeFromAPIString_TestPoints() {
        assertEquals(Assignment.GRADING_TYPE.POINTS,
                Assignment.getGradingTypeFromAPIString("points"));
    }

    @Test
    public void getGradingTypeFromAPIString_TestGPAScale() {
        assertEquals(Assignment.GRADING_TYPE.GPA_SCALE,
                Assignment.getGradingTypeFromAPIString("gpa_scale"));
    }

    @Test
    public void getGradingTypeFromAPIString_TestNotGraded() {
        assertEquals(Assignment.GRADING_TYPE.NOT_GRADED,
                Assignment.getGradingTypeFromAPIString("not_graded"));
    }
    //endregion


    //region gradingTypeToAPIString
    @Test
    public void gradingTypeToAPIString_TestPassFail() {
        assertEquals("pass_fail",
                Assignment.gradingTypeToAPIString(Assignment.GRADING_TYPE.PASS_FAIL));
    }

    @Test
    public void gradingTypeToAPIString_TestPercent() {
        assertEquals("percent",
                Assignment.gradingTypeToAPIString(Assignment.GRADING_TYPE.PERCENT));
    }

    @Test
    public void gradingTypeToAPIString_TestLetterGrade() {
        assertEquals("letter_grade",
                Assignment.gradingTypeToAPIString(Assignment.GRADING_TYPE.LETTER_GRADE));
    }

    @Test
    public void gradingTypeToAPIString_TestPoints() {
        assertEquals("points",
                Assignment.gradingTypeToAPIString(Assignment.GRADING_TYPE.POINTS));
    }

    @Test
    public void gradingTypeToAPIString_TestGPAScale() {
        assertEquals("gpa_scale",
                Assignment.gradingTypeToAPIString(Assignment.GRADING_TYPE.GPA_SCALE));
    }

    @Test
    public void gradingTypeToAPIString_TestNotGraded() {
        assertEquals("not_graded",
                Assignment.gradingTypeToAPIString(Assignment.GRADING_TYPE.NOT_GRADED));
    }
    //endregion

}
