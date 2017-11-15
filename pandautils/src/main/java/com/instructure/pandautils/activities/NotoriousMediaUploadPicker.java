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

package com.instructure.pandautils.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.R;
import com.instructure.pandautils.services.NotoriousUploadService;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.FileUploadUtils;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.utils.RequestCodes;
import com.instructure.pandautils.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v4.content.FileProvider.getUriForFile;

public class NotoriousMediaUploadPicker extends Activity {

    private Uri capturedImageURI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notorious_media_upload_picker);

        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.rootView);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView takeVideo = (TextView) rootView.findViewById(R.id.takeVideo);
        takeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newVideo();
            }
        });
        TextView selectMedia = (TextView) rootView.findViewById(R.id.chooseMedia);
        selectMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseMedia();
            }
        });
    }

    @TargetApi(23)
    private void newVideo() {
        if(PermissionUtils.hasPermissions(NotoriousMediaUploadPicker.this, PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO)) {
            takeVideoBecausePermissionsAlreadyGranted();
        } else {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO), PermissionUtils.PERMISSION_REQUEST_CODE);
        }
    }
    

    @TargetApi(23)
    private void chooseMedia() {
        if(PermissionUtils.hasPermissions(NotoriousMediaUploadPicker.this, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //only let the user use local images (no picasa...)
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.setType("video/*, audio/*");
            File file = new File(getFilesDir(), "/image/*");
            intent.setDataAndType(getUriForFile(NotoriousMediaUploadPicker.this, NotoriousMediaUploadPicker.this.getApplicationContext().getPackageName() + Const.FILE_PROVIDER_AUTHORITY, file), "video/*");
            startActivityForResult(intent, RequestCodes.SELECT_MEDIA);
        } else {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE);
        }
    }

    private void takeVideoBecausePermissionsAlreadyGranted() {
        //check to see if the device has a camera
        if(!Utils.hasCameraAvailable(NotoriousMediaUploadPicker.this)) {
            Toast.makeText(getApplicationContext(), R.string.noCameraOnDevice, Toast.LENGTH_LONG).show();
            return;
        }

        //create new Intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFileUri());  // set the image file name
        startActivityForResult(cameraIntent, RequestCodes.TAKE_VIDEO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                takeVideoBecausePermissionsAlreadyGranted();
            } else {
                Toast.makeText(NotoriousMediaUploadPicker.this, R.string.permissionDenied, Toast.LENGTH_LONG).show();
            }
        } else if(requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                chooseMedia();
            } else {
                Toast.makeText(NotoriousMediaUploadPicker.this, R.string.permissionDenied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri mediaUri = null;
        if (resultCode != Activity.RESULT_OK) {
            return;
        } else if (requestCode == RequestCodes.SELECT_MEDIA) {
            Uri tempMediaUri = data.getData();

            if(tempMediaUri == null){
                setResult(RESULT_CANCELED);
                finish();
            }

            mediaUri = Uri.parse(FileUploadUtils.getPath(this, tempMediaUri));
        } else if (requestCode == RequestCodes.TAKE_VIDEO) {
            mediaUri = capturedImageURI;
        }

        if (mediaUri != null) {
            Intent serviceIntent = new Intent(this, NotoriousUploadService.class);
            serviceIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            serviceIntent.putExtra(Const.URI, mediaUri);
            if (NotoriousUploadService.ACTION.isSubmissionComment((NotoriousUploadService.ACTION) getIntent().getSerializableExtra(Const.ACTION))) {
                serviceIntent.putExtra(Const.ACTION, NotoriousUploadService.ACTION.SUBMISSION_COMMENT);
                serviceIntent.putExtra(Const.ASSIGNMENT, getIntent().getParcelableExtra(Const.ASSIGNMENT));
                serviceIntent.putExtra(Const.STUDENT_ID, getIntent().getLongExtra(Const.STUDENT_ID, ApiPrefs.getUser().getId()));
                serviceIntent.putExtra(Const.IS_GROUP, getIntent().getBooleanExtra(Const.IS_GROUP, false));
            } else if(NotoriousUploadService.ACTION.isAssignmentSubmission((NotoriousUploadService.ACTION) getIntent().getSerializableExtra(Const.ACTION))){
                serviceIntent.putExtra(Const.ACTION, NotoriousUploadService.ACTION.ASSIGNMENT_SUBMISSION);
                serviceIntent.putExtra(Const.ASSIGNMENT, getIntent().getParcelableExtra(Const.ASSIGNMENT));
            } else if(NotoriousUploadService.ACTION.isDiscussionAttachment((NotoriousUploadService.ACTION) getIntent().getSerializableExtra(Const.ACTION))){
                serviceIntent.putExtra(Const.ACTION, NotoriousUploadService.ACTION.DISCUSSION_COMMENT);
                serviceIntent.putExtra(Const.DISCUSSION_ENTRY, getIntent().getParcelableExtra(Const.DISCUSSION_ENTRY));
                serviceIntent.putExtra(Const.MESSAGE, getIntent().getStringExtra(Const.MESSAGE));
                serviceIntent.putExtra(Const.DISCUSSION_ID, getIntent().getLongExtra(Const.DISCUSSION_ID, 0));
                serviceIntent.putExtra(Const.CANVAS_CONTEXT, getIntent().getParcelableExtra(Const.CANVAS_CONTEXT));
            }

            startService(serviceIntent);
            setResult(RESULT_OK);
            Intent intent = new Intent(Const.UPLOAD_STARTED);
            LocalBroadcastManager.getInstance(NotoriousMediaUploadPicker.this).sendBroadcast(intent);
            finish();
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private Uri getOutputMediaFileUri() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Canvas");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("pandaUtils", "NotoriousMediaUploadPicker failed to make Dir.");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir,
                "canvas_media_comment" + timeStamp + ".mp4");

        //set the file uri for use with Notorious uploading. Notorious doesn't deal with content:// uris
        capturedImageURI = Uri.parse(mediaFile.getAbsolutePath());

        //return the content uri for starting the intent
        return getUriForFile(NotoriousMediaUploadPicker.this, NotoriousMediaUploadPicker.this.getApplicationContext().getPackageName() + Const.FILE_PROVIDER_AUTHORITY, mediaFile);
    }

    public static Intent createIntentForAssignmentSubmission(Context context, Assignment assignment) {
        Intent intent = new Intent(context, NotoriousMediaUploadPicker.class);
        intent.putExtra(Const.ACTION, NotoriousUploadService.ACTION.ASSIGNMENT_SUBMISSION);
        intent.putExtra(Const.ASSIGNMENT, (Parcelable) assignment);
        return intent;
    }

    public static Intent createIntentForSubmissionComment(Context context, Assignment assignment) {
        Intent intent = new Intent(context, NotoriousMediaUploadPicker.class);
        intent.putExtra(Const.ACTION, NotoriousUploadService.ACTION.SUBMISSION_COMMENT);
        intent.putExtra(Const.ASSIGNMENT, (Parcelable) assignment);
        return intent;
    }

    public static Intent createIntentForTeacherSubmissionComment(Context context, Assignment assignment, long studentId, boolean isGroupComment) {
        Intent intent = new Intent(context, NotoriousMediaUploadPicker.class);
        intent.putExtra(Const.ACTION, NotoriousUploadService.ACTION.SUBMISSION_COMMENT);
        intent.putExtra(Const.ASSIGNMENT, (Parcelable) assignment);
        intent.putExtra(Const.STUDENT_ID, studentId);
        intent.putExtra(Const.IS_GROUP, isGroupComment);
        return intent;
    }

    public static Intent createIntentForDiscussionReply(Context context, DiscussionEntry discussionEntry, String message, long discussionId, CanvasContext canvasContext) {
        Intent intent = new Intent(context, NotoriousMediaUploadPicker.class);
        intent.putExtra(Const.ACTION, NotoriousUploadService.ACTION.DISCUSSION_COMMENT);
        intent.putExtra(Const.DISCUSSION_ENTRY, (Parcelable) discussionEntry);
        intent.putExtra(Const.MESSAGE, message);
        intent.putExtra(Const.DISCUSSION_ID, discussionId);
        intent.putExtra(Const.CANVAS_CONTEXT, (Parcelable) canvasContext);
        return intent;
    }
}
