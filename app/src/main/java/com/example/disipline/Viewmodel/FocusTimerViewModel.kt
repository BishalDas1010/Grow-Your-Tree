package com.example.disipline.Viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disipline.UI.FocusSettings
import com.example.disipline.UI.FocusTimerUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FocusTimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        FocusTimerUiState(
            taskName = "",
            totalSeconds = 25 * 60,
            remainingSeconds = 25 * 60,
            isRunning = false,
            whiteNoiseOn = false
        )
    )
    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null

    /** Called once with the user's choices from the Focus screen. */
    fun start(settings: FocusSettings) {
        tickJob?.cancel()
        _uiState.value = FocusTimerUiState(
            taskName = settings.task.ifBlank { settings.mode.label },
            totalSeconds = settings.minutes * 60,
            remainingSeconds = settings.minutes * 60,
            isRunning = true,
            whiteNoiseOn = settings.ambientSound
        )
        startTicking()
    }

    fun togglePause() {
        val wasRunning = _uiState.value.isRunning
        _uiState.update { it.copy(isRunning = !wasRunning) }
        if (!wasRunning) startTicking() else tickJob?.cancel()
    }

    fun toggleWhiteNoise() {
        _uiState.update { it.copy(whiteNoiseOn = !it.whiteNoiseOn) }
    }

    fun end() {
        tickJob?.cancel()
        _uiState.update { it.copy(isRunning = false, remainingSeconds = 0) }
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1_000)
                if (!_uiState.value.isRunning) continue
                _uiState.update {
                    it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0))
                }
            }
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    override fun onCleared() {
        tickJob?.cancel()
        super.onCleared()
    }
}