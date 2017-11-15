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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.weave.awaitApis
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.viewinterface.QuizListView
import instructure.androidblueprint.SyncExpandablePresenter
import kotlinx.coroutines.experimental.Job

class QuizListPresenter(private val mCanvasContext: CanvasContext) :
    SyncExpandablePresenter<String, Quiz, QuizListView>(String::class.java, Quiz::class.java) {

    var apiCalls: Job? = null

    override fun loadData(forceNetwork: Boolean) {
        if (data.size() > 0 && !forceNetwork) return
        // it's possible for a teacher to change the quiz type and it could create an empty group.
        // This will fix that
        if(forceNetwork) {
            clearData()
        }
        if (apiCalls?.isActive ?: false) {
            apiCalls?.invokeOnCompletion { performLoad(forceNetwork) }
        } else {
            performLoad(forceNetwork)
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun performLoad(forceNetwork: Boolean) {
        apiCalls = weave {
            onRefreshStarted()
            try {
                // Get assignments and quizzes
                val (assignments, quizzes) = awaitApis<List<Assignment>, List<Quiz>>(
                        { AssignmentManager.getAllAssignments(mCanvasContext.id, forceNetwork, it) },
                        { QuizManager.getAllQuizzes(mCanvasContext.id, forceNetwork, it) }
                )

                val assignmentsByQuizId = assignments.filter { it.quizId > 0 }.associateBy { it.quizId }

                // sort quizzes into their different types
                quizzes.onEach { it.assignment = assignmentsByQuizId[it.id] }
                        .groupBy { it.quizType }
                        .forEach { (quizType, quizList) ->
                            data.addOrUpdateAllItems(quizType, quizList)
                        }

            } catch (ignore: Throwable) {
            } finally {
                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            }
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        apiCalls?.cancel()
        clearData()
        loadData(forceNetwork)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        apiCalls?.cancel()
    }

    override fun compare(group: String?, quiz1: Quiz?, quiz2: Quiz?): Int {
        //quizzes sort by due date first, then by title alphabetically
        if(quiz1?.dueAt != null && quiz2?.dueAt != null) {
            return quiz1.dueAt?.compareTo(quiz2.dueAt) ?: -1
        } else if(quiz1?.dueAt == null && quiz2?.dueAt != null) {
            return 1
        } else if(quiz1?.dueAt != null && quiz2?.dueAt == null) {
            return -1
        }

        return quiz1?.title?.compareTo(quiz2?.title ?: "") ?: -1
    }
}
