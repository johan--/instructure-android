/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.canvasapi2.managers;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.AttendanceAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Attendance;
import com.instructure.canvasapi2.utils.ApiPrefs;

import java.util.Calendar;
import java.util.List;

public class AttendanceManager extends BaseManager {

    public static boolean mTesting = false;

    public static void getAttendance(long sectionId, @NonNull Calendar date, String token, String cookie, StatusCallback<List<Attendance>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO: Add testing
        } else {
            String domain = ApiPrefs.getDomain();
            String protocol = ApiPrefs.getProtocol();
            if(domain.contains(".beta.")) { domain = AttendanceAPI.BASE_TEST_DOMAIN; }
            else { domain = AttendanceAPI.BASE_DOMAIN; }

            domain = protocol + "://" + domain;

            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withDomain(domain)
                    .withForceReadFromNetwork(forceNetwork)
                    .withShouldIgnoreToken(false)
                    .build();

            AttendanceAPI.getAttendance(sectionId, date, token, cookie, adapter, callback, params);
        }
    }

    public static void markAttendance(Attendance attendance, String token, String cookie, StatusCallback<Attendance> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO: Add testing
        } else {
            String domain = ApiPrefs.getDomain();
            String protocol = ApiPrefs.getProtocol();
            if(domain.contains(".beta.")) { domain = AttendanceAPI.BASE_TEST_DOMAIN; }
            else { domain = AttendanceAPI.BASE_DOMAIN; }

            domain = protocol + "://" + domain;

            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withDomain(domain)
                    .withForceReadFromNetwork(forceNetwork)
                    .withShouldIgnoreToken(false)
                    .build();

            AttendanceAPI.markAttendance(attendance, token, cookie, adapter, callback, params);
        }
    }
}
