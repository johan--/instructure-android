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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebuki.homework.R;
import com.ebuki.homework.adapter.PeopleListRecyclerAdapter;
import com.ebuki.homework.delegate.Navigation;
import com.ebuki.homework.interfaces.AdapterToFragmentCallback;
import com.ebuki.homework.util.FragUtils;
import com.ebuki.homework.util.Param;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.canvasapi2.models.User;

public class PeopleListFragment extends ParentFragment {

    private View mRootView;
    private PeopleListRecyclerAdapter mRecyclerAdapter;
    private AdapterToFragmentCallback<User> mAdapterToFragmentCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.coursePeople);
    }

    @Override
    protected String getSelectedParamName() {
        return Param.USER_ID;
    }

    public String getTabId() {
        return Tab.PEOPLE_ID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mAdapterToFragmentCallback = new AdapterToFragmentCallback<User>() {
            @Override
            public void onRowClicked(User user, int position, boolean isOpenDetail) {
                Navigation navigation = getNavigation();
                if(navigation != null) {
                    navigation.addFragment(
                            FragUtils.getFrag(PeopleDetailsFragment.class, PeopleDetailsFragment.createBundle(user, getCanvasContext())));
                }
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }
        };

        mRootView = getLayoutInflater().inflate(R.layout.course_people, container, false);
        CardView cardView = (CardView)mRootView.findViewById(R.id.cardView);
        if(cardView != null) {
            cardView.setCardBackgroundColor(Color.WHITE);
        }
        mRecyclerAdapter = new PeopleListRecyclerAdapter(getContext(), getCanvasContext(), mAdapterToFragmentCallback);
        configureRecyclerView(mRootView, getContext(), mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);

        return mRootView;
    }

    @Override
    public boolean allowBookmarking() {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mRecyclerAdapter != null) mRecyclerAdapter.cancel();
    }
}
