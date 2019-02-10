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

import com.ebuki.portal.R;
import com.ebuki.portal.delegate.Navigation;
import com.ebuki.portal.util.FragUtils;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hosopy.actioncable.ActionCable;
import com.hosopy.actioncable.ActionCableException;
import com.hosopy.actioncable.Channel;
import com.hosopy.actioncable.Consumer;
import com.hosopy.actioncable.Subscription;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.CalendarEventManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.managers.ToDoManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.ToDo;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import org.json.JSONObject;

import android.support.design.widget.Snackbar;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends ParentFragment {

    private View mRootView;
    public boolean ignoreDebounce = false;

    private StatusCallback<List<Group>> mGroupsCallback;
    private StatusCallback<List<Course>> mCoursesCallback;
    private StatusCallback<List<ToDo>> mTodoCallback;
    private CanvasContext mCanvasContext;

    static String TAG ="::eSOCKET";

    URI uri = null;

    boolean isConnected = false;

    private Consumer consumer;
    private Subscription subscription;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.MASTER;
    }

    @Override
    public String getFragmentTitle() {
        if (isAdded()) {
            return getString(R.string.dashboard);
        } else {
            return "";
        }
    }

    @Override
    public boolean navigationContextIsCourse() {
        return false;
    }


    @Override
    public void loadData() {

        mCanvasContext =  getCanvasContext();

//        Log.i("::DATA", "before getCourses");


//        CourseManager.getCourses(true, mCoursesCallback);
//        Log.i("::DATA", "before getAllGroups");
//        GroupManager.getAllGroups(mGroupsCallback, true);


        Log.i("::DATA", "before getTodos");
//        ToDoManager.getTodos(mCanvasContext, mTodoCallback, true);

        mTodoCallback = new StatusCallback<List<ToDo>>() {
            @Override
            public void onResponse(retrofit2.Response<List<ToDo>> response, LinkHeaders linkHeaders, ApiType type) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setupConnection();
        loadData();

        mRootView = getLayoutInflater().inflate(R.layout.dashboard_fragment, container, false);

        final ImageView ivHomework = mRootView.findViewById(R.id.ivHomework);
        ivHomework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Navigation navigation = getNavigation();
                if (navigation != null) {
                    navigation.addFragment(FragUtils.getFrag(ToDoListFragment.class, getActivity()), Navigation.NavigationPosition.TODO, ignoreDebounce);
                    // navigation.addFragment(FragUtils.getFrag(ToDoListFragment.class, bundle));
                }
            }
        });

        final ImageView ivTextbooks = mRootView.findViewById(R.id.ivTextbooks);
        ivTextbooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), getString(R.string.textbooksSelected), Toast.LENGTH_SHORT).show();
                // String packageName="net.nightwhistler.pageturner";
                // String packageName="com.skytree.skyreader";
                String packageName="com.gitden.epub.reader.app";
                launchApp(packageName);
            }
        });

        final ImageView ivCalendar = mRootView.findViewById(R.id.ivCalendar);
        ivCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName="ws.xsoh.etar";
                launchApp(packageName);

            }
        });

        final ImageView ivMynotes = mRootView.findViewById(R.id.ivMynotes);
        ivMynotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "My notes selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="it.feio.android.omninotes.foss";
                launchApp(packageName);

            }
        });

        final ImageView ivCamera = mRootView.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "Camera selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="com.mediatek.camera";
                launchApp(packageName);
            }
        });

        final ImageView ivCalculator = mRootView.findViewById(R.id.ivCalculator);
        ivCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "Calculator selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="com.android.calculator2";
                launchApp(packageName);
            }
        });

        final ImageView ivSubjects = mRootView.findViewById(R.id.ivSubjects);
        ivSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "Subjects selected, well done :-)", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                Navigation navigation = getNavigation();
                if (navigation != null) {
                    navigation.addFragment(FragUtils.getFrag(CourseGridFragment.class, bundle));
                }
//                String packageName="org.wikipedia";
//                launchApp(packageName);

            }

        });

        final ImageView ivAnnouncements = mRootView.findViewById(R.id.ivAnnouncements);
        ivAnnouncements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "Announcements selected, well done :-)", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                Navigation navigation = getNavigation();
                if (navigation != null)
                {
                    navigation.addFragment(FragUtils.getFrag(NotificationListFragment.class, getActivity()), Navigation.NavigationPosition.NOTIFICATIONS, ignoreDebounce);
                    // navigation.addFragment(FragUtils.getFrag(NotificationListFragment.class, bundle));
                }
//                String packageName="it.feio.android.omninotes.foss";
//                launchApp(packageName);

            }
        });

        final ImageView ivMyStuff = mRootView.findViewById(R.id.ivMyStuff);
        ivMyStuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName="com.android.gallery3d";
                // one tablet doesn't have gallery3d
                // String packageName="com.google.android.apps.photos";
                launchApp(packageName);
            }
        });

        final ImageView ivWikipedia = mRootView.findViewById(R.id.ivWikipedia);
        ivWikipedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                 Toast.makeText(getActivity(), "Wikipedia selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="org.wikipedia";
                launchApp(packageName);
            }

        });

        final ImageView ivOffice = mRootView.findViewById(R.id.ivOffice);
        ivOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getActivity(), "Office selected, well done :-)", Toast.LENGTH_SHORT).show();
                String packageName="cn.wps.moffice_eng";
                launchApp(packageName);
            }
        });

        final ImageView ivPlaystore = mRootView.findViewById(R.id.ivPlaystore);
        ivPlaystore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPlayStore();
            }
        });

        return mRootView;
    }


    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        setupTitle(getString(R.string.dashboard));
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }


    private void setupConnection(){

        try {
//            uri = new URI("ws://sockets.nxtstepdsgn.com/cable");
            uri = new URI("ws://ebuki.nxtstepdsgn.com/cable");
            Log.i(TAG, uri.toString());
        }catch (Exception ignored){
        }

        Consumer.Options options = new Consumer.Options();

        options.reconnection = true;

        Map<String, String> headers = new HashMap<>();
        headers.put("token", "tokenkey");
        options.headers = headers;

        Log.d(TAG, "Setting up consumer");
        consumer = ActionCable.createConsumer(uri, options);

        Log.d(TAG, "Setting up channel");
        Channel wsChannel = new Channel("ClassChannel");

        Log.d(TAG, "Setting up subscription");
        subscription = consumer.getSubscriptions().create(wsChannel);

        Log.d(TAG, "connecting consumer");
        consumer.connect();

        Log.d(TAG, "adding listeners");

        subscription
                .onConnected(new Subscription.ConnectedCallback() {
                    @Override
                    public void call() {
                        Log.i(TAG, "onConnected");
                        altStatus(0);
                    }
                }).onRejected(new Subscription.RejectedCallback() {
            @Override
            public void call() {
                Log.i(TAG, "RejectedCallback");
                altStatus(3);
            }
        }).onReceived(new Subscription.ReceivedCallback() {
            @Override
            public void call(JsonElement data) {
                Log.i(TAG, "onReceived");
//                pre(data.toString());
                Log.i(TAG, data.toString());

                Snackbar.make(getView(), "Homework update received!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();

                altStatus(4);
            }
        }).onDisconnected(new Subscription.DisconnectedCallback() {
            @Override
            public void call() {
                Log.i(TAG, "onDisconnected");
                altStatus(1);
            }
        }).onFailed(new Subscription.FailedCallback() {
            @Override
            public void call(ActionCableException e) {
                Log.i(TAG, "onFailed");
                Log.i(TAG, e.getMessage());
            }
        });

    }


    public void altStatus(final int i){
        isConnected = false;
        switch (i) {
            case 0:
                isConnected = true;
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                isConnected = true;
                break;
        }

    }

    public void pre(String result){
        try {

            JSONObject jsonObj = new JSONObject(result);
            String texto = jsonObj.getString("message");
            String user = jsonObj.getString("user");

            JSONObject jsonObj2 = new JSONObject(texto);
            String mess = jsonObj2.getString("message");
        }catch (Exception e){
        }
    }

    // Custom method to launch an app
    public void launchPlayStore() {

        Intent intent = new Intent(Intent.ACTION_MAIN);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        if (intent != null) {
            startActivity(intent);
        }

    }

    // Custom method to launch an app
    public void launchApp(String packageName) {

        PackageManager pm = getActivity().getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);

//
//        List<ResolveInfo> activities = pm.queryIntentActivities(intent,
//                PackageManager.MATCH_DEFAULT_ONLY);
//
//        boolean isIntentSafe = activities.size() > 0;


        try {

            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            intent = pm.getLaunchIntentForPackage(packageName);

            if (intent == null) {
                throw new PackageManager.NameNotFoundException();
            } else {
                startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getActivity(), "Application not found", Toast.LENGTH_LONG).show();
            Log.e("Launch", e.getMessage());
        }
    }


//    private static boolean isIntentAvailable(Context context, String action) {
//        final PackageManager packageManager = context.getPackageManager();
//        final Intent intent = new Intent(action);
//        List<ResolveInfo> list =
//                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        return list.size() > 0;
//    }

}
















