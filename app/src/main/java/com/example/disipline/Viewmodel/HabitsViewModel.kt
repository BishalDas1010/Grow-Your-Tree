package com.example.disipline.Viewmodel
//
//import androidx.lifecycle.ViewModel
//import com.example.disipline.UI.Habits.habit_screen.Habit
//import com.example.disipline.UI.Habits.sampleHabits
//import com.example.disipline.data.Session
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlin.collections.listOf
//
//class HabitsViewModel : ViewModel() {
//
//    // Starts with the sample data. Replace with a repository / Room later.
//
//
//    //this portaion update  if new habit added or updated
//    private val _habits = MutableStateFlow(sampleHabits)
//    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()
//
//    //too add  the top row session
//
//    private val _sections = MutableStateFlow<List<String>>(emptyList())
//    val sections: StateFlow<List<String>> = _sections.asStateFlow()
//
//    fun addSection(name: String) {
//        _sections.update { current ->
//            if (current.any {
//                    it.equals(name, ignoreCase = true)
//            }) current else current + name
//        }
//    }
//
//    fun addHabit(
//        title: String,
//        emoji: String,
//        category: String,
//        target: Int
//    ) {
//        _habits.update { list ->
//            val newHabit = Habit(
//                // Generates a unique id
//                id = (list.maxOfOrNull { it.id } ?: 0) + 1,
//                title = title,
//                emoji = emoji,
//                category = category,
//                completedSessions = 0,
//                targetSessions = target
//            )
//            list + newHabit
//        }
//    }
//
//    // Removes the whole habit and all of its sessions.
//    fun deleteHabit(habitId: Int) {
//        _habits.update { list ->
//            list.filter { it.id != habitId }
//        }
//    }
//
//    fun addSession(
//        habitId: Int,
//        name: String,
//        start: String,
//        end: String,
//        goal: String,
//        date: String
//    ) {
//        changeSessions(habitId) { sessions ->
//            val session = Session(
//                id = (sessions.maxOfOrNull { it.id } ?: 0) + 1,
//                name = name,
//                startTime = start,
//                endTime = end,
//                goal = goal,
//                date = date
//            )
//            sessions + session
//        }
//    }
//
//    fun toggleSession(habitId: Int, sessionId: Int) {
//        changeSessions(habitId) { sessions ->
//            sessions.map { if (it.id == sessionId) it.copy(isDone = !it.isDone) else it }
//        }
//    }
//
//    fun deleteSession(habitId: Int, sessionId: Int) {
//        changeSessions(habitId) { sessions -> sessions.filter { it.id != sessionId } }
//    }
//
//    private fun changeSessions(habitId: Int, change: (List<Session>) -> List<Session>) {
//        _habits.update { list ->
//            list.map { habit ->
//                if (habit.id == habitId) habit.withSessions(change(habit.sessions)) else habit
//            }
//        }
//    }
//
//    // Replaces the sessions and recalculates the counts from them.
//    private fun Habit.withSessions(newSessions: List<Session>): Habit = copy(
//        sessions = newSessions,
//        completedSessions = newSessions.count { it.isDone },
//        targetSessions = maxOf(targetSessions, newSessions.size)
//    )
//}



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

    fun deleteHabit(habitId: Int) = HabitRepository.deleteHabit(habitId)

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

    fun deleteSession(habitId: Int, sessionId: Int) =
        HabitRepository.deleteSession(habitId, sessionId)
}