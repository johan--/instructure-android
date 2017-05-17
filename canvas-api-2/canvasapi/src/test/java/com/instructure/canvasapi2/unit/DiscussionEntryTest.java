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


import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.models.DiscussionTopic;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class DiscussionEntryTest {

    @Test
    public void initTest_Unread() throws Exception {
        DiscussionTopic topic = new DiscussionTopic();
        DiscussionEntry parent = new DiscussionEntry();
        DiscussionEntry entry = new DiscussionEntry();

        entry.init(topic, parent);

        assertEquals(0, entry.getUnreadChildren());
    }

    @Test
    public void initTest_TotalChildrenZero() throws Exception {
        DiscussionTopic topic = new DiscussionTopic();
        DiscussionEntry parent = new DiscussionEntry();
        DiscussionEntry entry = new DiscussionEntry();

        entry.init(topic, parent);

        assertEquals(0, entry.getTotalChildren());
    }

    @Test
    public void initTest_TotalChildrenOne() throws Exception {
        DiscussionTopic topic = new DiscussionTopic();
        DiscussionEntry parent = new DiscussionEntry();
        DiscussionEntry entry = new DiscussionEntry();
        DiscussionEntry reply = new DiscussionEntry();
        entry.addReply(reply);

        entry.init(topic, parent);

        assertEquals(1, entry.getTotalChildren());
    }

    @Test
    public void initTest_UnreadChildrenOne() throws Exception {
        long id = 7L;
        ArrayList<Long> unreadEntries = new ArrayList<>();

        DiscussionTopic topic = new DiscussionTopic();

        DiscussionEntry parent = new DiscussionEntry();
        DiscussionEntry entry = new DiscussionEntry();
        DiscussionEntry reply = new DiscussionEntry();

        reply.setId(id);
        unreadEntries.add(id);
        topic.setUnreadEntries(unreadEntries);

        entry.addReply(reply);

        entry.init(topic, parent);

        assertEquals(1, entry.getUnreadChildren());
    }

    @Test
    public void depthTest_Zero() throws Exception {
        DiscussionEntry entry = new DiscussionEntry();

        assertEquals(0, entry.getDepth());
    }

    @Test
    public void depthTest_One() throws Exception {
        DiscussionEntry entry = new DiscussionEntry();
        DiscussionEntry parent = new DiscussionEntry();
        entry.setParent(parent);

        assertEquals(1, entry.getDepth());
    }

    @Test
    public void addReplyTest() throws Exception {
        DiscussionEntry entry = new DiscussionEntry();

        entry.addReply(null);
        assertNotNull(entry.getReplies());
    }

    @Test
    public void addInnerReplyTest() throws Exception {
        DiscussionEntry entry = new DiscussionEntry();
        entry.setId(1L);
        DiscussionEntry reply = new DiscussionEntry();
        entry.addReply(reply);

        DiscussionEntry innerReply = new DiscussionEntry();

        entry.addInnerReply(reply, innerReply);

        assertEquals(1, entry.getReplies().get(0).getReplies().size());
    }
}