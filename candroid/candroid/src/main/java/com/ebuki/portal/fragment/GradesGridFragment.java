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

package com.ebuki.portal.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebuki.portal.R;
import com.ebuki.portal.adapter.CourseGradeRecyclerAdapter;
import com.ebuki.portal.decorations.DividerDecoration;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.holders.CourseViewHolder;
import com.ebuki.portal.interfaces.CourseAdapterToFragmentCallback;
import com.ebuki.portal.util.RouterUtils;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandarecycler.PandaRecyclerView;

public class GradesGridFragment extends OrientationChangeFragment {

    //Views
    private View mRootView;
    private PandaRecyclerView mRecyclerView;
    private CourseGradeRecyclerAdapter mRecyclerAdapter;

    @Override
    public String getFragmentTitle() {
        return getString(R.string.grades);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.MASTER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanvasContext(CanvasContext.emptyUserContext());
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        mRootView = getLayoutInflater().inflate(R.layout.grades_grid_fragment, container, false);
        mRecyclerView = (PandaRecyclerView)mRootView.findViewById(R.id.listView);
        configureRecyclerAdapter();
        configureRecyclerView(mRootView, getContext(), mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView, "");
        mRecyclerView.addItemDecoration(new DividerDecoration(getContext()));
        mRecyclerView.setSelectionEnabled(false);
        return mRootView;
    }

    private void configureRecyclerAdapter(){
        if(mRecyclerAdapter == null) {
            mRecyclerAdapter = new CourseGradeRecyclerAdapter(getActivity(), new CourseAdapterToFragmentCallback() {
                @Override
                public void onRefreshFinished() {
                    setRefreshing(false);
                }

                @Override
                public void onRowClicked(CanvasContext canvasContext) {
                    if (getActivity() instanceof Navigation) {
                        Navigation navigation = getNavigation();
                        if (navigation != null) {
                            String url = constructUrl(canvasContext.getId(), getContext());
                            RouterUtils.routeUrl(getActivity(), url, true);
                        }
                    }
                }

                @Override
                public void setupTutorial(CourseViewHolder holder) {

                }
            });
            configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView, R.string.no_courses_grades);
        }
    }

    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        setupTitle(getString(R.string.grades));
    }

    /*
     *Used to create url for RouterUtils
     */
    private String constructUrl(long id, Context context){
        return "https://" + ApiPrefs.getDomain() + "/courses/" + id + "/grades";
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}
