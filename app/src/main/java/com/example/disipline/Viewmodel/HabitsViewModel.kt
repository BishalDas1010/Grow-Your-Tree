package com.example.disipline
import androidx.lifecycle.ViewModel
import com.example.disipline.UI.Habits.habit_screen.Habit
import com.example.disipline.data.reposotry.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HabitsViewModel : ViewModel() {

    // The data lives in the repository, shared with the Home screen.
    val habits: StateFlow<List<Habit>> = HabitRepository.habits

    private val _sections = MutableStateFlow(listOf("Study", "Health", "Personal"))
    val sections: StateFlow<List<String>> = _sections.asStateFlow()

    fun addSection(name: String) {
        _sections.update { current ->
            if (current.any { it.equals(name, ignoreCase = true) }) current else current + name
        }
    }

    fun addHabit(title: String, emoji: String, category: String, target: Int) =
        HabitRepository.addHabit(title, emoji, category, target)

    fun deleteHabit(habitId: Int) {
        // Read everything BEFORE deleting
        val current = habits.value
        val category = current.firstOrNull { it.id == habitId }?.category

        // Is any OTHER habit still using this section?
        val stillUsed = category != null && current.any {
            it.id != habitId && it.category.trim().equals(category.trim(), ignoreCase = true)
        }

        HabitRepository.deleteHabit(habitId)

        // Remove the section if the deleted habit was the last one in it
        if (category != null && !stillUsed) {
            _sections.update { sections ->
                sections.filterNot { it.trim().equals(category.trim(), ignoreCase = true) }
            }
        }
    }
    fun addSession(
        habitId: Int,
        name: String,
        start: String,
        end: String,
        goal: String,
        date: String
    ) = HabitRepository.addSession(habitId, name, start, end, goal, date)

    fun toggleSession(habitId: Int, sessionId: Int) =
        HabitRepository.toggleSession(habitId, sessionId)

    // NEW: marks a session as done, but never un-checks it.
    // Call this only when the timer reaches zero.
    fun completeSession(habitId: Int, sessionId: Int) {
        val session = habits.value
            .firstOrNull { it.id == habitId }
            ?.sessions?.firstOrNull { it.id == sessionId }

        if (session != null && !session.isDone) {
            HabitRepository.toggleSession(habitId, sessionId)
        }
    }

    fun deleteSession(habitId: Int, sessionId: Int) =
        HabitRepository.deleteSession(habitId, sessionId)
}