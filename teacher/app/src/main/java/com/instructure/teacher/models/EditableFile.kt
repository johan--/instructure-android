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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.License
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class EditableFile(
    var file : FileFolder,
    var usageRights : Boolean = false,
    var licenses : List<License> = arrayListOf(),
    var courseColor : Int = 0,
    var canvasContext : CanvasContext? = null,
    var iconRes : Int = 0
) : PaperParcelable {
    companion object {
        @Suppress("unresolved_reference")
        @JvmField
        val CREATOR = PaperParcelEditableFile.CREATOR
    }
}