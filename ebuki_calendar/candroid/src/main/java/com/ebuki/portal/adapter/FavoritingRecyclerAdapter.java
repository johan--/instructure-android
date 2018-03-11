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
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ebuki.portal.R;
import com.ebuki.portal.binders.CourseBinder;
import com.ebuki.portal.binders.FavoritingBinder;
import com.ebuki.portal.holders.CourseHeaderViewHolder;
import com.ebuki.portal.holders.FavoritingViewHolder;
import com.ebuki.portal.interfaces.AdapterToFragmentCallback;
import com.ebuki.portal.model.CourseToggleHeader;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class FavoritingRecyclerAdapter extends ExpandableRecyclerAdapter<CourseToggleHeader, CanvasContext, RecyclerView.ViewHolder>{

    private AdapterToFragmentCallback<CanvasContext> mAdapterToFragmentCallback;

    public static final int ALL_COURSES_ID = 1111;
    public static final int ALL_GROUPS_ID = 3333;

    private Map<Integer, ArrayList<CanvasContext>> mCallbackSyncHash = new HashMap<>();

    //callbacks
    private StatusCallback<List<Course>> mAllCoursesCallback;
    private StatusCallback<List<Group>> mGroupsCallback;

    public FavoritingRecyclerAdapter(Activity context, AdapterToFragmentCallback<CanvasContext> adapterToFragmentCallback) {
        super(context, CourseToggleHeader.class, CanvasModel.class);
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        setExpandedByDefault(true);
        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        switch (viewType) {
            case Types.TYPE_ITEM:
                return new FavoritingViewHolder(v);
            default:
                return new CourseHeaderViewHolder(v);
        }
    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, CourseToggleHeader header, final CanvasContext canvasContext) {
        if(mCallbackSyncHash.containsKey(ALL_COURSES_ID) && mCallbackSyncHash.containsKey(ALL_GROUPS_ID)) {
            FavoritingBinder.bind(getContext(), canvasContext, (FavoritingViewHolder)holder, mAdapterToFragmentCallback);
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
                if(o1.getName() != null && o2.getName() != null) {
                    return o1.getName().toLowerCase(Locale.getDefault()).compareTo(o2.getName().toLowerCase(Locale.getDefault()));
                } else {
                    return 0;
                }
            }

            @Override
            public boolean areContentsTheSame(CanvasContext oldItem, CanvasContext newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(CanvasContext item1, CanvasContext item2) {
                return item1.getContextId().equals(item2.getContextId());
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
                return FavoritingViewHolder.holderResId();
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
                ArrayList<CanvasContext> list = new ArrayList<CanvasContext>(response.body());
                Collections.sort(list);
                mCallbackSyncHash.put(ALL_COURSES_ID, list);
                syncCallbacks();
            }
        };

        mGroupsCallback = new StatusCallback<List<Group>>() {

            @Override
            public void onResponse(retrofit2.Response<List<Group>> response, LinkHeaders linkHeaders, ApiType type) {
                ArrayList<CanvasContext> list = new ArrayList<CanvasContext>(response.body());
                Collections.sort(list);
                mCallbackSyncHash.put(ALL_GROUPS_ID, list);
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
        if(mCallbackSyncHash.containsKey(ALL_COURSES_ID) && mCallbackSyncHash.containsKey(ALL_GROUPS_ID)) {
            clear();

            addOrUpdateAllItems(makeHeader(R.string.groups, true, ALL_GROUPS_ID), mCallbackSyncHash.get(ALL_GROUPS_ID));
            addOrUpdateAllItems(makeHeader(R.string.courses, true, ALL_COURSES_ID), mCallbackSyncHash.get(ALL_COURSES_ID));

            notifyDataSetChanged();
            setAllPagesLoaded(true);
            if(getItemCount() == 0) {
                getAdapterToRecyclerViewCallback().setIsEmpty(true);
            }
        }
    }

    public CourseToggleHeader makeHeader(int textResId, boolean clickable, int id) {
        CourseToggleHeader header = new CourseToggleHeader();
        header.text = getContext().getResources().getString(textResId);
        header.clickable = clickable;
        header.id = id;
        return header;
    }

    @Override
    public void refresh() {
        mCallbackSyncHash.clear();
        super.refresh();
    }

    private void syncCallbacks() {
        if(!mCallbackSyncHash.containsKey(ALL_COURSES_ID) || !mCallbackSyncHash.containsKey(ALL_GROUPS_ID)) {
            return;
        }

        refreshAdapter();
        mAdapterToFragmentCallback.onRefreshFinished();
    }
}
