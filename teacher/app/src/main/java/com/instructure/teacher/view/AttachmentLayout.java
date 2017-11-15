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

package com.instructure.teacher.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.canvasapi2.models.Attachment;
import com.instructure.canvasapi2.models.RemoteFile;
import com.instructure.teacher.R;

import java.util.List;


public class AttachmentLayout extends ViewGroup {

    private int mSpacing;
    private int mColumnCount = 0;

    private int mPreviewChildCount = 3;

    public AttachmentLayout(Context context) {
        super(context);
        init(null);
    }

    public AttachmentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AttachmentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AttachmentLayout);
            int idx;
            for (int i = 0; i < a.getIndexCount(); i++) {
                idx = a.getIndex(i);
                switch (idx) {
                    case R.styleable.AttachmentLayout_al_previewChildCount:
                        mPreviewChildCount = a.getInteger(idx, mPreviewChildCount);
                        break;
                    case R.styleable.AttachmentLayout_al_spacing:
                        mSpacing = (int) a.getDimension(idx, 0);
                        break;
                }
            }
            a.recycle();
        }

        // Add dummy child views in preview mode
        if (isInEditMode() && getChildCount() == 0) {
            for (int i = 0; i < mPreviewChildCount; i++)
                addView(new AttachmentView(getContext()));
        }
    }

    public void setPendingAttachments(List<RemoteFile> attachments, boolean removeViewOnAction, AttachmentView.AttachmentClickedCallback<RemoteFile> callback) {
        removeAllViews();
        for (RemoteFile attachment : attachments) {
            AttachmentView attachmentView = new AttachmentView(getContext());
            attachmentView.setPendingAttachment(attachment, removeViewOnAction, callback);
            addView(attachmentView);
        }
    }

    public void setAttachments(List<Attachment> attachments, AttachmentView.AttachmentClickedCallback<Attachment> callback) {
        removeAllViews();
        for (Attachment attachment : attachments) {
            AttachmentView attachmentView = new AttachmentView(getContext());
            attachmentView.setAttachment(attachment, callback);
            addView(attachmentView);
        }
    }

    public void setAttachment(Attachment attachment, AttachmentView.AttachmentClickedCallback<Attachment> callback) {
        removeAllViews();
        AttachmentView attachmentView = new AttachmentView(getContext());
        attachmentView.setAttachment(attachment, callback);
        addView(attachmentView);
    }

    public void setRemoteFileAttachments(List<RemoteFile> attachments, AttachmentView.AttachmentClickedCallback<RemoteFile> callback) {
        removeAllViews();
        for (RemoteFile attachment : attachments) {
            AttachmentView attachmentView = new AttachmentView(getContext());
            attachmentView.setAttachment(attachment, callback);
            addView(attachmentView);
        }
    }

    public void clearAttachmentViews() {
        removeAllViews();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        // Skip layout if there are no children
        if (getChildCount() == 0) return;

        int row, column;

        int childWidth = getChildAt(0).getMeasuredWidth();
        int childHeight = getChildAt(0).getMeasuredHeight();

        int start = getPaddingStart();
        int top = getPaddingTop();

        int childStart, childTop, childEnd, childBottom;

        for (int i = 0; i < getChildCount(); i++) {
            row = i / mColumnCount;
            column = i % mColumnCount;

            childStart = start + (column * childWidth) + (column * mSpacing);
            childTop = top + (row * childHeight) + (row * mSpacing);
            childEnd = childStart + childWidth;
            childBottom = childTop + childHeight;

            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                getChildAt(i).layout(getWidth() - childEnd, childTop, getWidth() - childStart, childBottom);
            } else {
                getChildAt(i).layout(childStart, childTop, childEnd, childBottom);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // No dimensions if there are no children
        if (getChildCount() == 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        // Ensure we're working with homogeneous child views
        for (int i = 0; i < getChildCount(); i++) {
            if (!(getChildAt(i) instanceof AttachmentView))
                throw new IllegalStateException("AttachmentLayout can only contain views of type Attachment");
        }

        // Establish max available width
        int widthSpec = MeasureSpec.getSize(widthMeasureSpec);

        // Get padding
        int horizontalPadding = getPaddingStart() + getPaddingEnd();
        int verticalPadding = getPaddingBottom() + getPaddingTop();

        // Make sure the children get measured
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        // Get child height. Children are homogeneous and should all have the same measured dimensions
        View firstChild = getChildAt(0);
        int childWidth = firstChild.getMeasuredWidth();
        int childHeight = firstChild.getMeasuredHeight();

        // Determine how many columns we can fit
        mColumnCount = 1;
        while (horizontalPadding + ((mColumnCount + 1) * childWidth) + (mColumnCount * mSpacing) < widthSpec) {
            mColumnCount++;
        }

        // Determine how many rows we need
        int rowCount = 1 + ((getChildCount() - 1) / mColumnCount);

        // Calculate final dimensions
        int measureWidth = horizontalPadding + (mColumnCount * childWidth) + ((mColumnCount - 1) * mSpacing);

        //noinspection deprecation
        if (getLayoutParams().width == LayoutParams.MATCH_PARENT || getLayoutParams().width == LayoutParams.FILL_PARENT) {
            measureWidth = Math.max(widthSpec, measureWidth);
        }

        int measureHeight = verticalPadding + (rowCount * childHeight) + ((rowCount - 1) * mSpacing);

        setMeasuredDimension(measureWidth, measureHeight);
    }
}
