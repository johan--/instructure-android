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

import java.util.Date;


public class GradeableStudent extends CanvasModel<GradeableStudent> {

    private long id;
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("avatar_image_url")
    private String avatarImageUrl;
    @SerializedName("html_url")
    private String htmlUrl;
    @SerializedName("fake_student")
    private boolean isFakeStudent;

    @Override
    public long getId() {
        return id;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return displayName;
    }

    //region Getters

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public boolean isFakeStudent() {
        return isFakeStudent;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAvatarImageUrl(String avatarImageUrl) {
        this.avatarImageUrl = avatarImageUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public void setFakeStudent(boolean fakeStudent) {
        isFakeStudent = fakeStudent;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.displayName);
        dest.writeString(this.avatarImageUrl);
        dest.writeString(this.htmlUrl);
        dest.writeByte((byte) (this.isFakeStudent ? 1 : 0));
    }

    public GradeableStudent() {
    }

    protected GradeableStudent(Parcel in) {
        this.id = in.readLong();
        this.displayName = in.readString();
        this.avatarImageUrl = in.readString();
        this.htmlUrl = in.readString();
        this.isFakeStudent = in.readByte() != 0;
    }

    public static final Creator<GradeableStudent> CREATOR = new Creator<GradeableStudent>() {
        @Override
        public GradeableStudent createFromParcel(Parcel source) {
            return new GradeableStudent(source);
        }

        @Override
        public GradeableStudent[] newArray(int size) {
            return new GradeableStudent[size];
        }
    };

    //endregion
}
