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

package com.ebuki.homework.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.ebuki.homework.R;
import com.ebuki.homework.activity.NavigationActivity;
import com.ebuki.homework.model.PushNotification;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PushExternalReceiver extends WakefulBroadcastReceiver {

    public static final String NEW_PUSH_NOTIFICATION = "newPushNotification";

    private enum TYPE {RECEIVE, REGISTER, REGISTRATION, NONE}

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("PushExternalReceiver onReceive()");
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                String html_url = extras.getString(PushNotification.HTML_URL, "");
                final String from = extras.getString(PushNotification.FROM, "");
                final String alert = extras.getString(PushNotification.ALERT, "");
                final String collapse_key = extras.getString(PushNotification.COLLAPSE_KEY, "");
                final String user_id = extras.getString(PushNotification.USER_ID, "");

                if(getActionType(intent.getAction()) == TYPE.RECEIVE) {
                    PushNotification push = new PushNotification(html_url, from, alert, collapse_key, user_id);
                    if(PushNotification.store(context, push)) {
                        postNotification(context, extras);
                    }
                }
            }
            PushExternalReceiver.completeWakefulIntent(intent);
        }
    }

    private TYPE getActionType(String action) {
        if("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
            return TYPE.RECEIVE;
        } else if("com.google.android.c2dm.intent.REGISTER".equals(action)) {
            return TYPE.REGISTER;
        } else if("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
            return TYPE.REGISTRATION;
        }
        return TYPE.NONE;
    }

    public static void postNotification(Context context, Bundle extras){

        final User user = ApiPrefs.getUser();
        final String userDomain = ApiPrefs.getDomain();
        final String url = getHtmlUrl(extras);
        final String notificationUserId = PushNotification.getUserIdFromPush(getUserId(extras));

        String incomingDomain = "";

        try {
            incomingDomain = new URL(url).getHost();
        } catch (MalformedURLException e) {
            Logger.e("HTML URL MALFORMED");
        } catch (NullPointerException e) {
            Logger.e("HTML URL IS NULL");
        }

        if(user != null && !TextUtils.isEmpty(notificationUserId)) {
            String currentUserId = Long.toString(user.getId());
            if(!notificationUserId.equalsIgnoreCase(currentUserId)) {
                Logger.e("USER IDS MISMATCHED");
                return;
            }
        } else {
            Logger.e("USER WAS NULL OR USER_ID WAS NULL");
            return;
        }

        if(TextUtils.isEmpty(incomingDomain) || TextUtils.isEmpty(userDomain) || !incomingDomain.equalsIgnoreCase(userDomain)) {
            Logger.e("DOMAINS DID NOT MATCH");
            return;
        }

        List<PushNotification> pushes = PushNotification.getStoredPushes(context);

        if(pushes.size() == 0 && extras == null) {
            //Nothing to post, situation would occur from the BootReceiver
            return;
        }

        final Intent contentIntent = new Intent(context, NavigationActivity.getStartActivityClass());
        contentIntent.putExtra(NEW_PUSH_NOTIFICATION, true);
        if(extras != null) {
            contentIntent.putExtras(extras);
        }

        final Intent deleteIntent = new Intent(context, PushDeleteReceiver.class);

        final PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final String channelId = "generalNotifications";

        final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(context.getString(R.string.notification_primary_inbox_title));
        for (PushNotification push : pushes) {
            inboxStyle.addLine(push.alert);
        }

        final Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getMessage(extras))
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setAutoCancel(false)
                .setStyle(inboxStyle)
                .build();

        createNotificationChannel(context, channelId, user.getPrimaryEmail(), nm);

        nm.notify(555443, notification);
    }

    private static void createNotificationChannel(Context context, String channelId, String userEmail, NotificationManager nm) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        //Prevents recreation of notification channel if it exists.
        List<NotificationChannel> channelList = nm.getNotificationChannels();
        for (NotificationChannel channel : channelList) {
            if(channelId.equals(channel.getId())) {
                return;
            }
        }

        CharSequence name = context.getString(R.string.notification_channel_name_primary);
        String description = context.getString(R.string.notification_channel_description_primary);

        //Create a group for the user, this enabes support for multiple users
        nm.createNotificationChannelGroup(new NotificationChannelGroup(userEmail, name));

        //Create the channel and add the group
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setGroup(userEmail);

        //create the channel
        nm.createNotificationChannel(channel);
    }

    private static String getMessage(Bundle extras) {
        if(extras == null) {
            return "";
        }
        return extras.getString(PushNotification.ALERT, "");
    }

    private static String getUserId(Bundle extras) {
        if(extras == null) {
            return "";
        }
        return extras.getString(PushNotification.USER_ID, "");
    }

    private static String getHtmlUrl(Bundle extras) {
        if(extras == null) {
            return "";
        }
        return extras.getString(PushNotification.HTML_URL, "");
    }

    public static void postStoredNotifications(Context context) {
        postNotification(context, null);
    }
}
