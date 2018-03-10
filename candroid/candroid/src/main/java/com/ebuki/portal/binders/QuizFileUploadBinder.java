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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.ebuki.portal.R;
import com.ebuki.portal.holders.QuizFileUploadViewHolder;
import com.ebuki.portal.interfaces.QuizFileRemovedListener;
import com.ebuki.portal.interfaces.QuizFileUploadListener;
import com.ebuki.portal.interfaces.QuizToggleFlagState;
import com.instructure.canvasapi2.models.Attachment;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.QuizSubmissionQuestion;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.views.CanvasWebView;


public class QuizFileUploadBinder extends BaseBinder{
    public static void bind(final QuizFileUploadViewHolder holder,
                            final Context context,
                            CanvasWebView.CanvasWebViewClientCallback webViewClientCallback,
                            CanvasWebView.CanvasEmbeddedWebViewCallback callback,
                            final QuizSubmissionQuestion quizSubmissionQuestion,
                            int courseColor,
                            final QuizToggleFlagState flagStateCallback,
                            final CanvasContext canvasContext,
                            final int position,
                            final QuizFileUploadListener quizFileUploadListener,
                            final boolean shouldLetAnswer,
                            final Attachment attachment,
                            final boolean isLoading,
                            final QuizFileRemovedListener quizFileRemovedListener) {
        if(holder == null) {
            return;
        }

        if(isLoading) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
            holder.fileName.setText(R.string.loading);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.fileName.setText("");
        }

        //Configure file holder
        if(attachment != null && attachment.getDisplayName() != null) {
            holder.fileName.setText(attachment.getDisplayName());
            holder.fileIcon.setVisibility(View.VISIBLE);
            holder.remove.setVisibility(View.VISIBLE);
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    quizFileRemovedListener.quizFileUploadRemoved(quizSubmissionQuestion.getId(), position);
                }
            });
            if(attachment.getContentType().contains("image")){
                holder.fileIcon.setImageDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_image, canvasContext));
            }else{
                holder.fileIcon.setImageDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_document, canvasContext));
            }
        } else if(quizSubmissionQuestion.getAnswer() != null) {
            //This means the user has previously uploaded a file
            holder.fileName.setText(context.getString(R.string.fileUploadSuccess));
            holder.fileIcon.setVisibility(View.VISIBLE);
            holder.fileIcon.setImageDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_document, canvasContext));
            holder.remove.setVisibility(View.VISIBLE);
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    quizFileRemovedListener.quizFileUploadRemoved(quizSubmissionQuestion.getId(), position);
                }
            });
        } else {

            holder.fileIcon.setVisibility(View.GONE);
            holder.remove.setVisibility(View.GONE);
        }

        holder.question.loadUrl("about:blank");
        holder.question.setCanvasWebViewClientCallback(webViewClientCallback);

        holder.question.formatHTML(quizSubmissionQuestion.getQuestionText(), "");
        holder.question.setBackgroundColor(Color.TRANSPARENT);
        holder.question.setCanvasEmbeddedWebViewCallback(callback);
        holder.questionNumber.setText(context.getString(R.string.question) + " " + (position + 1));

        final Drawable courseColorFlag = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_bookmark_fill_grey, courseColor);

        if(quizSubmissionQuestion.isFlagged()) {
            holder.flag.setImageDrawable(courseColorFlag);
        } else {
            holder.flag.setImageResource(R.drawable.ic_bookmark_outline_grey);
        }

        if(shouldLetAnswer) {
            holder.flag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (quizSubmissionQuestion.isFlagged()) {
                        //unflag it
                        holder.flag.setImageResource(R.drawable.ic_bookmark_outline_grey);
                        flagStateCallback.toggleFlagged(false, quizSubmissionQuestion.getId());
                        quizSubmissionQuestion.setFlagged(false);
                    } else {
                        //flag it
                        holder.flag.setImageDrawable(courseColorFlag);
                        flagStateCallback.toggleFlagged(true, quizSubmissionQuestion.getId());
                        quizSubmissionQuestion.setFlagged(true);
                    }
                }
            });
        }

        if(shouldLetAnswer) {
            holder.uploadFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    quizFileUploadListener.onFileUploadClicked(quizSubmissionQuestion.getId(), position);
                }
            });
        }
    }
}
