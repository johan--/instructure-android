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
package com.instructure.teacher.viewinterface

import com.instructure.canvasapi2.models.*
import instructure.androidblueprint.FragmentViewInterface


interface StudentContextView : FragmentViewInterface {
    fun setData(course: Course, sections: List<Section>, student: User, summary: StudentSummary?, isStudent: Boolean)
    fun addSubmissions(submissions: List<Submission>, course: Course, student: User)
    fun showLoadMoreButton(show: Boolean)
    fun onErrorLoading()
}
