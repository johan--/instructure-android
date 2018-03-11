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

package com.ebuki.homework.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ebuki.homework.R;
import com.ebuki.homework.adapter.MasteryPathOptionsRecyclerAdapter;
import com.ebuki.homework.delegate.Navigation;
import com.ebuki.homework.interfaces.AdapterToFragmentCallback;
import com.ebuki.homework.util.FragUtils;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.ModuleManager;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.AssignmentSet;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.MasteryPathAssignment;
import com.instructure.canvasapi2.models.MasteryPathSelectResponse;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.utils.Const;


public class MasteryPathOptionsFragment extends ParentFragment {

    private View mRootView;
    private Button mSelect;

    private MasteryPathOptionsRecyclerAdapter mRecyclerAdapter;
    private MasteryPathAssignment[] mAssignments;
    private AssignmentSet mAssignmentSet;
    private StatusCallback<MasteryPathSelectResponse> mSelectOptionCallback;
    private long mModuleObjectId;
    private long mModuleItemId;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRootView = getLayoutInflater().inflate(R.layout.fragment_mastery_paths_options, container, false);
        mRecyclerAdapter = new MasteryPathOptionsRecyclerAdapter(getContext(), getCanvasContext(), mAssignments, new AdapterToFragmentCallback<Assignment>() {
            @Override
            public void onRowClicked(Assignment assignment, int position, boolean isOpenDetail) {
                Navigation navigation = getNavigation();
                if (navigation != null){
                    Bundle bundle = AssignmentBasicFragment.createBundle(getCanvasContext(), assignment);
                    navigation.addFragment(
                            FragUtils.getFrag(AssignmentBasicFragment.class, bundle));
                }
            }

            @Override
            public void onRefreshFinished() {

            }
        });
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);

        //disable the swiperefreshlayout because we don't want to pull to refresh. It doesn't make an API call, so it wouldn't refresh anything
        mRootView.findViewById(R.id.swipeRefreshLayout).setEnabled(false);

        mSelect = (Button) mRootView.findViewById(R.id.select_option);

        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ModuleManager.selectMasteryPath(getCanvasContext(),mModuleObjectId, mModuleItemId, mAssignmentSet.getId(), mSelectOptionCallback);
            }
        });

        setupCallbacks();

        return mRootView;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.choose_assignment_group);
    }


    private void setupCallbacks() {
        mSelectOptionCallback = new StatusCallback<MasteryPathSelectResponse>() {
            @Override
            public void onResponse(retrofit2.Response<MasteryPathSelectResponse> response, LinkHeaders linkHeaders, ApiType type) {
                //we have successfully selected the module. Now go back and refresh the list
                Navigation navigation = getNavigation();
                ModuleListFragment moduleListFragment = null;
                if(navigation.getPeekingFragment() instanceof ModuleListFragment) {
                    //the top fragment is the course module progression, the next one (peeking fragment) is the ModuleListFragment. We need
                    //to refresh that because they now have selected something.
                    moduleListFragment = ((ModuleListFragment)navigation.getPeekingFragment());
                }
                navigation.popCurrentFragment();

                if(moduleListFragment != null) {
                    moduleListFragment.refreshModuleList();
                }
            }
        };
    }

    // region Intent
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if(extras == null){return;}
        mAssignments = (MasteryPathAssignment[])extras.getParcelableArray(Const.ASSIGNMENT);
        mAssignmentSet = extras.getParcelable(Const.ASSIGNMENT_SET);
        mModuleObjectId = extras.getLong(Const.MODULE_ID);
        mModuleItemId = extras.getLong(Const.MODULE_ITEM);
    }

    public static Bundle createBundle(CanvasContext canvasContext, MasteryPathAssignment[] assignments, AssignmentSet assignmentSet, long moduleObjectId, long moduleItemId) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putParcelableArray(Const.ASSIGNMENT, assignments);
        bundle.putParcelable(Const.ASSIGNMENT_SET, assignmentSet);
        bundle.putLong(Const.MODULE_ID, moduleObjectId);
        bundle.putLong(Const.MODULE_ITEM, moduleItemId);
        return bundle;
    }
    // endregion


    @Override
    public boolean allowBookmarking() {
        return false;
    }
}
