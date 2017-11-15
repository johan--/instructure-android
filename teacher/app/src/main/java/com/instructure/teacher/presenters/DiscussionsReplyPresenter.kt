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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.teacher.viewinterface.DiscussionsReplyView
import instructure.androidblueprint.FragmentPresenter
import retrofit2.Call
import retrofit2.Response
import java.io.File

class DiscussionsReplyPresenter(
        val canvasContext: CanvasContext,
        val discussionTopicHeaderId: Long,
        val discussionEntryId: Long) : FragmentPresenter<DiscussionsReplyView>() {

    override fun loadData(forceNetwork: Boolean) {

    }

    override fun refresh(forceNetwork: Boolean) {
        mSendMessageCallback.reset()
        mSendMessageWithAttachmentCallback.reset()
    }

    fun sendMessage(message: String?) {
        //Handle debouncing
        if(mSendMessageWithAttachmentCallback.isCallInProgress || mSendMessageCallback.isCallInProgress) {
            viewCallback?.messageFailure(REASON_MESSAGE_IN_PROGRESS)
            return
        }

        if(message == null) {
            viewCallback?.messageFailure(REASON_MESSAGE_EMPTY)
        } else if(attachment != null) {
            if(discussionEntryId == discussionTopicHeaderId) {
                DiscussionManager.postToDiscussionTopic(canvasContext, discussionTopicHeaderId, message, File(attachment!!.fullPath), mSendMessageWithAttachmentCallback)
            } else {
                DiscussionManager.replyToDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntryId, message, File(attachment!!.fullPath), mSendMessageWithAttachmentCallback)
            }
        } else {
            if(discussionEntryId == discussionTopicHeaderId) {
                DiscussionManager.postToDiscussionTopic(canvasContext, discussionTopicHeaderId, message, mSendMessageCallback)
            } else {
                DiscussionManager.replyToDiscussionEntry(canvasContext, discussionTopicHeaderId, discussionEntryId, message, mSendMessageCallback)
            }
        }
    }

    private val mSendMessageCallback = object : StatusCallback<DiscussionEntry>() {
        override fun onResponse(response: Response<DiscussionEntry>, linkHeaders: LinkHeaders, type: ApiType) {
            if (response.code() in 200..299) {
                viewCallback?.messageSuccess(response.body())
            } else {
                viewCallback?.messageFailure(REASON_MESSAGE_FAILED_TO_SEND)
            }
        }

        override fun onFail(response: Call<DiscussionEntry>, error: Throwable) {
            viewCallback?.messageFailure(REASON_MESSAGE_FAILED_TO_SEND)
        }
    }

    private val mSendMessageWithAttachmentCallback = object : StatusCallback<DiscussionEntry>() {
        override fun onResponse(response: Response<DiscussionEntry>, linkHeaders: LinkHeaders, type: ApiType) {
            if (response.code() in 200..299) {
                viewCallback?.messageSuccess(response.body())
            } else {
                viewCallback?.messageFailure(REASON_MESSAGE_FAILED_TO_SEND)
            }
        }

        override fun onFail(response: Call<DiscussionEntry>, error: Throwable) {
            viewCallback?.messageFailure(REASON_MESSAGE_FAILED_TO_SEND)
        }
    }

    fun setAttachment(fileSubmitObject: FileSubmitObject?) {
        attachment = fileSubmitObject
    }

    fun getAttachment(): FileSubmitObject? {
        return attachment
    }

    fun hasAttachment(): Boolean {
        return attachment != null
    }

    companion object {
        val REASON_MESSAGE_IN_PROGRESS = 1
        val REASON_MESSAGE_EMPTY = 2
        val REASON_MESSAGE_FAILED_TO_SEND = 3
        var attachment: FileSubmitObject? = null
    }
}
