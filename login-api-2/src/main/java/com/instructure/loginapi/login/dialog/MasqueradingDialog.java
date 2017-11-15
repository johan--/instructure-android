/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.loginapi.login.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.instructure.loginapi.login.R;

public class MasqueradingDialog extends DialogFragment {

    private static final String DOMAIN = "domain";
    private static final String CURRENTLY_MASQUERADING = "currently_masquerading";

    private OnMasqueradingSet mCallback;
    private EditText mDomain, mUserId;

    public interface OnMasqueradingSet {
        void onStartMasquerading(String domain, Long userId);
        void onStopMasquerading();
    }

    public static MasqueradingDialog get(Fragment...target) {
        MasqueradingDialog dialog = new MasqueradingDialog();
        if(target != null && target.length > 0) {
            dialog.setTargetFragment(target[0], 1);
        }
        return dialog;
    }

    public static MasqueradingDialog get(String domain, boolean isCurrentlyMasquerading, Fragment...target) {
        MasqueradingDialog dialog = new MasqueradingDialog();
        if(target != null && target.length > 0) {
            dialog.setTargetFragment(target[0], 1);
        }
        Bundle args = new Bundle();
        args.putString(DOMAIN, domain);
        args.putBoolean(CURRENTLY_MASQUERADING, isCurrentlyMasquerading);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnMasqueradingSet) {
            mCallback = (OnMasqueradingSet) context;
        } else {
            if(getTargetFragment() instanceof OnMasqueradingSet) {
                mCallback = (OnMasqueradingSet) getTargetFragment();
            } else {
                throw new IllegalStateException("Context required to implement MasqueradingDialog.OnMasqueradingSet callback");
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.masquerade);
        View root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_masquerading, null);
        mDomain = (EditText) root.findViewById(R.id.domain);
        mUserId = (EditText) root.findViewById(R.id.userId);
        final Button stopMasqueradingButton = (Button) root.findViewById(R.id.stopMasqueradingButton);
        builder.setView(root);

        boolean isCurrentlyMasquerading = getArguments().getBoolean(CURRENTLY_MASQUERADING, false);
        if(isCurrentlyMasquerading) {
            mDomain.setEnabled(false);
            mUserId.setEnabled(false);
            stopMasqueradingButton.setVisibility(View.VISIBLE);
            stopMasqueradingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCallback != null) {
                        mCallback.onStopMasquerading();
                        getDialog().dismiss();
                    }
                }
            });
        } else {
            mDomain.setEnabled(true);
            mUserId.setEnabled(true);
        }

        builder.setPositiveButton(R.string.masqueradingBegin, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(TextUtils.isEmpty(mUserId.getText())) {
                    Toast.makeText(getContext(), R.string.masqueradeErrorUserId, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(mDomain.getText())) {
                    Toast.makeText(getContext(), R.string.masqueradeErrorDomain, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mCallback != null && mDomain.isEnabled() && mUserId.isEnabled()) {
                    mCallback.onStartMasquerading(validateDomain(mDomain.getText().toString()), Long.parseLong(mUserId.getText().toString()));
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private String validateDomain(String domain) {
        String url = domain.toLowerCase().replace(" ", "");

        //if there are no periods, append .instructure.com
        if (!url.contains(".") || url.endsWith(".beta")) {
            url += ".instructure.com";
        }

        //URIs need to to start with a scheme.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        //Get just the host.
        Uri uri = Uri.parse(url);
        if(uri.getHost() != null) {
            url = uri.getHost();
        }

        //Strip off www. if they typed it.
        if (url.startsWith("www.")) {
            url = url.substring(4);
        }
        return url;
    }
}
