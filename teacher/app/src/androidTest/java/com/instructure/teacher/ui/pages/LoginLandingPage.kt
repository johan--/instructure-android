package com.instructure.teacher.ui.pages

import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.OnViewWithId
import com.instructure.teacher.ui.utils.assertDisplayed
import com.instructure.teacher.ui.utils.click
import com.instructure.teacher.ui.utils.pageAssert.PageAssert
import com.instructure.teacher.ui.utils.pageAssert.SimplePageAssert

@Suppress("unused")
class LoginLandingPage : BasePage(), PageAssert by SimplePageAssert() {

    private val canvasLogoImageView by OnViewWithId(R.id.canvasLogo)
    private val findMySchoolButton by OnViewWithId(R.id.findMySchool)
    private val canvasNetworkTextView by OnViewWithId(R.id.canvasNetwork)
    private val previousLoginWrapper by OnViewWithId(R.id.previousLoginWrapper, autoAssert = false)
    private val previousLoginTitleText by  OnViewWithId(R.id.previousLoginTitleText, autoAssert = false)
    private val previousLoginDivider by  OnViewWithId(R.id.previousLoginDivider, autoAssert = false)
    private val previousLoginRecyclerView by  OnViewWithId(R.id.previousLoginRecyclerView, autoAssert = false)
    private val canvasNameTextView by OnViewWithId(R.id.canvasName, autoAssert = false)
    private val appDescriptionTypeTextView by OnViewWithId(R.id.appDescriptionType, autoAssert = false)

    fun clickFindMySchoolButton() {
        findMySchoolButton.click()
    }

    fun clickCanvasNetworkButton() {
        canvasNetworkTextView.click()
    }

    fun assertDisplaysCanvasName() {
        canvasNameTextView.assertDisplayed()
    }

    fun assertDisplaysAppDescriptionType() {
        appDescriptionTypeTextView.assertDisplayed()
    }
}