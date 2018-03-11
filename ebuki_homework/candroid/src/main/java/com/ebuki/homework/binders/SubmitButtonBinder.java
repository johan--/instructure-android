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

package com.ebuki.homework.binders;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.ebuki.homework.R;
import com.ebuki.homework.adapter.QuizSubmissionQuestionListRecyclerAdapter;
import com.ebuki.homework.holders.SubmitButtonViewHolder;
import com.ebuki.homework.interfaces.QuizSubmit;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.pandautils.utils.CanvasContextColor;

public class SubmitButtonBinder {

    public static void bind(SubmitButtonViewHolder holder, final Context context, CanvasContext canvasContext, final QuizSubmissionQuestionListRecyclerAdapter adapter, final QuizSubmit callback) {

        if(holder == null) {
            return;
        }

        int[] colors = CanvasContextColor.getCachedColors(context, canvasContext);
        StateListDrawable stateListDrawable=new StateListDrawable();
        if(colors.length >= 2) {
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(colors[1]));
            stateListDrawable.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(colors[0]));
            holder.submitButton.setBackgroundDrawable(stateListDrawable);
        } else {
            //it shouldn't get here, but this will just set the color as the course color, so no pressed state
            holder.submitButton.setBackgroundColor(CanvasContextColor.getCachedColor(context, canvasContext));
        }

        holder.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check to see if there are unanswered quiz questions
                boolean hasUnanswered = false;
                int numUnanswered = 0;
                if(adapter.getAnsweredQuestions().size() != adapter.getAnswerableQuestionCount()) {
                    hasUnanswered = true;
                    numUnanswered = (adapter.getAnswerableQuestionCount()) - adapter.getAnsweredQuestions().size();
                }

                String message;

                if(hasUnanswered) {
                    message = context.getResources().getQuantityString(R.plurals.unanswered_quiz_questions, numUnanswered, numUnanswered);
                } else {
                    message = context.getString(R.string.areYouSure);
                }

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle(R.string.submitQuiz)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                callback.submitQuiz();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setCancelable(false)
                        .create();

                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }
}
