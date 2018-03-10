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

package com.instructure.candroid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.binders.ExpandableHeaderBinder;
import com.instructure.candroid.binders.NotificationBinder;
import com.instructure.candroid.fragment.NotificationListFragment;
import com.instructure.candroid.holders.ExpandableViewHolder;
import com.instructure.candroid.holders.NotificationViewHolder;
import com.instructure.candroid.interfaces.NotificationAdapterToFragmentCallback;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.ConversationManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.managers.StreamManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.HiddenStreamItem;
import com.instructure.canvasapi2.models.StreamItem;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import retrofit2.Call;


public class NotificationListRecyclerAdapter extends ExpandableRecyclerAdapter<Date, StreamItem, RecyclerView.ViewHolder> {

    private NotificationAdapterToFragmentCallback<StreamItem> mAdapterToFragmentCallback;
    private NotificationListFragment.OnNotificationCountInvalidated mOnNotificationCountInvalidated;
    private NotificationCheckboxCallback mNotificationCheckboxCallback;

    private List<StreamItem> mStreamItems;
    private Map<Long, Course> mCourseMap;
    private Map<Long, Group> mGroupMap;

    private StatusCallback<List<StreamItem>> mStreamCallback;
    private StatusCallback<List<Course>> mCoursesCallback;
    private StatusCallback<List<Group>> mGroupsCallback;
    private CanvasContext mCanvasContext;

    private HashSet<StreamItem> mCheckedStreamItems = new HashSet<>();
    private HashSet<StreamItem> mDeletedStreamItems = new HashSet<>();

    private boolean mIsEditMode;
    private boolean mIsNoNetwork; // With multiple callbacks, some could fail while others don't. This manages when to display no connection when offline

    // region Interfaces
    public interface NotificationCheckboxCallback {
        void onCheckChanged(StreamItem streamItem, boolean isChecked, int position);
        boolean isEditMode();
    }

    // endregion

    public NotificationListRecyclerAdapter(
            Context context,
            CanvasContext canvasContext,
            NotificationListFragment.OnNotificationCountInvalidated onNotificationCountInvalidated,
            NotificationAdapterToFragmentCallback<StreamItem> adapterToFragmentCallback) {
        super(context, Date.class, StreamItem.class);
        mCanvasContext = canvasContext;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mOnNotificationCountInvalidated = onNotificationCountInvalidated;
        mIsEditMode = false;
        setExpandedByDefault(true);
        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else {
            return new NotificationViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else {
            return NotificationViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, Date date, StreamItem streamItem) {
        NotificationBinder.bind(getContext(), (NotificationViewHolder) holder, streamItem, mNotificationCheckboxCallback, mAdapterToFragmentCallback);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, Date date, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, date, DateHelper.getFormattedDate(getContext(), date), isExpanded, getViewHolderHeaderClicked());
    }

    // region Pagination

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void loadFirstPage() {
        CourseManager.getCourses(true, mCoursesCallback);
        GroupManager.getAllGroups(mGroupsCallback, true);

        if (mCanvasContext.getType() == CanvasContext.Type.USER) {
            StreamManager.getUserStream(mStreamCallback, true);
        } else {
            StreamManager.getCourseStream(mCanvasContext, mStreamCallback, true);
        }
    }

    @Override
    public void loadNextPage(String nextURL) {
        if (mCanvasContext.getType() == CanvasContext.Type.USER) {
            StreamManager.getUserStream(mStreamCallback, true);
        } else {
            StreamManager.getCourseStream(mCanvasContext, mStreamCallback, true);
        }
    }

    @Override
    public void setupCallbacks() {
        mNotificationCheckboxCallback = new NotificationCheckboxCallback() {
            @Override
            public void onCheckChanged(StreamItem streamItem, boolean isChecked, int position) {
                streamItem.setChecked(isChecked);
                if (isChecked && !mDeletedStreamItems.contains(streamItem)) {
                    mCheckedStreamItems.add(streamItem);
                } else {
                    mCheckedStreamItems.remove(streamItem);
                }

                //If we aren't in the edit mode, enable edit mode for future clicks
                if(!mIsEditMode){
                    mIsEditMode = true;
                } else if (mCheckedStreamItems.size() == 0){ //if this was the last item, cancel
                    mIsEditMode = false;
                }

                mAdapterToFragmentCallback.onShowEditView(mCheckedStreamItems.size() > 0);
                notifyItemChanged(position);
            }

            @Override
            public boolean isEditMode() {
                return mIsEditMode;
            }
        };


        mCoursesCallback = new StatusCallback<List<Course>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Course>> response, LinkHeaders linkHeaders, ApiType type) {
                mCourseMap = CourseManager.createCourseMap(response.body());
                populateActivityStreamAdapter();
            }
        };

        mGroupsCallback = new StatusCallback<List<Group>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Group>> response, LinkHeaders linkHeaders, ApiType type) {
                mGroupMap = GroupManager.createGroupMap(response.body());
                populateActivityStreamAdapter();
            }
        };

        mStreamCallback = new StatusCallback<List<StreamItem>>() {

            private void checkPreviouslyCheckedItems(List<StreamItem> items) {
                for (StreamItem item : items) {
                    if (mCheckedStreamItems.contains(item)) {
                        // update it do the actual item (the right object reference)
                        mCheckedStreamItems.remove(item);
                        mCheckedStreamItems.add(item);

                        item.setChecked(true);
                    }
                }
            }

            @Override
            public void onResponse(retrofit2.Response<List<StreamItem>> response, LinkHeaders linkHeaders, ApiType type) {
                List<StreamItem> streamItems = response.body();

                checkPreviouslyCheckedItems(streamItems);
                mStreamItems = streamItems;
                populateActivityStreamAdapter();
                mAdapterToFragmentCallback.onRefreshFinished();

                //Clear out the cached deleted items.
                mDeletedStreamItems.clear();
                setNextUrl(linkHeaders.nextUrl);
            }

            @Override
            public void onFail(Call<List<StreamItem>> callResponse, Throwable error, retrofit2.Response response) {
                if (!APIHelper.hasNetworkConnection()) {
                    NotificationListRecyclerAdapter.this.onNoNetwork();
                } else {
                    getAdapterToRecyclerViewCallback().setIsEmpty(true);
                }
            }

            @Override
            public void onFinished(ApiType type) {
                NotificationListRecyclerAdapter.this.onCallbackFinished(type);
            }
        };
    }
    // endregion

    // region Data

    public void confirmButtonClicked() {
        for (StreamItem streamItem : mCheckedStreamItems) {
            hideStreamItem(streamItem);
            mDeletedStreamItems.add(streamItem);
        }
        mIsEditMode = false;
        clearMarked();
    }

    public void cancelButtonClicked() {
        for (StreamItem streamItem : mCheckedStreamItems) {
            streamItem.setChecked(false);
        }
        mIsEditMode = false;
        clearMarked();
        notifyDataSetChanged();
    }

    public void clearMarked() {
        mCheckedStreamItems.clear();
        mAdapterToFragmentCallback.onShowEditView(mCheckedStreamItems.size() > 0);
    }

    @Override
    public void onNoNetwork() {
        super.onNoNetwork();
        mIsNoNetwork = true;
    }

    @Override
    public void onCallbackFinished(ApiType type) {
        // Workaround for the multiple callbacks, some will succeed while others don't
        setLoadedFirstPage(true);
        shouldShowLoadingFooter();
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            if (!mIsNoNetwork) { // double negative, only happens when there is network
                adapterToRecyclerViewCallback.setDisplayNoConnection(false);
                getAdapterToRecyclerViewCallback().setIsEmpty(isAllPagesLoaded() && size() == 0);
            }
        }
    }

    @Override
    public void refresh() {
        mIsNoNetwork = false;
        getAdapterToRecyclerViewCallback().setDisplayNoConnection(false);
        mStreamCallback.reset();
        super.refresh();
    }

    public void populateActivityStreamAdapter() {
        if (mIsNoNetwork) { // workaround for the multiple callbacks, which mess up the generic solution
            getAdapterToRecyclerViewCallback().setDisplayNoConnection(true);
            getAdapterToRecyclerViewCallback().setIsEmpty(size() == 0);
        }

        // wait until all calls return;
        if (mCourseMap == null || mGroupMap == null || mStreamItems == null) {
            return;
        }

        for (final StreamItem streamItem : mStreamItems) {
            streamItem.setCanvasContextFromMap(mCourseMap, mGroupMap);

            // load conversations if needed
            if (streamItem.getType() == StreamItem.Type.CONVERSATION) {

                ConversationManager.getConversation(streamItem.getConversationId(), false,
                        new StatusCallback<Conversation>() {
                    @Override
                    public void onResponse(retrofit2.Response<Conversation> response, LinkHeaders linkHeaders, ApiType type) {
                        streamItem.setConversation(getContext(), response.body(), ApiPrefs.getUser().getId(), getContext().getString(R.string.monologue));
                        notifyDataSetChanged();
                    }


                    @Override
                    public void onFail(Call<Conversation> response, Throwable error) {
                        //Show crouton if it's a network error
                        if (!APIHelper.hasNetworkConnection()) {
                            mAdapterToFragmentCallback.onShowErrorCrouton(R.string.noDataConnection);
                        }
                        //Otherwise show that it's been deleted.
                        else {
                            Conversation conversation = new Conversation();
                            conversation.setDeleted(true);
                            conversation.setDeletedString(getContext().getString(R.string.deleted));
                            streamItem.setConversation(getContext(), conversation, ApiPrefs.getUser().getId(), getContext().getString(R.string.monologue));
                            notifyDataSetChanged();
                        }
                    }
                });
            }

            //make sure there's something there
            if(streamItem.getUpdatedAtDate() == null) {
                continue;
            }

            addOrUpdateItem(DateHelper.getCleanDate(streamItem.getUpdatedAtDate().getTime()), streamItem);
        }

        mStreamItems = null;


        // update count in dashboard
        if (mOnNotificationCountInvalidated != null) {
            mOnNotificationCountInvalidated.invalidateNotificationsCount();
        }
    }

    private void hideStreamItem(final StreamItem streamItem) {
        StreamManager.hideStreamItem(streamItem.getId(), new StatusCallback<HiddenStreamItem>() {

            @Override
            public void onResponse(retrofit2.Response<HiddenStreamItem> response, LinkHeaders linkHeaders, ApiType type) {
                if (response.body().isHidden()) {
                    removeItem(streamItem);

                    if (mOnNotificationCountInvalidated != null) {
                        mOnNotificationCountInvalidated.invalidateNotificationsCount();
                    }
                }
            }

            @Override
            public void onFail(Call<HiddenStreamItem> callResponse, Throwable error, retrofit2.Response response) {
                mDeletedStreamItems.remove(streamItem);
            }
        });
    }

    // endregion

    // region Expandable Callbacks

    @Override
    public GroupSortedList.GroupComparatorCallback<Date> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return o2.compareTo(o1);
            }

            @Override
            public boolean areContentsTheSame(Date oldGroup, Date newGroup) {
                return oldGroup.equals(newGroup);
            }

            @Override
            public boolean areItemsTheSame(Date group1, Date group2) {
                return group1.getTime() == group2.getTime();
            }

            @Override
            public long getUniqueGroupId(Date group) {
                return group.getTime();
            }

            @Override
            public int getGroupType(Date group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<Date, StreamItem> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<Date, StreamItem>() {
            @Override
            public int compare(Date group, StreamItem o1, StreamItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(StreamItem oldItem, StreamItem newItem) {
                return oldItem.getTitle(getContext()).equals(newItem.getTitle(getContext()));
            }

            @Override
            public boolean areItemsTheSame(StreamItem item1, StreamItem item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(StreamItem item) {
                return item.getId();
            }

            @Override
            public int getChildType(Date group, StreamItem item) {
                return Types.TYPE_ITEM;
            }
        };
    }

    // endregion
}
