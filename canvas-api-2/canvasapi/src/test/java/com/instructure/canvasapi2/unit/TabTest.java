/**
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
 */

package com.instructure.canvasapi2.unit;

import com.instructure.canvasapi2.models.Tab;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TabTest {

    private Tab tabby1;
    private String id;
    private String label;

    @Before
    public void setUp() {
        id = "id";
        label = "label";
        tabby1 = Tab.newInstance(id, label);
    }

    @Test
    public void newInstance() {
        assertNotNull(tabby1);
    }

    //region equals
    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    public void equals_TestSameObject() {
        Tab tabby2 = tabby1;

        assertTrue(tabby1.equals(tabby2));
    }

    @SuppressWarnings("ObjectEqualsNull")
    @Test
    public void equals_TestObjectNull() {
        assertFalse(tabby1.equals(null));
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    public void equals_TestDifferentClass() {
        String tabby2 = "String class";

        assertFalse(tabby1.equals(tabby2));
    }

    @Test
    public void equals_TestSameValues() {
        Tab tabby2 = Tab.newInstance("id", "label");

        assertTrue(tabby1.equals(tabby2));
    }

    @Test
    public void equals_TestDifferentValues() {
        Tab tabby2 = Tab.newInstance("id", "label_extra");

        assertFalse(tabby1.equals(tabby2));
    }

    //endregion

    //region toString
    @Test
    public void toString_TestNotNull() {
        assertNotNull(tabby1);
    }

    @Test
    public void toString_TestCorrectFormat() {
        assertEquals(id + ":" + label, tabby1.toString());
    }
    //endregion
}