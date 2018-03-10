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

package com.ebuki.portal.binders;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.ebuki.portal.R;
import com.ebuki.portal.activity.ColorPickerActivity;
import com.ebuki.portal.holders.CourseHeaderViewHolder;
import com.ebuki.portal.holders.CourseViewHolder;
import com.ebuki.portal.interfaces.CourseAdapterToFragmentCallback;
import com.ebuki.portal.model.CourseToggleHeader;
import com.ebuki.portal.util.Analytics;
import com.ebuki.portal.util.MGPUtils;
import com.ebuki.portal.util.RouterUtils;
import com.instructure.canvasapi2.utils.NumberHelper;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.views.RippleView;

import java.util.ArrayList;
import java.util.List;

public class CourseBinder extends BaseBinder{

    public static void bind(
            final Activity context,
            final CanvasContext canvasContext,
            final CourseViewHolder holder,
            final ArrayList<CanvasContext> allCourses,
            final boolean showGrades,
            final boolean gradesTabHidden,
            final CourseAdapterToFragmentCallback adapterToFragmentCallback) {

        final int[] currentColors = CanvasContextColor.getCachedColors(context, canvasContext);
        setupViewsByColor(holder.name, currentColors[0], currentColors[0]);

        setupText(canvasContext, holder.name, holder.courseCode, allCourses);
        bindGradeTextView(context, canvasContext, holder.grade, holder.gradeRipple, showGrades, gradesTabHidden);

        holder.overflowRipple.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if(context != null) {
                    Analytics.trackAppFlow(context, ColorPickerActivity.class);
                    Intent intent = ColorPickerActivity.createIntent(context, canvasContext, holder.getAdapterPosition());
                    context.startActivity(intent);
                }
            }
        });

        holder.clickItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canvasContext != null) {
                    adapterToFragmentCallback.onRowClicked(canvasContext);
                }
            }
        });

        if (allCourses.size() > 0) {
            if (allCourses.get(0).getId() == canvasContext.getId()) {
                adapterToFragmentCallback.setupTutorial(holder);
            } else {
                setGone(holder.pulseOveflow);
                setGone(holder.pulseGrade);
            }
        } else {
            setGone(holder.pulseOveflow);
            setGone(holder.pulseGrade);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // Binding Helpers
    //////////////////////////////////////////////////////////////////////////

    public static boolean containsId(List<CanvasContext> list, long id) {
        for (CanvasContext canvasContext : list) {
            if (canvasContext.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static Course findCourseByGroup(List<CanvasContext> list, Group group) {
        for (CanvasContext canvasContext : list) {
            if(canvasContext instanceof Course) {
                if(canvasContext.getId() == group.getCourseId()) {
                    return (Course)canvasContext;
                }
            }
        }
        return null;
    }

    public static void setupText(CanvasContext canvasContext, TextView name, TextView courseCode, ArrayList<CanvasContext> allCourses) {
        //set course name and link to the corresponding course page
        name.setText(canvasContext.getName());
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);
        if (CanvasContext.Type.isCourse(canvasContext)) {
            courseCode.setVisibility(View.VISIBLE);
            courseCode.setText(((Course) canvasContext).getCourseCode());
        } else if (CanvasContext.Type.isGroup(canvasContext)) {
            Group group = (Group) canvasContext;
            //For groups we set the course name as the course code
            if ((group.getCourseId() > 0 && group.getContextType() == Group.GroupContext.Account) || containsId(allCourses, group.getCourseId())) {
                Course course = findCourseByGroup(allCourses, group);
                if(course != null) {
                    //It's a group in a course
                    courseCode.setVisibility(View.VISIBLE);
                    courseCode.setText(course.getName());
                }
            } else {
                //It's an account-level group or the course isn't public yet..
                //Warning: do NOT set the course code gone unless you make the xml work differently, things without a course code will become un-clickable.
                courseCode.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void setupViewsByColor(final TextView text, final int oldColor, final int newColor) {
        //Get the course/group color
        if (oldColor == newColor) {
            text.getBackground().setColorFilter(oldColor, PorterDuff.Mode.SRC_ATOP);
        } else {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, newColor);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final int color = (int) animation.getAnimatedValue();
                    text.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
            });
            colorAnimation.setDuration(500);
            colorAnimation.start();
        }
    }

    public static void bindHeader(final CourseToggleHeader header, final CourseHeaderViewHolder holder) {
        holder.button.setText(header.text);
    }

    private static void bindGradeTextView(final Activity context, final CanvasContext canvasContext, final TextView gradeText, final RippleView rippleView, final boolean showGrades, final boolean gradesTabHidden) {

        //If we show grades, is a course, and the final grade is NOT hidden
        if(showGrades && !gradesTabHidden && CanvasContext.Type.isCourse(canvasContext)
                && !(((Course)canvasContext).isHideFinalGrades()
                || !MGPUtils.isAllGradingPeriodsShown((Course)canvasContext))) {
            Course course = (Course)canvasContext;
            if(course.isTeacher()) {
                rippleView.setEnabled(false);
                rippleView.setClickable(false);
                setGone(gradeText);
                setInvisible(rippleView);
            } else {
                setVisible(gradeText);
                Double grade = ((Course) canvasContext).getCurrentScore();
                String gradeStr = NumberHelper.doubleToPercentage(grade, 1);
                String gradeLetter = "";
                if (((Course) canvasContext).getCurrentGrade() != null) {
                    gradeLetter = ((Course) canvasContext).getCurrentGrade();
                }

                if (course.getCurrentScore() == 0.0 && (course.getCurrentGrade() == null || "null".equals(course.getCurrentGrade()))) {
                    gradeText.setText(context.getString(R.string.noGradeText));
                } else {
                    gradeText.setText(gradeLetter + "  " + gradeStr);
                }

                setVisible(rippleView);
                rippleView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                    @Override
                    public void onComplete(RippleView rippleView) {
                        String url = constructUrl(canvasContext.getId(), context);
                        RouterUtils.routeUrl(context, url, true);
                    }
                });

                rippleView.setEnabled(true);
                rippleView.setClickable(true);
            }
        } else {
            rippleView.setEnabled(false);
            rippleView.setClickable(false);
            setGone(gradeText);
            setInvisible(rippleView);
        }
    }

    private static String constructUrl(long id, Context context){
        return "https://" + ApiPrefs.getDomain() + "/courses/" + id + "/grades";
    }
}
