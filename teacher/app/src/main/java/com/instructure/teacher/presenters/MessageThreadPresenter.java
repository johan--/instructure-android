/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.presenters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.ConversationAPI;
import com.instructure.canvasapi2.managers.ConversationManager;
import com.instructure.canvasapi2.models.BasicUser;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Message;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.teacher.R;
import com.instructure.teacher.viewinterface.MessageThreadView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import instructure.androidblueprint.SyncPresenter;
import retrofit2.Call;
import retrofit2.Response;

public class MessageThreadPresenter extends SyncPresenter<Message, MessageThreadView> {

    private Conversation mConversation;
    private int mPosition;

    private HashMap<Long, BasicUser> mParticipants = new HashMap<>();

    private boolean mSkipCacheForRefresh = false;

    public MessageThreadPresenter(@NonNull Conversation conversation, int position) {
        super(Message.class);
        mConversation = conversation;
        mPosition = position;
    }

    @Override
    public void loadData(boolean forceNetwork) {
        if (getData().size() == 0) {
            ConversationManager.getConversation(mConversation.getId(), forceNetwork, mConversationCallback);
        } else if (getViewCallback() != null) {
            getViewCallback().onRefreshFinished();
            getViewCallback().checkIfEmpty();
        }

        if (mConversation.getWorkflowState() == Conversation.WorkflowState.UNREAD && forceNetwork) {
            //we need to update this for our event
            mConversation.setWorkflowState("read");
            //we need to inform the inbox fragment to update this to read
            getViewCallback().onConversationRead(mPosition);
        }
    }

    @Override
    public void refresh(boolean forceNetwork) {
        mSkipCacheForRefresh = true;
        onRefreshStarted();
        mConversationCallback.reset();
        clearData();
        loadData(forceNetwork);
    }

    private StatusCallback<Conversation> mConversationCallback = new StatusCallback<Conversation>() {

        @Override
        public void onResponse(Response<Conversation> response, LinkHeaders linkHeaders, ApiType type) {

            // Skip cache if we're refreshing
            if (type == ApiType.CACHE && mSkipCacheForRefresh) {
                mSkipCacheForRefresh = false;
                return;
            }

            // Assemble list of messages
            List<Message> messages = new ArrayList<>();
            for (Message message : response.body().getMessages()) {
                appendMessages(messages, message);
            }

            // Map out conversation participants
            for (BasicUser participant : response.body().getParticipants()) {
                mParticipants.put(participant.getId(), participant);
            }

            getData().addOrUpdate(messages);
        }

        @Override
        public void onFinished(ApiType type) {
            if (getViewCallback() != null) {
                getViewCallback().onRefreshFinished();
                getViewCallback().checkIfEmpty();
            }
        }
    };

    private void appendMessages(List<Message> list, Message message) {
        for (Message innerMessage : message.getForwardedMessages()) {
            appendMessages(list, innerMessage);
        }
        list.add(message);
    }

    @Nullable
    public BasicUser getParticipantById(long id) {
        return mParticipants.get(id);
    }

    public Conversation getConversation() {
        return mConversation;
    }

    public void toggleArchived() {
        final boolean archive = mConversation.getWorkflowState() != Conversation.WorkflowState.ARCHIVED;
        ConversationManager.archiveConversation(mConversation.getId(), archive, new StatusCallback<Conversation>() {
            @Override
            public void onResponse(Response<Conversation> response, LinkHeaders linkHeaders, ApiType type) {
                super.onResponse(response, linkHeaders, type);
                if(type.isAPI()) {
                    showUserMessage(archive ? R.string.message_archived : R.string.message_unarchived);
                    if (getViewCallback() != null) {
                        if(mConversation.getWorkflowState() == Conversation.WorkflowState.ARCHIVED) {
                           mConversation.setWorkflowState("unknown");
                        } else {
                            mConversation.setWorkflowState("archived");
                        }
                        getViewCallback().onConversationArchived(mPosition);
                        getViewCallback().refreshConversationData();
                    }
                }
            }

            @Override
            public void onFail(Call<Conversation> response, Throwable error) {
                super.onFail(response, error);
                showUserMessage(R.string.error_conversation_generic);
            }
        });
    }

    public void toggleStarred() {
        ConversationManager.starConversation(mConversation.getId(), !mConversation.isStarred(), mConversation.getWorkflowState(), new StatusCallback<Conversation>() {
            @Override
            public void onResponse(Response<Conversation> response, LinkHeaders linkHeaders, ApiType type) {
                if(type.isAPI()) {
                    if (getViewCallback() != null) {
                        if(mConversation.isStarred()) {
                            mConversation.setStarred(false);
                        } else {
                            mConversation.setStarred(true);
                        }
                        getViewCallback().onConversationStarred(mPosition);
                        getViewCallback().refreshConversationData();
                    }
                }
            }

            @Override
            public void onFail(Call<Conversation> response, Throwable error) {
                super.onFail(response, error);
                showUserMessage(R.string.error_conversation_generic);
            }
        });
    }

    public void deleteConversation() {
        ConversationManager.deleteConversation(mConversation.getId(), new StatusCallback<Conversation>() {
            @Override
            public void onResponse(Response<Conversation> response, LinkHeaders linkHeaders, ApiType type) {
                super.onResponse(response, linkHeaders, type);
                if (getViewCallback() != null && type.isAPI()) {
                    getViewCallback().onConversationDeleted(mPosition);
                }
            }

            @Override
            public void onFail(Call<Conversation> response, Throwable error) {
                super.onFail(response, error);
                showUserMessage(R.string.error_conversation_generic);
            }
        });
    }

    public void deleteMessage(final Message message) {
        List<Long> messageIds = new ArrayList<>(1);
        messageIds.add(message.getId());
        ConversationManager.deleteMessages(mConversation.getId(), messageIds, new StatusCallback<Conversation>() {
            @Override
            public void onResponse(Response<Conversation> response, LinkHeaders linkHeaders, ApiType type) {
                super.onResponse(response, linkHeaders, type);
                if(type.isAPI()) {
                    boolean needsUpdate = false;
                    if(getData().indexOf(message) == 0) {
                        //the top one was removed, we need to refresh the list so the reply button is on the top message
                        needsUpdate = true;
                    }
                    getData().remove(message);
                    if (getData().size() > 0) {
                        showUserMessage(R.string.message_deleted);
                        if(needsUpdate && getViewCallback() != null) {
                            getViewCallback().onMessageDeleted();
                        }
                    } else if (getViewCallback() != null) {
                        getViewCallback().onConversationDeleted(mPosition);

                    }
                }
            }

            @Override
            public void onFail(Call<Conversation> response, Throwable error) {
                super.onFail(response, error);
                showUserMessage(R.string.error_conversation_generic);
            }
        });
    }

    public void markConversationUnread() {
        ConversationManager.markConversationAsUnread(mConversation.getId(), ConversationAPI.CONVERSATION_MARK_UNREAD, new StatusCallback<Void>() {
            @Override
            public void onResponse(Response<Void> response, LinkHeaders linkHeaders, ApiType type) {
                super.onResponse(response, linkHeaders, type);
                if(response.isSuccessful()) {
                    if(getViewCallback() != null) {
                        //we need to update this item since the api returns nothing
                        mConversation.setWorkflowState("unread");
                        getViewCallback().onConversationMarkedAsUnread(mPosition);
                    }
                }
            }
        });
    }
    private void showUserMessage(int userMessageResId) {
        if (getViewCallback() != null) {
            getViewCallback().showUserMessage(userMessageResId);
        }
    }

    public ArrayList<Message> getMessageChainForMessage(Message message) {
        int idx = getData().indexOf(message);
        ArrayList<Message> messageChain = new ArrayList<>();
        for (int i = idx; i >= 0; i--) {
            messageChain.add(getData().get(i));
        }
        return messageChain;
    }

    public ArrayList<BasicUser> getParticipants() {
        return new ArrayList<>(mParticipants.values());
    }

    @Override
    public int compare(Message message1, Message message2) {
        if(message1 != null && message1.getComparisonDate() != null && message2 != null && message2.getComparisonDate() != null) {
            return message2.getComparisonDate().compareTo(message1.getComparisonDate());
        }
        return super.compare(message1, message2);
    }

    @Override
    public boolean areContentsTheSame(Message oldItem, Message newItem) {
        return areItemsTheSame(oldItem, newItem);
    }

    @Override
    public boolean areItemsTheSame(Message item1, Message item2) {
        return item1.getId() == item2.getId();
    }

}
