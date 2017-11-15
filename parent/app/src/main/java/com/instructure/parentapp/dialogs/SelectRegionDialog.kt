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
 *
 */

package com.instructure.parentapp.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.widget.RadioGroup
import com.instructure.canvasapi2.apis.AlertAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.R
import kotlin.properties.Delegates
import android.support.v7.widget.AppCompatRadioButton
import android.view.View

class SelectRegionDialog : AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var callback: (String) -> Unit by Delegates.notNull()

    private var pendingRegionSelection: String? = null
    private var regionRadioGroup: RadioGroup? = null

    companion object {
        fun newInstance(callback: (String) -> Unit): SelectRegionDialog {
            val dialog = SelectRegionDialog()
            dialog.callback = callback
            val args = Bundle()
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity)
        val root = inflater.inflate(R.layout.dialog_select_region, null)

        regionRadioGroup = root.findViewById(R.id.regionRadioGroup)
        regionRadioGroup?.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.unitedStatesRadioButton -> pendingRegionSelection = AlertAPI.AIRWOLF_DOMAIN_AMERICA
                R.id.canadaRadioButton -> pendingRegionSelection = AlertAPI.AIRWOLF_DOMAIN_CANADA
                R.id.singaporeRadioButton -> pendingRegionSelection = AlertAPI.AIRWOLF_DOMAIN_SINGAPORE
                R.id.irelandRadioButton -> pendingRegionSelection = AlertAPI.AIRWOLF_DOMAIN_DUBLIN
                R.id.australiaRadioButton -> pendingRegionSelection = AlertAPI.AIRWOLF_DOMAIN_SYDNEY
                R.id.germanyRadioButton -> pendingRegionSelection = AlertAPI.AIRWOLF_DOMAIN_FRANKFURT
                R.id.gammaRadioButton -> pendingRegionSelection = BuildConfig.GAMMA_DOMAIN
            }
        }

        val themeColor = ContextCompat.getColor(context, R.color.login_loginFlowBlue)
        ViewStyler.themeRadioButton(context, root.findViewById(R.id.unitedStatesRadioButton), themeColor)
        ViewStyler.themeRadioButton(context, root.findViewById(R.id.canadaRadioButton), themeColor)
        ViewStyler.themeRadioButton(context, root.findViewById(R.id.singaporeRadioButton), themeColor)
        ViewStyler.themeRadioButton(context, root.findViewById(R.id.irelandRadioButton), themeColor)
        ViewStyler.themeRadioButton(context, root.findViewById(R.id.australiaRadioButton), themeColor)
        ViewStyler.themeRadioButton(context, root.findViewById(R.id.germanyRadioButton), themeColor)
        ViewStyler.themeRadioButton(context, root.findViewById(R.id.gammaRadioButton), themeColor)

        if(BuildConfig.DEBUG) {
            root.findViewById<AppCompatRadioButton>(R.id.gammaRadioButton).visibility = View.VISIBLE
        }

        //set the spinner value to be the closest region by default (found by pinging)
        setRegionRadioButton(ApiPrefs.airwolfDomain)

        val builder = AlertDialog.Builder(context)
                .setView(root)
                .setTitle(R.string.selectChildRegion)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if(pendingRegionSelection != null) {
                        callback.invoke(pendingRegionSelection!!)
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }


        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(themeColor)
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(themeColor)
        }
        return dialog
    }

    private fun setRegionRadioButton(airwolfDomain: String) {
        when(airwolfDomain) {
            AlertAPI.AIRWOLF_DOMAIN_AMERICA -> regionRadioGroup?.check(R.id.unitedStatesRadioButton)
            AlertAPI.AIRWOLF_DOMAIN_CANADA -> regionRadioGroup?.check(R.id.canadaRadioButton)
            AlertAPI.AIRWOLF_DOMAIN_SINGAPORE -> regionRadioGroup?.check(R.id.singaporeRadioButton)
            AlertAPI.AIRWOLF_DOMAIN_DUBLIN -> regionRadioGroup?.check(R.id.irelandRadioButton)
            AlertAPI.AIRWOLF_DOMAIN_SYDNEY -> regionRadioGroup?.check(R.id.australiaRadioButton)
            AlertAPI.AIRWOLF_DOMAIN_FRANKFURT -> regionRadioGroup?.check(R.id.germanyRadioButton)
            BuildConfig.GAMMA_DOMAIN -> regionRadioGroup?.check(R.id.gammaRadioButton)
        }
    }

    override fun onDestroyView() {
        val dialog = dialog
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) dialog.setDismissMessage(null)
        super.onDestroyView()
    }
}
