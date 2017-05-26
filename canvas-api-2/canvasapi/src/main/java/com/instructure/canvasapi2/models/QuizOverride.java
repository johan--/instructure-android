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

package com.instructure.canvasapi2.models;

import android.os.Parcel;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class QuizOverride extends CanvasModel<QuizOverride> {

    public long id;
    @SerializedName("assignment_id")
    public long assignmentId;
    public String title;
    @SerializedName("due_at")
    public Date dueAt;
    @SerializedName("all_day")
    boolean allDay;
    @SerializedName("all_day_date")
    public String allDayDate;
    @SerializedName("unlock_at")
    public Date unlockAt;
    @SerializedName("lock_at")
    public Date lockAt;
    @SerializedName("course_section_id")
    public long courseSectionId;
    @SerializedName("student_ids")
    private long[] studentIds;
    @SerializedName("group_id")
    private long groupId;

    public long getCourseSectionId() {
        return courseSectionId;
    }

    public void setCourseSectionId(long courseSectionId) {
        this.courseSectionId = courseSectionId;
    }

    public Date getLockAt() {
        return lockAt;
    }

    public void setLockAt(Date lockAt) {
        this.lockAt = lockAt;
    }

    public Date getUnlockAt() {
        return unlockAt;
    }

    public void setUnlockAt(Date unlockAt) {
        this.unlockAt = unlockAt;
    }

    public String getAllDayDate() {
        return allDayDate;
    }

    public void setAllDayDate(String allDayDate) {
        this.allDayDate = allDayDate;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public Date getDueAt() {
        return dueAt;
    }

    public void setDueAt(Date dueAt) {
        this.dueAt = dueAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    @Nullable
    public long[] getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(long[] studentIds) {
        this.studentIds = studentIds;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return dueAt;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.assignmentId);
        dest.writeString(this.title);
        dest.writeLong(this.dueAt != null ? this.dueAt.getTime() : -1);
        dest.writeByte(this.allDay ? (byte) 1 : (byte) 0);
        dest.writeString(this.allDayDate);
        dest.writeLong(this.unlockAt != null ? this.unlockAt.getTime() : -1);
        dest.writeLong(this.lockAt != null ? this.lockAt.getTime() : -1);
        dest.writeLong(this.courseSectionId);
        dest.writeLongArray(this.studentIds);
        dest.writeLong(groupId);
    }

    public QuizOverride() {
    }

    protected QuizOverride(Parcel in) {
        this.id = in.readLong();
        this.assignmentId = in.readLong();
        this.title = in.readString();
        long tmpDueAt = in.readLong();
        this.dueAt = tmpDueAt == -1 ? null : new Date(tmpDueAt);
        this.allDay = in.readByte() != 0;
        this.allDayDate = in.readString();
        long tmpUnlockAt = in.readLong();
        this.unlockAt = tmpUnlockAt == -1 ? null : new Date(tmpUnlockAt);
        long tmpLockAt = in.readLong();
        this.lockAt = tmpLockAt == -1 ? null : new Date(tmpLockAt);
        this.courseSectionId = in.readLong();
        this.studentIds = in.createLongArray();
        this.groupId = in.readLong();
    }

    public static final Creator<QuizOverride> CREATOR = new Creator<QuizOverride>() {
        @Override
        public QuizOverride createFromParcel(Parcel source) {
            return new QuizOverride(source);
        }

        @Override
        public QuizOverride[] newArray(int size) {
            return new QuizOverride[size];
        }
    };
}