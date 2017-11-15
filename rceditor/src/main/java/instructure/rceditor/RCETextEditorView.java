/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package instructure.rceditor;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class RCETextEditorView extends RelativeLayout {

    private RCETextEditor mEditor;
    private View mColorPickerView;
    private View mController;
    private View mBottomDivider;

    private @ColorInt int mThemeColor = Color.BLACK;
    private @ColorInt int mButtonColor = Color.BLACK;

    public RCETextEditorView(Context context) {
        super(context);
        init(context, null);
    }

    public RCETextEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RCETextEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public RCETextEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void setHtml(String html, String accessibilityTitle, String hint, @ColorInt int themeColor, @ColorInt int buttonColor) {
        mEditor.applyHtml(html, accessibilityTitle);
        mEditor.setPlaceholder(hint);
        setThemeColor(themeColor);
        mButtonColor = buttonColor;
    }

    public void setHint(String hint) {
        mEditor.setPlaceholder(hint);
    }

    public void setHint(@StringRes int hint) {
        mEditor.setPlaceholder(getContext().getString(hint));
    }

    public void hideEditorToolbar() {
        mController.setVisibility(View.GONE);
        mBottomDivider.setVisibility(View.GONE);
        mColorPickerView.setVisibility(View.GONE);
    }

    public void showEditorToolbar() {
        mController.setVisibility(View.VISIBLE);
        mBottomDivider.setVisibility(View.VISIBLE);
    }

    public boolean toolbarVisible() {
        return mController.getVisibility() == View.VISIBLE;
    }

    @Nullable
    public String getHtml() {
        return mEditor.getHtml();
    }

    /**
     * Takes care of making the label darker or lighter depending on when it's focused
     * @param label TextView label that is usually above the RCE view
     * @param focusedColor Color you want the label to be when focused
     * @param defaultColor Color you want the label to be when unfocused
     */
    public void setLabel(final TextView label, final int focusedColor, final int defaultColor) {
        mEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if(focused) {
                    label.setTextColor(ContextCompat.getColor(getContext(), focusedColor));
                } else {
                    label.setTextColor(ContextCompat.getColor(getContext(), defaultColor));
                }
            }
        });
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        inflate(getContext(), R.layout.rce_text_editor_view, this);

        mController = findViewById(R.id.rce_controller);
        mBottomDivider = findViewById(R.id.rce_bottomDivider);
        mColorPickerView = findViewById(R.id.rce_colorPickerWrapper);
        mEditor = (RCETextEditor) findViewById(R.id.rce_webView);

        if(attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.RCETextEditorView, 0, 0);
            try {
                if(ta.hasValue(R.styleable.RCETextEditorView_rce_editor_padding)) {
                    final int editorPadding = convertDpToPx(ta.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_editor_padding, 0));
                    mEditor.setPadding(editorPadding, editorPadding, editorPadding, editorPadding);
                } else {
                    final int editorPaddingStart = convertDpToPx(ta.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_editor_padding_start, 0));
                    final int editorPaddingEnd = convertDpToPx(ta.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_editor_padding_end, 0));
                    final int editorPaddingTop = convertDpToPx(ta.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_editor_padding_top, 0));
                    final int editorPaddingBottom = convertDpToPx(ta.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_editor_padding_bottom, 0));
                    mEditor.setPadding(editorPaddingStart, editorPaddingTop, editorPaddingEnd, editorPaddingBottom);
                }

                final int controlsMarginStart = ta.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_controls_margin_start, 0);
                final int controlsMarginEnd = ta.getDimensionPixelSize(R.styleable.RCETextEditorView_rce_controls_margin_end, 0);

                MarginLayoutParams controlLayoutParams = (MarginLayoutParams) mController.getLayoutParams();
                controlLayoutParams.setMarginStart(controlsMarginStart);
                controlLayoutParams.setMarginEnd(controlsMarginEnd);
                MarginLayoutParams dividerLayoutParams = (MarginLayoutParams) mBottomDivider.getLayoutParams();
                dividerLayoutParams.setMarginStart(controlsMarginStart);
                dividerLayoutParams.setMarginEnd(controlsMarginEnd);
                MarginLayoutParams colorPickerLayoutParams = (MarginLayoutParams) mColorPickerView.getLayoutParams();
                colorPickerLayoutParams.setMarginStart(controlsMarginStart);
                colorPickerLayoutParams.setMarginEnd(controlsMarginEnd);

                boolean controlsVisible = ta.getBoolean(R.styleable.RCETextEditorView_rce_controls_visible, true);
                if(controlsVisible) showEditorToolbar(); else hideEditorToolbar();

            } finally {
                if(ta != null) {
                    ta.recycle();
                }
            }
        }

        findViewById(R.id.rce_colorPickerWhite).setOnClickListener(onColorChosen);
        findViewById(R.id.rce_colorPickerBlack).setOnClickListener(onColorChosen);
        findViewById(R.id.rce_colorPickerGray).setOnClickListener(onColorChosen);
        findViewById(R.id.rce_colorPickerRed).setOnClickListener(onColorChosen);
        findViewById(R.id.rce_colorPickerOrange).setOnClickListener(onColorChosen);
        findViewById(R.id.rce_colorPickerYellow).setOnClickListener(onColorChosen);
        findViewById(R.id.rce_colorPickerGreen).setOnClickListener(onColorChosen);
        findViewById(R.id.rce_colorPickerBlue).setOnClickListener(onColorChosen);
        findViewById(R.id.rce_colorPickerPurple).setOnClickListener(onColorChosen);

        findViewById(R.id.action_bold).setOnClickListener(onBold);
        findViewById(R.id.action_italic).setOnClickListener(onItalic);
        findViewById(R.id.action_underline).setOnClickListener(onUnderline);
        findViewById(R.id.action_txt_color).setOnClickListener(onTextColor);
        findViewById(R.id.action_undo).setOnClickListener(onUndo);
        findViewById(R.id.action_redo).setOnClickListener(onRedo);
        findViewById(R.id.action_insert_bullets).setOnClickListener(onInsertBulletList);
        findViewById(R.id.action_insert_numbers).setOnClickListener(onInsertNumberedList);
        findViewById(R.id.action_insert_image).setOnClickListener(onInsertPicture);
        findViewById(R.id.action_insert_link).setOnClickListener(onInsertLink);

        mEditor.setOnDecorationChangeListener(new RichEditor.OnDecorationStateListener() {
            @Override
            public void onStateChangeListener(String text, List<RichEditor.Type> types) {
                if(!toolbarVisible()) {
                    showEditorToolbar();
                }
            }
        });
    }

    public void setThemeColor(@ColorInt int color) {
        mThemeColor = color;
    }

    private int convertDpToPx(int value) {
        return  (int) (value / Resources.getSystem().getDisplayMetrics().density);
    }

    private View.OnClickListener onColorChosen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int id = v.getId();

            if(R.id.rce_colorPickerWhite == id) {
                mEditor.setTextColor(Color.WHITE);
            } else if(R.id.rce_colorPickerBlack == id) {
                mEditor.setTextColor(Color.BLACK);
            } else if(R.id.rce_colorPickerGray == id) {
                mEditor.setTextColor(ContextCompat.getColor(getContext(), R.color.rce_pickerGray));
            } else if(R.id.rce_colorPickerRed == id) {
                mEditor.setTextColor(ContextCompat.getColor(getContext(), R.color.rce_pickerRed));
            } else if(R.id.rce_colorPickerOrange == id) {
                mEditor.setTextColor(ContextCompat.getColor(getContext(), R.color.rce_pickerOrange));
            } else if(R.id.rce_colorPickerYellow == id) {
                mEditor.setTextColor(ContextCompat.getColor(getContext(), R.color.rce_pickerYellow));
            } else if(R.id.rce_colorPickerGreen == id) {
                mEditor.setTextColor(ContextCompat.getColor(getContext(), R.color.rce_pickerGreen));
            } else if(R.id.rce_colorPickerBlue == id) {
                mEditor.setTextColor(ContextCompat.getColor(getContext(), R.color.rce_pickerBlue));
            } else if(R.id.rce_colorPickerPurple == id) {
                mEditor.setTextColor(ContextCompat.getColor(getContext(), R.color.rce_pickerPurple));
            }

            toggleColorPicker();
        }
    };

    private View.OnClickListener onTextColor = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleColorPicker();
        }
    };

    private View.OnClickListener onUndo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.undo();
        }
    };

    private View.OnClickListener onRedo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.redo();
        }
    };

    private View.OnClickListener onBold = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.setBold();
        }
    };

    private View.OnClickListener onItalic = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.setItalic();
        }
    };

    private View.OnClickListener onUnderline = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.setUnderline();
        }
    };

    private View.OnClickListener onInsertBulletList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.setBullets();
        }
    };

    private View.OnClickListener onInsertNumberedList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.setNumbers();
        }
    };

    private View.OnClickListener onInsertPicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager fm = getFragmentManager();
            if(fm == null) return;

            RCEInsertDialog dialog = RCEInsertDialog.newInstance(getContext().getString(R.string.rce_insertImage), mThemeColor, mButtonColor);
            dialog.setListener(new RCEInsertDialog.OnResultListener() {
                @Override
                public void onResults(String url, String alt) {
                    mEditor.insertImage(url, alt);
                }
            }).show(fm, RCEInsertDialog.class.getSimpleName());
        }
    };

    private View.OnClickListener onInsertLink = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager fm = getFragmentManager();
            if(fm == null) return;

            RCEInsertDialog dialog = RCEInsertDialog.newInstance(getContext().getString(R.string.rce_insertLink), mThemeColor, mButtonColor);
            dialog.setListener(new RCEInsertDialog.OnResultListener() {
                @Override
                public void onResults(String url, String alt) {
                    mEditor.insertLink(url, alt);
                }
            }).show(fm, RCEInsertDialog.class.getSimpleName());
        }
    };

    private void toggleColorPicker() {
        if(mColorPickerView.getVisibility() == View.VISIBLE) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mColorPickerView, "translationY", mColorPickerView.getHeight() * -1, 0);
            animator.setDuration(200);
            animator.addListener(new RCEAnimationListener() {
                @Override
                public void onAnimationFinish(Animator animation) {
                    mColorPickerView.setVisibility(View.INVISIBLE);
                }
            });
            animator.start();
        } else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mColorPickerView, "translationY", 0, mColorPickerView.getHeight() * -1);
            animator.setDuration(230);
            animator.addListener(new RCEAnimationListener() {
                @Override
                public void onAnimationBegin(Animator animation) {
                    mColorPickerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mColorPickerView.setVisibility(View.VISIBLE);
                            findViewById(R.id.rce_colorPickerWhite).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                        }
                    });
                }
            });
            animator.start();
        }
    }

    @Nullable
    private FragmentManager getFragmentManager() {
        try {
            return ((FragmentActivity)getContext()).getSupportFragmentManager();
        } catch (Exception e) {
            return null;
        }
    }

    public interface ExitDialogCallback {
        void onPositive();
        void onNegative();
    }

    public void showExitDialog(final int buttonColor, final ExitDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.rce_dialog_exit_title);
        builder.setMessage(R.string.rce_dialog_exit_message);
        builder.setPositiveButton(R.string.rce_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(callback != null) { callback.onPositive(); }
            }
        });
        builder.setNegativeButton(R.string.rce_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(callback != null) { callback.onNegative(); }
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(buttonColor);
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(buttonColor);
            }
        });
        alertDialog.show();
    }

    public void requestEditorFocus() {
        mEditor.focusEditor();
    }

    //region save and restore state

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.htmlState = getHtml();
        ss.contentDescription = mEditor.getAccessibilityContentDescription();
        ss.themeColor = mThemeColor;
        ss.buttonColor = mButtonColor;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());

        setHtml(ss.htmlState, ss.contentDescription, "", ss.themeColor, ss.buttonColor);
    }

    private static class SavedState extends BaseSavedState {
        String htmlState, contentDescription;
        int themeColor = Color.BLACK;
        int buttonColor = Color.BLACK;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.htmlState = in.readString();
            this.contentDescription = in.readString();
            this.themeColor = in.readInt();
            this.buttonColor = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(this.htmlState);
            out.writeString(this.contentDescription);
            out.writeInt(this.themeColor);
            out.writeInt(this.buttonColor);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    //endregion
}
