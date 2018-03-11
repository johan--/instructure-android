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

package com.ebuki.homework.fragment;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.ebuki.homework.R;
import com.ebuki.homework.delegate.Navigation;
import com.ebuki.homework.util.RouterUtils;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.video.ActivityContentVideoViewClient;
import com.instructure.pandautils.views.CanvasWebView;

import java.util.List;


public class SyllabusFragment extends ParentFragment {
    // view variables
    private CanvasWebView detailsWebView;

    // model variables
    private ScheduleItem syllabus;

    // callbacks
    StatusCallback<Course> syllabusCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.syllabus);
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.syllabus_fragment, container, false);
        detailsWebView = (CanvasWebView) rootView.findViewById(R.id.description);
        detailsWebView.setClient(new ActivityContentVideoViewClient(getActivity(), new ActivityContentVideoViewClient.HostingView() {

            @Nullable
            @Override
            public Dialog getDialogFragment() {
                List<Fragment> fragmentList = getFragmentManager().getFragments();

                if (fragmentList != null) {
                    if (fragmentList.get(0) instanceof DialogFragment) {
                        return ((DialogFragment) fragmentList.get(0)).getDialog();
                    }
                }
                return null;
            }
        }));
        detailsWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
            @Override
            public void openMediaFromWebView(String mime, String url, String filename) {
                openMedia(mime, url, filename);
            }

            @Override
            public void onPageStartedCallback(WebView webView, String url) {

            }

            @Override
            public void onPageFinishedCallback(WebView webView, String url) {

            }

            @Override
            public boolean canRouteInternallyDelegate(String url) {
                return RouterUtils.canRouteInternally(getActivity(), url, ApiPrefs.getDomain(), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                RouterUtils.canRouteInternally(getActivity(), url, ApiPrefs.getDomain(), true);
            }
        });

        detailsWebView.setCanvasEmbeddedWebViewCallback(new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }

            @Override
            public void launchInternalWebViewFragment(String url) {
                InternalWebviewFragment.loadInternalWebView(getActivity(), (Navigation) getActivity(), InternalWebviewFragment.createBundle(getCanvasContext(), url, false));
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupCallbacks();

        if (syllabus == null || syllabus.getDescription() == null) {
            CourseManager.getCourseWithSyllabus(getCanvasContext().getId(), syllabusCallback, true);
        } else {
            populateViews();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (detailsWebView != null) {
            detailsWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (detailsWebView != null) {
            detailsWebView.onResume();
        }
    }

    @Override
    public boolean handleBackPressed() {
        return detailsWebView.handleGoBack();
    }

    @Override
    protected ScheduleItem getModelObject() {
        return syllabus;
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return syllabus != null && !TextUtils.isEmpty(syllabus.getTitle()) ? syllabus.getTitle() : getString(R.string.syllabus);
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    void populateViews() {
        if (getActivity() == null || syllabus == null || syllabus.getItemType() != ScheduleItem.Type.TYPE_SYLLABUS) {
            return;
        }

        setupTitle(getActionbarTitle());
        detailsWebView.formatHTML(syllabus.getDescription(), syllabus.getTitle());
    }


    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    private void setupCallbacks() {
        syllabusCallback = new StatusCallback<Course>() {
            @Override
            public void onResponse(retrofit2.Response<Course> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()){
                    return;
                }
                Course course = response.body();
                if (course.getSyllabusBody() != null) {
                    syllabus = new ScheduleItem();
                    syllabus.setItemType(ScheduleItem.Type.TYPE_SYLLABUS);
                    syllabus.setTitle(course.getName());
                    syllabus.setDescription(course.getSyllabusBody());
                    populateViews();
                }
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        syllabus =  extras.getParcelable(Const.SYLLABUS);
    }

    public static Bundle createBundle(Course course, ScheduleItem syllabus) {
        Bundle bundle = createBundle(course);
        bundle.putParcelable(Const.ADD_SYLLABUS, syllabus);
        bundle.putParcelable(Const.SYLLABUS, syllabus);
        return bundle;
    }

    @Override
    public boolean allowBookmarking() {
        return true;
    }
}
