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

package com.instructure.teacher.binders;

import android.content.Context;
import android.view.View;

import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Message;
import com.instructure.teacher.R;
import com.instructure.teacher.holders.MessageHolder;
import com.instructure.teacher.interfaces.MessageAdapterCallback;


public class ReplyMessageBinder extends BaseBinder {

    public static void bind(Context context, final Message message, Conversation conversation, final MessageHolder holder, int position, final MessageAdapterCallback callback) {

        MessageBinder.INSTANCE.bind(context, message, conversation, callback.getParticipantById(message.getAuthorId()), holder, position, callback);

        // Hide attachments
        holder.attachmentContainer.setVisibility(View.GONE);

        // Set up remove button
        holder.messageOptions.setImageResource(R.drawable.vd_close);
        holder.messageOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onMessageAction(MessageAdapterCallback.MessageClickAction.DELETE, message);
            }
        });

    }
}
