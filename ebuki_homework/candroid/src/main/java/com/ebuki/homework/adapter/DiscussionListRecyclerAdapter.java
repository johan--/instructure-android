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

package com.ebuki.homework.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.ebuki.homework.R;
import com.ebuki.homework.binders.DiscussionTopicHeaderBinder;
import com.ebuki.homework.binders.EmptyBinder;
import com.ebuki.homework.binders.ExpandableHeaderBinder;
import com.ebuki.homework.holders.DiscussionTopicHeaderViewHolder;
import com.ebuki.homework.holders.EmptyViewHolder;
import com.ebuki.homework.holders.ExpandableViewHolder;
import com.ebuki.homework.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.AnnouncementManager;
import com.instructure.canvasapi2.managers.DiscussionManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.utils.CanvasContextColor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscussionListRecyclerAdapter extends ExpandableRecyclerAdapter<String, DiscussionTopicHeader, RecyclerView.ViewHolder> {

    public interface AdapterToDiscussionsCallback {
        boolean isDiscussions();
        HashMap<Long, Integer> getUnreadCount();
        void clearUnreadCount();
    }

    private CanvasContext mCanvasContext;

    private AdapterToDiscussionsCallback mAdapterToDiscussionsCallback;
    private AdapterToFragmentCallback<DiscussionTopicHeader> mAdapterToFragmentCallback;
    private StatusCallback<List<DiscussionTopicHeader>> mCanvasCallback;
    private StatusCallback<List<DiscussionTopicHeader>> mPinnedDiscussionCanvasCallback;

    private HashMap<String, List<DiscussionTopicHeader>> mSyncHash = new HashMap<>();
    private int mCourseColor;
    private boolean mIsDiscussions;

    public String pinnnedDiscussionsHeader;
    public String discussionsHeader;
    public String closedForCommentsHeader;
    public String announcementsHeader;

    /* This constructor is for testing purposes only */
    protected DiscussionListRecyclerAdapter(Context context) {
        super(context, String.class, DiscussionTopicHeader.class);
    }

    public DiscussionListRecyclerAdapter(Context context, CanvasContext canvasContext, boolean isDiscussions, AdapterToFragmentCallback<DiscussionTopicHeader> adapterToFragmentCallback,
                                         AdapterToDiscussionsCallback adapterToDiscussionsCallback) {
        super(context, String.class, DiscussionTopicHeader.class);
        mAdapterToDiscussionsCallback = adapterToDiscussionsCallback;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mCanvasContext = canvasContext;
        setExpandedByDefault(true);

        setViewHolderHeaderClicked(new ViewHolderHeaderClicked<String>() {
            @Override
            public void viewClicked(View view, String groupName) {
                expandCollapseGroup(groupName);

                if(!isGroupExpanded(groupName)) {
                    //if this group is collapsed we want to try to load the data to avoid having
                    //a progress bar spin forever.
                    loadData();
                }
            }
        });

        mIsDiscussions = isDiscussions;
        mCourseColor = CanvasContextColor.getCachedColor(context, canvasContext);
        pinnnedDiscussionsHeader = getContext().getString(R.string.pinned_discussions);
        discussionsHeader = getContext().getString(R.string.discussion);
        closedForCommentsHeader = getContext().getString(R.string.closed_discussion);
        announcementsHeader = getContext().getString(R.string.announcements);

        loadData();
    }

    // workaround for when lastReplyAt and postedAt is null, just sort by title and stick it at the bottom of the list
    private int sortDiscussions(DiscussionTopicHeader o1, DiscussionTopicHeader o2) {
        if (o1.getComparisonDate() == null && o2.getComparisonDate() == null) {
            return o1.getTitle().compareTo(o2.getTitle());
        } else if (o1.getComparisonDate() == null) {
            return 1;
        } else if (o2.getComparisonDate() == null) {
            return -1;
        } else {
            return o2.getComparisonDate().compareTo(o1.getComparisonDate());
        }
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if(viewType == Types.TYPE_HEADER){
            return new ExpandableViewHolder(v);
        } else {
            return new DiscussionTopicHeaderViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if(viewType == Types.TYPE_HEADER){
            return ExpandableViewHolder.holderResId();
        } else {
            return DiscussionTopicHeaderViewHolder.holderResId();
        }
    }

    private void populateAdapter(List<DiscussionTopicHeader> discussionTopicHeaders){
        for(DiscussionTopicHeader discussionTopicHeader : discussionTopicHeaders){
            if(!mIsDiscussions){
                addOrUpdateItem(announcementsHeader, discussionTopicHeader);
            } else if(discussionTopicHeader.isPinned()){
                addOrUpdateItem(pinnnedDiscussionsHeader, discussionTopicHeader);
            } else {
                addOrUpdateItem(discussionsHeader, discussionTopicHeader);
            }
        }

        mAdapterToFragmentCallback.onRefreshFinished();
        notifyDataSetChanged();
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void refresh() {
        mCanvasCallback.reset();
        super.refresh();
    }

    @Override
    public void loadFirstPage() {
        if(mAdapterToDiscussionsCallback.isDiscussions()){
            DiscussionManager.getAllPinnedDiscussions(mCanvasContext, true, mPinnedDiscussionCanvasCallback);
            DiscussionManager.getDiscussions(true, mCanvasContext.getId(), mCanvasCallback);
        } else {
            AnnouncementManager.getAnnouncements(mCanvasContext.getId(), true, mCanvasCallback);
        }
    }

    @Override
    public void loadNextPage(String nextURL) {
        if(mAdapterToDiscussionsCallback.isDiscussions()){
            //calls next page auto-magically
            DiscussionManager.getDiscussions(true, mCanvasContext.getId(), mCanvasCallback);
        } else {
            //calls next page auto-magically
            AnnouncementManager.getAnnouncements(mCanvasContext.getId(), true, mCanvasCallback);
        }
    }

    /**
     * Used to sync the pinned discussions and first page of discussions
     * @param isCache
     */
    private void syncCallback(boolean isCache) {
        if (mSyncHash.size() < 2) {
            return;
        }

        for (Map.Entry<String, List<DiscussionTopicHeader>> entry : mSyncHash.entrySet()) {
            List<DiscussionTopicHeader> discussionTopicHeaders = entry.getValue();
            if (discussionTopicHeaders != null && discussionTopicHeaders.size() > 0) {
                populateAdapter(discussionTopicHeaders);
            }
        }

        mAdapterToFragmentCallback.onRefreshFinished();
        notifyDataSetChanged();
    }

    @Override
    public void setupCallbacks() {
        mPinnedDiscussionCanvasCallback = new StatusCallback<List<DiscussionTopicHeader>>() {
            @Override
            public void onResponse(retrofit2.Response<List<DiscussionTopicHeader>> response, LinkHeaders linkHeaders, ApiType type) {
                mSyncHash.put("pinned", response.body());
                syncCallback(APIHelper.isCachedResponse(response));
            }
        };

        mCanvasCallback = new StatusCallback<List<DiscussionTopicHeader>>() {
            @Override
            public void onResponse(retrofit2.Response<List<DiscussionTopicHeader>> response, LinkHeaders linkHeaders, ApiType type) {
                if (mAdapterToDiscussionsCallback.isDiscussions()) {
                    mSyncHash.put("firstpage", response.body());
                    syncCallback(APIHelper.isCachedResponse(response));
                } else {
                    populateAdapter(response.body());
                }
                setNextUrl(linkHeaders.nextUrl);
            }

            @Override
            public void onFinished(ApiType type) {
                DiscussionListRecyclerAdapter.this.onCallbackFinished(type);
            }
        };
    }

    @Override
    public void resetData() {
        //The sync hash needs to be cleared on refresh
        mSyncHash.clear();
        super.resetData();
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, String s, DiscussionTopicHeader discussionTopicHeader) {
        DiscussionTopicHeaderBinder.bind((DiscussionTopicHeaderViewHolder)holder, discussionTopicHeader, getContext(), mCourseColor, mIsDiscussions, mAdapterToFragmentCallback);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, String s, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder)holder, s, s, isExpanded, getViewHolderHeaderClicked());
    }

    @Override
    public void onBindEmptyHolder(RecyclerView.ViewHolder holder, String s) {
        EmptyBinder.bind((EmptyViewHolder) holder, getContext().getResources().getString(R.string.empty_discussions));
    }

    @Override
    public GroupSortedList.GroupComparatorCallback<String> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<String>() {
            @Override
            public int compare(String o1, String o2) {
                //This sorting ensures that the order stays as:
                //Pinned
                //Discussions
                //Closed
                if(o1.equals(o2)){
                    return 0;
                } else {
                    if(o1.equals(pinnnedDiscussionsHeader) || o1.equals(discussionsHeader) && !o2.equals(pinnnedDiscussionsHeader)){
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }

            @Override
            public boolean areContentsTheSame(String oldGroup, String newGroup) {
                return oldGroup.equals(newGroup);
            }

            @Override
            public boolean areItemsTheSame(String group1, String group2) {
                return group1.equals(group2);
            }

            @Override
            public long getUniqueGroupId(String group) {
                return group.hashCode();
            }

            @Override
            public int getGroupType(String group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<String, DiscussionTopicHeader> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<String, DiscussionTopicHeader>() {
            @Override
            public int compare(String group, DiscussionTopicHeader o1, DiscussionTopicHeader o2) {
                return sortDiscussions(o1, o2);
            }

            @Override
            public boolean areContentsTheSame(DiscussionTopicHeader item1, DiscussionTopicHeader item2) {
                return item1.getTitle().equals(item2.getTitle()) && item1.getStatus().equals(item2.getStatus());
            }

            @Override
            public boolean areItemsTheSame(DiscussionTopicHeader item1, DiscussionTopicHeader item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(DiscussionTopicHeader discussionTopicHeader) {
                return discussionTopicHeader.getId();
            }

            @Override
            public int getChildType(String group, DiscussionTopicHeader item) {
                return Types.TYPE_ITEM;
            }
        };
    }
}
