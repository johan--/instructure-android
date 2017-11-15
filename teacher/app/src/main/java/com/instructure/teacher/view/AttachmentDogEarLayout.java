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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.instructure.teacher.R;


public class AttachmentDogEarLayout extends FrameLayout {

    private static final float DOG_EAR_DIMEN_DP = 20f;
    private static final float DOG_EAR_SHADOW_OFFSET_MULTIPLIER_X = 0.08f;
    private static final float DOG_EAR_SHADOW_OFFSET_MULTIPLIER_Y = 0.15f;

    private float mDogEarSize;
    private Path mClipPath;
    private Path mDogEarPath;
    private Path mDogEarShadowPath;
    private Paint mDogEarPaint;
    private Paint mDogEarShadowPaint;
    private PointF mDogEarPoint;
    private Matrix mRtlFlipMatrix;

    public AttachmentDogEarLayout(Context context) {
        super(context);
        init(null);
    }

    public AttachmentDogEarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AttachmentDogEarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AttachmentDogEarLayout);
            if (a.hasValue(R.styleable.AttachmentDogEarLayout_adl_dogear_size)) {
                mDogEarSize = a.getDimension(R.styleable.AttachmentDogEarLayout_adl_dogear_size, DOG_EAR_DIMEN_DP);
            } else {
                mDogEarSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DOG_EAR_DIMEN_DP, getResources().getDisplayMetrics());
            }
            a.recycle();
        }

        initPaint();

        /* Non-rectangular path clipping in hardware only works on API 18+, so we'll
        use a software layer for API 17. If this causes performance issues then we'll
        need to perform bitmap clipping using xfermode. Alternatively, draw everything
        to a bitmap and apply the bitmap as a path shader. */
        if (Build.VERSION.SDK_INT < 18 || isInEditMode()) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void initPaint() {
        mDogEarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDogEarPaint.setColor(0xFFD8D8D8);

        mDogEarShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDogEarShadowPaint.setColor(0x33000000);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        // Perform clip, draw children
        canvas.save();
        canvas.clipPath(getClipPath());
        super.dispatchDraw(canvas);
        canvas.restore();

        // Draw dog-ear
        canvas.drawPath(getDogEarShadowPath(), mDogEarShadowPaint);
        canvas.drawPath(getDogEarPath(), mDogEarPaint);
    }

    private Path getClipPath() {
        if (mClipPath == null) {
            mClipPath = new Path();
            PointF dogEarPoint = getDogEarPoint();
            mClipPath.moveTo(0, 0);
            mClipPath.lineTo(dogEarPoint.x, 0);
            mClipPath.lineTo(getWidth(), dogEarPoint.y);
            mClipPath.lineTo(getWidth(), getHeight());
            mClipPath.lineTo(0, getHeight());
            mClipPath.close();
            flipForRtlIfNecessary(mClipPath);
        }
        return mClipPath;
    }

    private Path getDogEarPath() {
        if (mDogEarPath == null) {
            PointF dogEarPoint = getDogEarPoint();
            mDogEarPath = new Path();
            mDogEarPath.moveTo(dogEarPoint.x, -1f);
            mDogEarPath.lineTo(getWidth() + 1f, dogEarPoint.y);
            mDogEarPath.lineTo(dogEarPoint.x, dogEarPoint.y);
            mDogEarPath.close();
            flipForRtlIfNecessary(mDogEarPath);
        }
        return mDogEarPath;
    }

    private PointF getDogEarPoint() {
        if (mDogEarPoint == null) {
            mDogEarPoint = new PointF(getWidth() - mDogEarSize, mDogEarSize);
        }
        return mDogEarPoint;
    }

    private Path getDogEarShadowPath() {
        if (mDogEarShadowPath == null) {
            PointF dogEarPoint = getDogEarPoint();
            mDogEarShadowPath = new Path();
            mDogEarShadowPath.moveTo(dogEarPoint.x, -1f);
            mDogEarShadowPath.lineTo(getWidth() + 1f, dogEarPoint.y + 1);
            mDogEarShadowPath.lineTo(
                    dogEarPoint.x + (dogEarPoint.y * DOG_EAR_SHADOW_OFFSET_MULTIPLIER_X),
                    dogEarPoint.y + (dogEarPoint.y * DOG_EAR_SHADOW_OFFSET_MULTIPLIER_Y));
            mDogEarShadowPath.close();
            flipForRtlIfNecessary(mDogEarShadowPath);
        }
        return mDogEarShadowPath;
    }

    private void flipForRtlIfNecessary(Path path) {
        if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            if (mRtlFlipMatrix == null) {
                mRtlFlipMatrix = new Matrix();
                mRtlFlipMatrix.postScale(-1, 1, getWidth() / 2f, 0);
            }
            path.transform(mRtlFlipMatrix);
        }
    }
}
