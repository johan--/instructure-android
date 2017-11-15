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

package com.instructure.teacher.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.loginapi.login.api.zendesk.utilities.ZendeskDialogStyled;
import com.instructure.pandautils.dialogs.RatingDialog;
import com.instructure.pandautils.utils.Const;
import com.instructure.teacher.R;
import com.instructure.teacher.activities.InternalWebViewActivity;

import java.util.Date;
import java.util.Locale;


    public class HelpDialogStyled extends DialogFragment
            {

        public static final String TAG = "helpDialog";

        private LinearLayout searchGuides;
        private LinearLayout reportProblem;
        private LinearLayout requestFeature;
        private LinearLayout showLove;

        public static HelpDialogStyled show(FragmentActivity activity) {
            HelpDialogStyled helpDialogStyled = new HelpDialogStyled();
            Bundle args = new Bundle();
            helpDialogStyled.setArguments(args);
            helpDialogStyled.show(activity.getSupportFragmentManager(), TAG);
            return helpDialogStyled;
        }


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).title(getActivity().getString(R.string.help));

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_help, null);
            searchGuides = (LinearLayout) view.findViewById(R.id.search_guides);
            reportProblem = (LinearLayout) view.findViewById(R.id.report_problem);
            requestFeature = (LinearLayout) view.findViewById(R.id.request_feature);
            showLove = (LinearLayout) view.findViewById(R.id.share_love);

            builder.customView(view, true);

            final MaterialDialog dialog = builder.build();
            dialog.setCanceledOnTouchOutside(true);
            return dialog;
        }

        @Override
        public void onDestroyView() {
            if (getDialog() != null && getRetainInstance())
                getDialog().setDismissMessage(null);
            super.onDestroyView();
        }

        ///////////////////////////////////////////////////////////////////////////
        // LifeCycle
        ///////////////////////////////////////////////////////////////////////////

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setupListeners();
        }

        ///////////////////////////////////////////////////////////////////////////
        // Helpers
        ///////////////////////////////////////////////////////////////////////////

        private void setupListeners() {


            searchGuides.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Search guides
                    startActivity(InternalWebViewActivity.createIntent(getActivity(), Const.CANVAS_USER_GUIDES, getString(R.string.canvasGuides), false));
                }
            });

            reportProblem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ZendeskDialogStyled dialog = new ZendeskDialogStyled();

                    dialog.setArguments(ZendeskDialogStyled.createBundle(false, true));
                    dialog.show(getActivity().getSupportFragmentManager(), ZendeskDialogStyled.TAG);
                }
            });

            requestFeature.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //let the user open their favorite mail client

                    Intent intent = populateMailIntent(getActivity().getString(R.string.featureSubject), getActivity().getString(R.string.understandRequest), false);

                    startActivity(Intent.createChooser(intent, getActivity().getString(R.string.sendMail)));
                }
            });

            showLove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RatingDialog.showRateDialog(getActivity(), com.instructure.pandautils.utils.AppType.TEACHER);
                }
            });
        }

        /*
            Pass in the subject and first line of the e-mail, all the other data is the same
         */
        private Intent populateMailIntent(String subject, String title, boolean supportFlag) {
            //let the user open their favorite mail client
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            if(supportFlag){
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getActivity().getString(R.string.utils_supportEmailAddress)});
            }else{
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getActivity().getString(R.string.utils_mobileSupportEmailAddress)});
            }
            //try to get the version number and version code
            PackageInfo pInfo = null;
            String versionName = "";
            int versionCode = 0;
            try {
                pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                versionName = pInfo.versionName;
                versionCode = pInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Logger.d(e.getMessage());
            }

            intent.putExtra(Intent.EXTRA_SUBJECT, "[" + subject + "] " + getString(R.string.issue_with_canvas) + versionName);

            User user = ApiPrefs.getUser();
            //populate the email body with information about the user
            String emailBody = "";
            emailBody += title + "\n";
            if (user != null){
                emailBody += getActivity().getString(R.string.help_userId) + " " + user.getId() + "\n";
                emailBody += getActivity().getString(R.string.help_email) + " " + user.getEmail() + "\n";
            }else{
                emailBody += getActivity().getString(R.string.no_user) + "\n";
            }

            emailBody += getActivity().getString(R.string.help_domain) + " " + ApiPrefs.getDomain() + "\n";
            emailBody += getActivity().getString(R.string.help_versionNum) + " " + versionName + " " + versionCode + "\n";
            emailBody += getActivity().getString(R.string.help_locale) + " " + Locale.getDefault() + "\n";
            emailBody += getActivity().getString(R.string.installDate) + " " + getInstallDateString() + "\n";
            emailBody += "----------------------------------------------\n";

            intent.putExtra(Intent.EXTRA_TEXT, emailBody);

            return intent;
        }

        private String getInstallDateString() {
            try {
                long installed = getActivity().getPackageManager()
                        .getPackageInfo(getActivity().getPackageName(), 0)
                        .firstInstallTime;
                return DateHelper.getDayMonthYearFormat().format(new Date(installed));
            } catch (Exception e) {
                return "";
            }
        }


    }

