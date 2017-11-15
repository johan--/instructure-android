/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.presenters;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.RecipientManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Enrollment;
import com.instructure.canvasapi2.models.Recipient;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.teacher.viewinterface.PeopleListView;

import java.util.ArrayList;
import java.util.List;

import instructure.androidblueprint.SyncPresenter;
import retrofit2.Response;


public class PeopleListPresenter extends SyncPresenter<User, PeopleListView> {


    public enum PeopleFilter{
        SORT_BY_NAME,
        SORT_GRADE_HIGH_TO_LOW,
        SORT_GRADE_LOW_TO_HIGH
    }

    private CanvasContext mCanvasContext;
    private PeopleFilter mFilter = PeopleFilter.SORT_BY_NAME;
    private ArrayList<User> mUserList = new ArrayList<>();
    private RecipientRunnable mRun;
    //If we try to automate this class the handler might create some issues. Cross that bridge when we come to it
    private Handler mHandler = new Handler();

    public PeopleListPresenter(@NonNull CanvasContext canvasContext) {
        super(User.class);
        mCanvasContext = canvasContext;
    }

    @Override
    public void loadData(boolean forceNetwork) {
        if(forceNetwork){
            mUserList.clear();
        } else {
            if(getData().size() > 0) return;
        }
        onRefreshStarted();
        UserManager.getAllEnrollmentsPeopleList(mCanvasContext, mUserListCallback, forceNetwork);
    }

    @Override
    public void refresh(boolean forceNetwork) {
        onRefreshStarted();
        mUserListCallback.reset();
        mUserList.clear();
        clearData();
        loadData(forceNetwork);
    }

    private StatusCallback<List<User>> mUserListCallback = new StatusCallback<List<User>>() {
        @Override
        public void onResponse(Response<List<User>> response, LinkHeaders linkHeaders, ApiType type) {
            getData().addOrUpdate(response.body());
            mUserList.addAll(response.body());
        }

        @Override
        public void onFinished(ApiType type) {
            if(getViewCallback() != null) {
                getViewCallback().checkIfEmpty();
                getViewCallback().onRefreshFinished();
            }
        }
    };

    /**
     *  Calls our API to query for possible recipients, with the mCurrentConstraint as the search parameter.
     *  This process will "kill" any pending runnables. With a delay of 500ms.
     */
    private void fetchAdditionalRecipients(String constraint){
        if(mRun != null){
            mRun.kill();
            mHandler.removeCallbacks(mRun);

        }
        mRun = new RecipientRunnable(constraint);
        mHandler.post(mRun);
    }

    public void searchPeopleList(String searchTerm) {

        mRecipientCallback.reset();
        mUserList.clear();
        clearData();
        fetchAdditionalRecipients(searchTerm);
    }

    private StatusCallback<List<Recipient>> mRecipientCallback = new StatusCallback<List<Recipient>>() {
        @Override
        public void onResponse(Response<List<Recipient>> response, LinkHeaders linkHeaders, ApiType type) {
            clearData();
            getData().beginBatchedUpdates();
            for (Recipient recipient : response.body()) {
                //convert recipient to user
                User user = convertRecipientToUser(recipient);

                getData().add(user);
            }
            getData().endBatchedUpdates();
        }

        @Override
        public void onFinished(ApiType type) {
            if (getViewCallback() != null) {
                getViewCallback().onRefreshFinished();
                getViewCallback().checkIfEmpty();
            }
        }
    };

    @NonNull
    private User convertRecipientToUser(Recipient recipient) {
        User user = new User();
        user.setAvatarUrl(recipient.getAvatarURL());
        user.setId(recipient.getIdAsLong());
        user.setName(recipient.getName());
        user.setSortableName(recipient.getName());
        //get enrollments
        ArrayList<Enrollment> enrollments = new ArrayList<>();
        if(recipient.getCommonCourses() != null) {
            String[] commonCoursesEnrollments = recipient.getCommonCourses().get(Long.toString(mCanvasContext.getId()));
            if (commonCoursesEnrollments != null) {
                for (String enrollment : commonCoursesEnrollments) {
                    Enrollment newEnrollment = new Enrollment();
                    newEnrollment.setType(enrollment);
                    enrollments.add(newEnrollment);
                }
                user.setEnrollments(enrollments);
            }
        }

        return user;
    }


    @Override
    protected int compare(User item1, User item2) {
        switch(mFilter) {
            case SORT_BY_NAME:
                return compareName(item1, item2);
            case SORT_GRADE_HIGH_TO_LOW:
                return Double.compare(getUserGrade(item2), getUserGrade(item1));
            default:
                //Low_to_high
                return Double.compare(getUserGrade(item1), getUserGrade(item2));
        }
    }

    private int compareName(User item1, User item2) {
        return item1.getSortableName().compareToIgnoreCase(item2.getSortableName());
    }

    private double getUserGrade(User user) {
        for(Enrollment enrollment : user.getEnrollments()){
            if(enrollment.isStudent()) {
                if (enrollment.isMultipleGradingPeriodsEnabled()) {
                    return enrollment.getCurrentPeriodComputedCurrentScore();
                } else {
                    return enrollment.getCurrentScore();
                }
            }
        }
        return 0.0;
    }

    @Override
    protected boolean areItemsTheSame(User user1, User user2) {
        return user1.getId() == user2.getId();
    }

    public void setFilter(PeopleFilter filter) {
        mFilter = filter;

        //Reset the data to use the new filter
        clearData();
        getData().addOrUpdate(mUserList);
        if (getViewCallback() != null) {
            getViewCallback().onRefreshFinished();
            getViewCallback().checkIfEmpty();
        }
    }

    private class RecipientRunnable implements Runnable{
        private boolean isKilled = false;
        private String constraint = "";
        RecipientRunnable(String constraint){
            this.constraint = constraint;
        }

        @Override
        public void run() {
            if(!isKilled && null != constraint && !TextUtils.isEmpty(constraint) && mCanvasContext != null){
                onRefreshStarted();
                RecipientManager.searchAllRecipientsNoSyntheticContexts(true, constraint, mCanvasContext.getContextId(), mRecipientCallback);
            } else {
                if (getViewCallback() != null) {
                    getViewCallback().onRefreshFinished();
                    getViewCallback().checkIfEmpty();
                }
            }
        }

        private void kill(){
            isKilled = true;
        }
    }
}
