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

package com.ebuki.homework.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;

import com.ebuki.homework.R;
import com.ebuki.homework.fragment.NotificationListFragment;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi2.models.StreamItem;

public class NotificationWidgetRouter extends ParentActivity {

    private StreamItem streamItem;
    DialogFragment dialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();

        showProgress();

        if(streamItem != null){
            dialog =  NotificationListFragment.addFragmentForStreamItem(streamItem, (ParentActivity) getContext(), true);
        }

        if (dialog == null) {
            finish();
        }
    }

    protected void handleIntent() {

        Intent intent = getIntent();
        if (intent.hasExtra(Const.STREAM_ITEM)) {
            streamItem = (StreamItem) intent.getParcelableExtra(Const.STREAM_ITEM);
        }
    }

    public static Intent createIntent(Context context, StreamItem streamItem) {
        Intent intent = createIntent(context, NotificationWidgetRouter.class, R.layout.notification_widget_router_empty);
        intent.putExtra(Const.STREAM_ITEM,  (Parcelable)streamItem);


        return intent;
    }

    @Override
    public int contentResId() {
        return 0;
    }

    @Override
    public boolean showHomeAsUp() {
        return false;
    }

    @Override
    public boolean showTitleEnabled() {
        return false;
    }

    @Override
    public void onUpPressed() {

    }
}
