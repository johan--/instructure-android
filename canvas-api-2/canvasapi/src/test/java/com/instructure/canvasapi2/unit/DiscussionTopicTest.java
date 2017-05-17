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

import com.instructure.canvasapi2.models.DiscussionParticipant;
import com.instructure.canvasapi2.models.DiscussionTopic;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

public class DiscussionTopicTest {
    @Test
    public void getUnreadEntriesMap_Size() throws Exception {
        DiscussionTopic topic = new DiscussionTopic();
        ArrayList<Long> unreadList = new ArrayList<>();
        unreadList.add(8L);
        unreadList.add(13435L);

        topic.setUnreadEntries(unreadList);

        assertEquals(2, topic.getUnreadEntriesMap().size());
    }

    @Test
    public void getUnreadEntriesMap_Content() throws Exception {
        long id = 8L;

        DiscussionTopic topic = new DiscussionTopic();
        ArrayList<Long> unreadList = new ArrayList<>();
        unreadList.add(id);

        topic.setUnreadEntries(unreadList);

        assertEquals(id, topic.getUnreadEntriesMap().keySet().toArray()[0]);
    }

    @Test
    public void getParticipantsMap_Size() throws Exception {
        DiscussionTopic topic = new DiscussionTopic();
        ArrayList<DiscussionParticipant> participants = new ArrayList<>();
        DiscussionParticipant participant = new DiscussionParticipant();
        participant.setId(64343L);

        participants.add(participant);
        topic.setParticipants(participants);

        assertEquals(1, topic.getParticipantsMap().size());
    }

    @Test
    public void getParticipantsMap_Content() throws Exception {
        long id = 534234L;

        DiscussionTopic topic = new DiscussionTopic();
        ArrayList<DiscussionParticipant> participants = new ArrayList<>();
        DiscussionParticipant participant = new DiscussionParticipant();
        participant.setId(id);

        participants.add(participant);
        topic.setParticipants(participants);

        assertEquals(id, topic.getParticipantsMap().keySet().toArray()[0]);
    }

    @Test
    public void getDiscussionURL() throws Exception {
        String url = "https://mobiledev.instructure.com/courses/24219/discussion_topics/1129998";

        assertEquals(url, DiscussionTopic.getDiscussionURL("https", "mobiledev.instructure.com", 24219, 1129998));
    }

}