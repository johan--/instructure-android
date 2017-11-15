/*
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

package com.instructure.canvasapi2.models;

import android.os.Parcel;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi2.R;
import com.instructure.canvasapi2.utils.APIHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Quiz extends CanvasModel<Quiz> {

    public final static String TYPE_PRACTICE = "practice_quiz";
    public final static String TYPE_ASSIGNMENT = "assignment";
    public final static String TYPE_GRADED_SURVEY = "graded_survey";
    public final static String TYPE_SURVEY = "survey";

    public static final String KEEP_AVERAGE = "keep_average";
    public static final String KEEP_HIGHEST = "keep_highest";

    public enum HIDE_RESULTS_TYPE { NULL, ALWAYS, AFTER_LAST_ATTEMPT }

    // Settings
    public enum SettingTypes{
        QUIZ_TYPE, POINTS, ASSIGNMENT_GROUP, SHUFFLE_ANSWERS, TIME_LIMIT,
        MULTIPLE_ATTEMPTS, SCORE_TO_KEEP, ATTEMPTS, VIEW_RESPONSES,
        SHOW_CORRECT_ANSWERS, ACCESS_CODE, IP_FILTER, ONE_QUESTION_AT_A_TIME,
        LOCK_QUESTIONS_AFTER_ANSWERING, ANONYMOUS_SUBMISSIONS
    }

    // API variables
    private long id;
    private String title;
    @SerializedName("mobile_url")
    private String mobileUrl;
    @SerializedName("html_url")
    private String htmlUrl;
    private String description;
    @SerializedName("quiz_type")
    private String quizType;
    @SerializedName("assignment_group_id")
    private long assignmentGroupId;
    @SerializedName("lock_info")
    private LockInfo lockInfo;
    private QuizPermission permissions;
    @SerializedName("allowed_attempts")
    private int allowedAttempts;
    @SerializedName("question_count")
    private int questionCount;
    @SerializedName("points_possible")
    private String pointsPossible;
    @SerializedName("cant_go_back")
    private boolean lockQuestionsAfterAnswering;
    @SerializedName("due_at")
    private String dueAt;
    @SerializedName("time_limit")
    private int timeLimit;
    @SerializedName("shuffle_answers")
    private boolean shuffleAnswers;
    @SerializedName("show_correct_answers")
    private boolean showCorrectAnswers;
    @SerializedName("scoring_policy")
    private String scoringPolicy;
    @SerializedName("access_code")
    private String accessCode;
    @SerializedName("ip_filter")
    private String ipFilter;
    @SerializedName("locked_for_user")
    private boolean lockedForUser;
    @SerializedName("lock_explanation")
    private String lockExplanation;
    @SerializedName("hide_results")
    private String hideResults;
    @SerializedName("show_correct_answers_at")
    private String showCorrectAnswersAt;
    @SerializedName("hide_correct_answers_at")
    private String hideCorrectAnswersAt;
    @SerializedName("unlock_at")
    private String unlockAt;
    @SerializedName("one_time_results")
    private boolean oneTimeResults;
    @SerializedName("lock_at")
    private String lockAt;
    @SerializedName("question_types")
    private List<String> questionTypes = new ArrayList<>();
    @SerializedName("has_access_code")
    private boolean hasAccessCode;
    @SerializedName("one_question_at_a_time")
    private boolean oneQuestionAtATime;
    @SerializedName("require_lockdown_broswer")
    private boolean requireLockdownBrowser;
    @SerializedName("require_lockdown_browser_for_results")
    private boolean requireLockdownBrowserForResults;
    @SerializedName("anonymous_submissions")
    private boolean allowAnonymousSubmissions;
    private boolean published;
    @SerializedName("assignment_id")
    private long assignmentId;
    @SerializedName("all_dates")
    private List<AssignmentDueDate> allDates = new ArrayList<>();
    @SerializedName("only_visible_to_overrides")
    private boolean onlyVisibleToOverrides;
    private boolean unpublishable;

    // Helper variables
    private Assignment assignment;
    private AssignmentGroup assignmentGroup;
    private List<QuizOverride> overrides;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {

        if (mobileUrl != null && !mobileUrl.equals("")) {
            return mobileUrl;
        }
        return htmlUrl;
    }

    public String getMobileUrl() {
        return mobileUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuizType() {
        return quizType;
    }

    public void setQuizType(String quizType) {
        this.quizType = quizType;
    }

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public void setLockInfo(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    public QuizPermission getPermissions() {
        return permissions;
    }

    public void setPermissions(QuizPermission permissions) {
        this.permissions = permissions;
    }

    public int getAllowedAttempts() {
        return allowedAttempts;
    }

    public void setAllowedAttempts(int allowedAttempts) {
        this.allowedAttempts = allowedAttempts;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    @Nullable
    public String getPointsPossible() {
        return pointsPossible;
    }

    public void setPointsPossible(@Nullable String pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    @Nullable
    public Date getDueAt() {
        return APIHelper.stringToDate(dueAt);
    }

    public void setDueAt(String dueAt) {
        this.dueAt = dueAt;
    }

    @Nullable
    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(@Nullable int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    @Nullable
    public String getIpFilter() {
        return ipFilter;
    }

    public void setIpFilter(String ipFilter) {
        this.ipFilter = ipFilter;
    }

    public boolean isLockedForUser() {
        return lockedForUser;
    }

    public void setLockedForUser(boolean lockedForUser) {
        this.lockedForUser = lockedForUser;
    }

    public String getLockExplanation() {
        return lockExplanation;
    }

    public void setLockExplanation(String lockExplanation) {
        this.lockExplanation = lockExplanation;
    }

    public HIDE_RESULTS_TYPE getHideResults() {
        if(hideResults == null || hideResults.equals("null")) {
            return HIDE_RESULTS_TYPE.NULL;
        } else if(hideResults.equals("always")) {
            return HIDE_RESULTS_TYPE.ALWAYS;
        } else if(hideResults.equals("until_after_last_attempt")) {
            return HIDE_RESULTS_TYPE.AFTER_LAST_ATTEMPT;
        }
        return HIDE_RESULTS_TYPE.NULL;
    }

    public int getHideResultsStringResource() {
        if ("always".equals(hideResults))
            return R.string.no;
        return R.string.always;
    }

    public boolean getIsGradeable() {
        return TYPE_ASSIGNMENT.equals(this.quizType) || TYPE_GRADED_SURVEY.equals(this.quizType);
    }

    public void setHideResults(String hideResults) {
        this.hideResults = hideResults;
    }

    @Nullable
    public Date getUnlockAt() {
        return APIHelper.stringToDate(unlockAt);
    }

    public void setUnlockAt(String unlockAt) {
        this.unlockAt = unlockAt;
    }

    public boolean isOneTimeResults() {
        return oneTimeResults;
    }

    public void setOneTimeResults(boolean oneTimeResults) {
        this.oneTimeResults = oneTimeResults;
    }

    @Nullable
    public Date getLockAt() {
        return APIHelper.stringToDate(lockAt);
    }

    public void setLockAt(String lockAt) {
        this.lockAt = lockAt;
    }

    public List<String> getQuestionTypes() {
        return questionTypes;
    }

    public ArrayList<QuizQuestion.QUESTION_TYPE> getParsedQuestionTypes() {
        return parseQuestionTypes(questionTypes);
    }

    private ArrayList<QuizQuestion.QUESTION_TYPE> parseQuestionTypes(List<String> question_types) {
        ArrayList<QuizQuestion.QUESTION_TYPE> questionTypesList = new ArrayList<>();
        for(String question_type : question_types) {
            if(question_type != null) {
                questionTypesList.add(QuizQuestion.parseQuestionType(question_type));
            }
        }

        return questionTypesList;
    }

    public void setQuestionTypes(List<String> questionTypes) {
        this.questionTypes = questionTypes;
    }

    public boolean isHasAccessCode() {
        return hasAccessCode;
    }

    public void setHasAccessCode(boolean hasAccessCode) {
        this.hasAccessCode = hasAccessCode;
    }

    public boolean getOneQuestionAtATime() {
        return oneQuestionAtATime;
    }

    public void setOneQuestionAtATime(boolean oneQuestionAtATime) {
        this.oneQuestionAtATime = oneQuestionAtATime;
    }

    public boolean isRequireLockdownBrowser() {
        return requireLockdownBrowser;
    }

    public void setRequireLockdownBrowser(boolean requireLockdownBrowser) {
        this.requireLockdownBrowser = requireLockdownBrowser;
    }

    public boolean isRequireLockdownBrowserForResults() {
        return requireLockdownBrowserForResults;
    }

    public void setRequireLockdownBrowserForResults(boolean requireLockdownBrowserForResults) {
        this.requireLockdownBrowserForResults = requireLockdownBrowserForResults;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public List<AssignmentDueDate> getAllDates() {
        return allDates;
    }

    public void setAllDates(List<AssignmentDueDate> allDates) {
        this.allDates = allDates;
    }

    public @Nullable Date getLockAtDate() {
        return APIHelper.stringToDate(lockAt);
    }

    public @Nullable Date getUnlockAtDate() {
        return APIHelper.stringToDate(unlockAt);
    }

    public boolean isUnpublishable() {
        return unpublishable;
    }

    public void setUnpublishable(boolean unpublishable) {
        this.unpublishable = unpublishable;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public boolean getShuffleAnswers() {
        return shuffleAnswers;
    }

    public void setShuffleAnswers(boolean shuffleAnswers) {
        this.shuffleAnswers = shuffleAnswers;
    }

    public boolean isOnlyVisibleToOverrides() {
        return onlyVisibleToOverrides;
    }

    public void setOnlyVisibleToOverrides(boolean onlyVisibleToOverrides) {
        this.onlyVisibleToOverrides = onlyVisibleToOverrides;
    }

    public int getScoringPolicy() {
        if (KEEP_HIGHEST.equals(scoringPolicy)) {
            return R.string.quiz_scoring_policy_highest;
        } else if (KEEP_AVERAGE.equals(scoringPolicy)) {
            return R.string.quiz_scoring_policy_average;
        }

        return R.string.quiz_scoring_policy_latest;
    }

    public void setScoringPolicy(String scoringPolicy) {
        this.scoringPolicy = scoringPolicy;
    }

    public boolean getShowCorrectAnswers() {
        return showCorrectAnswers;
    }

    public void setShowCorrectAnswers(boolean showCorrectAnswers) {
        this.showCorrectAnswers = showCorrectAnswers;
    }

    @Nullable
    public AssignmentGroup getAssignmentGroup() {
        return assignmentGroup;
    }

    public void setAssignmentGroup(AssignmentGroup assignmentGroup) {
        this.assignmentGroup = assignmentGroup;
    }

    public long getAssignmentGroupId() {
        return assignmentGroupId;
    }

    public void setAssignmentGroupId(long assignmentGroupId) {
        this.assignmentGroupId = assignmentGroupId;
    }

    @Nullable
    public Date getShowCorrectAnswersAt() {
        return APIHelper.stringToDate(showCorrectAnswersAt);
    }

    public void setShowCorrectAnswersAt(String showCorrectAnswersAt) {
        this.showCorrectAnswersAt = showCorrectAnswersAt;
    }

    @Nullable
    public Date getHideCorrectAnswersAt() {
        return APIHelper.stringToDate(hideCorrectAnswersAt);
    }

    public void setHideCorrectAnswersAt(String hideCorrectAnswersAt) {
        this.hideCorrectAnswersAt = hideCorrectAnswersAt;
    }

    public boolean isLockQuestionsAfterAnswering() {
        return lockQuestionsAfterAnswering;
    }

    public void setLockQuestionsAfterAnswering(boolean lockQuestionsAfterAnswering) {
        this.lockQuestionsAfterAnswering = lockQuestionsAfterAnswering;
    }

    public boolean getAllowAnonymousSubmissions() {
        return allowAnonymousSubmissions;
    }

    public void setAllowAnonymousSubmissions(boolean allowAnonymousSubmissions) {
        this.allowAnonymousSubmissions = allowAnonymousSubmissions;
    }
    public List<QuizOverride> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<QuizOverride> overrides) {
        this.overrides = overrides;
    }

    public long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return APIHelper.stringToDate(dueAt);
    }

    @Nullable
    @Override
    public String getComparisonString() {
        if (getAssignment() != null) {
            return getAssignment().getName();
        }
        return getTitle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.mobileUrl);
        dest.writeString(this.htmlUrl);
        dest.writeString(this.description);
        dest.writeString(this.quizType);
        dest.writeParcelable(this.lockInfo, flags);
        dest.writeParcelable(this.permissions, flags);
        dest.writeInt(this.allowedAttempts);
        dest.writeInt(this.questionCount);
        dest.writeString(this.pointsPossible);
        dest.writeByte(this.lockQuestionsAfterAnswering ? (byte) 1 : (byte) 0);
        dest.writeString(this.dueAt);
        dest.writeInt(this.timeLimit);
        dest.writeByte(this.shuffleAnswers ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showCorrectAnswers ? (byte) 1 : (byte) 0);
        dest.writeString(this.scoringPolicy);
        dest.writeString(this.accessCode);
        dest.writeString(this.ipFilter);
        dest.writeByte(this.lockedForUser ? (byte) 1 : (byte) 0);
        dest.writeString(this.lockExplanation);
        dest.writeString(this.hideResults);
        dest.writeString(this.showCorrectAnswersAt);
        dest.writeString(this.hideCorrectAnswersAt);
        dest.writeString(this.unlockAt);
        dest.writeByte(this.oneTimeResults ? (byte) 1 : (byte) 0);
        dest.writeString(this.lockAt);
        dest.writeStringList(this.questionTypes);
        dest.writeByte(this.hasAccessCode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.oneQuestionAtATime ? (byte) 1 : (byte) 0);
        dest.writeByte(this.requireLockdownBrowser ? (byte) 1 : (byte) 0);
        dest.writeByte(this.requireLockdownBrowserForResults ? (byte) 1 : (byte) 0);
        dest.writeByte(this.allowAnonymousSubmissions ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.assignment, flags);
        dest.writeByte(this.published ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.allDates);
        dest.writeLong(this.assignmentGroupId);
        dest.writeParcelable(this.assignmentGroup, flags);
        dest.writeByte(this.onlyVisibleToOverrides ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.overrides);
        dest.writeLong(this.assignmentId);
        dest.writeByte(this.unpublishable ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.assignment, flags);
    }

    public Quiz() {
    }

    protected Quiz(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.mobileUrl = in.readString();
        this.htmlUrl = in.readString();
        this.description = in.readString();
        this.quizType = in.readString();
        this.lockInfo = in.readParcelable(LockInfo.class.getClassLoader());
        this.permissions = in.readParcelable(QuizPermission.class.getClassLoader());
        this.allowedAttempts = in.readInt();
        this.questionCount = in.readInt();
        this.pointsPossible = in.readString();
        this.lockQuestionsAfterAnswering = in.readByte() != 0;
        this.dueAt = in.readString();
        this.timeLimit = in.readInt();
        this.shuffleAnswers = in.readByte() != 0;
        this.showCorrectAnswers = in.readByte() != 0;
        this.scoringPolicy = in.readString();
        this.accessCode = in.readString();
        this.ipFilter = in.readString();
        this.lockedForUser = in.readByte() != 0;
        this.lockExplanation = in.readString();
        this.hideResults = in.readString();
        this.showCorrectAnswersAt = in.readString();
        this.hideCorrectAnswersAt = in.readString();
        this.unlockAt = in.readString();
        this.oneTimeResults = in.readByte() != 0;
        this.lockAt = in.readString();
        this.questionTypes = in.createStringArrayList();
        this.hasAccessCode = in.readByte() != 0;
        this.oneQuestionAtATime = in.readByte() != 0;
        this.requireLockdownBrowser = in.readByte() != 0;
        this.requireLockdownBrowserForResults = in.readByte() != 0;
        this.allowAnonymousSubmissions = in.readByte() != 0;
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
        this.published = in.readByte() != 0;
        this.allDates = in.createTypedArrayList(AssignmentDueDate.CREATOR);
        this.assignmentGroupId = in.readLong();
        this.assignmentGroup = in.readParcelable(AssignmentGroup.class.getClassLoader());
        this.onlyVisibleToOverrides = in.readByte() != 0;
        this.overrides = in.createTypedArrayList(QuizOverride.CREATOR);
        this.assignmentId = in.readLong();
        this.unpublishable = in.readByte() != 0;
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
    }

    public static final Creator<Quiz> CREATOR = new Creator<Quiz>() {
        @Override
        public Quiz createFromParcel(Parcel source) {
            return new Quiz(source);
        }

        @Override
        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };
}
