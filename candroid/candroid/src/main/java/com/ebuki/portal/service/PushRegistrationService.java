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

package com.instructure.candroid.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.canvasapi2.managers.CommunicationChannelsManager;
import com.instructure.canvasapi2.utils.Logger;

import retrofit2.Response;


public class PushRegistrationService extends IntentService {

    //A unique id for communication between us and the server
    private static final String PROJECT_ID = "191083992402";//Canvas4Android Project
    private static final String PUSH_TOKEN_SENT_TO_SERVER = "kittenWithMittens";

    public PushRegistrationService() {
        super("PushService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            // Initially this call goes out to the network to retrieve the token, subsequent calls are local.
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(PROJECT_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationToServer(token);

            // Setting this prevents multiple registrations.
            // Re-registration should only happen when the PUshInstanceIdService gets called with a new token
            setTokenSentToServer(getApplicationContext(), true);
        } catch (Exception e) {
            setTokenSentToServer(getApplicationContext(), false);
        }
    }

    private void sendRegistrationToServer(String token) throws Exception {
        Response response = CommunicationChannelsManager.addNewPushCommunicationChannelSynchronous(token);
        if(response != null && response.code() == 200) {
            Logger.d("PUSH REGISTRATION SUCCESS");
        } else {
            LoggingUtility.LogCrashlytics("PUSH GCM REGISTRATION ERROR");
        }
    }

    public static boolean hasTokenBeenSentToServer(Context context) {
        return ApplicationManager.getPrefs(context).load(PUSH_TOKEN_SENT_TO_SERVER, false);
    }

    public static void setTokenSentToServer(Context context, boolean wasSent) {
        ApplicationManager.getPrefs(context).save(PUSH_TOKEN_SENT_TO_SERVER, wasSent);
    }
}
