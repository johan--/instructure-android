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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ebuki.portal.R;
import com.instructure.canvasapi2.managers.ExternalToolManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.LTITool;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.views.CanvasWebView;

public class LTIWebViewFragment extends InternalWebviewFragment {

    private Tab ltiTab;
    private String url;
    private boolean sessionLessLaunch = false;
    private String ltiTitle = null;
    private boolean shouldReload = true;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        if(ltiTitle == null) {
            return getContext().getString(R.string.link);
        } else {
            return ltiTitle;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShouldRouteInternally(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        canvasWebView.setCanvasWebChromeClientShowFilePickerCallback(new CanvasWebView.VideoPickerCallback() {
            @Override
            public void requestStartActivityForResult(Intent intent, int requestCode) {
                startActivityForResult(intent, requestCode);
            }

            @Override
            public boolean permissionsGranted() {
                if(PermissionUtils.hasPermissions(getActivity(), PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE))) {
                    return true;
                } else {
                    requestFilePermissions();
                    return false;
                }
            }
        });

        return view;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestFilePermissions() {
        requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA), PermissionUtils.PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
            if(canvasWebView != null) {
                canvasWebView.clearPickerCallback();
            }
            Toast.makeText(getContext(), R.string.pleaseTryAgain, Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!canvasWebView.handleOnActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            //when we return from selecting a video to upload we don't want to reload the webview because it will cancel the upload
            shouldReload = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!shouldReload)  return;

        try {
            if(ltiTab == null) {
                if(!TextUtils.isEmpty(url)) {
                    //modify the url
                    if(url.startsWith("canvas-courses://")) {
                        url = url.replaceFirst("canvas-courses", ApiPrefs.getProtocol());
                    }
                    Uri uri =  Uri.parse(url).buildUpon()
                            .appendQueryParameter("display", "borderless")
                            .appendQueryParameter("platform", "android")
                            .build();
                    if(sessionLessLaunch) {
                        String sessionless_launch = ApiPrefs.getFullDomain() +
                                "/api/v1/accounts/self/external_tools/sessionless_launch?url=" + url;
                        new GetSessionlessLtiURL().execute(sessionless_launch);
                    } else {
                        loadUrl(uri.toString());
                    }
                } else {
                    SpannedString spannedString = new SpannedString(getString(R.string.errorOccurred));
                    loadHtml(Html.toHtml(spannedString));
                }
            } else {
                new GetLtiURL().execute();
            }
        } catch (Exception e) {
            //if it gets here we're in trouble and won't know what the tab is, so just display an error message
            SpannedString spannedString = new SpannedString(getString(R.string.errorOccurred));
            loadHtml(Html.toHtml(spannedString));
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, Tab ltiTab) {
        Bundle extras = createBundle(canvasContext);
        extras.putBoolean(Const.AUTHENTICATE, false);
        extras.putParcelable(Const.TAB, ltiTab);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url) {
        Bundle extras = createBundle(canvasContext);
        extras.putBoolean(Const.AUTHENTICATE, false);
        extras.putString(Const.URL, url);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, String title, boolean sessionLessLaunch) {
        Bundle extras = createBundle(canvasContext);
        extras.putBoolean(Const.AUTHENTICATE, false);
        extras.putString(Const.URL, url);
        extras.putBoolean(Const.SESSIONLESS_LAUNCH, sessionLessLaunch);
        extras.putString(Const.TITLE, title);
        return extras;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Asynctask
    ///////////////////////////////////////////////////////////////////////////

    private class GetLtiURL extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        @Override
        protected String doInBackground(Void... params) {
            LTITool tool = ExternalToolManager.getLtiFromUrlSynchronous(ltiTab.getLTIUrl());
            return tool != null ? tool.getUrl() : null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(getActivity() == null){return;}

            hideProgressBar();

            //make sure we have a non null url before we add parameters
            if(!TextUtils.isEmpty(result)) {
                Uri uri = Uri.parse(result).buildUpon()
                        .appendQueryParameter("display", "borderless")
                        .appendQueryParameter("platform", "android")
                        .build();
                loadUrl(uri.toString());
            } else {
                loadUrl(result);
            }
        }
    }

    private class GetSessionlessLtiURL extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        @Override
        protected String doInBackground(String... params) {
            LTITool tool = ExternalToolManager.getLtiFromUrlSynchronous(params[0]);
            return tool != null ? tool.getUrl() : null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(getActivity() == null){return;}

            hideProgressBar();

            //make sure we have a non null url before we add parameters
            if(!TextUtils.isEmpty(result)) {
                Uri uri = Uri.parse(result).buildUpon()
                        .appendQueryParameter("display", "borderless")
                        .appendQueryParameter("platform", "android")
                        .build();
                loadUrl(uri.toString());
            } else {
                loadUrl(result);
            }
        }
    }

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if(extras.containsKey(Const.TAB)) {
            ltiTab = extras.getParcelable(Const.TAB);
        }

        url = extras.getString(Const.URL, "");
        sessionLessLaunch = extras.getBoolean(Const.SESSIONLESS_LAUNCH, false);

        if(extras.containsKey(Const.TITLE)) {
            ltiTitle = extras.getString(Const.TITLE);
        }
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}
