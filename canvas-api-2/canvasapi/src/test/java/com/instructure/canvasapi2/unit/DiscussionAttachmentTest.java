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

package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.models.DiscussionAttachment;
import com.instructure.canvasapi2.utils.APIHelper;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;


public class DiscussionAttachmentTest {

    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 10000;

    @Test
    public void shouldShowToUser_hidden() {
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setHidden(true);

        assertEquals(false, attachment.shouldShowToUser());
    }

    @Test
    public void shouldShowToUser_hiddenForUser() {
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setHiddenForUser(true);

        assertEquals(false, attachment.shouldShowToUser());
    }

    @Test
    public void shouldShowToUser_unlockedAndVisible() {
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setHidden(false);
        attachment.setHiddenForUser(false);
        attachment.setLocked(false);
        attachment.setLockedForUser(false);

        assertEquals(true, attachment.shouldShowToUser());
    }

    @Test
    public void shouldShowToUser_locked_noUnlockDate() {
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setLocked(true);
        attachment.setUnlockAt(null);

        assertEquals(false, attachment.shouldShowToUser());
    }

    @Test
    public void shouldShowToUser_lockedForUser_noUnlockDate() {
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setLockedForUser(true);
        attachment.setUnlockAt(null);

        assertEquals(false, attachment.shouldShowToUser());
    }

    @Test
    public void shouldShowToUser_locked_unlockDatePassed() {
        Date lockDate = new Date(System.currentTimeMillis() - MILLIS_PER_DAY);
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setUnlockAt(APIHelper.dateToString(lockDate));
        attachment.setLocked(true);

        assertEquals(true, attachment.shouldShowToUser());
    }

    @Test
    public void shouldShowToUser_locked_unlockDateNotPassed() {
        Date lockDate = new Date(System.currentTimeMillis() + MILLIS_PER_DAY);
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setUnlockAt(APIHelper.dateToString(lockDate));
        attachment.setLocked(true);

        assertEquals(false, attachment.shouldShowToUser());
    }

    @Test
    public void shouldShowToUser_lockedForUser_unlockDatePassed() {
        Date lockDate = new Date(System.currentTimeMillis() - MILLIS_PER_DAY);
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setUnlockAt(APIHelper.dateToString(lockDate));
        attachment.setLockedForUser(true);

        assertEquals(true, attachment.shouldShowToUser());
    }

    @Test
    public void shouldShowToUser_lockedForUser_unlockDateNotPassed() {
        Date lockDate = new Date(System.currentTimeMillis() + MILLIS_PER_DAY);
        DiscussionAttachment attachment = new DiscussionAttachment();
        attachment.setUnlockAt(APIHelper.dateToString(lockDate));
        attachment.setLockedForUser(true);

        assertEquals(false, attachment.shouldShowToUser());
    }

}