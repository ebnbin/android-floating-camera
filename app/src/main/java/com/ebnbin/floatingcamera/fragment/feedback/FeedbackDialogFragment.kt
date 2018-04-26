package com.ebnbin.floatingcamera.fragment.feedback

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.ebnbin.floatingcamera.R

class FeedbackDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context ?: return super.onCreateDialog(savedInstanceState)

        val rootView = View.inflate(context, R.layout.feedback, null)
        val editText = rootView.findViewById<EditText>(R.id.feedback)

        return AlertDialog.Builder(context)
                .setTitle(R.string.feedback_title)
                .setView(rootView)
                .setPositiveButton(R.string.feedback_positive) { _, _ ->
                    if (editText.text.isEmpty()) return@setPositiveButton
                    Crashlytics.logException(FeedbackException(editText.text.toString()))
                    Toast.makeText(context, R.string.feedback_toast, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.feedback_negative, null)
                .create()
    }
}
