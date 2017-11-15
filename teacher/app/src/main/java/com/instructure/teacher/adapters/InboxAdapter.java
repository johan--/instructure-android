/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.adapters;

import android.content.Context;
import android.view.View;

import com.instructure.canvasapi2.models.Conversation;
import com.instructure.teacher.binders.InboxBinder;
import com.instructure.teacher.holders.InboxViewHolder;
import com.instructure.teacher.interfaces.AdapterToFragmentCallback;
import com.instructure.teacher.presenters.InboxPresenter;

import instructure.androidblueprint.SyncRecyclerAdapter;


public class InboxAdapter extends SyncRecyclerAdapter<Conversation, InboxViewHolder> {

    private AdapterToFragmentCallback<Conversation> mCallback;

    public InboxAdapter(Context context, InboxPresenter presenter, AdapterToFragmentCallback<Conversation> callback) {
        super(context, presenter);
        mCallback = callback;
    }

    @Override
    public void bindHolder(Conversation conversation, InboxViewHolder holder, int position) {
        InboxBinder.bind(getContext(), conversation, holder, mCallback);
    }

    @Override
    public InboxViewHolder createViewHolder(View v, int viewType) {
        return new InboxViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return InboxViewHolder.holderResId();
    }
}
