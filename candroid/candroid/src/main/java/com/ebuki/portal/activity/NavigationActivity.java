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

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ebuki.portal.BuildConfig;
import com.ebuki.portal.R;
import com.ebuki.portal.adapter.CourseNavigationAdapter;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.dialog.AccountNotificationDialog;
import com.ebuki.portal.dialog.HelpDialogStyled;
import com.ebuki.portal.fragment.AnnouncementListFragment;
import com.ebuki.portal.fragment.ApplicationSettingsFragment;
import com.ebuki.portal.fragment.BookmarksFragment;
import com.ebuki.portal.fragment.CalendarListViewFragment;
import com.ebuki.portal.fragment.CourseGridFragment;
import com.ebuki.portal.fragment.DetailedConversationFragment;
import com.ebuki.portal.fragment.DetailedDiscussionFragment;
import com.ebuki.portal.fragment.DiscussionListFragment;
import com.ebuki.portal.fragment.GradesGridFragment;
import com.ebuki.portal.fragment.InboxFragment;
import com.ebuki.portal.fragment.LTIWebViewFragment;
import com.ebuki.portal.fragment.MessageListFragment;
import com.ebuki.portal.fragment.NotificationListFragment;
import com.ebuki.portal.fragment.ParentFragment;
import com.ebuki.portal.fragment.ProfileFragment;
import com.ebuki.portal.fragment.ToDoListFragment;
import com.ebuki.portal.interfaces.OnEventUpdatedCallback;
import com.ebuki.portal.model.PushNotification;
import com.ebuki.portal.receivers.PushExternalReceiver;
import com.ebuki.portal.tasks.LogoutAsyncTask;
import com.ebuki.portal.tasks.SwitchUsersAsyncTask;
import com.ebuki.portal.util.Analytics;
import com.ebuki.portal.util.ApplicationManager;
import com.ebuki.portal.util.FragUtils;
import com.ebuki.portal.util.RouterUtils;
import com.ebuki.portal.util.TabHelper;
import com.ebuki.portal.view.ActionbarCourseSpinner;
import com.ebuki.portal.view.SpinnerInteractionListener;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.BookmarkManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.UnreadCountManager;
import com.instructure.canvasapi2.models.AccountNotification;
import com.instructure.canvasapi2.models.Bookmark;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Enrollment;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.LaunchDefinition;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.canvasapi2.models.UnreadConversationCount;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.canvasapi2.utils.NumberHelper;
import com.instructure.loginapi.login.api.zendesk.utilities.ZendeskDialogStyled;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.ColorUtils;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.ProfileUtils;
import com.instructure.pandautils.utils.TutorialUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;

public class NavigationActivity extends BaseRouterActivity implements
        View.OnClickListener,
        Navigation,
        ProfileFragment.OnProfileChangedCallback,
        DetailedDiscussionFragment.UpdateUnreadListener,
        DetailedConversationFragment.UpdateMessageStateListener,
        ZendeskDialogStyled.ZendeskDialogResultListener,
        OnEventUpdatedCallback,
        MessageListFragment.OnUnreadCountInvalidated,
        AccountNotificationDialog.OnAnnouncementCountInvalidated {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    protected static final String COURSE_NAVIGATION_POSITION = "course_navigation_position";
    private static final String LAST_ACTIONBAR_COLOR = "lastActionbarColor";
    private static final String FRAGMENTS_BY_CONTAINER = "fragmentsByContainer";

    private ActionBarDrawerToggle mDrawerToggle;
    protected DrawerLayout mDrawerLayout;
    private CircleImageView mUserProfilePic;
    private TextView mUserName;
    private TextView mUserEmail;
    private ImageView mHeaderImage;
    private RelativeLayout userContainer;
    private LinearLayout navMenuContainer;
    private RelativeLayout previousUsersWrapper;
    private ImageView expandCollapse;
    private EditText mDialogEditText;
    private LinearLayout mNavigationShortcutContainer;
    private ProgressBar mNavigationShortcutProgressBar;
    protected boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    protected NavigationPosition mCurrentSelectedPosition = NavigationPosition.COURSES;
    private int mLastActionbarColor = Integer.MAX_VALUE;
    private ImageView mCourseShortcutDropdown;
    private RelativeLayout mExpandRippleView;

    protected HashMap<Integer, Integer> mFragmentIdsByContainerId = new HashMap<>();

    private View mSpinnerContainer;
    private CourseNavigationAdapter mCourseNavigationAdapter;

    private ActionbarCourseSpinner mActionbarSpinner;

    private List<AccountNotification> accountNotifications;

    private ScrollView mScrollView;

    private int mSelectedTabPosition = -1;

    private boolean addFragmentEnabled = true;
    private static final int FRAGMENT_TRANSACTION_DELAY_TIME = 1000;

    @Override
    public int contentResId() {
        return R.layout.drawer_layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        getSupportFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);
        mUserLearnedDrawer = ApplicationManager.getPrefs(getContext()).load(Const.PREF_USER_LEARNED_DRAWER, false);
        boolean userLearnedTutorial = ApplicationManager.getPrefs(getContext()).load(Const.TUTORIAL_VIEWED, false);

        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
            mCurrentSelectedPosition = (NavigationPosition)savedInstanceState.getSerializable(STATE_SELECTED_POSITION);
            mSelectedTabPosition = savedInstanceState.getInt(COURSE_NAVIGATION_POSITION, -1);
            mLastActionbarColor = savedInstanceState.getInt(LAST_ACTIONBAR_COLOR, Integer.MAX_VALUE);
            mFragmentIdsByContainerId = (HashMap)savedInstanceState.getSerializable(FRAGMENTS_BY_CONTAINER);
        }

        //Call after views are initialized
        setUpNavigationDrawer();
        getUserSelf(false, false);
        getUnreadCount();

        if(savedInstanceState == null && getIntent() != null) {
            if(!hasUnreadPushNotification(getIntent().getExtras())) {
                setPreferredLandingPage();
            } else {
                handlePushNotification(hasUnreadPushNotification(getIntent().getExtras()));
            }
        }

        if(!userLearnedTutorial) {
            startActivity(new Intent(getContext(), TutorialActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mLocaleChangeReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mLocaleChangeReceiver);
        super.onDestroy();
    }

    @Override
    public boolean showHomeAsUp() {
        return false;
    }

    @Override
    public boolean showTitleEnabled() {
        return true;
    }

    @Override
    public void onUpPressed() {}

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent != null && intent.getBooleanExtra(Const.RECEIVED_FROM_OUTSIDE, false) && getSupportFragmentManager() != null){
            try{
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }catch(Exception e){
                Logger.e("Exception Occurred in NavigationActivity onNewIntent while attempting to clear the backstack.");
            }
        }

        super.onNewIntent(intent);
        //Switching languages will trigger this, so we check for our Pending intent id
        if(intent != null
                && intent.hasExtra(com.ebuki.portal.util.Const.LANGUAGES_PENDING_INTENT_KEY)
                && intent.getIntExtra(com.ebuki.portal.util.Const.LANGUAGES_PENDING_INTENT_KEY, 0) != com.ebuki.portal.util.Const.LANGUAGES_PENDING_INTENT_ID) {
            handlePushNotification(hasUnreadPushNotification(intent.getExtras()));
        }
    }

    private void handlePushNotification(boolean hasUnreadNotifications) {
        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (hasUnreadNotifications) {
                    setPushNotificationAsRead();
                }

                String html_url = extras.getString(PushNotification.HTML_URL, "");

                if(!RouterUtils.canRouteInternally(this, html_url, ApiPrefs.getDomain(), true)) {
                    routeFragment(FragUtils.getFrag(NotificationListFragment.class, this), Navigation.NavigationPosition.NOTIFICATIONS);
                }
            }
        }
    }

    private boolean hasUnreadPushNotification(Bundle extras) {
        if(extras != null && extras.containsKey(PushExternalReceiver.NEW_PUSH_NOTIFICATION)) {
            return extras.getBoolean(PushExternalReceiver.NEW_PUSH_NOTIFICATION, false);
        }
        return false;
    }

    private void setPushNotificationAsRead() {
        getIntent().putExtra(PushExternalReceiver.NEW_PUSH_NOTIFICATION, false);
        PushNotification.clearPushHistory(getApplicationContext());
    }

    protected void setupViews(){
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mNavigationShortcutContainer = (LinearLayout) findViewById(R.id.courseShortcutContainer);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(getContext().getResources().getInteger(R.integer.slide_up_down_duration));
        mNavigationShortcutContainer.setLayoutTransition(layoutTransition);
        mNavigationShortcutProgressBar = (ProgressBar) findViewById(R.id.navigationShortcutProgressBar);
        mCourseShortcutDropdown = (ImageView) findViewById(R.id.courseDropdown);
        mExpandRippleView = (RelativeLayout) findViewById(R.id.expandRipple);
        bindViews();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getToolbar().setElevation(Const.ACTIONBAR_ELEVATION);
        }
    }

    @Override
    public void onCourseFavoritesFinished(List<Course> courses) {
        if(courses == null || mNavigationShortcutContainer == null) {
            return;
        }
        mNavigationShortcutProgressBar.setVisibility(View.GONE);
        boolean isExpanded = ApplicationManager.getPrefs(getContext()).load(Const.NAVIGATION_SHORTCUTS_EXPANDED, false);
        if(isExpanded) {
            mNavigationShortcutContainer.removeAllViewsInLayout();
            LayoutInflater inflater = getLayoutInflater();
            if(courses.size() == 0) {
                View viewToAdd = inflater.inflate(R.layout.course_shortcuts_emptyview, null);
                mNavigationShortcutContainer.addView(viewToAdd);
                mNavigationShortcutProgressBar.setVisibility(View.GONE);
                mNavigationShortcutContainer.setVisibility(View.VISIBLE);
                return;
            }

            for(Course course : courses) {
                addNavigationShortcutToContainer(inflater, course);
            }
            mNavigationShortcutProgressBar.setVisibility(View.GONE);
            mNavigationShortcutContainer.setVisibility(View.VISIBLE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mNavigationShortcutContainer.getLayoutTransition().setDuration(0);// Disable so the networking callback won't cause the UI to flash
            }
        }
    }

    @Override
    public void redrawNavigationShortcuts() {
        mNavigationShortcutProgressBar.setVisibility(View.VISIBLE);
        CourseManager.getAllFavoriteCourses(true, coursesCallback);
    }

    @Override
    public void courseNameChanged(CanvasContext canvasContext) {
        getCourseNavigationAdapter().updateCanvasContext(canvasContext);
        mNavigationShortcutProgressBar.setVisibility(View.VISIBLE);
        CourseManager.getAllFavoriteCourses(true, coursesNoCacheCallback);
    }

    private void addNavigationShortcutToContainer(LayoutInflater inflater, final CanvasContext canvasContext) {
        View viewToAdd = inflater.inflate(R.layout.navigation_shortcut_item, null);
        ImageView indicator = (ImageView) viewToAdd.findViewById(R.id.indicator);
        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint().setColor(CanvasContextColor.getCachedColor(getContext(), canvasContext.getContextId()));
        indicator.setBackgroundDrawable(circle);
        ((TextView) viewToAdd.findViewById(R.id.text)).setText(canvasContext.getName());
        viewToAdd.setTag(canvasContext);
        viewToAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBackStack(null);

                CanvasContext item = (CanvasContext) v.getTag();
                Tab homeTab = Tab.newInstance(canvasContext.getHomePageID(), getString(R.string.home));
                addFragment(TabHelper.getFragmentByTab(homeTab, item));
                closeNavigationDrawer();
            }
        });
        mNavigationShortcutContainer.addView(viewToAdd);
    }

    final public boolean isDrawerOpen() {
        if(mDrawerLayout == null || mScrollView == null) { return false; }
        return mDrawerLayout.isDrawerOpen(mScrollView);
    }

    final public void closeNavigationDrawer() {
        if(mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mScrollView);
        }
    }

    final public void openNavigationDrawer() {
        if(mDrawerLayout != null) {
            mDrawerLayout.openDrawer(mScrollView);
        }
    }

    private void setUpNavigationDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mDrawerLayout.setFocusableInTouchMode(false);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(NavigationActivity.this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    ApplicationManager.getPrefs(getContext()).save(Const.PREF_USER_LEARNED_DRAWER, true);
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                //make the scrollview that is inside the drawer scroll to the top
                mScrollView.scrollTo(0, 0);
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.openDrawer(mScrollView);
                }
            }, 230);

        }
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Setup the actionbar but make sure we call super last so the fragments can override it as needed.
        updateActionbar();
        mDrawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putInt(LAST_ACTIONBAR_COLOR, mLastActionbarColor);
        outState.putSerializable(FRAGMENTS_BY_CONTAINER, mFragmentIdsByContainerId);
        outState.putInt(COURSE_NAVIGATION_POSITION, mSelectedTabPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateActionbar();
    }

    ///////////////////////////////////////////////////////////////////////////
    // DRAWER SETUP
    ///////////////////////////////////////////////////////////////////////////

    private void bindViews() {
        RelativeLayout profileLayout = (RelativeLayout)findViewById(R.id.profileLayout);
        mHeaderImage = (ImageView)findViewById(R.id.headerImage);
        mUserName = (TextView)findViewById(R.id.userName);
        mUserEmail = (TextView)findViewById(R.id.userEmail);
        mUserProfilePic = (CircleImageView)findViewById(R.id.userProfilePic);
        userContainer = (RelativeLayout)findViewById(R.id.userNameContainer);
        navMenuContainer = (LinearLayout)findViewById(R.id.navMenuContainer);
        previousUsersWrapper = (RelativeLayout)findViewById(R.id.previousUsersWrapper);
        expandCollapse = (ImageView)findViewById(R.id.expand_collapse);
        findViewById(R.id.logout).setOnClickListener(this);
        profileLayout.setOnClickListener(this);


        userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navMenuContainer.getVisibility() == View.VISIBLE) {
                    expandCollapse.startAnimation(AnimationUtils.loadAnimation(NavigationActivity.this, R.anim.rotate));
                    navMenuContainer.setVisibility(View.GONE);
                    previousUsersWrapper.setVisibility(View.VISIBLE);
                } else {
                    expandCollapse.startAnimation(AnimationUtils.loadAnimation(NavigationActivity.this, R.anim.rotate_back));
                    previousUsersWrapper.setVisibility(View.GONE);
                    navMenuContainer.setVisibility(View.VISIBLE);
                }
            }
        });
        userContainer.setClickable(false);

        final int imageColor = getResources().getColor(R.color.navigationImageColor);

        final View accountNotification = findViewById(R.id.account_notifications);
        bindNavItems(accountNotification, imageColor, R.drawable.ic_cv_announcements_fill, R.string.accountNotifications);
        final View courses = findViewById(R.id.courses);
        bindNavItems(courses, imageColor, R.drawable.ic_cv_dashboard, R.string.dashboard);
        final View notifications = findViewById(R.id.notifications);
        bindNavItems(notifications, imageColor, R.drawable.ic_cv_notifications_fill, R.string.notifications);
        final View toDos = findViewById(R.id.todos);
        bindNavItems(toDos, imageColor, R.drawable.ic_cv_todo_fill, R.string.toDoList);
        final View inbox = findViewById(R.id.inbox);
        bindNavItems(inbox, imageColor, R.drawable.ic_cv_messages_fill, R.string.inbox);
        final View calendar = findViewById(R.id.calendar);
        bindNavItems(calendar, imageColor, R.drawable.ic_cv_calendar_fill, R.string.calendar);
        final View bookmarks = findViewById(R.id.bookmarks);
        bindNavItems(bookmarks, imageColor, R.drawable.ic_bookmark, R.string.bookmarks);
        final View grades = findViewById(R.id.grades);
        bindNavItems(grades, imageColor, R.drawable.ic_cv_grades_fill, R.string.grades);
        final View gauge = findViewById(R.id.gauge);
        bindNavItems(gauge, imageColor, R.drawable.ic_gauge, R.string.gauge);
        final View settings = findViewById(R.id.settings);
        bindNavItems(settings, imageColor, R.drawable.ic_settings_black_36dp, R.string.settings);
        final View help = findViewById(R.id.help);
        bindNavItems(help, imageColor, R.drawable.ic_help_black_36dp, R.string.help);

        //check to see if we should show the tutorial
        final ImageView pulseNavigationShortcut = (ImageView)findViewById(R.id.pulseNavigationShortcut);
        new TutorialUtils(NavigationActivity.this, ApplicationManager.getPrefs(getContext()), pulseNavigationShortcut, TutorialUtils.TYPE.NAVIGATION_SHORTCUTS)
                .setContent(getString(R.string.tutorial_tipNavigationShortcuts), getString(R.string.tutorial_tipNavigationShortcutsMessage))
                .build();

        userContainer.setClickable(true);

        findViewById(R.id.addAccount).setOnClickListener(this);

        mCourseShortcutDropdown.setImageDrawable(ColorUtils.colorIt(imageColor, mCourseShortcutDropdown.getDrawable()));
        boolean expandedState = ApplicationManager.getPrefs(getContext()).load(Const.NAVIGATION_SHORTCUTS_EXPANDED, false);
        if(expandedState) {
            mCourseShortcutDropdown.startAnimation(AnimationUtils.loadAnimation(NavigationActivity.this, R.anim.rotate));
        } else {
            mCourseShortcutDropdown.startAnimation(AnimationUtils.loadAnimation(NavigationActivity.this, R.anim.rotate_back));
        }

        mExpandRippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean expandedState = ApplicationManager.getPrefs(getContext()).load(Const.NAVIGATION_SHORTCUTS_EXPANDED, false);
                ApplicationManager.getPrefs(getContext()).save(Const.NAVIGATION_SHORTCUTS_EXPANDED, !expandedState);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    // Previous disabled to avoid UI flashing with cache and network calls. Enable here
                    mNavigationShortcutContainer.getLayoutTransition().setDuration(getContext().getResources().getInteger(R.integer.slide_up_down_duration));
                }
                if (!expandedState) {
                    mCourseShortcutDropdown.startAnimation(AnimationUtils.loadAnimation(NavigationActivity.this, R.anim.rotate));
                    mNavigationShortcutProgressBar.setVisibility(View.VISIBLE);
                    CourseManager.getAllFavoriteCourses(true, coursesCallback);
                } else {
                    mCourseShortcutDropdown.startAnimation(AnimationUtils.loadAnimation(NavigationActivity.this, R.anim.rotate_back));
                    mNavigationShortcutContainer.removeAllViewsInLayout();
                    mNavigationShortcutContainer.setVisibility(View.GONE);
                    mNavigationShortcutProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void gotEnrollments(List<Enrollment> enrollments) {

    }

    @Override
    public void gotLaunchDefinitions(@Nullable LaunchDefinition launchDefinition) {
        final View gauge = findViewById(R.id.gauge);
        if(gauge != null) {
            if(launchDefinition != null) {
                gauge.setTag(launchDefinition);
            }
            gauge.setVisibility(launchDefinition != null ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void updateCalendarStartDay() {
        //Restarts the calendarlist view fragment to update the changed start day of the week
        ParentFragment fragment = (ParentFragment)getSupportFragmentManager().findFragmentByTag(CalendarListViewFragment.class.getName());
        if(fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        addFragment(FragUtils.getFrag(CalendarListViewFragment.class, this), NavigationPosition.CALENDAR);
    }

    @Override
    public void gotNotifications(List<AccountNotification> accountNotificationsArray) {
        TextView unreadCountIndicator = (TextView) findViewById(R.id.navMenuUnreadCountAnnouncements);
        View unreadCountWrapper = findViewById(R.id.unreadCountWrapperAnnouncements);
        if(accountNotificationsArray.size() > 0) {
            findViewById(R.id.account_notifications).setVisibility(View.VISIBLE);
            accountNotifications = accountNotificationsArray;
            unreadCountIndicator.setText(NumberHelper.formatInt(accountNotificationsArray.size()));
            unreadCountWrapper.setVisibility(View.VISIBLE);
            ColorUtils.colorIt(getResources().getColor(R.color.navigationImageColor), unreadCountIndicator.getBackground());
        } else {
            findViewById(R.id.account_notifications).setVisibility(View.GONE);
            unreadCountWrapper.setVisibility(View.GONE);
        }
    }

    private void bindNavItems(View v, int imageColor, int iconResId, int textResId) {
        final ImageView image = (ImageView)v.findViewById(R.id.icon);
        final Drawable drawable = getResources().getDrawable(iconResId);
        ColorUtils.colorIt(imageColor, drawable);
        image.setImageDrawable(drawable);

        ((TextView) v.findViewById(R.id.text)).setText(textResId);
        v.setOnClickListener(this);
    }

    private void setPreferredLandingPage() {
        clearBackStack(null);
        routeToLandingPage(false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Actionbar
    ///////////////////////////////////////////////////////////////////////////


    @Nullable
    @Override
    public ActionBar getSupportActionBar() {
        return super.getSupportActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if(item.getItemId() == R.id.bookmark) {
            if(!APIHelper.hasNetworkConnection()) {
                Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                return true;
            }
            addBookmark();
            return true;
        } else if(item.getItemId() == android.R.id.home) {
            //if we hit the x while we're on a detail fragment, we always want to close the top fragment
            //and not have it trigger an actual "back press"
            final Fragment topFragment = getTopFragment();
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                if(topFragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(topFragment).commit();
                }
                super.onBackPressed();
            } else if(topFragment == null) {
                super.onBackPressed();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (v.getId()) {
                    case R.id.profileLayout:
                        addFragment(FragUtils.getFrag(ProfileFragment.class, NavigationActivity.this), NavigationPosition.PROFILE);
                        break;
                    case R.id.account_notifications:
                        AccountNotificationDialog.show(NavigationActivity.this, new ArrayList<>(accountNotifications));
                        break;
                    case R.id.courses:
                        clearBackStack(CourseGridFragment.class);
                        addFragment(FragUtils.getFrag(CourseGridFragment.class, NavigationActivity.this), NavigationPosition.COURSES);
                        break;
                    case R.id.notifications:
                        clearBackStack(NotificationListFragment.class);
                        addFragment(FragUtils.getFrag(NotificationListFragment.class, NavigationActivity.this), NavigationPosition.NOTIFICATIONS);
                        break;
                    case R.id.todos:
                        clearBackStack(ToDoListFragment.class);
                        addFragment(FragUtils.getFrag(ToDoListFragment.class, NavigationActivity.this), NavigationPosition.TODO);
                        break;
                    case R.id.inbox:
                        clearBackStack(InboxFragment.class);
                        addFragment(FragUtils.getFrag(InboxFragment.class, NavigationActivity.this), NavigationPosition.INBOX);
                        break;
                    case R.id.calendar:
                        clearBackStack(CalendarListViewFragment.class);
                        addFragment(FragUtils.getFrag(CalendarListViewFragment.class, NavigationActivity.this), NavigationPosition.CALENDAR);
                        break;
                    case R.id.gauge:
                        LaunchDefinition launchDefinition = (LaunchDefinition) v.getTag();
                        if(launchDefinition != null) {
                            String ltiUrl = launchDefinition.placements.globalNavigation.url;
                            Bundle bundle = LTIWebViewFragment.createBundle(ApiPrefs.getUser(), ltiUrl, getString(R.string.gauge), true);
                            addFragment(FragUtils.getFrag(LTIWebViewFragment.class, bundle));
                        }
                        break;
                    case R.id.logout:
                        logoutWarning();
                        break;
                    case R.id.settings:
                        Analytics.trackAppFlow(NavigationActivity.this, SettingsActivity.class);
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        break;
                    case R.id.help:
                        Analytics.trackAppFlow(NavigationActivity.this, HelpDialogStyled.class);
                        HelpDialogStyled.show(NavigationActivity.this, hasNonTeacherEnrollment);
                        break;
                    case R.id.addAccount:
                        new SwitchUsersAsyncTask().execute();
                        break;
                    case R.id.bookmarks:
                        addFragment(FragUtils.getFrag(BookmarksFragment.class, NavigationActivity.this), NavigationPosition.BOOKMARKS);
                        break;
                    case R.id.grades:
                        addFragment(FragUtils.getFrag(GradesGridFragment.class, NavigationActivity.this), NavigationPosition.GRADES);
                        break;
                }
            }
        }, 230);

        closeNavigationDrawer();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Parent Activity Overrides
    ///////////////////////////////////////////////////////////////////////////

    public static Intent createIntent(Context context) {
        return new Intent(context, NavigationActivity.class);
    }

    public static Intent createIntent(Context context, Bundle extras) {
        Intent intent = new Intent(context, NavigationActivity.class);
        intent.putExtra(Const.EXTRAS, extras);
        return intent;
    }

    public static Intent createIntent(Context context, String message, int messageType) {
        Intent intent = createIntent(context);
        intent.putExtra(Const.MESSAGE, message);
        intent.putExtra(Const.MESSAGE_TYPE, messageType);
        return intent;
    }

    @Override
    public void loadData() {
        Fragment fragment = getTopFragment();
        if(fragment instanceof ParentFragment){
            ((ParentFragment)fragment).loadData();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public static Class getStartActivityClass() {
        return NavigationActivity.class;
    }

    @Override
    public boolean isNavigationDrawerOpen() {
        return isDrawerOpen();
    }

    @Override
    public void requestNavigationDrawerClose() {
        closeNavigationDrawer();
    }

    @Override
    public void onProfileChangedCallback() {
        getUserSelf(false, false);
    }


    @Override
    public void onEventSaved(ScheduleItem item, boolean delete) {
        CalendarListViewFragment listFragment = (CalendarListViewFragment)getSupportFragmentManager().findFragmentByTag(CalendarListViewFragment.class.getName());
        if(listFragment != null){
            listFragment.refreshCalendar(item, delete);
        }
    }

    @Override
    public void updateMessageState(Conversation conversation, Conversation.WorkflowState state) {
        InboxFragment listFragment = (InboxFragment)getSupportFragmentManager().findFragmentByTag(InboxFragment.class.getName());
        if(listFragment != null){
            listFragment.setConversationState(conversation, state);
        }
    }

    @Override
    public void removeMessage(Conversation conversation) {
        InboxFragment listFragment = (InboxFragment)getSupportFragmentManager().findFragmentByTag(InboxFragment.class.getName());
        if(listFragment != null){
            listFragment.updateMessages(conversation);
        }
    }

    @Override
    public void updateUnread(long topicID, int unreadCount) {
        //inform DiscussionListFragment of updated unreadCount

        DiscussionListFragment listFragment = (DiscussionListFragment)getSupportFragmentManager().findFragmentByTag(DiscussionListFragment.class.getName());
        // Could be an AnnouncmentListFragment so try that
        if (listFragment == null) {
            listFragment = (DiscussionListFragment) getSupportFragmentManager().findFragmentByTag(AnnouncementListFragment.class.getName());
        }

        if(listFragment != null){
            listFragment.setUpdatedUnreadCount(topicID, unreadCount);
        }
    }

    @Override
    public void onUserCallbackFinished(User user) {
        if (user == null) { return; }

        mUserName.setText(user.getShortName());
        mUserEmail.setText(user.getPrimaryEmail());

        if (mUserProfilePic == null) {
            return;
        }

        ProfileUtils.configureAvatarView(this, user, mUserProfilePic);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adding/Removing Fragments
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void popCurrentFragment() {
        try {
            getSupportFragmentManager().popBackStack();
        } catch (Exception e){
            Logger.e("Unable to pop curent fragment." +e);
        }
    }

    @Override
    public void addFragment(ParentFragment fragment) {
        addFragment(fragment, false);
    }

    @Override
    public void addFragment(ParentFragment fragment, boolean ignoreDebounce) {
        addFragmentToSomething(fragment, R.anim.fade_in_quick, R.anim.fade_out_quick, null, null, ignoreDebounce);
    }

    @Override
    public void addFragment(ParentFragment fragment, int inAnimation, int outAnimation) {
        addFragmentToSomething(fragment, inAnimation, outAnimation, null, null, false);
    }
    @Override
    public void addFragment(ParentFragment fragment, int transitionId, View sharedElement) {
        addFragmentToSomething(fragment, R.anim.fade_in_quick, R.anim.fade_out_quick, transitionId, sharedElement, false);
    }

    @Override
    public void addFragment(ParentFragment fragment, NavigationPosition selectedPosition) {
        mCurrentSelectedPosition = selectedPosition;
        addFragmentToSomething(fragment, R.anim.fade_in_quick, R.anim.fade_out_quick, null, null, false);
    }

    @Override
    public void addFragment(ParentFragment fragment, NavigationPosition selectedPosition, boolean ignoreDebounce) {
        mCurrentSelectedPosition = selectedPosition;
        addFragmentToSomething(fragment, R.anim.fade_in_quick, R.anim.fade_out_quick, null, null, ignoreDebounce);
    }

    private void addFragmentToSomething(final ParentFragment fragment, final int inAnimation, final int outAnimation, @Nullable final Integer transitionId, @Nullable final View sharedElement, boolean ignoreDebounce) {
        if(fragment == null) {
            Logger.e("FAILED TO addFragmentToSomething with null fragment...");
            return;
        } else if(!ignoreDebounce && !addFragmentEnabled){
            Logger.e("FAILED TO addFragmentToSomething. Too many fragment transactions...");
            return;
        }

        setTransactionDelay();

        try {

            final ParentFragment.FRAGMENT_PLACEMENT placement = fragment.getFragmentPlacement(getContext());

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            setSharedElement(ft, sharedElement);
            switch (placement) {
                case MASTER: {
                    //Check if the fragment is from the navigation drawer and if it's already on top.

                    //If the navigation drawer is open...
                    //AND
                    //The current fragment is NOT a course fragment
                    //AND
                    //if the current fragment is the same as the fragment beind added

                    final ParentFragment currentFragment = (ParentFragment) getSupportFragmentManager().findFragmentById(R.id.fullscreen);
                    if (isNavigationDrawerOpen() &&
                            currentFragment != null &&
                            !currentFragment.navigationContextIsCourse() &&
                            currentFragment.getClass().isAssignableFrom(fragment.getClass())) {
                        closeNavigationDrawer();
                        return;
                    }

                    ft.setCustomAnimations(inAnimation, outAnimation);
                    ft.add(R.id.fullscreen, fragment, fragment.getClass().getName());
                    ft.addToBackStack(fragment.getClass().getName());
                    ft.commitAllowingStateLoss();
                    break;
                }
                case DETAIL: {
                    ft.setCustomAnimations(inAnimation, outAnimation);
                    ft.add(R.id.fullscreen, fragment, fragment.getClass().getName());
                    ft.addToBackStack(fragment.getClass().getName());
                    ft.commitAllowingStateLoss();
                    break;
                }
                case DIALOG: {
                    ft.addToBackStack(fragment.getClass().getName());
                    fragment.show(ft, fragment.getClass().getName());
                    break;
                }
            }
            //Tracks the flow of screens in Google Analytics
            Analytics.trackAppFlow(NavigationActivity.this, fragment);
        } catch (IllegalStateException e) {
            Logger.e("Could not commit fragment transaction: " + e);
        }
    }

    private void setTransactionDelay() {
        addFragmentEnabled = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addFragmentEnabled = true;
            }
        }, FRAGMENT_TRANSACTION_DELAY_TIME);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setSharedElement(FragmentTransaction ft, View sharedElement){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && sharedElement != null) {
            ft.addSharedElement(sharedElement, sharedElement.getTransitionName());
        }
    }

    private void logoutWarning(){
        new AlertDialog.Builder(this)
                .setMessage(R.string.logout_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new LogoutAsyncTask().execute();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }

    private void getUnreadCount() {
        UnreadCountManager.getUnreadConversationCount(new StatusCallback<UnreadConversationCount>() {
            @Override
            public void onResponse(retrofit2.Response<UnreadConversationCount> response, LinkHeaders linkHeaders, ApiType type) {
                UnreadConversationCount unreadConversationCount = response.body();
                if (unreadConversationCount.getUnreadCount() != null) {
                    TextView unreadCountIndicator = (TextView) findViewById(R.id.navMenuUnreadCount);
                    View unreadCountWrapper = findViewById(R.id.unreadCountWrapperMessages);
                    if (unreadConversationCount.getUnreadCount().equals("0")) {
                        unreadCountWrapper.setVisibility(View.GONE);
                    } else {
                        unreadCountIndicator.setText(unreadConversationCount.getUnreadCount());
                        ColorUtils.colorIt(getResources().getColor(R.color.navigationImageColor), unreadCountIndicator.getBackground());
                        unreadCountWrapper.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // BACK STACK
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {

        int entryCount = 0;
        boolean isDrawerOpen = isNavigationDrawerOpen();
        entryCount = getSupportFragmentManager().getBackStackEntryCount();

        //If we have stuff on the backstack and the drawer is open, close it.
        if(entryCount > 1 && isDrawerOpen) {
            closeNavigationDrawer();
            return;
        }
        //Opens the navigation drawer if only one fragment is on the stack
        else if(entryCount == 1 && !isDrawerOpen) {
            openNavigationDrawer();
            return;
        }
        //Exits if we only have one fragment and the navigation drawer is open
        else if(entryCount == 1) {
            finish();
            return;
        }

        final Fragment topFragment = getTopFragment();
        if(topFragment instanceof ParentFragment) {
            if(!((ParentFragment)topFragment).handleBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Fragment getTopFragment() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            final List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if(!fragments.isEmpty()) {
                return fragments.get(getSupportFragmentManager().getBackStackEntryCount() - 1);
            }
        }
        return null;
    }

    @Override
    public Fragment getPeekingFragment() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 1) {
            final List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if(!fragments.isEmpty()) {
                return fragments.get(getSupportFragmentManager().getBackStackEntryCount() - 2);
            }
        }
        return null;
    }

    @Override
    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fullscreen);
    }

    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            updateActionbar();
        }
    };

    private void clearBackStack(Class cls) {
        Fragment fragment = getTopFragment();
        if(fragment != null && cls != null && fragment.getClass().isAssignableFrom(cls)) {
            return;
        }
        try {
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e) {
            Logger.e("COULD NOT CLEAR BACKSTACK AND EXECUTE PENDING TRANSACTIONS. " + e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Course Navigation Tabs
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public CourseNavigationAdapter getCourseNavigationAdapter() {
        if (mCourseNavigationAdapter == null) {
            mCourseNavigationAdapter = new CourseNavigationAdapter(getContext());
        }
        return mCourseNavigationAdapter;
    }

    public void setupActionbarSpinnerForCourse(
            final ActionBar actionBar,
            final CourseNavigationAdapter adapter,
            final CanvasContext canvasContext,
            final WeakReference<ParentFragment> parentFragment) {

        mSpinnerContainer = LayoutInflater.from(getSupportActionBar().getThemedContext()).inflate(R.layout.actionbar_course_items_spinner, null);
        mActionbarSpinner = (ActionbarCourseSpinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
        SpinnerInteractionListener spinnerInteractionListener = new SpinnerInteractionListener(new SpinnerInteractionListener.ItemSelection() {
            @Override
            public void itemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mSelectedTabPosition != position) {
                    mSelectedTabPosition = position;
                    final Tab tab = adapter.getTab(position);
                    addFragment(TabHelper.getFragmentByTab(tab, canvasContext));
                    if (isNavigationDrawerOpen()) {
                        requestNavigationDrawerClose();
                    }
                }
            }
        });
        mActionbarSpinner.setOnTouchListener(spinnerInteractionListener);
        mActionbarSpinner.setOnItemSelectedListener(spinnerInteractionListener);
        mActionbarSpinner.setAdapter(adapter);
        actionBar.setCustomView(mSpinnerContainer);
        adapter.loadWithCanvasContext(canvasContext, new CourseNavigationAdapter.OnTabsLoaded() {
            @Override
            public void onTabsLoadFinished() {
                updateTabNavigationUI(parentFragment.get());
            }
        });
        updateTabNavigationUI(parentFragment.get());
    }

    public void updateTabNavigationUI(ParentFragment parentFragment) {

        //Responsible for updating the UI of the tab to be correct
        CourseNavigationAdapter adapter = getCourseNavigationAdapter();
        if(adapter.getCount() > 0) {
            final Tab taggedTab = parentFragment.getTaggedTab();
            if (taggedTab != null) {
                mSelectedTabPosition = adapter.getTabPosition(taggedTab.getTabId());
                mActionbarSpinner.setSelection(mSelectedTabPosition);
            } else {
                if(parentFragment.getTabId() != null) {
                    //We do not have the history of the tab so we make a best guess of where the user is.
                    mSelectedTabPosition = adapter.getTabPosition(parentFragment.getTabId());
                    mActionbarSpinner.setSelection(mSelectedTabPosition);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Zendesk Dialog Result
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onTicketPost() {
        dismissHelpDialog();
        Toast.makeText(getContext(), R.string.zendesk_feedbackThankyou, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTicketError() {
        dismissHelpDialog();
        Toast.makeText(getContext(), R.string.errorOccurred, Toast.LENGTH_LONG).show();
    }

    private void dismissHelpDialog() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(HelpDialogStyled.TAG);
        if(fragment instanceof HelpDialogStyled) {
            HelpDialogStyled dialog = (HelpDialogStyled)fragment;
            try {
                dialog.dismiss();
            } catch (IllegalStateException e) {
                Logger.e("Committing a transaction after activities saved state was called: " + e);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // EXTERNAL AND INTERNAL ROUTING
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void routeFragment(ParentFragment fragment, NavigationPosition position) {
        addFragment(fragment, position, true);
    }

    @Override
    protected void routeFragment(ParentFragment fragment) {
        addFragment(fragment, true);
    }

    @Override
    protected void routeToLandingPage(boolean ignoreDebounce) {
        int landingPage = ApplicationManager.getPrefs(getContext()).load(ApplicationSettingsFragment.LANDING_PAGE, 0);
        switch (landingPage) {
            case 0://Courses
                addFragment(FragUtils.getFrag(CourseGridFragment.class, this), NavigationPosition.COURSES, ignoreDebounce);
                break;
            case 1://Notifications
                addFragment(FragUtils.getFrag(NotificationListFragment.class, this), NavigationPosition.NOTIFICATIONS, ignoreDebounce);
                break;
            case 2://TO-DO
                addFragment(FragUtils.getFrag(ToDoListFragment.class, this), NavigationPosition.TODO, ignoreDebounce);
                break;
            case 3://Inbox
                addFragment(FragUtils.getFrag(InboxFragment.class, this), NavigationPosition.INBOX, ignoreDebounce);
                break;
            case 4://Calendar
                addFragment(FragUtils.getFrag(CalendarListViewFragment.class, this), NavigationPosition.CALENDAR, ignoreDebounce);
                break;
            case 5://Bookmarks
                addFragment(FragUtils.getFrag(BookmarksFragment.class, this), NavigationPosition.BOOKMARKS, ignoreDebounce);
                break;
            case 6://Grades
                addFragment(FragUtils.getFrag(GradesGridFragment.class, this), NavigationPosition.GRADES, ignoreDebounce);
                break;
            default://Courses
                addFragment(FragUtils.getFrag(CourseGridFragment.class, this), NavigationPosition.COURSES, ignoreDebounce);
                break;
        }
    }

    @Override
    protected int existingFragmentCount() {
        return getSupportFragmentManager().getBackStackEntryCount();
    }

    @Override
    public void redrawScreen() {
        if(mDrawerLayout != null) {
            mDrawerLayout.invalidate();
        }
    }

    @Override
    public void invalidateUnreadCount() {
        getUnreadCount();
    }

    @Override
    public void invalidateAnnouncementCount() {
        getAccountNotifications(false, false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Actionbar & Toolbar
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void setActionBarStatusBarColors(int actionBarColor, int statusBarColor) {
        setActionbarColor(actionBarColor);
        setStatusBarColor(statusBarColor);
    }

    public void setActionbarColor(int actionBarColor) {
        if(getSupportActionBar() != null) {
            ColorDrawable colorDrawable = new ColorDrawable(actionBarColor);
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int statusBarColor) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            getWindow().setStatusBarColor(statusBarColor);
            if(mDrawerLayout != null) {
                mDrawerLayout.setStatusBarBackgroundColor(statusBarColor);
            }
        }
    }

    private void updateActionbar() {
        List<Fragment> fragments = null;
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
             fragments = getSupportFragmentManager().getFragments();
        }

        if(fragments == null || fragments.isEmpty()) {
            return;
        }

        if(BuildConfig.IS_DEBUG) {
            Log.d("backstack", "****************** BACKSTACK *******************");
            for(Fragment fragment : fragments) {
                if(fragment != null) {
                    Log.d("backstack", "   ***> " + fragment.getClass().getSimpleName());
                }
            }
        }

        final Fragment fragment = getCurrentFragment();

        if (fragment instanceof ParentFragment) {
            final ParentFragment parentFragment = (ParentFragment) fragment;
            final CanvasContext canvasContext = parentFragment.getCanvasContext();
            final boolean isNavigationContextCourse = parentFragment.navigationContextIsCourse();
            final ParentFragment.FRAGMENT_PLACEMENT placement = parentFragment.getFragmentPlacement(getContext());

            switch (placement) {
                case MASTER:
                    setupActionbarForMaster(new WeakReference<>(parentFragment), canvasContext, isNavigationContextCourse);
                    break;
                case DETAIL:
                    setupActionbarForDetail(parentFragment, canvasContext, isNavigationContextCourse);
                    break;
                case DIALOG:
                    setupActionbarForDialog(parentFragment, canvasContext, isNavigationContextCourse);
                    break;
            }
        }
    }

    /**
     * Sets up the actionbar for master fragments (usually list or grid fragments)
     * @param parentFragment The fragment being added
     * @param canvasContext The canvas context of the fragment being added
     * @param isNavigationContextCourse If the fragment belongs to a single course or represents multiple courses.
     */
    private void setupActionbarForMaster(WeakReference<ParentFragment> parentFragment, CanvasContext canvasContext, boolean isNavigationContextCourse) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowCustomEnabled(isNavigationContextCourse);
        actionBar.setDisplayShowTitleEnabled(!isNavigationContextCourse);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (isNavigationContextCourse) {
            setupActionbarSpinnerForCourse(actionBar, getCourseNavigationAdapter(), canvasContext, parentFragment);
        } else {
            if (ApiPrefs.isMasquerading()) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.masqueradeRed)));
            }
            actionBar.setSubtitle(null);
            actionBar.setTitle(parentFragment.get().getFragmentTitle());
        }
        updateActionbarColors(isNavigationContextCourse, canvasContext);
        parentFragment.get().onFragmentActionbarSetupComplete(parentFragment.get().getFragmentPlacement(getContext()));
    }

    /**
     * Sets up the actionbar for a detail fragment type. Dialog is only used on tablets, behaves as details if it is a phone.
     * @param parentFragment The fragment being added
     * @param canvasContext The canvas context of the fragment being added
     * @param isNavigationContextCourse If the fragment belongs to a single course or represents multiple courses.
     */
    private void setupActionbarForDetail(ParentFragment parentFragment, CanvasContext canvasContext, boolean isNavigationContextCourse) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_content_close);

        updateActionbarColors(isNavigationContextCourse, canvasContext);
        parentFragment.onFragmentActionbarSetupComplete(parentFragment.getFragmentPlacement(getContext()));
    }

    /**
     * Sets up the actionbar for a dialog fragment type. Dialog is only used on tablets, behaves as details if it is a phone.
     * @param parentFragment The fragment being added
     * @param canvasContext The canvas context of the fragment being added
     * @param isNavigationContextCourse If the fragment belongs to a single course or represents multiple courses.
     */
    private void setupActionbarForDialog(ParentFragment parentFragment, CanvasContext canvasContext, boolean isNavigationContextCourse) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);

        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_content_close);

        updateActionbarColors(isNavigationContextCourse, canvasContext);
        parentFragment.onFragmentActionbarSetupComplete(parentFragment.getFragmentPlacement(getContext()));
    }

    private void updateActionbarColors(boolean navigationContextIsCourse, CanvasContext...canvasContext) {
        if(!navigationContextIsCourse) {
            //set actionbar color based on non-course context, which is gray.
            mLastActionbarColor = getResources().getColor(R.color.defaultPrimary);
            setActionBarStatusBarColors(mLastActionbarColor, mLastActionbarColor);
        } else {
            if(canvasContext != null && canvasContext.length > 0) {
                //set actionbar color with canvas context
                mLastActionbarColor = CanvasContextColor.getCachedColor(NavigationActivity.this, canvasContext[0]);
                setActionBarStatusBarColors(mLastActionbarColor, mLastActionbarColor);
            } else {
                if(mLastActionbarColor != Integer.MAX_VALUE) {
                    //rotation restored actionbar color
                    setActionbarColor(mLastActionbarColor);
                    setStatusBarColor(mLastActionbarColor);
                } else {
                    //could not determine actionbar color
                    mLastActionbarColor = getResources().getColor(R.color.defaultPrimary);
                    setActionBarStatusBarColors(mLastActionbarColor, mLastActionbarColor);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // BOOKMARKS
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void addBookmark() {
        final Fragment back = getPeekingFragment();
        final Fragment front = getTopFragment();

        ParentFragment peeking = null;
        ParentFragment top = null;
        CanvasContext canvasContext = null;
        String url = null;
        String label = null;

        if(back instanceof ParentFragment) {
            peeking = (ParentFragment)back;
        }

        if(front instanceof ParentFragment) {
            top = (ParentFragment)front;
        }

        //A to-do route doesn't actually exist so we will create a single fragment route by making the peeking null
        if(peeking instanceof ToDoListFragment) {
            peeking = null;
        }

        if(peeking != null && top != null &&
                peeking.getFragmentPlacement(getContext()) == ParentFragment.FRAGMENT_PLACEMENT.MASTER &&
                       (top.getFragmentPlacement(getContext()) == ParentFragment.FRAGMENT_PLACEMENT.DETAIL ||
                        top.getFragmentPlacement(getContext()) == ParentFragment.FRAGMENT_PLACEMENT.DIALOG)) {
            //Master & Detail
            canvasContext = top.getCanvasContext();
            ActionBar fragmentActionbar = top.getSupportActionBar();
            if(fragmentActionbar != null && fragmentActionbar.getTitle() != null) {
                label = fragmentActionbar.getTitle().toString();
            }
            if (canvasContext instanceof Course || canvasContext instanceof Group) {
                url = RouterUtils.createUrl(getContext(), canvasContext.getType(), peeking.getClass(), top.getClass(), top.getParamForBookmark(), top.getQueryParamForBookmark());
                Analytics.trackBookmarkSelected(NavigationActivity.this, peeking.getClass().getSimpleName() + " " + top.getClass().getSimpleName());
            }
        } else if(top != null) {
            //Master or Detail
            canvasContext = top.getCanvasContext();
            ActionBar fragmentActionbar = top.getSupportActionBar();
            if(fragmentActionbar != null && fragmentActionbar.getTitle() != null) {
                label = fragmentActionbar.getTitle().toString();
            }
            if (canvasContext instanceof Course || canvasContext instanceof Group) {
                url = RouterUtils.createUrl(getContext(), canvasContext.getType(), top.getClass(), top.getParamForBookmark());
                Analytics.trackBookmarkSelected(NavigationActivity.this, top.getClass().getSimpleName());
            }
        }

        //Log to GA
        Analytics.trackButtonPressed(NavigationActivity.this, "Add bookmark to fragment", null);

        showBookmarkCreationDialog(canvasContext, url, label);
    }

    private void showBookmarkCreationDialog(final CanvasContext canvasContext, final String url, final String label) {
        if(!TextUtils.isEmpty(url) & canvasContext != null) {
            final int color = CanvasContextColor.getCachedColorForUrl(getContext(), url);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.addBookmark);
            builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mDialogEditText != null) {
                        String name = mDialogEditText.getText().toString();
                        if (!TextUtils.isEmpty(name)) {
                            BookmarkManager.createBookmark(new Bookmark(name, url, 0), new StatusCallback<Bookmark>() {
                                @Override
                                public void onResponse(retrofit2.Response<Bookmark> response, LinkHeaders linkHeaders, ApiType type, int code) {
                                    if (code == 200) {
                                        Analytics.trackBookmarkCreated(NavigationActivity.this);
                                        Toast.makeText(getContext(), R.string.bookmarkAddedSuccess, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFail(Call<Bookmark> response, Throwable error, int code) {
                                    Toast.makeText(getContext(), R.string.bookmarkAddedFailure, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), R.string.bookmarkTitleRequired, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.bookmarkTitleRequired, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.setView(R.layout.dialog_bookmark);
            builder.setCancelable(true);

            AlertDialog dialog = builder.create();
            if(dialog != null) {
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                mDialogEditText = (EditText)dialog.findViewById(R.id.bookmarkEditText);
                if(mDialogEditText != null && !TextUtils.isEmpty(label)) {
                    mDialogEditText.setText(label);
                    mDialogEditText.setSelection(mDialogEditText.getText().length());
                }
            }
        } else {
            Toast.makeText(getContext(), R.string.bookmarkAddedFailure, Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver mLocaleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())) {
                //Locale changed, finish the app so it starts fresh when they come back.
                //We do this to stop a Toolbar bug which causes the toolbar to become unresponsive when a locale is changed.
                finish();
            }
        }
    };
}
