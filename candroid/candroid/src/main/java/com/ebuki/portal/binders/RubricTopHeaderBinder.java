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

package com.ebuki.portal.binders;

import android.content.Context;

import com.ebuki.portal.R;
import com.ebuki.portal.holders.RubricTopHeaderViewHolder;

public class RubricTopHeaderBinder extends BaseBinder {
    public static void bind(Context context, RubricTopHeaderViewHolder holder, String points, String grade, boolean isMuted) {
        if(isMuted){
            holder.mutedText.setText(context.getString(R.string.mutedText));
        } else {
            holder.gradeText.setText(grade);
            holder.pointsText.setText(points);
        }
        ifHasTextSetVisibleElseGone(holder.gradeText);
        ifHasTextSetVisibleElseGone(holder.pointsText);
        ifHasTextSetVisibleElseGone(holder.mutedText);
    }
}
