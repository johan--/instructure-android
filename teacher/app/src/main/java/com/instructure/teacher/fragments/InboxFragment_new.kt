package com.instructure.teacher.fragments

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.loadUri
import com.instructure.teacher.R
import com.instructure.teacher.utils.adoptToolbarStyle
import kotlinx.android.synthetic.main.fragment_inbox_coming_soon.*

class InboxFragment_new : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_inbox_coming_soon, container, false)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    fun setupToolbar() {
        titleTextView.adoptToolbarStyle(toolbar)
        logoImageView.loadUri(Uri.parse(ThemePrefs.logoUrl))
        ViewStyler.themeToolbar(activity, toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }
}
