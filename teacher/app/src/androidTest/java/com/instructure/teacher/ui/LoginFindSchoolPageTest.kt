package com.instructure.teacher.ui

import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.TestRail
import org.junit.Test

class LoginFindSchoolPageTest: TeacherTest() {

    @Test
    @TestRail(ID = "C3108892")
    override fun displaysPageObjects() {
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.assertPageObjects()
    }
}
