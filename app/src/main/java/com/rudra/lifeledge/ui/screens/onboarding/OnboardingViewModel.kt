package com.rudra.lifeledge.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.lifeledge.data.local.entity.Setting
import com.rudra.lifeledge.data.local.entity.SettingType
import com.rudra.lifeledge.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isOnboardingComplete = MutableStateFlow(false)
    val isOnboardingComplete: StateFlow<Boolean> = _isOnboardingComplete.asStateFlow()

    companion object {
        const val ONBOARDING_COMPLETED_KEY = "onboarding_completed"
    }

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            val isComplete = settingsRepository.getBooleanSetting(ONBOARDING_COMPLETED_KEY, false)
            _isOnboardingComplete.value = isComplete
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.saveSetting(
                Setting(
                    key = ONBOARDING_COMPLETED_KEY,
                    value = "true",
                    type = SettingType.BOOLEAN
                )
            )
            _isOnboardingComplete.value = true
        }
    }
}
