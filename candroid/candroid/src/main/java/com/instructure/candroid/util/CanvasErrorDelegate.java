/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.candroid.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.ParentActivity;
import com.instructure.candroid.asynctask.LogoutAsyncTask;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.UserCallback;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class CanvasErrorDelegate implements ErrorDelegate {

    @Override
    public void noNetworkError(RetrofitError error, final Context context) {

        if(APIHelpers.hasSeenNetworkErrorMessage(context) || !(context instanceof Activity)) {
            return;
        }
        //try to find the view the user is looking at so we can attach the snackbar to it
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);

        if(rootView != null) {
            Snackbar.make(rootView, R.string.noConnection, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            APIHelpers.setHasSeenNetworkErrorMessage(context, true);
                        }
                    })
                    .setActionTextColor(Color.WHITE)
                    .show();
        } else {
            Toast.makeText(context, R.string.noDataConnection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void notAuthorizedError(RetrofitError error, CanvasError canvasError, Context context) {
        //check to see if the access token is invalid and if it is then log them out.
        unauthorizedCheck(error, context);
    }

    @Override
    public void invalidUrlError(RetrofitError error, Context context) {
        int errorMessage = R.string.errorOccurred;
        if (error.getResponse().getStatus() == 404) {
            errorMessage = R.string.pageNotFound;
        }

        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();

        LoggingUtility.Log(context, Log.ERROR, "Invalid URL Error ( Status Code: " +error.getResponse().getStatus() +", URL: " +error.getUrl() +")");
        LoggingUtility.postCrashlyticsLogs(error.getUrl());
    }

    @Override
    public void serverError(RetrofitError error, Context context) {
        Toast.makeText(context, R.string.serverError, Toast.LENGTH_SHORT).show();

        LoggingUtility.Log(context, Log.ERROR, "Invalid URL Error ( Status Code: " +error.getResponse().getStatus() +", URL: " +error.getUrl() +")");
        LoggingUtility.postCrashlyticsLogs(error.getUrl());
    }

    @Override
    public void generalError(RetrofitError error, CanvasError canvasError, Context context) {
        Toast.makeText(context, R.string.errorOccurred, Toast.LENGTH_SHORT).show();

        //Log to Google Analytics
        if(error.getResponse() != null) {
            LoggingUtility.Log(context, Log.ERROR, "Invalid URL Error ( Status Code: " + error.getResponse().getStatus() + ", URL: " + error.getUrl() + ")");
        }
        LoggingUtility.postCrashlyticsLogs(error.getUrl());
        if (canvasError != null) {
            LoggingUtility.Log(context, Log.ERROR, canvasError.toString());
        }
    }

    /**
     * If we get a 401 we want to check to make sure their access token hasn't been revoked.
     * We'll do this by making an api call to get the user. If we get another 401, we'll log
     * them out.
     *
     * @param context
     */
    private void unauthorizedCheck(final RetrofitError error, final Context context) {
        UserCallback userCallback = new UserCallback(APIHelpers.statusDelegateWithContext(context)) {
            @Override
            public void cachedUser(User user) {

            }

            @Override
            public void user(User user, Response response) {
                //something was unauthorized, but the user was okay, so don't log them out. show them that whatever they
                //tried wasn't authorized.
                showUnauthorizedMessage(error, context);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if(retrofitError.getResponse().getStatus() == 401) {
                    //log them out
                    new LogoutAsyncTask((ParentActivity) context, context.getString(R.string.invalidAccessToken))
                            .execute();
                } else {
                    showUnauthorizedMessage(error, context);
                }
            }
        };

        UserAPI.getSelf(userCallback);
    }


    private void showUnauthorizedMessage(RetrofitError error, Context context) {
        Toast.makeText(context, R.string.unauthorized, Toast.LENGTH_SHORT).show();

        LoggingUtility.Log(context, Log.ERROR, "Not authorized error (URL: " + error.getUrl() + ")");

        LoggingUtility.postCrashlyticsLogs(error.getUrl());
    }
}