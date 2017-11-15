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

package com.instructure.candroid.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.instructure.candroid.R;
import com.instructure.candroid.fragment.ParentFragment;

public class DataLossFragmentStyled extends DialogFragment {

    public static final String TAG = "dataLossFragment";
    public static final int DATA_LOSS_FRAGMENT_STYLED_TARGET = 8291;

    public static void show(ParentFragment fragment) {
        DataLossFragmentStyled dataLossFragment = new DataLossFragmentStyled();
        dataLossFragment.setTargetFragment(fragment, DATA_LOSS_FRAGMENT_STYLED_TARGET);
        dataLossFragment.show(fragment.getFragmentManager(), TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.unsavedProgress)
                .setMessage(R.string.informationLost)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                        dismissAllowingStateLoss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                        dismissAllowingStateLoss();
                    }
                })
                .create();
    }
}