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

import com.instructure.canvasapi2.models.Recipient;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class RecipientTest {

    @Test
    public void getIdAsLongTest_Group() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setStringId("group_12345");

        assertEquals(12345, recipient.getIdAsLong());
    }

    @Test
    public void getIdAsLongTest_Course() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setStringId("course_456789");

        assertEquals(456789, recipient.getIdAsLong());
    }

    @Test
    public void getIdAsLongTest_Number() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setStringId("123456789");

        assertEquals(123456789, recipient.getIdAsLong());
    }

    @Test
    public void getIdAsLongTest_Null() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setStringId("invalid_id");

        assertEquals(0, recipient.getIdAsLong());
    }

    @Test
    public void getRecipientTypeTest_Person() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setStringId("1234567");

        assertEquals(Recipient.Type.person, recipient.getRecipientType());
    }

    @Test
    public void getRecipientTypeTest_Group() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setStringId("not_a_person");
        recipient.setUserCount(2);

        assertEquals(Recipient.Type.group, recipient.getRecipientType());
    }

    @Test
    public void getRecipientTypeTest_MetaGroup() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setStringId("not_a_person_or_group");
        recipient.setUserCount(0);

        assertEquals(Recipient.Type.metagroup, recipient.getRecipientType());
    }

    @Test
    public void recipientTypeToIntTest_Group() throws Exception {
        assertEquals(0, Recipient.recipientTypeToInt(Recipient.Type.group));
    }

    @Test
    public void recipientTypeToIntTest_MetaGroup() throws Exception {
        assertEquals(1, Recipient.recipientTypeToInt(Recipient.Type.metagroup));
    }

    @Test
    public void recipientTypeToIntTest_Person() throws Exception {
        assertEquals(2, Recipient.recipientTypeToInt(Recipient.Type.person));
    }

    @Test
    public void intToRecipientTypeTest_Group() throws Exception {
        assertEquals(Recipient.Type.group, Recipient.intToRecipientType(0));
    }

    @Test
    public void intToRecipientTypeTest_MetaGroup() throws Exception {
        assertEquals(Recipient.Type.metagroup, Recipient.intToRecipientType(1));
    }

    @Test
    public void intToRecipientTypeTest_Person() throws Exception {
        assertEquals(Recipient.Type.person, Recipient.intToRecipientType(2));
    }

    @Test
    public void intToRecipientTypeTest_Null() throws Exception {
        assertEquals(null, Recipient.intToRecipientType(3));
    }
}