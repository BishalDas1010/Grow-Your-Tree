////////package com.example.disipline.Viewmodel
////////
////////import androidx.lifecycle.ViewModel
////////import androidx.lifecycle.viewModelScope
////////import com.example.disipline.UI.FocusSettings
////////import com.example.disipline.UI.FocusTimerUiState
////////import com.example.disipline.data.reposotry.HabitRepository
////////import kotlinx.coroutines.Job
////////import kotlinx.coroutines.delay
////////import kotlinx.coroutines.flow.MutableStateFlow
////////import kotlinx.coroutines.flow.StateFlow
////////import kotlinx.coroutines.flow.asStateFlow
////////import kotlinx.coroutines.flow.update
////////import kotlinx.coroutines.launch
////////
////////class FocusTimerViewModel : ViewModel() {
////////
////////    private val _uiState = MutableStateFlow(
////////        FocusTimerUiState(
////////            taskName = "",
////////            totalSeconds = 25 * 60,
////////            remainingSeconds = 25 * 60,
////////            isRunning = false,
////////            whiteNoiseOn = false,
////////            completed = false
////////        )
////////    )
////////    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()
////////
////////    private var tickJob: Job? = null
////////
////////    // The session this timer belongs to (null when started without a session)
////////    private var currentHabitId: Int? = null
////////    private var currentSessionId: Int? = null
////////
////////    /**
////////     * Called once with the user's choices from the Focus screen.
////////     * Pass habitId and sessionId so the session gets checked when the timer finishes.
////////     */
////////    fun start(
////////        settings: FocusSettings,
////////        habitId: Int? = null,
////////        sessionId: Int? = null
////////    ) {
////////        tickJob?.cancel()
////////        currentHabitId = habitId
////////        currentSessionId = sessionId
////////
////////        _uiState.value = FocusTimerUiState(
////////            taskName = settings.task.ifBlank { settings.mode.label },
////////            totalSeconds = settings.minutes * 60,
////////            remainingSeconds = settings.minutes * 60,
////////            isRunning = true,
////////            whiteNoiseOn = settings.ambientSound,
////////            completed = false
////////        )
////////        startTicking()
////////    }
////////
////////    fun togglePause() {
////////        val wasRunning = _uiState.value.isRunning
////////        _uiState.update { it.copy(isRunning = !wasRunning) }
////////        if (!wasRunning) startTicking() else tickJob?.cancel()
////////    }
////////
////////    fun toggleWhiteNoise() {
////////        _uiState.update { it.copy(whiteNoiseOn = !it.whiteNoiseOn) }
////////    }
////////
////////    /** User pressed End or Back: cancels, does NOT mark as completed. */
////////    fun end() {
////////        tickJob?.cancel()
////////        _uiState.update {
////////            it.copy(isRunning = false, remainingSeconds = 0, completed = false)
////////        }
////////    }
////////
////////    private fun startTicking() {
////////        tickJob?.cancel()
////////        tickJob = viewModelScope.launch {
////////            while (_uiState.value.remainingSeconds > 0) {
////////                delay(1_000)
////////                if (!_uiState.value.isRunning) continue
////////                _uiState.update {
////////                    it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0))
////////                }
////////            }
////////            // Reached 0 on its own -> check the session, then flag completed for the UI.
////////            markSessionDone()
////////            _uiState.update { it.copy(isRunning = false, completed = true) }
////////        }
////////    }
////////
////////    /** Checks the session only if it isn't already done, so it can never be un-checked. */
////////    private fun markSessionDone() {
////////        val habitId = currentHabitId ?: return
////////        val sessionId = currentSessionId ?: return
////////
////////        val session = HabitRepository.habits.value
////////            .firstOrNull { it.id == habitId }
////////            ?.sessions?.firstOrNull { it.id == sessionId }
////////
////////        if (session != null && !session.isDone) {
////////            HabitRepository.toggleSession(habitId, sessionId)
////////        }
////////    }
////////
////////    override fun onCleared() {
////////        tickJob?.cancel()
////////        super.onCleared()
////////    }
////////}
//////
//////
//////
//////package com.example.disipline.Viewmodel
//////
//////import androidx.lifecycle.ViewModel
//////import androidx.lifecycle.viewModelScope
//////import com.example.disipline.UI.FocusSettings
//////import com.example.disipline.UI.FocusTimerUiState
//////import com.example.disipline.data.reposotry.HabitRepository
//////import kotlinx.coroutines.Job
//////import kotlinx.coroutines.delay
//////import kotlinx.coroutines.flow.MutableStateFlow
//////import kotlinx.coroutines.flow.StateFlow
//////import kotlinx.coroutines.flow.asStateFlow
//////import kotlinx.coroutines.flow.update
//////import kotlinx.coroutines.launch
//////
//////class FocusTimerViewModel : ViewModel() {
//////
//////    private val _uiState = MutableStateFlow(
//////        FocusTimerUiState(
//////            taskName = "",
//////            totalSeconds = 25 * 60,
//////            remainingSeconds = 25 * 60,
//////            isRunning = false,
//////            whiteNoiseOn = false,
//////            completed = false
//////        )
//////    )
//////    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()
//////
//////    private var tickJob: Job? = null
//////
//////    // Remember which habit/session to check off when the countdown completes.
//////    private var habitId: Int? = null
//////    private var sessionId: Int? = null
//////
//////    /**
//////     * Called once with the user's choices. [habitId]/[sessionId] are non-null
//////     * when the Focus screen was opened from a habit's "Start Next Session".
//////     */
//////    fun start(
//////        settings: FocusSettings,
//////        habitId: Int? = null,
//////        sessionId: Int? = null
//////    ) {
//////        this.habitId = habitId
//////        this.sessionId = sessionId
//////
//////        tickJob?.cancel()
//////        _uiState.value = FocusTimerUiState(
//////            taskName = settings.task.ifBlank { settings.mode.label },
//////            totalSeconds = settings.minutes * 60,
//////            remainingSeconds = settings.minutes * 60,
//////            isRunning = true,
//////            whiteNoiseOn = settings.ambientSound,
//////            completed = false
//////        )
//////        startTicking()
//////    }
//////
//////    fun togglePause() {
//////        val wasRunning = _uiState.value.isRunning
//////        _uiState.update { it.copy(isRunning = !wasRunning) }
//////        if (!wasRunning) startTicking() else tickJob?.cancel()
//////    }
//////
//////    fun toggleWhiteNoise() {
//////        _uiState.update { it.copy(whiteNoiseOn = !it.whiteNoiseOn) }
//////    }
//////
//////    /** User pressed End or Back — cancels, does NOT mark as completed. */
//////    fun end() {
//////        tickJob?.cancel()
//////        _uiState.update {
//////            it.copy(isRunning = false, remainingSeconds = 0, completed = false)
//////        }
//////        // Clear so a later toggleWhiteNoise etc. can't accidentally complete.
//////        habitId = null
//////        sessionId = null
//////    }
//////
//////    private fun startTicking() {
//////        tickJob?.cancel()
//////        tickJob = viewModelScope.launch {
//////            while (_uiState.value.remainingSeconds > 0) {
//////                delay(1_000)
//////                if (!_uiState.value.isRunning) continue
//////                _uiState.update {
//////                    it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0))
//////                }
//////            }
//////
//////            // Reached 0 by itself -> mark completed and check the session off.
//////            _uiState.update { it.copy(isRunning = false, completed = true) }
//////
//////            val h = habitId
//////            val s = sessionId
//////            if (h != null && s != null) {
//////                // This flips `isDone` in the sessions table. Stats (and any
//////                // other screen reading HabitRepository.habits) updates live.
//////                HabitRepository.toggleSession(h, s)
//////            }
//////        }
//////    }
//////
//////    override fun onCleared() {
//////        tickJob?.cancel()
//////        super.onCleared()
//////    }
//////}
////
////
////
////package com.example.disipline.Viewmodel
////
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import com.example.disipline.UI.FocusSettings
////import com.example.disipline.UI.FocusTimerUiState
////import com.example.disipline.data.reposotry.HabitRepository
////import kotlinx.coroutines.Job
////import kotlinx.coroutines.delay
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.flow.update
////import kotlinx.coroutines.launch
////
////class FocusTimerViewModel : ViewModel() {
////
////    private val _uiState = MutableStateFlow(
////        FocusTimerUiState(
////            taskName = "",
////            totalSeconds = 25 * 60,
////            remainingSeconds = 25 * 60,
////            isRunning = false,
////            whiteNoiseOn = false,
////            completed = false
////        )
////    )
////    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()
////
////    private var tickJob: Job? = null
////
////    // The session this timer belongs to (null when started without a session).
////    private var habitId: Int? = null
////    private var sessionId: Int? = null
////
////    fun start(
////        settings: FocusSettings,
////        habitId: Int? = null,
////        sessionId: Int? = null
////    ) {
////        this.habitId = habitId
////        this.sessionId = sessionId
////
////        tickJob?.cancel()
////        _uiState.value = FocusTimerUiState(
////            taskName = settings.task.ifBlank { settings.mode.label },
////            totalSeconds = settings.minutes * 60,
////            remainingSeconds = settings.minutes * 60,
////            isRunning = true,
////            whiteNoiseOn = settings.ambientSound,
////            completed = false
////        )
////        startTicking()
////    }
////
////    fun togglePause() {
////        val wasRunning = _uiState.value.isRunning
////        _uiState.update { it.copy(isRunning = !wasRunning) }
////        if (!wasRunning) startTicking() else tickJob?.cancel()
////    }
////
////    fun toggleWhiteNoise() {
////        _uiState.update { it.copy(whiteNoiseOn = !it.whiteNoiseOn) }
////    }
////
////    /** User pressed End or Back — cancels, does NOT mark as completed. */
////    fun end() {
////        tickJob?.cancel()
////        _uiState.update {
////            it.copy(isRunning = false, remainingSeconds = 0, completed = false)
////        }
////        habitId = null
////        sessionId = null
////    }
////
////    private fun startTicking() {
////        tickJob?.cancel()
////        tickJob = viewModelScope.launch {
////            while (_uiState.value.remainingSeconds > 0) {
////                delay(1_000)
////                if (!_uiState.value.isRunning) continue
////                _uiState.update {
////                    it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0))
////                }
////            }
////
////            // Reached 0 on its own -> check the session, then flag completed for the UI.
////            markSessionDone()
////            _uiState.update { it.copy(isRunning = false, completed = true) }
////        }
////    }
////
////    /**
////     * One-way "mark done". Only toggles if the session isn't already isDone,
////     * so completing the same session twice never un-checks it.
////     */
////    private fun markSessionDone() {
////        val h = habitId ?: return
////        val s = sessionId ?: return
////
////        val session = HabitRepository.habits.value
////            .firstOrNull { it.id == h }
////            ?.sessions?.firstOrNull { it.id == s }
////
////        if (session != null && !session.isDone) {
////            HabitRepository.toggleSession(h, s)
////        }
////    }
////
////    override fun onCleared() {
////        tickJob?.cancel()
////        super.onCleared()
////    }
////}
//
//
//package com.example.disipline.Viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.disipline.UI.FocusSettings
//import com.example.disipline.UI.FocusTimerUiState
//import com.example.disipline.data.reposotry.HabitRepository
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
//class FocusTimerViewModel : ViewModel() {
//
//    private val _uiState = MutableStateFlow(
//        FocusTimerUiState(
//            taskName = "",
//            totalSeconds = 25 * 60,
//            remainingSeconds = 25 * 60,
//            isRunning = false,
//            whiteNoiseOn = false,
//            completed = false
//        )
//    )
//    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()
//
//    private var tickJob: Job? = null
//
//    // The session this timer belongs to (null when started without a session).
//    private var habitId: Int? = null
//    private var sessionId: Int? = null
//
//    fun start(
//        settings: FocusSettings,
//        habitId: Int? = null,
//        sessionId: Int? = null
//    ) {
//        this.habitId = habitId
//        this.sessionId = sessionId
//
//        tickJob?.cancel()
//        _uiState.value = FocusTimerUiState(
//            taskName = settings.task.ifBlank { settings.mode.label },
//            totalSeconds = settings.minutes * 60,
//            remainingSeconds = settings.minutes * 60,
//            isRunning = true,
//            whiteNoiseOn = settings.ambientSound,
//            completed = false
//        )
//        startTicking()
//    }
//
//    fun togglePause() {
//        val wasRunning = _uiState.value.isRunning
//        _uiState.update { it.copy(isRunning = !wasRunning) }
//        if (!wasRunning) startTicking() else tickJob?.cancel()
//    }
//
//    fun toggleWhiteNoise() {
//        _uiState.update { it.copy(whiteNoiseOn = !it.whiteNoiseOn) }
//    }
//
//    /** User pressed End or Back — cancels, does NOT mark as completed. */
//    fun end() {
//        tickJob?.cancel()
//        _uiState.update {
//            it.copy(isRunning = false, remainingSeconds = 0, completed = false)
//        }
//        habitId = null
//        sessionId = null
//    }
//
//    private fun startTicking() {
//        tickJob?.cancel()
//        tickJob = viewModelScope.launch {
//            while (_uiState.value.remainingSeconds > 0) {
//                delay(1_000)
//                if (!_uiState.value.isRunning) continue
//                _uiState.update {
//                    it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0))
//                }
//            }
//
//            // Reached 0 on its own -> check the session, then flag completed for the UI.
//            markSessionDone()
//            _uiState.update { it.copy(isRunning = false, completed = true) }
//        }
//    }
//
//    /**
//     * One-way "mark done". Only toggles if the session isn't already isDone,
//     * so completing the same session twice never un-checks it.
//     */
//    private fun markSessionDone() {
//        val h = habitId ?: return
//        val s = sessionId ?: return
//
//        val session = HabitRepository.habits.value
//            .firstOrNull { it.id == h }
//            ?.sessions?.firstOrNull { it.id == s }
//
//        if (session != null && !session.isDone) {
//            HabitRepository.toggleSession(h, s)
//        }
//    }
//
//    override fun onCleared() {
//        tickJob?.cancel()
//        super.onCleared()
//    }
//}


package com.example.disipline.Viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disipline.UI.FocusSettings
import com.example.disipline.UI.FocusTimerUiState
import com.example.disipline.data.reposotry.HabitRepository
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
            whiteNoiseOn = false,
            completed = false
        )
    )
    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null

    // The session this timer belongs to (null when started without a session).
    private var habitId: Int? = null
    private var sessionId: Int? = null

    fun start(
        settings: FocusSettings,
        habitId: Int? = null,
        sessionId: Int? = null
    ) {
        this.habitId = habitId
        this.sessionId = sessionId

        // Block only the apps ticked on the Focus screen
        BlockerState.blockedPackages =
            if (settings.blockApps) settings.blockedApps.toSet() else emptySet()
        BlockerState.active = settings.blockApps

        tickJob?.cancel()
        _uiState.value = FocusTimerUiState(
            taskName = settings.task.ifBlank { settings.mode.label },
            totalSeconds = settings.minutes * 60,
            remainingSeconds = settings.minutes * 60,
            isRunning = true,
            whiteNoiseOn = settings.ambientSound,
            completed = false
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

    /** User pressed End or Back: cancels, does NOT mark as completed. */
    fun end() {
        tickJob?.cancel()
        BlockerState.active = false
        _uiState.update {
            it.copy(isRunning = false, remainingSeconds = 0, completed = false)
        }
        habitId = null
        sessionId = null
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

            // Reached 0 on its own -> check the session, stop blocking, flag completed.
            markSessionDone()
            BlockerState.active = false
            _uiState.update { it.copy(isRunning = false, completed = true) }
        }
    }

    /**
     * One-way "mark done". Only toggles if the session isn't already isDone,
     * so completing the same session twice never un-checks it.
     */
    private fun markSessionDone() {
        val h = habitId ?: return
        val s = sessionId ?: return

        val session = HabitRepository.habits.value
            .firstOrNull { it.id == h }
            ?.sessions?.firstOrNull { it.id == s }

        if (session != null && !session.isDone) {
            HabitRepository.toggleSession(h, s)
        }
    }

    override fun onCleared() {
        tickJob?.cancel()
        BlockerState.active = false
        super.onCleared()
    }
}