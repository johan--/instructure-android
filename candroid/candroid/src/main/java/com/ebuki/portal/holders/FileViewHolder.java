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

package com.instructure.candroid.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.candroid.R;

public class FileViewHolder extends RecyclerView.ViewHolder{

    public TextView fileName, fileDetails;
    public ImageView icon;
    public RelativeLayout rootView;
    public LinearLayout textContainer;

    public FileViewHolder(View itemView) {
        super(itemView);
        fileName = (TextView)itemView.findViewById(R.id.title);
        fileDetails = (TextView)itemView.findViewById(R.id.description);
        icon = (ImageView)itemView.findViewById(R.id.icon);
        rootView = (RelativeLayout)itemView.findViewById(R.id.rootView);
        textContainer = (LinearLayout)itemView.findViewById(R.id.textContainer);
    }

    public static int holderResId(){
        return R.layout.viewholder_file;
    }
}
