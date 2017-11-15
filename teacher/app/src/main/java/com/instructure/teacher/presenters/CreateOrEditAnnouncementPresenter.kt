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

import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.post_models.DiscussionTopicPostBody
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.teacher.events.DiscussionCreatedEvent
import com.instructure.teacher.events.DiscussionTopicHeaderDeletedEvent
import com.instructure.teacher.events.DiscussionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.fragments.DiscussionsDetailsFragment
import com.instructure.teacher.viewinterface.CreateOrEditAnnouncementView
import instructure.androidblueprint.FragmentPresenter
import kotlinx.coroutines.experimental.Job
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class CreateOrEditAnnouncementPresenter(
        private val mCanvasContext: CanvasContext,
        editAnnouncement: DiscussionTopicHeader? = null
) : FragmentPresenter<CreateOrEditAnnouncementView>() {

    private var apiJob: Job? = null

    /** *True* for editing mode, *false* for creation mode */
    var isEditing = editAnnouncement != null

    /**
     * The announcement that is being edited/created. Changes should be applied directly to this
     * object. For editing mode this object should be passed to the constructor as a deep copy of
     * the original so that canceled changes are not erroneously propagated back to other pages. In
     * creation mode this object will be generated with the values necessary to distinguish it as
     * an announcement instead of a normal discussion topic header.
     */
    val announcement: DiscussionTopicHeader = editAnnouncement ?: DiscussionTopicHeader().apply {
        isAnnouncement = true
        isPublished = true
        type = DiscussionTopicHeader.DiscussionType.SIDE_COMMENT
    }

    /**
     * (Creation mode only) An attachment to be uploaded alongside the announcement. Note that this
     * can only be used when creating new Announcements. Setting/changing attachments on existing
     * announcements (editing mode) is currently unsupported.
     */
    var attachment: FileSubmitObject? = null

    /** (Editing mode only) Set to *true* if the existing Announcement's attachment should be removed */
    var attachmentRemoved = false

    override fun loadData(forceNetwork: Boolean) {}
    override fun refresh(forceNetwork: Boolean) {}

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun saveAnnouncement() {
        viewCallback?.onSaveStarted()
        apiJob = tryWeave {
            if (isEditing) {
                val postBody = DiscussionTopicPostBody.fromAnnouncement(announcement, attachmentRemoved)
                val updatedAnnouncement = awaitApi<DiscussionTopicHeader> { callback ->
                    DiscussionManager.editDiscussionTopic(mCanvasContext, announcement.id, postBody, callback)
                }
                DiscussionUpdatedEvent(updatedAnnouncement).post()
            } else {
                var filePart: MultipartBody.Part? = null
                attachment?.let {
                    val file = File(it.fullPath)
                    val requestBody = RequestBody.create(MediaType.parse(it.contentType), file)
                    filePart = MultipartBody.Part.createFormData("attachment", file.name, requestBody)
                }
                awaitApi<DiscussionTopicHeader> {
                    DiscussionManager.createDiscussion(mCanvasContext, announcement, filePart, it)
                }
                DiscussionCreatedEvent(true).post()
            }
            viewCallback?.onSaveSuccess()
        } catch {
            viewCallback?.onSaveError()
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun deleteAnnouncement() {
        viewCallback?.onSaveStarted()
        apiJob = tryWeave {
            awaitApi<Void> { DiscussionManager.deleteDiscussionTopicHeader(mCanvasContext, announcement.id, it) }
            DiscussionTopicHeaderDeletedEvent(announcement.id, (DiscussionsDetailsFragment::class.java.toString() + ".onResume()")).post()
            viewCallback?.onDeleteSuccess()
        } catch {
            viewCallback?.onDeleteError()
        }
    }

}
