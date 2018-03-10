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

package com.ebuki.portal.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ebuki.portal.R;
import com.ebuki.portal.binders.ExpandableHeaderBinder;
import com.ebuki.portal.binders.RubricBinder;
import com.ebuki.portal.binders.RubricTopHeaderBinder;
import com.ebuki.portal.holders.ExpandableViewHolder;
import com.ebuki.portal.holders.RubricTopHeaderViewHolder;
import com.ebuki.portal.holders.RubricViewHolder;
import com.ebuki.portal.interfaces.AdapterToFragmentCallback;
import com.ebuki.portal.model.RubricCommentItem;
import com.ebuki.portal.model.RubricItem;
import com.ebuki.portal.model.RubricRatingItem;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.SubmissionManager;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.RubricCriterion;
import com.instructure.canvasapi2.models.RubricCriterionAssessment;
import com.instructure.canvasapi2.models.RubricCriterionRating;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class RubricRecyclerAdapter extends ExpandableRecyclerAdapter<RubricCriterion, RubricItem, RecyclerView.ViewHolder> {

    private CanvasContext mCanvasContext;
    private Assignment mAssignment;
    private AdapterToFragmentCallback mAdapterToFragment;
    private StatusCallback<Submission> mSubmissionCallback;
    private RubricCriterion mTopViewHeader; // The top header is just a group with a different view layout
    private Map<String, RubricCriterionAssessment> mAssessmentMap = new HashMap<>();

    // region Order Work-around
    // Since BaseListRecyclerAdapter uses a sorted list to store the list items, there has to be something to order them by.
    // Recipients have no clear way to order (Can't do by name, because the Last name isn't always in a consistent spot)
    // Since a hash is pretty easy, it made more sense than to create another BaseListRA that had a different representation.
    private HashMap<String, Integer> mInsertedOrderHash = new HashMap<>();

    /* For testing purposes only */
    protected RubricRecyclerAdapter(Context context){
        super(context, RubricCriterion.class, RubricItem.class);
    }

    public RubricRecyclerAdapter(Context context, CanvasContext canvasContext, AdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, RubricCriterion.class, RubricItem.class);
        mTopViewHeader = new RubricCriterion();
        mTopViewHeader.setId("TopViewHeader"); // needs an id for expandableRecyclerAdapter to work
        mCanvasContext = canvasContext;
        mAdapterToFragment = adapterToFragmentCallback;
        setExpandedByDefault(true);
        // loadData is called from RubricFragment
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else if (viewType == Types.TYPE_TOP_HEADER) {
            return new RubricTopHeaderViewHolder(v);
        } else {
            return new RubricViewHolder(v, viewType);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else if (viewType == Types.TYPE_TOP_HEADER){
            return RubricTopHeaderViewHolder.holderResId();
        } else {
            return RubricViewHolder.holderResId(viewType);
        }
    }

    @Override
    public void contextReady() {}

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, RubricCriterion rubricCriterion, RubricItem rubricItem) {
        if(!mAssignment.isMuted()){
            RubricCriterionAssessment assessment = mAssessmentMap.get(rubricCriterion.getId());
            RubricBinder.Companion.bind(getContext(), (RubricViewHolder) holder, rubricItem, rubricCriterion, mAssignment.isFreeFormCriterionComments(), assessment, mCanvasContext);
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, RubricCriterion rubricCriterion, boolean isExpanded) {
        if (holder instanceof RubricTopHeaderViewHolder) {
            onBindTopHeaderHolder(holder);
        } else {
            if(!mAssignment.isMuted()) {
                ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, rubricCriterion, rubricCriterion.getDescription(), isExpanded, getViewHolderHeaderClicked());
            }
        }
    }

    private void onBindTopHeaderHolder(RecyclerView.ViewHolder holder) {
        RubricTopHeaderBinder.bind(getContext(), (RubricTopHeaderViewHolder) holder, getCurrentPoints(), getCurrentGrade(), mAssignment.isMuted());
    }

    // region Data

    @Override
    public void loadData() {
        // use loadDataChained instead, since its a nested fragment and has chained callbacks
        loadDataChained(false, false); // Used when data is refreshed
    }

    /**
     * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.ebuki.portal.activity.CallbackActivity#getUserSelf}
     */
    public void loadDataChained(boolean isWithinAnotherCallback, boolean isCached) {
        if (mAssignment == null) { return; }
        SubmissionManager.getSingleSubmission(mCanvasContext.getId(), mAssignment.getId(), ApiPrefs.getUser().getId(), mSubmissionCallback, isRefresh());
    }

    @Override
    public void setupCallbacks() {
        mSubmissionCallback = new StatusCallback<Submission>() {
            @Override
            public void onResponse(retrofit2.Response<Submission> response, LinkHeaders linkHeaders, ApiType type) {
                Submission submission = response.body();
                mAssessmentMap = submission.getRubricAssessment();
                mAssignment.setSubmission(submission);
                mAdapterToFragment.onRefreshFinished();
                populateAssignmentDetails();
                setAllPagesLoaded(true);
            }

            @Override
            public void onFail(Call<Submission> callResponse, Throwable error, retrofit2.Response response) {
                populateAssignmentDetails();
            }
        };
    }

    private void populateAssignmentDetails() {
        addOrUpdateGroup(mTopViewHeader); // acts as a place holder for the top header

        final List<RubricCriterion> rubric = mAssignment.getRubric();

        if (mAssignment.hasRubric() && !mAssignment.isFreeFormCriterionComments()) {
            populateRatingItems(rubric);
        } else if(mAssignment.isFreeFormCriterionComments()){
            populateFreeFormRatingItems(rubric);
        } else {
            getAdapterToRecyclerViewCallback().setIsEmpty(true);
        }
    }
    // endregion

    // region Grade Helpers
    private void populateRatingItems(List<RubricCriterion> rubric){
        int insertCount = 0;
        for (RubricCriterion rubricCriterion : rubric) {
            final List<RubricCriterionRating> rubricCriterionRatings = rubricCriterion.getRatings();
            for(RubricCriterionRating rating : rubricCriterionRatings) {
                mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
                addOrUpdateItem(rubricCriterion, new RubricRatingItem(rating));
            }
            populateFreeFormRatingItems(rubric);
        }
    }

    private void populateFreeFormRatingItems(List<RubricCriterion> rubric) {
        int insertCount = 0;
        for(RubricCriterion rubricCriterion : rubric){
            RubricItem gradedRating = getFreeFormRatingForCriterion(rubricCriterion);
            if( gradedRating != null){
                mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
                addOrUpdateItem(rubricCriterion, gradedRating);
            }
        }
    }

    @Nullable
    private RubricItem getFreeFormRatingForCriterion(RubricCriterion criterion){
        Submission lastSubmission = mAssignment.getSubmission();
        if(lastSubmission != null){
          RubricCriterionAssessment rating =  lastSubmission.getRubricAssessment().get(criterion.getId());
            if(rating != null){
                return new RubricCommentItem(rating.getComments(), rating.getPoints());
            }
            return null;
        }
        return null;
    }

    private String getPointsPossible() {
        String pointsPossible = "";
        if (mAssignment != null) {
            if (Math.floor(mAssignment.getPointsPossible()) == mAssignment.getPointsPossible()) {
                pointsPossible += (int) mAssignment.getPointsPossible();
            } else {
                pointsPossible += mAssignment.getPointsPossible();
            }
        }
        return pointsPossible;
    }

    private boolean containsGrade() {
        return mAssignment != null && mAssignment.getSubmission() != null && mAssignment.getSubmission().getGrade() != null && !mAssignment.getSubmission().getGrade().equals("null");
    }

    private boolean isExcused() {
        return mAssignment != null && mAssignment.getSubmission() != null && mAssignment.getSubmission().isExcused();
    }

    private boolean isGradeLetterOrPercentage(String grade) {
        return grade.contains("%") || grade.matches("[a-zA-Z]+");
    }

    @Nullable
    private String getCurrentGrade() {
        String pointsPossible = getPointsPossible();
        if (isExcused()) {
            return getContext().getString(R.string.grade) + "\n" + getContext().getString(R.string.excused) + " / " + pointsPossible;
        }
        if (containsGrade()) {
            String grade = mAssignment.getSubmission().getGrade();
            if (isGradeLetterOrPercentage(grade)) {
                return getContext().getString(R.string.grade) + "\n" + grade;
            } else {
                return getContext().getString(R.string.grade) + "\n" + grade + " / " + pointsPossible;
            }
        }
        return null;
    }

    private String getCurrentPoints() {
        String pointsPossible = getPointsPossible();
        if (isExcused()) {
            return null;
        }
        if (containsGrade()) {
            String grade = mAssignment.getSubmission().getGrade();
            if (isGradeLetterOrPercentage(grade)) {
                return getContext().getString(R.string.points) + "\n" +  mAssignment.getSubmission().getScore() + " / " + pointsPossible;
            } else {
                return null;
            }
        } else {
            //the user doesn't have a grade, but we should display points possible if the mAssignment isn't null
            if(mAssignment != null) {
                //if the user is a teacher show them how many points are possible for the mAssignment
                if(((Course)mCanvasContext).isTeacher()) {
                    return getContext().getString(R.string.pointsPossibleNoPeriod) + "\n" + pointsPossible;
                } else {
                    return getContext().getString(R.string.points) + "\n" + "- / " + pointsPossible;
                }
            }
        }
        return getContext().getString(R.string.points) + "\n" + "- / -";
    }

    // endregion

    // region Expandable Callbacks
    @Override
    public GroupSortedList.GroupComparatorCallback<RubricCriterion> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<RubricCriterion>() {
            @Override
            public int compare(RubricCriterion o1, RubricCriterion o2) {
                // Always put the TopViewHeader at the top
                if (o1 == o2 && o1 == mTopViewHeader) {
                    return 0;
                } else if (o1 == mTopViewHeader) {
                    return -1;
                } else if (o2 == mTopViewHeader) {
                    return 1;
                }
                    return mInsertedOrderHash.get(o1.getId()) - mInsertedOrderHash.get(o2.getId());
                }

            @Override
            public boolean areContentsTheSame(RubricCriterion oldGroup, RubricCriterion newGroup) {
                return oldGroup.getDescription().equals(newGroup.getDescription());
            }

            @Override
            public boolean areItemsTheSame(RubricCriterion group1, RubricCriterion group2) {
                return group1.getId().equals(group2.getId());
            }

            @Override
            public long getUniqueGroupId(RubricCriterion group) {
                return group.getId().hashCode();
            }

            @Override
            public int getGroupType(RubricCriterion group) {
                if (group == mTopViewHeader) {
                    return Types.TYPE_TOP_HEADER;
                } else {
                    return Types.TYPE_HEADER;
                }
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<RubricCriterion, RubricItem> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<RubricCriterion, RubricItem>() {
            @Override
            public int compare(RubricCriterion group, RubricItem o1, RubricItem o2) {
                // put comments at the bottom
                if (o1 instanceof RubricCommentItem && o2 instanceof RubricCommentItem) {
                    return 0;
                } else if (o1 instanceof RubricCommentItem) {
                    return 1;
                } else if (o2 instanceof RubricCommentItem) {
                    return -1;
                }
                RubricCriterionRating r1 = ((RubricRatingItem)o1).getRating();
                RubricCriterionRating r2 = ((RubricRatingItem)o2).getRating();
                return Double.compare(r2.getPoints(), r1.getPoints());
            }

            @Override
            public boolean areContentsTheSame(RubricItem oldItem, RubricItem newItem) {
                if (newItem instanceof RubricCommentItem || oldItem instanceof RubricCommentItem) {
                    // if its a comment always refresh the layout
                    return false;
                } else {
                    RubricCriterionRating oldRating = ((RubricRatingItem) oldItem).getRating();
                    RubricCriterionRating newRating = ((RubricRatingItem) newItem).getRating();
                    return !(oldRating.getDescription() == null || newRating.getDescription() == null)
                            && oldRating.getDescription().equals(newRating.getDescription())
                            && oldRating.getPoints() == newRating.getPoints();
                }
            }

            @Override
            public boolean areItemsTheSame(RubricItem item1, RubricItem item2) {
                if (item1 instanceof RubricCommentItem ^ item2 instanceof RubricCommentItem) {
                    return false;
                } else if (item1 instanceof RubricCommentItem) {
                    return ((RubricCommentItem) item1).getComment().equals(((RubricCommentItem) item2).getComment());
                } else {
                    return ((RubricRatingItem) item1).getRating().getId().equals(((RubricRatingItem) item2).getRating().getId());
                }
            }

            @Override
            public long getUniqueItemId(RubricItem item) {
                if (item instanceof RubricCommentItem) {
                    return ((RubricCommentItem) item).getComment().hashCode();
                } else {
                    return ((RubricRatingItem)item).getRating().getId().hashCode();
                }
            }

            @Override
            public int getChildType(RubricCriterion group, RubricItem item) {
                return (item instanceof RubricCommentItem) ? RubricViewHolder.TYPE_ITEM_COMMENT : RubricViewHolder.TYPE_ITEM_POINTS;
            }
        };
    }
    // endregion

    // region Getter & Setters

    public Assignment getAssignment() {
        return mAssignment;
    }

    public void setAssignment(Assignment assignment) {
        this.mAssignment = assignment;
    }


    // endregion

}
