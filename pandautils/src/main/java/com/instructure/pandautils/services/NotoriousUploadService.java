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

package com.instructure.pandautils.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.DiscussionManager;
import com.instructure.canvasapi2.managers.NotoriousManager;
import com.instructure.canvasapi2.managers.SubmissionManager;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.models.NotoriousConfig;
import com.instructure.canvasapi2.models.NotoriousSession;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.models.notorious.NotoriousResultWrapper;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.ContextKeeper;
import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.R;
import com.instructure.pandautils.utils.Const;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static com.instructure.pandautils.services.FileUploadService.CHANNEL_ID;

public class NotoriousUploadService extends IntentService {

    private final static int notificationId = 666;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private StatusCallback<NotoriousConfig> notoriousConfigCallback;
    private StatusCallback<NotoriousSession> notoriousSessionCallback;
    private StatusCallback<NotoriousResultWrapper> notoriousUploadTokenCanvasCallback;
    private StatusCallback<Submission> submissionCanvasCallback;
    private StatusCallback<DiscussionEntry> discussionEntryCanvasCallback;
    private String notoriousDomain;
    private String notoriousUploadToken;
    private Uri mediaUri;
    private ACTION action;
    private Assignment assignment;
    private DiscussionEntry discussionEntry;
    private long discussionId;
    private String message;
    private CanvasContext canvasContext;
    private long studentId;
    private boolean isGroupComment;

    private String pageId;

    private Runnable fileUpload;

    public NotoriousUploadService() {
        super(NotoriousUploadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null || intent.getSerializableExtra(Const.ACTION) == null) {
            return;
        }
        action = (ACTION) intent.getSerializableExtra(Const.ACTION);

        if (ACTION.isSubmissionComment(action)) {
            assignment = intent.getParcelableExtra(Const.ASSIGNMENT);
            studentId = intent.getLongExtra(Const.STUDENT_ID, ApiPrefs.getUser().getId());
            isGroupComment = intent.getBooleanExtra(Const.IS_GROUP, false);
            pageId = intent.getStringExtra(Const.PAGE_ID);
        }
        else if (ACTION.isAssignmentSubmission(action)){
            assignment = intent.getParcelableExtra(Const.ASSIGNMENT);
        }
        else if (ACTION.isDiscussionAttachment(action)) {
            discussionEntry = intent.getParcelableExtra(Const.DISCUSSION_ENTRY);
            message = intent.getStringExtra(Const.MESSAGE);
            discussionId = intent.getLongExtra(Const.DISCUSSION_ID, 0);
            canvasContext = intent.getParcelableExtra(Const.CANVAS_CONTEXT);
        }

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                        .setContentTitle(getString(R.string.notificationTitle))
                        .setContentText(getString(R.string.preparingUpload))
                        .setOngoing(true);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel(CHANNEL_ID);

        mediaUri = intent.getParcelableExtra(Const.URI);

        notificationManager.notify(notificationId, builder.build());

        startForeground(notificationId, builder.build());

        startFileUpload();
    }

    private void createNotificationChannel(String channelId) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        if(notificationManager == null) return;

        //Prevents recreation of notification channel if it exists.
        List<NotificationChannel> channelList = notificationManager.getNotificationChannels();
        for (NotificationChannel channel : channelList) {
            if(channelId.equals(channel.getId())) {
                return;
            }
        }

        CharSequence name = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsName);
        String description = ContextKeeper.appContext.getString(R.string.notificationChannelNameFileUploadsDescription);

        //Create the channel and add the group
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        channel.enableLights(false);
        channel.enableVibration(false);

        //create the channel
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        //clean up
        builder.setOngoing(false);
        notificationManager.cancel(notificationId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //clean up again in case of swipe to dismiss
        builder.setOngoing(false);
        notificationManager.cancel(notificationId);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startFileUpload() {
        fileUpload = new Runnable() {
            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    uploadStartedToast();
                    String contentType = FileUtils.getMimeType(mediaUri.getPath());
                    File file = new File(mediaUri.getPath());
                    Response response = NotoriousManager.uploadFileSynchronous(notoriousUploadToken, file, contentType);
                    if (response != null && response.code() == 201) {
                        callNextAPIcall(response, NotoriousManager.getMediaIdSynchronous(notoriousUploadToken, file.getName(), contentType));
                    } else {
                        uploadError(response);
                    }
                } catch (Exception e){
                    uploadError(e);
                }
            }
        };

        setupCallbacks();

        NotoriousManager.getConfiguration(notoriousConfigCallback);
    }

    private void setupCallbacks() {

        notoriousConfigCallback = new StatusCallback<NotoriousConfig>() {
            @Override
            public void onResponse(Response<NotoriousConfig> response, LinkHeaders linkHeaders, ApiType type) {
                NotoriousConfig notoriousConfig = response.body();
                if (notoriousConfig.isEnabled()) {
                    notoriousDomain = notoriousConfig.getDomain();
                    NotoriousManager.startSession(notoriousSessionCallback);
                } else {
                    uploadError(notoriousConfig, response);
                }
            }

            @Override
            public void onFail(Call<NotoriousConfig> callResponse, Throwable error, Response response) {
                if (!APIHelper.hasNetworkConnection()) {
                    onNoNetwork();
                } else {
                    uploadError(error);
                }
            }
        };

        notoriousSessionCallback = new StatusCallback<NotoriousSession>() {

            @Override
            public void onResponse(Response<NotoriousSession> response, LinkHeaders linkHeaders, ApiType type) {
                NotoriousSession notoriousSession = response.body();
                ApiPrefs.setNotoriousDomain(notoriousDomain);
                ApiPrefs.setNotoriousToken(notoriousSession.getToken());
                notificationManager.notify(
                        notificationId,
                        builder.build());
                NotoriousManager.getUploadToken(notoriousUploadTokenCanvasCallback);
            }

            @Override
            public void onFail(Call<NotoriousSession> callResponse, Throwable error, Response response) {
                if (!APIHelper.hasNetworkConnection()) {
                    onNoNetwork();
                } else {
                    uploadError(error);
                }
            }
        };

        notoriousUploadTokenCanvasCallback = new StatusCallback<NotoriousResultWrapper>() {

            @Override
            public void onResponse(Response<NotoriousResultWrapper> response, LinkHeaders linkHeaders, ApiType type) {
                try {
                    NotoriousResultWrapper resultWrapper = response.body();
                    builder.setContentText(getString(R.string.uploadingFile));
                    builder.setProgress(0, 0, true);
                    notificationManager.notify(
                            notificationId,
                            builder.build());

                    if (resultWrapper.getResult() != null && resultWrapper.getResult().getError() == null) {
                        notoriousUploadToken = resultWrapper.getResult().getId();
                        new Thread(fileUpload).start();
                    } else {
                        uploadError(resultWrapper, response);
                    }
                } catch(Exception e) {
                    sendErrorBroadcast();
                }
            }

            @Override
            public void onFail(Call<NotoriousResultWrapper> callResponse, Throwable error, Response response) {
                if (!APIHelper.hasNetworkConnection()) {
                    onNoNetwork();
                } else {
                    uploadError(response);
                }
            }
        };

        submissionCanvasCallback = new StatusCallback<Submission>() {

            @Override
            public void onResponse(Response<Submission> response, LinkHeaders linkHeaders, ApiType type) {
                if (response.body() != null) {
                    builder.setContentText(getString(R.string.fileUploadSuccess))
                            .setProgress(100, 100, false)
                            .setOngoing(false);

                    notificationManager.notify(
                            notificationId,
                            builder.build());

                    Intent intent = new Intent(Const.SUBMISSION_COMMENT_SUBMITTED);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                    Intent successIntent = new Intent(Const.UPLOAD_SUCCESS);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(successIntent);

                    Intent mediaUploadIntent = new Intent(Const.ACTION_MEDIA_UPLOAD_SUCCESS);
                    mediaUploadIntent.putExtra(Const.MEDIA_FILE_PATH, mediaUri.getPath());
                    mediaUploadIntent.putExtra(Const.PAGE_ID, pageId);
                    mediaUploadIntent.putParcelableArrayListExtra(Const.SUBMISSION_COMMENT_LIST, new ArrayList<>(response.body().getSubmissionComments()));
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(mediaUploadIntent);
                }

                //Stop the service
                stopSelf();
            }

            @Override
            public void onFail(Call<Submission> callResponse, Throwable error, Response response) {
                uploadError(error);
            }
        };

        discussionEntryCanvasCallback = new StatusCallback<DiscussionEntry>() {
            @Override
            public void onResponse(Response<DiscussionEntry> response, LinkHeaders linkHeaders, ApiType type) {
                if (discussionEntry != null) {
                    builder.setContentText(getString(R.string.fileUploadSuccess))
                            .setProgress(100, 100, false)
                            .setOngoing(false);

                    notificationManager.notify(
                            notificationId,
                            builder.build());

                    Intent intent = new Intent(Const.DISCUSSION_REPLY_SUBMITTED);
                    intent.putExtra(Const.DISCUSSION_ENTRY, (Parcelable)discussionEntry);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                    Intent successIntent = new Intent(Const.UPLOAD_SUCCESS);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(successIntent);
                }

                //Stop the service
                stopSelf();
            }

            @Override
            public void onFail(Call<DiscussionEntry> callResponse, Throwable error, Response response) {
                uploadError(error);
            }
        };
    }

    private void uploadError(Throwable e) {
        sendErrorBroadcast();
        Logger.e("NOTORIOUS EXCEPTION: " + e);
        showErrorNotification();
    }

    private void uploadError(NotoriousResultWrapper resultWrapper, Response response) {
        sendErrorBroadcast();
        Logger.e("NOTORIOUS XML/RESPONSE ERROR: " + resultWrapper.toString() + " RESPONSE: " + response.message() + " CODE: " + response.code());
        showErrorNotification();
    }

    private void uploadError(@Nullable Response response) {
        sendErrorBroadcast();
        if (response == null) {
            uploadError();
        } else {
            Logger.e("NOTORIOUS RESPONSE ERROR: " + response.message() + " CODE: " + response.code());
            showErrorNotification();
        }
    }

    private void uploadError(NotoriousConfig config, Response response) {
        sendErrorBroadcast();
        Logger.e("NOTORIOUS CONFIG/RESPONSE ERROR: " + config.toString() + " REASON: " + response.message() + " CODE: " + response.code());

        showErrorNotification();
    }

    private void uploadError() {
        sendErrorBroadcast();
        Logger.e("UNKNOWN ERROR CAUSE");
        showErrorNotification();
    }

    private void showErrorNotification() {
        builder.setContentText(getString(R.string.errorUploadingFile))
                .setProgress(100, 100, false)
                .setOngoing(false);
        notificationManager.notify(
                notificationId,
                builder.build());
    }

    private void uploadStartedToast() {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getContext(), R.string.uploadMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendErrorBroadcast() {
        Intent errorIntent = new Intent(Const.ACTION_MEDIA_UPLOAD_FAIL);
        errorIntent.putExtra(Const.MEDIA_FILE_PATH, mediaUri.getPath());
        errorIntent.putExtra(Const.PAGE_ID, pageId);
        errorIntent.putExtra(Const.ERROR, true);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(errorIntent);
    }

    private void callNextAPIcall(Response response, NotoriousResultWrapper resultWrapper) {
        CanvasContext course;
        String mediaId = resultWrapper.getResult().getId();

        long assignmentId = 0;
        if(assignment != null) {
            assignmentId = assignment.getId();
            course = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, assignment.getCourseId(), Const.COURSE);
        } else {
            course = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, canvasContext.getId(), Const.COURSE);
        }

        String mediaType = FileUtils.mediaTypeFromNotoriousCode(resultWrapper.getResult().getMediaType());
        if (ACTION.isSubmissionComment(action)) {
            SubmissionManager.postMediaSubmissionComment(course, assignmentId, studentId, mediaId, mediaType, isGroupComment, submissionCanvasCallback);
        }
        else if(ACTION.isAssignmentSubmission(action)){
            SubmissionManager.postMediaSubmission(course, assignment.getId(), Const.MEDIA_RECORDING, mediaId, mediaType, submissionCanvasCallback);
        }
        else if(ACTION.isDiscussionAttachment(action)) {

            //this is the format that Canvas expects
            String attachment = String.format("<p><a id='media_comment_%s' " +
                    "class='instructure_inline_media_comment %s'" +
                    "href='/media_objects/%s'>this is a media comment</a></p>", mediaId, mediaType, mediaId);

            if(message == null) {
                message = "";
            }

            attachment += "\n" + message;

            if(discussionEntry.getParent() == null) {
                DiscussionManager.postToDiscussionTopic(canvasContext, discussionId, attachment, discussionEntryCanvasCallback);
            } else {
                DiscussionManager.replyToDiscussionEntry(canvasContext, discussionId, discussionEntry.getId(), attachment, discussionEntryCanvasCallback);
            }
        }
        else {
            uploadError(response);
        }
    }

    public enum ACTION {
        SUBMISSION_COMMENT,
        ASSIGNMENT_SUBMISSION,
        DISCUSSION_COMMENT,
        TEACHER_SUBMISSION_COMMENT;

        public static boolean isSubmissionComment(ACTION action) {
            return action == SUBMISSION_COMMENT;
        }

        public static boolean isAssignmentSubmission(ACTION action){
            return action == ASSIGNMENT_SUBMISSION;
        }

        public static boolean isDiscussionAttachment(ACTION action) {
            return action == DISCUSSION_COMMENT;
        }
    }

    private void onNoNetwork() {
        builder.setContentText(getString(R.string.noNetwork))
                .setOngoing(false);
        notificationManager.notify(
                notificationId,
                builder.build());
    }

    private Context getContext() {
        return getApplicationContext();
    }
}
