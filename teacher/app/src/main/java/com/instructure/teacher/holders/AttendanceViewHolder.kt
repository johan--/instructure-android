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

package com.instructure.teacher.holders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.instructure.canvasapi2.models.Attendance
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.pandautils.utils.onClick
import com.instructure.teacher.R
import com.instructure.teacher.interfaces.AttendanceToFragmentCallback
import com.instructure.teacher.utils.ProfileUtils
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import de.hdodenhof.circleimageview.CircleImageView

class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var studentAvatar: CircleImageView? = null
    var userName: TextView? = null
    var attendanceIndicator: ImageView? = null

    init {
        studentAvatar = itemView.findViewById<CircleImageView>(R.id.studentAvatar)
        userName = itemView.findViewById<TextView>(R.id.userName)
        attendanceIndicator = itemView.findViewById<ImageView>(R.id.attendanceIndicator)
    }

    fun bind(context: Context, attendance: Attendance, callback: AttendanceToFragmentCallback<Attendance>, holder: AttendanceViewHolder, position: Int) {
        // Set student avatar
        val basicUser = BasicUser()
        basicUser.name = attendance.student?.name
        basicUser.avatarUrl = attendance.student?.avatarUrl
        ProfileUtils.loadAvatarForUser(context, holder.studentAvatar, basicUser)

        // Set student name
        holder.userName?.text = attendance.student?.name

        holder.itemView.onClickWithRequireNetwork { callback.onRowClicked(attendance, position) }
        holder.studentAvatar?.onClick { callback.onAvatarClicked(attendance, position) }

        when(attendance.attendanceStatus()) {
            Attendance.Attendance.ABSENT -> holder.attendanceIndicator?.setImageResource(R.drawable.vd_attendance_missing)
            Attendance.Attendance.LATE -> holder.attendanceIndicator?.setImageResource(R.drawable.vd_attendance_late)
            Attendance.Attendance.PRESENT -> holder.attendanceIndicator?.setImageResource(R.drawable.vd_attendance_present)
            else -> holder.attendanceIndicator?.setImageResource(R.drawable.vd_attendance_unmarked)
        }
    }

    companion object {
        val holderResId = R.layout.adapter_attendance
    }
}
