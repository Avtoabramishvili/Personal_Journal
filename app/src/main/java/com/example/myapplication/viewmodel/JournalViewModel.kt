package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.room.JournalEntry
import com.example.myapplication.repository.JournalRepository
import kotlinx.coroutines.launch

class JournalViewModel(private val repository: JournalRepository) : ViewModel() {
    val allEntries: LiveData<List<JournalEntry>> = repository.getAllEntries()

    fun addEntry(entry: JournalEntry) = viewModelScope.launch {
        repository.addEntry(entry)
    }

    fun updateEntry(entry: JournalEntry) = viewModelScope.launch {
        repository.updateEntry(entry)
    }

    fun deleteEntry(entry: JournalEntry) = viewModelScope.launch {
        repository.deleteEntry(entry)
    }

    // Retrieves LiveData for a single entry by ID from the repository
    fun getEntryById(entryId: String): LiveData<JournalEntry> {
        return repository.getEntryById(entryId)
    }
}