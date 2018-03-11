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

package com.ebuki.homework.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ebuki.homework.R;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class CalendarChooserDialogStyled extends DialogFragment {

    private static final String TAG = "CalendarChooserDialogStyled";

    private boolean mIsFirstShow;
    private LinkedHashMap<String, String> mAllContextIDs; // <id , name>
    private List<String> mAllContextCourseCodes; // <id, course_code>
    private ArrayList<String> mSelectedIds; // list of context ids previously selected by the user

    private boolean[] mCurrentCheckedPositions;
    private boolean[] mOriginalCheckedPositions; // easy check for changed selection
    private static CalendarChooserCallback mCallback;

    public interface CalendarChooserCallback {
        void onCalendarsSelected(List<String> subscribedContexts);
    }

    //region Calendar Dialog

    /**
     * @param activity
     * @param cachedContextList Cached list of canvas contexts selected by the user
     * @param contextIDs        List of all canvas contexts user can subscribe to
     * @param callback
     */
    public static void show(FragmentActivity activity, ArrayList<String> cachedContextList, @NonNull HashMap<String, String> contextIDs, @NonNull ArrayList<String> contextCourseCodeIDs, boolean firstShow, CalendarChooserCallback callback)  {
        CalendarChooserDialogStyled frag = new CalendarChooserDialogStyled();
        mCallback = callback;

        Bundle args = new Bundle();
        args.putStringArrayList(Const.CALENDAR_DIALOG_FILTER_PREFS, cachedContextList);
        args.putSerializable(Const.CALENDAR_DIALOG_CONTEXT_IDS, contextIDs);
        args.putSerializable(Const.CALENDAR_DIALOG_CONTEXT_COURSE_IDS, contextCourseCodeIDs);
        args.putBoolean(Const.IS_FIRST_SHOW, firstShow);
        frag.setArguments(args);

        frag.show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User mUser = ApiPrefs.getUser();

        Bundle args = getArguments();
        if(args != null) {
            mSelectedIds = args.getStringArrayList(Const.CALENDAR_DIALOG_FILTER_PREFS);
            mAllContextIDs = (LinkedHashMap<String, String>) args.getSerializable(Const.CALENDAR_DIALOG_CONTEXT_IDS);
            mAllContextCourseCodes = (List<String>) args.getSerializable(Const.CALENDAR_DIALOG_CONTEXT_COURSE_IDS);
            mIsFirstShow = args.getBoolean(Const.IS_FIRST_SHOW);

            if(mAllContextIDs == null){
                mAllContextIDs = new LinkedHashMap<>();
            }

            if (mAllContextCourseCodes == null) {
                mAllContextCourseCodes = new ArrayList<>();
            }

            if(mSelectedIds.size() == 0){
                mSelectedIds.add(mUser.getContextId());
            }
            initCheckedItemsArray();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();

        final CalendarChooserAdapter listAdapter = new CalendarChooserAdapter(activity, android.R.layout.select_dialog_multichoice);
        final ListView listView;

        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title(R.string.selectCanvasCalendars)
                .adapter(listAdapter, null)
                .positiveText(R.string.done)
                .negativeText(R.string.cancel)
                .autoDismiss(false)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (mSelectedIds.size() > 0) {
                            if (mCallback != null) {
                                if (selectionChanged()) {
                                    //We only want to refresh if a change was made
                                    mCallback.onCalendarsSelected(getSelectedContexts());
                                }
                            } else {
                                Toast.makeText(activity, R.string.errorOccurred, Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        } else {
                            Toast.makeText(activity, getResources().getString(R.string.calendarDialogNoneWarning), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        if (mSelectedIds.size() < 1) {
                            Toast.makeText(activity, getResources().getString(R.string.calendarDialogNoneWarning), Toast.LENGTH_SHORT).show();
                        } else {
                            if (mCallback != null) {
                                if (mIsFirstShow) {
                                    //We only want to make a call here if its their first time (on negative)
                                    mCallback.onCalendarsSelected(getSelectedContexts());
                                }
                            }
                            dialog.dismiss();
                        }
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (mSelectedIds.size() > 0) {
                            if (mCallback != null) {
                                if (mIsFirstShow) {
                                    //We only want to make a call here if its their first time (on negative)
                                    mCallback.onCalendarsSelected(getSelectedContexts());
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                })
                .keyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                            if (mSelectedIds.size() > 0) {
                                if (mCallback != null) {
                                    if (mIsFirstShow) {
                                        //We only want to make a call here if its their first time (on negative)
                                        mCallback.onCalendarsSelected(getSelectedContexts());
                                    }
                                }
                            }
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                })
                .build();

        listView = dialog.getListView();

        //Override onItemCLick to implement "checking" behavior and handle contextsForReturn
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_box);
                    String contextId = "";
                    if (view.getTag() instanceof ViewHolder) {
                        contextId = ((ViewHolder) view.getTag()).contextId;
                    }

                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        removeSelection(position, contextId);
                    }
                    else {
                        if (mSelectedIds.size() >= 10) {
                            Toast.makeText(activity, getResources().getString(R.string.calendarDialog10Warning), Toast.LENGTH_SHORT).show();
                        } else {
                            checkBox.setChecked(true);
                            addSelection(position, contextId);
                        }
                    }
                }
            });
        }

        return dialog;
    }

    private void addSelection(int position, String contextId){
        mSelectedIds.add(contextId);
        mCurrentCheckedPositions[position] = true;
    }

    private void removeSelection(int position, String contextId){
        mSelectedIds.remove(contextId);
        mCurrentCheckedPositions[position] = false;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
    //endregion

    //region Helpers
    private void initCheckedItemsArray(){
        // create bool array of checked items length of mAllContextIds
        mCurrentCheckedPositions = new boolean[mAllContextIDs.size()];

        List<String> allContextIds = new ArrayList<>(mAllContextIDs.keySet());
        // generate a bool array of selected positions from our ordered list
        for(int i = 0; i < mAllContextIDs.size(); i++ ){
            mCurrentCheckedPositions[i] = mSelectedIds.contains(allContextIds.get(i));
        }
        // copy of checked items used to check if selection changed
        mOriginalCheckedPositions = Arrays.copyOf(mCurrentCheckedPositions, mCurrentCheckedPositions.length);
    }

    /**
     *
     * @return true if the users calendar selection has changed since the dialog was originally
     * opened or if this is the first time opening the calendar dialog chooser. This is done to
     * prevent a new user from nulling out the calendarAPI calls.
     */
    private boolean selectionChanged(){
        return mIsFirstShow || !Arrays.equals(mOriginalCheckedPositions, mCurrentCheckedPositions);
    }

    public List<String> getSelectedContexts(){
       return mSelectedIds;
    }
    //endregion

    //region CalendarChooserAdapter

    private class CalendarChooserAdapter extends ArrayAdapter<String> {

        List<String> keySet = new ArrayList<>(mAllContextIDs.keySet());

        CalendarChooserAdapter(Context context, int resource) {
            super(context, resource, new ArrayList<>(mAllContextIDs.values()));
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = li.inflate(R.layout.calendar_dialog_list_item, parent, false);
                holder = new ViewHolder();
                holder.courseName = (TextView) convertView.findViewById(R.id.course_name);
                holder.courseCode = (TextView) convertView.findViewById(R.id.course_code);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
                holder.indicator = (ImageView) convertView.findViewById(R.id.indicator);
                holder.parent = convertView.findViewById(R.id.parent);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // Get the name for a calendar
            final String courseName = getItem(position);
            final String courseCode = mAllContextCourseCodes.get(position);
            // The name for the calendar
            holder.courseName.setText(courseName);
            holder.courseCode.setVisibility(courseCode.isEmpty() ? View.GONE : View.VISIBLE);
            holder.courseCode.setText(courseCode);
            holder.contextId = keySet.get(position);
            holder.checkBox.setChecked(mCurrentCheckedPositions[position]);

            //get Context color
            ShapeDrawable circle = new ShapeDrawable(new OvalShape());
            circle.getPaint().setColor(CanvasContextColor.getCachedColor(getContext(), keySet.get(position)));
            holder.indicator.setBackgroundDrawable(circle);

            return convertView;
        }
    }

    private static class ViewHolder {
        View parent;
        TextView courseName;
        TextView courseCode;
        CheckBox checkBox;
        String contextId;
        ImageView indicator;
    }
    //endregion
}
