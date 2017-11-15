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

import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.teacher.viewinterface.PageListView
import instructure.androidblueprint.SyncPresenter
import kotlinx.coroutines.experimental.Job

class PageListPresenter(private val mCanvasContext: CanvasContext) :
        SyncPresenter<Page, PageListView>(Page::class.java) {

    var apiCalls: Job? = null

    override fun loadData(forceNetwork: Boolean) {
        if (data.size() > 0 && !forceNetwork) {
            viewCallback?.let {
                it.onRefreshFinished()
                it.checkIfEmpty()
            }
            return
        }

        if(forceNetwork) {
            clearData()
        }
        apiCalls = weave {
            onRefreshStarted()
            try {
                awaitApi<List<Page>> {
                    PageManager.getAllPages(mCanvasContext, forceNetwork, it)
                }.forEach { data.addOrUpdate(it) }
            } catch (e: StatusCallbackError) {
            } finally {
                viewCallback?.let {
                    it.onRefreshFinished()
                    it.checkIfEmpty()
                }
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
}
