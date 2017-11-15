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


import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@JvmSuppressWildcards
@PaperParcel
class Group : CanvasContext(), PaperParcelable {

    private var id: Long = 0
    private var name: String? = null
    var description: String? = null
    @SerializedName("avatar_url")
    var avatarUrl: String? = null
    @SerializedName("is_public")
    var isPublic: Boolean = false
    @SerializedName("followed_by_user")
    var isFollowedByUser: Boolean = false
    @SerializedName("members_count")
    var membersCount: Int = 0
    var users: List<User> = emptyList()
    @SerializedName("join_level")
    var joinLevel: JoinLevel? = JoinLevel.Unknown
    @SerializedName("context_type")
    var contextType: GroupContext? = GroupContext.Other

    // At most, ONE of these will be set.
    @SerializedName("course_id")
    var courseId: Long = 0
    @SerializedName("account_id")
    var accountId: Long = 0

    var role: GroupRole? = GroupRole.Course
    @SerializedName("group_category_id")
    var groupCategoryId: Long = 0
    @SerializedName("storage_quota_mb")
    var storageQuotaMb: Long = 0
    @SerializedName("is_favorite")
    var isFavorite: Boolean = false

    override fun getComparisonDate() = null
    override fun getComparisonString() = name
    override fun getType() = CanvasContext.Type.GROUP
    override fun getId() = id
    fun setId(id: Long) { this.id = id }
    override fun getName(): String? = name
    fun setName(name: String?) { this.name = name }
    override fun describeContents() = 0

    enum class JoinLevel {
        /* If "parent_context_auto_join", anyone can join and will be automatically accepted */
        @SerializedName("parent_context_auto_join") Automatic,
        /* If "parent_context_request", anyone  can request to join, which must be approved by a group moderator. */
        @SerializedName("parent_context_request") Request,
        /* If "invitation_only", only those how have received an invitation my join the group, by accepting that invitation. */
        @SerializedName("invitation_only") Invitation,
        Unknown
    }

    /* Certain types of groups have special role designations. Currently,
       these include: "communities", "student_organized", and "imported".
       Regular course/account groups have a role of null. */
    enum class GroupRole {
        @SerializedName("communities") Community,
        @SerializedName("student_organized") Student,
        @SerializedName("imported") Imported,
        Course
    }

    enum class GroupContext {
        @SerializedName("course") Course,
        @SerializedName("account") Account,
        Other
    }

    companion object {
        @Suppress("unresolved_reference")
        @JvmField val CREATOR = PaperParcelGroup.CREATOR
    }
}
