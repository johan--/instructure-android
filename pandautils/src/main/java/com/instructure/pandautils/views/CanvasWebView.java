/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.pandautils.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.R;
import com.instructure.pandautils.utils.Utils;
import com.instructure.pandautils.video.ContentVideoViewClient;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CanvasWebView extends WebView {

    private static final int VIDEO_PICKER_RESULT_CODE = 1202;
    private final String encoding = "UTF-8";

    public interface CanvasWebViewClientCallback {
        void openMediaFromWebView(String mime, String url, String filename);
        void onPageStartedCallback(WebView webView, String url);
        void onPageFinishedCallback(WebView webView, String url);
        void routeInternallyCallback(String url);
        boolean canRouteInternallyDelegate(String url);
    }

    public interface CanvasEmbeddedWebViewCallback {
        boolean shouldLaunchInternalWebViewFragment(String url);
        void launchInternalWebViewFragment(String url);
    }

    public interface CanvasWebChromeClientCallback {
        void onProgressChangedCallback(WebView view, final int newProgress);
    }

    public interface VideoPickerCallback {
        void requestStartActivityForResult(Intent intent, int requestCode);
        boolean permissionsGranted();
    }

    private CanvasWebViewClientCallback mCanvasWebViewClientCallback;
    private CanvasEmbeddedWebViewCallback mCanvasEmbeddedWebViewCallback;
    private CanvasWebChromeClientCallback mCanvasWebChromeClientCallback;
    private VideoPickerCallback mVideoPickerCallback;

    private Context mContext;
    private ContentVideoViewClient mClient;
    private WebChromeClient mWebChromeClient;
    private ValueCallback<Uri[]> mFilePathCallback;

    public CanvasWebView(Context context) {
        super(context);
        init(context);
    }

    public CanvasWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CanvasWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        mContext = context;
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(true);
        // Hide the zoom controls
        this.getSettings().setDisplayZoomControls(false);

        this.getSettings().setUseWideViewPort(true);
        this.setWebViewClient(new CanvasWebViewClient());

        //increase text size based on the devices accessibility setting
        //fontScale comes back as a float
        int scalePercent = (int)(getResources().getConfiguration().fontScale * 100);
        this.getSettings().setTextZoom(scalePercent);
        
        mWebChromeClient = new CanvasWebChromeClient();
        this.setWebChromeClient(mWebChromeClient);

        this.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                //get the filename
                String filename = "file" + contentLength;
                if (contentDisposition != null) {
                    String temp = "filename=";
                    int index = contentDisposition.indexOf(temp) + temp.length();

                    if (index > -1) {
                        int end = contentDisposition.indexOf(";", index);
                        if (end > -1) {
                            //+1 and -1 to remove the quotes
                            filename = contentDisposition.substring(index + 1, end - 1);
                        }
                        //make the filename unique
                        filename = String.format("%s_%d", filename, url.hashCode());
                    }

                    if (mCanvasWebViewClientCallback != null) {
                        mCanvasWebViewClientCallback.openMediaFromWebView(mimetype, url, filename);
                    }
                }
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }
    }

    @Override
    public void onPause() {
        // Calling onPause will stop Video's sound, but onResume must be called if resumed, otherwise the second time onPause is called it won't work
        try {
            super.onPause();
        } catch (NullPointerException npe) {
            // Catch for API 16 devices (and perhaps others) and webkit
            Logger.e(npe.getMessage());
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
    }

    /**
     * Handles back presses for the CanvasWebView and the lifecycle of the {@link com.video.ActivityContentVideoViewClient}
     *
     * Use instead of goBack and canGoBack
     *
     * @return true if handled; false otherwise
     */
    public boolean handleGoBack() {
        if (mClient.isFullscreen()) {
            mWebChromeClient.onHideCustomView();
            return true;
        } else if (super.canGoBack()) {
            super.goBack();
            return true;
        }
        return false;
    }


    public static String getRefererDomain(Context context) {
        // Mainly for embedded content such as vimeo, youtube, video tags, iframes, etc
        return APIHelpers.loadProtocol(context) + "://" + APIHelpers.getDomain(context);
    }

    public static String applyWorkAroundForDoubleSlashesAsUrlSource(String html) {
        if(TextUtils.isEmpty(html)) return "";
        // Fix for embedded videos that have // instead of http://
        html = html.replaceAll("href=\"//", "href=\"http://");
        html = html.replaceAll("href='//", "href='http://");
        html = html.replaceAll("src=\"//", "src=\"http://");
        html = html.replaceAll("src='//", "src='http://");
        return html;
    }


    public class CanvasWebChromeClient extends WebChromeClient {
        private CustomViewCallback mCallback;
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            mCallback = callback;
            mClient.onShowCustomView(view);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mCallback != null) {
                mCallback.onCustomViewHidden();
            }
            mClient.onDestroyContentVideoView();
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onCloseWindow(WebView window) {
            if(!handleGoBack()) {
                if(getContext() instanceof Activity) {
                    ((Activity) getContext()).onBackPressed();
                }
            }
        }

        @Override // For Android > 5.0
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if(mVideoPickerCallback != null && mVideoPickerCallback.permissionsGranted()) {
                showFileChooser(webView, filePathCallback, fileChooserParams);
                return true;
            }
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
    }

    public class CanvasWebViewClient extends WebViewClient {

        public CanvasWebViewClient() {
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //check to see if we need to do anything with the link that was clicked

            // Default headers
            Map<String, String> extraHeaders = Utils.getReferer(getContext());

            if (url.contains("yellowdig") && yellowdigInstalled()) {
                // Pertaining to the Yellowdig LTI:
                //  This is a yellowdig URL, they have a special condition on their end
                //  to not send us a yellowdig URI scheme link if they detect anything
                //  in the 'X-Requested-With' header, so we're putting in a special
                //  case here to intercept and remove the value in that header. The WebView
                //  automatically adds this in and we can't remove it so we just blank it out
                extraHeaders.put("X-Requested-With", "");
            }

            // Check if the URL has a scheme that we aren't handling
            Uri uri = Uri.parse(url);
            if (!uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
                // Special scheme, send URL to app that can handle it
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // Verify that the intent will resolve to an activity
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    if (uri.getScheme().equals("yellowdig"))
                        // Pop off the LTI page so it doesn't try to reload the yellowdig app when going back to our app
                        ((AppCompatActivity)CanvasWebView.this.getContext()).getSupportFragmentManager().popBackStack();

                    getContext().startActivity(intent);
                    return true;
                }
            }

            if (mCanvasWebViewClientCallback != null) {
                //Is the URL something we can link to inside our application?
                if (mCanvasWebViewClientCallback.canRouteInternallyDelegate(url)) {
                    mCanvasWebViewClientCallback.routeInternallyCallback(url);
                    return true;
                }
            }

            // Handle the embedded webview case (Its not within the InternalWebViewFragment)
            if (mCanvasEmbeddedWebViewCallback != null && mCanvasEmbeddedWebViewCallback.shouldLaunchInternalWebViewFragment(url)) {
                String contentTypeGuess = URLConnection.guessContentTypeFromName(url);
                // null when type can't be determined, launchInternalWebView anyway
                // When contentType has 'application', it typically means it's a pdf or some type of document that needs to be downloaded,
                //   so allow the embedded webview to open the url, which will trigger the DownloadListener. If for some reason the content can
                //   be loaded in the webview, the content will just load in the embedded webview (which isn't ideal, but in majority of cases it won't happen).
                if (contentTypeGuess == null || !contentTypeGuess.contains("application")) {
                    mCanvasEmbeddedWebViewCallback.launchInternalWebViewFragment(url);
                    return true;
                }
            }
            view.loadUrl(url, extraHeaders);
            //we're handling the url ourselves, so return true.
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mCanvasWebViewClientCallback != null) {
                mCanvasWebViewClientCallback.onPageStartedCallback(view, url);
            }
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            // Clear the history if formatHtml was called more than once. Refer to formatHtml's NOTE
            if (url.startsWith(getHtmlAsUrl("", encoding))) {
                view.clearHistory();
            }
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mCanvasWebViewClientCallback != null) {
                mCanvasWebViewClientCallback.onPageFinishedCallback(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (failingUrl != null && failingUrl.startsWith("file://")) {
                failingUrl = failingUrl.replaceFirst("file://", "https://");
                view.loadUrl(failingUrl, Utils.getReferer(getContext()));
            }
        }
    }
    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
    }


    @Override
    public void loadData(String data, String mimeType, String encoding) {
        super.loadData(data, mimeType, encoding);
    }

    /**
     * Makes html content somewhat suitable for mobile
     *
     * NOTE: The web history is cleared when formatHtml is called. Only the loaded page will appear in the webView.copyBackForwardList()
     *       Back history will not work with multiple pages. This allows for formatHtml to be called several times without causing the user to
     *          press back 2 or 3 times.
     *
     * @param content
     * @param title
     * @return
     */
    public String formatHTML(String content, String title) {
        String html = APIHelper.getAssetsFile(mContext, "html_wrapper.html");

        content = CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(content);

        String result = html.replace("{$CONTENT$}", content);

        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        this.loadDataWithBaseURL(CanvasWebView.getRefererDomain(getContext()), result, "text/html", encoding, getHtmlAsUrl(result, encoding));

        setupAccessibilityContentDescription(result, title);

        return result;
    }

    /*
     *  Work around for API 16 devices (and perhaps others). When pressing back the webview was loading 'about:blank' instead of the custom html
     */
    private String getHtmlAsUrl(String html, String encoding) {
        return String.format("data:text/html; charset=%s, %s", encoding, html);
    }

    /**
     * Check if the Yellowdig app is installed by checking
     * to see if there is an app that handles the yellowdig URI scheme.
     * @return True if installed, false if not
     */
    private boolean yellowdigInstalled() {
        Uri uri = Uri.parse("yellowdig://");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        return intent.resolveActivity(getContext().getPackageManager()) != null;
    }

    private void setupAccessibilityContentDescription(String formattedHtml, String title) {
        //Remove all html tags and set content description for accessibility
        // call toString on fromHTML because certain Spanned objects can cause this to crash
        String contentDescription = formattedHtml;
        if (title != null) {
            contentDescription = title + " " + formattedHtml;
        }
        this.setContentDescription(APIHelper.simplifyHTML(Html.fromHtml(contentDescription)));
    }

    // region Getter & Setters

    public CanvasEmbeddedWebViewCallback getCanvasEmbeddedWebViewCallback() {
        return mCanvasEmbeddedWebViewCallback;
    }

    public void setCanvasEmbeddedWebViewCallback(CanvasEmbeddedWebViewCallback mCanvasEmbeddedWebViewCallback) {
        this.mCanvasEmbeddedWebViewCallback = mCanvasEmbeddedWebViewCallback;
    }

    public CanvasWebViewClientCallback getCanvasWebViewClientCallback() {
        return mCanvasWebViewClientCallback;
    }

    public void setCanvasWebViewClientCallback(CanvasWebViewClientCallback canvasWebViewClientCallback) {
        this.mCanvasWebViewClientCallback = canvasWebViewClientCallback;
    }

    public CanvasWebChromeClientCallback getCanvasWebChromeClientCallback() {
        return mCanvasWebChromeClientCallback;
    }

    public void setCanvasWebChromeClientCallback(CanvasWebChromeClientCallback mCanvasWebChromeClientCallback) {
        this.mCanvasWebChromeClientCallback = mCanvasWebChromeClientCallback;
    }

    public void setCanvasWebChromeClientShowFilePickerCallback(VideoPickerCallback callback) {
        this.getSettings().setAllowFileAccess(true);
        this.mVideoPickerCallback = callback;
    }

    public ContentVideoViewClient getClient() {
        return mClient;
    }

    public void setClient(ContentVideoViewClient mClient) {
        this.mClient = mClient;
    }

    // endregion


    //region Video Picking for WebView (Used for ARC)

    public void clearPickerCallback() {
        mFilePathCallback = null;
    }

    // For Android 5.0+
    private boolean showFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {

        if(mVideoPickerCallback != null) {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;
            startVideoChooser(VIDEO_PICKER_RESULT_CODE);
            return true;
        }

        return false;
    }

    private void startVideoChooser(final int requestCode) {
        Intent vi‌​deoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        vi‌​deoIntent.setType("video/*");

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        final PackageManager packageManager = mContext.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }
        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(vi‌​deoIntent, getContext().getString(R.string.pickVideo));

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        mVideoPickerCallback.requestStartActivityForResult(chooserIntent, requestCode);
    }

    public boolean handleOnActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VIDEO_PICKER_RESULT_CODE && mFilePathCallback != null) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri[] results = new Uri[]{data.getData()};
                mFilePathCallback.onReceiveValue(results);
            } else {
                mFilePathCallback.onReceiveValue(null);
            }
        }

        clearPickerCallback();

        return true;
    }

    //endregion
}

