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

import com.instructure.canvasapi2.models.User;
import com.instructure.teacher.binders.UserBinder;
import com.instructure.teacher.holders.UserViewHolder;
import com.instructure.teacher.interfaces.AdapterToFragmentCallback;

import instructure.androidblueprint.SyncPresenter;
import instructure.androidblueprint.SyncRecyclerAdapter;


public class PeopleListRecyclerAdapter extends SyncRecyclerAdapter<User, UserViewHolder> {

    private AdapterToFragmentCallback<User> mCallback;

    public PeopleListRecyclerAdapter(Context context, SyncPresenter presenter, AdapterToFragmentCallback<User> callback) {
        super(context, presenter);
        mCallback = callback;
    }

    @Override
    public void bindHolder(User user, UserViewHolder holder, int position) {
        UserBinder.bind(getContext(), user, mCallback, holder, position);
    }

    @Override
    public UserViewHolder createViewHolder(View v, int viewType) {
        return new UserViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return UserViewHolder.holderResId();
    }
}
