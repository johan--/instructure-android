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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.canvasapi2.models.Attachment;
import com.instructure.canvasapi2.models.RemoteFile;
import com.instructure.teacher.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AttachmentView extends FrameLayout {

    @BindView(R.id.preview_image)
    ImageView mPreview;
    @BindView(R.id.attachment_icon)
    ImageView mIcon;
    @BindView(R.id.attachment_name)
    TextView mName;
    @BindView(R.id.attachment_action_button)
    ImageButton mActionButton;

    private AttachmentClickedCallback mClickCallback;

    public enum AttachmentAction { PREVIEW, DOWNLOAD, REMOVE }

    public interface AttachmentClickedCallback<ATTACHMENT> {
        void onAttachmentClicked(AttachmentAction action, ATTACHMENT attachment);
    }

    public AttachmentView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.view_attachment, this, true);
        ButterKnife.bind(this);
    }

    // Picasso transformation to apply gray overlay on thumbnail
    public static Transformation ATTACHMENT_PREVIEW_TRANSFORMER = new Transformation() {
        @Override
        public Bitmap transform(Bitmap source) {
            if (source == null) return null;
            Bitmap mutableSource = source.copy(source.getConfig(), true);
            source.recycle();
            Canvas canvas = new Canvas(mutableSource);
            canvas.drawColor(0xBB9B9B9B);
            return mutableSource;
        }

        @Override
        public String key() {
            return "gray-overlay";
        }
    };

    public void setPendingAttachment(@NonNull final RemoteFile attachment, final boolean removeViewOnAction, @NonNull AttachmentClickedCallback<RemoteFile> callback) {

        mClickCallback = callback;

        mName.setText(attachment.getDisplayName());

        setColorAndIcon(getContext(), attachment.getContentType(), attachment.getFileName(), mPreview, mIcon);

        setThumbnail(attachment.getThumbnailUrl());

        mActionButton.setImageResource(R.drawable.vd_close);
        mActionButton.setContentDescription(getContext().getString(R.string.remove_attachment));
        mActionButton.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                if (removeViewOnAction && getParent() != null) {
                    ((ViewGroup) getParent()).removeView(AttachmentView.this);
                }
                mClickCallback.onAttachmentClicked(AttachmentAction.REMOVE, attachment);
            }
        });

    }

    public void setPendingAttachment(@NonNull final Attachment attachment, final boolean removeViewOnAction, AttachmentClickedCallback<Attachment> callback) {

        mClickCallback = callback;

        mName.setText(attachment.getDisplayName());

        setColorAndIcon(getContext(), attachment.getContentType(), attachment.getFilename(), mPreview, mIcon);

        setThumbnail(attachment.getThumbnailUrl());

        mActionButton.setImageResource(R.drawable.vd_close);
        mActionButton.setContentDescription(getContext().getString(R.string.remove_attachment));
        mActionButton.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                if (removeViewOnAction && getParent() != null) {
                    ((ViewGroup) getParent()).removeView(AttachmentView.this);
                }
                mClickCallback.onAttachmentClicked(AttachmentAction.REMOVE, attachment);
            }
        });

    }

    public void setAttachment(@NonNull final Attachment attachment, @NonNull AttachmentClickedCallback<Attachment> callback) {

        mClickCallback = callback;

        mName.setText(attachment.getDisplayName());

        setColorAndIcon(getContext(), attachment.getContentType(), attachment.getFilename(), mPreview, mIcon);

        setThumbnail(attachment.getThumbnailUrl());

        setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                if (mClickCallback != null)
                    mClickCallback.onAttachmentClicked(AttachmentAction.PREVIEW, attachment);
            }
        });

        mActionButton.setImageResource(R.drawable.vd_download);
        mActionButton.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                mClickCallback.onAttachmentClicked(AttachmentAction.DOWNLOAD, attachment);
            }
        });
    }

    public void setAttachment(@NonNull final RemoteFile attachment, @NonNull AttachmentClickedCallback<RemoteFile> callback) {

        mClickCallback = callback;

        mName.setText(attachment.getDisplayName());

        setColorAndIcon(getContext(), attachment.getContentType(), attachment.getFileName(), mPreview, mIcon);

        setThumbnail(attachment.getThumbnailUrl());

        setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                if (mClickCallback != null)
                    mClickCallback.onAttachmentClicked(AttachmentAction.PREVIEW, attachment);
            }
        });

        mActionButton.setImageResource(R.drawable.vd_download);
        mActionButton.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                mClickCallback.onAttachmentClicked(AttachmentAction.DOWNLOAD, attachment);
            }
        });
    }

    public void setThumbnail(@Nullable String path) {
        if (path == null) return;
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            Picasso.with(getContext()).load(file).fit().centerCrop().transform(ATTACHMENT_PREVIEW_TRANSFORMER).into(mPreview);
        } else {
            Picasso.with(getContext()).load(path).fit().centerCrop().transform(ATTACHMENT_PREVIEW_TRANSFORMER).into(mPreview);
        }
    }

    public static void setColorAndIcon(Context context, String contentType, String filename, @Nullable ImageView preview, ImageView icon) {

        if (contentType == null) contentType = "";
        if (filename == null) filename = "";

        // Default icon and color (misc)
        int iconRes = R.drawable.vd_attachment;
        int colorRes = R.color.attachment_color_misc;

        // Image
        if (contentType.startsWith("image")) {
            colorRes = R.color.attachment_color_image;
            iconRes = R.drawable.vd_image;

        // Video
        } else if (contentType.startsWith("video")) {
            colorRes = R.color.attachment_color_video;
            iconRes = R.drawable.vd_media;

        // Audio
        } else if (contentType.startsWith("audio")) {
            colorRes = R.color.attachment_color_audio;
            iconRes = R.drawable.vd_audio;

        } else {

            String extension = "";
            int idx = filename.lastIndexOf('.');
            if (idx > 0) extension = filename.substring(idx + 1).toLowerCase();

            switch (extension) {
                // Document
                case "doc":
                case "docx":
                    colorRes = R.color.attachment_color_doc;
                    iconRes = R.drawable.vd_document;
                    break;

                // Text
                case "txt":
                    colorRes = R.color.attachment_color_txt;
                    iconRes = R.drawable.vd_document;
                    break;

                // Rich text
                case "rtf":
                    colorRes = R.color.attachment_color_txt;
                    iconRes = R.drawable.vd_document;
                    break;

                // PDF
                case "pdf":
                    colorRes = R.color.attachment_color_pdf;
                    iconRes = R.drawable.vd_pdf;
                    break;

                // Spreadsheet
                case "xls":
                    colorRes = R.color.attachment_color_xls;
                    iconRes = R.drawable.vd_document;
                    break;

                // Archive
                case "zip":
                case "tar":
                case "7z":
                case "apk":
                case "jar":
                case "rar":
                    colorRes = R.color.attachment_color_zip;
                    iconRes = R.drawable.vd_attachment;
                    break;
            }
        }

        if(preview != null) {
            preview.setBackgroundColor(ContextCompat.getColor(context, colorRes));
        }
        icon.setImageResource(iconRes);
    }
}
