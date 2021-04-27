package org.supersoniclegend.notes.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.supersoniclegend.notes.model.Note
import org.supersoniclegend.notes.model.NotesRepository

class NotesListViewModel : ViewModel() {

    private val notesRepository = NotesRepository.get()
    val notesLiveData = notesRepository.getAllNotes().asLiveData()

    fun insertNote(note: Note) {
        viewModelScope.launch {
            notesRepository.insertNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            notesRepository.deleteNote(note)
        }
    }
}