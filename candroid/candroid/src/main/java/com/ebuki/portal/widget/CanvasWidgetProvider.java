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

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.WidgetSetupActivity;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;

public abstract class CanvasWidgetProvider extends AppWidgetProvider {

    public static int cycleBit = 100;


    public static final String REFRESH_ALL = "com.instructure.candroid.widget.allwidget.REFRESH";

    //region Broadcast Callbacks
    @Override
    public void onEnabled(Context context) {
        if(context instanceof Activity) {
            //This may not return a context of type activity...
            Analytics.trackWidgetFlow((Activity)context, getWidgetSimpleName());
        }
        updateWidgetList(context);
    }

    /**
     * Called when the widget is added to the home screen.
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {

        if(context == null || appWidgetManager == null || appWidgetIds.length == 0) {return;}

        for(int id : appWidgetIds) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = buildUpdate(manager, id, context);
            pushWidgetUpdate(remoteViews, id, context);
        }
    }

    /**
     * This is a callback that occurs when the refresh button is pressed.
     * It determines the origin of the
     * broadcast based of the action string.
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        if (action.equals(getRefreshString()) || action.equals("android.net.conn.CONNECTIVITY_CHANGE") || action.equals(CanvasWidgetProvider.REFRESH_ALL)) {
            updateWidgetList(context);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for(int id : appWidgetIds) {
            ApplicationManager.getPrefs(context).remove(WidgetSetupActivity.WIDGET_BACKGROUND_PREFIX + id);
        }

        super.onDeleted(context, appWidgetIds);
    }

    //endregion

    protected void pushWidgetUpdate(RemoteViews remoteViews, int widgetId, Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(widgetId, remoteViews);
    }


    public RemoteViews buildUpdate(AppWidgetManager appWidgetManager, int widgetId, Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_homescreen);
        final int textColor = BaseRemoteViewsService.getWidgetTextColor(widgetId, context.getApplicationContext());
        setWidgetDependentViews(context, remoteViews, widgetId, textColor);

        //Setup Refresh

        PendingIntent pendingRefreshIntent = PendingIntent.getBroadcast(context, getRefreshIntentID(), getRefreshIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_refresh, pendingRefreshIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.contentList);
        return remoteViews;
    }



    public static void updateWidget(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);

        context.sendBroadcast(intent);

    }

    /**
     * Invalidates the collection list on the widget,
     * and makes it re-pull data from the api.
     *
     * @param context
     */
    private void updateWidgetList(Context context) {
        if(context != null) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    /*
   *Returns simple name for Google Analytics
    */
    public abstract String getWidgetSimpleName();

    /*
    * Returns string for broadcast receivers
     */
    public abstract String getRefreshString();

    protected abstract void setWidgetDependentViews(Context context, RemoteViews remoteViews, int widgetId, int textColor);
    protected abstract int getRefreshIntentID();
    protected abstract Intent getRefreshIntent(Context context);
}
