package com.instructure.teacher.ui

import com.instructure.dataseeding.InProcessServer
import com.instructure.soseedy.CreateTeacherRequest
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class DataSeedingTest : TeacherTest() {

    /* Ignoring this */
    override fun displaysPageObjects() = Unit

    @Test
    fun testLogin() {
        loginLandingPage.clickFindMySchoolButton()
        val teacher_domain = "mobileqa.test.instructure.com"
        loginFindSchoolPage.enterDomain(teacher_domain)
        loginFindSchoolPage.clickToolbarNextMenuItem()
        val user = InProcessServer.stub.createTeacher(CreateTeacherRequest.getDefaultInstance())
        loginSignInPage.loginAs(user.username, user.password)
        coursesListPage.assertDisplaysNoCoursesView()
    }

    @Test
    fun testTokenLogin() {
        val teacher = InProcessServer.stub.createTeacher(CreateTeacherRequest.getDefaultInstance())
        tokenLogin(teacher)
        coursesListPage.assertDisplaysNoCoursesView()
    }

}
