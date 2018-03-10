/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.candroid.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.instructure.candroid.R;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

public class SettingsFragment extends OrientationChangeFragment implements
        DatePickerFragment.DatePickerCancelListener {


    @Override
    public void onCancel() {}
    private View rootView;

    private Course course;

    private ViewFlipper viewFlipper;


    //View the Settings
    private TextView courseName;
    private TextView courseCode;

    private LinearLayout startAtLayout;
    private TextView startAt;

    private LinearLayout endAtLayout;
    private TextView endAt;

    private TextView license;
    private TextView visibility;

    private LinearLayout editStartAtLayout;
    private TextView editStartDate;
    private ImageView editStartAt;

    private LinearLayout editEndAtLayout;
    private TextView editEndDate;
    private ImageView editEndAt;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.settings);
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.settings_fragment_layout, container, false);

        viewFlipper = (ViewFlipper) rootView.findViewById(R.id.view_flipper);

        //View the Settings
        courseName = (TextView) rootView.findViewById(R.id.course_name);
        courseCode = (TextView) rootView.findViewById(R.id.course_code);
        startAt = (TextView) rootView.findViewById(R.id.start_date);
        startAtLayout = (LinearLayout) rootView.findViewById(R.id.starts_layout);

        endAt = (TextView) rootView.findViewById(R.id.end_date);
        endAtLayout = (LinearLayout) rootView.findViewById(R.id.ends_layout);

        license = (TextView) rootView.findViewById(R.id.license_string);
        visibility = (TextView) rootView.findViewById(R.id.visibility_string);


        editStartAt = (ImageView) rootView.findViewById(R.id.edit_start_date);
        editStartAt.setImageDrawable(CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_calendar_fill, course));

        editStartDate = (TextView) rootView.findViewById(R.id.tvStartDate);

        if(course.getStartDate() != null) {
            editStartDate.setText(DateHelper.getShortDate(getActivity(), course.getStartDate()));
        } else {
            editStartDate.setText(getString(R.string.noDate));
        }
        editEndAt = (ImageView) rootView.findViewById(R.id.edit_end_date);
        editEndAt.setImageDrawable(CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_calendar_fill, course));

        editEndDate = (TextView) rootView.findViewById(R.id.tvEndDate);

        if(course.getEndDate() != null) {
            editEndDate.setText(DateHelper.getShortDate(getActivity(), course.getEndDate()));
        } else {
            editEndDate.setText(getString(R.string.noDate));
        }
        editEndAtLayout = (LinearLayout) rootView.findViewById(R.id.edit_ends_layout);
        editStartAtLayout = (LinearLayout) rootView.findViewById(R.id.edit_starts_layout);


        viewFlipper.setInAnimation(getContext(), R.anim.fade_in_quick);
        viewFlipper.setOutAnimation(getContext(), R.anim.fade_out_quick);

        resetCourseData();

        return rootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {


        if(getArguments() != null) {
            getArguments().putBoolean(Const.IN_EDIT_MODE, isEditMode());
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCallbackFinished(ApiType type) {
        if(type.isAPI()) {
            hideProgressBar();
        }
    }

    public boolean isEditMode() {
        return viewFlipper.getDisplayedChild() != 0;
    }

    public void resetCourseData() {
        courseName.setText(course.getName());

        courseCode.setText(course.getCourseCode());

        if (course.getStartDate() != null) {
            startAt.setText(DateHelper.dateToDayMonthYearString(getContext(), course.getStartDate()));
        } else {
            startAtLayout.setVisibility(View.GONE);
            editStartAtLayout.setVisibility(View.GONE);
        }

        if (course.getEndDate() != null) {
            endAt.setText(DateHelper.dateToDayMonthYearString(getContext(), course.getEndDate()));

        } else {
            endAtLayout.setVisibility(View.GONE);
            editEndAtLayout.setVisibility(View.GONE);
        }

        license.setText(course.getLicensePrettyPrint());

        if (course.isPublic()) {
            visibility.setText(getString(R.string.publiclyAvailable));
        } else {
            visibility.setText(getString(R.string.privatelyAvailable));
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle bundle) {
        super.handleIntentExtras(bundle);

        if(getCanvasContext() instanceof Course) {
            course = (Course) getCanvasContext();
        }
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}
