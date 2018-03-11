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

package com.ebuki.homework.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import com.ebuki.homework.delegate.APIContract;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.AccountNotificationManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.LaunchDefinitionsManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.AccountNotification;
import com.instructure.canvasapi2.models.CanvasColor;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Enrollment;
import com.instructure.canvasapi2.models.LaunchDefinition;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.dialogs.RatingDialog;
import com.instructure.pandautils.utils.AppType;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import java.util.List;

import retrofit2.Call;

/**
 * This class is responsible for handling any API requests that base activities may require.
 */
public abstract class CallbackActivity extends ParentActivity implements
        APIContract {

    protected StatusCallback<User> userCallback;
    protected StatusCallback<List<Enrollment>> getUserEnrollments;
    protected StatusCallback<List<Course>> coursesCallback;
    protected StatusCallback<List<Course>> coursesNoCacheCallback;
    protected StatusCallback<List<AccountNotification>> accountNotificationCallback;
    protected StatusCallback<CanvasColor> courseColorsCallback;
    protected StatusCallback<List<LaunchDefinition>> accountLaunchDefinitionsCallback;

    protected User mUser;
    protected boolean hasNonTeacherEnrollment = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RatingDialog.showRatingDialog(CallbackActivity.this, AppType.CANDROID);
    }

    /**
     * Sometimes getUserSelf is not called in a callback (in onCreate) other times its called from a callback's first page
     *
     * In the case that it is called from within a callback, the booleans help make it so that there aren't duplicate callbacks
     *
     * Duplicate callbacks would look like the following
     *                                                                   Cache 1
     *                                   cache -> getCourseFavorites /
     *            cache -> getUserSelf /                             \   Network 1
     * getCourse /                     \
     *           \                       network -> getCourseFavorites / Cache 2`
     *            \                                                    \ Network 2
     *             \
     *              network -> getUserSelf / ... etc            Cache 3
     *                                     \                    Network 3
     *                                                          Cache 4
     *                                                          Network 4
     *
     * With the booleans and API chained methods, it'll avoid having the duplicate calls to the network, and will look like the following
     *
     *                                                                  Cache 1
     *                                   cache -> getCourseFavorites /
     *            cache -> getUserSelf /
     * getCourse /
     *           \
     *            \
     *             \
     *              network -> getUserSelf
     *                                     \
     *                                      network -> getCourseFavorites
     *                                                                   \
     *                                                                    network 1
     *
     * @param isWithinAnotherCallback Means that getUserSelf has been called from a callback (firstpage)
     * @param isCached Helps determine the path the call should take
     */
    public void getUserSelf(boolean isWithinAnotherCallback, boolean isCached) {
        setupCallbacks();
        setupListeners();

        UserManager.getSelf(true, userCallback);
        CourseManager.getAllFavoriteCourses(true, coursesCallback);

        getAccountNotifications(isWithinAnotherCallback, isCached);
        LaunchDefinitionsManager.getLaunchDefinitions(accountLaunchDefinitionsCallback, !isCached);
    }

    // isCached is only used when isWithinAnotherCallback
    public void getAccountNotifications(boolean isWithinAnotherCallback, boolean isCached) {
        if (isWithinAnotherCallback) {
            AccountNotificationManager.getAllAccountNotifications(accountNotificationCallback, !isCached);
        } else {
            AccountNotificationManager.getAccountNotifications(accountNotificationCallback, !isCached);
        }
    }

    @Override
    public void setupCallbacks() {
        getUserEnrollments = new StatusCallback<List<Enrollment>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Enrollment>> response, LinkHeaders linkHeaders, ApiType type) {
                if(response.body() != null) {
                    gotEnrollments(response.body());
                }
            }
        };

        userCallback = new StatusCallback<User>() {
            @Override
            public void onResponse(retrofit2.Response<User> response, LinkHeaders linkHeaders, ApiType type) {
                //We don't load from cache on this because it will load the users avatar two times and cause world hunger.
                //but if we're masquerading we want to, because masquerading can't get user info, so we need to read it from
                //cache
                User user = response.body();
                if(type.isCache()) {
                    if(ApiPrefs.isMasquerading()) {
                        setup(ApiPrefs.getUser());
                    } else if(!APIHelper.hasNetworkConnection()) {
                        setup(user);
                    }
                } else {
                    setup(user);
                }
            }

            private void setup(User user) {
                if (user != null) {
                    mUser = user;
                    ApiPrefs.setUser(user);
                    UserManager.getColors(courseColorsCallback, true);
                    UserManager.getSelfEnrollments(true, getUserEnrollments);
                    onUserCallbackFinished(mUser);
                }
            }
        };

        coursesCallback = new StatusCallback<List<Course>>() {

            @Override
            public void onResponse(retrofit2.Response<List<Course>> response, LinkHeaders linkHeaders, ApiType type) {

                for(Course course: response.body()){
                    if(!course.isTeacher()){
                        hasNonTeacherEnrollment = true;
                        break;
                    }
                }
                onCourseFavoritesFinished(response.body());
            }
        };

        coursesNoCacheCallback = new StatusCallback<List<Course>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Course>> response, LinkHeaders linkHeaders, ApiType type) {
                if(type == ApiType.API) {
                    for (Course course : response.body()) {
                        if (!course.isTeacher()) {
                            hasNonTeacherEnrollment = true;
                            break;
                        }
                    }
                    onCourseFavoritesFinished(response.body());
                }
            }
        };

        accountNotificationCallback = new StatusCallback<List<AccountNotification>>() {
            @Override
            public void onResponse(retrofit2.Response<List<AccountNotification>> response, LinkHeaders linkHeaders, ApiType type) {
                gotNotifications(response.body());
            }
        };

        courseColorsCallback = new StatusCallback<CanvasColor>() {
            @Override
            public void onResponse(retrofit2.Response<CanvasColor> response, LinkHeaders linkHeaders, ApiType type, int code) {
                if(code == 200) {
                    //Replaces the current cache with the updated fresh one from the api.
                    CanvasContextColor.addToCache(response.body());
                    //Sends a broadcast so the course grid can refresh it's colors if needed.
                    //When first logging in this will probably get called/return after the courses.
                    Intent intent = new Intent(Const.COURSE_THING_CHANGED);
                    Bundle extras = new Bundle();
                    extras.putBoolean(Const.COURSE_COLOR, true);
                    intent.putExtras(extras);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }
            }
        };

        accountLaunchDefinitionsCallback = new StatusCallback<List<LaunchDefinition>>() {
            @Override
            public void onResponse(retrofit2.Response<List<LaunchDefinition>> response, LinkHeaders linkHeaders, ApiType type) {
                if (response.code() == 200) {
                    for (LaunchDefinition definition : response.body()) {
                        if ("gauge.instructure.com".equals(definition.domain)) {
                            gotLaunchDefinitions(definition);
                            return;
                        }
                    }
                }
                gotLaunchDefinitions(null);
            }

            @Override
            public void onFail(Call<List<LaunchDefinition>> callResponse, Throwable error, retrofit2.Response response) {
                gotLaunchDefinitions(null);
            }
        };
    }

    public abstract void gotLaunchDefinitions(@Nullable LaunchDefinition launchDefinition);
    public abstract void gotEnrollments(List<Enrollment> enrollments);
    public abstract void gotNotifications(List<AccountNotification> accountNotifications);
    public abstract void onUserCallbackFinished(User user);

    public void onCourseFavoritesFinished(List<Course> courses) {}

    @Override
    public void setupListeners() {}

    @Override
    public void onCallbackFinished(ApiType type) {
        if(userCallback != null && !userCallback.isCallInProgress()) {
            super.onCallbackFinished(type);
        }
    }
}
