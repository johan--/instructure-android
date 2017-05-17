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

import com.instructure.canvasapi2.models.Conversation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;


public class ConversationTest {

    @Test
    public void getWorkflowState_unread() {
        Conversation conversation = new Conversation();
        conversation.setWorkflowState("unread");

        assertEquals(Conversation.WorkflowState.UNREAD, conversation.getWorkflowState());
    }

    @Test
    public void getWorkflowState_archived() {
        Conversation conversation = new Conversation();
        conversation.setWorkflowState("ARCHIVED");

        assertEquals(Conversation.WorkflowState.ARCHIVED, conversation.getWorkflowState());
    }

    @Test
    public void getWorkflowState_read() {
        Conversation conversation = new Conversation();
        conversation.setWorkflowState("read");

        assertEquals(Conversation.WorkflowState.READ, conversation.getWorkflowState());
    }

    @Test
    public void getWorkflowState_unknown() {
        Conversation conversation = new Conversation();

        assertEquals(Conversation.WorkflowState.UNKNOWN, conversation.getWorkflowState());
    }

    @Test
    public void hasAttachments() {
        Conversation conversation = new Conversation();
        ArrayList<String> properties = new ArrayList<>();
        properties.add("attachments");
        conversation.setProperties(properties);

        assertEquals(true, conversation.hasAttachments());
    }

    @Test
    public void hasMedia() {
        Conversation conversation = new Conversation();
        ArrayList<String> properties = new ArrayList<>();
        properties.add("media_objects");
        conversation.setProperties(properties);

        assertEquals(true, conversation.hasMedia());
    }

    @Test
    public void getLastMessagePreview() {
        Conversation conversation = new Conversation();
        conversation.setDeletedString("conversation deleted");
        conversation.setLastMessage("last message");
        conversation.setDeleted(false);

        assertEquals("last message", conversation.getLastMessagePreview());
    }

    @Test
    public void getLastMessagePreview_deleted() {
        Conversation conversation = new Conversation();
        conversation.setDeletedString("conversation deleted");
        conversation.setLastMessage("last message");
        conversation.setDeleted(true);

        assertEquals("conversation deleted", conversation.getLastMessagePreview());
    }

    @Test
    public void isMonologue_noAudience() {
        Conversation conversation = new Conversation();
        conversation.setAudience(null);

        assertEquals(false, conversation.isMonologue(0L));
    }

    @Test
    public void isMonologue_emptyAudience() {
        Conversation conversation = new Conversation();
        conversation.setAudience(new ArrayList<Long>());

        assertEquals(true, conversation.isMonologue(0L));
    }

    @Test
    public void isMonologue_userInAudience() {
        Conversation conversation = new Conversation();
        ArrayList<Long> audience = new ArrayList<>();
        audience.add(1L);
        audience.add(2L);
        audience.add(3L);
        conversation.setAudience(audience);

        assertEquals(true, conversation.isMonologue(1L));
    }

    @Test
    public void isMonologue_userNotInAudience() {
        Conversation conversation = new Conversation();
        ArrayList<Long> audience = new ArrayList<>();
        audience.add(1L);
        audience.add(2L);
        audience.add(3L);
        conversation.setAudience(audience);

        assertEquals(false, conversation.isMonologue(4L));
    }

    @Test
    public void getLastMessageSent() {
        Conversation conversation = new Conversation();
        conversation.setLastMessageAt("2008-09-15T15:53:00+05:00");

        Date expectedDate = new Date(1221475980000L);

        assertEquals(0, expectedDate.compareTo(conversation.getLastMessageSent()));
    }

    @Test
    public void getLastAuthoredMessageSent() {
        Conversation conversation = new Conversation();
        conversation.setLastAuthoredMessageAt("2008-09-15T15:53:00+05:00");

        Date expectedDate = new Date(1221475980000L);

        assertEquals(0, expectedDate.compareTo(conversation.getLastAuthoredMessageSent()));
    }


}