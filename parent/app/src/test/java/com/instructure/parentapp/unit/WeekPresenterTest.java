package com.instructure.parentapp.unit;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Student;
import com.instructure.parentapp.factorys.WeekViewPresenterFactory;
import com.instructure.parentapp.models.WeekHeaderItem;
import com.instructure.parentapp.presenters.WeekPresenter;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class WeekPresenterTest {

    private static final long MONDAY = 1484000031210L;//Monday when test was written
    private static final long TUESDAY = 1484086479434L;//Day after Monday, Tuesday

    private WeekPresenter mPresenter;

    @Before
    public void setUp() throws Exception {

        Student student = new Student();
        student.setStudentId("student_12345");
        student.setParentId("parent_12345");
        student.setStudentDomain("https://localhost.com");
        student.setStudentName("Man in the Yellow Hat");

        Course course = new Course();
        course.setName("Curious George and the Hidden Course of Doom");
        course.setCourseCode("course_12345");

        mPresenter = new WeekViewPresenterFactory(student, course).create();
    }

    @Test
    public void compare_headerOrder() throws Exception {
        assertTrue(mPresenter.compare(getHeader(MONDAY), getHeader(TUESDAY)) == -1);
    }

    @Test
    public void compare_headerOrderReverse() throws Exception {
        assertTrue(mPresenter.compare(getHeader(TUESDAY), getHeader(MONDAY)) == 1);
    }

    @Test
    public void compare_headerOrderEquals() throws Exception {
        assertTrue(mPresenter.compare(getHeader(MONDAY), getHeader(MONDAY)) == 0);
    }

    @Test
    public void getStudent_notNull() throws Exception {
        assertTrue(mPresenter.getStudent() != null);
    }

    @Test
    public void getCourses_notNull() throws Exception {
        assertTrue(mPresenter.getCourse() != null);
    }

    @Test
    public void getCoursesMap_notNull() throws Exception {
        assertTrue(mPresenter.getCoursesMap() != null);
    }

    private static WeekHeaderItem getHeader(long time) {
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(time);
        WeekHeaderItem header = new WeekHeaderItem(date.get(Calendar.DAY_OF_WEEK));
        header.setDate(date);
        return header;
    }
}