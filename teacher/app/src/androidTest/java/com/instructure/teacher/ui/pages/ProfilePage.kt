package com.instructure.teacher.ui.pages

import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.instructure.teacher.R
import com.instructure.teacher.ui.models.CanvasUser
import com.instructure.teacher.ui.utils.OnViewWithId
import com.instructure.teacher.ui.utils.WaitForToolbarTitle
import com.instructure.teacher.ui.utils.pageAssert.PageAssert
import com.instructure.teacher.ui.utils.pageAssert.SimplePageAssert

class ProfilePage: BasePage(), PageAssert by SimplePageAssert() {

    private val toolbarTitle by WaitForToolbarTitle(R.string.tab_profile)

    private val usersAvatar by OnViewWithId(R.id.usersAvatar)

    private val usersName by OnViewWithId(R.id.usersName)

    private val usersEmail by OnViewWithId(R.id.usersEmail)

    fun assertProfileDetails(teacher: CanvasUser) {
        usersName.check(matches(withText(teacher.shortName)))
        usersEmail.check(matches(withText(teacher.loginId)))
    }
}
