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

import com.instructure.canvasapi2.models.Recipient;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.teacher.holders.RecipientViewHolder;
import com.instructure.teacher.interfaces.RecipientAdapterCallback;

import instructure.androidblueprint.SyncPresenter;
import instructure.androidblueprint.SyncRecyclerAdapter;


public class ChooseMessageRecipientRecyclerAdapter extends SyncRecyclerAdapter<Recipient, RecipientViewHolder> {

    private RecipientAdapterCallback mAdapterCallback;

    public ChooseMessageRecipientRecyclerAdapter(Context context, SyncPresenter presenter, RecipientAdapterCallback callback) {
        super(context, presenter);
        mAdapterCallback = callback;
    }

    @Override
    public void bindHolder(Recipient recipient, RecipientViewHolder holder, int position) {
        holder.bind(getContext(), holder, recipient, mAdapterCallback, ThemePrefs.getBrandColor(), mAdapterCallback.isRecipientSelected(recipient));
    }

    @Override
    public RecipientViewHolder createViewHolder(View v, int viewType) {
        return new RecipientViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return RecipientViewHolder.Companion.getHolderResId();
    }

}
