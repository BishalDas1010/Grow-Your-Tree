package com.example.disipline.Viewmodel


import androidx.lifecycle.ViewModel
import com.example.disipline.UI.Habits.habit_screen.formatTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

data class AddSessionUiState(
    val name: String = "",
    val goal: String = "",
    val startMinutes: Int = 8 * 60,                       // 08:00 AM
    val endMinutes: Int = 9 * 60,                         // 09:00 AM
    val dateMillis: Long = System.currentTimeMillis()     // today
) {
    val timeOk: Boolean get() = endMinutes > startMinutes
    val canSave: Boolean get() = name.isNotBlank() && timeOk
    val startText: String get() = formatTime(startMinutes)
    val endText: String get() = formatTime(endMinutes)
    val dateText: String get() = formatDate(dateMillis)
}

class AddSessionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddSessionUiState())
    val uiState: StateFlow<AddSessionUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onGoalChange(value: String) = _uiState.update { it.copy(goal = value) }

    fun onStartChange(minutes: Int) = _uiState.update { it.copy(startMinutes = minutes) }

    fun onEndChange(minutes: Int) = _uiState.update { it.copy(endMinutes = minutes) }

    fun onDateChange(millis: Long) = _uiState.update { it.copy(dateMillis = millis) }

    // Clears the form after a session is saved.
    fun reset() {
        _uiState.value = AddSessionUiState()
    }
}