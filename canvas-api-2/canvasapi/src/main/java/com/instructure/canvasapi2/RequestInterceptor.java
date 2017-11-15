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
package com.instructure.canvasapi2;

import android.support.annotation.Nullable;

import com.instructure.canvasapi2.builders.RestParams;
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

        /* Nearly all requests are instantiated using RestBuilder and will have been tagged with
        a RestParams instance. Here we will attempt to retrieve it, but if unsuccessful we will
        fall back to a new RestParams instance with default values. */
        RestParams params;
        if (request.tag() != null && request.tag() instanceof RestParams) {
            params = (RestParams) request.tag();
        } else {
            params = new RestParams.Builder().build();
        }

        //Set the UserAgent
        if(!userAgent.equals("")) {
            builder.addHeader("User-Agent", userAgent);
        }

        //Authenticate if possible
        if(!params.shouldIgnoreToken() && !token.equals("")){
            builder.addHeader("Authorization", "Bearer " + token);
        }

        //Add Accept-Language header for a11y
        builder.addHeader("accept-language", getAcceptedLanguageString());

        if(!APIHelper.hasNetworkConnection() || params.isForceReadFromCache()) {
            //Offline or only want cached data
            builder.cacheControl(CacheControl.FORCE_CACHE);
        } else if(params.isForceReadFromNetwork()) {
            //Typical from a pull-to-refresh
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }

        //Fun Fact: HTTP referer (originally a misspelling of referrer) is an HTTP header field that identifies
        // the address of the webpage that linked to the resource being requested
        //Source: https://en.wikipedia.org/wiki/HTTP_referer
        //Institutions need the referrer for a variety of reasons - mostly for restricted content
        // Strip out non-ascii characters, otherwise addHeader may throw an exception
        builder.addHeader("Referer", domain.replaceAll("[^\\x20-\\x7e]", ""));

        request = builder.build();

        //Masquerade if necessary
        if (ApiPrefs.isMasquerading()) {
            HttpUrl url = request.url().newBuilder().addQueryParameter("as_user_id", Long.toString(ApiPrefs.getMasqueradeId())).build();
            request = request.newBuilder().url(url).build();
        }

        if(params.usePerPageQueryParam()) {
            HttpUrl url = request.url().newBuilder().addQueryParameter("per_page", Integer.toString(ApiPrefs.getPerPageCount())).build();
            request = request.newBuilder().url(url).build();
        }

        return chain.proceed(request);
    }

    public static String getLocale() {
        // This is kinda gross, but Android is terrible and doesn't use the standard for lang strings...
        return Locale.getDefault().toString().replace("_", "-");
    }

    public static String getAcceptedLanguageString() {
        String language = Locale.getDefault().getLanguage();
        return getLocale() + "," + language;
    }

    public static String getSessionLocaleString() {
        String lang = getLocale();

        // Canvas supports Chinese (Traditional) and Chinese (Simplified)
        if (lang.equalsIgnoreCase("zh-hk") || lang.equalsIgnoreCase("zh-tw") || lang.equalsIgnoreCase("zh-hant-hk") || lang.equalsIgnoreCase("zh-hant-tw")) {
            lang = "zh-Hant";
        } else if (lang.equalsIgnoreCase("zh") || lang.equalsIgnoreCase("zh-cn") || lang.equalsIgnoreCase("zh-hans-cn")) {
            lang = "zh-Hans";
        } else if (!lang.equalsIgnoreCase("pt-BR") && !lang.equalsIgnoreCase("en-AU") && !lang.equalsIgnoreCase("en-GB")) {
            // Canvas only supports 3 region tags (not including Chinese), remove any other tags
            lang = Locale.getDefault().getLanguage();
        }

        return "?session_locale=" + lang;
    }
}
