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

package com.ebuki.portal.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ebuki.portal.R;

public class GradeViewHolder extends RecyclerView.ViewHolder {

    public TextView title, date, points;
    public ImageView icon, edit, pendingReviewIcon;

    public GradeViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        date = itemView.findViewById(R.id.date);
        points = itemView.findViewById(R.id.points);
        icon = itemView.findViewById(R.id.icon);
        edit = itemView.findViewById(R.id.edit);
        pendingReviewIcon = itemView.findViewById(R.id.pendingReviewIcon);
    }

    public static int holderResId(){
        return R.layout.viewholder_grade;
    }
}
