package com.rudra.lifeledge.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.JournalEntry
import com.rudra.lifeledge.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false,
    val isLoading: Boolean = true,
    val recentlyDeletedEntry: JournalEntry? = null
)

class JournalViewModel(
    private val journalRepository: JournalRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val _dbEntries = MutableStateFlow<List<JournalEntry>>(emptyList())

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            journalRepository.getAllJournalEntries().collect { entries ->
                _dbEntries.value = entries
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val currentQuery = _uiState.value.searchQuery
        val showFavOnly = _uiState.value.showFavoritesOnly
        
        var filtered = _dbEntries.value
        if (showFavOnly) {
            filtered = filtered.filter { it.isFavorite }
        }
        if (currentQuery.isNotBlank()) {
            filtered = filtered.filter { 
                it.title.contains(currentQuery, ignoreCase = true) || 
                it.content.contains(currentQuery, ignoreCase = true) 
            }
        }
        
        _uiState.value = _uiState.value.copy(
            entries = filtered,
            isLoading = false
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun toggleFavoritesFilter() {
        _uiState.value = _uiState.value.copy(showFavoritesOnly = !_uiState.value.showFavoritesOnly)
        applyFilters()
    }

    fun toggleFavorite(entry: JournalEntry) {
        viewModelScope.launch {
            val updatedEntry = entry.copy(isFavorite = !entry.isFavorite)
            journalRepository.saveJournalEntry(updatedEntry)
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentlyDeletedEntry = entry)
            journalRepository.deleteJournalEntry(entry)
        }
    }

    fun undoDeleteEntry() {
        viewModelScope.launch {
            _uiState.value.recentlyDeletedEntry?.let { entry ->
                journalRepository.saveJournalEntry(entry)
                _uiState.value = _uiState.value.copy(recentlyDeletedEntry = null)
            }
        }
    }

    fun clearDeletedEntry() {
        _uiState.value = _uiState.value.copy(recentlyDeletedEntry = null)
    }
}
