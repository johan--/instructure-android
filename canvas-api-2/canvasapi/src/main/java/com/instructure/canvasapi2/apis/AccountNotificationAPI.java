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

package com.instructure.canvasapi2.apis;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.AccountNotification;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * APIs for working with account notifications (aka global announcements)
 */
public class AccountNotificationAPI {

    interface AccountNotificationInterface {

        @GET("canvas/{parentId}/{studentId}/account_notifications/{notificationId}")
        Call<AccountNotification> getAccountNotificationForStudent(
                @Path("parentId") String parentId,
                @Path("studentId") String studentId,
                @Path("notificationId") String notificationId);

        @GET("accounts/self/users/self/account_notifications")
        Call<List<AccountNotification>> getAccountNotifications();

        @GET
        Call<List<AccountNotification>> getNextPageNotifications(@Url String url);

        @DELETE("accounts/self/users/self/account_notifications/{accountNotificationId}")
        Call<AccountNotification> deleteAccountNotification(@Path("accountNotificationId") long accountNotificationId);
    }

    public static void getAccountNotificationForStudentById(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String notificationId,
            @NonNull StatusCallback<AccountNotification> callback){

        callback.addCall(adapter.build(AccountNotificationInterface.class, params).getAccountNotificationForStudent(parentId, studentId, notificationId)).enqueue(callback);
    }

    public static void getAccountNotifications(@NonNull RestBuilder adapter, @NonNull RestParams params, @NonNull StatusCallback<List<AccountNotification>> callback) {
        if (callback.isFirstPage()) {
            callback.addCall(adapter.build(AccountNotificationInterface.class, params).getAccountNotifications()).enqueue(callback);
        } else if (callback.moreCallsExist()){
            callback.addCall(adapter.build(AccountNotificationInterface.class, params).getNextPageNotifications(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void deleteAccountNotification(long notificationId, @NonNull RestBuilder adapter, @NonNull RestParams params, @NonNull StatusCallback<AccountNotification> callback){
        callback.addCall(adapter.build(AccountNotificationInterface.class, params).deleteAccountNotification(notificationId)).enqueue(callback);
    }
}
