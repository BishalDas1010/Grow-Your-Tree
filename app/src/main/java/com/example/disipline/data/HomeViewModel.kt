package com.example.disipline.data


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disipline.UI.Goal
import com.example.disipline.UI.Habits.habit_screen.Habit
import com.example.disipline.data.HomeUiState
import com.example.disipline.data.reposotry.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel : ViewModel() {

    // Rebuilt every time the habits change (add, tick, delete).
    val uiState: StateFlow<HomeUiState> = HabitRepository.habits
        .map { habits -> buildState(habits) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildState(HabitRepository.habits.value)
        )

    private fun buildState(habits: List<Habit>): HomeUiState {
        val doneSessions = habits.flatMap { it.sessions }.filter { it.isDone }

        val doneMinutes = doneSessions.sumOf { session ->
            val start = toMinutes(session.startTime)
            val end = toMinutes(session.endTime)
            if (start != null && end != null && end > start) end - start else 0
        }

        return HomeUiState(
            userName = USER_NAME,
            quote = QUOTE,
            focusSessions = doneSessions.size,
            focusTime = formatDuration(doneMinutes),
            treesGrown = doneSessions.size,   // one tree per completed session
            // One goal per habit; done when the habit reached its daily target
            goals = habits.map { Goal(id = it.id, title = it.title, done = it.isDone) }
        )
    }

    // "08:00 AM" / "02:30 PM" -> minutes since midnight (null if the text is odd)
    private fun toMinutes(text: String): Int? {
        val parts = text.trim().split(" ")
        if (parts.size != 2) return null
        val hm = parts[0].split(":")
        if (hm.size != 2) return null
        val hour = hm[0].toIntOrNull() ?: return null
        val minute = hm[1].toIntOrNull() ?: return null
        val isPm = parts[1].equals("PM", ignoreCase = true)
        return ((hour % 12) + if (isPm) 12 else 0) * 60 + minute
    }

    private fun formatDuration(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    private companion object {
        const val USER_NAME = "Alex"   // change to the real user name later
        const val QUOTE = "\"Discipline today creates the life you want tomorrow.\""
    }
}