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
package com.instructure.pandautils.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.R;
import com.instructure.pandautils.utils.AppType;
import com.instructure.pandautils.utils.Utils;

public class RatingDialog extends DialogFragment {

    private static final String APP_TYPE = "app_type";
    private static final String DATE_FIRST_LAUNCHED = "date_first_launched";
    private static final String DONT_SHOW_AGAIN = "dont_show_again";
    private static final String RATING_DIALOG = "rating_dialog";
    private static final String DATE_SHOW_AGAIN = "date_show_again";
    private static final String HAS_SHOWN = "has_shown";

    private static final int FOUR_WEEKS = 28;
    private static final int SIX_WEEKS = 42;

    private EditText comment;
    private Button send;

    private ImageView star1, star2, star3, star4, star5;
    private int starsSelected = 0;

    public static RatingDialog newInstance(AppType appType) {

        RatingDialog frag = new RatingDialog();
        Bundle args = new Bundle();
        args.putSerializable(APP_TYPE, appType);
        frag.setArguments(args);
        return frag;
    }


    /**
     * Will show the rating dialog when:
     *
     *  - user has used the app for 4 weeks
     *  - when the user sees the dialog, there are a few use cases for when to show the
     *    dialog again
     *
     *     1. User presses 5 stars -> take user to play store and don't show dialog again
     *     2. User presses < 5 stars with no comment -> show again 4 weeks later
     *     3. User presses < 5 stars with a comment -> show again 6 weeks later
     *     4. User presses back -> show again 4 weeks later
     *
     *  - when the user sees the dialog again there will be a "don't show again" button
     *    that they can press
     * @param context A valid context
     */
    public static void showRatingDialog(FragmentActivity context, AppType appType) {
        SharedPreferences prefs = context.getSharedPreferences(RATING_DIALOG, Context.MODE_PRIVATE);
        if (prefs.getBoolean(DONT_SHOW_AGAIN, false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(DATE_FIRST_LAUNCHED, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(DATE_FIRST_LAUNCHED, date_firstLaunch);
        }

        long daysUntilShow = prefs.getInt(DATE_SHOW_AGAIN, FOUR_WEEKS);
        // Wait at least daysUntilShow days before opening
        if (System.currentTimeMillis() >= date_firstLaunch +
                (daysUntilShow * 24 * 60 * 60 * 1000)) {
            showRateDialog(context, appType, prefs);
        }

        editor.apply();
    }

    public static void showRateDialog(final FragmentActivity context, final AppType appType, final SharedPreferences prefs) {
        final RatingDialog dialog = RatingDialog.newInstance(appType);
        dialog.show(context.getSupportFragmentManager(), "ratingDialog");
    }


    /**
     * Called when the user hits the back button, this will delay the dialog from showing for 4 weeks
     *
     * @param dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        SharedPreferences prefs = getActivity().getSharedPreferences(RATING_DIALOG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //show again in 4 weeks
        editor.putInt(DATE_SHOW_AGAIN, FOUR_WEEKS);
        //reset the date_first_launched to be right now
        editor.putLong(DATE_FIRST_LAUNCHED, System.currentTimeMillis());

        editor.apply();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle args = getArguments();

        final SharedPreferences prefs = getActivity().getSharedPreferences(RATING_DIALOG, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        final AppType appType = (AppType) args.getSerializable(APP_TYPE);
        final String buttonText = ((prefs.getBoolean(HAS_SHOWN, false) ? getString(R.string.utils_dontShowAgain) : getString(R.string.done)));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rating, null);
        builder.setTitle(R.string.utils_howAreWeDoing);
        builder.setView(view);

        //setup the views and listeners
        setupViews(view, prefs);
        setupListeners(editor, appType);

        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putBoolean(DONT_SHOW_AGAIN, true);
                editor.apply();
                dismiss();
            }
        });

        //we have now shown the dialog
        editor.putBoolean(HAS_SHOWN, true);
        editor.apply();

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        return dialog;
    }

    private void setupViews(View view, SharedPreferences prefs) {
        comment = (EditText)view.findViewById(R.id.comments);

        send = (Button)view.findViewById(R.id.send);
        star1 = (ImageView) view.findViewById(R.id.star1);
        star2 = (ImageView) view.findViewById(R.id.star2);
        star3 = (ImageView) view.findViewById(R.id.star3);
        star4 = (ImageView) view.findViewById(R.id.star4);
        star5 = (ImageView) view.findViewById(R.id.star5);
        star1.setImageResource(R.drawable.ic_star);
        star2.setImageResource(R.drawable.ic_star);
        star3.setImageResource(R.drawable.ic_star);
        star4.setImageResource(R.drawable.ic_star);
        star5.setImageResource(R.drawable.ic_star);
    }

    private void setupListeners(final SharedPreferences.Editor editor, final AppType appType) {

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "";
                if (comment.getText() != null) {
                    message = comment.getText().toString();
                }
                if (TextUtils.isEmpty(message)) {
                    //show again in 4 weeks
                    editor.putInt(DATE_SHOW_AGAIN, FOUR_WEEKS);
                    //just close the dialog, they didn't provide any text feedback
                    dismiss();
                } else {
                    //show again in 6 weeks
                    editor.putInt(DATE_SHOW_AGAIN, SIX_WEEKS);

                    //close the dialog
                    dismiss();

                    //figure out which app they're using so we can track that in the suggestion
                    //App names aren't localized so they don't need translated
                    String appTitle = "";
                    if (appType == AppType.CANDROID) {
                        appTitle = getString(R.string.utils_canvas);
                    } else if (appType == AppType.POLLING) {
                        appTitle = getString(R.string.utils_polls);
                    } else if (appType == AppType.SPEEDGRADER) {
                        appTitle = getString(R.string.utils_speedgrader);
                    } else if (appType == AppType.PARENT) {
                        appTitle = getString(R.string.utils_canvasParent);
                    } else if (appType == AppType.TEACHER) {
                        appTitle = getString(R.string.utils_canvasTeacher);
                    }
                    //they provided some feedback, so we will open an email with their suggestion populated
                    Intent intent = populateMailIntent(getString(R.string.utils_suggestions) + " - " + appTitle, message);
                    PackageManager packageManager = getActivity().getPackageManager();
                    //check to make sure they have an app that can handle the mail intent
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent);
                    } else {
                        //don't do anything. The dialog has been dismissed. We won't capture their comment though
                    }
                }
                //reset the date_first_launched to be right now
                editor.putLong(DATE_FIRST_LAUNCHED, System.currentTimeMillis());
                editor.commit();
            }
        });

        View.OnClickListener startClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                star1.setImageResource(R.drawable.ic_star);
                star2.setImageResource(R.drawable.ic_star);
                star3.setImageResource(R.drawable.ic_star);
                star4.setImageResource(R.drawable.ic_star);
                star5.setImageResource(R.drawable.ic_star);

                final int id = v.getId();
                if(id == R.id.star1) {
                    starsSelected = 1;
                    star1.setImageResource(R.drawable.ic_star_default_color);
                    comment.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
                } else if(id == R.id.star2) {
                    starsSelected = 2;
                    star1.setImageResource(R.drawable.ic_star_default_color);
                    star2.setImageResource(R.drawable.ic_star_default_color);
                    comment.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
                } else if(id == R.id.star3) {
                    starsSelected = 3;
                    star1.setImageResource(R.drawable.ic_star_default_color);
                    star2.setImageResource(R.drawable.ic_star_default_color);
                    star3.setImageResource(R.drawable.ic_star_default_color);
                    comment.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
                } else if(id == R.id.star4) {
                    starsSelected = 4;
                    star1.setImageResource(R.drawable.ic_star_default_color);
                    star2.setImageResource(R.drawable.ic_star_default_color);
                    star3.setImageResource(R.drawable.ic_star_default_color);
                    star4.setImageResource(R.drawable.ic_star_default_color);
                    comment.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
                } else if(id == R.id.star5) {
                    starsSelected = 5;
                    star1.setImageResource(R.drawable.ic_star_default_color);
                    star2.setImageResource(R.drawable.ic_star_default_color);
                    star3.setImageResource(R.drawable.ic_star_default_color);
                    star4.setImageResource(R.drawable.ic_star_default_color);
                    star5.setImageResource(R.drawable.ic_star_default_color);

                    comment.setVisibility(View.GONE);
                    send.setVisibility(View.GONE);
                    Utils.goToAppStore(appType, getActivity());

                    //don't show it again
                    editor.putBoolean(DONT_SHOW_AGAIN, true);
                    editor.commit();

                    dismiss();
                }
            }
        };

        star1.setOnClickListener(startClickListener);
        star2.setOnClickListener(startClickListener);
        star3.setOnClickListener(startClickListener);
        star4.setOnClickListener(startClickListener);
        star5.setOnClickListener(startClickListener);
    }

    private Intent populateMailIntent(String subject, String title) {
        //let the user open their favorite mail client
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.utils_mobileSupportEmailAddress)});
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
        intent.putExtra(Intent.EXTRA_SUBJECT, subject + " " + versionName);

        User user = ApiPrefs.getUser();
        //populate the email body with information about the user
        String emailBody = "";
        emailBody += title + "\n\n";

        if(user != null) {
            emailBody += getActivity().getString(R.string.utils_userId) + ": " + user.getId() + "\n";
            emailBody += getActivity().getString(R.string.utils_email) + ": " + user.getPrimaryEmail() + "\n";
        }
        emailBody += getActivity().getString(R.string.utils_domain) + ": " + ApiPrefs.getDomain() + "\n";
        emailBody += getActivity().getString(R.string.utils_versionNum) + " " + versionName + " " + versionCode + "\n";
        emailBody += getString(R.string.utils_device) + ": " + Build.MANUFACTURER + " " + Build.MODEL + "\n";
        emailBody += getString(R.string.utils_osVersion) + ": " + Build.VERSION.RELEASE + "\n";
        emailBody += "-----------------------\n";

        intent.putExtra(Intent.EXTRA_TEXT, emailBody);

        return intent;
    }
}
