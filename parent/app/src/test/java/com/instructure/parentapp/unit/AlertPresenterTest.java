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

import com.instructure.canvasapi2.models.Alert;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.parentapp.presenters.AlertPresenter;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class AlertPresenterTest {

    AlertPresenter presenter;
    Alert alert1;
    Alert alert2;

    @Before
    public void setup() {
        presenter = new AlertPresenter(new Student());
        alert1 = new Alert();
        alert2 = new Alert();
    }

    @Test
    public void compare_MarkedReadBefore() throws Exception {
        alert1.setMarkedRead(true);
        alert2.setMarkedRead(false);

        assertEquals(1, presenter.compare(alert1, alert2));
    }

    @Test
    public void compare_MarkedReadAfter() throws Exception {
        alert1.setMarkedRead(false);
        alert2.setMarkedRead(true);

        assertEquals(-1, presenter.compare(alert1, alert2));
    }

    @Test
    public void compare_NullActionDates() throws Exception {
        alert1.setMarkedRead(true);
        alert1.setActionDate(null);
        alert2.setMarkedRead(true);
        alert2.setActionDate(null);

        assertEquals(0, presenter.compare(alert1, alert2));
    }

    @Test
    public void compare_NullActionDateBefore() throws Exception {
        alert1.setMarkedRead(true);
        alert1.setActionDate(APIHelper.dateToString(new Date()));
        alert2.setMarkedRead(true);
        alert2.setActionDate(null);

        assertEquals(1, presenter.compare(alert1, alert2));
    }

    @Test
    public void compare_NullActionDateAfter() throws Exception {
        alert1.setMarkedRead(true);
        alert1.setActionDate(null);
        alert2.setMarkedRead(true);
        alert2.setActionDate(APIHelper.dateToString(new Date()));

        assertEquals(-1, presenter.compare(alert1, alert2));
    }

    @Test
    public void compare_SameDate() throws Exception {
        Date date = new Date();
        alert1.setMarkedRead(true);
        alert1.setActionDate(APIHelper.dateToString(date));
        alert2.setMarkedRead(true);
        alert2.setActionDate(APIHelper.dateToString(date));

        assertEquals(0, presenter.compare(alert1, alert2));
    }

    @Test
    public void compare_DateBefore() throws Exception {
        Date date1 = new Date();
        Date date2 = new Date(Calendar.getInstance().getTimeInMillis() + 10000);
        alert1.setMarkedRead(true);
        alert1.setActionDate(APIHelper.dateToString(date1));
        alert2.setMarkedRead(true);
        alert2.setActionDate(APIHelper.dateToString(date2));

        assertEquals(1, presenter.compare(alert1, alert2));
    }

    @Test
    public void compare_DateAfter() throws Exception {
        Date date1 = new Date(Calendar.getInstance().getTimeInMillis() + 10000);
        Date date2 = new Date();
        alert1.setMarkedRead(true);
        alert1.setActionDate(APIHelper.dateToString(date1));
        alert2.setMarkedRead(true);
        alert2.setActionDate(APIHelper.dateToString(date2));

        assertEquals(-1, presenter.compare(alert1, alert2));
    }

    @Test
    public void areContentsTheSame_NullFalse() throws Exception {
        alert1.setTitle(null);
        alert2.setTitle(null);

        assertEquals(false, presenter.areContentsTheSame(alert1, alert2));
    }

    @Test
    public void areContentsTheSame_True() throws Exception {
        String title = "Hodor";
        alert1.setTitle(title);
        alert1.setMarkedRead(true);
        alert2.setTitle(title);
        alert2.setMarkedRead(true);

        assertEquals(true, presenter.areContentsTheSame(alert1, alert2));
    }

    @Test
    public void areContentsTheSame_FalseNotNull() throws Exception {
        String title = "Hodor";
        alert1.setTitle(title);
        alert1.setMarkedRead(true);
        alert2.setTitle(title);
        alert2.setMarkedRead(false);

        assertEquals(false, presenter.areContentsTheSame(alert1, alert2));
    }

}