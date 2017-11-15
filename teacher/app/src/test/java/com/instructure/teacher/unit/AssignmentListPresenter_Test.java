/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.unit;

import com.instructure.canvasapi2.managers.AssignmentManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.models.Course;
import org.junit.Before;
import org.junit.Test;

import com.instructure.teacher.factory.AssignmentListPresenterFactory;
import com.instructure.teacher.presenters.AssignmentListPresenter;

import static org.junit.Assert.*;

public class AssignmentListPresenter_Test {

    private AssignmentListPresenter mPresenter;

    @Before
    public void setup() {
        //Create base state for presenter
        Course course = new Course();
        course.setId(1);
        AssignmentListPresenterFactory factory = new AssignmentListPresenterFactory(course);
        mPresenter = factory.create();

        CourseManager.mTesting = true;
        AssignmentManager.mTesting = true;
        //Initialize presenters state for testing
        mPresenter.loadData(false);
        mPresenter.selectGradingPeriodIndex(1);
    }

    @Test
    public void baseStateTest_GradingPeriodsSize() {
        //Assert equals should be written as: assertEquals(expected, actual);
        assertEquals(4, mPresenter.getGradingPeriods().size());
    }

    @Test
    public void baseStateTest_SelectedGradingPeriod() {
        assertEquals(1L, mPresenter.getSelectedGradingPeriodId());
    }

    @Test
    public void baseStateTest_CourseNotNull() {
        assertTrue(mPresenter.getCanvasContext() != null);
    }

    @Test
    public void selectGradingPeriodIndex_TestNewIndex() {
        mPresenter.selectGradingPeriodIndex(2);
        assertTrue(mPresenter.getSelectedGradingPeriodId() == 2);
    }

}



































