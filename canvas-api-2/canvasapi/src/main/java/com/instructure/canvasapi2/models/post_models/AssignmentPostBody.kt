package com.instructure.canvasapi2.models.post_models

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.models.AssignmentOverride
import java.util.*


class AssignmentPostBody {

    var name: String? = null

    var description: String? = null

    @SerializedName("group_category_id")
    var groupCategoryId: Long? = null

    @SerializedName("assignment_group_id")
    var assignmentGroupId: Long? = null

    @SerializedName("points_possible")
    var pointsPossible: Double? = null

    @SerializedName("grading_type")
    var gradingType: String? = null

    @SerializedName("due_at")
    var dueAt: String? = null

    @SerializedName("notify_of_update")
    var notifyOfUpdate: Boolean? = null

    @SerializedName("peer_reviews")
    var peerReviews: Int? = null

    @SerializedName("unlock_at")
    var unlockAt: String? = null

    @SerializedName("lock_at")
    var lockAt: String? = null

    var muted: Boolean? = null

    var published: Boolean? = null

    @SerializedName("assignment_overrides")
    var assignmentOverrides: List<OverrideBody>? = null

    @SerializedName("only_visible_to_overrides")
    var isOnlyVisibleToOverrides: Boolean? = null

}

class OverrideBody {

    @SerializedName("assignment_id")
    var assignmentId: Long? = null
    
    @SerializedName("due_at")
    var dueAt: Date? = null
    
    @SerializedName("unlock_at")
    var unlockAt: Date? = null
    
    @SerializedName("lock_at")
    var lockAt: Date? = null
    
    @SerializedName("student_ids")
    var studentIds: LongArray? = null
    
    @SerializedName("course_section_id")
    var courseSectionId: Long? = null
    
    @SerializedName("group_id")
    var groupId: Long? = null

    companion object {
        fun fromAssignmentOverride(override: AssignmentOverride) = OverrideBody().apply {
            dueAt = override.dueAt
            lockAt = override.lockAt
            unlockAt = override.unlockAt
            if (override.getAssignmentId() != 0L) assignmentId = override.assignmentId
            if (override.groupId != 0L) groupId = override.groupId
            if (override.studentIds?.isNotEmpty() ?: false) studentIds = override.studentIds
            if (override.getCourseSectionId() != 0L) courseSectionId = override.getCourseSectionId()
        }
    }
    
}

class AssignmentPostBodyWrapper {
    var assignment: AssignmentPostBody? = null
}
