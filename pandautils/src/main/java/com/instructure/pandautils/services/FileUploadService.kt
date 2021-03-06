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

package com.instructure.pandautils.services

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.NotificationCompat
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import java.util.*

class FileUploadService @JvmOverloads constructor(name: String = FileUploadService::class.java.simpleName) : IntentService(name) {

    private var uploadCount: Int = 0

    lateinit private var notificationBuilder: NotificationCompat.Builder
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onHandleIntent(intent: Intent) {
        val action = intent.action
        val bundle = intent.extras
        val fileSubmitObjects = bundle.getParcelableArrayList<FileSubmitObject>(Const.FILES) ?: return
        val assignment = bundle.getParcelable<Assignment>(Const.ASSIGNMENT)

        uploadCount = fileSubmitObjects.size
        showNotification(uploadCount)

        if (assignment != null && assignment.groupCategoryId != 0L) {
            // This is a group assignment, we need to get the list of groups before starting uploads
            GroupManager.getGroupsSynchronous(true)
                    ?.find { it.groupCategoryId == assignment.groupCategoryId }
                    ?.let { startUploads(action, fileSubmitObjects, bundle, it.id) }
                    ?: broadcastError(getString(R.string.errorSubmittingFiles))
        } else {
            startUploads(action, fileSubmitObjects, bundle, null)
        }
    }

    // region Upload
    private fun startUploads(action: String, fileSubmitObjects: ArrayList<FileSubmitObject>, bundle: Bundle, groupId: Long?) {
        val courseId = bundle.getLong(Const.COURSE_ID)
        val assignment = bundle.getParcelable<Assignment>(Const.ASSIGNMENT)
        val conversationId = bundle.getLong(Const.CONVERSATION)
        val quizQuestionId = bundle.getLong(Const.QUIZ_ANSWER_ID, -1)
        val quizId = bundle.getLong(Const.QUIZ)
        val position = bundle.getInt(Const.POSITION)
        val messageText = bundle.getString(Const.MESSAGE)
        val subject = bundle.getString(Const.SUBJECT)
        val isGroup = bundle.getBoolean(Const.IS_GROUP)
        val userIds = bundle.getStringArrayList(Const.USER_IDS)
        val contextId = bundle.getString(Const.CONTEXT_ID)
        val parentFolderId = if (bundle.containsKey(Const.PARENT_FOLDER_ID)) bundle.getLong(Const.PARENT_FOLDER_ID) else null

        val attachmentsIds = mutableListOf<Long>()
        val attachments = mutableListOf<Attachment>()

        try {
            fileSubmitObjects.forEachIndexed { idx, fso ->
                updateNotificationCount(fso.name, idx + 1)
                val config = FileUploadConfig(fso.name, fso.fullPath, fso.size, fso.contentType)
                when (action) {
                    ACTION_ASSIGNMENT_SUBMISSION -> {
                        val uploadContext = if (groupId == null) SubmissionUploadContext(courseId, assignment!!.id) else GroupUploadContext(groupId)
                        attachmentsIds += FileUploadManager.uploadFileSynchronous(uploadContext, config)!!.id
                    }
                    ACTION_COURSE_FILE -> {
                        config.parentFolderId = parentFolderId
                        attachmentsIds += FileUploadManager.uploadFileSynchronous(CourseUploadContext(courseId), config)!!.id
                    }
                    ACTION_USER_FILE -> {
                        config.parentFolderId = parentFolderId
                        attachmentsIds += FileUploadManager.uploadFileSynchronous(UserUploadContext(), config)!!.id
                    }
                    ACTION_MESSAGE_ATTACHMENTS, ACTION_NEW_MESSAGE_ATTACHMENTS -> {
                        config.parentFolderPath = MESSAGE_ATTACHMENT_PATH
                        attachmentsIds += FileUploadManager.uploadFileSynchronous(UserUploadContext(), config)!!.id
                    }
                    ACTION_QUIZ_FILE -> {
                        attachments += FileUploadManager.uploadFileSynchronous(QuizUploadContext(courseId, quizId), config)!!
                    }
                    ACTION_DISCUSSION_ATTACHMENT -> {
                        config.parentFolderPath = DISCUSSION_ATTACHMENT_PATH
                        attachments += FileUploadManager.uploadFileSynchronous(UserUploadContext(), config)!!
                    }
                }
                broadcastUploadCompleted(fso)
            }
            // Submit fileIds to the assignment
            when (action) {
                ACTION_ASSIGNMENT_SUBMISSION -> submitAttachmentsForSubmission(courseId, assignment, attachmentsIds)
                ACTION_MESSAGE_ATTACHMENTS -> submitAttachmentsForMessage(conversationId, messageText, attachmentsIds)
                ACTION_DISCUSSION_ATTACHMENT -> broadcastDiscussionSuccess(attachments)
                ACTION_QUIZ_FILE -> broadcastQuizSuccess(attachments[0], quizQuestionId, position)
                ACTION_NEW_MESSAGE_ATTACHMENTS -> submitAttachmentsForNewMessage(messageText, subject, userIds, contextId, isGroup, attachmentsIds)
                else -> {
                    updateNotificationComplete()
                    broadCastAllUploadsCompleted()
                }
            }
        } catch (exception: Exception) {
            updateNotification(getString(R.string.errorUploadingFile))
            if (quizQuestionId != -1L) {
                broadcastQuizError(exception.message.orEmpty(), quizQuestionId, position)
            } else {
                broadcastError(exception.message.orEmpty())
            }
        }

        stopSelf()
    }

    private fun submitAttachmentsForMessage(conversationID: Long, messageText: String?, attachmentsIds: List<Long>) {
        ConversationManager.addMessageToConversationSynchronous(conversationID, messageText, attachmentsIds)?.let {
            updateMessageComplete()
            broadCastMessageSuccess(it)
        } ?: broadcastError(getString(R.string.errorSendingMessage))
    }

    private fun submitAttachmentsForNewMessage(messageText: String?, subject: String?, userIds: List<String>, contextId: String?, isGroup: Boolean, attachmentsIds: List<Long>) {
        ConversationManager.createConversationWithAttachmentSynchronous(messageText, subject, userIds, contextId, isGroup, attachmentsIds)?.let {
            updateMessageComplete()
            broadCastNewMessageSuccess()
        } ?: broadcastError(getString(R.string.errorSendingMessage))
    }

    private fun submitAttachmentsForSubmission(courseId: Long, assignment: Assignment, attachmentsIds: List<Long>) {
        SubmissionManager.postSubmissionAttachmentsSynchronous(courseId, assignment.id, attachmentsIds)?.let {
            updateSubmissionComplete()
            broadCastAllUploadsCompleted()
        } ?: broadcastError(getString(R.string.errorSubmittingFiles))
    }
    // endregion Upload

    // region Notifications
    private fun broadcastUploadCompleted(fso: FileSubmitObject) {
        val bundle = Bundle()
        bundle.putParcelable(Const.FILENAME, fso)
        val status = Intent(UPLOAD_COMPLETED)
        status.putExtras(bundle)
        sendBroadcast(status)
    }

    private fun broadCastAllUploadsCompleted() {
        val status = Intent(ALL_UPLOADS_COMPLETED)
        sendBroadcast(status)
    }

    private fun broadCastMessageSuccess(conversation: Conversation) {
        val status = Intent(ALL_UPLOADS_COMPLETED)
        status.putExtra(Const.CONVERSATION, conversation as Parcelable)
        sendBroadcast(status)
    }

    private fun broadCastNewMessageSuccess() {
        val status = Intent(ALL_UPLOADS_COMPLETED)
        sendBroadcast(status)
    }

    private fun broadcastDiscussionSuccess(attachments: List<Attachment>) {
        updateNotificationComplete()
        val status = Intent(ALL_UPLOADS_COMPLETED)
        status.putParcelableArrayListExtra(Const.ATTACHMENTS, ArrayList(attachments))
        sendBroadcast(status)
    }

    private fun broadcastQuizSuccess(attachment: Attachment, quizQuestionId: Long, position: Int) {
        updateNotificationComplete()
        val status = Intent(QUIZ_UPLOAD_COMPLETE)
        status.putExtra(Const.ATTACHMENT, attachment as Parcelable)
        status.putExtra(Const.QUIZ_ANSWER_ID, quizQuestionId)
        status.putExtra(Const.POSITION, position)
        sendBroadcast(status)
    }

    private fun broadcastQuizError(message: String, quizQuestionId: Long, position: Int) {
        updateNotificationError(message)
        val bundle = Bundle()
        val status = Intent(UPLOAD_ERROR)
        status.putExtra(Const.QUIZ_ANSWER_ID, quizQuestionId)
        status.putExtra(Const.POSITION, position)
        bundle.putString(Const.MESSAGE, message)
        status.putExtras(bundle)
        sendBroadcast(status)
    }

    private fun broadcastError(message: String) {
        updateNotificationError(message)
        val bundle = Bundle()
        val status = Intent(UPLOAD_ERROR)

        bundle.putString(Const.MESSAGE, message)
        status.putExtras(bundle)
        sendBroadcast(status)
    }

    private fun showNotification(size: Int) {
        createNotificationChannel(CHANNEL_ID)
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(String.format(Locale.US, getString(R.string.uploadingFileNum), 1, size))
                .setProgress(0, 0, true)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateNotificationCount(fileName: String, currentItem: Int) {
        notificationBuilder.setContentTitle(String.format(Locale.US, getString(R.string.uploadingFileNum), currentItem, uploadCount))
                .setContentText(fileName)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun updateNotification(message: String) {
        notificationBuilder.setContentText(message)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateNotificationError(message: String) {
        notificationBuilder.setContentText(message)
                .setProgress(0, 0, false)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateNotificationComplete() {
        notificationBuilder.setProgress(0, 0, false)
                .setContentTitle(getString(R.string.filesUploadedSuccessfully))
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateSubmissionComplete() {
        notificationBuilder.setProgress(0, 0, false)
                .setContentTitle(getString(R.string.filesSubmittedSuccessfully))
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateMessageComplete() {
        notificationBuilder.setProgress(0, 0, false)
                .setContentTitle(getString(R.string.messageSentSuccessfully))
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // Prevents recreation of notification channel if it exists.
        if (notificationManager.notificationChannels.any { it.id == channelId }) return

        val name = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsName)
        val description = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsDescription)

        // Create the channel and add the group
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = description
        channel.enableLights(false)
        channel.enableVibration(false)

        // Create the channel
        notificationManager.createNotificationChannel(channel)
    }
    // endregion Notifications

    override fun onDestroy() {
        notificationBuilder.setOngoing(false)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        FileUploadUtils.deleteDirectory(FileUploadUtils.getTempFolder(this))
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "fileUploadChannel"

        // Upload broadcasts
        const val ALL_UPLOADS_COMPLETED = "ALL_UPLOADS_COMPLETED"
        const val QUIZ_UPLOAD_COMPLETE = "QUIZ_UPLOAD_COMPLETE"
        const val UPLOAD_COMPLETED = "UPLOAD_COMPLETED"
        const val UPLOAD_ERROR = "UPLOAD_ERROR"

        // Upload Actions
        const val ACTION_ASSIGNMENT_SUBMISSION = "ACTION_ASSIGNMENT_SUBMISSION"
        const val ACTION_MESSAGE_ATTACHMENTS = "ACTION_MESSAGE_ATTACHMENTS"
        const val ACTION_NEW_MESSAGE_ATTACHMENTS = "ACTION_NEW_MESSAGE_ATTACHMENTS"
        const val ACTION_COURSE_FILE = "ACTION_COURSE_FILE"
        const val ACTION_USER_FILE = "ACTION_USER_FILE"
        const val ACTION_QUIZ_FILE = "ACTION_QUIZ_FILE"
        const val ACTION_DISCUSSION_ATTACHMENT = "ACTION_DISCUSSION_ATTACHMENT"

        private const val MESSAGE_ATTACHMENT_PATH = "conversation attachments"
        private const val DISCUSSION_ATTACHMENT_PATH = "discussion attachments"

        @JvmStatic
        fun getUserFilesBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                parentFolderId: Long?
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            parentFolderId?.let { putLong(Const.PARENT_FOLDER_ID, it) }
        }

        @JvmStatic
        fun getQuizFileBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                parentFolderId: Long?,
                quizQuestionId: Long,
                position: Int,
                courseId: Long,
                quizId: Long
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.QUIZ_ANSWER_ID, quizQuestionId)
            putLong(Const.QUIZ, quizId)
            putLong(Const.COURSE_ID, courseId)
            putInt(Const.POSITION, position)
            parentFolderId?.let { putLong(Const.PARENT_FOLDER_ID, it) }
        }

        @JvmStatic
        fun getCourseFilesBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                courseId: Long,
                parentFolderId: Long?
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.COURSE_ID, courseId)
            parentFolderId?.let { putLong(Const.PARENT_FOLDER_ID, it) }
        }

        @JvmStatic
        fun getAssignmentSubmissionBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                courseId: Long,
                assignment: Assignment
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.COURSE_ID, courseId)
            putParcelable(Const.ASSIGNMENT, assignment)
        }

        @JvmStatic
        fun getMessageBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                messageText: String,
                conversationId: Long
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putLong(Const.CONVERSATION, conversationId)
            putString(Const.MESSAGE, messageText)
        }

        @JvmStatic
        fun getNewMessageBundle(
                fileSubmitObjects: ArrayList<FileSubmitObject>,
                userIds: ArrayList<String>,
                subject: String,
                messageText: String,
                isGroup: Boolean,
                contextId: String
        ) = Bundle().apply {
            putParcelableArrayList(Const.FILES, fileSubmitObjects)
            putStringArrayList(Const.USER_IDS, userIds)
            putString(Const.SUBJECT, subject)
            putString(Const.MESSAGE, messageText)
            putBoolean(Const.IS_GROUP, isGroup)
            putString(Const.CONTEXT_ID, contextId)
        }
    }
}
