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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ebuki.portal.R;
import com.ebuki.portal.adapter.FavoritingRecyclerAdapter;
import com.ebuki.portal.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Favorite;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.utils.ColorUtils;
import com.instructure.pandautils.utils.Const;

import retrofit.client.Response;

import static com.ebuki.portal.adapter.FavoritingRecyclerAdapter.ALL_COURSES_ID;
import static com.ebuki.portal.adapter.FavoritingRecyclerAdapter.ALL_GROUPS_ID;

public class FavoritingFragment extends ParentFragment {

    private View mRootView;
    private Toolbar mToolbar;
    private FavoritingRecyclerAdapter mRecyclerAdapter;
    private PandaRecyclerView mRecyclerView;
    private boolean mCourseFavoriteChanged = false;

    @Override
    public String getFragmentTitle() {
        return getActivity().getString(R.string.selectFavorites);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DIALOG;
    }

    @Override
    public boolean navigationContextIsCourse() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = getLayoutInflater().inflate(R.layout.fragment_favoriting, container, false);
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        setupToolbar();
        mRecyclerAdapter = new FavoritingRecyclerAdapter(getActivity(), new AdapterToFragmentCallback<CanvasContext>() {
            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }

            @Override
            public void onRowClicked(CanvasContext canvasContext, int position, boolean isOpenDetail) {
                mCourseFavoriteChanged = true;
                if(canvasContext instanceof Course) {
                    updateCourseFavorite((Course)canvasContext);
                } else if(canvasContext instanceof Group) {
                    updateGroupFavorite((Group)canvasContext);
                }
            }
        });

        mRecyclerView = (PandaRecyclerView)mRootView.findViewById(R.id.listView);
        configureRecyclerView(mRootView, getContext(), mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView, R.string.no_courses_available);
        mRecyclerView.setSelectionEnabled(false);
        return mRootView;
    }

    private void setupToolbar() {
        mToolbar.setBackgroundColor(getResources().getColor(R.color.canvasBackgroundOffWhite));
        mToolbar.setTitleTextColor(getResources().getColor(R.color.canvasTextDark));
        mToolbar.setSubtitleTextColor(getResources().getColor(R.color.canvasTextDark));
        mToolbar.setTitle(R.string.selectFavorites);
        mToolbar.setSubtitle(R.string.selectFavoritesHint);
        mToolbar.setNavigationIcon(ColorUtils.colorIt(getActivity(), getResources().getColor(R.color.canvasTextDark), R.drawable.ic_content_close));
        mToolbar.setNavigationContentDescription(R.string.toolbar_close);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(Const.ACTIONBAR_ELEVATION);
        }
    }

    private void updateCourseFavorite(final Course course) {
        //Update the favorite to what it should be
        course.setFavorite(!course.isFavorite());
        if (course.isFavorite()) {
            CourseManager.addCourseToFavorites(course.getId(), new StatusCallback<Favorite>() {

                @Override
                public void onResponse(retrofit2.Response<Favorite> response, LinkHeaders linkHeaders, ApiType type) {
                    mRecyclerAdapter.addOrUpdateItem(mRecyclerAdapter.makeHeader(R.string.courses, true, ALL_COURSES_ID), course);
                }

            }, true);
        } else {
            CourseManager.removeCourseFromFavorites(course.getId(), new StatusCallback<Favorite>() {

                @Override
                public void onResponse(retrofit2.Response<Favorite> response, LinkHeaders linkHeaders, ApiType type) {
                    mRecyclerAdapter.addOrUpdateItem(mRecyclerAdapter.makeHeader(R.string.courses, true, ALL_COURSES_ID), course);
                }

            }, true);
        }
    }

    private void updateGroupFavorite(final Group group) {
        group.setFavorite(!group.isFavorite());
        if (group.isFavorite()) {
            GroupManager.addGroupToFavorites(group.getId(), new StatusCallback<Favorite>() {
                @Override
                public void onResponse(retrofit2.Response<Favorite> response, LinkHeaders linkHeaders, ApiType type) {
                    mRecyclerAdapter.addOrUpdateItem(mRecyclerAdapter.makeHeader(R.string.groups, true, ALL_GROUPS_ID), group);
                }

            });
        } else {
            GroupManager.removeGroupFromFavorites(group.getId(), new StatusCallback<Favorite>() {
                @Override
                public void onResponse(retrofit2.Response<Favorite> response, LinkHeaders linkHeaders, ApiType type) {
                    mRecyclerAdapter.addOrUpdateItem(mRecyclerAdapter.makeHeader(R.string.groups, true, ALL_GROUPS_ID), group);
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog != null && !isTablet(getActivity())) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mRecyclerAdapter != null) {
            mRecyclerAdapter.removeCallbacks();
        }
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    private void fireChangedIntent() {
        Intent intent = new Intent(Const.COURSE_THING_CHANGED);
        Bundle extras = new Bundle();
        extras.putBoolean(Const.COURSE_FAVORITES, mCourseFavoriteChanged);
        intent.putExtras(extras);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        fireChangedIntent();
        super.onDismiss(dialog);
    }
}
