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

package com.instructure.pandautils.unit;

import com.instructure.canvasapi2.models.User;
import com.instructure.pandautils.utils.ProfileUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProfileUtilsTest {

    @Test
    public void getUserInitials_string1() throws Exception {
        User user = new User();
        user.setShortName("Billy Bob");

        String testValue = ProfileUtils.getUserInitials(user.getShortName());

        assertEquals("BB", testValue);
    }

    @Test
    public void getUserInitials_string2() throws Exception {
        User user = new User();
        user.setShortName("Billy Joel");

        String testValue = ProfileUtils.getUserInitials(user.getShortName());

        assertEquals("BJ", testValue);
    }

    @Test
    public void getUserInitials_string3() throws Exception {
        User user = new User();
        user.setShortName("Billy Bob Thorton");

        String testValue = ProfileUtils.getUserInitials(user.getShortName());

        assertEquals("B", testValue);
    }

    @Test
    public void getUserHexColorString_case0() throws Exception {
        String name = "Ivan";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#EF4437", textValue);
    }


    @Test
    public void getUserHexColorString_case1() throws Exception {
        String name = "Jill";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#F0592B", textValue);
    }

    @Test
    public void getUserHexColorString_case2() throws Exception {
        String name = "Kevin";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#F8971C", textValue);
    }

    @Test
    public void getUserHexColorString_case3() throws Exception {
        String name = "Lauren";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#009688", textValue);
    }

    @Test
    public void getUserHexColorString_case4() throws Exception {
        String name = "Mary";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#4CAE4E", textValue);
    }

    @Test
    public void getUserHexColorString_case5() throws Exception {
        String name = "Nancy";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#09BCD3", textValue);
    }

    @Test
    public void getUserHexColorString_case6() throws Exception {
        String name = "Oliver";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#35A4DC", textValue);
    }

    @Test
    public void getUserHexColorString_case7() throws Exception {
        String name = "Alfred";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#2083C5", textValue);
    }

    @Test
    public void getUserHexColorString_case8() throws Exception {
        String name = "Ben";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#4554A4", textValue);
    }

    @Test
    public void getUserHexColorString_case9() throws Exception {
        String name = "Cindy";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#65499D", textValue);
    }

    @Test
    public void getUserHexColorString_case10() throws Exception {
        String name = "David";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#F06291", textValue);
    }

    @Test
    public void getUserHexColorString_case11() throws Exception {
        String name = "Ethan";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#E71F63", textValue);
    }

    @Test
    public void getUserHexColorString_case12() throws Exception {
        String name = "Frank";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#9D9E9E", textValue);
    }

    @Test
    public void getUserHexColorString_case13() throws Exception {
        String name = "Gilbert";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#FDC010", textValue);
    }

    @Test
    public void getUserHexColorString_case14() throws Exception {
        String name = "Hank";

        String textValue = ProfileUtils.getUserHexColorString(name);

        assertEquals("#8F3E97", textValue);
    }
}