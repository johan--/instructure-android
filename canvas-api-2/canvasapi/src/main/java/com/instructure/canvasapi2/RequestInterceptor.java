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

package com.instructure.canvasapi2;

import android.support.annotation.Nullable;

import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;

import java.io.IOException;
import java.util.Locale;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class RequestInterceptor implements Interceptor {

    @Nullable
    private StatusCallback mCallback;

    public RequestInterceptor(StatusCallback callback) {
        mCallback = callback;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        Request.Builder builder = request.newBuilder();

        final String token = ApiPrefs.getToken();
        final String userAgent = ApiPrefs.getUserAgent();
        final String domain = ApiPrefs.getFullDomain();

        //Set the UserAgent
        if(!userAgent.equals("")) {
            builder.addHeader("User-Agent", userAgent);
        }

        //Authenticate if possible
        if(!ApiPrefs.getRestParams().shouldIgnoreToken() && !token.equals("")){
            builder.addHeader("Authorization", "Bearer " + token);
        }

        //Add Accept-Language header for a11y
        builder.addHeader("accept-language", getAcceptedLanguageString());

        if(!APIHelper.hasNetworkConnection() || ApiPrefs.getRestParams().isForceReadFromCache()) {
            //Offline or only want cached data
            builder.cacheControl(CacheControl.FORCE_CACHE);
        } else if(ApiPrefs.getRestParams().isForceReadFromNetwork()) {
            //Typical from a pull-to-refresh
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }

        //Fun Fact: HTTP referer (originally a misspelling of referrer) is an HTTP header field that identifies
        // the address of the webpage that linked to the resource being requested
        //Source: https://en.wikipedia.org/wiki/HTTP_referer
        //Institutions need the referrer for a variety of reasons - mostly for restricted content
        builder.addHeader("Referer", domain);

        request = builder.build();

        //Masquerade if necessary
        if (ApiPrefs.isMasquerading()) {
            HttpUrl url = request.url().newBuilder().addQueryParameter("as_user_id", Long.toString(ApiPrefs.getMasqueradeId())).build();
            request = request.newBuilder().url(url).build();
        }

        if(ApiPrefs.getRestParams().usePerPageQueryParam()) {
            HttpUrl url = request.url().newBuilder().addQueryParameter("per_page", Integer.toString(ApiPrefs.getPerPageCount())).build();
            request = request.newBuilder().url(url).build();
        }

        return chain.proceed(request);
    }

    public String getAcceptedLanguageString() {
        String language = Locale.getDefault().getLanguage();
        //This is kinda gross, but Android is terrible and doesn't use the standard for lang strings...
        String language3 = Locale.getDefault().toString().replace("_", "-");

        return language3 + "," + language;
    }
}
