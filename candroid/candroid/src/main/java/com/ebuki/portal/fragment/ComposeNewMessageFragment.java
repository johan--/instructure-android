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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.util.Rfc822Tokenizer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;
import com.instructure.candroid.R;
import com.instructure.candroid.adapter.CanvasContextSpinnerAdapter;
import com.instructure.candroid.adapter.NothingSelectedSpinnerAdapter;
import com.instructure.candroid.adapter.RecipientAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.dialog.FileUploadDialog;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.view.CanvasRecipientManager;
import com.instructure.candroid.view.IndicatorCircleView;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.ConversationManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.Recipient;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.services.FileUploadService;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ComposeNewMessageFragment extends ParentFragment implements FileUploadDialog.FileSelectionInterface {

    // Callbacks
    private StatusCallback<List<Conversation>> mConversationCanvasCallback;

    private StatusCallback<List<Course>> mCoursesCallback;
    private StatusCallback<List<Group>> mGroupsCallback;
	private EditText mMessage;
    private EditText mSubject;

    // Course Spinner
    private Spinner mCourseSpinner;
    private List<Course> mCourses;
    private List<Group> mGroups;
    private CanvasContext mSelectedCourse;

    // Recipient Chips
    private ArrayList<String> mIds;
    private RecipientAdapter mChipsAdapter;
    private RecipientEditTextView mChipsTextView;
    private boolean mIsChooseRecipientsVisable = false;
    private boolean mIsSendEnabled = true;
    private RelativeLayout mChipsTextViewWrapper;

    // Attachment Uploads
    private ArrayList<FileSubmitObject> mAttachmentsList = new ArrayList<>();
    private FileUploadDialog mUploadFileSourceFragment;
    private BroadcastReceiver mErrorBroadcastReceiver;
    private BroadcastReceiver mAllUploadsCompleteBroadcastReceiver;
    private boolean mNeedsUnregister;
    private ProgressDialog mProgressDialog;
    private IndicatorCircleView mAttachmentCount;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        if (isTablet(context)) {
            return FRAGMENT_PLACEMENT.DIALOG;
        }
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public String getFragmentTitle() {
        return ChooseMessageRecipientsFragment.getRecipientsTitle(getResources().getString(R.string.noRecipients), getResources().getString(R.string.users));
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return getString(R.string.compose);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.compose_message, container, false);
        setupDialogToolbar(rootView);
        mMessage = (EditText) rootView.findViewById(R.id.message);
        mSubject = (EditText) rootView.findViewById(R.id.subject);
        mCourseSpinner   = (Spinner)  rootView.findViewById(R.id.course_spinner);
        mChipsTextViewWrapper = (RelativeLayout) rootView.findViewById(R.id.recipientWrapper);
        mChipsTextView = (RecipientEditTextView) rootView.findViewById(R.id.recipient);
        mChipsTextView.setTokenizer(new Rfc822Tokenizer());

        mIds = new ArrayList<>();
        mChipsAdapter = new RecipientAdapter(getActivity().getApplicationContext());
        mChipsTextView.setAdapter(mChipsAdapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpCallback();
        CourseManager.getAllFavoriteCourses(true, mCoursesCallback);
        GroupManager.getFavoriteGroups(mGroupsCallback, true);

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSubject.getWindowToken(), 0);
    }

    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        if(getDialogToolbar() != null && placement == FRAGMENT_PLACEMENT.DIALOG) {
            getDialogToolbar().setBackgroundColor(getResources().getColor(R.color.defaultPrimary));
        } else {
            Navigation navigation = getNavigation();
            if(navigation != null) {
                final int color = getResources().getColor(R.color.defaultPrimary);
                navigation.setActionBarStatusBarColors(color, color);
            }
        }
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		//Check to see if we need to add a new title for the action bar after selecting recipients

		//See if we now need a compose button or if we need to remove it.
		getActivity().supportInvalidateOptionsMenu();

        if(mUploadFileSourceFragment != null){
            mUploadFileSourceFragment.onActivityResult(requestCode, resultCode, data);
        }
	}

    public void setUpCallback(){
        mConversationCanvasCallback = new StatusCallback<List<Conversation>>() {

            @Override
            public void onResponse(retrofit2.Response<List<Conversation>> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()){
                    return;
                }
                sendMessageSuccess();
            }
        };

        mCoursesCallback = new StatusCallback<List<Course>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Course>> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()){
                    return;
                }
                mCourses = response.body();
                populateCourseSpinnerAdapter();
            }
        };

        mGroupsCallback = new StatusCallback<List<Group>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Group>> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()){
                    return;
                }
                mGroups = response.body();
                populateCourseSpinnerAdapter();
            }
        };
    }

    private void sendMessageSuccess() {
        //Let the conversation list know to update itself
        Intent intent = new Intent();
        intent.putExtra(Const.CHANGED, true);
        getActivity().setResult(Activity.RESULT_OK, intent);
        showToast(R.string.successSendingMessage);
        ChooseMessageRecipientsFragment.allRecipients.clear();
        //Allow time for the success message to pop up.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Clear out everything so back pressed does not think data is being lost.
                mMessage.setText("");
                mChipsTextView.setText("");
                mSubject.setText("");
                getActivity().onBackPressed();
            }
        }, TimeUnit.SECONDS.toMillis(1));

        //close keyboard if it is showing
        View view = getActivity().getCurrentFocus();
        if(view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void populateCourseSpinnerAdapter() {
        if (mGroups == null || mCourses == null) {
            return;
        }
        final CanvasContextSpinnerAdapter adapter = CanvasContextSpinnerAdapter.newAdapterInstance(getContext(), mCourses, mGroups);
        mCourseSpinner.setAdapter(new NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_item_nothing_selected, getContext()));
        if (mSelectedCourse != null) {
            mCourseSpinner.setOnItemSelectedListener(null); // prevent listener from firing the when selection is placed
            mCourseSpinner.setSelection(adapter.getPosition(mSelectedCourse) + 1, false); //  + 1 is for the nothingSelected position
            courseWasSelected();
        }
        mCourseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // position zero is nothingSelected prompt
                    mChipsTextView.removeAllRecipientEntry();
                    CanvasContext canvasContext = adapter.getItem(position - 1); // -1 to account for nothingSelected item
                    mSelectedCourse = canvasContext;
                    courseWasSelected();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void courseWasSelected() {
        mChipsTextViewWrapper.setVisibility(View.VISIBLE);
        getActivity().invalidateOptionsMenu();
        mIsChooseRecipientsVisable = true;
        mChipsAdapter.getCanvasRecipientManager().setCanvasContext(mSelectedCourse);

        ViewTreeObserver vto = mChipsTextView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                populateRecipients();
                ViewTreeObserver obs = mChipsTextView.getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });
    }

    public void populateRecipients() {
        for(Recipient recipient : ChooseMessageRecipientsFragment.allRecipients){
            RecipientEntry recipientEntry = new RecipientEntry(recipient.getIdAsLong(), recipient.getName(), recipient.getStringId(), "", recipient.getAvatarURL(), recipient.getUserCount(), recipient.getItemCount(), true,
                    recipient.getCommonCourses() != null ? recipient.getCommonCourses().keySet() : null,
                    recipient.getCommonGroups() != null ?  recipient.getCommonGroups().keySet() : null);

            mChipsTextView.appendRecipientEntry(recipientEntry);
        }


        ChooseMessageRecipientsFragment.allRecipients.clear();
    }


    public String getRecipientsString() {
        if(mChipsTextView == null || mChipsTextView.getSelectedRecipients() == null || mChipsTextView.getSelectedRecipients().size() == 0) {
            return getString(R.string.noRecipients);
        }
        if(mChipsTextView.getSelectedRecipients().size() > 2) {
            return mChipsTextView.getSelectedRecipients().get(0).getName() + String.format(Locale.getDefault(), getString(R.string.andMore), mChipsTextView.getSelectedRecipients().size() - 1);
        } else {
            String participants = "";
            for (int i = 0; i < mChipsTextView.getSelectedRecipients().size(); i++) {
                if (!participants.equals("")) {
                    participants += ", ";
                }

                participants += mChipsTextView.getSelectedRecipients().get(i).getName();
            }
            return participants;
        }
    }

    @Override
    public void onFilesSelected(ArrayList<FileSubmitObject> fileSubmitObjects) {
        mAttachmentsList = fileSubmitObjects;

        updateAttachmentCount();
    }

    private void updateAttachmentCount() {
        if(mAttachmentsList != null && mAttachmentsList.size() > 0) {
            mAttachmentCount.setVisibility(View.VISIBLE);
            mAttachmentCount.setText(Integer.toString(mAttachmentsList.size()));
        } else {
            mAttachmentCount.setVisibility(View.GONE);
        }
    }

    private BroadcastReceiver getAllUploadsCompleted() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}

                sendMessageSuccess();

                if(mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        };
    }

    private BroadcastReceiver getErrorReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}

                if(mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                final Bundle bundle = intent.getExtras();
                String errorMessage = bundle.getString(Const.MESSAGE);
                if(null == errorMessage || "".equals(errorMessage)){
                    errorMessage = getString(R.string.errorUploadingFile);
                }
                showToast(errorMessage);
            }
        };
    }

    private void registerReceivers() {
        mErrorBroadcastReceiver = getErrorReceiver();
        mAllUploadsCompleteBroadcastReceiver = getAllUploadsCompleted();

        getActivity().registerReceiver(mErrorBroadcastReceiver, new IntentFilter(FileUploadService.UPLOAD_ERROR));
        getActivity().registerReceiver(mAllUploadsCompleteBroadcastReceiver, new IntentFilter(FileUploadService.ALL_UPLOADS_COMPLETED));

        mNeedsUnregister = true;
    }

    private void unregisterReceivers() {
        if(getActivity() == null || !mNeedsUnregister){return;}

        if(mErrorBroadcastReceiver != null){
            getActivity().unregisterReceiver(mErrorBroadcastReceiver);
            mErrorBroadcastReceiver = null;
        }

        if(mAllUploadsCompleteBroadcastReceiver != null){
            getActivity().unregisterReceiver(mAllUploadsCompleteBroadcastReceiver);
            mAllUploadsCompleteBroadcastReceiver = null;
        }

        mNeedsUnregister = false;
    }

    @Override
    public boolean handleBackPressed() {
        mMessage.setText("");
        mChipsTextView.setText("");
        mSubject.setText("");
        return super.handleBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSelectedCourse == null) {
            mSelectedCourse = ChooseMessageRecipientsFragment.canvasContext;
        }

        ChooseMessageRecipientsFragment.canvasContext = null;
        setUpCallback();


        dataLossResume(mMessage, Const.DATA_LOSS_COMPOSE_NEW_MESSAGE);
        dataLossAddTextWatcher(mMessage, Const.DATA_LOSS_COMPOSE_NEW_MESSAGE);
    }

    @Override
    public void onPause() {
        dataLossPause(mMessage, Const.DATA_LOSS_COMPOSE_NEW_MESSAGE);
        CanvasRecipientManager.getInstance(getContext()).saveCache();
        CanvasRecipientManager.releaseInstance();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceivers();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceivers();
    }

    //region Data Submission.

    private boolean isValidNewMessage() {
        if (mSelectedCourse == null) {
            showToast(R.string.noCourseSelected);
            return false;
        } else if (mChipsTextView.getSelectedRecipients().size() == 0) {
            showToast(R.string.messageHasNoRecipients);
            return false;
        } else if ("".equals(mMessage.getText().toString().trim())) {
            showToast(R.string.emptyMessage);
            return false;
        }
        return true;
    }

    void sendMessage(boolean group){
        for(Recipient R: ChooseMessageRecipientsFragment.allRecipients) {
            if(!mIds.contains(R.getStringId())){
                mIds.add(R.getStringId());
            }
        }

        for(RecipientEntry entry : mChipsTextView.getSelectedRecipients()){
            if(!mIds.contains(entry.getDestination())){
                mIds.add(entry.getDestination());
            }
        }

        if(mAttachmentsList.size() == 0) {
            ConversationManager.createConversation(mIds, mMessage.getText().toString(), mSubject.getText().toString(), mSelectedCourse.getContextId(), null, group, mConversationCanvasCallback);
        } else {
            //show dialog while it is uploading the attachments
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.panda_loading));
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            Intent intent = new Intent(getActivity(), FileUploadService.class);
            Bundle bundle = FileUploadService.getNewMessageBundle(mAttachmentsList, mIds, mSubject.getText().toString(), mMessage.getText().toString(), group, mSelectedCourse.getContextId());
            intent.setAction(FileUploadService.ACTION_NEW_MESSAGE_ATTACHMENTS);
            intent.putExtras(bundle);
            getActivity().startService(intent);
            mAttachmentsList.clear();
        }

        dataLossDeleteStoredData(Const.DATA_LOSS_COMPOSE_NEW_MESSAGE);
    }

    //endregion

    //region ActionBar

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_send) {
            if(!APIHelper.hasNetworkConnection()) {
                Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                return true;
            }

            if (isValidNewMessage()) {
                //	Determine whether or not the user intended this as a group message
                if (ChooseMessageRecipientsFragment.isPossibleGroupMessage() || (ChooseMessageRecipientsFragment.allRecipients.size() +  mChipsTextView.getSelectedRecipients().size()) > 1) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.groupDialogTitle)
                            .setMessage(R.string.groupDialogMessage)
                            .setPositiveButton(R.string.group, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sendMessage(true);
                                }
                            })
                            .setNegativeButton(R.string.individually, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sendMessage(false);
                                }
                            })
                            .setCancelable(true)
                            .create()
                            .show();
                } else {
                    //Send the message
                    mIsSendEnabled = false;
                    getActivity().invalidateOptionsMenu();
                    sendMessage(false);
                }
            }
            return true;
        }  else if (item.getItemId() == R.id.menu_choose_recipients) {
            //We want to assume that nothing has been selected yet.
            if (mSelectedCourse == null) {
                showToast(R.string.noCourseSelected);
                return true;
            }
            ChooseMessageRecipientsFragment.allRecipients.clear();
            Navigation navigation = getNavigation();
            if(navigation != null){
                navigation.addFragment(FragUtils.getFrag(ChooseMessageRecipientsFragment.class, createBundle(mSelectedCourse)));
            }

            return true;
        } else if (item.getItemId() == R.id.menu_add_attachment) {

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_compose_message, menu);
        //make all the icons white
        for(int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
        }
        RelativeLayout container = (RelativeLayout)menu.findItem(R.id.menu_add_attachment).getActionView();
        mAttachmentCount = (IndicatorCircleView)container.findViewById(R.id.attachmentCount);
        mAttachmentCount.setBackgroundColor(getResources().getColor(R.color.canvasRed));

        //set the attachment count. we invalidate the options menu a few times so we can't assume we don't
        //have any attachments here
        updateAttachmentCount();

        ImageView paperclip = (ImageView)container.findViewById(R.id.attachment);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = FileUploadDialog.createAttachmentsBundle(getRecipientsString(), mAttachmentsList);
                mUploadFileSourceFragment = FileUploadDialog.newInstance(getFragmentManager(),bundle);
                mUploadFileSourceFragment.setTargetFragment(ComposeNewMessageFragment.this, 1337);
                mUploadFileSourceFragment.show(getFragmentManager(), FileUploadDialog.TAG);
            }
        });
        paperclip.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        menu.findItem(R.id.menu_choose_recipients).setVisible(mIsChooseRecipientsVisable);
        menu.findItem(R.id.menu_send).setEnabled(mIsSendEnabled);
    }

    //endregion

    //region Intent

    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
    }

    public static Bundle createBundle(CanvasContext canvasContext, Boolean fromPeople) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putBoolean(Const.FROM_PEOPLE, fromPeople);
        return bundle;
    }

    //endregion

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}
