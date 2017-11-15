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

package com.instructure.teacher.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.teacher.holders.AssigneeItemViewHolder
import com.instructure.teacher.holders.AssigneeTypeViewHolder
import com.instructure.teacher.holders.AssigneeViewHolder
import com.instructure.teacher.models.AssigneeCategory
import com.instructure.teacher.presenters.AssigneeListPresenter
import instructure.androidblueprint.SyncExpandableRecyclerAdapter

class AssigneeListAdapter(context: Context, val presenter: AssigneeListPresenter) : SyncExpandableRecyclerAdapter<AssigneeCategory, CanvasComparable<*>, AssigneeViewHolder>(context, presenter) {
    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: AssigneeCategory, isExpanded: Boolean) {
        (holder as? AssigneeTypeViewHolder)?.bind(group)
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: AssigneeCategory, item: CanvasComparable<*>) {
        (holder as? AssigneeItemViewHolder)?.bind(item, presenter, ThemePrefs.brandColor)
    }

    override fun itemLayoutResId(viewType: Int)
            = if (viewType == Types.TYPE_ITEM) AssigneeItemViewHolder.holderResId else AssigneeTypeViewHolder.holderResId

    override fun createViewHolder(v: View, viewType: Int)
            = if (viewType == Types.TYPE_ITEM) AssigneeItemViewHolder(v) else AssigneeTypeViewHolder(v)
}
