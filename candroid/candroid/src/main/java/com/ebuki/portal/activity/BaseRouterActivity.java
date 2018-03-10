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

package com.ebuki.portal.activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.ebuki.portal.R;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.fragment.CalendarListViewFragment;
import com.ebuki.portal.fragment.CourseGridFragment;
import com.ebuki.portal.fragment.CourseModuleProgressionFragment;
import com.ebuki.portal.fragment.GradesGridFragment;
import com.ebuki.portal.fragment.InternalWebviewFragment;
import com.ebuki.portal.fragment.LTIWebViewRoutingFragment;
import com.ebuki.portal.fragment.MessageListFragment;
import com.ebuki.portal.fragment.NotificationListFragment;
import com.ebuki.portal.fragment.ParentFragment;
import com.ebuki.portal.fragment.ToDoListFragment;
import com.ebuki.portal.util.Analytics;
import com.ebuki.portal.util.FileUtils;
import com.ebuki.portal.util.FragUtils;
import com.ebuki.portal.util.LoggingUtility;
import com.ebuki.portal.util.ModuleProgressionUtility;
import com.ebuki.portal.util.Param;
import com.ebuki.portal.util.RouterUtils;
import com.ebuki.portal.util.TabHelper;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.FileFolderManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.managers.ModuleManager;
import com.instructure.canvasapi2.managers.TabManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.FileFolder;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.ModuleItem;
import com.instructure.canvasapi2.models.ModuleItemSequence;
import com.instructure.canvasapi2.models.ModuleObject;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.LoaderUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;


//Intended to handle all routing to fragments from links both internal and external
public abstract class BaseRouterActivity extends CallbackActivity {

    protected abstract void routeFragment(ParentFragment fragment, Navigation.NavigationPosition position);
    protected abstract void routeFragment(ParentFragment fragment);
    protected abstract int existingFragmentCount();
    protected abstract void routeToLandingPage(boolean ignoreDebounce);

    // region Used for param handling
    public static String SUBMISSIONS_ROUTE = "submissions";
    public static String RUBRIC_ROUTE = "rubric";
    // endregion

    // region OpenMediaAsyncTaskLoader
    private Bundle openMediaBundle;
    private LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> openMediaCallbacks;
    private ProgressDialog progressDialog;
    // endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("BaseRouterActivity: onCreate()");

        if(savedInstanceState == null) {
            parse(getIntent());
        }
        LoaderUtils.restoreLoaderFromBundle(this.getSupportLoaderManager(), savedInstanceState, getLoaderCallbacks(), R.id.openMediaLoaderID, Const.OPEN_MEDIA_LOADER_BUNDLE);
        if (savedInstanceState != null && savedInstanceState.getBundle(Const.OPEN_MEDIA_LOADER_BUNDLE) != null) {
            showProgressDialog();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LoaderUtils.saveLoaderBundle(outState, openMediaBundle, Const.OPEN_MEDIA_LOADER_BUNDLE);
        dismissProgressDialog();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Logger.d("BaseRouterActivity: onNewIntent()");
        parse(intent);
    }

    /**
     * Handles the Route based on Navigation context, route type, and master/detail classes
     * Use RouterUtils.canRouteInternally()
     * @param route
     */
    public void handleRoute(RouterUtils.Route route) {
        try {
            if (route.getParamsHash().containsKey(Param.COURSE_ID)) {
                long courseId = Long.parseLong(route.getParamsHash().get(Param.COURSE_ID));

                if (RouterUtils.ROUTE_TYPE.FILE_DOWNLOAD == route.getRouteType()) {
                    if (route.getQueryParamsHash().containsKey(Param.VERIFIER) && route.getQueryParamsHash().containsKey(Param.DOWNLOAD_FRD)) {
                        openMedia(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, courseId, ""), route.getUrl());
                        return;
                    }
                    handleSpecificFile(courseId, route.getParamsHash().get(Param.FILE_ID));
                    return;
                }


                if(RouterUtils.ROUTE_TYPE.LTI == route.getRouteType()) {
                    routeLTI(courseId, route);
                } else {
                    Tab tab = TabHelper.getTabForType(this, route.getTabId());
                    if (route.getContextType() == CanvasContext.Type.COURSE) {
                        routeToCourse(courseId, route, tab);
                    } else if (route.getContextType() == CanvasContext.Type.GROUP) {
                        routeToGroup(courseId, route, tab);
                    }
                }
                return; // do not remove return
            }

            CanvasContext canvasContext = CanvasContext.emptyUserContext();
            if (RouterUtils.ROUTE_TYPE.FILE_DOWNLOAD == route.getRouteType()) {
                openMedia(canvasContext, route.getUrl());
                return;
            }

            if(RouterUtils.ROUTE_TYPE.NOTIFICATION_PREFERENCES == route.getRouteType()) {
                Analytics.trackAppFlow(BaseRouterActivity.this, NotificationPreferencesActivity.class);
                startActivity(new Intent(getContext(), NotificationPreferencesActivity.class));
                return;
            }

            if (route.getMasterCls() != null) {
                Bundle bundle = ParentFragment.createBundle(canvasContext, route.getParamsHash(), route.getQueryParamsHash(), route.getUrl(), null);
                if (route.getDetailCls() != null) {
                    if(existingFragmentCount() == 0) {
                        //Add the landing page fragment, then the details fragment.
                        routeToLandingPage(true);
                    }
                    routeFragment(FragUtils.getFrag(route.getDetailCls(), bundle), route.getNavigationPosition());
                } else {
                    routeFragment(FragUtils.getFrag(route.getMasterCls(), bundle), route.getNavigationPosition());
                }
            }

        } catch (Exception e) {
            LoggingUtility.LogExceptionPlusCrashlytics(BaseRouterActivity.this, e);
            Logger.e("Could not parse and route url in BaseRouterActivity");
            routeToCourseGrid();
        }
    }

    /**
     * The intent will have information about the url to open (usually from clicking on a link in an email)
     * @param intent
     */
    private void parse(Intent intent) {
        if(intent == null) {
            Logger.d("INTENT WAS NULL");
            return;
        }
        Logger.d("INTENT ACTION WAS: " + intent.getAction());
        if(intent.getExtras() == null) {
            Logger.d("INTENT EXTRAS WERE NULL");
            return;
        }

        final Bundle extras = intent.getExtras();
        Logger.logBundle(extras);

        if(extras.containsKey(Const.GOOGLE_NOW_VOICE_SEARCH)) {
            Navigation.NavigationPosition position = (Navigation.NavigationPosition)extras.getSerializable(Const.PARSE);
            if(position == Navigation.NavigationPosition.COURSES) {
                routeFragment(FragUtils.getFrag(CourseGridFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.NOTIFICATIONS) {
                routeFragment(FragUtils.getFrag(NotificationListFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.TODO) {
                routeFragment(FragUtils.getFrag(ToDoListFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.INBOX) {
                routeFragment(FragUtils.getFrag(MessageListFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.GRADES) {
                routeFragment(FragUtils.getFrag(GradesGridFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.CALENDAR) {
                routeFragment(FragUtils.getFrag(CalendarListViewFragment.class, this), position);
            }

            return;
        }


        if(extras.containsKey(Const.MESSAGE) && extras.containsKey(Const.MESSAGE_TYPE)) {
            showMessage(extras.getString(Const.MESSAGE));
        }

        if(extras.containsKey(Const.PARSE)) {
            final String url = extras.getString(Const.URL);
            RouterUtils.routeUrl(this, url, false);
        } else if(extras.containsKey(Const.BOOKMARK)) {
            final String url = extras.getString(Const.URL);
            RouterUtils.routeUrl(this, url, false);
        }
    }

    private void routeLTI(final long courseId, final RouterUtils.Route route) {
        //Since we do not know if the LTI is a tab we load in a details fragment.
        if (route.getContextType() == CanvasContext.Type.COURSE) {
            CourseManager.getCourseWithGrade(courseId, new StatusCallback<Course>() {

                private boolean routedAlready = false;

                @Override
                public void onResponse(retrofit2.Response<Course> response, LinkHeaders linkHeaders, ApiType type) {

                    // In order to avoid adding fragments twice, just route with the cache if we can
                    if (routedAlready) {
                        return;
                    }
                    routedAlready = true;
                    if(response.body() == null) {
                        showMessage(getString(R.string.could_not_route_course));
                    }
                    else {
                        getUserSelf(true, true);
                        routeFragment(ParentFragment.createFragment(LTIWebViewRoutingFragment.class,
                                LTIWebViewRoutingFragment.createBundle(response.body(), route.getUrl())));
                    }
                }
            }, true);
        } else if (route.getContextType() == CanvasContext.Type.GROUP) {
            GroupManager.getDetailedGroup(courseId, new StatusCallback<Group>() {

                private boolean routedAlready = false;

                @Override
                public void onResponse(retrofit2.Response<Group> response, LinkHeaders linkHeaders, ApiType type) {
                    if (routedAlready) {
                        return;
                    }
                    routedAlready = true;
                    if (response.body() == null) {
                        showMessage(getString(R.string.could_not_route_group));
                    } else {
                        getUserSelf(true, false);
                        routeFragment(ParentFragment.createFragment(LTIWebViewRoutingFragment.class,
                                LTIWebViewRoutingFragment.createBundle(response.body(), route.getUrl())));
                    }
                }
            }, true);
        }
    }

    private void routeModuleProgression(final CanvasContext canvasContext, final RouterUtils.Route route) {

        ModuleManager.getModuleItemSequence(canvasContext, ModuleManager.MODULE_ASSET_MODULE_ITEM, route.getQueryParamsHash().get("module_item_id"), new StatusCallback<ModuleItemSequence>() {

            private boolean routedWithCache = false;

            @Override
            public void onResponse(retrofit2.Response<ModuleItemSequence> response, LinkHeaders linkHeaders, ApiType type) {
                final ModuleItemSequence moduleItemSequence = response.body();
                if(routedWithCache) {
                    return;
                }

                if(APIHelper.isCachedResponse(response)) {
                    routedWithCache = true;
                }

                //make sure that there is a sequence
                if(moduleItemSequence.getItems().length > 0) {
                    //get the current module item. we'll use the id of this down below
                    final ModuleItem current = moduleItemSequence.getItems()[0].getCurrent();

                    ModuleManager.getAllModuleItems(canvasContext, current.getModuleId(), new StatusCallback<List<ModuleItem>>() {

                        private boolean routedModuleItemsWithCache = false;

                        @Override
                        public void onResponse(retrofit2.Response<List<ModuleItem>> response, LinkHeaders linkHeaders, ApiType type) {

                            if (routedModuleItemsWithCache) {
                                return;
                            }

                            if(APIHelper.isCachedResponse(response)) {
                                routedModuleItemsWithCache = true;
                            }

                            ArrayList<ArrayList<ModuleItem>> moduleItemsArrayList = new ArrayList<>();

                            ArrayList<ModuleObject> moduleObjectsArray = new ArrayList<>();
                            moduleObjectsArray.add(moduleItemSequence.getModules()[0]);

                            ArrayList<ModuleItem> items = new ArrayList<>();
                            items.addAll(response.body());
                            moduleItemsArrayList.add(items);

                            ModuleProgressionUtility.ModuleHelper moduleHelper = ModuleProgressionUtility.prepareModulesForCourseProgression(getContext(), current.getId(), moduleObjectsArray, moduleItemsArrayList);

                            routeFragment(ParentFragment.createFragment(CourseModuleProgressionFragment.class, CourseModuleProgressionFragment.createBundle(moduleObjectsArray, moduleHelper.strippedModuleItems, (Course) canvasContext, moduleHelper.newGroupPosition, moduleHelper.newChildPosition)));

                        }
                    }, true);
                }

            }
        }, true);
    }

    private void routeToCourseGrid() {
        Logger.d("routeToCourseGrid()");
        routeFragment(FragUtils.getFrag(CourseGridFragment.class, this));
    }

    private void routeMasterDetail(CanvasContext canvasContext, RouterUtils.Route route, Tab tab) {
        Logger.d("routing with tab: " + (tab == null ? "??" : tab.getTabId()));
        Bundle bundle = ParentFragment.createBundle(canvasContext, route.getParamsHash(), route.getQueryParamsHash(), route.getUrl(), tab);
        if (route.getDetailCls() != null) {
            if(existingFragmentCount() == 0) {
                //Add the landing page fragment, then the details fragment.
                routeToLandingPage(true);
            }
            routeFragment(FragUtils.getFrag(route.getDetailCls(), bundle));
        } else {
            if (route.getMasterCls() != null) {
                routeFragment(FragUtils.getFrag(route.getMasterCls(), bundle));//TODO: test this, not sure if that is correct but if we have a tab probably in a course.
            } else { // Used for Tab.Home (so that no masterCls has to be set)
                routeFragment(TabHelper.getFragmentByTab(tab, canvasContext));
            }
        }
    }

    private void routeToCourse(long id, final RouterUtils.Route route, final Tab tab) {
        CourseManager.getCourseWithGrade(id, new StatusCallback<Course>() {
            Course cacheCourse;

            private void tryToRoute(@Nullable Course course) {
                if (course == null) {
                    Logger.d("Course was null, could not route.");
                    showMessage(getString(R.string.could_not_route_course));
                } else {
                    routeToCourseOrGroupWithTabCheck(course, route, tab);
                }
            }
            @Override
            public void onResponse(retrofit2.Response<Course> response, LinkHeaders linkHeaders, ApiType type) {
                if(type == ApiType.CACHE) {
                    cacheCourse = response.body();
                } else {
                    tryToRoute(response.body());
                }
            }

            @Override
            public void onFail(Call<Course> callResponse, Throwable error, retrofit2.Response response) {
                if(response != null && response.code() != 504) {
                    tryToRoute(cacheCourse);
                }
            }

        }, true);
    }

    private void routeToGroup(long id, final RouterUtils.Route route, final Tab tab) {
        Logger.d("routeToGroup()");
        GroupManager.getDetailedGroup(id, new StatusCallback<Group>() {
            Group cacheGroup;

            @Override
            public void onResponse(retrofit2.Response<Group> response, LinkHeaders linkHeaders, ApiType type) {
                if(type.isCache()) {
                    cacheGroup = response.body();
                } else {
                    routeToGroup(response.body());
                }
            }

            private void routeToGroup(@Nullable Group group) {
                if (group == null) {
                    Logger.d("Group was null, could not route.");
                    showMessage(getString(R.string.could_not_route_group));
                } else {
                    routeToCourseOrGroupWithTabCheck(group, route, tab);
                }
            }
            @Override
            public void onFail(Call<Group> response, Throwable error, int code) {
                //we don't want to go first page on a 504, it just means we haven't cached the data yet
                if(code != 504) {
                    routeToGroup(cacheGroup);
                }
            }
        }, true);
    }

    private void routeToCourseOrGroupWithTabCheck(final CanvasContext canvasContext, final RouterUtils.Route route, final Tab tab) {
        TabManager.getTabs(canvasContext, new StatusCallback<List<Tab>>() {
            @Override
            public void onResponse(retrofit2.Response<List<Tab>> response, LinkHeaders linkHeaders, ApiType type) {
                if(Tab.SYLLABUS_ID.equals(tab.getTabId())) {
                    //We do not allow routing to the syllabus if it's hidden
                    boolean tabExistsForCourse = false;
                    for (Tab t : response.body()) {
                        if (t.getTabId().equals(tab.getTabId())) {
                            tabExistsForCourse = true;
                            break;
                        }
                    }

                    if(tabExistsForCourse) {
                        //Route cause tab exists
                        Logger.d("Attempting to route to group: " + canvasContext.getName());
                        getUserSelf(true, true);
                        routeMasterDetail(canvasContext, route, tab);
                    } else {
                        Logger.d("Course/Group tab hidden, or locked.");
                        showMessage(getString(R.string.could_not_route_locked));
                    }

                } else if (route.getQueryParamsHash() != null && route.getQueryParamsHash().containsKey("module_item_id")) {
                    //if we're routing to something in a module then we need to open it inside of CourseModuleProgression
                    routeModuleProgression(canvasContext, route);
                } else {
                    Logger.d("Attempting to route to course or group: " + canvasContext.getName());
                    getUserSelf(true, true);
                    routeMasterDetail(canvasContext, route, tab);
                }
                cancel();
            }
        }, true);
    }

    private void handleSpecificFile(long courseId, String fileID) {
        final CanvasContext canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, courseId, "");
        Logger.d("handleSpecificFile()");
        //If the file no longer exists (404), we want to show a different crouton than the default.
        StatusCallback<FileFolder> fileFolderCanvasCallback = new StatusCallback<FileFolder>() {
            @Override
            public void onResponse(retrofit2.Response<FileFolder> response, LinkHeaders linkHeaders, ApiType type) {
                FileFolder fileFolder = response.body();
                if (fileFolder.isLocked() || fileFolder.isLockedForUser()) {
                    Toast.makeText(getContext(), String.format(getContext().getString(R.string.fileLocked), (fileFolder.getDisplayName() == null) ? getString(R.string.file) : fileFolder.getDisplayName()), Toast.LENGTH_LONG).show();
                } else {
                    openMedia(canvasContext, fileFolder.getContentType(), fileFolder.getUrl(), fileFolder.getDisplayName());
                }
            }
        };

        FileFolderManager.getFileFolderFromURL("files/" + fileID, fileFolderCanvasCallback);
    }

    ///////////////////////////////////////////////////////////////////////////
    // OpenMediaAsyncTaskLoader
    ///////////////////////////////////////////////////////////////////////////

    private LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> getLoaderCallbacks() {
        if (openMediaCallbacks == null) {
            openMediaCallbacks = new LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>() {
                @Override
                public Loader<OpenMediaAsyncTaskLoader.LoadedMedia> onCreateLoader(int id, Bundle args) {
                    showProgressDialog();
                    return new OpenMediaAsyncTaskLoader(getContext(), args);
                }

                @Override
                public void onLoadFinished(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader, OpenMediaAsyncTaskLoader.LoadedMedia loadedMedia) {
                    dismissProgressDialog();

                    try {
                        if (loadedMedia.isError()) {
                            Toast.makeText(getContext(), getString(loadedMedia.getErrorMessage()), Toast.LENGTH_LONG).show();
                        } else if (loadedMedia.isHtmlFile()) {
                            InternalWebviewFragment.loadInternalWebView(BaseRouterActivity.this, (Navigation) BaseRouterActivity.this, loadedMedia.getBundle());
                        } else if (loadedMedia.getIntent() != null) {
                            if(loadedMedia.getIntent().getType().contains("pdf")){
                                //show pdf with PSPDFkit
                                Uri uri = loadedMedia.getIntent().getData();
                                FileUtils.showPdfDocument(uri, loadedMedia, getContext());
                            } else {
                                getContext().startActivity(loadedMedia.getIntent());
                            }
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getContext(), R.string.noApps, Toast.LENGTH_LONG).show();
                    }
                    openMediaBundle = null; // set to null, otherwise the progressDialog will appear again
                }

                @Override
                public void onLoaderReset(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader) {

                }
            };
        }
        return openMediaCallbacks;
    }

    public void openMedia(CanvasContext canvasContext, String url) {
        openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(canvasContext, url);
        LoaderUtils.restartLoaderWithBundle(this.getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(), R.id.openMediaLoaderID);
    }

    public void openMedia(CanvasContext canvasContext, String mime, String url, String filename) {
        openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(canvasContext, mime, url, filename);
        LoaderUtils.restartLoaderWithBundle(this.getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(), R.id.openMediaLoaderID);
    }

    // ProgressDialog
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage(getString(R.string.opening));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dismissProgressDialog();
                openMediaBundle = null; // set to null, otherwise the progressDialog will appear again
                BaseRouterActivity.this.getSupportLoaderManager().destroyLoader(R.id.openMediaLoaderID);
            }
        });
        progressDialog.setCanceledOnTouchOutside(true);
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            initProgressDialog();
        }
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

