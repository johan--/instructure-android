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
 */
package com.instructure.canvasapi2.utils

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.MediaComment
import java.util.*

fun Assignment.SUBMISSION_TYPE.prettyPrint(context: Context): String
        = Assignment.submissionTypeToPrettyPrintString(this, context)

/**
 * The global course name. This may be different from [Course.name] which could be the the user's
 * nickname for this course.
 * */
var Course.globalName: String
    get() = originalName.takeUnless(String::isNullOrBlank) ?: name
    set(value) {
        if (originalName.isNullOrBlank()) name = value else originalName = value
    }

/**
 *  If the term is concluded, it can't be favorited. So if it was favorited, and then the term concluded, we don't want it favorited now.
 *  We also don't want it included in the list of favorite courses
 */
fun Course.isValidTerm(): Boolean = term?.endAt?.after(Date()) ?: true

fun MediaComment.asAttachment() = Attachment().also {
    it.contentType = contentType ?: ""
    it.displayName = displayName
    it.filename = _fileName
    it.url = url
}

inline fun <reified T : Parcelable> T.parcelCopy(): T {
    val parcel = Parcel.obtain()
    parcel.writeParcelable(this, 0)
    parcel.setDataPosition(0)
    val copy = parcel.readParcelable<T>(T::class.java.classLoader)
    parcel.recycle()
    return copy
}
