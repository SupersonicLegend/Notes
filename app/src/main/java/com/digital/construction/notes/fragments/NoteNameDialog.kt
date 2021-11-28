package com.digital.construction.notes.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.digital.construction.notes.R
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.*

private const val TAG = "NoteNameDialog"
private const val ARG_DIALOG_TYPE = "dialog_type"
private const val ARG_NOTE_NAME = "note_name"
private const val MISSING_ARGUMENTS_MSG = "Get an instance of this class using the newInstance function"
private val INVALID_CHARACTERS = arrayOf("/")

class NoteNameDialog : DialogFragment() {

    private val notesListViewModel: NotesListViewModel by activityViewModels()

    enum class DialogType {
        CREATE, RENAME
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(TAG)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogType = requireArguments().getString(ARG_DIALOG_TYPE)
            ?: throw MissingFormatArgumentException(MISSING_ARGUMENTS_MSG)

        val initialNoteName = requireArguments().getString(ARG_NOTE_NAME)
            ?: throw MissingFormatArgumentException(MISSING_ARGUMENTS_MSG)

        val dialogView = layoutInflater.inflate(R.layout.dialog_note_name, null)
        val noteNameEditText: AppCompatEditText = dialogView.findViewById(R.id.note_name)

        noteNameEditText.setText(initialNoteName)
        noteNameEditText.filters = arrayOf(
            object : InputFilter {
                override fun filter(
                    source: CharSequence,
                    start: Int,
                    end: Int,
                    dest: Spanned,
                    dstart: Int,
                    dend: Int
                ): CharSequence {
                    if (INVALID_CHARACTERS.contains(source)) {
                        Snackbar
                            .make(requireView(), R.string.invalid_character, Snackbar.LENGTH_SHORT)
                            .show()

                        return String()
                    }

                    return source
                }
            }
        )

        @StringRes val dialogTitle: Int
        @StringRes val positiveButtonText: Int
        val broadcastIntent: Intent

        when (DialogType.valueOf(dialogType)) {
            DialogType.CREATE -> {
                dialogTitle = R.string.new_note
                positiveButtonText = R.string.create
                broadcastIntent = Intent(ACTION_CREATE_NOTE)
            }
            DialogType.RENAME -> {
                dialogTitle = R.string.rename_note
                positiveButtonText = R.string.rename
                broadcastIntent = Intent(ACTION_RENAME_NOTE)
            }
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(dialogTitle)
            .setPositiveButton(positiveButtonText, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton.setOnClickListener {
                val confirmedNoteName = noteNameEditText.text.toString()

                Timber.i("Note name chosen: $confirmedNoteName")

                notesListViewModel.notesLiveData.observe(this) { allNotes ->
                    val noteNames = allNotes.map { it.name }

                    if (noteNames.contains(confirmedNoteName)) {
                        noteNameEditText.apply {
                            setText(String())
                            hint = getString(R.string.note_exists)
                        }
                    } else if (confirmedNoteName.isNotBlank()) {
                        alertDialog.dismiss()

                        broadcastIntent.apply {
                            putExtra(NOTE_NAME, confirmedNoteName)
                            requireContext().sendBroadcast(this)
                        }
                    }
                }
            }

            negativeButton.setOnClickListener {
                alertDialog.cancel()
            }
        }

        return alertDialog
    }

    override fun onDestroy() {
        super.onDestroy()
        notesListViewModel.notesLiveData.removeObservers(this)
    }

    companion object {
        fun newInstance(dialogType: DialogType, noteName: String = String()): NoteNameDialog {
            return NoteNameDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_DIALOG_TYPE, dialogType.name)
                    putString(ARG_NOTE_NAME, noteName)
                }
            }
        }
    }
}