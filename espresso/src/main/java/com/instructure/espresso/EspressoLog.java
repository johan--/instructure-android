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

import android.util.Log;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

/**
 * Wrapper for android.util.log
 **/
public class EspressoLog {

    private final String tag;

    /**
     * Create a logger using class simple name for the tag.
     * @param klass The class to derive the tag from
     */
    public EspressoLog(Class klass) {
        checkNotNull(klass);
        this.tag = klass.getSimpleName();
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param msg The message you would like logged.
     */
    public int v(String msg) {
        checkNotNull(msg);
        return Log.v(tag, msg);
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public int v(String msg, Throwable tr) {
        checkNotNull(msg);
        return Log.v(tag, msg, tr);
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param msg The message you would like logged.
     */
    public int d(String msg) {
        checkNotNull(msg);
        return Log.d(tag, msg);
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public int d(String msg, Throwable tr) {
        checkNotNull(msg);
        return Log.d(tag, msg, tr);
    }

    /**
     * Send an {@link android.util.Log#INFO} log message.
     *
     * @param msg The message you would like logged.
     */
    public int i(String msg) {
        checkNotNull(msg);
        return Log.i(tag, msg);
    }

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public int i(String msg, Throwable tr) {
        checkNotNull(msg);
        return Log.i(tag, msg, tr);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message.
     *
     * @param msg The message you would like logged.
     */
    public int w(String msg) {
        checkNotNull(msg);
        return Log.w(tag, msg);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public int w(String msg, Throwable tr) {
        checkNotNull(msg);
        return Log.w(tag, msg, tr);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param tr An exception to log
     */
    public int w(Throwable tr) {
        return Log.w(tag, tr);
    }

    /**
     * Send an {@link android.util.Log#ERROR} log message.
     *
     * @param msg The message you would like logged.
     */
    public int e(String msg) {
        checkNotNull(msg);
        return Log.e(tag, msg);
    }

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public int e(String msg, Throwable tr) {
        checkNotNull(msg);
        return Log.e(tag, msg, tr);
    }
}
