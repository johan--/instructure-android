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

package com.instructure.teacher.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.UserAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.managers.CourseNicknameManager;
import com.instructure.canvasapi2.models.CanvasColor;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.CourseNickname;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.activities.BasePresenterActivity;
import com.instructure.pandautils.dialogs.RatingDialog;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.teacher.BuildConfig;
import com.instructure.teacher.R;
import com.instructure.teacher.dialog.ColorPickerDialog;
import com.instructure.teacher.dialog.EditCourseNicknameDialog;
import com.instructure.teacher.events.CourseUpdatedEvent;
import com.instructure.teacher.factory.InitActivityPresenterFactory;
import com.instructure.teacher.fragments.AllCoursesFragment;
import com.instructure.teacher.fragments.CourseBrowserFragment;
import com.instructure.teacher.fragments.CoursesFragment;
import com.instructure.teacher.fragments.EditFavoritesFragment;
import com.instructure.teacher.fragments.EmptyFragment;
import com.instructure.teacher.fragments.InboxFragment;
import com.instructure.teacher.fragments.InboxFragment_new;
import com.instructure.teacher.fragments.ProfileFragment;
import com.instructure.teacher.interfaces.Identity;
import com.instructure.teacher.interfaces.InitActivityInteractions;
import com.instructure.teacher.presenters.InitActivityPresenter;
import com.instructure.teacher.router.Route;
import com.instructure.teacher.router.RouteMatcher;
import com.instructure.teacher.utils.AppType;
import com.instructure.teacher.utils.ColorKeeper;
import com.instructure.teacher.viewinterface.InitActivityView;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import instructure.androidblueprint.PresenterFactory;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Response;

import static android.view.View.GONE;
import static com.instructure.teacher.R.id.detail;

public class InitActivity extends BasePresenterActivity<InitActivityPresenter, InitActivityView> implements InitActivityView,
        CoursesFragment.CourseListCallback,
        AllCoursesFragment.CourseBrowserCallback,
        InitActivityInteractions {

    //region Binding

    @BindView(R.id.bottom_bar) BottomNavigationView mBottomBar;
    @BindView(R.id.fakeToolbar) View mFakeToolbar;
    @BindView(R.id.container) FrameLayout mContainer;
    @BindView(R.id.rootView) PercentRelativeLayout mMasterDetailContainer;
    @BindView(R.id.middleTopDivider) View mMiddleTopDivider;
    //endregion

    private final int COURSES_SELECTED = 0;
    private final int INBOX_SELECTED = 1;
    private final int PROFILE_SELECTED = 2;
    private int mCurrentTabSelected = 0;
    private static final String CURRENT_TAB = "currentTab";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        ButterKnife.bind(this);

        if(savedInstanceState != null && savedInstanceState.containsKey(CURRENT_TAB)) {
            mCurrentTabSelected = savedInstanceState.getInt(CURRENT_TAB, 0);
        }

        RatingDialog.showRatingDialog(this, com.instructure.pandautils.utils.AppType.TEACHER);
    }

    @Override
    protected void onReadySetGo(InitActivityPresenter presenter) {
        final ColorStateList colorStateList = getNavigationColorStateList();
        mBottomBar.setItemTextColor(colorStateList);
        mBottomBar.setItemIconTintList(colorStateList);
        mBottomBar.setOnNavigationItemSelectedListener(mTabSelectedListener);
        mFakeToolbar.setBackgroundColor(ThemePrefs.getPrimaryColor());
        switch(mCurrentTabSelected) {
            case 0:
                addCoursesFragment();
                break;
            case 1:
                addInboxFragment();
                break;
            case 2:
                addProfileFragment();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_TAB, mCurrentTabSelected);
    }

    @Override
    protected PresenterFactory<InitActivityPresenter> getPresenterFactory() {
        return new InitActivityPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(InitActivityPresenter presenter) {

    }

    @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mTabSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.tab_courses:
                    addCoursesFragment();
                    mCurrentTabSelected = COURSES_SELECTED;
                    return true;
                case R.id.tab_inbox:
                    addInboxFragment();
                    mCurrentTabSelected = INBOX_SELECTED;
                    return true;
                case R.id.tab_profile:
                    addProfileFragment();
                    mCurrentTabSelected = PROFILE_SELECTED;
                    return true;
                default:
                    return false;
            }
        }
    };

    private void addCoursesFragment() {
        if (getSupportFragmentManager().findFragmentByTag(CoursesFragment.class.getSimpleName()) == null) {
            setBaseFragment(CoursesFragment.getInstance(AppType.TEACHER));
        } else if(getResources().getBoolean(R.bool.is_device_tablet)) {
            mContainer.setVisibility(View.VISIBLE);
            mMasterDetailContainer.setVisibility(View.GONE);
        }
    }

    private void addInboxFragment() {
        if(BuildConfig.POINT_FIVE) {
            if (getSupportFragmentManager().findFragmentByTag(InboxFragment.class.getSimpleName()) == null) {
                // if we're a tablet we want the master detail view
                if(getResources().getBoolean(R.bool.is_device_tablet)) {
                    Route route = new Route(InboxFragment.class, null);
                    Fragment masterFragment = RouteMatcher.getMasterFragment(null, route);
                    Fragment detailFragment = EmptyFragment.newInstance(RouteMatcher.getClassDisplayName(this, route.getPrimaryClass()));
                    putFragments(masterFragment, detailFragment, true);
                    mMiddleTopDivider.setBackgroundColor(ThemePrefs.getPrimaryColor());

                } else {
                    setBaseFragment(new InboxFragment());
                }
            } else if(getResources().getBoolean(R.bool.is_device_tablet)) {
                mMasterDetailContainer.setVisibility(View.VISIBLE);
                mContainer.setVisibility(GONE);
                mMiddleTopDivider.setBackgroundColor(ThemePrefs.getPrimaryColor());
            }
        } else {
            if (getSupportFragmentManager().findFragmentByTag(InboxFragment_new.class.getSimpleName()) == null) {
                setBaseFragment(new InboxFragment_new());
            }
        }
    }

    @Override
    public void addFragment(Route route) {
        addDetailFragment(RouteMatcher.getDetailFragment(route.getCanvasContext(), route));
    }

    private void addDetailFragment(Fragment fragment) {
        if(fragment == null) throw new IllegalStateException("InitActivity.class addDetailFragment was null");

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment currentFragment = fm.findFragmentById(R.id.detail);

        if(identityMatch(currentFragment, fragment)) return;

        ft.replace(R.id.detail, fragment, fragment.getClass().getSimpleName());
        if(currentFragment != null && !(currentFragment instanceof EmptyFragment)) {
            //Add to back stack if not empty fragment and a fragment exists
            ft.addToBackStack(fragment.getClass().getSimpleName());
        }
        ft.commit();
    }

    private boolean identityMatch(Fragment fragment1, Fragment fragment2) {
        if(fragment1 != null && fragment2 instanceof Identity && fragment1 instanceof Identity) {
            if(((Identity) fragment1).getIdentity() != null && ((Identity) fragment2).getIdentity()!= null) {
                //Check if fragment identities are the same
                if(((Identity) fragment1).getIdentity().longValue() == ((Identity) fragment2).getIdentity().longValue()) return true;
            }
        }
        return false;
    }

    private void addProfileFragment() {
        if (getSupportFragmentManager().findFragmentByTag(ProfileFragment.class.getSimpleName()) == null) {
            setBaseFragment(new ProfileFragment());
        } else if(getResources().getBoolean(R.bool.is_device_tablet)) {
            mContainer.setVisibility(View.VISIBLE);
            mMasterDetailContainer.setVisibility(View.GONE);
        }
    }

    private void setBaseFragment(Fragment fragment) {
        putFragment(fragment, true);
    }

    private void addFragment(Fragment fragment) {
        putFragment(fragment, false);
    }

    private void putFragment(Fragment fragment, boolean clearBackStack) {
        mMasterDetailContainer.setVisibility(GONE);
        mContainer.setVisibility(View.VISIBLE);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (clearBackStack) {
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStackImmediate(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        } else {
            ft.addToBackStack(null);
        }
        ft.replace(R.id.container, fragment, fragment.getClass().getSimpleName());
        ft.commit();
    }

    private void putFragments(Fragment fragment, Fragment detailFragment, boolean clearBackStack) {
        mMasterDetailContainer.setVisibility(View.VISIBLE);
        mContainer.setVisibility(GONE);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (clearBackStack) {
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStackImmediate(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        } else {
            ft.addToBackStack(null);
        }
        ft.replace(R.id.master, fragment, fragment.getClass().getSimpleName());
        ft.replace(detail, detailFragment, detailFragment.getClass().getSimpleName());

        ft.commit();
    }
    private ColorStateList getNavigationColorStateList() {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked},  // checked
                new int[] { -android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_selected}  // unchecked
        };

        int[] colors = new int[] {
                ThemePrefs.getBrandColor(),
                ContextCompat.getColor(InitActivity.this, R.color.canvas_default_tab_unselected),
                ContextCompat.getColor(InitActivity.this, R.color.canvas_default_tab_unselected)
        };

        return new ColorStateList(states, colors);
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, InitActivity.class);
    }

    @Override
    public void onShowAllCoursesList() {
        addFragment(AllCoursesFragment.getInstance(AppType.TEACHER));
    }

    @Override
    public void onShowEditFavoritesList() {
        Bundle args = EditFavoritesFragment.makeBundle(AppType.TEACHER);
        RouteMatcher.route(this, new Route(EditFavoritesFragment.class,  null, args));
    }

    @Override
    public void onEditCourseNickname(final Course course) {
        EditCourseNicknameDialog.getInstance(this.getSupportFragmentManager(), course, new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
                CourseNicknameManager.setCourseNickname(course.getId(), s, new StatusCallback<CourseNickname>() {
                    @Override
                    public void onResponse(Response<CourseNickname> response,
                                           LinkHeaders linkHeaders,
                                           ApiType type) {
                        super.onResponse(response, linkHeaders, type);
                        String name = response.body().getName();
                        String nickname = response.body().getNickname();
                        if (nickname == null) {
                            course.setName(name);
                            course.setOriginalName(null);
                        } else {
                            course.setName(nickname);
                            course.setOriginalName(name);
                        }
                        CourseUpdatedEvent event = new CourseUpdatedEvent(course, null);
                        //remove any events just in case they want to change the name more than once
                        EventBus.getDefault().removeStickyEvent(event);
                        EventBus.getDefault().postSticky(event);
                    }
                });
                return null;
            }
        }).show(this.getSupportFragmentManager(), EditCourseNicknameDialog.class.getName());
    }

    @Override
    public void onShowCourseDetails(@NotNull Course course) {
        RouteMatcher.route(this, new Route(CourseBrowserFragment.class, course));
    }

    @Override
    public void onPickCourseColor(@NotNull final Course course) {
        ColorPickerDialog.newInstance(getSupportFragmentManager(), course, new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(final Integer color) {

                StatusCallback<CanvasColor> courseColorCallback = new StatusCallback<CanvasColor>() {
                    @Override
                    public void onResponse(Response<CanvasColor> response, LinkHeaders linkHeaders, ApiType type) {
                        ColorKeeper.INSTANCE.addToCache(course.getContextId(), color);

                        CourseUpdatedEvent event = new CourseUpdatedEvent(course, null);
                        EventBus.getDefault().removeStickyEvent(event);
                        EventBus.getDefault().postSticky(event);
                    }

                    @Override
                    public void onFail(Call<CanvasColor> callResponse, Throwable error, Response response) {
                        Toast.makeText(InitActivity.this, R.string.colorPickerError, Toast.LENGTH_SHORT).show();
                    }
                };

                RestBuilder adapter = new RestBuilder(courseColorCallback);
                UserAPI.setColor(adapter, courseColorCallback, course.getContextId(), color);

                return null;
            }
        }).show(getSupportFragmentManager(), ColorPickerDialog.class.getSimpleName());
    }
}
