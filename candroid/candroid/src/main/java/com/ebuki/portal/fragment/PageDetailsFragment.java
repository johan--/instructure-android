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

package com.instructure.candroid.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.LockInfoHTMLHelper;
import com.instructure.candroid.util.Param;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.PageManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Page;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.views.CanvasWebView;

import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;

public class PageDetailsFragment extends InternalWebviewFragment {

    // logic
    private String pageName;

    // asyncTasks
    private StatusCallback<Page> pageCallback;

    private Page page;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.pages);
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return page != null ? page.getTitle() : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShouldLoadUrl(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpCallBack();

        if (pageName == null || pageName.equals(Page.FRONT_PAGE_NAME)) {
            PageManager.getFrontPage(getCanvasContext(), true, pageCallback);
        } else {
            PageManager.getPageDetails(getCanvasContext(), pageName, true, pageCallback);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        canvasWebView.setCanvasEmbeddedWebViewCallback(new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }

            @Override
            public void launchInternalWebViewFragment(String url) {
                InternalWebviewFragment.loadInternalWebView(getActivity(), (Navigation) getActivity(), InternalWebviewFragment.createBundle(getCanvasContext(), url, isLTITool()));
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected Page getModelObject() {
        return page;
    }

    @Override
    public HashMap<String, String> getParamForBookmark() {
        HashMap<String, String> map = getCanvasContextParams();
        if(Page.FRONT_PAGE_NAME.equals(pageName)) {
            map.put(Param.PAGE_ID, Page.FRONT_PAGE_NAME);
        } else if(!TextUtils.isEmpty(pageName)){
            map.put(Param.PAGE_ID, pageName);
        }
        return map;
    }

    @Override
    public boolean allowBookmarking() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // CallBack
    ///////////////////////////////////////////////////////////////////////////

    public void setUpCallBack(){
        pageCallback = new StatusCallback<Page>() {

            @Override
            public void onResponse(retrofit2.Response<Page> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()){
                    return;
                }

                getActivity().supportInvalidateOptionsMenu();
                page = response.body();

                if(page.getLockInfo() != null) {
                    String lockedMessage = LockInfoHTMLHelper.getLockedInfoHTML(page.getLockInfo(),getActivity(), R.string.lockedPageDesc, R.string.lockedAssignmentDescLine2);
                    populateWebView(lockedMessage, getString(R.string.pages));
                    return;
                }
                if (page.getBody() != null && !page.getBody().equals("null") && !page.getBody().equals("")) {
                    //this sets the width to be the device width and makes the images not be bigger than the width of the screen
                    populateWebView(page.getBody(), getString(R.string.pages));

                } else if (page.getBody() == null || page.getBody().endsWith("")) {
                    loadHtml("file:///android_asset/", getResources().getString(R.string.noPageFound), "text/html", "utf-8", null);
                }
                setupTitle(getActionbarTitle());
            }

            @Override
            public void onFail(Call<Page> response, Throwable error, int code) {
                if (response != null && code >= 400 && code < 500 && pageName != null && pageName.equals(Page.FRONT_PAGE_NAME)) {

                    String context;
                    if (getCanvasContext().getType() == CanvasContext.Type.COURSE) {
                        context = getString(R.string.course);
                    } else {
                        context = getString(R.string.group);
                    }

                    //We want a complete sentence.
                    context += ".";

                    //We want it to be lowercase.
                    context = context.toLowerCase(Locale.getDefault());

                    loadHtml("file:///android_asset/", getResources().getString(R.string.noPagesInContext) + " " + context, "text/html", "utf-8", null);
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

        if (getUrlParams() != null) {
            pageName = getUrlParams().get(Param.PAGE_ID);
        } else {
            pageName = extras.getString(Const.PAGE_NAME);
        }
    }

    public static Bundle createBundle(String pageName, CanvasContext canvasContext) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.PAGE_NAME, pageName);
        return extras;
    }
}
