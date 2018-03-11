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

package com.ebuki.portal.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ebuki.portal.R;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.UserAPI;
import com.instructure.canvasapi2.managers.ConversationManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;

public class AskInstructorDialogStyled extends DialogFragment {

    public static final String TAG = "askInstructorDialog";

    //dialogs
    private ProgressDialog progressDialog;

    private ArrayList<Course> courseList;
    private HashSet<User> emailList;
    private Course course;

    //views
    private Spinner courseSpinner;
    private EditText message;

    //adapter
    private CourseSpinnerAdapter courseAdapter;

    //Inflater
    private LayoutInflater inflater;

    private boolean hasLoadedFirstPage = false;
    private String nextURL;
    private boolean foundTeachers = false;

    //Callbacks
    StatusCallback<List<User>> getPeopleCallback;
    StatusCallback<List<Course>> getFavoriteCoursesCallback;
    StatusCallback<List<Conversation>> sendMessageCanvasCallback;

    private boolean canClickSend = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getActivity())
                                  .title(getActivity().getString(R.string.instructor_question))
                                  .positiveText(getActivity().getString(R.string.send));


        View view = LayoutInflater.from(getActivity()).inflate(R.layout.ask_instructor, null);
        courseSpinner = (Spinner)view.findViewById(R.id.course_spinner);

        ArrayList<Course> loadingIndicator = new ArrayList<Course>();
        Course loading = new Course();
        loading.setName(getActivity().getString(R.string.loading));
        loadingIndicator.add(loading);
        courseAdapter = new CourseSpinnerAdapter (getActivity(), android.R.layout.simple_spinner_dropdown_item, loadingIndicator);
        //we haven't set an onItemSelectedListener, so selecting the item shouldn't do anything
        courseSpinner.setAdapter(courseAdapter);

        message = (EditText)view.findViewById(R.id.message);

        builder.customView(view, true);

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);

                if(canClickSend) {
                    if (message == null || message.getText().toString().trim().equals("")) {
                        Toast.makeText(getActivity(), getString(R.string.emptyMessage), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        progressDialog = ProgressDialog.show(getActivity(), "", getActivity().getString(R.string.sending));
                        loadTeacherData();
                    }
                }
            }
        });

        MaterialDialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // anything that relies on intent data belongs here
        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        emailList = new HashSet<>();
        courseList = new ArrayList<>();

        //Set up the callbacks.
        setUpCallbacks();
        CourseManager.getAllFavoriteCourses(true, getFavoriteCoursesCallback);
    }

    private void loadTeacherData(){
        UserAPI.ENROLLMENT_TYPE enrollmentType;
        if(!foundTeachers){
            enrollmentType = UserAPI.ENROLLMENT_TYPE.TEACHER;
        }
        else{
            enrollmentType = UserAPI.ENROLLMENT_TYPE.TA;
        }

        if(!hasLoadedFirstPage){
            UserManager.getFirstPagePeopleList(course, enrollmentType, true, getPeopleCallback);
        }
        else{
            UserManager.getNextPagePeopleList(true, nextURL, getPeopleCallback);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////

    public class CourseSpinnerAdapter extends ArrayAdapter<Course> {

        private ArrayList<Course> courses = new ArrayList<Course>();

        public CourseSpinnerAdapter(Context context, int textViewResourceId,
                                    ArrayList<Course> courses) {
            super(context, textViewResourceId, courses);
            this.courses = courses;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {


            CourseViewHolder holder = null;

            if ( convertView == null )
            {
				/* There is no view at this position, we create a new one.
		           In this case by inflating an xml layout */
                convertView = (LinearLayout)(inflater.inflate(R.layout.spinner_row_courses, null));

                holder = new CourseViewHolder();
                holder.courseName = (TextView)convertView.findViewById(R.id.courseName);

                convertView.setTag (holder);
            }
            else
            {
				/* We recycle a View that already exists */
                holder = (CourseViewHolder) convertView.getTag ();
            }

            if(courses.get(position) != null) {
                holder.courseName.setText(courses.get(position).getName());
            }
            return convertView;
        }
    }
    private static class CourseViewHolder {
        TextView courseName;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////
    public void setUpCallbacks(){
        getFavoriteCoursesCallback = new StatusCallback<List<Course>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Course>> response, LinkHeaders linkHeaders, ApiType type) {
                //only add courses in which the user isn't a teacher
                if(!isAdded()){return;}
                for(Course course : response.body()) {
                    if(!course.isTeacher())  {
                        //for duplicate prevention
                        if(!courseList.contains(course)){
                            courseList.add(course);
                        }
                    }
                }

                courseAdapter = new CourseSpinnerAdapter (getActivity(), android.R.layout.simple_spinner_dropdown_item, courseList);
                courseSpinner.setAdapter(courseAdapter);
                courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        course = (Course)parent.getAdapter().getItem(position);
                        //we now have a valid course, let them send a message
                        canClickSend = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        };

        getPeopleCallback = new StatusCallback<List<User>>() {
            @Override
            public void onResponse(retrofit2.Response<List<User>> response, LinkHeaders linkHeaders, ApiType type) {
                nextURL = linkHeaders.nextUrl;
                hasLoadedFirstPage = true;
                List<User> users = response.body();
                if(users != null) {
                    emailList.addAll(users);
                }

                //only get the next group if we haven't found all the groups and we've found all the members of the current group
                if((nextURL == null) && (!foundTeachers)) {

                    //we got all of this type's enrollment because nextURL is null (so no next page)
                    //now get the next enrollment
                    hasLoadedFirstPage = false;
                    nextURL = null;

                    foundTeachers = true;

                    //get the next group.
                    loadTeacherData();
                    return;
                }


                //not done yet, still have a next page of teachers or tas to get
                if(nextURL != null) {
                    //get all the people in the paginated list, force it
                    loadTeacherData();
                }
                else {
                    //now we should be done, send the message
                    String messageText = message.getText().toString();

                    //Get the ids as an arraylist.
                    ArrayList<String> ids = new ArrayList<>();
                    for(User user : emailList)
                    {
                        ids.add(Long.toString(user.getId()));
                    }

                    ConversationManager.createConversation(ids, messageText, "", course.getContextId(), null, true, sendMessageCanvasCallback);
                }
            }

            @Override
            public void onFail(Call<List<User>> response, Throwable error) {
                hasLoadedFirstPage = false;
                progressDialog.dismiss();
            }
        };

        sendMessageCanvasCallback = new StatusCallback<List<Conversation>>() {

            @Override
            public void onResponse(retrofit2.Response<List<Conversation>> response, LinkHeaders linkHeaders, ApiType type) {
                //close progress dialog
                progressDialog.dismiss();
                //close this dialog
                dismiss();
            }

            @Override
            public void onFail(Call<List<Conversation>> response, Throwable error) {
                //Croutons are shown in the background, which makes them hard to see. Use a dialog instead
                FatalErrorDialogStyled fatalErrorDialog = FatalErrorDialogStyled.newInstance(R.string.error, R.string.errorSendingMessage, R.drawable.ic_cv_alert, true);
                if(getActivity() == null) {
                    return;
                }
                fatalErrorDialog.show(getActivity().getSupportFragmentManager(), FatalErrorDialogStyled.TAG);
            }
        };
    }
}
