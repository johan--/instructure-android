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

package com.ebuki.portal.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ebuki.portal.R;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.dialog.FileUploadDialog;
import com.ebuki.portal.util.Analytics;
import com.ebuki.portal.util.VisibilityAnimator;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.ExternalToolManager;
import com.instructure.canvasapi2.managers.SubmissionManager;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.LTITool;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.activities.NotoriousMediaUploadPicker;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.RequestCodes;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;

public class AddSubmissionFragment extends ParentFragment {

	//TODO: make sure they're a student!

	//View
	private ScrollView scrollView;

	private LinearLayout textEntryContainer;
	private LinearLayout urlEntryContainer;
    private LinearLayout mArcEntryContainer;

	private WebView webView;

	private TextView textEntry;
	private EditText textSubmission;
	private Button submitTextEntry;

	private TextView urlEntry;
	private EditText urlSubmission;
	private Button submitURLEntry;

	private TextView fileEntry;

    private TextView mediaUpload;

    private TextView mArcUpload;
    private EditText mArcSubmission;
    private Button mSubmitArcEntry;

	//Passed In Assignment and course
	private Assignment assignment;
	private Course course;

	
	//Assignment Permissions
	private boolean isOnlineTextAllowed;
	private boolean isUrlEntryAllowed;
	private boolean isFileEntryAllowed;
    private boolean isMediaRecordingAllowed;

	//Timer
	private Timer timer;
	private Timer isPageDone;

	//Handler
	private Handler mHandler;

    private boolean isFileUploadCanceled = false;
    private FileUploadDialog.DialogLifecycleCallback uploadDialogLifecycleCallback;

    private StatusCallback<Submission> canvasCallbackSubmission;
    private StatusCallback<List<LTITool>> mLTIToolCallback;

    private LTITool mArcLTITool;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.assignmentTabSubmission);
    }

    ///////////////////////////////////////////////////////////////////////////
	// LifeCycle
	///////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mHandler = new Handler();

		View rootView = getLayoutInflater().inflate(R.layout.add_submission_fragment, container, false);

		scrollView = (ScrollView)rootView.findViewById(R.id.activity_root);

		textEntryContainer = (LinearLayout)rootView.findViewById(R.id.textEntryContainer);
		urlEntryContainer = (LinearLayout)rootView.findViewById(R.id.urlEntryContainer);

		webView = (WebView)rootView.findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);

		textEntry = (TextView)rootView.findViewById(R.id.textEntryHeader);
		textSubmission = (EditText)rootView.findViewById(R.id.textEntry);
		submitTextEntry = (Button)rootView.findViewById(R.id.submitTextEntry);

		urlEntry = (TextView)rootView.findViewById(R.id.onlineURLHeader);
		urlSubmission = (EditText)rootView.findViewById(R.id.onlineURL);
		submitURLEntry = (Button)rootView.findViewById(R.id.submitURLEntry);

		fileEntry = (TextView)rootView.findViewById(R.id.fileUpload);

        mediaUpload = (TextView)rootView.findViewById(R.id.mediaSubmission);

        mArcUpload = (TextView)rootView.findViewById(R.id.arcSubmission);
        mArcSubmission = (EditText) rootView.findViewById(R.id.arcEntry);
        mSubmitArcEntry = (Button) rootView.findViewById(R.id.submitArcEntry);
        mArcEntryContainer = (LinearLayout)rootView.findViewById(R.id.arcEntryContainer);

		timer = new Timer();
		isPageDone = new Timer();

		return rootView;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpCallback();
        setupViews();
        setupListeners();

        //if file entry is allowed check to see if the account has arc installed
        if(isFileEntryAllowed) {
            ExternalToolManager.getExternalToolsForCanvasContext(getCanvasContext(), mLTIToolCallback, true);
        }

        // If only file is allowed, move to the next page (or go back if we are coming from file uploads).
        if(!isOnlineTextAllowed && !isUrlEntryAllowed) {
            if(isMediaRecordingAllowed && !isFileEntryAllowed){
                Intent intent = NotoriousMediaUploadPicker.createIntentForAssignmentSubmission(getContext(), assignment);
                startActivityForResult(intent, RequestCodes.NOTORIOUS_REQUEST);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fileUploadDialog =  getChildFragmentManager().findFragmentByTag(FileUploadDialog.TAG);
        if (resultCode == Activity.RESULT_OK && requestCode == RequestCodes.NOTORIOUS_REQUEST) {
            // When its a Notorious request, just dismiss the fragment, and the user can look at the notification to see progress.
            getActivity().onBackPressed();
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == RequestCodes.NOTORIOUS_REQUEST) {
            isFileUploadCanceled = true;
        } else if(fileUploadDialog != null && fileUploadDialog instanceof FileUploadDialog){
            fileUploadDialog.onActivityResult(requestCode, resultCode, data);
        }
    }

	//  This gets called by the activity in onBackPressed().
    //  Call super so that we can check if there is unsaved data.
	@Override
	public boolean handleBackPressed() {
		if(urlEntryContainer.getVisibility() == View.VISIBLE && webView.canGoBack()) {
			webView.goBack();
			return true;
		} else {
            return super.handleBackPressed();
        }
	}

    @Override
    public void onPause() {
        dataLossPause(textSubmission, Const.DATA_LOSS_ADD_SUBMISSION);
        dataLossPause(urlSubmission, Const.DATA_LOSS_ADD_SUBMISSION_URL);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(dataLossResume(textSubmission, Const.DATA_LOSS_ADD_SUBMISSION)) {
            if(isOnlineTextAllowed) {
                textEntry.performClick();
            }
        }

        if(dataLossResume(urlSubmission, Const.DATA_LOSS_ADD_SUBMISSION_URL)) {
            if(isUrlEntryAllowed) {
                urlEntry.performClick();
            }
        }
        dataLossAddTextWatcher(textSubmission, Const.DATA_LOSS_ADD_SUBMISSION);
        dataLossAddTextWatcher(urlSubmission, Const.DATA_LOSS_ADD_SUBMISSION_URL);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mArcSubmissionReceiver, new IntentFilter(Const.ARC_SUBMISSION));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mArcSubmissionReceiver);
    }

	///////////////////////////////////////////////////////////////////////////
	// View
	///////////////////////////////////////////////////////////////////////////

	/*
	 * Useful for 2 reasons.
	 * 1.) The WebView won't load without a scheme
	 * 2.) The API automatically puts http or https at the front if it's not there anyways.
	 */
	public String getHttpURLSubmission()
	{
		String url = urlSubmission.getText().toString();
		if(!url.startsWith("http://") && !url.startsWith("https://"))
		{
			return "http://"+url;
		}
		else
		{
			return url;
		}
	}

    private void setupViews() {
        //hide arc until we can figure out if arc is installed for the course
        mArcUpload.setVisibility(View.GONE);

        //Hide text if it's not allowed.
        if(!isOnlineTextAllowed) {
            textEntry.setVisibility(View.GONE);
        }

        //Hide url if it's not allowed.
        if(!isUrlEntryAllowed) {
            urlEntry.setVisibility(View.GONE);
        }

        //Hide file if it's not allowed.
        if(!isFileEntryAllowed) {
            fileEntry.setVisibility(View.GONE);
        }

        if(!isMediaRecordingAllowed) {
            mediaUpload.setVisibility(View.GONE);
        }
        //If only text is allowed, open the tab.
        if(isOnlineTextAllowed && !isUrlEntryAllowed && !isFileEntryAllowed) {
            VisibilityAnimator.animateVisible(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.slow_push_left_in), textEntryContainer);
        }

        //If only url is allowed, open the tab.
        if(!isOnlineTextAllowed && isUrlEntryAllowed && !isFileEntryAllowed) {
            VisibilityAnimator.animateVisible(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.slow_push_left_in), urlEntryContainer);
        }

        setupWebview();
    }

    private void setupWebview() {
        //Give it a default.
        webView.loadUrl("");

        //Clicking links in a webview doesn't always trigger onPageFinished.
        isPageDone.schedule(new TimerTask() {

            @Override
            public void run() {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if(webView.getProgress() == 100) {
                            hideProgressBar();
                        }
                    }
                });
            }
        }, TimeUnit.SECONDS.toMillis(1), 2500);

        //Start off by hiding webview box.
        webView.setVisibility(View.GONE);

        //Fit to width.
        // Configure the webview
        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        //Open all urls with our webview.
        webView.setWebViewClient(new WebViewClient()
                //Once a page has loaded, stop the spinner.
        {
            @Override
            public void onPageFinished(WebView view, final String finishedURL)
            {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String url = finishedURL;
                        scrollView.scrollTo(0, urlEntryContainer.getTop());

                        //In Kit-Kat, an empty url auto-redirects to "about:blank".
                        //This handles that case.
                        if (url.equals("about:blank")) {
                            return;
                        }
                        //Do my best to not interrupt their typing.
                        else if (urlSubmission.getText().toString().endsWith("/")) {
                            if (!url.endsWith("/")) {
                                url = url + "/";
                            }
                        } else {
                            if(url.endsWith("/")) {
                                url = url.substring(0, url.length()-1);
                            }
                        }

                        //we only want to set the text to the url if it's a valid url. If you put an invalid url (www.goog for example)
                        //the webview redirects and eventually returns some html that then is put into the urlSubmission editText
                        if(Patterns.WEB_URL.matcher(url).matches()) {
                            urlSubmission.setText(url);
                            urlSubmission.setSelection(urlSubmission.getText().length());
                        }
                        hideProgressBar();
                    }
                });
            }
        });
    }


    private void showFileUploadDialog() {
        if(!isMediaRecordingAllowed && isFileEntryAllowed && mArcLTITool == null){
            if (isFileUploadCanceled) {
                getActivity().onBackPressed();
            } else {
                isFileUploadCanceled = true;
                FileUploadDialog fileUploadDialog = FileUploadDialog.newInstance(getChildFragmentManager(), FileUploadDialog.createAssignmentBundle(null, course, assignment));
                fileUploadDialog.setDialogLifecycleCallback(uploadDialogLifecycleCallback);
                fileUploadDialog.show(getFragmentManager(), FileUploadDialog.TAG);
            }
        }
    }

    private BroadcastReceiver mArcSubmissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String url = extras.getString(Const.URL);
                    mArcEntryContainer.setVisibility(View.VISIBLE);
                    mArcSubmission.setText(url);
                }
            }
        }
    };

    private void setupListeners() {
        uploadDialogLifecycleCallback = new FileUploadDialog.DialogLifecycleCallback() {
            @Override
            public void onCancel(Dialog dialog) {

            }

            @Override
            public void onAllUploadsComplete(Dialog dialog) {
                // Send broadcast so list is updated.
                Intent intent = new Intent(Const.SUBMISSION_COMMENT_SUBMITTED);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                getActivity().onBackPressed();
            }
        };

        fileEntry.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            Bundle bundle = FileUploadDialog.createAssignmentBundle(null, course, assignment);
            FileUploadDialog fileUploadDialog = FileUploadDialog.newInstance(getChildFragmentManager(), bundle);
            fileUploadDialog.setDialogLifecycleCallback(uploadDialogLifecycleCallback);
            fileUploadDialog.show(getFragmentManager(), FileUploadDialog.TAG);
            }
        });

        urlEntry.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(urlEntryContainer.getVisibility() == View.VISIBLE)
                {
                    VisibilityAnimator.animateGone(AnimationUtils.loadAnimation(getActivity(),
                            R.anim.slow_push_right_out), urlEntryContainer);
                }
                else
                {
                    VisibilityAnimator.animateVisible(AnimationUtils.loadAnimation(getActivity(),
                            R.anim.slow_push_left_in), urlEntryContainer);
                }
            }
        });

        textEntry.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(textEntryContainer.getVisibility() == View.VISIBLE)
                {
                    VisibilityAnimator.animateGone(AnimationUtils.loadAnimation(getActivity(),
                            R.anim.slow_push_right_out), textEntryContainer);
                }
                else
                {
                    VisibilityAnimator.animateVisible(AnimationUtils.loadAnimation(getActivity(),
                            R.anim.slow_push_left_in), textEntryContainer);
                }
            }
        });

        submitTextEntry.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                tryToSubmitText();
            }
        });

        //Because it's not single line, we have to handle the enter button.
        textSubmission.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    tryToSubmitText();

                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });

        submitURLEntry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToSubmitURL();
            }
        });

        //Because it's not single line, we have to handle the enter button.
        urlSubmission.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    tryToSubmitURL();

                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });


        urlSubmission.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}

            @Override
            public void afterTextChanged(final Editable string) {
                timer.cancel();
                timer = new Timer();
                showProgressBar();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        //Start it again just in case another page finished loading
                        //in the one second after the text changed and the webview starts loading.
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                String originalURL = webView.getUrl();

                                //Null pointer check
                                if(originalURL == null)
                                {
                                    originalURL = "";
                                }

                                //Strip off ending / characters
                                if(originalURL.endsWith("/"))
                                {
                                    originalURL = originalURL.substring(0, originalURL.length()-1);
                                }

                                String currentURL = getHttpURLSubmission();
                                //Null pointer check
                                if(currentURL == null)
                                {
                                    currentURL = "";
                                }
                                if(currentURL.endsWith("/"))
                                {
                                    currentURL = currentURL.substring(0, currentURL.length()-1);
                                }

                                //If it's empty clear the view.
                                if(string.toString().trim().length() == 0)
                                {
                                    webView.setVisibility(View.GONE);
                                    webView.loadUrl("");
                                }
                                else if (!originalURL.equals(currentURL))	//if it's already loaded, don't do it.
                                {
                                    webView.setVisibility(View.VISIBLE);
                                    webView.loadUrl(getHttpURLSubmission());
                                }
                            }
                        });
                    }
                }, 2000);
            }
        });

        mediaUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = NotoriousMediaUploadPicker.createIntentForAssignmentSubmission(getContext(), assignment);
                startActivityForResult(intent, RequestCodes.NOTORIOUS_REQUEST);
            }
        });

        mArcUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(Locale.getDefault(), "%s/%s/external_tools/%d/resource_selection?launch_type=homework_submission&assignment_id=%d", ApiPrefs.getFullDomain(), getCanvasContext().toAPIString(), mArcLTITool.getId(), assignment.getId());
                ArcWebviewFragment.loadInternalWebView(getActivity(), ((Navigation)getActivity()), InternalWebviewFragment.createBundle(getCanvasContext(), url, mArcLTITool.getName(), true));
            }
        });

        mSubmitArcEntry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToSubmitArc();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Submit Submission Data
    ///////////////////////////////////////////////////////////////////////////

    public void tryToSubmitText(){
        //get the text, replace all line breaks with <br/> tags so they are preserved when displayed in a webview.
        String textToSubmit = textSubmission.getText().toString().replaceAll("\\n", "<br/>");
        if(textToSubmit.trim().length() == 0) { 	//It's an empty submission
            Toast.makeText(getActivity(), R.string.blankSubmission, Toast.LENGTH_LONG).show();
        }
        else {
            //Log to GA
            Analytics.trackButtonPressed(getActivity(), "Submit Text Assignment", null);

            SubmissionManager.postTextSubmission(course, assignment.getId(), textToSubmit, canvasCallbackSubmission);
            dataLossDeleteStoredData(Const.DATA_LOSS_ADD_SUBMISSION);
        }
    }

    public void tryToSubmitURL(){
        String urlToSubmit = urlSubmission.getText().toString();
        if(urlToSubmit.trim().length() == 0) { 	//It's an empty submission
            Toast.makeText(getActivity(), R.string.blankSubmission, Toast.LENGTH_LONG).show();
        }
        else {
            //Log to GA
            Analytics.trackButtonPressed(getActivity(), "Submit URL Assignment", null);

            SubmissionManager.postUrlSubmission(course, assignment.getId(), urlToSubmit, false, canvasCallbackSubmission);
            dataLossDeleteStoredData(Const.DATA_LOSS_ADD_SUBMISSION_URL);
        }
    }

    public void tryToSubmitArc(){
        String urlToSubmit = mArcSubmission.getText().toString();
        if(urlToSubmit.trim().length() == 0) { 	//It's an empty submission
            Toast.makeText(getActivity(), R.string.blankSubmission, Toast.LENGTH_LONG).show();
        }
        else {
            //Log to GA
            Analytics.trackButtonPressed(getActivity(), "Submit Arc Assignment", null);

            SubmissionManager.postUrlSubmission(course, assignment.getId(), urlToSubmit, true, canvasCallbackSubmission);
            dataLossDeleteStoredData(Const.DATA_LOSS_ADD_SUBMISSION_URL);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CallBack
    ///////////////////////////////////////////////////////////////////////////

    public void setUpCallback() {

        canvasCallbackSubmission = new StatusCallback<Submission>() {
            @Override
            public void onResponse(retrofit2.Response<Submission> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()){
                    return;
                }

                Submission result = response.body();
                if(result.getBody() != null || result.getUrl() != null) {
                    Toast.makeText(getActivity(), R.string.successPostingSubmission, Toast.LENGTH_LONG).show();
                    // clear text fields because they are saved
                    textSubmission.setText("");
                    urlSubmission.setText("");
                    // Send broadcast so list is updated.
                    Intent intent = new Intent(Const.SUBMISSION_COMMENT_SUBMITTED);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(getActivity() != null) {
                                getActivity().onBackPressed();
                            }
                        }
                    }, TimeUnit.SECONDS.toMillis(1));
                } else {
                    Toast.makeText(getActivity(), R.string.errorPostingSubmission, Toast.LENGTH_LONG).show();
                }
            }
        };

        mLTIToolCallback = new StatusCallback<List<LTITool>>() {

            @Override
            public void onResponse(retrofit2.Response<List<LTITool>> response, LinkHeaders linkHeaders, ApiType type) {
                for (LTITool ltiTool : response.body()) {
                    final String url = ltiTool.getUrl();
                    if (url != null && url.contains("instructuremedia.com/lti/launch")) {
                        mArcUpload.setVisibility(View.VISIBLE);
                        mArcLTITool = ltiTool;
                        break;
                    }
                }
                //check to see if we should automatically show the file upload dialog
                showFileUploadDialog();
            }

            @Override
            public void onFail(Call<List<LTITool>> callResponse, Throwable error, retrofit2.Response response) {
                //check to see if we should automatically show the file upload dialog
                //we don't want to show it if this failed due to there being no cache
                if (response.code() != 504) {
                    showFileUploadDialog();
                }
            }

        };
    }

	///////////////////////////////////////////////////////////////////////////
	// Intent
	///////////////////////////////////////////////////////////////////////////

	@Override
	public void handleIntentExtras(Bundle extras) {
		super.handleIntentExtras(extras);
		// do stuff with bundle here
        course = (Course) getCanvasContext();

		assignment = extras.getParcelable(Const.ASSIGNMENT);
        isOnlineTextAllowed = extras.getBoolean(Const.TEXT_ALLOWED);
		isUrlEntryAllowed = extras.getBoolean(Const.URL_ALLOWED);
		isFileEntryAllowed = extras.getBoolean(Const.FILE_ALLOWED);
        isMediaRecordingAllowed = extras.getBoolean(Const.MEDIA_UPLOAD_ALLOWED);
	}

	public static Bundle createBundle(Course course, Assignment assignment, boolean textEntryAllowed, boolean urlEntryAllowed, boolean fileEntryAllowed, boolean mediaUploadAllowed) {
		Bundle extras = createBundle(course);
		extras.putParcelable(Const.ASSIGNMENT, assignment);
		extras.putBoolean(Const.TEXT_ALLOWED, textEntryAllowed);
		extras.putBoolean(Const.URL_ALLOWED, urlEntryAllowed);
		extras.putBoolean(Const.FILE_ALLOWED, fileEntryAllowed);
        extras.putBoolean(Const.MEDIA_UPLOAD_ALLOWED, mediaUploadAllowed);

		return extras;
	}

    @Override
    public boolean allowBookmarking() {
        return true;
    }
}
