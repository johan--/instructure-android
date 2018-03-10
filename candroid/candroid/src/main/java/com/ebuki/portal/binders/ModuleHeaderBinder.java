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

package com.instructure.candroid.binders;

import android.content.Context;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.ModuleHeaderViewHolder;
import com.instructure.candroid.util.ModuleUtility;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.ModuleObject;
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.ColorUtils;
import com.nineoldandroids.animation.Animator;

public class ModuleHeaderBinder extends BaseBinder {

    public static void bind(
            final ModuleHeaderViewHolder holder,
            final ModuleObject moduleObject,
            final Context context,
            final CanvasContext canvasContext,
            final int groupCount,
            final ViewHolderHeaderClicked<ModuleObject> viewHolderHeaderClicked,
            final boolean isExpanded) {


        if (holder == null) { return; }

        boolean isLocked = ModuleUtility.isGroupLocked(moduleObject);

        holder.isExpanded = isExpanded;
        if (!isExpanded) {
            holder.expandCollapse.setRotation(0);
            setVisible(holder.divider);
        } else {
            holder.expandCollapse.setRotation(180);
            setInvisible(holder.divider);
        }

        final int color = context.getResources().getColor(R.color.canvasTextMedium);
        holder.expandCollapse.setImageDrawable(ColorUtils.colorIt(color, context.getResources().getDrawable(R.drawable.ic_cv_expand_black)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolderHeaderClicked.viewClicked(v, moduleObject);
                int animationType;
                if (holder.isExpanded) {
                    animationType = R.animator.rotation_from_neg90_to_0;
                } else {
                    animationType = R.animator.rotation_from_0_to_neg90;
                    setInvisible(holder.divider);
                }
                holder.isExpanded = !holder.isExpanded;
                final com.nineoldandroids.animation.ObjectAnimator flipAnimator = (com.nineoldandroids.animation.ObjectAnimator) com.nineoldandroids.animation.AnimatorInflater.loadAnimator(v.getContext(), animationType);
                flipAnimator.setTarget(holder.expandCollapse);
                flipAnimator.setDuration(200);
                flipAnimator.start();
                //make the dividers visible/invisible after the animation
                flipAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!holder.isExpanded) {
                            setVisible(holder.divider);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        });

        holder.title.setText(moduleObject.getName());

        //reset the status text and drawable to default state
        if (moduleObject.getState() != null) {
            if (isLocked) {
                holder.moduleStatus.setImageDrawable(ColorUtils.colorIt(color, context.getResources().getDrawable(R.drawable.ic_cv_locked_fill)));
            } else if (moduleObject.getState().equals(ModuleObject.STATE.completed.toString())) {
                //if the instructor doesn't add any completion requirements, the module will already be complete
                final int courseColor = CanvasContextColor.getCachedColor(context, canvasContext);
                holder.moduleStatus.setImageDrawable(ColorUtils.colorIt(courseColor, context.getResources().getDrawable(R.drawable.ic_cv_check_fill)));
            } else {
                holder.moduleStatus.setImageDrawable(ColorUtils.colorIt(color, context.getResources().getDrawable(R.drawable.ic_cv_circle)));
            }
        } else {
            holder.moduleStatus.setImageDrawable(ColorUtils.colorIt(color, context.getResources().getDrawable(R.drawable.ic_cv_circle)));
        }
    }
}
