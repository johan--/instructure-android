/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.parentapp.ui.utils;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.FinishingActivityTestRule;

import com.instructure.parentapp.BuildConfig;

class ParentActivityTestRule<T extends Activity> extends FinishingActivityTestRule<T> {

    ParentActivityTestRule(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void beforeActivityLaunched() {
        finishAllActivities();
        new ParentAppResetter().performReset(InstrumentationRegistry.getTargetContext(), BuildConfig.IS_TESTING);
    }
}
