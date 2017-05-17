package com.instructure.canvasapi2.models;

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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CourseNickname extends CanvasModel<CourseNickname> {

    @SerializedName("course_id")
    private long id;
    private String name;
    private String nickname;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    @Nullable
    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.nickname);
    }

    public CourseNickname() {
    }

    protected CourseNickname(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.nickname = in.readString();
    }

    public static final Parcelable.Creator<CourseNickname> CREATOR = new Parcelable.Creator<CourseNickname>() {
        @Override
        public CourseNickname createFromParcel(Parcel source) {
            return new CourseNickname(source);
        }

        @Override
        public CourseNickname[] newArray(int size) {
            return new CourseNickname[size];
        }
    };
}
