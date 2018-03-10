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

package com.ebuki.portal.adapter;

import android.app.Activity;
import android.view.View;

import com.ebuki.portal.binders.CourseGradeBinder;
import com.ebuki.portal.holders.CourseGradeViewHolder;
import com.ebuki.portal.interfaces.CourseAdapterToFragmentCallback;
import com.ebuki.portal.util.Analytics;
import com.ebuki.portal.util.MGPUtils;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.TabManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class CourseGradeRecyclerAdapter extends BaseListRecyclerAdapter<CanvasContext, CourseGradeViewHolder> {

    private CourseAdapterToFragmentCallback mAdapterToFragmentCallback;
    private StatusCallback<List<Course>> mFavoriteCoursesCallback;
    private Map<CanvasContext, Boolean> mGradesTabVisibilityStatus = new HashMap<>();

    public CourseGradeRecyclerAdapter(Activity context, CourseAdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, CanvasContext.class);
        //Context passed in must be of type Activity
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        loadData();
    }

    @Override
    public CourseGradeViewHolder createViewHolder(View v, int viewType) {
        return new CourseGradeViewHolder(v);
    }

    @Override
    public void bindHolder(CanvasContext canvasContext, CourseGradeViewHolder holder, int position) {
        boolean gradesTabExists = true;
        if(mGradesTabVisibilityStatus.containsKey(canvasContext)) {
            gradesTabExists = mGradesTabVisibilityStatus.get(canvasContext);
        }

        boolean isAllGradingPeriodsShown = MGPUtils.isAllGradingPeriodsShown((Course)canvasContext);
        CourseGradeBinder.bind(getContext(), canvasContext, holder, gradesTabExists, isAllGradingPeriodsShown, mAdapterToFragmentCallback);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return CourseGradeViewHolder.holderResId();
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void setupCallbacks() {
        mFavoriteCoursesCallback = new StatusCallback<List<Course>>() {

            @Override
            public void onResponse(retrofit2.Response<List<Course>> response, LinkHeaders linkHeaders, ApiType type) {
                List<Course> courses = response.body();
                if (size() == 0) {
                    for (final Course course : courses) {
                        TabManager.getTabs(course, new StatusCallback<List<Tab>>() {
                            @Override
                            public void onResponse(retrofit2.Response<List<Tab>> response, LinkHeaders linkHeaders, ApiType type) {
                                boolean gradesTabExists = false;
                                for (Tab tab : response.body()) {
                                    //we need to check if the tab exists and hidden is false
                                    if(Tab.GRADES_ID.equals(tab.getTabId()) && !tab.isHidden()) {
                                        gradesTabExists = true;
                                        break;
                                    }
                                }
                                mGradesTabVisibilityStatus.put(course, gradesTabExists);
                                add(course);
                            }
                        }, false);
                    }
                }

                if(type == ApiType.API) {
                    Analytics.trackEnrollment((Activity)getContext(), courses);
                    Analytics.trackDomain((Activity)getContext());
                    setNextUrl(linkHeaders.nextUrl);
                }
                mAdapterToFragmentCallback.onRefreshFinished();

            }

            @Override
            public void onFail(Call<List<Course>> callResponse, Throwable error, retrofit2.Response response) {
                if (response != null && !APIHelper.isCachedResponse(response) || !APIHelper.hasNetworkConnection()) {
                    getAdapterToRecyclerViewCallback().setIsEmpty(true);
                }
            }
        };
    }

    @Override
    public void loadData() {
        CourseManager.getAllFavoriteCourses(true, mFavoriteCoursesCallback);
    }
}
