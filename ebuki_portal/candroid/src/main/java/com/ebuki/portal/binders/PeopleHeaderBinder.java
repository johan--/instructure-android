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


import android.content.Context;
import android.view.View;

import com.ebuki.portal.R;
import com.ebuki.portal.holders.PeopleHeaderViewHolder;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked;
import com.instructure.pandautils.utils.ColorUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorInflater;
import com.nineoldandroids.animation.ObjectAnimator;

public class PeopleHeaderBinder extends BaseBinder {
    public static <MODEL> void bind(
            final Context context,
            final CanvasContext canvasContext,
            final PeopleHeaderViewHolder holder,
            final MODEL genericHeader,
            final String headerText,
            boolean isExpanded,
            final ViewHolderHeaderClicked<MODEL> viewHolderHeaderClicked) {

        holder.title.setText(headerText);
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

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolderHeaderClicked.viewClicked(v, genericHeader);
                int animationType;
                if (holder.isExpanded) {
                    animationType = R.animator.rotation_from_neg90_to_0;
                } else {
                    animationType = R.animator.rotation_from_0_to_neg90;
                    setInvisible(holder.divider);

                }
                holder.isExpanded = !holder.isExpanded;
                final ObjectAnimator flipAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(v.getContext(), animationType);
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
    }
}
