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

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.RecipientAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Recipient;
import com.instructure.canvasapi2.tests.RecipientManager_Test;
import com.instructure.canvasapi2.utils.ExhaustiveListCallback;

import java.util.List;


public class RecipientManager extends BaseManager {

    private static boolean mTesting = false;

    public static void searchRecipients(String searchQuery, String context, StatusCallback<List<Recipient>> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();
            RecipientManager_Test.getFirstPageRecipients(searchQuery, context, callback, adapter, params);
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();
            RecipientAPI.getRecipients(searchQuery, context, callback, adapter, params);
        }
    }

    public static void searchAllRecipients(final boolean forceNetwork, String searchQuery, String context, StatusCallback<List<Recipient>> callback) {
        if (isTesting() || mTesting) {
            // TODO...
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            StatusCallback<List<Recipient>> depaginatedCallback = new ExhaustiveListCallback<Recipient>(callback) {
                @Override
                public void getNextPage(@NonNull StatusCallback<List<Recipient>> callback, @NonNull String nextUrl, boolean isCached) {
                    RecipientAPI.getNextPageRecipients(forceNetwork, nextUrl, adapter, callback);
                }
            };
            adapter.setStatusCallback(depaginatedCallback);
            RecipientAPI.getFirstPageRecipients(forceNetwork, searchQuery, context, adapter, depaginatedCallback);
        }
    }

    /**
     * Synthetic contexts == sections and groups, so this will return only actual users, not groups or sections
     *
     * @param forceNetwork
     * @param searchQuery
     * @param context
     * @param callback
     */
    public static void searchAllRecipientsNoSyntheticContexts(final boolean forceNetwork, String searchQuery, String context, StatusCallback<List<Recipient>> callback) {
        if (isTesting() || mTesting) {
            // TODO...
        } else {
            final RestBuilder adapter = new RestBuilder(callback);
            StatusCallback<List<Recipient>> depaginatedCallback = new ExhaustiveListCallback<Recipient>(callback) {
                @Override
                public void getNextPage(@NonNull StatusCallback<List<Recipient>> callback, @NonNull String nextUrl, boolean isCached) {
                    RecipientAPI.getNextPageRecipients(forceNetwork, nextUrl, adapter, callback);
                }
            };
            adapter.setStatusCallback(depaginatedCallback);
            RecipientAPI.getFirstPageRecipientsNoSyntheticContexts(forceNetwork, searchQuery, context, adapter, depaginatedCallback);
        }
    }

}
