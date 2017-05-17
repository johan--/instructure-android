package com.instructure.canvasapi2.apis;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.AccountNotification;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class AccountNotificationAPI {

    interface AccountNotificationInterface {

        @GET("canvas/{parentId}/{studentId}/account_notifications/{notificationId}")
        Call<AccountNotification> getAccountNotificationForStudent(
                @Path("parentId") String parentId,
                @Path("studentId") String studentId,
                @Path("notificationId") String notificationId);
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
}
