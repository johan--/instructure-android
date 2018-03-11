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
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.pandarecycler.PaginatedRecyclerAdapter;
import com.instructure.pandarecycler.util.UpdatableSortedList;

import java.util.Arrays;
import java.util.List;


public abstract class BaseListRecyclerAdapter<MODEL extends CanvasComparable, T extends RecyclerView.ViewHolder> extends PaginatedRecyclerAdapter<T> {
    private static final int DEFAULT_LIST_SIZE = 50; // List will increase in size automatically

    public abstract void bindHolder(MODEL model, T holder, int position);

    private UpdatableSortedList<MODEL> mList;
    private SortedList.Callback<MODEL> mCallback;
    private ItemComparableCallback<MODEL> mItemCallback;
    private long mSelectedItemId;


    public static abstract class ItemComparableCallback<MDL extends CanvasComparable> { // Provides optional overrides
        public int compare(MDL o1, MDL o2) {
            return o1.compareTo(o2);
        }
        public boolean areContentsTheSame(MDL oldItem, MDL newItem) {
            return false;
        }
        public boolean areItemsTheSame(MDL item1, MDL item2) {
            return item1.getId() == item2.getId();
        }
        public long getUniqueItemId(MDL mdl) {
            return mdl.getId();
        }
    }

    public BaseListRecyclerAdapter(Context context, Class<MODEL> klazz) {
        super(context);
        mItemCallback = new ItemComparableCallback<MODEL>() {};
        mCallback = new SortedList.Callback<MODEL>() {
            @Override
            public int compare(MODEL o1, MODEL o2) {
                return mItemCallback.compare(o1, o2);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(MODEL oldItem, MODEL newItem) {
                if (mItemCallback != null) {
                    return mItemCallback.areContentsTheSame(oldItem, newItem);
                }
                return false;
            }

            @Override
            public boolean areItemsTheSame(MODEL item1, MODEL item2) {
                if (mItemCallback != null) {
                    return mItemCallback.areItemsTheSame(item1, item2);
                }
                return item1.getId() == item2.getId();
            }
        };
        mList = new UpdatableSortedList<MODEL>(klazz, mCallback, new UpdatableSortedList.ItemCallback<MODEL>() {
            @Override
            public long getId(MODEL model) {
                return mItemCallback.getUniqueItemId(model);
            }
        }, DEFAULT_LIST_SIZE);
        setupCallbacks();
    }

    public BaseListRecyclerAdapter(Context context, Class<MODEL> klazz, List items) {
        this(context, klazz);
        addAll(items);
    }

    @Override
    public void onBindViewHolder(T baseHolder, int position) {
        super.onBindViewHolder(baseHolder, position);
        if (position < mList.size()) {
            bindHolder(mList.get(position), baseHolder, position);
        }
    }

    // region Selection


    @Override
    public int getSelectedPosition() {
        return mList.indexOfItemById(mSelectedItemId);
    }

    @Override
    public void setSelectedPosition(int position) {
        if (position == -1) { return; }
        if (mSelectedItemId != -1) {
            int oldPosition = mList.indexOfItemById(mSelectedItemId);
            if (oldPosition != UpdatableSortedList.NOT_IN_LIST) {
                notifyItemChanged(oldPosition);
            }
        }
        mSelectedItemId = mItemCallback.getUniqueItemId(getItemAtPosition(position));
        notifyItemChanged(position);
        super.setSelectedPosition(position);
    }

    public boolean isItemSelected(MODEL model) {
        return mItemCallback.getUniqueItemId(model) == mSelectedItemId;
    }

    @Override
    public void setSelectedItemId(long selectedItemId) {
        this.mSelectedItemId = selectedItemId;
    }

    // endregion

    // region Pagination

    public void onCallbackFinished() {
        setLoadedFirstPage(true);
        shouldShowLoadingFooter();
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            adapterToRecyclerViewCallback.setDisplayNoConnection(false);
            getAdapterToRecyclerViewCallback().setIsEmpty(isAllPagesLoaded() && size() == 0);
        }
    }

    public void onNoNetwork() {
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            int size = size();
            adapterToRecyclerViewCallback.setDisplayNoConnection(size == 0);
            adapterToRecyclerViewCallback.setIsEmpty(size == 0);
        }
    }
    // endregion

    // region MODEL Helpers

    /**
     * The loading footer from pagination will be position == size(). So it'll be index out of bounds.
     *  Perform a check before calling getItemAtPosition or make sure isPaginated() returns false
     * @param position
     * @return
     */
    public MODEL getItemAtPosition(int position) {
        return mList.get(position);
    }

    public int indexOf(MODEL item) {
        return mList.indexOf(item);
    }

    public void add(MODEL item) {
        mList.addOrUpdate(item);
    }

    public void addAll(List<MODEL> items) {
        mList.beginBatchedUpdates();
        for (MODEL item : items) {
            add(item);
        }
        mList.endBatchedUpdates();
    }

    public void addAll(MODEL[] items) {
        addAll(Arrays.asList(items));
    }

    public void removeItemAt(int position) {
        mList.removeItemAt(position);
    }

    public void remove(MODEL item) { mList.remove(item); }

    @Override
    public void clear() {
        //remove items at end, to avoid unnecessary ARRAY SHIFTING MADNESS
        while (mList.size() > 0) {
            mList.removeItemAt(mList.size() - 1);
        }
        notifyDataSetChanged();
    }

    @Override
    public int size(){
        return mList.size();
    }
    // endregion

    // region Getter & Setters
    public ItemComparableCallback<MODEL> getItemCallback() {
        return mItemCallback;
    }

    public void setItemCallback(ItemComparableCallback<MODEL> itemCallback) {
        this.mItemCallback = itemCallback;
    }
    // endregion
}
