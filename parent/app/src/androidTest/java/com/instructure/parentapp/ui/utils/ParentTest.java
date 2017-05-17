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
 *
 */

package com.instructure.parentapp.ui.utils;

import com.instructure.espresso.ScreenshotTestRule;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.activity.SplashActivity;
import com.instructure.parentapp.ui.pages.AddStudentDomainPage;
import com.instructure.parentapp.ui.pages.CanvasDomainPickerPage;
import com.instructure.parentapp.ui.pages.CanvasLoginPage;
import com.instructure.parentapp.ui.pages.CreateAccountPage;
import com.instructure.parentapp.ui.pages.DashboardPage;
import com.instructure.parentapp.ui.pages.ForgotPasswordPage;
import com.instructure.parentapp.ui.pages.HelpPage;
import com.instructure.parentapp.ui.pages.NoStudentsPage;
import com.instructure.parentapp.ui.pages.ParentLoginPage;
import com.instructure.parentapp.ui.pages.ReportProblemPage;
import com.instructure.parentapp.ui.pages.SettingsPage;
import com.instructure.parentapp.ui.pages.ThresholdValuePage;
import com.instructure.parentapp.ui.pages.ThresholdsPage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public abstract class ParentTest {
    /**
     * Required for auto complete of page objects within tests
     **/
    protected ParentLoginPage parentloginPage = new ParentLoginPage();
    protected CanvasDomainPickerPage canvasDomainPickerPage = new CanvasDomainPickerPage();
    protected CanvasLoginPage canvasLoginPage = new CanvasLoginPage();
    protected CreateAccountPage createAccountPage = new CreateAccountPage();
    protected ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage();
    protected NoStudentsPage noStudentsPage = new NoStudentsPage();
    protected DashboardPage dashboardPage = new DashboardPage();
    protected SettingsPage settingsPage = new SettingsPage();
    protected ThresholdsPage thresholdsPage = new ThresholdsPage();
    protected ThresholdValuePage thresholdCourseBelowPage = ThresholdValuePage.courseBelow();
    protected ThresholdValuePage thresholdCourseAbovePage = ThresholdValuePage.courseAbove();
    protected ThresholdValuePage thresholdAssignmentBelowPage = ThresholdValuePage.assignmentBelow();
    protected ThresholdValuePage thresholdAssignmentAbovePage = ThresholdValuePage.assignmentAbove();
    protected AddStudentDomainPage addStudentDomainPage = new AddStudentDomainPage();
    protected HelpPage helpPage = new HelpPage();
    protected ReportProblemPage reportProblemPage = new ReportProblemPage();

    // Don't annotate with @Rule because we're using this in a chain.
    // NOTE: Don't get activity from mActivityRule.
    // It won't work. Use ActivityHelper (ActivityLifecycleMonitorRegistry)
    private ParentActivityTestRule<SplashActivity> mActivityRule =
            new ParentActivityTestRule<>(SplashActivity.class);

    @Rule
    public TestRule chain = RuleChain
            .outerRule(mActivityRule)
            .around(new ScreenshotTestRule());

    private static boolean configChecked = false;

    @Before
    public void launchActivity() {
        if (!configChecked) {
            checkBuildConfig();
            configChecked = true;
        }

        try {
            mActivityRule.launchActivity(null);
        } catch (Exception e) {
            // MBL-7400: retry Could not launch intent once.
            mActivityRule.launchActivity(null);
        }
    }

    private static void checkBuildConfig() {
        if (!BuildConfig.IS_TESTING) {
            throw new RuntimeException("Build config must be IS_TESTING! (qaDebug)");
        }
    }

    @Test
    public abstract void displaysPageObjects();
}
