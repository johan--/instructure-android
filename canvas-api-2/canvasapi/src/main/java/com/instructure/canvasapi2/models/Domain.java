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

import java.util.Date;

public class Domain extends CanvasModel<Domain> {

    private String canvas_domain;

    public String getCanvasDomain() {
        return canvas_domain;
    }

    public void setCanvasDomain(String canvas_domain) {
        this.canvas_domain = canvas_domain;
    }

    @Override
    public long getId() {
        return canvas_domain.hashCode();
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

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
        dest.writeString(this.canvas_domain);
    }

    public Domain() {
    }

    protected Domain(Parcel in) {
        this.canvas_domain = in.readString();
    }

    public static final Creator<Domain> CREATOR = new Creator<Domain>() {
        public Domain createFromParcel(Parcel source) {
            return new Domain(source);
        }

        public Domain[] newArray(int size) {
            return new Domain[size];
        }
    };
}
