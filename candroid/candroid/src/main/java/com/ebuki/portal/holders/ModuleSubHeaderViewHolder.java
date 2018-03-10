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
import android.widget.TextView;

import com.ebuki.portal.R;

public class ModuleSubHeaderViewHolder extends RecyclerView.ViewHolder {

    public TextView subTitle;
    public View shadowTop, shadowBottom;

    public ModuleSubHeaderViewHolder(View itemView) {
        super(itemView);
        subTitle = (TextView) itemView.findViewById(R.id.subTitle);
        shadowTop = itemView.findViewById(R.id.shadowTop);
        shadowBottom = itemView.findViewById(R.id.shadowBottom);
    }

    public static int holderResId() {
        return R.layout.viewholder_sub_header_module;
    }
}
