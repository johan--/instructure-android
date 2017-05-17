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

package com.instructure.espresso;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;

import org.junit.runner.Description;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Used to automatically capture screenshots of failed tests.
 **/
public abstract class EspressoScreenshot {
    private static final AtomicInteger imageCounter = new AtomicInteger(0);
    private static final String dotPNG = ".png";
    private static final String underscore = "_";
    // Firebase Test Lab requires screenshots to be saved to /sdcard/screenshots
    private static final File screenshotPath = new File(getExternalStorageDirectory(), "screenshots");

    private static String getScreenshotName(Description description) {
        String className = description.getClassName();
        String methodName = description.getMethodName();

        int imageNumberInt = imageCounter.incrementAndGet();
        String number = String.valueOf(imageNumberInt);
        if (imageNumberInt < 10) number = "0" + number;

        String[] components = new String[] { className, underscore, methodName, underscore, number, dotPNG };

        int length = 0;

        for (String component : components) {
            length += component.length();
        }

        StringBuilder result = new StringBuilder(length);

        for (String component : components) {
            result.append(component);
        }

        return result.toString();
    }

    public static void deleteAllScreenshots() {
        try {
            File[] screenshots = screenshotPath.listFiles();
            if (screenshots == null) return;

            for (File screenshot : screenshots) {
                screenshot.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private static void prepareScreenshotPath() {
        try{
            screenshotPath.mkdirs();
        } catch (Exception ignored) {
        }
    }

    public static void takeScreenshot(Description description) {
        // uiautomation screenshot requires API >= 18
        if (android.os.Build.VERSION.SDK_INT < 18) {
            return;
        }

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Bitmap screenshot = instrumentation.getUiAutomation().takeScreenshot();
        BufferedOutputStream buffferedOut = null;

        prepareScreenshotPath();

        try {
            String screenshotName = getScreenshotName(description);
            buffferedOut = new BufferedOutputStream(new FileOutputStream(new File(screenshotPath, screenshotName)));
            int quality = 100; // png ignores quality
            screenshot.compress(Bitmap.CompressFormat.PNG, quality, buffferedOut);
            buffferedOut.flush();
        } catch (IOException ignored) {
        } finally {
            try {
                if (buffferedOut != null) buffferedOut.close();
            } catch (IOException ignored) {
            }
            screenshot.recycle();
        }
    }
}
