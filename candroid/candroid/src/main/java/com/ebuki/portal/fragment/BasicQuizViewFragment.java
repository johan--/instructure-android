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

package com.ebuki.portal.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ebuki.portal.R;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.util.FragUtils;
import com.ebuki.portal.util.LockInfoHTMLHelper;
import com.ebuki.portal.util.Param;
import com.ebuki.portal.util.RouterUtils;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.QuizManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Quiz;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.utils.Const;

import java.util.HashMap;

import retrofit2.Call;

public class BasicQuizViewFragment extends InternalWebviewFragment {

    private String baseURL;
    private String apiURL;
    private Quiz quiz;
    private long quizId = -1;

    private StatusCallback<Quiz> canvasCallback;
    private StatusCallback<Quiz> getDetailedQuizCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.quizzes);
    }

    @Override
    public String getTabId() {
        return Tab.QUIZZES_ID;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //we need to set the webviewclient before we get the quiz so it doesn't try to open the
        //quiz in a different browser
        if(baseURL == null) {
            //if the baseURL is null something went wrong, nothing will show here
            //but at least it won't crash
            return;
        }
        final Uri uri = Uri.parse(baseURL);
        final String host = uri.getHost();
        canvasWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        canvasWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri currentUri = Uri.parse(url);

                if (url.contains(host)) { //we need to handle it.
                    if (currentUri != null && currentUri.getPathSegments().size() >= 3 && currentUri.getPathSegments().get(2).equals("quizzes")) {  //if it's a quiz, stay here.
                        view.loadUrl(url, APIHelper.getReferrer());
                        return true;
                    }
                    //might need to log in to take the quiz -- the url would say domain/login. If we just use the AppRouter it will take the user
                    //back to the dashboard. This check will keep them here and let them log in and take the quiz
                    else if (currentUri != null && currentUri.getPathSegments().size() >= 1 && currentUri.getPathSegments().get(0).equalsIgnoreCase("login")) {
                        view.loadUrl(url, APIHelper.getReferrer());
                        return true;
                    } else { //It's content but not a quiz. Could link to a discussion (or whatever) in a quiz. Route
                        return RouterUtils.canRouteInternally(getActivity(), url, ApiPrefs.getDomain(), true);
                    }
                }

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                canvasLoading.setVisibility(View.GONE);
                Navigation navigation = getNavigation();
                if (navigation != null) {
                    navigation.redrawScreen();
                }
            }
        });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpCallback();
        // anything that relies on intent data belongs here
        if(apiURL != null) {
            QuizManager.getDetailedQuizByUrl(apiURL, true, canvasCallback);
        } else if(quiz != null && quiz.getLockInfo() != null && (CanvasContext.Type.isCourse(getCanvasContext()) && !((Course)getCanvasContext()).isTeacher())) {
            //if the quiz is locked we don't care if they're a teacher
            populateWebView(LockInfoHTMLHelper.getLockedInfoHTML(quiz.getLockInfo(),getActivity(), R.string.lockedQuizDesc, R.string.lockedAssignmentDescLine2));
        } else if (quizId != -1) {
            QuizManager.getDetailedQuiz(getCanvasContext(), quizId, true, getDetailedQuizCallback);
        } else {
            authenticate = true;
            loadUrl(baseURL);
        }
    }

    @Override
    public boolean handleBackPressed() {
        if(canvasWebView != null) {
            return canvasWebView.handleGoBack();
        }
        return false;
    }

    @Override
    protected Quiz getModelObject() {
        return quiz;
    }

    public void setUpCallback(){
        getDetailedQuizCallback =  new StatusCallback<Quiz>() {
            Quiz cacheQuiz;

            @Override
            public void onResponse(retrofit2.Response<Quiz> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()) return;

                if(type == ApiType.CACHE) cacheQuiz = response.body();
                loadQuiz(response.body());
            }

            @Override
            public void onFail(Call<Quiz> callResponse, Throwable error, retrofit2.Response response) {
                if(!apiCheck()) return;

                loadQuiz(cacheQuiz);
            }

            private void loadQuiz(Quiz quiz) {
                if(quiz == null) return;

                BasicQuizViewFragment.this.quiz = quiz;
                BasicQuizViewFragment.this.baseURL = quiz.getUrl();

                if (shouldShowNatively(quiz)) { return; }

                if (quiz.getLockInfo() != null) {
                    populateWebView(LockInfoHTMLHelper.getLockedInfoHTML(quiz.getLockInfo(),getActivity(), R.string.lockedQuizDesc, R.string.lockedAssignmentDescLine2));
                } else {
                    canvasWebView.loadUrl(quiz.getUrl(), APIHelper.getReferrer());
                }
            }
        };
        canvasCallback = new StatusCallback<Quiz>() {

            @Override
            public void onResponse(retrofit2.Response<Quiz> response, LinkHeaders linkHeaders, ApiType type) {
                if(!apiCheck()) return;

                BasicQuizViewFragment.this.quiz = quiz;
                if (shouldShowNatively(quiz)) return;

                if (quiz.getLockInfo() != null) {
                    populateWebView(LockInfoHTMLHelper.getLockedInfoHTML(quiz.getLockInfo(),getActivity(), R.string.lockedQuizDesc, R.string.lockedAssignmentDescLine2));
                } else {
                    String url = quiz.getUrl();
                    if (TextUtils.isEmpty(url)) {
                        url = baseURL;
                    }
                    canvasWebView.loadUrl(url, APIHelper.getReferrer());
                }
            }
        };
    }

    /**
     * When we route we always route quizzes here, so this checks to see if we support
     * native quizzes and if we do then we'll show it natively
     * @param quiz
     * @return
     */
    private boolean shouldShowNatively(Quiz quiz) {
        if(QuizListFragment.isNativeQuiz(getCanvasContext(), quiz)) {

            //take them to the quiz start fragment instead, let them take it natively
            Navigation navigation = getNavigation();
            if (navigation != null) {
                navigation.popCurrentFragment();
                Bundle bundle = QuizStartFragment.createBundle(getCanvasContext(), quiz);
                navigation.addFragment(FragUtils.getFrag(QuizStartFragment.class, bundle), true);
                return true;
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if (getUrlParams() != null) {
            quizId = parseLong(getUrlParams().get(Param.QUIZ_ID), -1);
        } else {
            baseURL = extras.getString(Const.URL);
            apiURL = extras.getString(Const.API_URL);
            quiz = extras.getParcelable(Const.QUIZ);
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.URL, url);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, Quiz quiz) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.QUIZ, quiz);
        extras.putString(Const.URL, url);
        return extras;
    }

    //for module progression we need the api url too
    public static Bundle createBundle(CanvasContext canvasContext, String url, String apiURL) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.URL, url);
        extras.putString(Const.API_URL, apiURL);
        return extras;
    }

    //Currently there isn't a way to know how to decide if we want to route
    //to this fragment or the QuizStartFragment.
    @Override
    public boolean allowBookmarking() {
        return false;
    }

    @Override
    public HashMap<String, String> getParamForBookmark() {
        HashMap<String, String> map = getCanvasContextParams();
        if(quiz != null) {
            map.put(Param.QUIZ_ID, Long.toString(quiz.getId()));
        } else if(quizId != -1) {
            map.put(Param.QUIZ_ID, Long.toString(quizId));
        }
        return map;
    }
}
