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

package com.ebuki.homework.binders;

import android.widget.CompoundButton;

import com.ebuki.homework.holders.NotificationPreferencesViewHolder;
import com.ebuki.homework.interfaces.NotifyChecked;
import com.ebuki.homework.model.NotificationSubCategory;
import com.instructure.canvasapi2.managers.NotificationPreferencesManager;

public class NotificationPreferenceBinder {

    public static void bind(NotificationPreferencesViewHolder holder, final NotificationSubCategory item, final NotifyChecked callback) {
        if(holder == null) {
            return;
        }
        holder.text1.setText(item.title);
        holder.checkbox.setChecked(!item.frequency.equalsIgnoreCase(NotificationPreferencesManager.NEVER));
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isPressed()) return;
                callback.notifyCheckChanged(item, isChecked);
            }
        });
    }
}
