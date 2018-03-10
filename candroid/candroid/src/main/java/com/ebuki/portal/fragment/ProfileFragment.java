/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.candroid.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.PandaAvatarActivity;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.FragUtils;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.FileUploadManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.AvatarWrapper;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Enrollment;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.utils.AvatarCropActivity;
import com.instructure.pandautils.utils.AvatarCropConfig;
import com.instructure.pandautils.utils.ColorUtils;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.LoaderUtils;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.utils.ProfileUtils;
import com.instructure.pandautils.utils.RequestCodes;
import com.instructure.pandautils.utils.Utils;
import com.instructure.pandautils.views.RippleView;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends OrientationChangeFragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<AvatarWrapper> {

    private static final int REQUEST_CODE_PERMISSIONS_TAKE_PHOTO = 223;
    private static final int REQUEST_CODE_PERMISSIONS_GALLERY = 332;

    private View nameChangeWrapper;
    private EditText name;
    private RippleView files;
    private ImageView headerImage, nameChangeDone;
    private RelativeLayout clickContainer;
    private CircleImageView avatar;
    private Bundle loaderBundle = null;
    private TextView bio, enrollment;

    private Uri mCapturedImageURI;

    //Callbacks.
    private StatusCallback<User> updateUserCallback;
    private StatusCallback<User> updateCanvasCallback;
    private StatusCallback<User> userPermissionCallback;

    //User
    private User user;

    //Logic
    ApplicationManager applicationManager;
    boolean canUpdateName;
    boolean canUpdateAvatar;
    private boolean editMode;

    public static final String noPictureURL = "images/dotted_pic.png";

    private OnProfileChangedCallback onProfileChangedCallback;

    public interface OnProfileChangedCallback {
        void onProfileChangedCallback();
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DIALOG;
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.profile);
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(this, true);
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        View rootView = getLayoutInflater().inflate(R.layout.profile_fragment_layout, container, false);
        setupDialogToolbar(rootView);
        styleToolbar();
        clickContainer = rootView.findViewById(R.id.clickContainer);
        name = rootView.findViewById(R.id.userName);
        headerImage = rootView.findViewById(R.id.headerImage);
        nameChangeDone = rootView.findViewById(R.id.nameChangeDone);
        nameChangeDone.setImageDrawable(ColorUtils.colorIt(Color.BLACK, nameChangeDone.getDrawable()));
        nameChangeDone.setOnClickListener(nameChangedListener);
        nameChangeWrapper = rootView.findViewById(R.id.userNameWrapper);
        ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
        avatar = rootView.findViewById(R.id.avatar);
        name.setBackground(colorDrawable);
        files = rootView.findViewById(R.id.files);

        enrollment = rootView.findViewById(R.id.enrollment);
        bio = rootView.findViewById(R.id.bio);
        bio.setMovementMethod(new ScrollingMovementMethod());

        if(!editMode){
            hideEditTextView();
        }

        setUpCallbacks();
        setUpListeners();

        user = ApiPrefs.getUser();
        setUpUserViews();
        UserManager.getSelfWithPermissions(true, userPermissionCallback);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog != null && !isTablet(getActivity())) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof OnProfileChangedCallback){
            onProfileChangedCallback = ((OnProfileChangedCallback)activity);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RequestCodes.CROP_IMAGE && resultCode == Activity.RESULT_OK) {
            String croppedPath = data.getData().getPath();
            File croppedFile = new File(croppedPath);
            loaderBundle = createLoaderBundle("profilePic.jpg", "image/jpeg", croppedPath, croppedFile.length(), true);
            showProgressBar();
            LoaderUtils.restartLoaderWithBundle(getLoaderManager(), loaderBundle, this, R.id.avatarLoaderID);

        } else if (requestCode == RequestCodes.CAMERA_PIC_REQUEST && resultCode != Activity.RESULT_CANCELED) {
            // Don't want to directly decode the stream because we could get out of memory errors
            // if the URI is null, try to get the information from the intent that we saved earlier
            if (mCapturedImageURI == null) {
                //save the intent information in case we get booted from memory.
                SharedPreferences settings = getActivity().getSharedPreferences(ApplicationManager.PREF_NAME, 0);
                mCapturedImageURI = Uri.parse(settings.getString("ProfileFragment-URI", null));
            }

            // If it's still null, tell the user there is an error and return.
            if (mCapturedImageURI == null) {
                showToast(R.string.errorGettingPhoto);
                return;
            }

            // Open image for cropping
            AvatarCropConfig config = new AvatarCropConfig(mCapturedImageURI);
            Intent cropIntent = AvatarCropActivity.createIntent(getContext(), config);
            startActivityForResult(cropIntent, RequestCodes.CROP_IMAGE);

        } else if (requestCode == RequestCodes.PICK_IMAGE_GALLERY && resultCode != Activity.RESULT_CANCELED) {
            if (data.getData() != null) {
                Uri u = data.getData();
                String urlPath = u.getPath();

                if (u.getPath().contains("googleusercontent")) {
                    urlPath = changeGoogleURL(urlPath);
                    ProfileFragment.this.user.setAvatarUrl(urlPath);
                    UserManager.updateUsersAvatar(urlPath, updateCanvasCallback);
                    return;
                }

                // Open image for cropping
                AvatarCropConfig config = new AvatarCropConfig(u);
                Intent cropIntent = AvatarCropActivity.createIntent(getContext(), config);
                startActivityForResult(cropIntent, RequestCodes.CROP_IMAGE);
            }

        } else if (requestCode == Const.PANDA_AVATAR_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String pandaPath = data.getStringExtra(Const.PATH);
                long size = data.getLongExtra(Const.SIZE, 0);
                //the api will rename the avatar automatically for us
                loaderBundle = createLoaderBundle("pandaAvatar.png", "image/png", pandaPath, size, false);
                showProgressBar();
                LoaderUtils.restartLoaderWithBundle(getLoaderManager(), loaderBundle, this, R.id.avatarLoaderID);
            }
        }
    }

    private String changeGoogleURL(String url){
        int start = url.indexOf("http");
        int end = url.indexOf("-d");

        url = url.substring(start, end + 1);

        return url;
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        if (canUpdateName) {
            inflater.inflate(R.menu.menu_edit_username, menu);
        }

        if(canUpdateAvatar) {
            inflater.inflate(R.menu.menu_update_avatar, menu);
        }

        inflater.inflate(R.menu.menu_about_me, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Prevent all actions besides About when there is no network
        if(item.getItemId() != R.id.about && !APIHelper.hasNetworkConnection()) {
            Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_edit_username:
                showEditTextView();
                break;
            case R.id.menu_take_photo:
                newPhoto();
                break;
            case R.id.menu_choose_from_gallery:
                chooseFromGallery();
                break;
            case R.id.menu_set_to_default:
                UserManager.updateUsersAvatar(noPictureURL, updateCanvasCallback);
                break;
            case R.id.menu_create_panda_avatar:
                startActivityForResult(new Intent(getActivity(), PandaAvatarActivity.class), Const.PANDA_AVATAR_RESULT_CODE);
                break;
            case R.id.about:
                if(user != null) {
                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setTitle(R.string.about)
                            .setView(R.layout.dialog_about)
                            .show();

                    if (dialog != null) {
                        TextView domain = dialog.findViewById(R.id.domain);
                        TextView loginId = dialog.findViewById(R.id.loginId);
                        TextView email = dialog.findViewById(R.id.email);
                        TextView version = dialog.findViewById(R.id.version);

                        domain.setText(ApiPrefs.getDomain());
                        loginId.setText(user.getLoginId());
                        email.setText(user.getEmail());
                        version.setText(getText(R.string.canvasVersionNum) + " " + ApplicationManager.getVersionName(getActivity()));
                    }
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void newPhoto(){
        //check to see if the device has a camera
        if(!Utils.hasCameraAvailable(getActivity())) {
        //this device doesn't have a camera, show a crouton that lets the user know
            showToast(R.string.noCameraOnDevice);
            return;
        }

        if(PermissionUtils.hasPermissions(getActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)) {
            takeNewPhotoBecausePermissionsAlreadyGranted();
        } else {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA), REQUEST_CODE_PERMISSIONS_TAKE_PHOTO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
            if (requestCode == REQUEST_CODE_PERMISSIONS_TAKE_PHOTO) {
                takeNewPhotoBecausePermissionsAlreadyGranted();
            } else if (requestCode == REQUEST_CODE_PERMISSIONS_GALLERY) {
                chooseFromGallery();
            }
        } else {
            Toast.makeText(getActivity(), R.string.permissionDenied, Toast.LENGTH_LONG).show();
        }
    }

    private void takeNewPhotoBecausePermissionsAlreadyGranted() {
        //let the user take a picture
        //get the location of the saved picture
        String fileName = "profilePic_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);

        mCapturedImageURI = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (mCapturedImageURI != null) {
            //save the intent information in case we get booted from memory.
            SharedPreferences settings = getContext().getSharedPreferences(ApplicationManager.PREF_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("ProfileFragment-URI", mCapturedImageURI.toString());
            editor.apply();
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        cameraIntent.putExtra(Const.IS_OVERRIDDEN, true);
        startActivityForResult(cameraIntent, RequestCodes.CAMERA_PIC_REQUEST);
    }

    private void chooseFromGallery(){
        if (!PermissionUtils.hasPermissions(getActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS_GALLERY);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        File file = new File(getContext().getFilesDir(), "/image/*");
        intent.setDataAndType(FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + Const.FILE_PROVIDER_AUTHORITY, file), "image/*");
        startActivityForResult(intent, RequestCodes.PICK_IMAGE_GALLERY);
    }

    private void styleToolbar() {
        if(getDialogToolbar() != null) {
            getDialogToolbar().setTitle("");
            getDialogToolbar().setBackgroundColor(getResources().getColor(R.color.semi_transparent));
        }
    }

    private void setUpListeners() {
        files.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                Navigation navigation = getNavigation();
                if(navigation != null) {
                    dismissAllowingStateLoss();
                    navigation.addFragment(FragUtils.getFrag(FileListFragment.class, getActivity()), Navigation.NavigationPosition.FILES);
                }
            }
        });

        clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editMode) {
                    hideEditTextView();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        applicationManager = (ApplicationManager) getActivity().getApplication();

        if(savedInstanceState == null) {
            //if we rotate in edit mode we don't want it to kick us out of edit mode
            editMode = false;
        }

        //Restore loader if necessary
        LoaderUtils.restoreLoaderFromBundle(getLoaderManager(), savedInstanceState, this, R.id.avatarLoaderID);
        //Prevent the keyboard from popping up
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LoaderUtils.saveLoaderBundle(outState, loaderBundle);
        super.onSaveInstanceState(outState);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    private View.OnClickListener nameChangedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String nameText = name.getText().toString().trim();
            if(!TextUtils.isEmpty(nameText)){
                name.setText(nameText);
                hideEditTextView();

                UserManager.updateUserShortName(nameText, updateUserCallback);
            }
        }
    };

    public void setUpCallbacks(){
        updateUserCallback = new StatusCallback<User>() {
            @Override
            public void onResponse(retrofit2.Response<User> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()){
                    return;
                }
                User user = response.body();
                name.setText(user.getShortName());
                User cacheUser = ApiPrefs.getUser();
                cacheUser.setName(user.getShortName());
                ApiPrefs.setUser(cacheUser);
                ProfileFragment.this.user.setShortName(user.getShortName());
                setUpUserAvatar();
                if(onProfileChangedCallback != null){
                    onProfileChangedCallback.onProfileChangedCallback();
                }
            }
        };


        updateCanvasCallback = new StatusCallback<User>() {
            @Override
            public void onResponse(retrofit2.Response<User> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()){
                    return;
                }
                user = response.body();
                setUpUserAvatar();
                if(onProfileChangedCallback != null){
                    onProfileChangedCallback.onProfileChangedCallback();
                }
            }
        };

        userPermissionCallback = new StatusCallback<User>() {
            @Override
            public void onResponse(retrofit2.Response<User> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()) {
                    return;
                }
                canUpdateAvatar = response.body().canUpdateAvatar();
                canUpdateName = response.body().canUpdateName();
                getActivity().invalidateOptionsMenu();
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Loader test
    /////////////////////////////////////////////////////////////////////////

    public static class PostAvatarLoader extends android.support.v4.content.AsyncTaskLoader<AvatarWrapper> {
        final String name;
        final String contentType;
        final String path;
        final long size;
        final boolean deleteOnCompletion;

        PostAvatarLoader(Context context, String name, String contentType, String path, long size, boolean deleteOnCompletion) {
            super(context);

            this.name = name;
            this.contentType = contentType;
            this.path = path;
            this.size = size;
            this.deleteOnCompletion = deleteOnCompletion;
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public AvatarWrapper loadInBackground() {
            AvatarWrapper wrapper = FileUploadManager.uploadAvatarSynchronous(name, size, contentType, path);
            if (deleteOnCompletion) new File(path).delete();
            return wrapper;
        }

        @Override protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

    }

    /**
     * Used to instantiate the PostAvatarLoader
     * @param id - a unique ID
     * @param args - a bundle, containing:
     *             -String name
     *             -String content type
     *             -String path
     *             -int size
     * @return Loader
     */
    @Override
    public android.support.v4.content.Loader<AvatarWrapper> onCreateLoader(int id, Bundle args) {
        return new PostAvatarLoader(getActivity(), args.getString(Const.NAME), args.getString(Const.CONTENT_TYPE), args.getString(Const.PATH), args.getLong(Const.SIZE), args.getBoolean(Const.DELETE));
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<AvatarWrapper> loader, AvatarWrapper data) {
        hideProgressBar();

        if(data != null && data.getAvatar() != null) {
            ProfileFragment.this.user.setAvatarUrl(data.getAvatar().getUrl());
            setUpUserAvatar();
            UserManager.updateUsersAvatar(data.getAvatar().getUrl(), updateCanvasCallback);
        }
        else if(data != null) {
            //check to see the error messages
            if(data.getError() == AvatarWrapper.ERROR_QUOTA_EXCEEDED) {
                showToast(R.string.fileQuotaExceeded);
            }
            else if(data.getError() == AvatarWrapper.ERROR_UNKNOWN) {
                showToast(R.string.errorUploadingFile);
            }
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<AvatarWrapper> loader) {}


    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private void showEditTextView(){
        editMode = true;
        nameChangeDone.setVisibility(View.VISIBLE);
        nameChangeWrapper.setBackgroundResource(R.drawable.profile_name_edit_bg);
        name.setEnabled(true);
        name.setSelection(name.getText().length());
    }

    private void hideEditTextView(){
        editMode = false;
        nameChangeDone.setVisibility(View.GONE);
        nameChangeWrapper.setBackground(null);
        name.setEnabled(false);
    }

    private void setUpUserViews(){
        name.setText(user.getShortName());

        String enrolledAs = "";
        List<Enrollment> enrollments = user.getEnrollments();
        for(Enrollment enrollment : enrollments) {
            enrolledAs = enrolledAs + enrollment.getType() + ",";
        }

        if(enrolledAs.endsWith(",")) {
            enrolledAs = enrolledAs.substring(0, enrolledAs.length() - 1);
        }

        if(TextUtils.isEmpty(enrolledAs)) {
            enrollment.setVisibility(View.GONE);
        } else {
            enrollment.setText(enrolledAs);
        }

        //show the bio if one exists
        if(!TextUtils.isEmpty(user.getBio()) && !user.getBio().equals("null")) {
            bio.setText(user.getBio());
        }
        setUpUserAvatar();
    }

    private void setUpUserAvatar(){
        ProfileUtils.configureAvatarView(getContext(), user, avatar);
    }

    private Bundle createLoaderBundle(String name, String contentType, String path, long size, boolean deleteOnCompletion){
        Bundle bundle = new Bundle();
        bundle.putString(Const.NAME, name);
        bundle.putString(Const.CONTENT_TYPE, contentType);
        bundle.putString(Const.PATH, path);
        bundle.putLong(Const.SIZE, size);
        bundle.putBoolean(Const.DELETE, deleteOnCompletion);

        return bundle;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if (extras.containsKey(Const.USER)) {
            user = extras.getParcelable(Const.USER);
        }
    }

    public static Bundle createBundle(User user, CanvasContext canvasContext, FRAGMENT_PLACEMENT placement) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.USER, user);
        extras.putSerializable(Const.PLACEMENT, placement);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, FRAGMENT_PLACEMENT placement) {
        Bundle extras = createBundle(canvasContext);
        extras.putSerializable(Const.PLACEMENT, placement);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}
