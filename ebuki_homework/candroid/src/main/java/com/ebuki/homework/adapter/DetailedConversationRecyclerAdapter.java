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
import com.ebuki.homework.binders.DetailedConversationBinder;
import com.ebuki.homework.fragment.DetailedConversationFragment;
import com.ebuki.homework.holders.DetailedConversationAttachmentViewHolder;
import com.ebuki.homework.holders.DetailedConversationMessageViewHolder;
import com.ebuki.homework.interfaces.DetailedConversationAdapterToFragmentCallback;
import com.ebuki.homework.model.MessageAttachment;
import com.ebuki.homework.model.MessageWithDepth;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.ConversationManager;
import com.instructure.canvasapi2.models.Attachment;
import com.instructure.canvasapi2.models.BasicUser;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Message;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailedConversationRecyclerAdapter extends ExpandableRecyclerAdapter<MessageWithDepth, MessageAttachment, RecyclerView.ViewHolder>  {

    private StatusCallback<Conversation> mConversationCallback;
    private DetailedConversationFragment.UpdateMessageStateListener mUpdateUnreadCallback;
    private DetailedConversationAdapterToFragmentCallback mAdapterToFragmentCallback;

    private boolean mIsUnread;
    private long mConversationID;
    private long mUserId;
    private List<BasicUser> mAllParticipants;
    private List<Long> mConversationAudience;
    private Map<Long, BasicUser> mParticipantsMap;

    public DetailedConversationRecyclerAdapter(Context context, long conversationID,
        DetailedConversationFragment.UpdateMessageStateListener updateUnreadCallback,
        DetailedConversationAdapterToFragmentCallback adapterToFragmentCallback, boolean isUnread) {

        super(context, MessageWithDepth.class, MessageAttachment.class);

        mConversationID = conversationID;
        mUpdateUnreadCallback = updateUnreadCallback;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mUserId = ApiPrefs.getUser().getId();
        mIsUnread = isUnread;
        setExpandedByDefault(true);
        setChildrenAboveGroup(true);
        setupCallbacks();
        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if(viewType == DetailedConversationMessageViewHolder.TYPE_LEFT_TEXT || viewType == DetailedConversationMessageViewHolder.TYPE_RIGHT_TEXT){
            return new DetailedConversationMessageViewHolder(v);
        } else {
            return new DetailedConversationAttachmentViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if(viewType == DetailedConversationMessageViewHolder.TYPE_LEFT_TEXT || viewType == DetailedConversationMessageViewHolder.TYPE_RIGHT_TEXT){
            return DetailedConversationMessageViewHolder.holderResId(viewType);
        }else{
            return DetailedConversationAttachmentViewHolder.holderResId(viewType);
        }
    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, MessageWithDepth message, MessageAttachment attachment) {
        DetailedConversationBinder.bindAttachment(mContext, (DetailedConversationAttachmentViewHolder) holder, message, attachment, mAllParticipants, mAdapterToFragmentCallback);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, MessageWithDepth message, boolean isExpanded) {
        DetailedConversationBinder.bindMessageText((DetailedConversationMessageViewHolder) holder, message, mContext, mAllParticipants);
    }

    @Override
    public GroupSortedList.GroupComparatorCallback<MessageWithDepth> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<MessageWithDepth>() {
            @Override
            public int compare(MessageWithDepth o1, MessageWithDepth o2) {
                return o1.message.getCreatedAt().compareTo(o2.message.getCreatedAt());
            }

            @Override
            public boolean areContentsTheSame(MessageWithDepth item1, MessageWithDepth item2) {
                return item1.message.getComparisonString().equals(item2.message.getComparisonString());
            }

            @Override
            public boolean areItemsTheSame(MessageWithDepth item1, MessageWithDepth item2) {
                return item1.message.getId() == item2.message.getId();
            }

            @Override
            public long getUniqueGroupId(MessageWithDepth group) {
                return group.message.getId();
            }

            @Override
            public int getGroupType(MessageWithDepth group) {
                return isUser(group.message.getAuthorId()) ? DetailedConversationMessageViewHolder.TYPE_RIGHT_TEXT : DetailedConversationMessageViewHolder.TYPE_LEFT_TEXT;
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<MessageWithDepth, MessageAttachment > createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<MessageWithDepth, MessageAttachment>() {
            @Override
            public int compare(MessageWithDepth group, MessageAttachment o1, MessageAttachment o2) {
                return ((Integer)o2.getOrder()).compareTo(o1.getOrder());
            }

            @Override
            public boolean areContentsTheSame(MessageAttachment oldItem, MessageAttachment newItem) {
                return oldItem.getComparisonString().equals(newItem.getComparisonString());
            }

            @Override
            public boolean areItemsTheSame(MessageAttachment item1, MessageAttachment item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(MessageAttachment item) {
                return item.getId();
            }

            @Override
            public int getChildType(MessageWithDepth group, MessageAttachment item) {
                return isUser(group.message.getAuthorId()) ? DetailedConversationAttachmentViewHolder.TYPE_RIGHT_ATTACHMENT : DetailedConversationAttachmentViewHolder.TYPE_LEFT_ATTACHMENT;
            }
        };
    }

    @Override
    public void contextReady() {}

    @Override
    public void setupCallbacks() {
        mConversationCallback = new StatusCallback<Conversation>() {

            @Override
            public void onResponse(retrofit2.Response<Conversation> response, LinkHeaders linkHeaders, ApiType type) {
                unwindConversation(response.body());

                if (mIsUnread) {
                    if (mUpdateUnreadCallback != null && response.body().getWorkflowState() == Conversation.WorkflowState.READ){
                        mUpdateUnreadCallback.updateMessageState(response.body(), Conversation.WorkflowState.READ);
                    }
                }
                mAdapterToFragmentCallback.conversationWasFetched(response.body());
                mAdapterToFragmentCallback.onRefreshFinished();
            }
        };
    }

    @Override
    public void loadFirstPage() {
        ConversationManager.getConversation(mConversationID, true, mConversationCallback);
    }

    @Override
    public void loadNextPage(String nextURL) {}

    private void unwindConversation(final Conversation c) {
        if (getContext() == null) {return;}

        if (c != null) {
            clear();
            mAllParticipants = c.getParticipants();
            mConversationAudience = c.getAudience();

            Collections.sort(mConversationAudience);
            createParticipantsMap();
        }

        unwindConversationRecursively(c.getMessages(), 0);
    }

    private void createParticipantsMap(){
        mParticipantsMap = new HashMap<>();
        for(BasicUser user : mAllParticipants){
            mParticipantsMap.put(user.getId(), user);
        }
    }

    // TODO : Forwarded messages currently seem to be broken? Need clarification if it's been deprecated, or if there's a bug.
    // Leave the recursive method call for now.
    private void unwindConversationRecursively(List<Message> m, int depth) {
        for (int i = (m.size() - 1); i >= 0; i--) {
            Message msg = m.get(i);

            addMessage(msg, depth);

            unwindConversationRecursively(m.get(i).getForwardedMessages(), ++depth);
        }
    }

    /**
     * Helper method to determine whether or not we want to display an additional line indicating
     * that the message has been sent to a subset of users different than the conversations audience.
     * Should be noted that the messageParticipants returned from the API will include current user Ids,
     * where as Audience will not. We remove the user id from the participants list prior to calling this method.
     * @param conversationAudience
     * @param messageParticipants
     * @return
     */
    private static boolean areParticipantsListEqual(List<Long> conversationAudience, List<Long> messageParticipants){
        if(conversationAudience == null && messageParticipants == null){
            return true;
        }

        if((conversationAudience == null && messageParticipants != null)
                || (conversationAudience != null && messageParticipants == null)
                || (conversationAudience.size() != messageParticipants.size())){
            return false;
        }

        Collections.sort(messageParticipants);
        return conversationAudience.equals(messageParticipants);
    }

    private static String getMessageParticipantsString(Context context, long author_id, List<Long> participantIds, Map<Long, BasicUser> participantsMap){
        final int MAX_USERNAMES_TO_DISPLAY = 2;
        StringBuilder participants = new StringBuilder();

        // If participants contains author_id, that means this is a conversation from a different user since we removed the current user id from that list.
        // Otherwise it must be a message from the current user.
        final String sharedStringPrefix = participantIds.contains(author_id) ? context.getResources().getString(R.string.sharedWithYou) : context.getResources().getString(R.string.sharedWith);
        participants.append(sharedStringPrefix);

        participantIds.remove(author_id);
        
        final int usersToShow = participantIds.size() >= MAX_USERNAMES_TO_DISPLAY ? MAX_USERNAMES_TO_DISPLAY :participantIds.size();

        for (int i = 0; i < usersToShow; i++) {
            if (i != 0) {
                participants.append(", ");
            }
            if(participantsMap.containsKey(participantIds.get(i))) {
                BasicUser user = participantsMap.get(participantIds.get(i));
                if(user != null && user.getName() != null) {
                    participants.append(user.getName());
                }
            }
        }

        if(participantIds.size() - usersToShow > 0 ){
            participants.append(String.format(" + %d", participantIds.size() - usersToShow));
        }

        return  participants.toString();
    }

    public void addMessage(Message msg, int depth){
        MessageWithDepth mwd = new MessageWithDepth();
        mwd.depth = depth;
        mwd.message = msg;

        // the participants list includes the current users' id, where as the audience
        // list doesn't. We remove the current user id here in order to compare the two lists.
        if( msg.getParticipatingUserIds() != null){
            msg.getParticipatingUserIds().remove(mUserId);
        }

        mwd.isAllParticipants = areParticipantsListEqual(mConversationAudience, msg.getParticipatingUserIds());
        mwd.participantsString = getMessageParticipantsString(getContext(), msg.getAuthorId(), msg.getParticipatingUserIds(), mParticipantsMap);

        addOrUpdateGroup(mwd);
        int tempOrder = 0;
        for(Attachment attachment : msg.getAttachments()){
            MessageAttachment messageAttachment = new MessageAttachment(msg, attachment, tempOrder++);
            addOrUpdateItem(mwd, messageAttachment);
        }

        if(msg.getMediaComment() != null){
            MessageAttachment mediaComment = new MessageAttachment(msg, msg.getMediaComment(), tempOrder);
            addOrUpdateItem(mwd, mediaComment);
        }
    }

    private boolean isUser(long authorId){
        return authorId == mUserId;
    }
}
