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

package com.ebuki.homework.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebuki.homework.R;
import com.ebuki.homework.adapter.PageListRecyclerAdapter;
import com.ebuki.homework.delegate.Navigation;
import com.ebuki.homework.interfaces.AdapterToFragmentCallback;
import com.ebuki.homework.util.FragUtils;
import com.ebuki.homework.util.Param;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Page;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.pandautils.utils.Const;

public class PageListFragment extends ParentFragment {

    private View mRootView;

    private PageListRecyclerAdapter mRecyclerAdapter;
    private String mDefaultSelectedPageTitle = PageListRecyclerAdapter.FRONT_PAGE_DETERMINER; // blank string is used to determine front page
    private boolean mIsShowFrontPage = false;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    public String getTabId() {
        return Tab.PAGES_ID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRootView = getLayoutInflater().inflate(R.layout.course_pages, container, false);
        mRecyclerAdapter = new PageListRecyclerAdapter(getContext(), getCanvasContext(), new AdapterToFragmentCallback<Page>() {
            @Override
            public void onRowClicked(Page page, int position, boolean isOpenDetail) {
                Navigation navigation = getNavigation();
                if (navigation != null){
                    Bundle bundle = PageDetailsFragment.createBundle(page.getUrl(), getCanvasContext());
                    navigation.addFragment(
                            FragUtils.getFrag(PageDetailsFragment.class, bundle));
                }
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }
        }, mDefaultSelectedPageTitle);

        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mIsShowFrontPage) {
            Bundle bundle = PageDetailsFragment.createBundle(Page.FRONT_PAGE_NAME, getCanvasContext());
            Navigation navigation = getNavigation();
            if(navigation != null) {
                ParentFragment fragment = createFragment(PageDetailsFragment.class, bundle);
                navigation.addFragment(fragment, true);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.pages);
    }


    @Override
    protected String getSelectedParamName() {
        return Param.PAGE_ID;
    }

    // region Intent
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if(extras == null){return;}
        mIsShowFrontPage = extras.getBoolean(Const.SHOW_FRONT_PAGE, true);
        if (getUrlParams() != null) {
            mDefaultSelectedPageTitle = getUrlParams().get(getSelectedParamName());
            if  (!getUrlParams().containsKey(getSelectedParamName())) {
                mIsShowFrontPage = false; // case when the url is /pages (only meant to go to the page list)
            }
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, boolean showHomePage, Tab tab) {
        Bundle bundle = createBundle(canvasContext, tab);
        bundle.putBoolean(Const.SHOW_FRONT_PAGE, showHomePage);
        return bundle;
    }
    // endregion


    @Override
    public boolean allowBookmarking() {
        return true;
    }
}
