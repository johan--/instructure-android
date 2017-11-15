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
import android.support.annotation.NonNull;
import android.view.View;

import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Message;
import com.instructure.teacher.R;
import com.instructure.teacher.binders.MessageBinder;
import com.instructure.teacher.holders.MessageHolder;
import com.instructure.teacher.interfaces.MessageAdapterCallback;

import instructure.androidblueprint.SyncPresenter;
import instructure.androidblueprint.SyncRecyclerAdapter;


public class MessageAdapter extends SyncRecyclerAdapter<Message, MessageHolder> {

    protected MessageAdapterCallback mCallback;
    protected Conversation mConversation;

    public MessageAdapter(Context context, SyncPresenter presenter, Conversation conversation, @NonNull MessageAdapterCallback callback) {
        super(context, presenter);
        this.mCallback = callback;
        mConversation = conversation;
    }

    @Override
    public void bindHolder(Message message, MessageHolder holder, int position) {
        MessageBinder.INSTANCE.bind(getContext(), message, mConversation, mCallback.getParticipantById(message.getAuthorId()), holder, position, mCallback);
    }

    @Override
    public MessageHolder createViewHolder(View v, int viewType) {
        return new MessageHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return R.layout.adapter_message;
    }

}
