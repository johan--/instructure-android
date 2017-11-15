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
package com.instructure.teacher.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.RemoteFile;
import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.utils.RequestCodes;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.teacher.R;
import com.instructure.teacher.adapters.FileUploadAdapter;
import com.instructure.teacher.services.FileUploadService;
import com.instructure.teacher.utils.FileUploadUtils;
import com.instructure.teacher.utils.TeacherPrefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FileUploadDialog extends AppCompatDialogFragment {

    private static final String KEY_IS_SINGLE_SELECT = "isSingleSelect";

    public enum FileUploadType {ASSIGNMENT, COURSE, USER, MESSAGE, DISCUSSION}

    public interface DialogLifecycleCallback {
        void onCancel(Dialog dialog);
        void onAllUploadsComplete(@Nullable Dialog dialog, List<RemoteFile> uploadedFiles);
    }

    public interface OnSingleFileSelectedListener {
        void onSingleFileSelected(FileSubmitObject file);
    }

    private OnSingleFileSelectedListener mSelectedListener;

    // member variables
    private FileUploadType mUploadType = FileUploadType.ASSIGNMENT;

    // bundled data

    private Assignment mAssignment;

    @Nullable private Course mCourse;
    @Nullable private Long mParentFolderID;
    private boolean mIsSingleSelect;

    @BindView(R.id.allowed_extensions) TextView mAllowedExtensions;
    @BindView(R.id.file_list) ListView mFileListView;
    @BindView(R.id.add_buttons_container) ViewGroup mAddButtonsContainer;

    private ProgressDialog mProgressDialog;

    // dialog header info
    private String mPositiveText;

    // data
    private FileUploadAdapter mAdapter;
    private ArrayList<FileSubmitObject> mFileList = new ArrayList<>();
    private Uri submitUri;

    // receivers
    private BroadcastReceiver mAllUploadsCompleteBroadcastReceiver;
    private BroadcastReceiver mUploadBroadcastReceiver;
    private BroadcastReceiver mErrorBroadcastReceiver;

    private boolean mNeedsUnregister;
    private DialogLifecycleCallback mDialogLifecycleCallback;

    public static FileUploadDialog newInstance(FragmentManager manager, Bundle bundle) {
        Fragment fragment = manager.findFragmentByTag(FileUploadDialog.class.getSimpleName());
        if (fragment instanceof FileUploadDialog) {
            FileUploadDialog dialog = (FileUploadDialog) fragment;
            dialog.dismissAllowingStateLoss();
        }

        FileUploadDialog uploadFileDialog = new FileUploadDialog();
        uploadFileDialog.setArguments(bundle);
        uploadFileDialog.setRetainInstance(true);
        return uploadFileDialog;
    }

    public static FileUploadDialog newInstanceSingleSelect(
            @NonNull FragmentManager manager, @NonNull Bundle bundle, @NonNull OnSingleFileSelectedListener listener) {
        bundle.putBoolean(KEY_IS_SINGLE_SELECT, true);
        FileUploadDialog dialog = newInstance(manager, bundle);
        dialog.setOnSingleFileSelectedListener(listener);
        dialog.setRetainInstance(true);
        return dialog;
    }

    public void setOnSingleFileSelectedListener(OnSingleFileSelectedListener listener) {
        mSelectedListener = listener;
    }

    //region  LifeCycle

    @Override
    public void onStart() {
        super.onStart();
        registerReceivers();
    }

    @Override
    public void onStop() {
        unregisterReceivers();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mDialogLifecycleCallback != null) {
            mDialogLifecycleCallback.onCancel(getDialog());
        }
        onDismiss(dialog);
    }

    public void onDismiss(DialogInterface dialog) {
        unregisterReceivers();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void setDialogLifecycleCallback(DialogLifecycleCallback dialogLifecycleCallback) {
        this.mDialogLifecycleCallback = dialogLifecycleCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loadBundleData();
        initProgressDialog();
        handleUriContents();

        mAdapter = new FileUploadAdapter(getActivity(), mFileList, new FileUploadAdapter.OnFileEvent() {
            @Override
            public void onRemoveFile() {
                refreshFileButtonsVisibility();
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.attachments)
                .setView(initViews())
                .setPositiveButton(mPositiveText, null)
                .setNegativeButton(R.string.teacher_cancel, null)
                .create();

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positive.setTextColor(ThemePrefs.getButtonColor());
                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        positiveClicked();
                    }
                });
                Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                negative.setTextColor(ThemePrefs.getButtonColor());
                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), FileUploadService.class);
                        intent.setAction(FileUploadService.ACTION_CANCEL_UPLOAD);
                        getActivity().startService(intent);

                        onCancel(dialog);
                        dismiss();
                    }
                });
            }
        });

        return dialog;
    }

    //endregion

    //region upload

    private void positiveClicked() {
        uploadFiles();
    }

    public void uploadFiles() {
        if (mFileList.size() == 0) {
            if (mIsSingleSelect) {
                mAddButtonsContainer.setVisibility(View.VISIBLE);
                if (mSelectedListener != null) {
                    mSelectedListener.onSingleFileSelected(null);
                }
                dismiss();
                return;
            }
            Toast.makeText(getActivity(), R.string.no_files_uploaded, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mUploadType == FileUploadType.ASSIGNMENT) {
            if (!checkIfFileSubmissionAllowed()) { //see if we can actually submit files to this assignment
                Toast.makeText(getActivity(), R.string.file_upload_not_supported, Toast.LENGTH_SHORT).show();
                return;
            }

            //make sure that what we've uploaded can still be uploaded (allowed extensions)
            for (int i = 0; i < mFileList.size(); i++) {
                if (!isExtensionAllowed(mFileList.get(i).getFullPath())) {
                    //didn't match any of the extensions, don't upload
                    Toast.makeText(getActivity(), R.string.one_or_more_extension_not_allowed, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        startUploadService();
    }

    public void startUploadService() {
        setViewsToUploading();
        Bundle bundle = null;
        Intent intent = new Intent(getActivity(), FileUploadService.class);
        if (mUploadType == FileUploadType.USER) {
            bundle = FileUploadService.getUserFilesBundle(mFileList, mParentFolderID);
            intent.setAction(FileUploadService.ACTION_USER_FILE);
        } else if (mUploadType == FileUploadType.COURSE && mCourse != null) {
            bundle = FileUploadService.getCourseFilesBundle(mFileList, mCourse.getId(), mParentFolderID);
            intent.setAction(FileUploadService.ACTION_COURSE_FILE);
        } else if (mUploadType == FileUploadType.MESSAGE) {
            bundle = FileUploadService.getUserFilesBundle(mFileList, null);
            intent.setAction(FileUploadService.ACTION_MESSAGE_ATTACHMENTS);
        } else if (mUploadType == FileUploadType.DISCUSSION) {
            bundle = FileUploadService.getUserFilesBundle(mFileList, null);
            intent.setAction(FileUploadService.ACTION_DISCUSSION_ATTACHMENT);
        } else if(mCourse != null) {
            bundle = FileUploadService.getAssignmentSubmissionBundle(mFileList, mCourse.getId(), mAssignment.getId());
            intent.setAction(FileUploadService.ACTION_ASSIGNMENT_SUBMISSION);
        }

        if(bundle != null) {
            intent.putExtras(bundle);
            getActivity().startService(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //no result
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == RequestCodes.CAMERA_PIC_REQUEST) {
            // Attempt to restore URI in case were were booted from memory
            if (mCapturedImageURI == null) {
                mCapturedImageURI = Uri.parse(TeacherPrefs.getTempCaptureUri());
            }

            //if it's still null, tell the user there is an error and return.
            if (mCapturedImageURI == null) {
                Toast.makeText(getActivity(), R.string.error_getting_photo, Toast.LENGTH_SHORT).show();
                return;
            }

            new GetUriContentsAsyncTask(getActivity(), mCapturedImageURI).execute();
        } else {
            if (data != null && data.getData() != null) {
                Uri imageURI = data.getData();
                new GetUriContentsAsyncTask(getActivity(), imageURI).execute();
            }
        }
    }

    private void handleUriContents() {
        //we only want to open the dialog in the beginning if we're not coming from an external source (sharing)
        if (submitUri != null && mUploadType != FileUploadType.MESSAGE && mUploadType != FileUploadType.DISCUSSION) {
            new GetUriContentsAsyncTask(getActivity(), submitUri).execute();
        }
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.uploading_file));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    //endregion

    //region Views

    private View initViews() {
        final View rootView = View.inflate(getActivity(), R.layout.dialog_file_upload, null);

        ButterKnife.bind(this, rootView);

        // listview
        mFileListView.setAdapter(mAdapter);
        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FileSubmitObject fileSubmitObject = mFileList.get(i);
                File file = new File(fileSubmitObject.getFullPath());
                Intent newIntent = new Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(getContext(), getContext().getPackageName() + Const.FILE_PROVIDER_AUTHORITY, file));

                if (!file.setReadable(true)) {
                    Toast.makeText(getContext(), R.string.no_apps, Toast.LENGTH_SHORT).show();
                    return;
                }

                // need this to let other apps read the URI
                newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    getActivity().startActivity(newIntent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.no_apps, Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkAllowedExtensions();

        refreshFileButtonsVisibility();

        return rootView;
    }

    private void setViewsToUploading() {
        mAdapter.setFilesToUploading();
        mAddButtonsContainer.setVisibility(View.GONE);
        getDialog().setTitle(R.string.uploading_files);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().findViewById(R.id.file_list).announceForAccessibility(getString(R.string.loading));
    }

    private void refreshFileButtonsVisibility() {
        if(mIsSingleSelect && mFileList.size() > 0) {
            mAddButtonsContainer.setVisibility(View.GONE);
        } else {
            mAddButtonsContainer.setVisibility(View.VISIBLE);
        }
    }

    //endregion

    //region Receivers

    private void registerReceivers() {
        mUploadBroadcastReceiver = getSingleUploadCompleted();
        mErrorBroadcastReceiver = getErrorReceiver();
        mAllUploadsCompleteBroadcastReceiver = getAllUploadsCompleted();

        getActivity().registerReceiver(mUploadBroadcastReceiver, new IntentFilter(FileUploadService.UPLOAD_COMPLETED));
        getActivity().registerReceiver(mErrorBroadcastReceiver, new IntentFilter(FileUploadService.UPLOAD_ERROR));
        getActivity().registerReceiver(mAllUploadsCompleteBroadcastReceiver, new IntentFilter(FileUploadService.ALL_UPLOADS_COMPLETED));

        mNeedsUnregister = true;
    }

    private void unregisterReceivers() {
        if (getActivity() == null || !mNeedsUnregister) {
            return;
        }

        if (mUploadBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mUploadBroadcastReceiver);
            mUploadBroadcastReceiver = null;
        }

        if (mErrorBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mErrorBroadcastReceiver);
            mErrorBroadcastReceiver = null;
        }

        if (mAllUploadsCompleteBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mAllUploadsCompleteBroadcastReceiver);
            mAllUploadsCompleteBroadcastReceiver = null;
        }

        mNeedsUnregister = false;
    }

    private BroadcastReceiver getSingleUploadCompleted() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (!isAdded()) {
                    return;
                }
                final Bundle bundle = intent.getExtras();
                FileSubmitObject fso = bundle.getParcelable(Const.FILENAME);
                mAdapter.setFileState(fso, FileSubmitObject.STATE.COMPLETE);
            }
        };
    }

    private BroadcastReceiver getAllUploadsCompleted() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(getActivity(), R.string.files_uploaded_successfully, Toast.LENGTH_SHORT).show();
                if (mDialogLifecycleCallback != null) {
                    mDialogLifecycleCallback.onAllUploadsComplete(getDialog(), intent.<RemoteFile>getParcelableArrayListExtra(Const.ATTACHMENTS));
                }
                dismiss();
            }
        };
    }

    private BroadcastReceiver getErrorReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (!isAdded()) {
                    return;
                }

                final Bundle bundle = intent.getExtras();
                String errorMessage = bundle.getString(Const.MESSAGE);
                if (null == errorMessage || "".equals(errorMessage)) {
                    errorMessage = getString(R.string.error_uploading_file);
                }
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        };
    }

    //endregion

    //region Helpers

    private boolean checkIfFileSubmissionAllowed() {
        return mAssignment != null && (mAssignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.ONLINE_UPLOAD));
    }

    //used when the user hits the submit button after sharing files, we want to make sure they are allowed
    private boolean isExtensionAllowed(String filePath) {
        if (mAssignment != null && (mAssignment.getAllowedExtensions() == null || mAssignment.getAllowedExtensions().size() == 0)) {
            //there is an assignment, but no extension restriction...
            return true;
        }
        //get the extension and compare it to the list of allowed extensions
        int index = filePath.lastIndexOf(".");
        if (mAssignment != null && index != -1) {
            String ext = filePath.substring(index + 1);
            for (int i = 0; i < mAssignment.getAllowedExtensions().size(); i++) {
                if (mAssignment.getAllowedExtensions().get(i).trim().equalsIgnoreCase(ext)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean addIfExtensionAllowed(FileSubmitObject fileSubmitObject) {
        if (mAssignment != null && (mAssignment.getAllowedExtensions() == null || mAssignment.getAllowedExtensions().size() == 0)) {
            addToFileSubmitObjects(fileSubmitObject);
            return true;
        }

        //get the extension and compare it to the list of allowed extensions
        int index = fileSubmitObject.getFullPath().lastIndexOf(".");
        if (mAssignment != null && index != -1) {
            String ext = fileSubmitObject.getFullPath().substring(index + 1);
            for (int i = 0; i < mAssignment.getAllowedExtensions().size(); i++) {
                if (mAssignment.getAllowedExtensions().get(i).trim().equalsIgnoreCase(ext)) {
                    addToFileSubmitObjects(fileSubmitObject);
                    return true;
                }
            }
            //didn't match any of the extensions, don't upload
            Toast.makeText(getActivity(), R.string.extension_not_allowed, Toast.LENGTH_SHORT).show();
            return false;
        }

        //if we're sharing it from an external source we won't know which assignment they're trying to
        //submit to, so we won't know if there are any extension limits
        //also, the assignment and/or course could be null due to memory pressures
        if ((mAssignment == null || mCourse == null)) {
            addToFileSubmitObjects(fileSubmitObject);
            return true;
        }
        //don't want to try to upload it since it's not allowed.
        Toast.makeText(getActivity(), R.string.extension_not_allowed, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void addToFileSubmitObjects(FileSubmitObject fileSubmitObject) {
        if (mIsSingleSelect) {
            if (mSelectedListener != null) {
                mSelectedListener.onSingleFileSelected(fileSubmitObject);
            }
            dismiss();
        } else {
            mFileList.add(fileSubmitObject);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void checkAllowedExtensions() {
        //if there are only certain file types that are allowed, let the user know
        if (mAssignment != null && mAssignment.getAllowedExtensions() != null && mAssignment.getAllowedExtensions().size() > 0) {
            mAllowedExtensions.setVisibility(View.VISIBLE);
            String extensions = getString(R.string.allowed_extensions);
            for (int i = 0; i < mAssignment.getAllowedExtensions().size(); i++) {
                extensions += mAssignment.getAllowedExtensions().get(i);
                if (mAssignment.getAllowedExtensions().size() > 1 && i < mAssignment.getAllowedExtensions().size() - 1) {
                    extensions += ",";
                }
            }
            mAllowedExtensions.setText(extensions);
        } else {
            mAllowedExtensions.setVisibility(View.GONE);
        }
    }

    //endregion

    private class GetUriContentsAsyncTask extends AsyncTask<Void, Void, FileSubmitObject> {

        private Context context;
        private @NonNull Uri uri;

        GetUriContentsAsyncTask(Context context, @NonNull Uri uri) {
            this.context = context;
            this.uri = uri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage(getString(R.string.loading_files_indeterminate));
            mProgressDialog.show();
        }

        @Override
        protected FileSubmitObject doInBackground(Void... params) {
            try {
                ContentResolver cr = context.getContentResolver();
                String mimeType = FileUploadUtils.getFileMimeType(cr, uri);
                String fileName = FileUploadUtils.getFileNameWithDefault(cr, uri, mimeType);
                return FileUploadUtils.getFileSubmitObjectFromInputStream(context, uri, fileName, mimeType);
            } catch(SecurityException se) {
                //some file manager gave us back the wrong type of URI
                return null;
            }
        }

        @Override
        protected void onPostExecute(FileSubmitObject submitObject) {
            mProgressDialog.dismiss();
            if(submitObject != null) {
                if ("".equals(submitObject.getErrorMessage())) {
                    addIfExtensionAllowed(submitObject);
                } else {
                    Toast.makeText(getActivity(), submitObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.errorUploadingFile, Toast.LENGTH_SHORT).show();
            }
        }

    }

    //region Data

    public static Bundle createBundle(Uri submitURI, FileUploadType type, Long parentFolderId) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.URI, submitURI);
        bundle.putSerializable(Const.UPLOAD_TYPE, type);
        if (parentFolderId != null) {
            bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderId);
        }
        return bundle;
    }

    public static Bundle createAttachmentsBundle(String userName, ArrayList<FileSubmitObject> defaultFileList) {
        Bundle bundle = createBundle(null, FileUploadType.MESSAGE, null);
        bundle.putString(Const.USER, userName);
        bundle.putParcelableArrayList(Const.FILES, defaultFileList);
        return bundle;
    }

    public static Bundle createDiscussionsBundle(String userName, @Nullable ArrayList<FileSubmitObject> defaultFileList) {
        Bundle bundle = createBundle(null, FileUploadType.DISCUSSION, null);
        bundle.putString(Const.USER, userName);
        if (defaultFileList == null) defaultFileList = new ArrayList<>();
        bundle.putParcelableArrayList(Const.FILES, defaultFileList);
        return bundle;
    }

    public static Bundle createFilesBundle(Uri submitURI, Long parentFolderId) {
        return createBundle(submitURI, FileUploadType.USER, parentFolderId);
    }

    public static Bundle createCourseBundle(Uri submitURI, Course course, Long parentFolderId) {
        Bundle bundle = createBundle(submitURI, FileUploadType.COURSE, parentFolderId);
        bundle.putParcelable(Const.CANVAS_CONTEXT, course);
        return bundle;
    }

    public static Bundle createAssignmentBundle(Uri submitURI, Course course, Assignment assignment) {
        Bundle bundle = createBundle(submitURI, FileUploadType.ASSIGNMENT, null);
        bundle.putParcelable(Const.CANVAS_CONTEXT, course);
        bundle.putParcelable(Const.ASSIGNMENT, assignment);
        return bundle;
    }

    public void loadBundleData() {
        Bundle bundle = getArguments();

        mUploadType = (FileUploadType) bundle.getSerializable(Const.UPLOAD_TYPE);
        CanvasContext mCanvasContext = bundle.getParcelable(Const.CANVAS_CONTEXT);
        mAssignment = bundle.getParcelable(Const.ASSIGNMENT);
        submitUri = bundle.getParcelable(Const.URI);
        mIsSingleSelect = bundle.getBoolean(KEY_IS_SINGLE_SELECT);

        mParentFolderID = (bundle.containsKey(Const.PARENT_FOLDER_ID)) ? bundle.getLong(Const.PARENT_FOLDER_ID) : null;
        mCourse = (mUploadType != FileUploadType.USER) ? (Course) mCanvasContext : null;

        if (mUploadType == FileUploadType.ASSIGNMENT) {
            mPositiveText = getString(R.string.turn_in);
        } else if (mUploadType == FileUploadType.COURSE) {
            mPositiveText = getString(R.string.teacher_upload);
        } else if (mUploadType == FileUploadType.MESSAGE) {
            mPositiveText = getString(R.string.teacher_okay);
            mFileList = bundle.getParcelableArrayList(Const.FILES);
        } else if (mUploadType == FileUploadType.DISCUSSION) {
            mPositiveText = getString(R.string.teacher_okay);
            mFileList = bundle.getParcelableArrayList(Const.FILES);
        } else {
            mPositiveText = getString(R.string.teacher_upload);
        }

        if (mFileList == null) mFileList = new ArrayList<>();
    }

    //endregion

    //region Attachment Options

    private Uri mCapturedImageURI;

    @OnClick({R.id.from_camera,
            R.id.from_photo_gallery,
            R.id.from_device})
    void onAddButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.from_camera:
                pickFromCamera();
                break;
            case R.id.from_photo_gallery:
                pickFromGallery();
                break;
            case R.id.from_device:
                pickFromDevice();
                break;
        }
    }

    private boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public void onSourceSelected(Intent intent, int requestCode) {
        if (getParentFragment() != null) {
            getParentFragment().startActivityForResult(intent, requestCode);
        } else {
            FileUploadDialog.this.startActivityForResult(intent, requestCode);
        }
    }

    private void pickFromCamera() {
        if (!PermissionUtils.hasPermissions(getActivity(), PermissionUtils.CAMERA)) {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.CAMERA), PermissionUtils.PERMISSION_REQUEST_CODE);
            return;
        }
        String fileName = "pic_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File file = new File(FileUploadUtils.getExternalCacheDir(getContext()), fileName);
        mCapturedImageURI = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + Const.FILE_PROVIDER_AUTHORITY, file);

        if (mCapturedImageURI != null) {
            //save the intent information in case we get booted from memory.
            TeacherPrefs.setTempCaptureUri(mCapturedImageURI.toString());
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        if (isIntentAvailable(getActivity(), cameraIntent.getAction())) {
            onSourceSelected(cameraIntent, RequestCodes.CAMERA_PIC_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                pickFromCamera();
            } else {
                Toast.makeText(getActivity(), R.string.permissionDenied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        File file = new File(getContext().getFilesDir(), "/image/*");
        intent.setDataAndType(FileProvider.getUriForFile(getContext(), getContext().getPackageName() + Const.FILE_PROVIDER_AUTHORITY, file), "image/*");
        onSourceSelected(intent, RequestCodes.PICK_IMAGE_GALLERY);
    }

    private void pickFromDevice() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        onSourceSelected(intent, RequestCodes.PICK_FILE_FROM_DEVICE);
    }

    //endregion
}
