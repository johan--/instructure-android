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

import com.instructure.canvasapi2.models.MediaComment;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class MediaCommentTest {

    private MediaComment mediaComment;

    @Before
    public void setUp() throws Exception {
        mediaComment = new MediaComment();
    }

    @Test
    public void getMediaType_audioWithNull() throws Exception {
        mediaComment.setMediaType(null);
        assertTrue(mediaComment.getMediaType() == MediaComment.MediaType.AUDIO);
    }

    @Test
    public void getMediaType_audioWithString() throws Exception {
        mediaComment.setMediaType("audio");
        assertTrue(mediaComment.getMediaType() == MediaComment.MediaType.AUDIO);
    }

    @Test
    public void getMediaType_audioWithRandomString() throws Exception {
        mediaComment.setMediaType("abcde");
        assertTrue(mediaComment.getMediaType() == MediaComment.MediaType.AUDIO);
    }

    @Test
    public void getMediaType_videoWithNull() throws Exception {
        mediaComment.setMediaType(null);
        assertTrue(mediaComment.getMediaType() != MediaComment.MediaType.VIDEO);
    }

    @Test
    public void getMediaType_videoWithRandomString() throws Exception {
        mediaComment.setMediaType("abcde");
        assertTrue(mediaComment.getMediaType() != MediaComment.MediaType.VIDEO);
    }

    @Test
    public void getMediaType_videoWithString() throws Exception {
        mediaComment.setMediaType("video");
        assertTrue(mediaComment.getMediaType() == MediaComment.MediaType.VIDEO);
    }

    @Test
    public void getFileName_noMediaIdNoUrl() throws Exception {
        assertTrue(mediaComment.getFileName() == null);
    }

    @Test
    public void getFileName_withMediaIdNoUrl() throws Exception {
        mediaComment.setMediaId("audio_1234");
        assertTrue(mediaComment.getFileName() == null);
    }

    @Test
    public void getFileName_noMediaIdWithUrl() throws Exception {
        mediaComment.setUrl("http://localhost.com");
        assertTrue(mediaComment.getFileName() == null);
    }

    @Test
    public void getFileName_withMediaIdWithUrl() throws Exception {
        mediaComment.setMediaId("kitty_kat");
        mediaComment.setUrl("http://localhost.com?abcde=1234");
        assertTrue(mediaComment.getFileName().equals("kitty_kat.1234"));
    }


    @Test
    public void getFileName_withMediaIdBadUrl() throws Exception {
        mediaComment.setMediaId("kitty_kat");
        mediaComment.setUrl("http://localhost.com");
        assertTrue(mediaComment.getFileName().equals("kitty_kat.http://localhost.com"));
    }

    @Test
    public void getDisplayName_fixedDateNoDisplayName() throws Exception {
        final long time = 1483737388840L;//The date test was written
        Date date = new Date(time);
        assertTrue(mediaComment.getDisplayName(date).equals("Friday 06 Jan 2017."));
    }

    @Test
    public void getDisplayName_fixedDateWithDisplayName() throws Exception {
        final long time = 1483737388840L;//The date test was written
        Date date = new Date(time);
        mediaComment.setDisplayName("Curious George");
        assertTrue(mediaComment.getDisplayName(date).equals("Curious George"));
    }
}