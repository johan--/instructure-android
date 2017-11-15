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
import android.widget.TextView;

import com.instructure.teacher.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class UserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.studentAvatar) public CircleImageView studentAvatar;
    @BindView(R.id.userName) public TextView userName;
    @BindView(R.id.userRole) public TextView userRole;


    public UserViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static int holderResId() { return R.layout.adapter_users;}
}
