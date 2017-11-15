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
package com.instructure.canvasapi2.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class AttendanceStats() : Parcelable {

    @SerializedName("presences")
    val presences: Long = 0

    @SerializedName("tardies")
    val tardies: Long = 0

    @SerializedName("absences")
    val absences: Long = 0

    @SerializedName("attendanceGrade")
    val attendanceGrade: String = ""

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<AttendanceStats> = object : Parcelable.Creator<AttendanceStats> {
            override fun createFromParcel(source: Parcel): AttendanceStats = AttendanceStats(source)
            override fun newArray(size: Int): Array<AttendanceStats?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}
}
