/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Nico Küchler
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.instructure.espresso;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.internal.deps.guava.base.Preconditions;
import android.support.test.espresso.core.internal.deps.guava.collect.Iterables;
import android.support.test.espresso.core.internal.deps.guava.collect.Sets;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * source: https://github.com/nenick/espresso-macchiato/blob/2c85c7461065f1cee36bbb06386e91adaef47d86/espresso-macchiato/src/main/java/de/nenick/espressomacchiato/testbase/EspCloseAllActivitiesFunction.java
 */
public abstract class ActivityHelper {
    public static Activity currentActivity() {
        // fix: java.lang.IllegalStateException: Querying activity state off main thread is not allowed.
        final AtomicReference<Activity> activity = new AtomicReference<>(null);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                activity.set(Iterables.getOnlyElement(getActivitiesInStages(Stage.RESUMED)));
            }
        });

        Activity result = activity.get();
        Preconditions.checkNotNull(result);
        return result;
    }

    private static Set<Activity> getActivitiesInStages(Stage... stages) {
        final Set<Activity> activities = new HashSet();
        final ActivityLifecycleMonitor instance = ActivityLifecycleMonitorRegistry.getInstance();
        for (Stage stage : stages) {
            activities.addAll(instance.getActivitiesInStage(stage));
        }
        return activities;
    }
}
