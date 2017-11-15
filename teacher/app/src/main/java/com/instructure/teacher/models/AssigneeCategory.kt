/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.models

import android.os.Parcel
import android.os.Parcelable.Creator
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.teacher.utils.readBoolean
import com.instructure.teacher.utils.writeBoolean

enum class AssigneeCategory { SECTIONS, GROUPS, STUDENTS }

class EveryoneAssignee(
        val peopleCount: Int,
        val displayAsEveryoneElse: Boolean
) : CanvasComparable<EveryoneAssignee>() {

    constructor(inp: Parcel) : this(
            inp.readInt(),
            inp.readBoolean()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(peopleCount)
        dest?.writeBoolean(displayAsEveryoneElse)
    }

    override fun getId() = -1L
    override fun getComparisonDate() = null
    override fun getComparisonString() = ""

    companion object {
        @JvmStatic
        val CREATOR: Creator<EveryoneAssignee> = object : Creator<EveryoneAssignee> {
            override fun createFromParcel(source: Parcel) = EveryoneAssignee(source)
            override fun newArray(size: Int) = emptyArray<EveryoneAssignee>()
        }
    }

}