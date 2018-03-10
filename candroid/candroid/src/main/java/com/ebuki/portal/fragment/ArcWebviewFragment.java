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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.ebuki.portal.R;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.util.FragUtils;
import com.ebuki.portal.util.RouterUtils;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.views.CanvasWebView;

import org.apache.commons.lang3.StringEscapeUtils;

public class ArcWebviewFragment extends InternalWebviewFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShouldRouteInternally(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        canvasWebView.addJavascriptInterface(new MyJavaScriptInterface(getContext()), "HtmlViewer");

        canvasWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
            @Override
            public void openMediaFromWebView(String mime, String url, String filename) {
                openMedia(mime, url, filename);
            }

            @Override
            public void onPageFinishedCallback(WebView webView, String url) {
                canvasLoading.setVisibility(View.GONE);
                hideProgressBar();

                //check for a successful arc submission
                if(url.contains("success/external_tool_dialog")) {

                    webView.loadUrl("javascript:HtmlViewer.showHTML" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                }

            }

            @Override
            public void onPageStartedCallback(WebView webView, String url) {
                canvasLoading.setVisibility(View.VISIBLE);

            }

            @Override
            public boolean canRouteInternallyDelegate(String url) {
                return shouldRouteInternally && !isUnsupportedFeature && RouterUtils.canRouteInternally(getActivity(), url, ApiPrefs.getDomain(), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                RouterUtils.canRouteInternally(getActivity(), url, ApiPrefs.getDomain(), true);
            }

        });

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

    class MyJavaScriptInterface {

        private Context context;
        MyJavaScriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void showHTML(String html) {

            String mark = "@id\":\"";
            int index = html.indexOf(mark);
            if(index != -1) {
                int endIndex = html.indexOf(",", index);
                String url = html.substring(index+mark.length(), endIndex-1);
                url = StringEscapeUtils.unescapeJava(url);

                Intent intent = new Intent(Const.ARC_SUBMISSION);
                Bundle extras = new Bundle();
                extras.putString(Const.URL, url);

                intent.putExtras(extras);
                //let the add submission fragment know that we have an arc submission
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                //close this page
                getNavigation().popCurrentFragment();
            }
        }

    }

    public static void loadInternalWebView(FragmentActivity activity, Navigation navigation, Bundle bundle) {
        if(activity == null || navigation == null) {
            Logger.e("loadInternalWebView could not complete, activity or navigation null");
            return;
        }

        navigation.addFragment(FragUtils.getFrag(ArcWebviewFragment.class, bundle));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!canvasWebView.handleOnActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
