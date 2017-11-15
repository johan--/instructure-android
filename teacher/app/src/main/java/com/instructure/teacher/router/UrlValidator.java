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
 *
 */
package com.instructure.teacher.router;

import android.net.Uri;
import android.support.annotation.Nullable;

public class UrlValidator {
    private boolean mIsHostForLoggedInUser = false;
    private boolean mIsValid = false;
    private Uri mUri;

    public UrlValidator(String url, String userDomain) {
        mUri =  Uri.parse(url);
        if (mUri != null) {
            mIsValid = true;
            String host = mUri.getHost();
            mIsHostForLoggedInUser = isLoggedInUserHost(host, userDomain);
        }
    }

    private boolean isLoggedInUserHost(String host, String userDomain) {
        // Assumes user is already signed in (InterwebsToApplication does a signin check)
        return ((userDomain != null && userDomain.equals(host)));
    }

    public boolean isHostForLoggedInUser() {
        return mIsHostForLoggedInUser;
    }

    public boolean isValid() {
        return mIsValid;
    }

    @Nullable
    public Uri getUri() {
        return mUri;
    }
}
