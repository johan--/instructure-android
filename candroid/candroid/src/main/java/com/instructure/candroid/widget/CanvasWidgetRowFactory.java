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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.LoginActivity;
import com.instructure.candroid.util.ApplicationManager;

import java.util.List;

public abstract class CanvasWidgetRowFactory <I> implements RemoteViewsService.RemoteViewsFactory{

    public final static String WIDGET_ERROR = "Canvas-Widget";
    protected Context mContext;
    protected boolean mIsLoggedIn;
    private List<I> mData;

    @Override
    public void onCreate() {}

    @Override
    public void onDestroy() {}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public void onDataSetChanged() {

        mIsLoggedIn = ((ApplicationManager)mContext.getApplicationContext()).isUserLoggedIn();
        if (!mIsLoggedIn || !isOnline()) {
            return;
        }

        List<I> apiResponse = makeApiCalls();

        if(apiResponse == null){
            Log.w(WIDGET_ERROR, "data is null");
            return;
        }

        if(apiResponse.size() == 0){
            Log.w(WIDGET_ERROR, "data is empty");
        }

        mData = apiResponse;
    }

    @Override
    public int getCount() {
        if (!mIsLoggedIn) {
            return 1;
        } else  if(mData == null){
            return  0;

        } else if(mData.size() == 0){
            return 1;
        } else {
            return mData.size();
        }
    }
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(mContext.getPackageName(), getLayoutId());

        if (setViewLoginVisibility(row)) {
            return row;
        }

        if(mData != null) {
            if (mData.size() == 0) {
                setEmptyViewText(row, R.string.noItemsToDisplayShort);
                return row;
            }

            if (mData.get(position) != null) {
                setViewData(mData.get(position), row);
            }
        }

        return row;
    }

    public boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    protected boolean setViewLoginVisibility(RemoteViews row) {
        if (!mIsLoggedIn) {
            //clear out any data that is currently there
            clearViewData(row);

            setEmptyViewText(row, R.string.notLoggedIn);
            //create log in intent
            Intent intent = LoginActivity.Companion.createIntent(mContext);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, CanvasWidgetProvider.cycleBit++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            row.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
            return true;
        } else {
            row.setViewVisibility(R.id.is_not_logged_in, View.GONE);
            return false;
        }
    }

    private void setEmptyViewText(RemoteViews row, int textResId) {
        final int textColor = BaseRemoteViewsService.getWidgetTextColor(giveMeAppWidgetId(), mContext);
        row.setViewVisibility(R.id.is_not_logged_in, View.VISIBLE);
        row.setTextColor(R.id.is_not_logged_in, textColor);
        row.setTextViewText(R.id.is_not_logged_in, mContext.getString(textResId));
    }

    protected abstract List<I> makeApiCalls();
    protected abstract int getLayoutId();
    protected abstract void setViewData(I objectAtPosition, RemoteViews row);
    protected abstract Intent createIntent(I objectAtPosition);
    protected abstract int giveMeAppWidgetId();
    protected abstract void clearViewData(RemoteViews row);
}
