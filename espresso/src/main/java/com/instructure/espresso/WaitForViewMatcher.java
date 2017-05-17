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

package com.instructure.espresso;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

public abstract class WaitForViewMatcher {
    private static final EspressoLog log = new EspressoLog(WaitForViewMatcher.class);
    private static final AtomicBoolean waiting = new AtomicBoolean(false);

    public static boolean finishedWaiting() {
        return !waiting.get();
    }

    // http://stackoverflow.com/questions/21417954/espresso-thread-sleep/22563297#22563297
    // https://github.com/braintree/braintree_android/blob/25513d76da88fe2ce9f476c4dc51f24cf6e26104/TestUtils/src/main/java/com/braintreepayments/testutils/ui/ViewHelper.java#L30

    // The viewMatcher is called on every view to determine what matches. Must be fast!
    public static ViewInteraction waitForView(final Matcher<View> viewMatcher) {
        waiting.set(true);
        final long waitTime = TimeUnit.SECONDS.toMillis(10);
        final long endTime = System.currentTimeMillis() + waitTime;

        log.i("waitForView matching...");
        do {
            try {
                ViewInteraction result = onView(viewMatcher).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
                waiting.set(false);
                return result;
            } catch (Exception | Error ignored) {
            }
        } while (System.currentTimeMillis() < endTime);

        waiting.set(false);
        return onView(viewMatcher).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
