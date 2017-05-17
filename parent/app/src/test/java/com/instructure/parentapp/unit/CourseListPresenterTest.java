/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */


package com.instructure.parentapp.unit;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Student;
import com.instructure.parentapp.presenters.CourseListPresenter;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class CourseListPresenterTest {

    private Course course1;
    private Course course2;
    private CourseListPresenter presenter;

    @Before
    public void setUp() throws Exception {
        course1 = new Course();
        course2 = new Course();
        Student student = new Student();
        presenter = new CourseListPresenter(student);
    }

    @Test
    public void compare_TestEquals() throws Exception {
        course1.setId(12345);
        course2.setId(12345);

        assertEquals(0, presenter.compare(course1, course2));
    }

    @Test
    public void compare_TestNegative() throws Exception {
        course1.setId(12345);
        course2.setId(123457);

        assertEquals(-1, presenter.compare(course1, course2));
    }

    @Test
    public void compare_TestPositive() throws Exception {
        course1.setId(1234578);
        course2.setId(123457);

        assertEquals(1, presenter.compare(course1, course2));
    }

    @Test
    public void areContentsTheSame() throws Exception {
        //always false
        assertEquals(false, presenter.areContentsTheSame(course1, course1));
    }

    @Test
    public void areItemsTheSame_TestTrue() throws Exception {
        course1.setId(123456);
        course2.setId(123456);

        assertEquals(true, presenter.areItemsTheSame(course1, course2));
    }

    @Test
    public void areItemsTheSame_TestFalse() throws Exception {
        course1.setId(123456);
        course2.setId(1234565);

        assertEquals(false, presenter.areItemsTheSame(course1, course2));
    }
}