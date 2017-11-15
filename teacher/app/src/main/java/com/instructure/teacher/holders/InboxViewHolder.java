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

package com.instructure.teacher.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.teacher.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class InboxViewHolder extends RecyclerView.ViewHolder {

    public TextView subject, message, userName, date;
    public ImageView star, attachment, unreadMark;
    public CircleImageView avatar1, avatar2;

    public InboxViewHolder(View itemView) {
        super(itemView);
        subject = (TextView) itemView.findViewById(R.id.subject);
        message = (TextView) itemView.findViewById(R.id.message);
        userName = (TextView) itemView.findViewById(R.id.userName);
        date = (TextView) itemView.findViewById(R.id.date);
        star = (ImageView) itemView.findViewById(R.id.star);
        attachment = (ImageView) itemView.findViewById(R.id.attachment);
        avatar1 = (CircleImageView) itemView.findViewById(R.id.avatar1);
        avatar2 = (CircleImageView) itemView.findViewById(R.id.avatar2);
        unreadMark = (ImageView) itemView.findViewById(R.id.unreadMark);
    }

    public static int holderResId() { return R.layout.adapter_inbox; }
}
