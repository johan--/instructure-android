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
package com.instructure.teacher.ui.utils

import android.support.test.espresso.PerformException
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.util.HumanReadables
import android.support.v4.view.ViewPager
import android.view.View
import org.hamcrest.Matcher

class SetViewPagerCurrentItemAction(val pageNumber: Int) : ViewAction {
    override fun getDescription() = "set ViewPager current item to $pageNumber"

    override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(ViewPager::class.java)

    override fun perform(uiController: UiController, view: View?) {
        val pager = view as ViewPager

        val adapter = pager.adapter ?: throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(RuntimeException("ViewPager adapter cannot be null"))
                .build()

        if (pageNumber >= adapter.count) throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(IndexOutOfBoundsException("Requested page $pageNumber in ViewPager of size ${adapter.count}"))
                .build()

        pager.setCurrentItem(pageNumber, false)

        uiController.loopMainThreadUntilIdle()
    }

}