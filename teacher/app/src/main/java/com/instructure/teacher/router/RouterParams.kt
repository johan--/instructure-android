/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.router

class RouterParams {
    companion object {
        val ASSIGNMENT_ID = "assignment_id"
        val EVENT_ID = "event_id"
        val CONVERSATION_ID = "conversation_id"
        val COURSE_ID = "course_id"
        val FILE_ID = "file_id"
        val GRADE_ID = "grade_id"
        val MESSAGE_ID = "message_id" // used by discussions and announcements
        val MODULE_ID = "module_id"
        val MODULE_ITEM_ID = "module_item_id"
        val PAGE_ID = "page_id"
        val QUIZ_ID = "quiz_id"
        val USER_ID = "user_id"
        val SLIDING_TAB_TYPE = "sliding_tab_type"
        val SUBMISSION_ID = "submission_id"
        val VERIFIER = "verifier"
        val DOWNLOAD_FRD = "download_frd"
        val MODULE_TYPE_SLASH_ID = "module_type_slash_id"
    }
}