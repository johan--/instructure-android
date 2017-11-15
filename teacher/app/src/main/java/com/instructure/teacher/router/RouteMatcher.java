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
package com.instructure.teacher.router;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.FileFolderManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.FileFolder;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.LoaderUtils;
import com.instructure.teacher.PSPDFKit.AnnotationComments.AnnotationCommentListFragment;
import com.instructure.teacher.R;
import com.instructure.teacher.activities.BottomSheetActivity;
import com.instructure.teacher.activities.FullscreenActivity;
import com.instructure.teacher.activities.InternalWebViewActivity;
import com.instructure.teacher.activities.MasterDetailActivity;
import com.instructure.teacher.activities.SpeedGraderActivity;
import com.instructure.teacher.activities.ViewMediaActivity;
import com.instructure.teacher.adapters.StudentContextFragment;
import com.instructure.teacher.fragments.AddMessageFragment;
import com.instructure.teacher.fragments.AllCoursesFragment;
import com.instructure.teacher.fragments.AnnouncementListFragment;
import com.instructure.teacher.fragments.AssigneeListFragment;
import com.instructure.teacher.fragments.AssignmentDetailsFragment;
import com.instructure.teacher.fragments.AssignmentListFragment;
import com.instructure.teacher.fragments.AssignmentSubmissionListFragment;
import com.instructure.teacher.fragments.AttendanceListFragment;
import com.instructure.teacher.fragments.ChooseRecipientsFragment;
import com.instructure.teacher.fragments.CourseBrowserEmptyFragment;
import com.instructure.teacher.fragments.CourseBrowserFragment;
import com.instructure.teacher.fragments.CourseSettingsFragment;
import com.instructure.teacher.fragments.CoursesFragment;
import com.instructure.teacher.fragments.CreateDiscussionFragment;
import com.instructure.teacher.fragments.CreateOrEditAnnouncementFragment;
import com.instructure.teacher.fragments.CreateOrEditPageDetailsFragment;
import com.instructure.teacher.fragments.DiscussionsDetailsFragment;
import com.instructure.teacher.fragments.DiscussionsListFragment;
import com.instructure.teacher.fragments.DiscussionsReplyFragment;
import com.instructure.teacher.fragments.DiscussionsUpdateFragment;
import com.instructure.teacher.fragments.DueDatesFragment;
import com.instructure.teacher.fragments.EditAssignmentDetailsFragment;
import com.instructure.teacher.fragments.EditFavoritesFragment;
import com.instructure.teacher.fragments.EditFileFolderFragment;
import com.instructure.teacher.fragments.EditQuizDetailsFragment;
import com.instructure.teacher.fragments.FileListFragment;
import com.instructure.teacher.fragments.FullscreenInternalWebViewFragment;
import com.instructure.teacher.fragments.InboxFragment;
import com.instructure.teacher.fragments.InternalWebViewFragment;
import com.instructure.teacher.fragments.LTIWebViewFragment;
import com.instructure.teacher.fragments.MessageThreadFragment;
import com.instructure.teacher.fragments.PageDetailsFragment;
import com.instructure.teacher.fragments.PageListFragment;
import com.instructure.teacher.fragments.PeopleListFragment;
import com.instructure.teacher.fragments.ProfileEditFragment;
import com.instructure.teacher.fragments.ProfileFragment;
import com.instructure.teacher.fragments.ProfileSettingsFragment;
import com.instructure.teacher.fragments.QuizDetailsFragment;
import com.instructure.teacher.fragments.QuizListFragment;
import com.instructure.teacher.fragments.QuizPreviewWebviewFragment;
import com.instructure.teacher.fragments.SpeedGraderQuizWebViewFragment;
import com.instructure.teacher.fragments.ViewHtmlFragment;
import com.instructure.teacher.fragments.ViewImageFragment;
import com.instructure.teacher.fragments.ViewMediaFragment;
import com.instructure.teacher.fragments.ViewPdfFragment;
import com.instructure.teacher.fragments.ViewUnsupportedFileFragment;
import com.instructure.teacher.interfaces.BottomSheetInteractions;
import com.instructure.teacher.interfaces.InitActivityInteractions;
import com.instructure.teacher.interfaces.MasterDetailInteractions;
import com.instructure.teacher.utils.AppType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import instructure.rceditor.RCEFragment;

public class RouteMatcher {

    private static Bundle openMediaBundle;
    private static LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> openMediaCallbacks;

    /**
     * To test via ADB
     * adb shell
     * am start -W -a android.intent.action.VIEW -d "https://mobiledev.instructure.com/courses/833052/assignments"
     *
     * am start -W -a android.intent.action.VIEW -d "http://mobiledev.instructure.com"
     *
     * adb shell am start -a android.intent.action.VIEW -d {URL}
     */

    private static final List<Route> sRoutes = new ArrayList<>();
    private static final List<Class<? extends Fragment>> sFullscreenFragments = new ArrayList<>();
    private static final List<Class<? extends Fragment>> sBottomSheetFragments = new ArrayList<>();

    private static final String COURSE_OR_GROUP_REGEX = "/(?:courses|groups)";
    private static String courseOrGroup(String route) {
        return COURSE_OR_GROUP_REGEX + route;
    }

    static {
        sRoutes.add(new Route("/", CoursesFragment.class));

        sRoutes.add(new Route("/login.*", Route.RouteContext.DO_NOT_ROUTE));//FIXME: we know about this

        sRoutes.add(new Route("/conversations", InboxFragment.class));

        sRoutes.add(new Route(courseOrGroup("/"), CoursesFragment.class));
        sRoutes.add(new Route(courseOrGroup("/:course_id"), CourseBrowserFragment.class));

        // We don't want to route to the syllabus, but this needs to be above the other assignments routing so it catches here first
        sRoutes.add(new Route(courseOrGroup("/:course_id/assignments/syllabus"), Route.RouteContext.DO_NOT_ROUTE));

        sRoutes.add(new Route(courseOrGroup("/:course_id/assignments"), AssignmentListFragment.class));
        sRoutes.add(new Route(courseOrGroup("/:course_id/assignments/:assignment_id"), AssignmentListFragment.class, AssignmentDetailsFragment.class));

        sRoutes.add(new Route(courseOrGroup("/:course_id/quizzes"), QuizListFragment.class));
        sRoutes.add(new Route(courseOrGroup("/:course_id/quizzes/:quiz_id"), QuizListFragment.class, QuizDetailsFragment.class));

        sRoutes.add(new Route(courseOrGroup("/:course_id/discussion_topics"), DiscussionsListFragment.class));
        sRoutes.add(new Route(courseOrGroup("/:course_id/discussion_topics/:message_id"), DiscussionsListFragment.class, DiscussionsDetailsFragment.class));

        sRoutes.add(new Route(courseOrGroup("/:course_id/files/:file_id/download"), Route.RouteContext.FILE));
        sRoutes.add(new Route(courseOrGroup("/:course_id/files/:file_id"), Route.RouteContext.FILE));

        sRoutes.add(new Route(courseOrGroup("/:course_id/files"), FileListFragment.class));

        //TODO: sRoutes.add(new Route(courseOrGroup("/:course_id/pages/:page_id/"), PageListFragment.class, PageDetailsFragment.class));
        sRoutes.add(new Route(courseOrGroup("/:course_id/pages/"), PageListFragment.class));
        sRoutes.add(new Route(courseOrGroup("/:course_id/pages/:page_id/"), PageListFragment.class, PageDetailsFragment.class));
        sRoutes.add(new Route(courseOrGroup("/:course_id/wiki/"), PageListFragment.class));
        sRoutes.add(new Route(courseOrGroup("/:course_id/wiki/:page_id/"), PageListFragment.class, PageDetailsFragment.class));
    }

    /**
     * Here we keep track of which fragments should be placed as fullscreen vs bottomSheet
     */
    static {
        //Fullscreen Fragments
        sFullscreenFragments.add(CoursesFragment.class);
        sFullscreenFragments.add(AllCoursesFragment.class);
        sFullscreenFragments.add(ProfileFragment.class);
        sFullscreenFragments.add(ViewImageFragment.class);
        sFullscreenFragments.add(FullscreenInternalWebViewFragment.class);
        sFullscreenFragments.add(LTIWebViewFragment.class);
        sFullscreenFragments.add(SpeedGraderQuizWebViewFragment.class);

        //Bottom Sheet Fragments
        sBottomSheetFragments.add(EditAssignmentDetailsFragment.class);
        sBottomSheetFragments.add(AssigneeListFragment.class);
        sBottomSheetFragments.add(EditFavoritesFragment.class);
        sBottomSheetFragments.add(CourseSettingsFragment.class);
        sBottomSheetFragments.add(RCEFragment.class);
        sBottomSheetFragments.add(EditQuizDetailsFragment.class);
        sBottomSheetFragments.add(QuizPreviewWebviewFragment.class);
        sBottomSheetFragments.add(AddMessageFragment.class);
        sBottomSheetFragments.add(DiscussionsReplyFragment.class);
        sBottomSheetFragments.add(DiscussionsUpdateFragment.class);
        sBottomSheetFragments.add(ChooseRecipientsFragment.class);
        sBottomSheetFragments.add(CreateDiscussionFragment.class);
        sBottomSheetFragments.add(CreateOrEditAnnouncementFragment.class);
        sBottomSheetFragments.add(AnnotationCommentListFragment.class);
        sBottomSheetFragments.add(ProfileSettingsFragment.class);
        sBottomSheetFragments.add(ProfileEditFragment.class);
        sBottomSheetFragments.add(StudentContextFragment.class);
        sBottomSheetFragments.add(AttendanceListFragment.class);
        sBottomSheetFragments.add(EditFileFolderFragment.class);
        sBottomSheetFragments.add(CreateOrEditPageDetailsFragment.class);
    }

    public static void routeUrl(Context context, String url, Route.RouteContext routeContext) {
        routeUrl(context, url, ApiPrefs.getDomain(), routeContext);
    }

    public static void routeUrl(Context context, String url, String domain, Route.RouteContext routeContext) {
        /* Possible activity types we can navigate too: Unknown Link, InitActivity, Master/Detail, Fullscreen, WebView, ViewMedia */

        //Find the best route
        //Pass that along to the activity
        //One or two classes? (F, or M/D)

        route(context, getInternalRoute(url, domain));
    }

    public static void route(Context context, Route route) {
        //noinspection StatementWithEmptyBody
        if(route == null || route.getRouteContext() == Route.RouteContext.DO_NOT_ROUTE) {
            if(route != null && route.getUrl() != null) {
                //No route, no problem
                handleWebViewUrl(context, route.getUrl());
            }
        } else if(route.getRouteContext() == Route.RouteContext.FILE) {
            if (route.getQueryParamsHash().containsKey(RouterParams.Companion.getVERIFIER()) && route.getQueryParamsHash().containsKey(RouterParams.Companion.getDOWNLOAD_FRD())) {
                if(route.getUrl() != null) {
                    openMedia((FragmentActivity) context, route.getUrl());
                } else if(route.getUri() != null) {
                    openMedia((FragmentActivity) context, route.getUri().toString());
                }
            } else {
                handleSpecificFile(((FragmentActivity) context), route.getParamsHash().get(RouterParams.Companion.getFILE_ID()));
            }

        } else if(route.getRouteContext() == Route.RouteContext.MEDIA) {
            handleMediaRoute(context, route);
        } else if(route.getRouteContext() == Route.RouteContext.SPEED_GRADER) {
            handleSpeedGraderRoute(context, route);
        } else if(context.getResources().getBoolean(R.bool.is_device_tablet)) {
            handleTabletRoute(context, route);
        } else {
            handleFullscreenRoute(context, route);
        }
    }

    private static void handleTabletRoute(Context context, Route route) {
        final Class<? extends Fragment> primaryClass = route.getPrimaryClass();
        final Class<? extends Fragment> secondaryClass = route.getSecondaryClass();

        if(primaryClass != null && secondaryClass != null) {
            handleMasterDetailRoute(context, route);
        } else {
            if(primaryClass == null && secondaryClass == null) {
                handleWebViewRoute(context, route);
            } else if(primaryClass == null) {
                handleDetailRoute(context, route);
            } else {
                if(isFullScreenClass(primaryClass)) {
                    handleFullscreenRoute(context, route);
                } else if(isBottomSheetClass(primaryClass)) {
                    handleBottomSheetRoute(context, route);
                } else {
                    //Master only, no Detail exists yet
                    handleMasterDetailRoute(context, route);
                }
            }
        }
    }

    private static void handleMasterDetailRoute(Context context, Route route) {
        Logger.i("RouteMatcher:handleMasterDetailRoute()");
        context.startActivity(MasterDetailActivity.createIntent(context, route));
    }

    private static void handleDetailRoute(Context context, Route route) {
        if(context instanceof MasterDetailInteractions) {
            Logger.i("RouteMatcher:handleDetailRoute() - MasterDetailInteractions");
            ((MasterDetailInteractions)context).addFragment(route);
        } else if(context instanceof InitActivityInteractions) {
            ((InitActivityInteractions)context).addFragment(route);
        }
    }

    private static void handleFullscreenRoute(Context context, Route route) {
        Logger.i("RouteMatcher:handleFullscreenRoute()");
        context.startActivity(FullscreenActivity.createIntent(context, route));
    }

    private static void handleMediaRoute(Context context, Route route) {
        Logger.i("RouteMatcher:handleMediaRoute()");
        context.startActivity(ViewMediaActivity.createIntent(context, route));
    }

    private static void handleSpeedGraderRoute(Context context, Route route) {
        Logger.i("RouteMatcher:handleSpeedGraderRoute()");
        context.startActivity(SpeedGraderActivity.createIntent(context, route));
    }

    private static void handleWebViewRoute(Context context, Route route) {
        context.startActivity(InternalWebViewActivity.Companion.createIntent(context, route, "", false));
    }

    private static void handleWebViewUrl(Context context, String url) {
        context.startActivity(InternalWebViewActivity.Companion.createIntent(context, url, "", false));
        Logger.i("RouteMatcher:handleWebViewRoute()");
    }

    private static void handleBottomSheetRoute(Context context, Route route) {
        if(context instanceof BottomSheetInteractions) {
            Logger.i("RouteMatcher:handleBottomSheetRoute() - BottomSheetInteractions");
            ((BottomSheetInteractions)context).addFragment(route);
        } else {
            Logger.i("RouteMatcher:handleBottomSheetRoute()");
            context.startActivity(BottomSheetActivity.createIntent(context, route));
        }
    }

    private static boolean isFullScreenClass(Class<? extends Fragment> clazz) {
        return sFullscreenFragments.contains(clazz);
    }

    private static boolean isBottomSheetClass(Class<? extends Fragment> clazz) {
        return sBottomSheetFragments.contains(clazz);
    }

    /**
     * Gets the Route, null if route cannot be handled internally
     * @param url A Url String
     * @return Route if application can handle link internally; null otherwise
     */
    public static Route getInternalRoute(String url, String domain) {
        UrlValidator urlValidator = new UrlValidator(url, domain);

        if (!urlValidator.isHostForLoggedInUser() || !urlValidator.isValid()) {
            return null;
        }

        Route route = null;
        for (Route r : sRoutes) {
            if (r.apply(url)) {
                if (Route.RouteContext.INTERNAL == r.getRouteContext() || Route.RouteContext.DO_NOT_ROUTE == r.getRouteContext()) {
                    /* returning null allows for routes that are not supported
                       to skip the unsupported fragment and are usually just opened in a webview */
                    return null;
                }
                route = r;
                break; // Do NOT remove break (matches the first route it comes across, more specific routes must come first)
            }
        }
        return route;
    }

    public static Route getInternalRoute(Class<? extends Fragment> primaryClass, Class<? extends Fragment> secondaryClass) {
        Route route = null;
        for (Route r : sRoutes) {
            if (r.apply(primaryClass, secondaryClass)) {
                route = r;
                break; // Do NOT remove break (matches the first route it comes across, more specific routes must come first)
            }
        }
        return route;
    }

    /**
     * Gets a course id from a url, if url is invalid or could not be parsed a 0 will return.
     * @param url a Url String
     * @return a CanvasContext context_id (group_12345, course_12345)
     */
    public String getContextIdFromURL(String url, List<Route> routes) {
        if(url == null || url.length() == 0) {
            return "";
        }

        try {
            HashMap<String, String> params = new HashMap<>();
            Route route = null;

            for (Route r : routes) {
                if (r.apply(url)) {
                    route = r;
                    params = r.getParamsHash();
                    break;
                }
            }

            if (route == null) {
                return "";
            }

            return CanvasContext.makeContextId(route.getContextType(), Long.parseLong(params.get(RouterParams.Companion.getCOURSE_ID())));
        } catch (Exception e) {
            return "";
        }
    }

    public static String getCourseIdFromUrl(String url) {
        if(url == null || url.length() == 0) {
            return "";
        }

        try {
            HashMap<String, String> params = new HashMap<>();
            Route route = new Route(courseOrGroup("/:course_id/(.*)"));

            if (route.apply(url)) {
                params = route.getParamsHash();
            }

            return params.get(RouterParams.Companion.getCOURSE_ID());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns true if url can be routed to a fragment, false otherwise
     * @param activity
     * @param url
     * @param routeIfPossible
     * @return
     */
    public static boolean canRouteInternally(Activity activity, String url, String domain, boolean routeIfPossible) {
        boolean canRoute = getInternalRoute(url, domain) != null;

        if (canRoute && activity != null && routeIfPossible) {
            routeUrl(activity, url, Route.RouteContext.INTERNAL);
        }
        return canRoute;
    }



    //region Application Routing

    /**
     * Pass in a route and a course, get a fragment back!
     */
    @Nullable
    public static Fragment getFullscreenFragment(@Nullable CanvasContext canvasContext, @NonNull Route route) {

        if(canvasContext == null) {
            //TODO: INBOX, PROFILE, or CourseList
            if(route.getPrimaryClass() != null) {
                return getFrag(route.getPrimaryClass(), null, route);
            } else {
                return getFrag(route.getSecondaryClass(), null, route);
            }
        } else {
            //we have a route, load up the secondary class if there is one, otherwise primary
            if(route.getSecondaryClass() != null) {
                //load it up
                return getFrag(route.getSecondaryClass(), canvasContext, route);
            } else {
                //load up the primary class
                return getFrag(route.getPrimaryClass(), canvasContext, route);
            }
        }
    }

    @Nullable
    public static Fragment getMasterFragment(@Nullable CanvasContext canvasContext, @NonNull Route route) {
        //TODO: INBOX
        return getFrag(route.getPrimaryClass(), canvasContext, route);
    }

    @Nullable
    public static Fragment getDetailFragment(@Nullable CanvasContext canvasContext, @NonNull Route route) {
        //TODO: INBOX
        return getFrag(route.getSecondaryClass(), canvasContext, route);
    }

    @Nullable
    public static Fragment getBottomSheetFragment(@Nullable CanvasContext canvasContext, @NonNull Route route) {
        return getFrag(route.getPrimaryClass(), canvasContext, route);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <Type extends Fragment> Type getFrag(Class<Type> cls,  CanvasContext canvasContext, Route route) {
        if(cls == null) return null;

        Fragment fragment = null;

        if (ProfileFragment.class.isAssignableFrom(cls)) {
            fragment = new ProfileFragment();
        } else if (CourseBrowserFragment.class.isAssignableFrom(cls)) {
            fragment = CourseBrowserFragment.newInstance((Course) canvasContext);
        } else if (CourseBrowserEmptyFragment.class.isAssignableFrom(cls)) {
            fragment = CourseBrowserEmptyFragment.newInstance((Course) canvasContext);
        } else if (CoursesFragment.class.isAssignableFrom(cls)) {
            fragment = CoursesFragment.getInstance(AppType.TEACHER);
        } else if (AssignmentListFragment.class.isAssignableFrom(cls)) {
            fragment = AssignmentListFragment.getInstance(canvasContext, route.getArguments());
        } else if (AssignmentDetailsFragment.class.isAssignableFrom(cls)) {
            fragment = getAssignmentDetailsFragment(canvasContext, route);
        } else if (DueDatesFragment.class.isAssignableFrom(cls)) {
            fragment = DueDatesFragment.getInstance((Course) canvasContext, route.getArguments());
        } else if (AssignmentSubmissionListFragment.class.isAssignableFrom(cls)) {
            fragment = AssignmentSubmissionListFragment.newInstance((Course) canvasContext, route.getArguments());
        } else if (EditAssignmentDetailsFragment.class.isAssignableFrom(cls)) {
            fragment = EditAssignmentDetailsFragment.newInstance((Course) canvasContext, route.getArguments());
        } else if (AssigneeListFragment.class.isAssignableFrom(cls)) {
            fragment = AssigneeListFragment.newInstance(route.getArguments());
        } else if (EditFavoritesFragment.class.isAssignableFrom(cls)) {
            fragment = EditFavoritesFragment.newInstance(route.getArguments());
        } else if (CourseSettingsFragment.class.isAssignableFrom(cls)) {
            fragment = CourseSettingsFragment.newInstance((Course) canvasContext);
        } else if (QuizListFragment.class.isAssignableFrom(cls)) {
            fragment = QuizListFragment.newInstance(canvasContext);
        } else if (QuizDetailsFragment.class.isAssignableFrom(cls)) {
            fragment = QuizDetailsFragment.newInstance((Course) canvasContext, route.getArguments());
        } else if (RCEFragment.class.isAssignableFrom(cls)) {
            fragment = RCEFragment.newInstance(route.getArguments());
        } else if (EditQuizDetailsFragment.class.isAssignableFrom(cls)) {
            fragment = EditQuizDetailsFragment.newInstance((Course) canvasContext, route.getArguments());
        } else if (QuizPreviewWebviewFragment.class.isAssignableFrom(cls)) {
            fragment = QuizPreviewWebviewFragment.newInstance(route.getArguments());
        } else if (EditQuizDetailsFragment.class.isAssignableFrom(cls)) {
            fragment = EditQuizDetailsFragment.newInstance((Course) canvasContext, route.getArguments());
        } else if (AnnouncementListFragment.class.isAssignableFrom(cls)) {
            // This needs to be above DiscussionsListFragment because it extends it
            fragment = AnnouncementListFragment.newInstance(canvasContext);
        } else if (DiscussionsListFragment.class.isAssignableFrom(cls)) {
            fragment = DiscussionsListFragment.newInstance(canvasContext);
        } else if (DiscussionsDetailsFragment.class.isAssignableFrom(cls)) {
            fragment = getDiscussionDetailsFragment(canvasContext, route);
        } else if (InboxFragment.class.isAssignableFrom(cls)) {
            fragment = new InboxFragment();
        } else if (AddMessageFragment.class.isAssignableFrom(cls)) {
            fragment = AddMessageFragment.newInstance(route.getArguments());
        } else if (MessageThreadFragment.class.isAssignableFrom(cls)) {
            fragment = MessageThreadFragment.newInstance(route.getArguments());
        } else if (ViewPdfFragment.class.isAssignableFrom(cls)) {
            fragment = ViewPdfFragment.newInstance(route.getArguments());
        } else if (ViewImageFragment.class.isAssignableFrom(cls)) {
            fragment = ViewImageFragment.newInstance(route.getArguments());
        } else if (ViewMediaFragment.class.isAssignableFrom(cls)) {
            fragment = ViewMediaFragment.newInstance(route.getArguments());
        } else if (ViewHtmlFragment.class.isAssignableFrom(cls)) {
            fragment = ViewHtmlFragment.newInstance(route.getArguments());
        } else if (ViewUnsupportedFileFragment.class.isAssignableFrom(cls)) {
            fragment = ViewUnsupportedFileFragment.newInstance(route.getArguments());
        } else if(cls.isAssignableFrom(DiscussionsReplyFragment.class)) {
            fragment = DiscussionsReplyFragment.newInstance(canvasContext, route.getArguments());
        } else if(cls.isAssignableFrom(DiscussionsUpdateFragment.class)) {
            fragment = DiscussionsUpdateFragment.newInstance(canvasContext, route.getArguments());
        } else if (ChooseRecipientsFragment.class.isAssignableFrom(cls)) {
            fragment = ChooseRecipientsFragment.newInstance(route.getArguments());
        } else if (SpeedGraderQuizWebViewFragment.class.isAssignableFrom(cls)) {
            fragment = SpeedGraderQuizWebViewFragment.newInstance(route.getArguments());
        } else if (AnnotationCommentListFragment.class.isAssignableFrom(cls)) {
            fragment = AnnotationCommentListFragment.newInstance(route.getArguments());
        } else if (CreateDiscussionFragment.class.isAssignableFrom(cls)) {
            fragment = CreateDiscussionFragment.newInstance(route.getArguments());
        } else if (CreateOrEditAnnouncementFragment.class.isAssignableFrom(cls)) {
            fragment = CreateOrEditAnnouncementFragment.newInstance(route.getArguments());
        } else if (ProfileSettingsFragment.class.isAssignableFrom(cls)) {
            fragment = ProfileSettingsFragment.newInstance(route.getArguments());
        } else if (ProfileEditFragment.class.isAssignableFrom(cls)) {
            fragment = ProfileEditFragment.newInstance(route.getArguments());
        } else if (LTIWebViewFragment.class.isAssignableFrom(cls)) {
            fragment = LTIWebViewFragment.newInstance(route.getArguments());
        } else if (PeopleListFragment.class.isAssignableFrom(cls)) {
            fragment = PeopleListFragment.newInstance(canvasContext);
        } else if (StudentContextFragment.class.isAssignableFrom(cls)) {
            fragment = StudentContextFragment.newInstance(route.getArguments());
        } else if (AttendanceListFragment.class.isAssignableFrom(cls)) {
            fragment = AttendanceListFragment.newInstance(canvasContext, route.getArguments());
        } else if (FileListFragment.class.isAssignableFrom(cls)) {
            fragment = FileListFragment.newInstance(route.getArguments());
        } else if (PageListFragment.class.isAssignableFrom(cls)) {
            fragment = PageListFragment.newInstance((canvasContext));
        } else if (PageDetailsFragment.class.isAssignableFrom(cls)) {
            fragment = getPageDetailsFragment(canvasContext, route);
        } else if (EditFileFolderFragment.class.isAssignableFrom(cls)) {
            fragment = EditFileFolderFragment.newInstance(route.getArguments());
        } else if (CreateOrEditPageDetailsFragment.class.isAssignableFrom(cls)) {
            fragment = CreateOrEditPageDetailsFragment.newInstance(route.getArguments());
        }

        //NOTE: These should remain at or near the bottom to give fragments that extend InternalWebViewFragment the chance first
        else if (FullscreenInternalWebViewFragment.class.isAssignableFrom(cls)) {
            fragment = FullscreenInternalWebViewFragment.newInstance(route.getArguments());
        } else if (InternalWebViewFragment.class.isAssignableFrom(cls)) {
            fragment = InternalWebViewFragment.newInstance(route.getArguments());
        }

        return (Type)fragment;
    }

    private static AssignmentDetailsFragment getAssignmentDetailsFragment(CanvasContext canvasContext, Route route) {
        if(route.getArguments().containsKey(AssignmentDetailsFragment.getASSIGNMENT())) {
            return AssignmentDetailsFragment.newInstance((Course) canvasContext, route.getArguments());
        } else {
            //parse the route to get the assignment id
            long assignmentId = Long.parseLong(route.getParamsHash().get(RouterParams.Companion.getASSIGNMENT_ID()));
            Bundle args = AssignmentDetailsFragment.makeBundle(assignmentId);
            return AssignmentDetailsFragment.newInstance((Course) canvasContext, args);
        }
    }

    private static QuizDetailsFragment getQuizDetailsFragment(CanvasContext canvasContext, Route route) {
        if(route.getArguments().containsKey(QuizDetailsFragment.getQUIZ())) {
            return QuizDetailsFragment.newInstance((Course) canvasContext, route.getArguments());
        } else {
            //parse the route to get the quiz id
            long quizId = Long.parseLong(route.getParamsHash().get(RouterParams.Companion.getQUIZ_ID()));
            Bundle args = QuizDetailsFragment.makeBundle(quizId);
            return QuizDetailsFragment.newInstance((Course) canvasContext, args);
        }
    }

    private static PageDetailsFragment getPageDetailsFragment(CanvasContext canvasContext, Route route) {
        if(route.getArguments().containsKey(PageDetailsFragment.PAGE)) {
            return PageDetailsFragment.newInstance(canvasContext, route.getArguments());
        } else {
            //parse the route to get the page id
            String pageId = route.getParamsHash().get(RouterParams.Companion.getPAGE_ID());
            Bundle args = PageDetailsFragment.makeBundle(pageId);
            return PageDetailsFragment.newInstance(canvasContext, args);
        }
    }

    private static DiscussionsDetailsFragment getDiscussionDetailsFragment(CanvasContext canvasContext, Route route) {
        if(route.getArguments().containsKey(DiscussionsDetailsFragment.Companion.getDISCUSSION_TOPIC_HEADER())) {
            return DiscussionsDetailsFragment.newInstance(canvasContext, route.getArguments());
        } else if(route.getArguments().containsKey(DiscussionsDetailsFragment.Companion.getDISCUSSION_TOPIC_HEADER_ID())) {
            final long discussionTopicHeaderId = route.getArguments().getLong(DiscussionsDetailsFragment.Companion.getDISCUSSION_TOPIC_HEADER_ID());
            Bundle args = DiscussionsDetailsFragment.makeBundle(discussionTopicHeaderId);
            return DiscussionsDetailsFragment.newInstance(canvasContext, args);
        } else {
            //parse the route to get the discussion id
            final long discussionTopicHeaderId = Long.parseLong(route.getParamsHash().get(RouterParams.Companion.getMESSAGE_ID()));
            Bundle args = DiscussionsDetailsFragment.makeBundle(discussionTopicHeaderId);
            return DiscussionsDetailsFragment.newInstance(canvasContext, args);
        }
    }

    //endregion

    @NonNull
    public static <Type extends Fragment> String getClassDisplayName(@NonNull Context context, @Nullable Class<Type> cls) {
        if(cls == null) return "";

        if(cls.isAssignableFrom(AssignmentListFragment.class)) {
            return context.getString(R.string.tab_assignments);
        } else if(cls.isAssignableFrom(QuizListFragment.class)) {
            return context.getString(R.string.tab_quizzes);
        } else if(cls.isAssignableFrom(DiscussionsListFragment.class)) {
            return context.getString(R.string.tab_discussions);
        } else if(cls.isAssignableFrom(InboxFragment.class)) {
            return context.getString(R.string.tab_inbox);
        } else {
            return "";
        }
    }

    private static LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> getLoaderCallbacks(final Activity activity) {
        if (openMediaCallbacks == null) {
            openMediaCallbacks = new LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>() {
                @Override
                public Loader<OpenMediaAsyncTaskLoader.LoadedMedia> onCreateLoader(int id, Bundle args) {
                    return new OpenMediaAsyncTaskLoader(activity, args);
                }

                @Override
                public void onLoadFinished(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader, OpenMediaAsyncTaskLoader.LoadedMedia loadedMedia) {

                    try {
                        if (loadedMedia.isError()) {
                            if(loadedMedia.getErrorType() == OpenMediaAsyncTaskLoader.ERROR_TYPE.NO_APPS) {
                                Bundle args = ViewUnsupportedFileFragment.newInstance(loadedMedia.getIntent().getData(), ((OpenMediaAsyncTaskLoader)loader).getFilename(), loadedMedia.getIntent().getType(), null, R.drawable.vd_attachment).getArguments();
                                RouteMatcher.route(activity, new Route(ViewUnsupportedFileFragment.class, null, args));
                            } else {
                                Toast.makeText(activity, activity.getResources().getString(loadedMedia.getErrorMessage()), Toast.LENGTH_LONG).show();
                            }
                        } else if (loadedMedia.isHtmlFile()) {
                            Bundle args = ViewHtmlFragment.newInstance(loadedMedia.getBundle().getString(Const.INTERNAL_URL), loadedMedia.getBundle().getString(Const.ACTION_BAR_TITLE)).getArguments();
                            RouteMatcher.route(activity, new Route(ViewHtmlFragment.class, null, args));
                        } else if (loadedMedia.getIntent() != null) {
                            if(loadedMedia.getIntent().getType().contains("pdf") && !loadedMedia.isUseOutsideApps()){
                                //show pdf with PSPDFkit
                                Uri uri = loadedMedia.getIntent().getData();
                                Bundle args = ViewPdfFragment.newInstance(((OpenMediaAsyncTaskLoader)loader).getUrl(), 0).getArguments();
                                RouteMatcher.route(activity, new Route(ViewPdfFragment.class, null, args));

                            } else if (loadedMedia.getIntent().getType().equals("video/mp4")){
                                Bundle bundle = ViewMediaActivity.makeBundle(loadedMedia.getIntent().getData().toString(), null, "video/mp4", loadedMedia.getIntent().getDataString(), true);
                                RouteMatcher.route(activity, new Route(bundle, Route.RouteContext.MEDIA));

                            } else if (loadedMedia.getIntent().getType().startsWith("image/")) {
                                Bundle args = ViewImageFragment.newInstance(loadedMedia.getIntent().getDataString(), loadedMedia.getIntent().getData(), "image/*", true, 0).getArguments();
                                RouteMatcher.route(activity, new Route(ViewImageFragment.class, null, args));
                            } else {
                                activity.startActivity(loadedMedia.getIntent());
                            }
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity, R.string.noApps, Toast.LENGTH_LONG).show();
                    }
                    openMediaBundle = null;
                }

                @Override
                public void onLoaderReset(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader) {
                }
            };
        }
        return openMediaCallbacks;
    }

    public static void openMedia(FragmentActivity activity, String url) {
        if(activity != null) {
            openMediaCallbacks = null;
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(url);
            LoaderUtils.restartLoaderWithBundle(activity.getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID);
        }
    }

    private static void openMedia(FragmentActivity activity, String mime, String url, String filename) {
        if(activity != null) {
            openMediaCallbacks = null;
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(mime, url, filename);
            LoaderUtils.restartLoaderWithBundle(activity.getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(activity), R.id.openMediaLoaderID);
        }
    }

    private static void handleSpecificFile(final FragmentActivity activity, String fileID) {

        StatusCallback<FileFolder> fileFolderStatusCallback = new StatusCallback<FileFolder>() {
            @Override
            public void onResponse(retrofit2.Response<FileFolder> response, com.instructure.canvasapi2.utils.LinkHeaders linkHeaders, ApiType type) {
                super.onResponse(response, linkHeaders, type);
                FileFolder fileFolder = response.body();
                if (fileFolder.isLocked() || fileFolder.isLockedForUser()) {
                    Toast.makeText(activity, String.format(activity.getString(R.string.fileLocked), (fileFolder.getDisplayName() == null) ? activity.getString(R.string.file) : fileFolder.getDisplayName()), Toast.LENGTH_LONG).show();
                } else {
                    openMedia(activity, fileFolder.getContentType(), fileFolder.getUrl(), fileFolder.getDisplayName());
                }
            }
        };

        FileFolderManager.getFileFolderFromURL("files/" + fileID, fileFolderStatusCallback);
    }
}
