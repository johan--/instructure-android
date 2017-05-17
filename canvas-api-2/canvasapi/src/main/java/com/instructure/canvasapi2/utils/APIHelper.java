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

package com.instructure.canvasapi2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.instructure.canvasapi2.apis.AlertAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Headers;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class APIHelper {

    private final static String SHARED_PREFERENCES_DISMISSED_NETWORK_ERROR = "dismissed_network_error";
    private final static String SHARED_PREFERENCES_AIRWOLF_DOMAIN = "airwolf_domain";

    public static boolean hasNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) ContextKeeper.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * parseLinkHeaderResponse parses HTTP headers to return the first, next, prev, and last urls. Used for pagination.
     *
     * @param headers List of headers
     * @return A LinkHeaders object
     */
    public static
    @NonNull
    LinkHeaders parseLinkHeaderResponse(Headers headers) {
        LinkHeaders linkHeaders = new LinkHeaders();

        Map<String, List<String>> map = headers.toMultimap();

        for (String name : map.keySet()) {
            if ("link".equalsIgnoreCase(name)) {
                for (String value : map.get(name)) {
                    String[] split = value.split(",");
                    for (int j = 0; j < split.length; j++) {
                        int index = split[j].indexOf(">");
                        String url = split[j].substring(0, index);
                        url = url.substring(1);

                        //Remove the domain.
                        try {
                            url = URLDecoder.decode(removeDomainFromUrl(url), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            url = removeDomainFromUrl(url);
                        }

                        if (split[j].contains("rel=\"next\"")) {
                            linkHeaders.nextUrl = url;
                        } else if (split[j].contains("rel=\"prev\"")) {
                            linkHeaders.prevUrl = url;
                        } else if (split[j].contains("rel=\"first\"")) {
                            linkHeaders.firstUrl = url;
                        } else if (split[j].contains("rel=\"last\"")) {
                            linkHeaders.lastUrl = url;
                        }
                    }
                }
                break;
            }
        }

        return linkHeaders;
    }

    /**
     * removeDomainFromUrl is a helper function for removing the domain from a url. Used for pagination/routing
     *
     * @param url A url
     * @return a String without a domain
     */
    public static String removeDomainFromUrl(String url) {
        if (url == null) {
            return null;
        }

        String prefix = "/api/v1/";
        int index = url.indexOf(prefix);
        if (index != -1) {
            url = url.substring(index + prefix.length());
        }
        return url;
    }

    public static boolean isCachedResponse(@NonNull okhttp3.Response response) {
        return response.cacheResponse() != null;
    }

    public static boolean isCachedResponse(@NonNull Response response) {
        return isCachedResponse(response.raw());
    }


    public static boolean paramIsNull(Object... args) {
        if(args == null) return true;
        for (Object arg : args) {
            if (arg == null) {
                return true;
            }
        }
        return false;
    }

    //Don't make static please
    public Observable<LinkHeaders> processHeaders(@NonNull final okhttp3.Response response) {

        return Observable.create(new Observable.OnSubscribe<LinkHeaders>() {
            @Override
            public void call(Subscriber<? super LinkHeaders> subscriber) {
                try {
                    subscriber.onNext(APIHelper.parseLinkHeaderResponse(response.headers()));
                } catch (Exception e) {
                    subscriber.onError(e);
                    Logger.e("Could not process headers: " + e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Date stringToDate(final String iso8601string) {
        try {
            String s = iso8601string.replace("Z", "+00:00");
            s = s.substring(0, 22) + s.substring(23);
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static String dateToString(final Date date) {
        if (date == null) {
            return null;
        }

        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    public static String dateToString(final GregorianCalendar date) {
        if (date == null) {
            return null;
        }

        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(new Date(date.getTimeInMillis()));
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /**
     * booleanToInt is a Helper function for Converting boolean to URL booleans (ints)
     */
    public static int booleanToInt(boolean bool) {
        if (bool) {
            return 1;
        }
        return 0;
    }

    /**
     * Sets whether the user has seen the network error message
     *
     * @param context
     * @param hasSeenErrorMessage
     */
    public static void setHasSeenNetworkErrorMessage(Context context, boolean hasSeenErrorMessage) {
        if (context == null) return;

        SharedPreferences sharedPreferences = context.getSharedPreferences(ApiPrefsKt.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sharedPrefsKey = SHARED_PREFERENCES_DISMISSED_NETWORK_ERROR;
        editor.putBoolean(sharedPrefsKey, hasSeenErrorMessage);
        editor.apply();
    }

    /*
     *
     * GetAssetsFile allows you to open a file that exists in the Assets directory.
     *
     * @param context
     * @param fileName
     * @return the contents of the file.
     */
    public static String getAssetsFile(Context context, String fileName) {
        try {
            String file = "";
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(fileName)));

            // do reading
            String line = "";
            while (line != null) {
                file += line;
                line = reader.readLine();
            }

            reader.close();
            return file;

        } catch (Exception e) {
            return "";
        }
    }

    /*
     * The fromHTML method can cause a character that looks like [obj]
     * to show up. This is undesired behavior most of the time.
     *
     * Replace the [obj] with an empty space
     * [obj] is char 65532 and an empty space is char 32
     * @param sequence The fromHTML typically
     * @return The modified charSequence
     */
    public static String simplifyHTML(CharSequence sequence) {
        if(sequence != null) {
            CharSequence toReplace = sequence;
            toReplace = toReplace.toString().replace(((char) 65532), (char) 32).trim();
            return toReplace.toString();
        }
        return "";
    }

    public static Map<String,String> getAuthenticatedURL(Context context) {
        String token = ApiPrefs.getToken();
        String headerValue = null;
        headerValue = String.format("Bearer %s", token);
        Map<String,String> map = new HashMap<String,String>();
        map.put("Authorization", headerValue);
        return map;
    }

    public static Map<String, String> getReferrer(Context context){
        Map<String, String> extraHeaders = new HashMap<>();
        //Spelled as it should, misspelled...
        extraHeaders.put("Referer", ApiPrefs.getDomain());
        return extraHeaders;
    }

    public static Map<String, String> getReferrerAndAuthentication(Context context){
        Map<String, String> extraHeaders = getAuthenticatedURL(context);
        //Spelled as it should, misspelled...
        extraHeaders.put("Referer", ApiPrefs.getDomain());
        return extraHeaders;
    }

    public static RestParams paramsWithDomain(String domain, RestParams params) {
        RestParams.Builder builder = new RestParams.Builder(params).withDomain(domain);
        return builder.build();
    }

    //region Airwolf


    /**
     * Check to see if the Airwolf domain is set. We don't want to return an empty domain, so this check
     * will return whether the user has set a domain.
     *
     * @param context
     * @return True if an Airwolf domain has been set, false otherwise
     */
    public static boolean isAirwolfDomainSet(Context context) {
        if(context == null){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(ApiPrefsKt.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        //if the shared preference here is empty, that means a domain hasn't been set
        return !TextUtils.isEmpty(sharedPreferences.getString(SHARED_PREFERENCES_AIRWOLF_DOMAIN, ""));
    }

    /**
     * Get the Airwolf region to use. This will be set when the user first opens the app depending on which region is fastest
     *
     * If no region is set it will use the American Region as default
     *
     * @param context
     * @return Domain to use for Airwolf API calls
     */
    public static String getAirwolfDomain(Context context) {
        if(context == null){
            return "";
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(ApiPrefsKt.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        //make america the default region
        return sharedPreferences.getString(SHARED_PREFERENCES_AIRWOLF_DOMAIN, AlertAPI.AIRWOLF_DOMAIN_AMERICA);
    }

    public static boolean airwolfDomainExists(Context context) {
        if(context == null){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(ApiPrefsKt.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(SHARED_PREFERENCES_AIRWOLF_DOMAIN);
    }

    /**
     * Sets the current Airwolf domain
     *
     * @param context
     * @param airwolfDomain
     * @return True if saved successfully, false otherwise
     */

    public static boolean setAirwolfDomain(Context context, String airwolfDomain) {

        if(airwolfDomain == null || airwolfDomain.equals("")){
            return false;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(ApiPrefsKt.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_AIRWOLF_DOMAIN, airwolfDomain);
        return editor.commit();
    }

    //endregion
}
