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

package com.instructure.canvasapi2.managers;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.AccountNotificationAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.AccountNotification;
import com.instructure.canvasapi2.tests.AccountNotificationManager_Test;
import com.instructure.canvasapi2.utils.ExhaustiveListCallback;

import java.util.List;


/**
 * Manager for working with account notifications (aka global announcements)
 */
public class AccountNotificationManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getAccountNotificationForStudentAirwolf(String airwolfDomain, String parentId, String studentId, String accountNotificationId, boolean forceNetwork, StatusCallback<AccountNotification> callback) {
        if (isTesting() || mTesting) {
            AccountNotificationManager_Test.getAccountNotificationForStudent(parentId, studentId, accountNotificationId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withForceReadFromNetwork(forceNetwork)
                    .withAPIVersion("")
                    .build();

            AccountNotificationAPI.getAccountNotificationForStudentById(adapter, params, parentId, studentId, accountNotificationId, callback);
        }
    }

    public static void getAccountNotifications(StatusCallback<List<AccountNotification>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(true)
                    .build();

            AccountNotificationAPI.getAccountNotifications(adapter, params, callback);
        }
    }

    public static void getAllAccountNotifications(StatusCallback<List<AccountNotification>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            final RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withPerPageQueryParam(true)
                    .build();

            StatusCallback<List<AccountNotification>> depaginatedCallback = new ExhaustiveListCallback<AccountNotification>(callback) {
                @Override
                public void getNextPage(StatusCallback<List<AccountNotification>> callback, String nextUrl, boolean isCached) {
                    AccountNotificationAPI.getAccountNotifications(adapter, params, callback);
                }
            };

            adapter.setStatusCallback(depaginatedCallback);
            AccountNotificationAPI.getAccountNotifications(adapter, params, depaginatedCallback);
        }
    }

    public static void deleteAccountNotification(long notificationId, StatusCallback<AccountNotification> callback) {
        if (isTesting() || mTesting) {
            // TODO
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder().build();
            AccountNotificationAPI.deleteAccountNotification(notificationId, adapter, params, callback);
        }
    }

}
