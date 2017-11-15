package com.instructure.teacher.ui.pages

import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.OnViewWithId
import com.instructure.teacher.ui.utils.click
import com.instructure.teacher.ui.utils.pageAssert.PageAssert
import com.instructure.teacher.ui.utils.pageAssert.SimplePageAssert
import com.instructure.teacher.ui.utils.replaceText

@Suppress("unused")
class LoginFindSchoolPage: BasePage(), PageAssert by SimplePageAssert() {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val whatsYourSchoolNameTextView by OnViewWithId(R.id.whatsYourSchoolName)
    private val topDivider by OnViewWithId(R.id.topDivider)
    private val bottomDivider by OnViewWithId(R.id.bottomDivider)
    private val domainInputEditText by OnViewWithId(R.id.domainInput)
    private val findSchoolRecyclerView by OnViewWithId(R.id.findSchoolRecyclerView)
    private val toolbarNextMenuButton by OnViewWithId(R.id.next)

    fun clickToolbarNextMenuItem() {
        toolbarNextMenuButton.click()
    }

    fun enterDomain(domain: String) {
        domainInputEditText.replaceText(domain)
    }
}
