package com.instructure.parentapp.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import com.instructure.parentapp.R
import kotlin.properties.Delegates

class OutOfRegionDialog : AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var callback: (Int) -> Unit by Delegates.notNull()

    companion object {
        private val REGION_STRING = "region"

        fun newInstance(readableRegion: String, callback: (Int) -> Unit): OutOfRegionDialog {
            val dialog = OutOfRegionDialog()
            dialog.callback = callback
            val bundle = Bundle()
            bundle.putString(REGION_STRING, readableRegion)
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
                .setTitle(R.string.unauthorizedRegion)
                .setMessage(getString(R.string.mismatchedRegionMessage, arguments.getString(REGION_STRING, "")))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    callback.invoke(0)
                }

        val dialog = builder.create()
        dialog.setOnShowListener {
            val themeColor = ContextCompat.getColor(context, R.color.login_loginFlowBlue)
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(themeColor)
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(themeColor)
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onDestroyView() {
        val dialog = dialog
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) dialog.setDismissMessage(null)
        super.onDestroyView()
    }
}