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
package com.instructure.loginapi.login.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.OAuthManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.OAuthToken;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.adapter.SnickerDoodleAdapter;
import com.instructure.loginapi.login.api.MobileVerifyAPI;
import com.instructure.loginapi.login.dialog.AuthenticationDialog;
import com.instructure.loginapi.login.model.DomainVerificationResult;
import com.instructure.loginapi.login.model.SignedInUser;
import com.instructure.loginapi.login.snicker.SnickerDoodle;
import com.instructure.loginapi.login.util.Const;
import com.instructure.loginapi.login.util.PreviousUsersUtils;
import com.instructure.pandautils.utils.ViewStyler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

public abstract class BaseSignInActivity extends AppCompatActivity implements AuthenticationDialog.OnAuthenticationSet {

    static { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); }

    protected abstract Intent launchApplicationMainActivityIntent();
    protected abstract void refreshWidgets();

    protected static final String DOMAIN = "signInDomain";

    private static final String SUCCESS_URL = "/login/oauth2/auth?code=";
    private static final String ERROR_URL = "/login/oauth2/auth?error=access_denied";

    private WebView mWebView;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerRecyclerView;

    private String mClientId;
    private String mClientSecret;
    private int mCanvasLogin = 0;
    boolean mSpecialCase = false;
    private String mAuthenticationURL;
    private HttpAuthHandler mHttpAuthHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mCanvasLogin = getIntent().getExtras().getInt(Const.CANVAS_LOGIN, 0);
        setupViews();
        applyTheme();
        beginSignIn(getUrl());
    }

    private void setupViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getUrl());
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mWebView = (WebView) findViewById(R.id.webView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.drawerRecyclerView);
        boolean isDebuggable =  (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        mDrawerLayout.setDrawerLockMode(isDebuggable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        clearCookies();
        CookieManager.getInstance().setAcceptCookie(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setUserAgentString(com.instructure.pandautils.utils.Utils.generateUserAgent(this, "candroid"));
        mWebView.setWebViewClient(mWebViewClient);

        if(isDebuggable) {
            eatSnickerDoodles();
        }
    }

    private void applyTheme() {
        ViewStyler.setStatusBarLight(this);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return handleShouldOverrideUrlLoading(view, request.getUrl().toString());
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleShouldOverrideUrlLoading(view, url);
        }

        private boolean handleShouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains(SUCCESS_URL)) {
                String oAuthRequest = url.substring(url.indexOf(SUCCESS_URL) + SUCCESS_URL.length());
                OAuthManager.getToken(mClientId, mClientSecret, oAuthRequest, mGetTokenCallback);
            } else if (url.contains(ERROR_URL)) {
                clearCookies();
                view.loadUrl(mAuthenticationURL);
            } else {
                view.loadUrl(url);
            }

            return true; // then it is not handled by default action
        }

        @SuppressWarnings("deprecation")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            handleShouldInterceptRequest(view, url);
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                handleShouldInterceptRequest(view, request.getUrl().toString());
            }
            return super.shouldInterceptRequest(view, request);
        }

        private void handleShouldInterceptRequest(WebView view, String url) {
            if (url.contains("idp.sfcollege.edu/idp/santafe")) {
                mSpecialCase = true;
                String oAuthRequest = url.substring(url.indexOf("hash=") + "hash=".length());
                OAuthManager.getToken(mClientId, mClientSecret, oAuthRequest, mGetTokenCallback);
            }
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            mHttpAuthHandler = handler;
            AuthenticationDialog.get().show(getSupportFragmentManager(), AuthenticationDialog.class.getSimpleName());
        }
    };

    public String getUrl() {
        return getIntent().getStringExtra(DOMAIN);
    }

    private void beginSignIn(String url) {
        MobileVerifyAPI.mobileVerify(url, mMobileVerifyCallback);
    }

    @Override
    public void onRetrieveCredentials(String username, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            if (mHttpAuthHandler != null) {
                mHttpAuthHandler.proceed(username, password);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.invalidEmailPassword, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("deprecation")
    void clearCookies() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    //region Callbacks

    StatusCallback<DomainVerificationResult> mMobileVerifyCallback = new StatusCallback<DomainVerificationResult>() {

        @Override
        public void onResponse(Response<DomainVerificationResult> response, LinkHeaders linkHeaders, ApiType type) {
            if (type.isAPI()) {

                DomainVerificationResult domainVerificationResult = response.body();

                if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.Success) {
                    //Domain is now verified.
                    //save domain to the preferences.
                    String domain = "";

                    //mobile verify can change the hostname we need to use
                    if (domainVerificationResult.getBase_url() != null && !domainVerificationResult.getBase_url().equals("")) {
                        domain = domainVerificationResult.getBase_url();
                    } else {
                        domain = getUrl();
                    }

                    //The domain gets set afterwards in SetUpInstance, but domain is required a bit before that works.
                    ApiPrefs.setDomain(domain);

                    mClientId = domainVerificationResult.getClient_id();
                    mClientSecret = domainVerificationResult.getClient_secret();

                    //Get the protocol
                    final String apiProtocol = domainVerificationResult.getProtocol();

                    //Set the protocol
                    ApiPrefs.setProtocol(domainVerificationResult.getProtocol());

                    //Get device name for the login request.
                    String deviceName = Build.MODEL;
                    if (deviceName == null || deviceName.equals("")) {
                        deviceName = getString(R.string.unknownDevice);
                    }

                    //Remove spaces
                    deviceName = deviceName.replace(" ", "_");

                    //changed for the online update to have an actual formatted login page
                    mAuthenticationURL = apiProtocol + "://" + domain + "/login/oauth2/auth?client_id=" +
                            mClientId + "&response_type=code&redirect_uri=urn:ietf:wg:oauth:2.0:oob&mobile=1";
                    mAuthenticationURL += "&purpose=" + deviceName;

                    if (mCanvasLogin == 1) {
                        mAuthenticationURL += "&canvas_login=1";
                    } else if (mCanvasLogin == 2) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.setCookie(apiProtocol + "://" + domain, "canvas_sa_delegated=1");
                    }

                    mWebView.loadUrl(mAuthenticationURL);
                } else {
                    //Error message
                    int errorId;

                    if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.GeneralError) {
                        errorId = R.string.mobileVerifyGeneral;
                    } else if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.DomainNotAuthorized) {
                        errorId = R.string.mobileVerifyDomainUnauthorized;
                    } else if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.UnknownUserAgent) {
                        errorId = R.string.mobileVerifyUserAgentUnauthorized;
                    } else {
                        errorId = R.string.mobileVerifyUnknownError;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(BaseSignInActivity.this);
                    builder.setTitle(R.string.errorOccurred);
                    builder.setMessage(errorId);
                    builder.setCancelable(true);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    };

    StatusCallback<OAuthToken> mGetTokenCallback = new StatusCallback<OAuthToken>() {

        final SignedInUser user = new SignedInUser();

        @Override
        public void onResponse(retrofit2.Response<OAuthToken> response, LinkHeaders linkHeaders, ApiType type) {
            if(type.isAPI()) {
                OAuthToken token = response.body();
                ApiPrefs.setToken(token.getAccessToken());
                user.token = token.getAccessToken();

                //We now need to get the cache user
                UserManager.getSelf(new StatusCallback<User>() {

                    @Override
                    public void onResponse(retrofit2.Response<User> response, LinkHeaders linkHeaders, ApiType type) {
                        if(type.isAPI()) {
                            ApiPrefs.setUser(response.body());

                            user.user = response.body();
                            user.domain = ApiPrefs.getDomain();
                            user.protocol = ApiPrefs.getProtocol();
                            PreviousUsersUtils.add(BaseSignInActivity.this, user);

                            refreshWidgets();

                            Intent intent = launchApplicationMainActivityIntent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        }

        @Override
        public void onFail(Call<OAuthToken> response, Throwable error) {
            super.onFail(response, error);
            if (!mSpecialCase) {
                Toast.makeText(BaseSignInActivity.this, R.string.errorOccurred, Toast.LENGTH_SHORT).show();
            } else {
                mSpecialCase = false;
            }

            mWebView.loadUrl(mAuthenticationURL);
        }
    };

    //endregion

    //region Snicker Doodles

    /**
     * Adds a simple login method for devs. To add credentials add your snickers (credentials) to the snickers.json
     * Slide the drawer out from the right to have a handy one click login. FYI: Only works on Debug.
     * Sample Format is:

     [
     {
     "password":"password",
     "subtitle":"subtitle",
     "title":"title",
     "username":"username"
     },
     ...
     ]

     */
    private void eatSnickerDoodles() {
        Writer writer = new StringWriter();
        try {
            InputStream is = getResources().openRawResource(getResources().getIdentifier("snickers", "raw", getPackageName()));

            char[] buffer = new char[1024];
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) { writer.write(buffer, 0, n); }
            is.close();
        } catch (Exception e) {
            //Do Nothing
        }

        String jsonString = writer.toString();
        if(jsonString != null && jsonString.length() > 0) {
            ArrayList<SnickerDoodle> snickerDoodles = new Gson().fromJson(jsonString, new TypeToken<ArrayList<SnickerDoodle>>(){}.getType());

            if(snickerDoodles.size() == 0) {
                findViewById(R.id.drawerEmptyView).setVisibility(View.VISIBLE);
                findViewById(R.id.drawerEmptyText).setVisibility(View.VISIBLE);
                return;
            }

            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.setWebChromeClient(new WebChromeClient());

            mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
            mDrawerRecyclerView.setAdapter(new SnickerDoodleAdapter(snickerDoodles, new SnickerDoodleAdapter.SnickerCallback() {
                @Override
                public void onClick(SnickerDoodle snickerDoodle) {
                    mDrawerLayout.closeDrawers();

                    final String js = "javascript: { " +
                            "document.getElementsByName('pseudonym_session[unique_id]')[0].value = '" + snickerDoodle.username + "'; " +
                            "document.getElementsByName('pseudonym_session[password]')[0].value = '" + snickerDoodle.password + "'; " +
                            "document.getElementsByClassName('btn')[0].click(); " +
                            "};";

                    mWebView.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {}
                    });

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final String js = "javascript: { " +
                                            "document.getElementsByClassName('btn')[0].click();" +
                                            "};";

                                    mWebView.evaluateJavascript(js, new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String s) {}
                                    });
                                }
                            });
                        }
                    }, 750);
                }
            }));
        } else {
            findViewById(R.id.drawerEmptyView).setVisibility(View.VISIBLE);
            findViewById(R.id.drawerEmptyText).setVisibility(View.VISIBLE);
        }
    }

    //endregion
}
