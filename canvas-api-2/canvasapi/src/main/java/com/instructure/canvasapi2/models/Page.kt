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

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.util.*

@PaperParcel
data class Page(
        @SerializedName("page_id")
        var pageId: Long = 0,
        var url: String? = null,
        var title: String? = null,
        @SerializedName("created_at")
        var createdAt: Date? = null,
        @SerializedName("updated_at")
        var updatedAt: Date? = null,
        @SerializedName("hide_from_students")
        var hideFromStudents: Boolean = false,
        var status: String? = null,
        var body: String? = null,
        @SerializedName("front_page")
        var isFrontPage: Boolean = false,
        @SerializedName("lock_info")
        var lockInfo: LockInfo? = null,
        @SerializedName("published")
        var isPublished: Boolean = false,
        @SerializedName("editing_roles")
        var editingRoles: String? = null
) : CanvasModel<Page>(), PaperParcelable {
    override fun getComparisonDate() = updatedAt
    override fun getComparisonString() = title
    override fun describeContents() = 0
    override fun getId() = pageId
    fun setId(id: Long) { this.pageId = id }

    companion object {
        const val FRONT_PAGE_NAME = "front-page"

        @Suppress("unresolved_reference")
        @JvmField val CREATOR = PaperParcelPage.CREATOR
        const val TEACHERS = "teachers"
        const val STUDENTS = "students"
        const val ANYONE = "public"
        const val GROUP_MEMBERS = "members"
    }
}
