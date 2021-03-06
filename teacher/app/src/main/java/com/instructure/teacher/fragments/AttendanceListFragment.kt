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

package com.instructure.teacher.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.instructure.canvasapi2.apis.AttendanceAPI
import com.instructure.canvasapi2.models.Attendance
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.HttpHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.R
import com.instructure.teacher.adapters.AttendanceListRecyclerAdapter
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.factory.AttendanceListPresenterFactory
import com.instructure.teacher.holders.AttendanceViewHolder
import com.instructure.teacher.interfaces.AttendanceToFragmentCallback
import com.instructure.teacher.presenters.AttendanceListPresenter
import com.instructure.teacher.router.Route
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.viewinterface.AttendanceListView
import instructure.androidblueprint.PresenterFactory
import kotlinx.android.synthetic.main.fragment_attendance_list.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

class AttendanceListFragment : BaseSyncFragment<
        Attendance, AttendanceListPresenter, AttendanceListView, AttendanceViewHolder, AttendanceListRecyclerAdapter>(), AttendanceListView {

    private var mCanvasContext: CanvasContext by ParcelableArg(default = CanvasContext.emptyCourseContext())
    private var mTab: Tab by ParcelableArg(default = Tab.newInstance("", ""))

    lateinit private var mRecyclerView: RecyclerView
    override fun getList() = presenter.data

    override fun layoutResId(): Int {
        return R.layout.fragment_attendance_list
    }

    override fun onCreateView(view: View) {}

    override fun onReadySetGo(presenter: AttendanceListPresenter) {
        mRecyclerView.adapter = adapter
        setupViews()
        presenter.loadData(true)
        themeToolbar()
    }

    private fun setupViews() {
        toolbar.setupMenu(R.menu.menu_attendance) { menuItem ->
            when(menuItem.itemId) {
                R.id.menuFilterSections -> { /* Do Nothing */ }
                R.id.menuCalendar -> {
                    val selectedDate = presenter.getSelectedDate()
                    DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        presenter?.setSelectedDate(calendar)
                        toolbar?.subtitle = DateHelper.getFormattedDate(context, calendar.time)
                    }, selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)).show()
                }
                else -> {
                    //Should be a section chosen
                    presenter?.selectSectionByPosition(menuItem.itemId)
                }
            }
        }

        toolbar.setTitle(R.string.tab_attendance)
        toolbar.subtitle = DateHelper.getFormattedDate(context, presenter.getSelectedDate().time)
        toolbar.setupBackButton(this)

        markRestButton.setBackgroundColor(ThemePrefs.buttonColor)
        markRestButtonText.setTextColor(ThemePrefs.buttonTextColor)
        markRestButton.onClickWithRequireNetwork {
            hideMarkRestButton()
            presenter?.bulkMarkAttendance()
        }
    }

    private fun themeToolbar() {
        if(isTablet) {
            ViewStyler.themeToolbarBottomSheet(activity, isTablet, toolbar, Color.BLACK, false)
            ViewStyler.setToolbarElevationSmall(context, toolbar)
        } else {
            ViewStyler.themeToolbar(activity, toolbar, mCanvasContext.color, Color.WHITE)
        }
    }

    override fun getPresenterFactory(): PresenterFactory<AttendanceListPresenter> {
        return AttendanceListPresenterFactory(mCanvasContext, mTab)
    }

    override fun onPresenterPrepared(presenter: AttendanceListPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(mRootView, context, adapter,
                presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.no_items_to_display_short))
        addSwipeToRefresh(swipeRefreshLayout)
    }

    override fun getAdapter(): AttendanceListRecyclerAdapter {
        if (mAdapter == null) {
            mAdapter = AttendanceListRecyclerAdapter(context, presenter, object : AttendanceToFragmentCallback<Attendance> {
                override fun onRowClicked(attendance: Attendance, position: Int) {
                    presenter?.markAttendance(attendance)
                }

                override fun onAvatarClicked(model: Attendance?, position: Int) {
                    if(model != null && mCanvasContext.id != 0L) {
                        val bundle = StudentContextFragment.makeBundle(model.studentId, mCanvasContext.id, true)
                        RouteMatcher.route(context, Route(null, StudentContextFragment::class.java, mCanvasContext, bundle))
                    }
                }
            })
        }
        return mAdapter
    }

    override fun getRecyclerView(): RecyclerView {
        return mRecyclerView
    }

    override fun withPagination(): Boolean {
        return true
    }

    override fun perPageCount(): Int {
        return ApiPrefs.perPageCount
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
        emptyPandaView.setGone()
        toolbar.menu.findItem(R.id.menuCalendar)?.isEnabled = true
        toolbar.menu.findItem(R.id.menuFilterSections)?.isEnabled = true
        themeToolbar()
    }

    override fun onRefreshStarted() {
        toolbar.menu.findItem(R.id.menuCalendar)?.isEnabled = false
        toolbar.menu.findItem(R.id.menuFilterSections)?.isEnabled = false
        emptyPandaView.setVisible(!swipeRefreshLayout.isRefreshing)
        emptyPandaView.setLoading()
        hideMarkRestButton()
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun launchHiddenAttendanceLTI(url: String) {
        // Tried this headless without adding to the root view but it ended up loading faster when the view exists in the view group.
        CookieManager.getInstance().acceptCookie()
        CookieManager.getInstance().acceptThirdPartyCookies(webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.setWebChromeClient(WebChromeClient())
        webView.setWebViewClient(object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if(url != null) {
                    if (url.contains(AttendanceAPI.BASE_DOMAIN) || url.contains(AttendanceAPI.BASE_TEST_DOMAIN)) {
                        val pattern = "name=\\\\\"csrf-token\\\\\"\\scontent=\\\\\"([^\"]*)\\\\\""
                        view?.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();", { value ->
                            if (value != null) {
                                val matcher = Pattern.compile(pattern).matcher(value)
                                var matchFound = false
                                while (matcher.find()) {
                                    matchFound = true
                                    presenter?.fetchAttendance(matcher.group(1), CookieManager.getInstance().getCookie(url))
                                }
                                if(!matchFound) {
                                    unableToLoad()
                                }
                            }
                        })
                    }
                }
            }
        })
        webView.loadUrl(url)
    }

    override fun launchLTI(tab: Tab) {
        tryWeave {
            onRefreshStarted()
            var ltiUrl: String? = null

            inBackground {
                val result = HttpHelper.externalHttpGet(this@AttendanceListFragment.context, tab.ltiUrl, true).responseBody
                if (result != null) {
                    ltiUrl = JSONObject(result).getString("url")
                }
            }

            //make sure we have a non null url before we add parameters
            if (ltiUrl != null) {
                val uri = Uri.parse(ltiUrl).buildUpon()
                        .appendQueryParameter("display", "borderless")
                        .appendQueryParameter("platform", "android")
                        .build()
                launchHiddenAttendanceLTI(uri.toString())
            }
        } catch {
            Logger.e("ERROR Launching LTI: " + it.message)
            unableToLoad()
        }
    }

    override fun unableToLoad() {
        Toast.makeText(context, R.string.unableToLoadAttendance, Toast.LENGTH_LONG).show()
    }

    override fun notifyAttendanceAsMarked(attendance: Attendance) {
        activity?.runOnUiThread { list.addOrUpdate(attendance) }
    }

    override fun updateMarkAllButton(atLeastOneMarkedPresentLateOrAbsent: Boolean) {
        markRestButtonText.post {
            markRestButtonText.text = if (atLeastOneMarkedPresentLateOrAbsent) getString(R.string.markRemainingAsPresent)
            else getString(R.string.markAllAsPresent)
        }
    }

    override fun updateMarkAllButtonVisibility(visible: Boolean) {
        if(visible) showMarkRestButton() else hideMarkRestButton()
    }

    private fun hideMarkRestButton() {
        markRestButton.post {
            if(markRestButton.visibility == View.VISIBLE) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_down)
                animation.duration = 400
                markRestButton.startAnimation(animation)
            }
            markRestButton.setGone()
        }
    }

    private fun showMarkRestButton() {
        markRestButton.post {
            if(markRestButton.visibility != View.VISIBLE) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
                animation.duration = 400
                markRestButton.startAnimation(animation)
            }
            markRestButton.setVisible()
        }
    }

    override fun addSectionMenu(selectedSection: Section?, sections: List<Section>?) {
        val subMenu = toolbar.menu.findItem(R.id.menuFilterSections)?.subMenu
        subMenu?.clear()
        sections?.forEachIndexed { index, section ->
            subMenu?.add(Menu.NONE, index, Menu.NONE, section.name)
        }
    }

    override fun updateSectionPicked(section: Section?) {
        sectionFilterName.text = section?.name
    }

    companion object {
        @JvmStatic
        fun makeBundle(ltiTab: Tab): Bundle {
            val args = Bundle()
            args.putParcelable(Const.TAB, ltiTab)
            return args
        }

        @JvmStatic
        fun newInstance(canvasContext: CanvasContext, args: Bundle) = AttendanceListFragment().apply {
            mCanvasContext = canvasContext
            mTab = args.getParcelable(Const.TAB)
        }
    }
}
