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
package com.instructure.teacher.utils

import android.content.Context
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R

/**
 * Used to convert DiscussionEntries into HTML. Typically this class only takes data and does little calculation.
 * Be careful doing heavy work within here since this will get used in some form of loop.
 */
class DiscussionEntryHtmlConverter {

    fun buildHtml(
            context: Context,
            brandColor: Int,
            likeColor: Int,
            discussionEntry: DiscussionEntry,
            template: String,
            avatarImage: String,
            canReply: Boolean,
            canEdit: Boolean,
            allowLiking: Boolean,
            canDelete: Boolean,
            reachedViewableEnd: Boolean,
            indent: Int,
            likeImage: String,
            replyButtonWidth: String,
            deletedText: String): String {

        val repliesButtonText = context.getString(R.string.discussions_replies, discussionEntry.totalChildren.toString())

        val htmlListener = getItemClickListener(discussionEntry.id.toString())
        val replyListener = getReplyListener(discussionEntry.id.toString())
        val likeListener = getLikeListener(discussionEntry.id.toString())
        val avatarListener = getAvatarListener(discussionEntry.id.toString())
        val attachmentListener = getAttachmentListener(discussionEntry.id.toString())
        val editListener = getEditListener(discussionEntry.id.toString())
        val deleteListener = getDeleteListener(discussionEntry.id.toString())
        val moreRepliesListener = getMoreRepliesListener(discussionEntry.id.toString())

        var authorName: String = ""
        var date = ""
        var content = getContentHTML(discussionEntry.getMessage(""))
        val reply = formatReplyText(context, canReply)
        val edit = formatEditText(context, canEdit)
        val delete = formatDeleteText(context, canDelete)
        val userId: String
        var replyButtonWrapperStyle = "display: block;"
        var detailsWrapperStyle = "display: block;"
        var indentDisplay1 = "display: none;"
        var indentDisplay2 = "display: none;"
        var indentDisplay3 = "display: none;"
        var indentDisplay4 = "display: none;"
        var indentDisplay5 = "display: none;"
        val attachments = getAttachments(discussionEntry)

        val liking = if(allowLiking) "display: inline;" else "display: none;"
        val likingSum = if(discussionEntry.ratingSum == 0) "" else "(" + discussionEntry.ratingSum + ")"
        val likingIcon = if(discussionEntry.hasRated()) likeImage else "file:///android_asset/discussion_unliked.png"
        val likingColor = if(discussionEntry.hasRated()) brandColor else likeColor

        //Prevents the topic from appearing as deleted if the editorId was not set
        if (discussionEntry.userId == 0L && discussionEntry.editorId != 0L) {
            userId = discussionEntry.editorId.toString()
        } else {
            userId = discussionEntry.userId.toString()
        }

        //Check if we can like, reply, and if there are comments
        if (discussionEntry.totalChildren == 0) {
            if(!canReply && !allowLiking) {
                //Hide detailsWrapper
                detailsWrapperStyle = "display: none;"
            }
        }

        if(reachedViewableEnd && discussionEntry.totalChildren > 0) {
            replyButtonWrapperStyle = "display: block;"
        } else {
            replyButtonWrapperStyle = "display: none;"
        }

        //Sets up indentation based on the indent param
        if(indent == 0) {
            indentDisplay1 = "display: none;"
            indentDisplay2 = "display: none;"
            indentDisplay3 = "display: none;"
            indentDisplay4 = "display: none;"
            indentDisplay5 = "display: none;"
        } else if(indent == 1) {
            indentDisplay1 = "display: inline;"
            indentDisplay2 = "display: none;"
            indentDisplay3 = "display: none;"
            indentDisplay4 = "display: none;"
            indentDisplay5 = "display: none;"
        } else if(indent == 2) {
            indentDisplay1 = "display: inline;"
            indentDisplay2 = "display: inline;"
            indentDisplay3 = "display: none;"
            indentDisplay4 = "display: none;"
            indentDisplay5 = "display: none;"
        } else if(indent == 3) {
            indentDisplay1 = "display: inline;"
            indentDisplay2 = "display: inline;"
            indentDisplay3 = "display: inline;"
            indentDisplay4 = "display: none;"
            indentDisplay5 = "display: none;"
        } else if(indent == 4) {
            indentDisplay1 = "display: inline;"
            indentDisplay2 = "display: inline;"
            indentDisplay3 = "display: inline;"
            indentDisplay4 = "display: inline;"
            indentDisplay5 = "display: none;"
        } else if(indent == 5) {
            indentDisplay1 = "display: inline;"
            indentDisplay2 = "display: inline;"
            indentDisplay3 = "display: inline;"
            indentDisplay4 = "display: inline;"
            indentDisplay5 = "display: inline;"
        }

        //Populate Author Information
        if(discussionEntry.author != null) {
            authorName = discussionEntry.author.displayName
            if(BuildConfig.DEBUG) {
                authorName = discussionEntry.author.displayName + " " + discussionEntry.id
            }
        } else {
            authorName = context.getString(R.string.discussions_unknown_author)
        }

        if(discussionEntry.isDeleted) {
            //Hide details_wrapper
            detailsWrapperStyle = "display: none;"
            //Hide the message content just in case
            content = ""
            date = deletedText
        } else if(discussionEntry.updatedAt != null) {
            date = DateHelper.getDateTimeString(context, discussionEntry.updatedAt)
        }

        return template
                .replace("__GROUP__", "display: block;")
                .replace("__LIKE_ICON__", likingIcon)
                .replace("__LIKE_COUNT__", likingSum)
                .replace("__LIKE_ALLOWED__", liking)
                .replace("__LIKE_COLOR__", colorToHex(likingColor))
                .replace("__BRAND_COLOR__", colorToHex(brandColor))
                .replace("__ATTACHMENTS_WRAPPER__", attachments)
                .replace("__INDENT_DISPLAY_1__", indentDisplay1)
                .replace("__INDENT_DISPLAY_2__", indentDisplay2)
                .replace("__INDENT_DISPLAY_3__", indentDisplay3)
                .replace("__INDENT_DISPLAY_4__", indentDisplay4)
                .replace("__INDENT_DISPLAY_5__", indentDisplay5)
                .replace("__REPLY_BUTTON_TEXT__", repliesButtonText)
                .replace("__REPLY_BUTTON_WIDTH__", replyButtonWidth)

                .replace("__HTML_LISTENER__", htmlListener)
                .replace("__AVATAR_LISTENER__", avatarListener)
                .replace("__ATTACHMENT_LISTENER__", attachmentListener)
                .replace("__REPLY_LISTENER__", replyListener)
                .replace("__EDIT_LISTENER__", editListener)
                .replace("__LIKE_LISTENER__", likeListener)
                .replace("__DELETE_LISTENER__", deleteListener)
                .replace("__MORE_REPLIES_LISTENER__", moreRepliesListener)

                .replace("__AVATAR_URL__", avatarImage)
                .replace("__TITLE__", authorName)
                .replace("__DATE__", date)
                .replace("__CONTENT_HTML__", content)
                .replace("__HEADER_ID__", discussionEntry.id.toString())
                .replace("__ENTRY_ID__", discussionEntry.id.toString())
                .replace("__USER_ID__", userId)
                .replace("__REPLY_TEXT__", reply)
                .replace("__EDIT_TEXT__", edit)
                .replace("__DELETE_TEXT__", delete)
                .replace("__DETAILS_WRAPPER__", detailsWrapperStyle)
                .replace("__REPLY_BUTTON_WRAPPER__", replyButtonWrapperStyle)
                .replace("__READ_STATE__", getReadState(discussionEntry))
    }

    private fun getContentHTML(message: String?): String {
        if (message == null || message == "null") return "" else return message
    }

    private fun getAttachments(discussionEntry: DiscussionEntry): String {
        val display = "display: none;"
        discussionEntry.attachments?.firstOrNull() ?: return display
        discussionEntry.attachments.filter { !it.isHiddenForUser }.forEach {
            //Display the attachment icon if we have at least one not hidden for the user
            return "display: inline;"
        }
        return display
    }

    private fun getItemClickListener(id: String): String {
        return " onClick=\"onItemPressed('$id')\""
    }

    private fun getReplyListener(id: String): String {
        return " onClick=\"onReplyPressed('$id')\""
    }

    private fun getAvatarListener(id: String): String {
        return " onClick=\"onAvatarPressed('$id')\""
    }

    private fun getLikeListener(id: String): String {
        return " onClick=\"onLikePressed('$id')\""
    }

    private fun getAttachmentListener(id: String): String {
        return " onClick=\"onAttachmentPressed('$id')\""
    }

    private fun getEditListener(id: String): String {
        return " onClick=\"onEditPressed('$id')\""
    }

    private fun getDeleteListener(id: String): String {
        return " onClick=\"onDeletePressed('$id')\""
    }

    private fun getMoreRepliesListener(id: String): String {
        return " onClick=\"onMoreRepliesPressed('$id')\""
    }

    private fun getReadState(discussionEntry: DiscussionEntry): String {
        //Hidden strings to identify if a discussion entry was read or unread
        return if (discussionEntry.isUnread) "unread" else "read"
    }

    private fun colorToHex(color: Int): String {
        return "#" + Integer.toHexString(color).substring(2)
    }

    private fun formatReplyText(context: Context, canReply: Boolean): String {
        if (canReply) {
            return String.format("<div class=\"reply\">%s</div>", context.getString(R.string.discussions_reply))
        }
        return ""
    }

    private fun formatEditText(context: Context, canReply: Boolean): String {
        if (canReply) {
            return String.format("<div class=\"edit\">%s</div>", context.getString(R.string.discussions_edit))
        }
        return ""
    }

    private fun formatDeleteText(context: Context, canDelete: Boolean): String {
        if (canDelete) {
            return String.format("<div class=\"delete\">%s</div>", context.getString(R.string.delete))
        }
        return ""
    }
}
