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
package com.instructure.loginapi.login.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.canvasapi2.models.User;

import java.util.ArrayList;
import java.util.Date;

public class SignedInUser implements Comparable<SignedInUser>, Parcelable {

    public User user;
    public String domain;
    public String protocol;
    public String token;
    public ArrayList<String> calendarFilterPrefs;

    public Date lastLogoutDate;

    public SignedInUser() {
    }

    @Override
    public int compareTo(SignedInUser signedInUser) {
        //We want newest first.
        return -1 * CanvasComparable.compare(lastLogoutDate, signedInUser.lastLogoutDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.domain);
        dest.writeString(this.protocol);
        dest.writeString(this.token);
        dest.writeStringList(this.calendarFilterPrefs);
        dest.writeLong(this.lastLogoutDate != null ? this.lastLogoutDate.getTime() : -1);
    }

    protected SignedInUser(Parcel in) {
        this.user = in.readParcelable(User.class.getClassLoader());
        this.domain = in.readString();
        this.protocol = in.readString();
        this.token = in.readString();
        this.calendarFilterPrefs = in.createStringArrayList();
        long tmpLastLogoutDate = in.readLong();
        this.lastLogoutDate = tmpLastLogoutDate == -1 ? null : new Date(tmpLastLogoutDate);
    }

    public static final Parcelable.Creator<SignedInUser> CREATOR = new Parcelable.Creator<SignedInUser>() {
        @Override
        public SignedInUser createFromParcel(Parcel source) {
            return new SignedInUser(source);
        }

        @Override
        public SignedInUser[] newArray(int size) {
            return new SignedInUser[size];
        }
    };
}