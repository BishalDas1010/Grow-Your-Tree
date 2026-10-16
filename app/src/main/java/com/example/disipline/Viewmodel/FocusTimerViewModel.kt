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
import java.util.Locale

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
    private var currentSettings: FocusSettings? = null

    fun start(
        settings: FocusSettings,
        habitId: Int? = null,
        sessionId: Int? = null
    ) {
        this.habitId = habitId
        this.sessionId = sessionId
        this.currentSettings = settings

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
        currentSettings = null
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
     * Updates the endTime in the database based on the actual focus minutes completed.
     */
    private fun markSessionDone() {
        val h = habitId ?: return
        val s = sessionId ?: return

        val session = HabitRepository.habits.value
            .firstOrNull { it.id == h }
            ?.sessions?.firstOrNull { it.id == s }

        if (session != null && !session.isDone) {
            val actualMins = currentSettings?.minutes ?: 25
            val newEndTime = calculateEndTime(session.startTime, actualMins)
            HabitRepository.completeSession(s, newEndTime)
        }
    }

    private fun calculateEndTime(startTime: String, durationMinutes: Int): String {
        val startMins = parseTime(startTime) ?: return startTime
        val endMins = (startMins + durationMinutes) % (24 * 60)
        return formatTimeFromMinutes(endMins)
    }

    private fun parseTime(text: String): Int? {
        val parts = text.trim().split(" ")
        if (parts.size != 2) return null
        val hm = parts[0].split(":")
        if (hm.size != 2) return null
        val hour = hm[0].toIntOrNull() ?: return null
        val minute = hm[1].toIntOrNull() ?: return null
        val isPm = parts[1].equals("PM", ignoreCase = true)
        return ((hour % 12) + if (isPm) 12 else 0) * 60 + minute
    }

    private fun formatTimeFromMinutes(minutesSinceMidnight: Int): String {
        val hours24 = (minutesSinceMidnight / 60) % 24
        val mins = minutesSinceMidnight % 60
        val isPm = hours24 >= 12
        val hours12 = when (val h = hours24 % 12) {
            0 -> 12
            else -> h
        }
        val amPm = if (isPm) "PM" else "AM"
        return String.format(Locale.US, "%02d:%02d %s", hours12, mins, amPm)
    }

    override fun onCleared() {
        tickJob?.cancel()
        BlockerState.active = false
        super.onCleared()
    }
}