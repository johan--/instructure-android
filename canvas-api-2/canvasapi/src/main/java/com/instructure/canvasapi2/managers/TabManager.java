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
*/
package com.instructure.canvasapi2.managers;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.TabAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Tab;

import java.util.List;

public class TabManager extends BaseManager {

    public static boolean mTesting = false;

    public static void getTabs(CanvasContext canvasContext, StatusCallback<List<Tab>> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            // TODO: Add testing
//            TabManager_Test.getTabs(callback, courseId)
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .withShouldIgnoreToken(false)
                    .build();

            TabAPI.getTabs(canvasContext.getId(), adapter, callback, params);
        }
    }
}