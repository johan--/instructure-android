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
package com.instructure.loginapi.login.api.zendesk.utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.ErrorReportManager;
import com.instructure.canvasapi2.models.ErrorReportResult;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.loginapi.login.R;
import com.instructure.loginapi.login.util.Const;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * NOTE: this no longer uses Zendesk. There is an api from canvas that we can post to and the endpoint
 * will take care of the help desk client that customer support is currently using.
 */
public class ZendeskDialogStyled extends DialogFragment {

    public interface ZendeskDialogResultListener {
        void onTicketPost();
        void onTicketError();
    }

    private static final String DEFAULT_DOMAIN = "ebuki.nxtstepdsgn.com";
    public static final String TAG = "zendeskDialog";
    private static final int customFieldTag = 20470321;
    private EditText descriptionEditText;
    private EditText subjectEditText;
    private EditText emailAddressEditText;
    private TextView emailAddress;
    private Spinner severitySpinner;

    private boolean fromLogin;
    private boolean mUseDefaultDomain;

    private ZendeskDialogResultListener resultListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            resultListener = (ZendeskDialogResultListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ZendeskDialogResultListener");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        //check to see if there are custom colors we need to set
        handleBundle();

        // Create View
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_zendesk_ticket, null);
        builder.setView(view);

        subjectEditText = (EditText) view.findViewById(R.id.subjectEditText);
        descriptionEditText = (EditText) view.findViewById(R.id.descriptionEditText);
        emailAddressEditText = (EditText) view.findViewById(R.id.emailAddressEditText);
        emailAddress = (TextView) view.findViewById(R.id.emailAddress);

        if (fromLogin) {
            emailAddressEditText.setVisibility(View.VISIBLE);
            emailAddress.setVisibility(View.VISIBLE);
        }

        initSpinner(view);

        builder.setTitle(R.string.zendesk_reportAProblem);

        builder.setPositiveButton(R.string.zendesk_send, null);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveZendeskTicket();
                    }
                });
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        return dialog;
    }

    public void initSpinner(View view) {
        List<String> severityList = Arrays.asList(
                getString(R.string.zendesk_casualQuestion),
                getString(R.string.zendesk_needHelp),
                getString(R.string.zendesk_somethingsBroken),
                getString(R.string.zendesk_cantGetThingsDone),
                getString(R.string.zendesk_extremelyCritical));

        severitySpinner = (Spinner) view.findViewById(R.id.severitySpinner);
        ZenDeskAdapter adapter = new ZenDeskAdapter(getActivity(), R.layout.zendesk_spinner_item, severityList);
        severitySpinner.setAdapter(adapter);
    }

    private class ZenDeskAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;

        public ZenDeskAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getViewForText(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getViewForText(position, convertView, parent);
        }

        private View getViewForText(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.zendesk_spinner_item, parent, false);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(getItem(position));
            holder.text.post(new Runnable() {
                @Override
                public void run() {
                    holder.text.setSingleLine(false);
                }
            });
            com.instructure.pandautils.utils.Utils.testSafeContentDescription(holder.text,
                    getContext().getString(R.string.severity_text_content_desc, position),
                    holder.text.getText().toString(),
                    com.instructure.canvasapi2.BuildConfig.IS_TESTING);

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView text;
    }

    public void saveZendeskTicket() {
        String comment = descriptionEditText.getText().toString();
        String subject = subjectEditText.getText().toString();

        if (comment.isEmpty() || subject.isEmpty()){
            Toast.makeText(getContext(), R.string.empty_feedback, Toast.LENGTH_LONG).show();
            return;
        }

        // if we're on the login page we need to set the cache user's email address so that support can
        // contact the user
        if (fromLogin) {
            if (emailAddressEditText.getText() != null) {
                User user = new User();
                user.setPrimaryEmail(emailAddressEditText.getText().toString());
                ApiPrefs.setUser(user);
            }
        }

        final User user = ApiPrefs.getUser();
        String email = "";
        if(user != null) {
            email = user.getPrimaryEmail();
        }
        String domain = ApiPrefs.getDomain();
        if (domain.isEmpty()) {
            domain = DEFAULT_DOMAIN;
        }

        //add device info to comment
        //try to get the version number and version code
        PackageInfo pInfo = null;
        String versionName = "";
        int versionCode = 0;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //Do nothing
        }
        String deviceInfo = "";
        deviceInfo += getString(R.string.device) + " " + Build.MANUFACTURER + " " + Build.MODEL + "\n" +
                getString(R.string.osVersion) + " " + Build.VERSION.RELEASE + "\n" +
                getString(R.string.versionNum) + ": " + versionName + " " + versionCode + "\n" +
                getString(R.string.zendesk_severityText) + " " + getUserSeveritySelectionTag() + "\n" +
                getString(R.string.utils_installDate) + " " + getInstallDateString() + "\n\n";

        comment = deviceInfo + comment;
        if (mUseDefaultDomain) {
            ErrorReportManager.postGenericErrorReport(subject, domain, email, comment, getUserSeveritySelectionTag(), new StatusCallback<ErrorReportResult>() {

                @Override
                public void onResponse(Response<ErrorReportResult> response, LinkHeaders linkHeaders, ApiType type) {
                    resetCachedUser();
                    if(type.isAPI()) {
                        resultListener.onTicketPost();
                    }
                }

                @Override
                public void onFail(Call<ErrorReportResult> response, Throwable error) {
                    resetCachedUser();
                    resultListener.onTicketError();
                }

                @Override
                public void onFinished(ApiType type) {
                    dismiss();
                }
            });
        } else {
            ErrorReportManager.postErrorReport(subject, domain, email, comment, getUserSeveritySelectionTag(), new StatusCallback<ErrorReportResult>() {

                @Override
                public void onResponse(Response<ErrorReportResult> response, LinkHeaders linkHeaders, ApiType type) {
                    resetCachedUser();
                    if(type.isAPI()) {
                        resultListener.onTicketPost();
                    }
                }

                @Override
                public void onFail(Call<ErrorReportResult> response, Throwable error) {
                    resetCachedUser();
                    resultListener.onTicketError();
                }

                @Override
                public void onFinished(ApiType type) {
                    dismiss();
                }
            });
        }
    }

    private void resetCachedUser() {
        if (fromLogin) {
            //reset the cached user so we don't have any weird data hanging around
            ApiPrefs.setUser(null);
        }
    }

    private String getInstallDateString() {
        try {
            long installed = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0)
                    .firstInstallTime;
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
            return format.format(new Date(installed));
        } catch (Exception e) {
            return "";
        }
    }


    private String getUserSeveritySelectionTag() {
        if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_extremelyCritical))) {
            return getString(R.string.zendesk_extremelyCritical_tag);
        } else if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_casualQuestion))) {
            return getString(R.string.zendesk_casualQuestion_tag);
        } else if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_somethingsBroken))) {
            return getString(R.string.zendesk_somethingsBroken_tag);
        } else if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_cantGetThingsDone))) {
            return getString(R.string.zendesk_cantGetThingsDone_tag);
        } else if (severitySpinner.getSelectedItem().equals(getString(R.string.zendesk_needHelp))) {
            return getString(R.string.zendesk_needHelp_tag);
        } else {
            return "";
        }
    }

    /**
     * if we're coming from the login screen there won't be any user information (because the user hasn't
     * logged in)
     * @param fromLogin boolean telling if coming from a login page where their may not be a valid user.
     * @return Bundle
     */
    public static Bundle createBundle(boolean fromLogin) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Const.FROM_LOGIN, fromLogin);
        return bundle;
    }

    /**
     * if we're coming from the parent app we want to use the default domain
     *
     * @param fromLogin boolean telling if coming from a login page where their may not be a valid user.
     * @param useDefaultDomain use the default domain, ebuki.nxtstepdsgn.com
     * @return Bundle
     */
    public static Bundle createBundle(boolean fromLogin, boolean useDefaultDomain) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Const.FROM_LOGIN, fromLogin);
        bundle.putBoolean(Const.USE_DEFAULT_DOMAIN, useDefaultDomain);
        return bundle;
    }

    /**
     * Set the colors of the dialog based on the arguments passed in the bundle. If there isn't a color
     * we just use what is already set
     */
    public void handleBundle() {
        if (getArguments() == null) {
            return;
        }

        fromLogin = getArguments().getBoolean(Const.FROM_LOGIN, false);
        mUseDefaultDomain = getArguments().getBoolean(Const.USE_DEFAULT_DOMAIN, false);
    }
}
