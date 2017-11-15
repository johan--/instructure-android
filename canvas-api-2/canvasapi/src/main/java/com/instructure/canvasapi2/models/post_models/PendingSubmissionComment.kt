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
package com.instructure.canvasapi2.models.post_models

import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.util.*

@PaperParcel
data class PendingSubmissionComment(
        var pageId: String,
        var comment: String? = null
) : PaperParcelable {
    var date = Date()
    var id = UUID.randomUUID().mostSignificantBits
    var status = CommentSendStatus.DRAFT
    var progress = 0f
    var filePath = ""

    companion object {
        @Suppress("unresolved_reference")
        @JvmStatic val CREATOR = PaperParcelPendingSubmissionComment.CREATOR
    }

}

enum class CommentSendStatus { DRAFT, SENDING, ERROR }


