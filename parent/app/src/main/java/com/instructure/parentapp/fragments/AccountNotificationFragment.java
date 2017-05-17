package com.instructure.parentapp.fragments;

/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.instructure.canvasapi2.models.AccountNotification;
import com.instructure.canvasapi2.models.Student;
import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.RouterUtils;
import com.instructure.parentapp.video.ActivityContentVideoViewClient;
import com.instructure.parentapp.view.CanvasWebView;

public class AccountNotificationFragment extends ParentFragment {

    private CanvasWebView mAccountNotificationWebView;
    private AccountNotification mAccountNotification;
    private Student mStudent;

    public static AccountNotificationFragment newInstance(AccountNotification notification, Student student) {
        Bundle args = new Bundle();
        args.putParcelable(Const.ACCOUNT_NOTIFICATION, notification);
        args.putParcelable(Const.STUDENT, student);
        AccountNotificationFragment fragment = new AccountNotificationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getRootLayout() {
        return R.layout.fragment_account_notification;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAccountNotificationWebView != null) {
            mAccountNotificationWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAccountNotificationWebView != null) {
            mAccountNotificationWebView.onResume();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountNotification = getArguments().getParcelable(Const.ACCOUNT_NOTIFICATION);
        mStudent = getArguments().getParcelable(Const.STUDENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getRootLayout(), container, false);

        setupViews(view);
        setupDialogToolbar(view);
        return view;
    }

    @Override
    protected void setupDialogToolbar(View rootView) {
        super.setupDialogToolbar(rootView);

        TextView toolbarTitle = (TextView)rootView.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(mAccountNotification.getSubject());
    }

    private void setupViews(View rootView) {
//        mAnnouncementTitle = (TextView) rootView.findViewById(R.id.announcementName);
        mAccountNotificationWebView = (CanvasWebView) rootView.findViewById(R.id.announcementWebView);

//        mAnnouncementTitle.setText(mAnnouncement.getTitle());

        mAccountNotificationWebView.setClient(new ActivityContentVideoViewClient(getActivity()));

        mAccountNotificationWebView.setCanvasEmbeddedWebViewCallback(new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public void launchInternalWebViewFragment(String url) {
                //create and add the InternalWebviewFragment to deal with the link they clicked
                InternalWebviewFragment internalWebviewFragment = new InternalWebviewFragment();
                internalWebviewFragment.setArguments(InternalWebviewFragment.createBundle(url, "", null, mStudent));

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom);
                ft.add(R.id.fullscreen, internalWebviewFragment, internalWebviewFragment.getClass().getName());
                ft.addToBackStack(internalWebviewFragment.getClass().getName());
                ft.commitAllowingStateLoss();
            }

            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }
        });

        mAccountNotificationWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
            @Override
            public void openMediaFromWebView(String mime, String url, String filename) {

            }

            @Override
            public void onPageStartedCallback(WebView webView, String url) {

            }

            @Override
            public void onPageFinishedCallback(WebView webView, String url) {

            }

            @Override
            public boolean canRouteInternallyDelegate(String url) {
                Uri uri = Uri.parse(url);
                return RouterUtils.canRouteInternally(null, url, mStudent, uri.getHost(), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                Uri uri = Uri.parse(url);
                RouterUtils.canRouteInternally(getActivity(), url, mStudent, uri.getHost(), true);
            }

            @Override
            public String studentDomainReferrer() {
                return mStudent.getStudentDomain();
            }
        });


        mAccountNotificationWebView.formatHTML(mAccountNotification.getMessage(), mAccountNotification.getSubject());

    }
}
