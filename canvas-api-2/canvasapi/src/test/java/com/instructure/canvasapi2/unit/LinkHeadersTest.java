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

import com.instructure.canvasapi2.utils.LinkHeaders;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinkHeadersTest {

    @Test
    public void toString_TestPrev() throws Exception {
        LinkHeaders linkHeaders = generateLinkHeaders();

        String testVal = linkHeaders.toString();

        String[] testValArray = testVal.split("\n");

        assertEquals("PREV:  prevUrl", testValArray[0]);
    }

    @Test
    public void toString_TestNext() throws Exception {
        LinkHeaders linkHeaders = generateLinkHeaders();

        String testVal = linkHeaders.toString();

        String[] testValArray = testVal.split("\n");

        assertEquals("NEXT:  nextUrl", testValArray[1]);
    }

    @Test
    public void toString_TestLast() throws Exception {
        LinkHeaders linkHeaders = generateLinkHeaders();

        String testVal = linkHeaders.toString();

        String[] testValArray = testVal.split("\n");

        assertEquals("LAST:  lastUrl", testValArray[2]);
    }

    @Test
    public void toString_TestFirst() throws Exception {
        LinkHeaders linkHeaders = generateLinkHeaders();

        String testVal = linkHeaders.toString();

        String[] testValArray = testVal.split("\n");

        assertEquals("FIRST: firstUrl", testValArray[3]);
    }

    private LinkHeaders generateLinkHeaders() {
        LinkHeaders linkHeaders = new LinkHeaders();
        linkHeaders.prevUrl = "prevUrl";
        linkHeaders.nextUrl = "nextUrl";
        linkHeaders.lastUrl = "lastUrl";
        linkHeaders.firstUrl = "firstUrl";

        return linkHeaders;
    }

}
