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
import java.text.SimpleDateFormat
import java.util.*

class Attendance() : CanvasModel<Attendance>(), Parcelable {

    enum class Attendance {
        PRESENT, ABSENT, LATE, UNMARKED
    }

    @SerializedName("id")
    var statusId: Long? = null //If null then unmarked

    @SerializedName("student_id")
    val studentId: Long = 0

    @SerializedName("teacher_id")
    val teacherId: Long = 0

    @SerializedName("section_id")
    val sectionId: Long = 0

    @SerializedName("course_id")
    val courseId: Long = 0

    @SerializedName("student")
    val student: User? = null

    @SerializedName("class_date")
    var date: String? = null

    @SerializedName("attendance")
    var attendance: String? = null //present, absent, or late, unmarked when null

    //Used to store the attendance status in case of an API failure.
    var _postingAttendance: String? = null

    @SerializedName("stats")
    val stats: AttendanceStats = AttendanceStats()

    @SerializedName("seated")
    val seated: Boolean = false

    @SerializedName("row")
    val row: Int = 0

    @SerializedName("col")
    val column: Int = 0

    fun attendanceStatus(): Attendance {
        if(attendance == null) {
            return Attendance.UNMARKED
        }

        when (attendance) {
            "present" -> return Attendance.PRESENT
            "absent" -> return Attendance.ABSENT
            "late" -> return Attendance.LATE
            else -> return Attendance.UNMARKED
        }
    }

    fun postingAttendanceStatus(): Attendance {
        if(_postingAttendance == null) {
            return Attendance.UNMARKED
        }

        when (_postingAttendance) {
            "present" -> return Attendance.PRESENT
            "absent" -> return Attendance.ABSENT
            "late" -> return Attendance.LATE
            else -> return Attendance.UNMARKED
        }
    }

    fun setAttendanceStatus(statusTo: Attendance) {
        when (statusTo) {
            Attendance.PRESENT -> attendance = "present"
            Attendance.ABSENT -> attendance = "absent"
            Attendance.LATE -> attendance = "late"
            Attendance.UNMARKED -> attendance = null
        }
    }

    fun setDate(calendar: Calendar) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = calendar.timeZone
        date = formatter.format(calendar.time)
    }

    fun getDate(): Calendar? {
        val date = comparisonDate
        val calendar = GregorianCalendar.getInstance()
        calendar.timeInMillis = date?.time ?: calendar.timeInMillis
        return calendar
    }

    override fun getId(): Long {
        return studentId
    }

    override fun getComparisonDate(): Date? {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.parse(date)
    }

    override fun getComparisonString(): String? {
        return attendance
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<com.instructure.canvasapi2.models.Attendance> = object : Parcelable.Creator<com.instructure.canvasapi2.models.Attendance> {
            override fun createFromParcel(source: Parcel): com.instructure.canvasapi2.models.Attendance = Attendance(source)
            override fun newArray(size: Int): Array<com.instructure.canvasapi2.models.Attendance?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}
}
