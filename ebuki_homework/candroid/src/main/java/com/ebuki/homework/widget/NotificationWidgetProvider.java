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

package com.ebuki.homework.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ebuki.homework.R;
import com.ebuki.homework.activity.LoginActivity;
import com.ebuki.homework.activity.NotificationWidgetRouter;

public class NotificationWidgetProvider extends CanvasWidgetProvider {
    public final static int NOTIFICATIONS_REFRESH_ID = 4;
    public final static String REFRESH = "com.ebuki.homework.widget.notificationwidget.REFRESH";
    private final static String SIMPLE_NAME = "Nofification Widget";

    @Override
    public String getWidgetSimpleName() {
        return SIMPLE_NAME;
    }

    @Override
    public String getRefreshString() { return REFRESH; }

    @Override
    public void setWidgetDependentViews(Context context, RemoteViews remoteViews, int appWidgetId, int textColor) {
        remoteViews.setRemoteAdapter(R.id.contentList, NotificationViewWidgetService.createIntent(context, appWidgetId));
        remoteViews.setTextViewText(R.id.widget_title, context.getString(R.string.notificationWidgetTitle));

        //Sets Titlebar to launch app when clicked
        Intent titleBarIntent = new Intent(context, LoginActivity.class);
        remoteViews.setOnClickPendingIntent(R.id.widget_logo, PendingIntent.getActivity(context, cycleBit++, titleBarIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        remoteViews.setInt(R.id.widget_root, "setBackgroundResource", BaseRemoteViewsService.getWidgetBackgroundResourceId(context.getApplicationContext(), appWidgetId));
        remoteViews.setTextColor(R.id.widget_title, textColor);

        Intent listViewItemIntent = new Intent(context, NotificationWidgetRouter.class);
        remoteViews.setPendingIntentTemplate(R.id.contentList, PendingIntent.getActivity(context, cycleBit++, listViewItemIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public int getRefreshIntentID() {
        return NOTIFICATIONS_REFRESH_ID;
    }

    @Override
    public Intent getRefreshIntent(Context context) {
        Intent updateIntent = new Intent(context, NotificationWidgetProvider.class);
        updateIntent.setAction(NotificationWidgetProvider.REFRESH);

        return updateIntent;
    }
}
