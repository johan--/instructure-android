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

import com.ebuki.homework.binders.RecipientBinder;
import com.ebuki.homework.holders.RecipientViewHolder;
import com.ebuki.homework.interfaces.RecipientAdapterToFragmentCallback;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.RecipientManager;
import com.instructure.canvasapi2.models.Recipient;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.HashMap;
import java.util.List;

public class ChooseMessageRecipientRecyclerAdapter extends BaseListRecyclerAdapter<Recipient, RecyclerView.ViewHolder> {
    private RecipientAdapterToFragmentCallback<Recipient> mAdapterToFragmentCallback;
    private StatusCallback<List<Recipient>> mRecipientCallback;

    private boolean mIsLastURLFetched = false; // Prevents the lastPage from being fetched in an endless loop

    // region Order Work-around
    // Since BaseListRecyclerAdapter uses a sorted list to store the list items, there has to be something to order them by.
    // Recipients have no clear way to order (Can't do by name, because the Last name isn't always in a consistent spot)
    // Since a hash is pretty easy, it made more sense than to create another BaseListRA that had a different representation.
    private HashMap<String, Integer> mInsertedOrderHash = new HashMap<>();
    // endregion
    private int mInsertCount = 0;
    private String mSearchTerm = "";
    private String mContextId = "";
    private Recipient mCurrentRecipient;

    /* This is the real constructor and should be called to create instances of this adapter */
    public ChooseMessageRecipientRecyclerAdapter(Context context, String contextId, Recipient currentRecipient, RecipientAdapterToFragmentCallback<Recipient> adapterToFragmentCallback) {
        this(context, contextId, currentRecipient, adapterToFragmentCallback, true);
    }

    /* This overloaded constructor is for testing purposes ONLY, and should not be used to create instances of this adapter. */
    protected ChooseMessageRecipientRecyclerAdapter(Context context, String contextId, Recipient currentRecipient, RecipientAdapterToFragmentCallback<Recipient> adapterToFragmentCallback, boolean isLoadData) {
        super(context, Recipient.class);
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mContextId = contextId;
        mCurrentRecipient = currentRecipient;
        setItemCallback(new ItemComparableCallback<Recipient>() {
            @Override
            public int compare(Recipient o1, Recipient o2) {
                return mInsertedOrderHash.get(o1.getStringId()) - mInsertedOrderHash.get(o2.getStringId());
            }

            @Override
            public boolean areContentsTheSame(Recipient item1, Recipient item2) {
                return item1.getName().equals(item2.getName());
            }

            @Override
            public boolean areItemsTheSame(Recipient item1, Recipient item2) {
                return item1.getStringId().equals(item2.getStringId());
            }

            @Override
            public long getUniqueItemId(Recipient recipient) {
                return recipient.getStringId().hashCode();
            }
        });
        if(isLoadData){
            loadData();
        }
    }

    @Override
    public void bindHolder(Recipient recipient, RecyclerView.ViewHolder holder, int position) {
        RecipientBinder.bind(getContext(), (RecipientViewHolder) holder, recipient, mAdapterToFragmentCallback, mAdapterToFragmentCallback.isRecipientSelected(recipient));
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        return new RecipientViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return RecipientViewHolder.holderResId();
    }

    @Override
    public void contextReady() {

    }

    // region Pagination

    @Override
    public void refresh() {
        super.refresh();
        mInsertCount = 0;
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void loadFirstPage() {
        RecipientManager.searchRecipients(mSearchTerm, getContextID(), mRecipientCallback);
    }

    @Override
    public void loadNextPage(String nextURL) {
        RecipientManager.searchRecipients(mSearchTerm, getContextID(), mRecipientCallback);
    }

    @Override
    public void setupCallbacks() {
        mRecipientCallback = new StatusCallback<List<Recipient>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Recipient>> response, LinkHeaders linkHeaders, ApiType type) {
                for (Recipient recipient : response.body()) {
                    mInsertedOrderHash.put(recipient.getStringId(), mInsertCount++);
                    add(recipient);
                }
                mAdapterToFragmentCallback.onRefreshFinished();

                String nextUrl = linkHeaders.nextUrl;
                // Deals with an edge case of not getting the last page. The search API only sends a lastURL
                //   instead of a nextURL for the last page.
                if (linkHeaders.nextUrl == null && !mIsLastURLFetched) {
                    nextUrl = linkHeaders.lastUrl;
                    linkHeaders.nextUrl = nextUrl;
                    mIsLastURLFetched = true;
                }
                setNextUrl(nextUrl);
            }

        };
    }

    // endregion

    // region Api helpers
    private String getContextID() {
        if(mCurrentRecipient == null || mCurrentRecipient.getStringId() == null) {
            return mContextId;
        } else {
            return mCurrentRecipient.getStringId();
        }
    }
    // endregion

}
