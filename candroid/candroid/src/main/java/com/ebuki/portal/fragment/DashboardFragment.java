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
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.ebuki.portal.R;

import android.text.TextUtils;


public class DashboardFragment extends ParentFragment {

    private View mRootView;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.MASTER;
    }

    @Override
    public String getFragmentTitle() {
        if(isAdded()) {
            return getString(R.string.dashboard);
        } else {
            return "";
        }
    }

    @Override
    public boolean navigationContextIsCourse()
    {
        return false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = getLayoutInflater().inflate(R.layout.dashboard_fragment, container, false);

        final ImageView ivHomework = mRootView.findViewById(R.id.ivHomework);
        ivHomework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), getString(R.string.homeworkSelected), Toast.LENGTH_LONG).show();
            }
        });

        final ImageView ivTextbooks = mRootView.findViewById(R.id.ivTextbooks);
        ivTextbooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), getString(R.string.textbooksSelected), Toast.LENGTH_LONG).show();
            }
        });


        return mRootView;
    }


    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        setupTitle(getString(R.string.dashboard));
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

}
















