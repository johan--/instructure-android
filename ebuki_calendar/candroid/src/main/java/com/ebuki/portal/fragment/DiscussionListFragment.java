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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ebuki.portal.R;
import com.ebuki.portal.adapter.DiscussionListRecyclerAdapter;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.interfaces.AdapterToFragmentCallback;
import com.ebuki.portal.util.FragUtils;
import com.ebuki.portal.util.Param;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.utils.Const;

import java.util.HashMap;

public class DiscussionListFragment extends ParentFragment {

    private View mRootView;

    //Used to map ids with their actual unread count
    private HashMap<Long,Integer> mUpdatedUnreadCount = new HashMap<>();
    private DiscussionListRecyclerAdapter mRecyclerAdapter;
    private AdapterToFragmentCallback<DiscussionTopicHeader> mAdapterToFragmentCallback;
    private DiscussionListRecyclerAdapter.AdapterToDiscussionsCallback mAdapterToDiscussionsCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.discussion);
    }

    @Override
    protected String getSelectedParamName() {
        return Param.MESSAGE_ID;
    }

    @Override
    public String getTabId() {
        return Tab.DISCUSSIONS_ID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(this, true);

        mAdapterToFragmentCallback = new AdapterToFragmentCallback<DiscussionTopicHeader>() {
            @Override
            public void onRowClicked(DiscussionTopicHeader discussionTopicHeader, int position, boolean isOpenDetail) {
                Navigation navigation = getNavigation();
                if(navigation != null){
                    //if the discussion/announcement hasn't been published take them back to the publish screen
                    if(!discussionTopicHeader.isPublished()) {
                        navigation.addFragment(FragUtils.getFrag(ComposeNewDiscussionFragment.class, ComposeNewDiscussionFragment.createBundle(getCanvasContext(), isAnnouncement(), discussionTopicHeader)));
                    } else {
                        //clear out the children when we select a new item
                        navigation.addFragment(
                                FragUtils.getFrag(DetailedDiscussionFragment.class,
                                        DetailedDiscussionFragment.createBundle(getCanvasContext(), discussionTopicHeader, isAnnouncement())));
                    }
                }
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }
        };

        mAdapterToDiscussionsCallback = new DiscussionListRecyclerAdapter.AdapterToDiscussionsCallback() {
            @Override
            public boolean isDiscussions() {
                return !isAnnouncement();
            }

            @Override
            public HashMap<Long, Integer> getUnreadCount() {
                return mUpdatedUnreadCount;
            }

            @Override
            public void clearUnreadCount() {
                mUpdatedUnreadCount.clear();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(Const.REFRESH);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshReceiver);
        } catch (Exception e) {
            Logger.e("Could not unregister refreshReceiver");
        }
    }

    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mRecyclerAdapter != null) {
                setRefreshing(true);
                mRecyclerAdapter.refresh();
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = getLayoutInflater().inflate(R.layout.course_discussion_topic, container, false);

        mRecyclerAdapter = new DiscussionListRecyclerAdapter(getContext(), getCanvasContext(), !isAnnouncement(), mAdapterToFragmentCallback, mAdapterToDiscussionsCallback);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);

        return mRootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        checkCoursePermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null && data.hasExtra(Const.DISCUSSION_HEADER)) {
            try {
                DiscussionTopicHeader header =  data.getParcelableExtra(Const.DISCUSSION_HEADER);
                if(header.isPinned()){
                    mRecyclerAdapter.addOrUpdateItem(mRecyclerAdapter.pinnnedDiscussionsHeader, header);
                } else {
                    mRecyclerAdapter.addOrUpdateItem(mRecyclerAdapter.discussionsHeader, header);
                }
            } catch(Exception E) {}
        }
    }

    public void setUpdatedUnreadCount(long topicID, int count){
        mUpdatedUnreadCount.put(topicID, count);
        updateUnreadCount(topicID);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Actionbar
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        CanvasContext canvasContext = getCanvasContext();
        if(canvasContext != null && canvasContext.canCreateDiscussion()) {
            if(canvasContext instanceof Course || canvasContext instanceof Group) {
                //Check permissions to see if the use can create a discussion or an announcement
                if(canvasContext.getPermissions() != null && canvasContext.getPermissions().getCanCreateDiscussionTopic() && !isAnnouncement()) {
                    //Can create discussions
                    inflater.inflate(R.menu.menu_new_discussion, menu);
                } else if(canvasContext.getPermissions() != null && canvasContext.getPermissions().getCanCreateAnnouncement() && isAnnouncement()) {
                    //Can create announcement
                    inflater.inflate(R.menu.menu_new_announcement, menu);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Navigation navigation = getNavigation();
        if(navigation != null) {
            switch (item.getItemId()) {
                case R.id.menu_add_announcement:
                    if(!APIHelper.hasNetworkConnection()) {
                        Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    navigation.addFragment(FragUtils.getFrag(ComposeNewDiscussionFragment.class,
                            ComposeNewDiscussionFragment.createBundle(getCanvasContext(), true)));
                    return true;
                case R.id.menu_add_discussion:
                    if(!APIHelper.hasNetworkConnection()) {
                        Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    navigation.addFragment(FragUtils.getFrag(ComposeNewDiscussionFragment.class,
                            ComposeNewDiscussionFragment.createBundle(getCanvasContext(), false)));
                    return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUnreadCount(long id) {
        for(int j = 0; j < mRecyclerAdapter.size(); j++) {
            String group = mRecyclerAdapter.getGroup(j);
            DiscussionTopicHeader discussionTopicHeader = mRecyclerAdapter.getItem(group, j);
            if(discussionTopicHeader != null && discussionTopicHeader.getId() == id) {
                int unreadCount = mUpdatedUnreadCount.get(id);
                if(unreadCount < 0) {
                    unreadCount = 0;
                }

                discussionTopicHeader.setUnreadCount(unreadCount);
                //set the read state as read so we can unbold the topic
                discussionTopicHeader.setStatus(DiscussionTopicHeader.ReadState.READ);
                //For some reason, notifyItemChanged() isn't working, so use range instead.
                mRecyclerAdapter.notifyItemRangeChanged(j, j);
                break;
            }
        }
    }

    //Manage whether or not a user can post a new discussion.
    public void checkCoursePermission() {
        if (getCanvasContext().getPermissions() == null) {
            //Figure out whether to get the COURSE or the GROUP permissions.
            if (getCanvasContext().getType() == CanvasContext.Type.COURSE) {
                CourseManager.getCourse(getCanvasContext().getId(), new StatusCallback<Course>() {
                    @Override
                    public void onResponse(retrofit2.Response<Course> response, LinkHeaders linkHeaders, ApiType type) {
                        if(!apiCheck()){
                            return;
                        }

                        getCanvasContext().setPermissions(response.body().getPermissions());
                        getActivity().supportInvalidateOptionsMenu();
                    }
                }, true);
            } else {
                GroupManager.getDetailedGroup(getCanvasContext().getId(), new StatusCallback<Group>() {
                    @Override
                    public void onResponse(retrofit2.Response<Group> response, LinkHeaders linkHeaders, ApiType type) {
                        if(!apiCheck()){
                            return;
                        }

                        getCanvasContext().setPermissions(response.body().getPermissions());
                        getActivity().supportInvalidateOptionsMenu();
                    }
                }, true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
    }

    @Override
    public boolean allowBookmarking() {
        return true;
    }

    protected boolean isAnnouncement() {
        return false;
    }
}
