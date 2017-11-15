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

package com.instructure.candroid.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.InterwebsToApplication;
import com.instructure.candroid.activity.LoginActivity;

public class GradesWidgetProvider extends CanvasWidgetProvider {

    public final static int GRADES_REFRESH_ID = 3;

    public final static String REFRESH = "com.instructure.candroid.widget.gradeswidget.REFRESH";
    private final static String SIMPLE_NAME = "Grades Widget";

    @Override
    public String getRefreshString() {
        return REFRESH;
    }

    @Override
    public String getWidgetSimpleName() {
        return SIMPLE_NAME;
    }

    @Override
    public Intent getRefreshIntent(Context context) {
        Intent updateIntent = new Intent(context, GradesWidgetProvider.class);
        updateIntent.setAction(GradesWidgetProvider.REFRESH);

        return updateIntent;
    }

    @Override
    public void setWidgetDependentViews(Context context, RemoteViews remoteViews, int appWidgetId, int textColor) {
        remoteViews.setRemoteAdapter(R.id.contentList, GradesViewWidgetService.createIntent(context, appWidgetId));
        remoteViews.setTextViewText(R.id.widget_title, context.getString(R.string.gradesWidgetTitle));

        //Sets Titlebar to launch app when clicked
        Intent titleBarIntent = new Intent(context, LoginActivity.class);
        remoteViews.setOnClickPendingIntent(R.id.widget_logo, PendingIntent.getActivity(context, cycleBit++, titleBarIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        remoteViews.setInt(R.id.widget_root, "setBackgroundResource", BaseRemoteViewsService.getWidgetBackgroundResourceId(context.getApplicationContext(), appWidgetId));
        remoteViews.setTextColor(R.id.widget_title, textColor);

        Intent listViewItemIntent = new Intent(context, InterwebsToApplication.class);
        remoteViews.setPendingIntentTemplate(R.id.contentList, PendingIntent.getActivity(context, cycleBit++, listViewItemIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public int getRefreshIntentID() {
        return GRADES_REFRESH_ID;
    }
}

