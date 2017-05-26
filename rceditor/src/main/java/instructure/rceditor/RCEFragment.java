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
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static instructure.rceditor.RCEConst.HTML_ACCESSIBILITY_TITLE;
import static instructure.rceditor.RCEConst.HTML_CONTENT;
import static instructure.rceditor.RCEConst.HTML_RESULT;
import static instructure.rceditor.RCEConst.HTML_TITLE;

public class RCEFragment extends Fragment {

    public interface RCEFragmentCallbacks {
        void onResult(int activityResult, @Nullable Intent data);
    }

    private RCETextEditor mEditor;
    private View mColorPickerView;
    private RCEFragmentCallbacks mCallback;

    public RCEFragment() {
        if(getArguments() == null) {
            setArguments(new Bundle());
        }
    }

    public void loadArguments(String html, String title, String accessibilityTitle) {
        getArguments().putString(HTML_CONTENT, html);
        getArguments().putString(HTML_TITLE, title);
        getArguments().putString(HTML_ACCESSIBILITY_TITLE, accessibilityTitle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof RCEFragmentCallbacks) {
            mCallback = (RCEFragmentCallbacks) context;
        } else {
            throw new IllegalStateException("Context must implement RCEFragment.RCEFragmentCallbacks()");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rce_fragment_layout, container, false);
        setupViews(view);
        return view;
    }

    private void setupViews(View view) {
        mColorPickerView = view.findViewById(R.id.rce_colorPickerWrapper);

        mEditor = (RCETextEditor) view.findViewById(R.id.rce_webView);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.applyHtml(
                getArguments().getString(HTML_CONTENT),
                getArguments().getString(HTML_ACCESSIBILITY_TITLE));

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.rce_toolbar);
        toolbar.setNavigationIcon(R.drawable.vd_rce_cancel);
        toolbar.setNavigationContentDescription(R.string.rce_cancel);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check to see if we made any changes. If we haven't, just close the fragment
                if(mEditor.getHtml() != null && getArguments().getString(HTML_CONTENT) != null) {
                    if(mEditor.getHtml().equals(getArguments().getString(HTML_CONTENT))) {
                        mCallback.onResult(RESULT_CANCELED, null);
                        return;
                    }
                }
                showExitDialog();
            }
        });

        toolbar.setTitle(getArguments().getString(HTML_TITLE));
        toolbar.inflateMenu(R.menu.rce_save_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.rce_save) {
                    Intent data = new Intent();
                    data.putExtra(HTML_RESULT, mEditor.getHtml() == null ?
                            getArguments().getString(HTML_CONTENT) : mEditor.getHtml());
                    mCallback.onResult(RESULT_OK, data);
                    return true;
                }
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.rce_dimStatusBarGray));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        view.findViewById(R.id.rce_colorPickerWhite).setOnClickListener(onColorChosen);
        view.findViewById(R.id.rce_colorPickerBlack).setOnClickListener(onColorChosen);
        view.findViewById(R.id.rce_colorPickerGray).setOnClickListener(onColorChosen);
        view.findViewById(R.id.rce_colorPickerRed).setOnClickListener(onColorChosen);
        view.findViewById(R.id.rce_colorPickerOrange).setOnClickListener(onColorChosen);
        view.findViewById(R.id.rce_colorPickerYellow).setOnClickListener(onColorChosen);
        view.findViewById(R.id.rce_colorPickerGreen).setOnClickListener(onColorChosen);
        view.findViewById(R.id.rce_colorPickerBlue).setOnClickListener(onColorChosen);
        view.findViewById(R.id.rce_colorPickerPurple).setOnClickListener(onColorChosen);

        view.findViewById(R.id.action_undo).setOnClickListener(onUndo);
        view.findViewById(R.id.action_redo).setOnClickListener(onRedo);
        view.findViewById(R.id.action_bold).setOnClickListener(onBold);
        view.findViewById(R.id.action_italic).setOnClickListener(onItalic);
        view.findViewById(R.id.action_underline).setOnClickListener(onUnderline);
        view.findViewById(R.id.action_insert_bullets).setOnClickListener(onInsertBulletList);
        view.findViewById(R.id.action_insert_image).setOnClickListener(onInsertPicture);
        view.findViewById(R.id.action_insert_link).setOnClickListener(onInsertLink);
        view.findViewById(R.id.action_txt_color).setOnClickListener(onTextColor);
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

    private View.OnClickListener onInsertPicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RCEInsertDialog dialog = RCEInsertDialog.newInstance(getString(R.string.rce_insertImage));
            dialog.setListener(new RCEInsertDialog.OnResultListener() {
                @Override
                public void onResults(String url, String alt) {
                    mEditor.insertImage(url, alt);
                }
            }).show(getFragmentManager(), RCEInsertDialog.class.getSimpleName());
        }
    };

    private View.OnClickListener onInsertLink = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RCEInsertDialog dialog = RCEInsertDialog.newInstance(getString(R.string.rce_insertLink));
            dialog.setListener(new RCEInsertDialog.OnResultListener() {
                @Override
                public void onResults(String url, String alt) {
                    mEditor.insertLink(url, alt);
                }
            }).show(getFragmentManager(), RCEInsertDialog.class.getSimpleName());
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
                        public void run() { mColorPickerView.setVisibility(View.VISIBLE); }
                    });
                }
            });
            animator.start();
        }
    }

    public void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.rce_dialog_exit_title);
        builder.setMessage(R.string.rce_dialog_exit_message);
        builder.setPositiveButton(R.string.rce_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mCallback.onResult(RESULT_CANCELED, null);
            }
        });
        builder.setNegativeButton(R.string.rce_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public static RCEFragment newInstance(String html, String title, String accessibilityTitle) {
        RCEFragment fragment = new RCEFragment();
        Bundle args = new Bundle();
        args.putString(HTML_CONTENT, html);
        args.putString(HTML_TITLE, title);
        args.putString(HTML_ACCESSIBILITY_TITLE, accessibilityTitle);
        fragment.setArguments(args);
        return fragment;
    }

    public static RCEFragment newInstance(Bundle args) {
        RCEFragment fragment = new RCEFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle makeBundle(String html, String title, String accessibilityTitle) {
        Bundle args = new Bundle();
        args.putString(HTML_CONTENT, html);
        args.putString(HTML_TITLE, title);
        args.putString(HTML_ACCESSIBILITY_TITLE, accessibilityTitle);
        return args;
    }
}
