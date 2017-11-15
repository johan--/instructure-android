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
package com.instructure.teacher.router;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.instructure.canvasapi2.models.CanvasContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route implements Parcelable {

    //region Members

    @Nullable private CanvasContext mCanvasContext = null;

    @NonNull private Bundle mArguments = new Bundle();

    /* The pattern of the URL we want to match against */
    private Pattern mRoutePattern;

    /* The original URL */
    @Nullable private String mUrl = null;

    @Nullable private Uri mUri;

    /* Primary Java/Kotlin Class Name ex: AssignmentsList.class */
    @Nullable private Class<? extends Fragment> mPrimaryClass = null;

    /* Secondary Java/Kotlin Class Name ex: AssignmentsList.class */
    @Nullable private Class<? extends Fragment> mSecondaryClass = null;

    /* The hash of params we care about. ex: assignments/12345 */
    private HashMap<String, String> mParamsHash = new HashMap<>();

    /* The hash of query params we care about. ex: ?modules/12345 */
    private HashMap<String, String> mQueryParamsHash = new HashMap<>();

    /* A temporary store of param names that get used when we obtain a URL */
    private ArrayList<String> mParamNames = new ArrayList<>();

    /* A temporary store of query param names that get used when we obtain a URL */
    private List<String> mQueryParamNames = new ArrayList<>();

    /*The Course ID, no course id if not relevant. ex: Inbox vs AssignmentsList */
    @Nullable private Long mCourseId = null;

    private RouteContext mRouteContext = RouteContext.UNKNOWN;

    public enum RouteContext {
        INTERNAL, FILE, LTI, MEDIA, SPEED_GRADER, EXTERNAL, DO_NOT_ROUTE, UNKNOWN
    }

    //endregion

    public Route(String route) {
        if(route == null) return;

        /* match anything but a slash after a colon and create a group for the name of the param */
        Matcher matcher = Pattern.compile("/:([^/]*)").matcher(route);

        // Get the names of the params
        while (matcher.find()) {
            mParamNames.add(matcher.group(1));
        }

        /* match a slash, colon and then anything but a slash. Matched value is replaced so the param value can be parsed */
        Matcher paramValueMatcher = Pattern.compile("/:[^/]*").matcher(route);

        if (paramValueMatcher.find()) {
            /* Create a group where the param was, so the value can be located */
            String paramValueRegex = paramValueMatcher.replaceAll("/([^/]*)");
            paramValueRegex = addLineMatchingAndOptionalEndSlash(paramValueRegex);
            mRoutePattern = Pattern.compile(paramValueRegex);
        } else { // does not contain params, just look for exact match
            mRoutePattern = Pattern.compile(addLineMatchingAndOptionalEndSlash(route));
        }
    }

    public Route(@Nullable String route, RouteContext routeContext) {
        this(route);
        this.mRouteContext = routeContext;
    }

    public <Type extends Fragment> Route(@Nullable String route, @Nullable Class<Type> primaryClass) {
        this(route);
        mPrimaryClass = primaryClass;
    }

    public Route(@Nullable String route, Class<? extends Fragment> primaryClass, @Nullable Class<? extends Fragment> secondaryClass) {
        this(route, primaryClass);
        this.mSecondaryClass = secondaryClass;
    }

    public String getUrl() { return this.mUrl; }

    @Nullable
    public Uri getUri() {
        return mUri;
    }

    public Route(@Nullable Class<? extends Fragment> primaryClass, @Nullable CanvasContext canvasContext) {
        this.mPrimaryClass = primaryClass;
        this.mSecondaryClass = null;
        this.mCanvasContext = canvasContext;
    }

    public Route(@Nullable Class<? extends Fragment> primaryClass, @Nullable CanvasContext canvasContext, @NonNull Bundle arguments) {
        this(primaryClass, canvasContext);
        this.mArguments = arguments;
    }

    public Route(@Nullable Class<? extends Fragment> primaryClass, @Nullable Class<? extends Fragment> secondaryClass, @Nullable CanvasContext canvasContext, @NonNull Bundle arguments) {
        this(null, primaryClass, secondaryClass);
        this.mCanvasContext = canvasContext;
        this.mArguments = arguments;
    }

    public Route(@NonNull Bundle bundle, @NonNull RouteContext routeContext) {
        this.mArguments = bundle;
        this.mRouteContext = routeContext;
    }

    public RouteContext getRouteContext() {
        return mRouteContext;
    }

    public HashMap<String, String> getParamsHash() {
        return mParamsHash;
    }

    public HashMap<String, String> getQueryParamsHash() {
        return mQueryParamsHash;
    }

    @Nullable
    public Class<? extends Fragment> getPrimaryClass() {
        return mPrimaryClass;
    }

    @Nullable
    public Class<? extends Fragment> getSecondaryClass() {
        return mSecondaryClass;
    }

    @Nullable
    public CanvasContext getCanvasContext() {
        return mCanvasContext;
    }

    @NonNull
    public Bundle getArguments() {
        return mArguments;
    }

    public static long extractCourseId(@Nullable Route route) {
        if(route != null && route.getParamsHash().containsKey(RouterParams.Companion.getCOURSE_ID())) {
            return Long.parseLong(route.getParamsHash().get(RouterParams.Companion.getCOURSE_ID()));
        }
        return 0L;
    }
    /**
     * When a route is a match, the paramsHash, queryParamsHash, and Uri are set.
     *
     * @param url A Url string to be checked against routes
     * @return true is route is a match, false otherwise
     */
    public boolean apply(String url) {
        if (url == null) {
            return false;
        }
        Uri parsedUri = Uri.parse(url);
        String path = parsedUri.getPath();
        boolean isMatch = mRoutePattern.matcher(path).find();
        if (isMatch) {
            if (RouteContext.EXTERNAL.equals(mRouteContext)) {
                return true; // recognized as a match so the unsupported fragment doesn't match it, then getInternalRoute will handle it
            }

            mUri = parsedUri;
            mParamsHash = createParamsHash(path);
            mQueryParamsHash = createQueryParamsHash(parsedUri);

            if (mQueryParamNames != null) {
                return checkQueryParamNamesExist(mQueryParamNames, mQueryParamsHash.keySet());
            }

            mUrl = url;
        }
        return isMatch;
    }

    public boolean apply(Class<? extends Fragment> primaryClass, Class<? extends Fragment> secondaryClass) {
        return (!RouteContext.EXTERNAL.equals(mRouteContext) && primaryClass == mPrimaryClass && secondaryClass == mSecondaryClass);
    }

    /**
     * A param hash contains the key and values for the route
     * Example: If the route is /courses/:course_id and the url /courses/1234 is applied. The paramsHash will contain "course_id" -> "1234"
     *
     * @param url A Url String used to create a hash of url params
     * @return HashMap of url params
     */
    private HashMap<String, String> createParamsHash(String url) {
        HashMap<String, String> params = new HashMap<>();
        Matcher matcher = mRoutePattern.matcher(url);
        ArrayList<String> paramValues = new ArrayList<>();
        if (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                try {
                    // index 0 is the original string that was matched. Just get the group values
                    paramValues.add(matcher.group(i + 1));
                } catch (Exception e) {
                    //do nothing
                }
            }
        }
        for (int i = 0; i < mParamNames.size(); i++) {
            if (i < paramValues.size()) {
                params.put(mParamNames.get(i), paramValues.get(i));
            }
        }

        return params;
    }

    /**
     * Query params for the url.
     * Example: The url /courses/1234?hello=world would have a Query params hash containing "hello" -> "world"
     *
     * @return HashMap of query params
     */
    private HashMap<String, String> createQueryParamsHash(@Nullable Uri uri) {
        HashMap<String, String> queryParams = new HashMap<>();
        if (uri != null) {
            for (String param : uri.getQueryParameterNames()) {
                queryParams.put(param, uri.getQueryParameter(param));
            }
        }
        return queryParams;
    }

    /**
     * Adds '^' and '$' to regex for line matching
     * Also makes ending slash and api/v1 optional
     *
     * @param regex
     * @return
     */
    private String addLineMatchingAndOptionalEndSlash(String regex) {
        if (regex.endsWith("/")) {
            regex = String.format("^(?:/api/v1)?%s?$", regex);
        } else {
            regex = String.format("^(?:/api/v1)?%s/?$", regex);
        }
        return regex;
    }

    private boolean checkQueryParamNamesExist(List<String> expectedQueryParams, Set<String> actualQueryParams) {
        for (String expectedKey : expectedQueryParams) {
            if (!actualQueryParams.contains(expectedKey)) {
                return false;
            }
        }
        return true;
    }

    public CanvasContext.Type getContextType() {
        if(mUrl == null && mCanvasContext == null) return CanvasContext.Type.UNKNOWN;
        if(mCanvasContext != null) return mCanvasContext.getType();

        Matcher coursesMatcher = Pattern.compile("^/courses/?").matcher(mUrl);
        if (coursesMatcher.find()) {
            return CanvasContext.Type.COURSE;
        }

        Matcher groupsMatcher = Pattern.compile("^/groups/?").matcher(mUrl);
        if (groupsMatcher.find()) {
            return CanvasContext.Type.GROUP;
        }

        Matcher usersMatcher = Pattern.compile("^/users/?").matcher(mUrl);
        if (usersMatcher.find()) {
            return CanvasContext.Type.USER;
        }

        return CanvasContext.Type.UNKNOWN;
    }

    public String getQueryString() {
        return mUri.getQuery();
    }

    public String getFragmentIdentifier() {
        return mUri.getFragment();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCanvasContext, flags);
        dest.writeBundle(this.mArguments);
        dest.writeSerializable(this.mRoutePattern);
        dest.writeString(this.mUrl);
        dest.writeSerializable(this.mPrimaryClass);
        dest.writeSerializable(this.mSecondaryClass);
        dest.writeSerializable(this.mParamsHash);
        dest.writeSerializable(this.mQueryParamsHash);
        dest.writeStringList(this.mParamNames);
        dest.writeStringList(this.mQueryParamNames);
        dest.writeValue(this.mCourseId);
        dest.writeInt(this.mRouteContext == null ? -1 : this.mRouteContext.ordinal());
    }

    protected Route(Parcel in) {
        this.mCanvasContext = in.readParcelable(CanvasContext.class.getClassLoader());
        this.mArguments = in.readBundle(getClass().getClassLoader());
        this.mRoutePattern = (Pattern) in.readSerializable();
        this.mUrl = in.readString();
        this.mPrimaryClass = (Class<? extends Fragment>) in.readSerializable();
        this.mSecondaryClass = (Class<? extends Fragment>) in.readSerializable();
        this.mParamsHash = (HashMap<String, String>) in.readSerializable();
        this.mQueryParamsHash = (HashMap<String, String>) in.readSerializable();
        this.mParamNames = in.createStringArrayList();
        this.mQueryParamNames = in.createStringArrayList();
        this.mCourseId = (Long) in.readValue(Long.class.getClassLoader());
        int tmpMRouteContext = in.readInt();
        this.mRouteContext = tmpMRouteContext == -1 ? null : RouteContext.values()[tmpMRouteContext];
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel source) {
            return new Route(source);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };
}

