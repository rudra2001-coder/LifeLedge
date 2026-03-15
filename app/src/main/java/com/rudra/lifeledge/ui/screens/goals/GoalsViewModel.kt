package com.rudra.lifeledge.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.Goal
import com.rudra.lifeledge.data.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoalsUiState(
    val activeGoals: List<Goal> = emptyList(),
    val completedGoals: List<Goal> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val recentlyDeletedGoal: Goal? = null
)

class GoalsViewModel(
    private val goalRepository: GoalRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            launch {
                goalRepository.getActiveGoals().collect { active ->
                    _uiState.value = _uiState.value.copy(
                        activeGoals = active,
                        isLoading = false
                    )
                }
            }

            launch {
                goalRepository.getCompletedGoals().collect { completed ->
                    _uiState.value = _uiState.value.copy(
                        completedGoals = completed,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentlyDeletedGoal = goal)
            goalRepository.deleteGoal(goal)
        }
    }

    fun undoDeleteGoal() {
        viewModelScope.launch {
            _uiState.value.recentlyDeletedGoal?.let { goal ->
                goalRepository.saveGoal(goal)
                _uiState.value = _uiState.value.copy(recentlyDeletedGoal = null)
            }
        }
    }

    fun clearDeletedGoal() {
        _uiState.value = _uiState.value.copy(recentlyDeletedGoal = null)
    }
}
