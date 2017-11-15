/**
 * Copyright (C) 2017 Drew Hannay
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.test.rule;

import android.app.Activity;
import android.support.test.runner.MonitoringInstrumentationAccessor;
import android.util.Log;

import java.lang.reflect.Field;

// https://gist.github.com/drewhannay/7fa758847cad8a6dc26f0d1d3cb068ad

/**
 * Subclass of {@link ActivityTestRule} that cleanly handles finishing multiple activities.
 * <p/>
 * The official ActivityTestRule only calls finish() on the initial activity. However, this can cause problems if the
 * test ends in a different activity than which it was started. In this implementation, we call finish() on all
 * Activity classes that are started and wait until they actually finish before proceeding.
 */
public class FinishingActivityTestRule<T extends Activity> extends ActivityTestRule<T> {

    private static final String TAG = FinishingActivityTestRule.class.getSimpleName();

    public FinishingActivityTestRule(Class<T> activityClass) {
        super(activityClass);
    }

    public FinishingActivityTestRule(Class<T> activityClass, boolean initialTouchMode) {
        super(activityClass, initialTouchMode);
    }

    public FinishingActivityTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
        super(activityClass, initialTouchMode, launchActivity);
    }

    public void finishAllActivities() {
        MonitoringInstrumentationAccessor.finishAllActivities();

        // purposefully don't call super since we've already finished all the activities
        // instead, null out the mActivity field in the parent class using reflection
        try {
            Field activityField = ActivityTestRule.class.getDeclaredField("mActivity");
            activityField.setAccessible(true);
            activityField.set(this, null);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unable to get field through reflection", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unable to get access field through reflection", e);
        }
    }

    @Override
    public void finishActivity() {
        finishAllActivities();
    }
}
