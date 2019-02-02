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
import com.ebuki.portal.util.ConnectionDetector;

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

import java.net.URI;

import com.instructure.canvasapi2.models.CanvasContext;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.ws.RealWebSocket;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.ByteString;

import java.util.concurrent.TimeUnit;


public class DashboardFragment extends ParentFragment {

    private CanvasContext canvasContext;
    private View mRootView;
    public boolean ignoreDebounce = false;
    private OkHttpClient client;

    URI uri = null;
    String wsUri = "ws://sockets.nxtstepdsgn.com/cable";

    public enum State {
        CLOSED, CLOSING, CONNECT_ERROR, RECONNECT_ATTEMPT, RECONNECTING, OPENING, OPEN
    }



    private void setupConnection(){

//        final ConnectionDetector cd = new ConnectionDetector(getCanvasContext());

        try {
            uri = new URI("ws://sockets.nxtstepdsgn.com/cable");
            Log.i("::CHECK", uri.toString());
        }catch (Exception ignored){
        }

        client = new OkHttpClient();
        Request request = new Request.Builder().url(wsUri).build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();

    }
    private class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("ClientActivity", "Waiting info...");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d("Exam", "Receiving: " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d("Exam", "Receiving bytes: " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            Log.d("Exam", "Closing: " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d("Exam", "Error: " + t.getMessage());
        }
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setupConnection();

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
                // Toast.makeText(getActivity(), "Wikipedia selected, well done :-)", Toast.LENGTH_SHORT).show();
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
















