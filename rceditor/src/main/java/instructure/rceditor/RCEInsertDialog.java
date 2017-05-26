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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class RCEInsertDialog extends DialogFragment {

    private static final String TITLE = "title";

    private EditText mUrlEditText, mAltEditText;
    private OnResultListener mCallback;

    public interface OnResultListener {
        void onResults(String url, String alt);
    }

    public static RCEInsertDialog newInstance(String title) {
        RCEInsertDialog dialog = new RCEInsertDialog();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.rce_dialog_insert, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(root);
        builder.setTitle(getArguments().getString(TITLE));
        builder.setPositiveButton(R.string.rce_dialogDone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mCallback != null) {
                    mCallback.onResults(mUrlEditText.getText().toString(), mAltEditText.getText().toString());
                }
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.rce_dialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        mUrlEditText = (EditText) root.findViewById(R.id.urlEditText);
        mAltEditText = (EditText) root.findViewById(R.id.altEditText);
        return builder.create();
    }

    public RCEInsertDialog setListener(OnResultListener callback) {
        mCallback = callback;
        return this;
    }
}
