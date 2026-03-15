package com.rudra.lifeledge.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.Habit
import com.rudra.lifeledge.data.local.entity.HabitCategory
import com.rudra.lifeledge.data.local.entity.HabitCompletion
import com.rudra.lifeledge.data.local.entity.HabitFrequency
import com.rudra.lifeledge.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitsUiState(
    val habits: List<Habit> = emptyList(),
    val todayCompletions: List<Long> = emptyList(),
    val selectedCategory: HabitCategory? = null,
    val isLoading: Boolean = true,
    val recentlyDeletedHabit: Habit? = null,
    val habitStreaks: Map<Long, Int> = emptyMap(),
    val completionHistory: Map<Long, List<String>> = emptyMap(),
    val showAddHabitDialog: Boolean = false
)

class HabitsViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            launch {
                habitRepository.getActiveHabits().collect { habits ->
                    _uiState.value = _uiState.value.copy(
                        habits = habits,
                        isLoading = false
                    )
                    // Calculate streaks for each habit
                    calculateStreaks(habits)
                }
            }

            launch {
                habitRepository.getCompletionsForDate(todayStr).collect { completions ->
                    _uiState.value = _uiState.value.copy(
                        todayCompletions = completions.map { it.habitId }
                    )
                }
            }
        }
    }

    private fun calculateStreaks(habits: List<Habit>) {
        viewModelScope.launch {
            val streaks = mutableMapOf<Long, Int>()
            val history = mutableMapOf<Long, List<String>>()
            
            habits.forEach { habit ->
                val completions = habitRepository.getAllCompletionsForHabit(habit.id)
                val dates = completions.map { it.date }.sorted().reversed()
                
                history[habit.id] = dates
                
                // Calculate streak
                var streak = 0
                var currentDate = LocalDate.now()
                
                for (date in dates) {
                    val completionDate = LocalDate.parse(date)
                    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(completionDate, currentDate)
                    
                    if (daysBetween <= 1) {
                        streak++
                        currentDate = completionDate
                    } else {
                        break
                    }
                }
                streaks[habit.id] = streak
            }
            
            _uiState.value = _uiState.value.copy(
                habitStreaks = streaks,
                completionHistory = history
            )
        }
    }

    fun selectCategory(category: HabitCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun toggleHabitCompletion(habit: Habit) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            val existingCompletion = habitRepository.getCompletionForDate(habit.id, todayStr)
            if (existingCompletion != null) {
                habitRepository.deleteHabitCompletion(existingCompletion)
            } else {
                val newCompletion = HabitCompletion(
                    habitId = habit.id,
                    date = todayStr,
                    value = 1.0,
                    notes = null,
                    synced = false
                )
                habitRepository.saveHabitCompletion(newCompletion)
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentlyDeletedHabit = habit)
            habitRepository.deleteHabit(habit)
        }
    }

    fun undoDeleteHabit() {
        viewModelScope.launch {
            _uiState.value.recentlyDeletedHabit?.let { habit ->
                habitRepository.saveHabit(habit)
                _uiState.value = _uiState.value.copy(recentlyDeletedHabit = null)
            }
        }
    }

    fun clearDeletedHabit() {
        _uiState.value = _uiState.value.copy(recentlyDeletedHabit = null)
    }

    fun showAddHabitDialog() {
        _uiState.value = _uiState.value.copy(showAddHabitDialog = true)
    }

    fun hideAddHabitDialog() {
        _uiState.value = _uiState.value.copy(showAddHabitDialog = false)
    }

    fun createHabit(
        name: String,
        description: String?,
        category: HabitCategory,
        frequency: HabitFrequency,
        target: Double,
        unit: String,
        icon: String,
        color: Int
    ) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = description,
                category = category,
                frequency = frequency,
                customFrequency = null,
                target = target,
                unit = unit,
                icon = icon,
                color = color,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            habitRepository.saveHabit(habit)
            hideAddHabitDialog()
        }
    }
}
