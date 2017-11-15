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
package com.instructure.teacher.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.weave.inParallel
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.R
import com.instructure.teacher.adapters.CanvasContextDialogAdapter
import com.instructure.pandautils.utils.dismissExisting
import kotlinx.coroutines.experimental.Job
import kotlin.properties.Delegates

class CanvasContextListDialog : AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var recyclerView: RecyclerView? = null
    private var mSelectedCallback: (canvasContext: CanvasContext) -> Unit by Delegates.notNull()
    companion object {

        @JvmStatic
        fun getInstance(manager: FragmentManager, callback: (canvasContext: CanvasContext) -> Unit) : CanvasContextListDialog {
            manager.dismissExisting<CanvasContextListDialog>()
            val dialog = CanvasContextListDialog()
            val args = Bundle()
            dialog.arguments = args
            dialog.mSelectedCallback = callback
            return dialog
        }
    }


    fun updateCanvasContexts(courses: ArrayList<Course>, groups: ArrayList<Group>) {
        recyclerView?.adapter = CanvasContextDialogAdapter(this, getCanvasContextList(context, courses, groups), mSelectedCallback)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(ContextThemeWrapper(activity, 0), R.layout.dialog_canvas_context_list, null)
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)


        val dialog = AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle(activity.getString(R.string.selectCanvasContext))
                .setView(view)
                .create()

        dialog.setOnShowListener { loadData(false) }
        return dialog
    }

    private var mApiCalls: Job? = null

    fun loadData(forceNetwork: Boolean) {
        mApiCalls = weave {

            try {
                var courses: ArrayList<Course> = ArrayList()
                var groups: ArrayList<Group> = ArrayList()
                inParallel {
                    // Get Courses
                    await<List<Course>>({ CourseManager.getCourses(forceNetwork, it) }) {
                        courses = it as ArrayList<Course>
                    }

                    // Get groups
                    await<List<Group>>({ GroupManager.getFavoriteGroups(it, forceNetwork) }) {
                        groups = it as ArrayList<Group>
                    }
                }

                updateCanvasContexts(courses, groups)
            } catch (ignore: Exception) {
                if (activity != null) {
                    Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getCanvasContextList(context: Context, courses: List<Course>, groups: List<Group>): ArrayList<CanvasContext> {
        val canvasContexts = ArrayList<CanvasContext>()

        if(courses.isNotEmpty()) {
            val courseSeparator = Course()
            courseSeparator.name = context.getString(R.string.courses)
            courseSeparator.id = -1
            canvasContexts.add(courseSeparator)

            canvasContexts.addAll(courses)
        }

        if(groups.isNotEmpty()) {
            val groupSeparator = Course()
            groupSeparator.name = context.getString(R.string.assignee_type_groups)
            groupSeparator.id = -1
            canvasContexts.add(groupSeparator)

            canvasContexts.addAll(groups)
        }

        return canvasContexts
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        mApiCalls?.cancel()
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
