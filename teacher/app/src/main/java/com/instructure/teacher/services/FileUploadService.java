/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.services;

import android.accounts.NetworkErrorException;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.instructure.canvasapi2.managers.FileFolderManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.FileUploadParams;
import com.instructure.canvasapi2.models.RemoteFile;
import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.utils.Const;
import com.instructure.teacher.R;
import com.instructure.teacher.utils.FileUploadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class FileUploadService extends IntentService {

    public FileUploadService() {
        this(FileUploadService.class.getSimpleName());
    }

    public FileUploadService(final String name) {
        super(name);
    }

    private int mUploadCount;
    private static final int NOTIFICATION_ID = 1;
    private boolean isCanceled = false;

    // Upload broadcasts
    public static final String ALL_UPLOADS_COMPLETED = "ALL_UPLOADS_COMPLETED";
    public static final String UPLOAD_COMPLETED = "UPLOAD_COMPLETED";
    public static final String UPLOAD_ERROR = "UPLOAD_ERROR";

    // Upload Actions
    public static final String ACTION_ASSIGNMENT_SUBMISSION = "ACTION_ASSIGNMENT_SUBMISSION";
    public static final String ACTION_MESSAGE_ATTACHMENTS = "ACTION_MESSAGE_ATTACHMENTS";
    public static final String ACTION_COURSE_FILE = "ACTION_COURSE_FILE";
    public static final String ACTION_USER_FILE = "ACTION_USER_FILE";
    public static final String ACTION_DISCUSSION_ATTACHMENT = "ACTION_DISCUSSION_ATTACHMENT";
    public static final String ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD";
    public static final String ACTION_SG_MEDIA_COMMENT = "actionSgMediaComment";

    public static final String MESSAGE_ATTACHMENT_PATH = "conversation attachments";
    public static final String DISCUSSION_ATTACHMENT_PATH = "discussion attachments";

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Handle 'cancel' action in onStartCommand instead of onHandleIntent, because threading.
        if (ACTION_CANCEL_UPLOAD.equals(intent.getAction())) isCanceled = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Skip if canceled
        if (isCanceled) return;

        final String action = intent.getAction();
        final Bundle bundle = intent.getExtras();
        final ArrayList<FileSubmitObject> fileSubmitObjects = bundle.getParcelableArrayList(Const.FILES);

        mUploadCount = fileSubmitObjects == null ? 0 : fileSubmitObjects.size();
        showNotification(mUploadCount);

        startUploads(action, fileSubmitObjects, bundle);
    }

    //region Upload functionality

    private void startUploads(String action, ArrayList<FileSubmitObject> fileSubmitObjects, Bundle bundle) {
        ArrayList<RemoteFile> attachments = new ArrayList<>();

        final long courseId = bundle.getLong(Const.COURSE_ID);
        final long assignmentId = bundle.getLong(Const.ASSIGNMENT_ID);
        final long conversationId = bundle.getLong(Const.CONVERSATION);

        Long parentFolderId = null;
        if (bundle.containsKey(Const.PARENT_FOLDER_ID)) {
            parentFolderId = bundle.getLong(Const.PARENT_FOLDER_ID);
        }

        try {
            for (int i = 0; i < fileSubmitObjects.size(); i++) {

                // Handle upload cancellation
                if (isCanceled) {
                    stopForeground(true);
                    mNotificationManager.cancel(NOTIFICATION_ID);
                    stopSelf();
                    return;
                }

                RemoteFile attachment = null;
                FileSubmitObject fso = fileSubmitObjects.get(i);

                updateNotificationCount(fso.getName(), i + 1);

                if (ACTION_ASSIGNMENT_SUBMISSION.equals(action)) {
                    attachment = uploadSubmissionFiles(courseId, assignmentId, fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath());
                } else if (ACTION_COURSE_FILE.equals(action)) {
                    attachment = uploadCourseFiles(courseId, parentFolderId, fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath(), null);
                } else if (ACTION_USER_FILE.equals(action)) {
                    attachment = uploadUserFiles(fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath(), null, parentFolderId);
                } else if (ACTION_MESSAGE_ATTACHMENTS.equals(action)) {
                    attachment = uploadUserFiles(fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath(), MESSAGE_ATTACHMENT_PATH, null);
                } else if (ACTION_DISCUSSION_ATTACHMENT.equals(action)) {
                    attachment = uploadUserFiles(fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath(), DISCUSSION_ATTACHMENT_PATH, null);
                }

                if (attachment != null) {
                    attachments.add(attachment);
                } else {
                    // TODO Show error notification?
                }
                broadCastUploadCompleted(fso);
            }

            // Submit fileIds to the assignment
            if (ACTION_ASSIGNMENT_SUBMISSION.equals(action)) {
                submitFilesForSubmission(courseId, assignmentId, attachments);
            } else if (ACTION_DISCUSSION_ATTACHMENT.equals(action)) {
                broadCastDiscussionSuccess(attachments);
            } else {
                updateNotificationComplete();
                broadCastAllUploadsCompleted(attachments);
            }

        } catch (Exception exception) {
            updateNotification(getString(R.string.error_uploading_file));
            broadcastError(exception.getMessage());
        }

        stopSelf();
    }

    private void submitFilesForSubmission(long courseId, long assignmentId, ArrayList<RemoteFile> attachmentsIds) {
        // TODO
    }

    private RemoteFile uploadSubmissionFiles(long courseId, long assignmentId, long size, String fileName, String contentType, String path) {
        // TODO
        return null;
    }

    private RemoteFile uploadCourseFiles(long courseId, Long parentFolderId, long size, String fileName, String contentType, String path, @Nullable String parentFolderPath) throws IOException, NetworkErrorException {

        if (parentFolderPath != null && parentFolderId != null) throw new IllegalArgumentException("Cannot specify both parentFolderPath and parentFolderId");

        FileUploadParams uploadParams;
        if (parentFolderId != null) {
            uploadParams = FileFolderManager.getFileUploadParamsSynchronous(courseId, fileName, size, contentType, parentFolderId);
        } else {
            uploadParams = FileFolderManager.getFileUploadParamsSynchronous(courseId, fileName, size, contentType, parentFolderPath);
        }

        if (uploadParams == null) throw new NetworkErrorException("Unable to obtain file upload parameters");

        return FileFolderManager.uploadFileSynchronousNoRedirect(uploadParams.getUploadUrl(),
                uploadParams.getPlainTextUploadParams(),
                contentType,
                new File(path));
    }

    private RemoteFile uploadUserFiles(long size, String fileName, String contentType, String path, @Nullable String parentFolderPath, @Nullable Long parentFolderId) throws IOException, NetworkErrorException {

        if (parentFolderPath != null && parentFolderId != null) throw new IllegalArgumentException("Cannot specify both parentFolderPath and parentFolderId");

        FileUploadParams uploadParams;
        if (parentFolderId != null) {
            uploadParams = UserManager.getFileUploadParamsSynchronous(fileName, size, contentType, parentFolderId);
        } else {
            uploadParams = UserManager.getFileUploadParamsSynchronous(fileName, size, contentType, parentFolderPath);
        }

        if (uploadParams == null) throw new NetworkErrorException("Unable to obtain file upload parameters");

        return UserManager.uploadUserFileSynchronous(uploadParams.getUploadUrl(),
                uploadParams.getPlainTextUploadParams(),
                contentType,
                new File(path));
    }

    //endregion

    //region Notifications

    private void broadCastUploadCompleted(FileSubmitObject fso) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.FILENAME, fso);
        final Intent status = new Intent(UPLOAD_COMPLETED);
        status.putExtras(bundle);
        sendBroadcast(status);
    }

    private void broadCastAllUploadsCompleted(ArrayList<RemoteFile> attachments) {
        final Intent status = new Intent(ALL_UPLOADS_COMPLETED);
        status.putParcelableArrayListExtra(Const.ATTACHMENTS, attachments);
        sendBroadcast(status);
    }

    private void broadCastDiscussionSuccess(ArrayList<RemoteFile> attachments) {
        updateNotificationComplete();
        final Intent status = new Intent(ALL_UPLOADS_COMPLETED);
        status.putExtra(Const.ATTACHMENTS, attachments);
        sendBroadcast(status);
    }


    private void broadcastError(String message) {
        updateNotificationError(message);
        Bundle bundle = new Bundle();
        Intent status = new Intent(UPLOAD_ERROR);

        bundle.putString(Const.MESSAGE, message);
        status.putExtras(bundle);
        sendBroadcast(status);
    }

    private void showNotification(int size) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.canvas_logo_white)
                .setContentTitle(String.format(Locale.US, getString(R.string.uploading_file_num), 1, size))
                .setProgress(0, 0, true);
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void updateNotificationCount(final String fileName, int currentItem) {

        mNotificationBuilder.setContentTitle(String.format(Locale.US, getString(R.string.uploading_file_num), currentItem, mUploadCount))
                .setContentText(fileName);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }


    private void updateNotification(final String message) {
        mNotificationBuilder.setContentText(message);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void updateNotificationError(final String message) {
        mNotificationBuilder.setContentText(message)
                .setProgress(0, 0, false);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void updateNotificationComplete() {
        mNotificationBuilder.setProgress(0, 0, false)
                .setContentTitle(getString(R.string.files_uploaded_successfully));
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void updateSubmissionComplete() {
        mNotificationBuilder.setProgress(0, 0, false)
                .setContentTitle(getString(R.string.files_submitted_successfully));
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void updateMessageComplete() {
        mNotificationBuilder.setProgress(0, 0, false)
                .setContentTitle(getString(R.string.message_sent_successfully));
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    //endregion

    @Override
    public void onDestroy() {
        if(mNotificationManager != null) {
            if (isCanceled) {
                mNotificationManager.cancel(NOTIFICATION_ID);
            } else {
                mNotificationBuilder.setOngoing(false);
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            }
        }
        FileUploadUtils.deleteTempDirectory(getApplicationContext());
    }

    //region Helpers

    public static Bundle getUserFilesBundle(ArrayList<FileSubmitObject> fileSubmitObjects, Long parentFolderID) {
        //Long size, String fileName, String contentType, String path
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);

        if (parentFolderID != null) {
            bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderID);
        }

        return bundle;
    }

    public static Bundle getCourseFilesBundle(ArrayList<FileSubmitObject> fileSubmitObjects, long courseId, Long parentFolderID) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);
        bundle.putLong(Const.COURSE_ID, courseId);
        if (parentFolderID != null) {
            bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderID);
        }

        return bundle;
    }

    public static Bundle getAssignmentSubmissionBundle(ArrayList<FileSubmitObject> fileSubmitObjects, long courseId, long assignmentId) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);
        bundle.putLong(Const.COURSE_ID, courseId);
        bundle.putLong(Const.ASSIGNMENT_ID, assignmentId);
        return bundle;
    }

    public static Bundle getMessageBundle(ArrayList<FileSubmitObject> fileSubmitObjects, String messageText, long conversationId) {
        //Long size, String fileName, String contentType, String path
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);
        bundle.putLong(Const.CONVERSATION, conversationId);
        bundle.putString(Const.MESSAGE, messageText);

        return bundle;
    }

    //endregion
}
