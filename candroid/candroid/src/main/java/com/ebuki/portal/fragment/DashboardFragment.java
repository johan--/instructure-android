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

import com.ebuki.portal.R;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.util.FragUtils;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;

public class DashboardFragment extends ParentFragment {

    private View mRootView;
    public boolean ignoreDebounce = false;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.MASTER;
    }

    @Override
    public String getFragmentTitle() {
        if (isAdded()) {
            return getString(R.string.dashboard);
        } else {
            return "";
        }
    }

    @Override
    public boolean navigationContextIsCourse() {
        return false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = getLayoutInflater().inflate(R.layout.dashboard_fragment, container, false);

        final ImageView ivHomework = mRootView.findViewById(R.id.ivHomework);
        ivHomework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Navigation navigation = getNavigation();
                if (navigation != null) {
                    navigation.addFragment(FragUtils.getFrag(ToDoListFragment.class, getActivity()), Navigation.NavigationPosition.TODO, ignoreDebounce);
                    // navigation.addFragment(FragUtils.getFrag(ToDoListFragment.class, bundle));
                }
            }
        });

        final ImageView ivTextbooks = mRootView.findViewById(R.id.ivTextbooks);
        ivTextbooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), getString(R.string.textbooksSelected), Toast.LENGTH_SHORT).show();
                // String packageName="net.nightwhistler.pageturner";
                // String packageName="com.skytree.skyreader";
                String packageName="com.gitden.epub.reader.app";
                launchApp(packageName);
            }
        });

        final ImageView ivCalendar = mRootView.findViewById(R.id.ivCalendar);
        ivCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Navigation navigation = getNavigation();
                if (navigation != null) {
                    navigation.addFragment(FragUtils.getFrag(CalendarListViewFragment.class, bundle));
                }
            }
        });

        final ImageView ivMynotes = mRootView.findViewById(R.id.ivMynotes);
        ivMynotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "My notes selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="it.feio.android.omninotes.foss";
                launchApp(packageName);

            }
        });

        final ImageView ivCamera = mRootView.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "Camera selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="com.mediatek.camera";
                launchApp(packageName);
            }
        });

        final ImageView ivCalculator = mRootView.findViewById(R.id.ivCalculator);
        ivCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "Calculator selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="com.android.calculator2";
                launchApp(packageName);
            }
        });

        final ImageView ivWikipedia = mRootView.findViewById(R.id.ivWikipedia);
        ivWikipedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "Wikipedia selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="org.wikipedia";
                launchApp(packageName);
            }
        });

//        final ImageView ivPlaystore = mRootView.findViewById(R.id.ivPlaystore);
//        ivPlaystore.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                launchPlayStore();
//            }
//        });

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

    // Custom method to launch an app
    public void launchPlayStore() {

        Intent intent = new Intent(Intent.ACTION_MAIN);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        if (intent != null) {
            startActivity(intent);
        }

    }

    // Custom method to launch an app
    public void launchApp(String packageName) {

        PackageManager pm = getActivity().getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);

//
//        List<ResolveInfo> activities = pm.queryIntentActivities(intent,
//                PackageManager.MATCH_DEFAULT_ONLY);
//
//        boolean isIntentSafe = activities.size() > 0;


        try {

            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            intent = pm.getLaunchIntentForPackage(packageName);

            if (intent == null) {
                throw new PackageManager.NameNotFoundException();
            } else {
                startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getActivity(), "Application not found", Toast.LENGTH_LONG).show();
            Log.e("Launch", e.getMessage());
        }
    }


//    private static boolean isIntentAvailable(Context context, String action) {
//        final PackageManager packageManager = context.getPackageManager();
//        final Intent intent = new Intent(action);
//        List<ResolveInfo> list =
//                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        return list.size() > 0;
//    }

}
















