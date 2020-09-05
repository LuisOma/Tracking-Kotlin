package com.example.tracking.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.tracking.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog : DialogFragment() {
    private var positiveButtonListener: (() -> Unit)? = null

    fun setPositiveButtonListener(listener: () -> Unit) {
        positiveButtonListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancelar la ruta")
            .setMessage("¿Esta seguro que desea cancelar la ruta?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Si") { _, _ -> positiveButtonListener?.let { it() } }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            .create()
}
