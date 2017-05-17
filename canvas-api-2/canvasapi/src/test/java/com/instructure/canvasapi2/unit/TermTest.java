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

import com.instructure.canvasapi2.models.Term;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TermTest {

    private Term term1;
    private Term term2;

    @Before
    public void setup() {
        term1 = new Term();
        term2 = new Term();
    }

    @Test
    public void compareTo_Equal() {
        term1.setGroupTerm(true);
        term2.setGroupTerm(true);

        assertEquals(0, term1.compareTo(term2));
    }

    @Test
    public void compareTo_After() {
        term1.setGroupTerm(true);
        term2.setGroupTerm(false);

        assertEquals(1, term1.compareTo(term2));
    }

    @Test
    public void compareTo_Before() {
        term1.setGroupTerm(false);
        term2.setGroupTerm(true);

        assertEquals(-1, term1.compareTo(term2));
    }

    @Test
    public void getEndAt_TestNullEndDate() {
        term1.setEndDate(null);

        assertEquals(null, term1.getEndAt());
    }

    @Test
    public void getStartAt_TestNullStartDate() {
        term1.setStartDate(null);

        assertEquals(null, term1.getStartAt());
    }

}