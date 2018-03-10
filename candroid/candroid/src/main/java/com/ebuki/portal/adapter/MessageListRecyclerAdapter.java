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
import android.view.View;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.binders.MessageBinder;
import com.instructure.candroid.fragment.MessageListFragment;
import com.instructure.candroid.holders.MessageViewHolder;
import com.instructure.candroid.util.DebounceMessageToAdapterListener;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.ConversationAPI;
import com.instructure.canvasapi2.managers.ConversationManager;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.List;

public class MessageListRecyclerAdapter extends MultiSelectRecyclerAdapter<Conversation, MessageViewHolder> {

    //model
    private ConversationAPI.ConversationScope mMessageType;
    private long mMyUserID;

    //callbacks and interfaces
    private StatusCallback<List<Conversation>> mInboxConversationCallback;
    private StatusCallback<List<Conversation>> mUnreadConversationCallback;
    private StatusCallback<List<Conversation>> mArchivedConversationCallback;
    private StatusCallback<List<Conversation>> mSentConversationCallback;

    private DebounceMessageToAdapterListener mAdapterToFragmentCallback;
    private ItemClickedInterface mItemClickedInterface;
    private MessageListFragment.OnUnreadCountInvalidated mUnReadCountInvalidated;

    public interface ItemClickedInterface{
        void itemClick(Conversation item, MessageViewHolder viewHolder);
        void itemLongClick(Conversation item, MessageViewHolder viewHolder);
    }

    public MessageListRecyclerAdapter(Context context, List items,
                                      ConversationAPI.ConversationScope messageType,
                                      MultiSelectCallback multiSelectCallback, long myUserID,
                                      DebounceMessageToAdapterListener adapterToFragmentCallback,
                                      MessageListFragment.OnUnreadCountInvalidated unreadCountInvalidated) {

        super(context, Conversation.class, items, multiSelectCallback);
        mMessageType = messageType;
        mMyUserID = myUserID;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mUnReadCountInvalidated = unreadCountInvalidated;
        setItemCallback(new ItemComparableCallback<Conversation>() {
            @Override
            public int compare(Conversation o1, Conversation o2) {
                // Don't sort the data since the API already gives us the correct order
                if (o1.getId() == o2.getId()) {
                    return 0;
                } else {
                    return -1;
                }
            }

            @Override
            public boolean areContentsTheSame(Conversation oldItem, Conversation newItem) {
                if(containsNull(oldItem.getLastMessagePreview(), newItem.getLastMessagePreview()) || !oldItem.getWorkflowState().equals(newItem.getWorkflowState())){
                    return false;
                }
                return oldItem.getLastMessagePreview().equals(newItem.getLastMessagePreview());
            }

            @Override
            public boolean areItemsTheSame(Conversation item1, Conversation item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(Conversation conversation) {
                return conversation.getId();
            }

        });
        loadData();
    }

    private boolean containsNull(Object oldItem, Object newItem) {
        return (oldItem == null || newItem == null);
    }

    @Override
    public void bindHolder(Conversation conversation, MessageViewHolder viewHolder, int position) {
        MessageBinder
                .bind(  viewHolder,
                        conversation,
                        mContext,
                        mMyUserID,
                        isItemSelected(conversation),
                        mItemClickedInterface);
    }

    @Override
    public MessageViewHolder createViewHolder(View v, int viewType) {
        return new MessageViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return MessageViewHolder.holderResId();
    }

    @Override
    public void contextReady() {
        setupCallbacks();
    }

    @Override
    public void setupCallbacks() {
        mInboxConversationCallback = createCallback(ConversationAPI.ConversationScope.ALL);
        mUnreadConversationCallback = createCallback(ConversationAPI.ConversationScope.UNREAD);
        mArchivedConversationCallback = createCallback(ConversationAPI.ConversationScope.ARCHIVED);
        mSentConversationCallback = createCallback(ConversationAPI.ConversationScope.SENT);

        mItemClickedInterface = new ItemClickedInterface() {
            @Override
            public void itemClick(Conversation item, MessageViewHolder viewHolder) {
                int prevPosition = getSelectedPosition();
                if(isMultiSelectMode()) {
                    toggleSelection(item);
                    clearSelectedPosition();
                    notifyItemChanged(prevPosition);
                    notifyItemChanged(viewHolder.getAdapterPosition());
                } else {
                    mAdapterToFragmentCallback.onClick(item, viewHolder.getAdapterPosition(), viewHolder.userAvatar, true);
                }
            }

            @Override
            public void itemLongClick(Conversation item, MessageViewHolder viewHolder) {
                if(!APIHelper.hasNetworkConnection()) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isMultiSelectMode()) {
                    setMultiSelectMode(true);
                }
                toggleSelection(item);
                int prevPosition = getSelectedPosition();
                clearSelectedPosition();
                notifyItemChanged(prevPosition);
                notifyItemChanged(viewHolder.getAdapterPosition());
            }
        };
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void resetData() {
        super.resetData();
        mInboxConversationCallback.reset();
        mUnreadConversationCallback.reset();
        mArchivedConversationCallback.reset();
        mSentConversationCallback.reset();
    }

    @Override
    public void loadFirstPage() {
        if(mMessageType.equals(ConversationAPI.ConversationScope.ALL)){
            ConversationManager.getConversations(mMessageType, true, mInboxConversationCallback);
        } else if (mMessageType.equals(ConversationAPI.ConversationScope.UNREAD)){
            ConversationManager.getConversations(mMessageType, true, mUnreadConversationCallback);
        } else if (mMessageType.equals(ConversationAPI.ConversationScope.ARCHIVED)){
            ConversationManager.getConversations(mMessageType, true, mArchivedConversationCallback);
        } else {
            ConversationManager.getConversations(mMessageType, true, mSentConversationCallback);
        }
    }

    @Override
    public void loadNextPage(String nextURL) {
        loadFirstPage();
    }

    public void clearSelectedPosition(){
        setSelectedPosition(-1);
    }

    public void setMessageType(ConversationAPI.ConversationScope messageType){
        mMessageType = messageType;
    }

    public void removeConversation(Conversation conversation){
        if(conversation != null){
            mSelectedItems.add(conversation);
        }
    }

    private StatusCallback<List<Conversation>> createCallback(final ConversationAPI.ConversationScope messageType){
        return new StatusCallback<List<Conversation>>() {

            @Override
            public void onResponse(retrofit2.Response<List<Conversation>> response, LinkHeaders linkHeaders, ApiType type) {

                if(!mMessageType.equals(messageType)){
                    return;
                }
                addAll(response.body());
                setNextUrl(linkHeaders.nextUrl);

                //update unread count
                if (mUnReadCountInvalidated != null && !APIHelper.isCachedResponse(response)) {
                    mUnReadCountInvalidated.invalidateUnreadCount();
                }

                notifyDataSetChanged();
                //notify swipe to refresh layout
                mAdapterToFragmentCallback.onRefreshFinished();
            }

            @Override
            public void onFinished(ApiType type) {
                MessageListRecyclerAdapter.this.onCallbackFinished();
            }
        };
    }
}
