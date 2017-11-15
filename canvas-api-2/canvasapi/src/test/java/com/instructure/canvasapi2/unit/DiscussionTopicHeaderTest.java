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

package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class DiscussionTopicHeaderTest {

    @Test
    public void convertToDiscussionEntryTest_Null() throws Exception {
        DiscussionTopicHeader header = new DiscussionTopicHeader();

        assertNotNull(header.convertToDiscussionEntry("localized_graded", "localized_points"));
    }

    @Test
    public void convertToDiscussionEntryTest_Message() throws Exception {
        String message = "here is a message";
        DiscussionTopicHeader header = new DiscussionTopicHeader();
        header.setMessage(message);
        DiscussionEntry entry = header.convertToDiscussionEntry("graded", "points");
        assertEquals(header.getMessage(), entry.getMessage());
    }

    @Test
    public void convertToDiscussionEntryTest_Description() throws Exception {
        String localized_graded = "Graded discussion";
        DiscussionTopicHeader header = new DiscussionTopicHeader();
        Assignment assignment = new Assignment();
        header.setAssignment(assignment);

        DiscussionEntry entry = header.convertToDiscussionEntry(localized_graded, "points");

        assertEquals(localized_graded, entry.getDescription());
    }

    @Test
    public void convertToDiscussionEntryTest_NullParent() throws Exception {
        DiscussionTopicHeader header = new DiscussionTopicHeader();

        DiscussionEntry entry = header.convertToDiscussionEntry("grade", "points");
        assertEquals(null, entry.getParent());
    }

    @Test
    public void convertToDiscussionEntryTest_ParentID() throws Exception {
        DiscussionTopicHeader header = new DiscussionTopicHeader();

        DiscussionEntry entry = header.convertToDiscussionEntry("grade", "points");
        assertEquals(-1, entry.getParentId());
    }

    @Test
    public void getTypeTest_SideComment() throws Exception {
        DiscussionTopicHeader discussionTopicHeader = new DiscussionTopicHeader();
        discussionTopicHeader.setDiscussionType("side_comment");

        assertEquals(DiscussionTopicHeader.DiscussionType.SIDE_COMMENT, discussionTopicHeader.getType());
    }

    @Test
    public void getTypeTest_Threaded() throws Exception {
        DiscussionTopicHeader discussionTopicHeader = new DiscussionTopicHeader();
        discussionTopicHeader.setDiscussionType("threaded");

        assertEquals(DiscussionTopicHeader.DiscussionType.THREADED, discussionTopicHeader.getType());
    }

    @Test
    public void getTypeTest_Unknown() throws Exception {
        DiscussionTopicHeader discussionTopicHeader = new DiscussionTopicHeader();
        discussionTopicHeader.setDiscussionType("other");

        assertEquals(DiscussionTopicHeader.DiscussionType.UNKNOWN, discussionTopicHeader.getType());
    }

    @Test
    public void getStatusTest_Read() throws Exception {
        DiscussionTopicHeader discussionTopicHeader = new DiscussionTopicHeader();
        discussionTopicHeader.setReadState("read");

        assertEquals(DiscussionTopicHeader.ReadState.READ, discussionTopicHeader.getStatus());
    }

    @Test
    public void getStatusTest_Unread() throws Exception {
        DiscussionTopicHeader discussionTopicHeader = new DiscussionTopicHeader();
        discussionTopicHeader.setReadState("unread");

        assertEquals(DiscussionTopicHeader.ReadState.UNREAD, discussionTopicHeader.getStatus());
    }

    @Test
    public void getStatusTest_Other() throws Exception {
        DiscussionTopicHeader discussionTopicHeader = new DiscussionTopicHeader();
        discussionTopicHeader.setReadState("anything_else");

        assertEquals(DiscussionTopicHeader.ReadState.UNREAD, discussionTopicHeader.getStatus());
    }

}