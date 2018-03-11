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
import android.view.View;

import com.ebuki.homework.binders.FileBinder;
import com.ebuki.homework.holders.FileViewHolder;
import com.ebuki.homework.interfaces.AdapterToFragmentLongClick;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.FileFolderManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.FileFolder;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FileListRecyclerAdapter extends BaseListRecyclerAdapter<FileFolder, FileViewHolder>{

    //region Models
    private long mCurrentFolderId = 0;
    private String mFolderName;
    private FileFolder mLongClickItem;
    private CanvasContext mCanvasContext;
    private boolean mIsFilesAllPagesLoaded = false;
    private boolean mIsFoldersAllPagesLoaded = false;
    //endregion

    //region Callbacks
    private StatusCallback<List<FileFolder>> mFolderCallback;
    private StatusCallback<List<FileFolder>> mFileCallback;
    private ArrayList<FileFolder> mDeletedFileFolders = new ArrayList<FileFolder>();
    private AdapterToFragmentLongClick<FileFolder> mAdaptertoFragmentLongCLick;
    //endregion

    /* This is the real constructor and should be called to create instances of this adapter */
    public FileListRecyclerAdapter(Context context, CanvasContext canvasContext,
        long folderId, String folderName,
        AdapterToFragmentLongClick<FileFolder> adapterToFragmentLongClick) {
        this(context, canvasContext, folderId, folderName, adapterToFragmentLongClick, true);
    }

    /* This overloaded constructor is for testing purposes ONLY, and should not be used to create instances of this adapter. */
    protected FileListRecyclerAdapter(Context context, CanvasContext canvasContext,
                                   long folderId, String folderName,
                                   AdapterToFragmentLongClick<FileFolder> adapterToFragmentLongClick, boolean isLoadData) {
        super(context, FileFolder.class);
        setItemCallback(new ItemComparableCallback<FileFolder>() {
            @Override
            public int compare(FileFolder o1, FileFolder o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(FileFolder item1, FileFolder item2) {
                return compareFileFolders(item1, item2);
            }

            @Override
            public boolean areItemsTheSame(FileFolder item1, FileFolder item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(FileFolder fileFolder) {
                return fileFolder.getId();
            }
        });

        mCanvasContext = canvasContext;
        mAdaptertoFragmentLongCLick = adapterToFragmentLongClick;
        mCurrentFolderId = folderId;
        mFolderName = folderName;

        if(isLoadData){
            loadData();
        }
    }

    @Override
    public void bindHolder(FileFolder baseItem, FileViewHolder holder, int position) {
        FileBinder.bind(holder, baseItem, getContext(), mCanvasContext, mAdaptertoFragmentLongCLick);
    }

    @Override
    public FileViewHolder createViewHolder(View v, int viewType) {
        return new FileViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return FileViewHolder.holderResId();
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void contextReady() {

    }

    //region Setup Callbacks
    @Override
    public void setupCallbacks() {
        mFolderCallback = new StatusCallback<List<FileFolder>>() {

            @Override
            public void onResponse(retrofit2.Response<List<FileFolder>> response, LinkHeaders linkHeaders, ApiType type) {

                mIsFoldersAllPagesLoaded = linkHeaders.nextUrl == null;
                mAdaptertoFragmentLongCLick.onRefreshFinished();
                setNextUrl(linkHeaders.nextUrl);

                addAll(response.body());
                if(linkHeaders.nextUrl == null){
                    getFiles(type == ApiType.API);
                }
            }

            @Override
            public void onFail(Call<List<FileFolder>> response, Throwable error) {
                mAdaptertoFragmentLongCLick.onRefreshFinished();
            }
        };

        mFileCallback = new StatusCallback<List<FileFolder>>() {

            @Override
            public void onResponse(retrofit2.Response<List<FileFolder>> response, LinkHeaders linkHeaders, ApiType type) {
                mIsFilesAllPagesLoaded = linkHeaders.nextUrl == null;
                mAdaptertoFragmentLongCLick.onRefreshFinished();
                setNextUrl(linkHeaders.nextUrl);

                addAll(response.body());
            }

            @Override
            public void onFail(Call<List<FileFolder>> callResponse, Throwable error, Response response) {
                mAdaptertoFragmentLongCLick.onRefreshFinished();
            }
        };
    }
    //endregion

    @Override
    public void loadFirstPage() {
        //First request all folders, folders callback will call files
        if (mCurrentFolderId > 0) {
            FileFolderManager.getFirstPageFolders(mCurrentFolderId, true, mFolderCallback);
        } else {
            FileFolderManager.getFirstPageFoldersRoot(mCanvasContext, true, mFolderCallback);
        }
    }

    private void getFiles(boolean isNetwork) {
        if (mCurrentFolderId > 0) {
            FileFolderManager.getFirstPageFiles(mCurrentFolderId, isNetwork, mFileCallback);
        } else {
            FileFolderManager.getFirstPageFilesRoot(mCanvasContext, isNetwork, mFileCallback);
        }
    }

    @Override
    public void loadNextPage(String nextURL) {
        //The FileFolderAPI request works for both files and folders
        FileFolderManager.getNextPageFilesFolder(nextURL, true, mIsFoldersAllPagesLoaded ? mFileCallback : mFolderCallback);
    }

    private void removeDeletedFileFolders() {
        for (FileFolder fileFolder : mDeletedFileFolders) {
            remove(fileFolder);
        }
    }

    @Override
    public boolean shouldShowLoadingFooter() {
        //override here to let both api calls properly display pagination loader
        return (!mIsFilesAllPagesLoaded && size() > 0) && isPaginated();
    }

    @Override
    public boolean isAllPagesLoaded() {
        return mIsFilesAllPagesLoaded && mIsFoldersAllPagesLoaded;
    }

    //region GETTERS/SETTERS
    public void setCurrentFolderId(long id){
        mCurrentFolderId = id;
    }

    public long getCurrentFolderId(){
        return mCurrentFolderId;
    }

    public void setCurrentFolderName(String name){
        mFolderName = name;
    }

    public String getCurrentFolderName(){
        return mFolderName;
    }

    public void setLongClickItem(FileFolder fileFolder){
        mLongClickItem = fileFolder;
    }

    public FileFolder getLongClickItem(){
        return mLongClickItem;
    }

    public ArrayList<FileFolder> getDeletedFileFolders(){
        return mDeletedFileFolders;
    }
    //endregion

    private boolean compareFileFolders(FileFolder oldItem, FileFolder newItem){
        //object items
        if(oldItem.getDisplayName() != null && newItem.getDisplayName() != null){
            boolean sameName = oldItem.getDisplayName().equals(newItem.getDisplayName());
            boolean sameSize = oldItem.getSize() == newItem.getSize();
            return sameName && sameSize;
        }

        //folder objects
        if(oldItem.getName() != null && newItem.getName() != null){
            boolean sameName = oldItem.getName().equals(newItem.getName());
            boolean sameSize = oldItem.getSize() == newItem.getSize();
            return sameName && sameSize;
        }

        //if old and new aren't one of the same object types then contents have changed
        return false;
    }
}
