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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ebuki.portal.R;
import com.ebuki.portal.binders.CourseBinder;
import com.ebuki.portal.holders.CourseHeaderViewHolder;
import com.ebuki.portal.holders.CourseViewHolder;
import com.ebuki.portal.interfaces.CourseAdapterToFragmentCallback;
import com.ebuki.portal.model.CourseToggleHeader;
import com.ebuki.portal.util.Analytics;
import com.ebuki.portal.util.ApplicationManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.CanvasModel;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CourseRecyclerAdapter extends ExpandableRecyclerAdapter<CourseToggleHeader, CanvasContext, RecyclerView.ViewHolder>{

    private CourseAdapterToFragmentCallback mAdapterToFragmentCallback;

    public static final int ALL_COURSES_ID = 1111;
    public static final int FAV_COURSES_ID = 2222;
    private static final int ALL_GROUPS_ID = 3333;
    private static final int FAV_GROUPS_ID = 4444;

    private Map<Integer, ArrayList<CanvasContext>> mCallbackSyncHash = new HashMap<>();
    private Map<CanvasContext, Boolean> mGradesTabHidden = new HashMap<>();

    private CourseToggleHeader mGroupHeader;
    private CourseToggleHeader mCourseHeader;

    //callbacks
    private StatusCallback<List<Course>> mAllCoursesCallback;
    private StatusCallback<List<Group>> mGroupsCallback;
    private ArrayList<Course> mCourseList = new ArrayList<>();

    private boolean mShowGrades = false;

    public CourseRecyclerAdapter(Activity context, CourseAdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, CourseToggleHeader.class, CanvasModel.class);
        mGroupHeader = makeHeader(R.string.groups, false, FAV_GROUPS_ID);
        mCourseHeader = makeHeader(R.string.courses, true, FAV_COURSES_ID);
        mShowGrades = ApplicationManager.getPrefs(context).load(Const.SHOW_GRADES_ON_CARD, true);
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        setExpandedByDefault(true);
        loadData();
    }

    public CourseToggleHeader getItemGroupHeader(@Nullable CanvasContext canvasContext) {
        if(canvasContext instanceof Course) {
            return mCourseHeader;
        } else if(canvasContext instanceof Group) {
            return mGroupHeader;
        }
        return null;
    }

    public void setShowGrades(boolean showGrades) {
        mShowGrades = showGrades;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        switch (viewType) {
            case Types.TYPE_ITEM:
                return new CourseViewHolder(v);
            default:
                return new CourseHeaderViewHolder(v);
        }
    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, CourseToggleHeader header, final CanvasContext canvasContext) {
        if(allDataAvailable()) {
            boolean gradesTabHidden = false;
            if(mGradesTabHidden.containsKey(canvasContext)) {
                //TODO when we get an include=tabs option in courses/groups use this to determine if the grade should be clickable to route users.
                gradesTabHidden = mGradesTabHidden.get(canvasContext);
            }
            CourseBinder.bind((Activity)getContext(), canvasContext, (CourseViewHolder) holder, mCallbackSyncHash.get(ALL_COURSES_ID), mShowGrades, gradesTabHidden, mAdapterToFragmentCallback);
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, CourseToggleHeader header, boolean isExpanded) {
        CourseBinder.bindHeader(header, (CourseHeaderViewHolder) holder);
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<CourseToggleHeader, CanvasContext> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<CourseToggleHeader, CanvasContext>() {
            @Override
            public int compare(CourseToggleHeader group, CanvasContext o1, CanvasContext o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(CanvasContext oldItem, CanvasContext newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(CanvasContext item1, CanvasContext item2) {
                return item1.getContextId().hashCode() == item2.getContextId().hashCode();
            }

            @Override
            public long getUniqueItemId(CanvasContext item) {
                return item.getContextId().hashCode();
            }

            @Override
            public int getChildType(CourseToggleHeader group, CanvasContext item) {
                return Types.TYPE_ITEM;
            }
        };
    }

    @Override
    public GroupSortedList.GroupComparatorCallback<CourseToggleHeader> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<CourseToggleHeader>() {
            @Override
            public int compare(CourseToggleHeader o1, CourseToggleHeader o2) {
                return Long.valueOf(o1.getId()).compareTo(o2.getId());
            }

            @Override
            public boolean areContentsTheSame(CourseToggleHeader oldGroup, CourseToggleHeader newGroup) {
                return oldGroup.equals(newGroup);
            }

            @Override
            public boolean areItemsTheSame(CourseToggleHeader group1, CourseToggleHeader group2) {
                return group1.equals(group2);
            }

            @Override
            public long getUniqueGroupId(CourseToggleHeader group) {
                return group.hashCode();
            }

            @Override
            public int getGroupType(CourseToggleHeader group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    @Override
    public void loadData() {
        CourseManager.getCourses(true, mAllCoursesCallback);
        GroupManager.getAllGroups(mGroupsCallback, true);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        switch (viewType) {
            case Types.TYPE_ITEM:
                return CourseViewHolder.holderResId();
            default:
                return CourseHeaderViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {}

    @Override
    public void setupCallbacks() {
        mAllCoursesCallback = new StatusCallback<List<Course>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Course>> response, LinkHeaders linkHeaders, ApiType type) {
                ArrayList<Course> courses = new ArrayList<>(response.body());
                mCallbackSyncHash.put(ALL_COURSES_ID, new ArrayList<CanvasContext>(courses));
                mCallbackSyncHash.put(FAV_COURSES_ID, getFavoritesFromAllCourses(courses));
                syncCallbacks();
                if (type.isAPI()) {
                    Analytics.trackDomain((Activity) getContext());
                    Analytics.trackEnrollment((Activity) getContext(), courses);
                }
            }
        };

        mGroupsCallback = new StatusCallback<List<Group>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Group>> response, LinkHeaders linkHeaders, ApiType type) {
                mCallbackSyncHash.put(ALL_GROUPS_ID, new ArrayList<CanvasContext>(response.body()));
                mCallbackSyncHash.put(FAV_GROUPS_ID, getFavoritesFromAllGroups(response.body()));
                syncCallbacks();
            }
        };
    }

    public void removeCallbacks() {
        if(mGroupsCallback != null) {
            mGroupsCallback.cancel();
        }
        if(mAllCoursesCallback != null) {
            mAllCoursesCallback.cancel();
        }
    }

    public void refreshAdapter() {
        if(allDataAvailable()) {
            clear();
            addOrUpdateAllItems(mGroupHeader, mCallbackSyncHash.get(FAV_GROUPS_ID));
            addOrUpdateAllItems(mCourseHeader, mCallbackSyncHash.get(FAV_COURSES_ID));

            notifyDataSetChanged();
            setAllPagesLoaded(true);
            if(getItemCount() == 0) {
                getAdapterToRecyclerViewCallback().setIsEmpty(true);
            }
        }
    }

    private CourseToggleHeader makeHeader(int textResId, boolean clickable, int id) {
        CourseToggleHeader header = new CourseToggleHeader();
        header.text = getContext().getResources().getString(textResId);
        header.clickable = clickable;
        header.id = id;
        return header;
    }

    @Override
    public void refresh() {
        mCallbackSyncHash.clear();
        setupCallbacks();
        super.refresh();
    }

    private void syncCallbacks() {
        if(!allDataAvailable()) {
            return;
        }

        refreshAdapter();
        mAdapterToFragmentCallback.onRefreshFinished();
    }

    private ArrayList<CanvasContext> getFavoritesFromAllCourses(List<Course> courses) {
        ArrayList<CanvasContext> favs = new ArrayList<>();
        for(CanvasContext canvasContext : courses) {
            if(((Course)canvasContext).isFavorite()) {
                favs.add(canvasContext);
                mCourseList.add((Course)canvasContext);
            }
        }
        return favs;
    }

    private ArrayList<CanvasContext> getFavoritesFromAllGroups(List<Group> groups) {
        ArrayList<CanvasContext> favs = new ArrayList<>();
        for(CanvasContext canvasContext : groups) {
            if(((Group)canvasContext).isFavorite()) {
                favs.add(canvasContext);
            }
        }
        return favs;
    }

    private boolean allDataAvailable() {
        return mCallbackSyncHash.containsKey(ALL_COURSES_ID) && mCallbackSyncHash.containsKey(ALL_GROUPS_ID);
    }
}
