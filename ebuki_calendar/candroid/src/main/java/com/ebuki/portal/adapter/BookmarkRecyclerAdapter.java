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

import android.content.Context;
import android.view.View;

import com.ebuki.portal.binders.BookmarkBinder;
import com.ebuki.portal.holders.BookmarkViewHolder;
import com.ebuki.portal.interfaces.BookmarkAdapterToFragmentCallback;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.BookmarkManager;
import com.instructure.canvasapi2.models.Bookmark;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.List;

import retrofit2.Call;

public class BookmarkRecyclerAdapter extends BaseListRecyclerAdapter<Bookmark, BookmarkViewHolder> {

    private StatusCallback<List<Bookmark>> bookmarksCallback;
    private BookmarkAdapterToFragmentCallback<Bookmark> mAdapterToFragmentCallback;
    private boolean mIsShortcutActivity = false;

    public BookmarkRecyclerAdapter(Context context, boolean isShortcutActivity, BookmarkAdapterToFragmentCallback<Bookmark> mAdapterToFragmentCallback) {
        super(context, Bookmark.class);
        mIsShortcutActivity = isShortcutActivity;
        setItemCallback(new ItemComparableCallback<Bookmark>() {
            @Override
            public int compare(Bookmark o1, Bookmark o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }

            @Override
            public boolean areContentsTheSame(Bookmark item1, Bookmark item2) {
                return item1.getName().toLowerCase().equals(item2.getName().toLowerCase());
            }

            @Override
            public long getUniqueItemId(Bookmark bookmark) {
                return bookmark.getId();
            }
        });
        this.mAdapterToFragmentCallback = mAdapterToFragmentCallback;
        setupCallbacks();
        loadData();
    }

    @Override
    public void bindHolder(Bookmark bookmark, BookmarkViewHolder holder, int position) {
        BookmarkBinder.bind(getContext(), mIsShortcutActivity, holder, bookmark, mAdapterToFragmentCallback);
    }

    @Override
    public BookmarkViewHolder createViewHolder(View v, int viewType) {
        return new BookmarkViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return BookmarkViewHolder.holderResId();
    }

    @Override
    public void setupCallbacks() {
        bookmarksCallback = new StatusCallback<List<Bookmark>>() {

            @Override
            public void onResponse(retrofit2.Response<List<Bookmark>> response, LinkHeaders linkHeaders, ApiType type) {
                addAll(response.body());
                setNextUrl(linkHeaders.nextUrl);
                mAdapterToFragmentCallback.onRefreshFinished();
            }

            @Override
            public void onFail(Call<List<Bookmark>> callResponse, Throwable error, retrofit2.Response response) {
                if (response != null && !APIHelper.isCachedResponse(response) || !APIHelper.hasNetworkConnection()) {
                    getAdapterToRecyclerViewCallback().setIsEmpty(true);
                }
            }

            @Override
            public void onFinished(ApiType type) {
                BookmarkRecyclerAdapter.this.onCallbackFinished();
            }
        };
    }

    @Override
    public void loadData() {
        BookmarkManager.getBookmarks(bookmarksCallback, isRefresh());
    }
}
