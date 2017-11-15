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

import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.CanvasContextPermission;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.Section;
import com.instructure.canvasapi2.models.Tab;
import com.instructure.canvasapi2.models.User;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CanvasContextTest {

    //region typeIs
    @Test
    public void typeIsGroup_TestFalse() {
        Course course = new Course();

        assertEquals(false, CanvasContext.Type.isGroup(course));
    }

    @Test
    public void typeIsGroup_TestTrue() {
        Group group = new Group();

        assertEquals(true, CanvasContext.Type.isGroup(group));
    }

    @Test
    public void typeIsCourse_TestFalse() {
        Group group = new Group();

        assertEquals(false, CanvasContext.Type.isCourse(group));
    }

    @Test
    public void typeIsCourse_TestTrue() {
        Course course = new Course();

        assertEquals(true, CanvasContext.Type.isCourse(course));
    }

    @Test
    public void typeIsUser_TestFalse() {
        Course course = new Course();

        assertEquals(false, CanvasContext.Type.isUser(course));
    }

    @Test
    public void typeIsUser_TestTrue() {
        User user = new User();

        assertEquals(true, CanvasContext.Type.isUser(user));
    }

    @Test
    public void typeIsSection_TestFalse() {
        Course course = new Course();

        assertEquals(false, CanvasContext.Type.isSection(course));
    }

    @Test
    public void typeIsSection_TestTrue() {
        Section section = new Section();

        assertEquals(true, CanvasContext.Type.isSection(section));
    }
    //endregion

    @Test
    public void canCreateDiscussions_TestTrue() {
        Course course = new Course();
        CanvasContextPermission canvasContextPermission = new CanvasContextPermission();
        canvasContextPermission.setCanCreateDiscussionTopic(true);
        course.setPermissions(canvasContextPermission);

        assertEquals(true, course.canCreateDiscussion());
    }

    @Test
    public void canCreateDiscussions_TestFalse() {
        Course course = new Course();
        CanvasContextPermission canvasContextPermission = new CanvasContextPermission();
        canvasContextPermission.setCanCreateDiscussionTopic(false);
        course.setPermissions(canvasContextPermission);

        assertEquals(false, course.canCreateDiscussion());
    }

    //region equalsTest
    @Test
    public void equals_TestNull() {
        Course course1 = new Course();
        Course course2 = null;

        assertEquals(false, course1.equals(course2));
    }

    @Test
    public void equals_TestFalse() {
        Course course = new Course();
        Group group = new Group();

        assertEquals(false, course.equals(group));
    }

    @Test
    public void equals_TestTrue() {
        Course course1 = new Course();
        Course course2 = new Course();

        assertEquals(true, course1.equals(course2));
    }
    //endregion

    @Test
    public void getSecondaryName_TestCourse() {
        Course course = new Course();
        String courseCode = "Hodor";
        course.setCourseCode(courseCode);

        assertEquals(courseCode, course.getSecondaryName());
    }

    @Test
    public void getSecondaryName_TestGroup() {
        Group group = new Group();
        String name = "Hodor";
        group.setName(name);

        assertEquals(name, group.getSecondaryName());
    }

    //region toAPIString
    @Test
    public void toAPIString_TestGroup() {
        Group group = new Group();
        group.setId(1234);

        assertEquals("/groups/1234", group.toAPIString());
    }

    @Test
    public void toAPIString_TestCourse() {
        Course course = new Course();
        course.setId(1234);

        assertEquals("/courses/1234", course.toAPIString());
    }

    @Test
    public void toAPIString_TestSection() {
        Section section = new Section();
        section.setId(1234);

        assertEquals("/sections/1234", section.toAPIString());
    }

    @Test
    public void toAPIString_TestUsers() {
        User user = new User();
        user.setId(1234);

        assertEquals("/users/1234", user.toAPIString());
    }

    @Test
    public void toAPIString_TestSelf() {
        User user = new User();
        user.setId(0);

        assertEquals("/users/self", user.toAPIString());
    }
    //endregion

    //region getContextId
    @Test
    public void getContextId_TestCourse() {
        Course course = new Course();
        course.setId(1234);

        assertEquals("course_1234", course.getContextId());
    }

    @Test
    public void getContextId_TestGroup() {
        Group group = new Group();
        group.setId(1234);

        assertEquals("group_1234", group.getContextId());
    }

    @Test
    public void getContextId_TestUser() {
        User user = new User();
        user.setId(1234);

        assertEquals("user_1234", user.getContextId());
    }
    //endregion

    //region fromContextCode
    @Test
    public void fromContextCode_TestNull1() {
        assertEquals(null, CanvasContext.fromContextCode(""));
    }

    @Test
    public void fromContextCode_TestNull2() {
        assertEquals(null, CanvasContext.fromContextCode("gr"));
    }

    @Test
    public void fromContextCode_TestCourse() {
        Course course = new Course();
        course.setId(1234);

        assertEquals(true, course.equals(CanvasContext.fromContextCode(course.getContextId())));
    }

    @Test
    public void fromContextCode_TestGroup() {
        Group group = new Group();
        group.setId(1234);

        assertEquals(true, group.equals(CanvasContext.fromContextCode(group.getContextId())));
    }

    @Test
    public void fromContextCode_TestUser() {
        User user = new User();
        user.setId(1234);

        assertEquals(true, user.equals(CanvasContext.fromContextCode(user.getContextId())));
    }
    //endregion

    @Test
    public void getApiContext_TestCourses() {
        Course course = new Course();

        assertEquals("courses", CanvasContext.getApiContext(course));
    }

    @Test
    public void getApiContext_TestGroups() {
        Group group = new Group();

        assertEquals("groups", CanvasContext.getApiContext(group));
    }

    //region getHomePageID
    @Test
    public void getHomePageID_TestNull() {
        Course course = new Course();
        course.setHomePage(null);

        assertEquals(Tab.NOTIFICATIONS_ID, course.getHomePageID());
    }

    @Test
    public void getHomePageID_TestHomeFeed() {
        Course course = new Course();
        course.setHomePage(CanvasContext.HOME_FEED);

        assertEquals(Tab.NOTIFICATIONS_ID, course.getHomePageID());
    }

    @Test
    public void getHomePageID_TestHomeSyllabus() {
        Course course = new Course();
        course.setHomePage(CanvasContext.HOME_SYLLABUS);

        assertEquals(Tab.SYLLABUS_ID, course.getHomePageID());
    }

    @Test
    public void getHomePageID_TestHomeWiki() {
        Course course = new Course();
        course.setHomePage(CanvasContext.HOME_WIKI);

        assertEquals(Tab.PAGES_ID, course.getHomePageID());
    }

    @Test
    public void getHomePageID_TestHomeAssignment() {
        Course course = new Course();
        course.setHomePage(CanvasContext.HOME_ASSIGNMENTS);

        assertEquals(Tab.ASSIGNMENTS_ID, course.getHomePageID());
    }

    @Test
    public void getHomePageID_TestHomeModules() {
        Course course = new Course();
        course.setHomePage(CanvasContext.HOME_MODULES);

        assertEquals(Tab.MODULES_ID, course.getHomePageID());
    }
    //endregion

    //region getGenericContext
    @Test
    public void getGenericContext_TestUser() {
        long id = 1234;
        String name = "hodor";
        User user = new User(id);
        user.setName(name);

        assertEquals(true,
                user.equals(CanvasContext.getGenericContext(CanvasContext.Type.USER, id, name)));
    }

    @Test
    public void getGenericContext_TestCourse() {
        long id = 1234;
        String name = "hodor";
        Course course = new Course();
        course.setId(id);
        course.setName(name);

        assertEquals(true,
                course.equals(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, id, name)));
    }

    @Test
    public void getGenericContext_TestGroup() {
        long id = 1234;
        String name = "hodor";
        Group group = new Group();
        group.setName(name);
        group.setId(id);

        assertEquals(true,
                group.equals(CanvasContext.getGenericContext(CanvasContext.Type.GROUP, id, name)));
    }

    @Test
    public void getGenericContext_TestSection() {
        long id = 1234;
        String name = "hodor";
        Section section = new Section();
        section.setName(name);
        section.setId(id);

        assertEquals(true,
                section.equals(CanvasContext.getGenericContext(CanvasContext.Type.SECTION, id, name)));
    }
    //endregion

}
