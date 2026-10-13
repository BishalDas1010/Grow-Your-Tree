package com.example.disipline.data.reposotry

import com.example.disipline.UI.Habits.sampleHabits
import com.example.disipline.data.Session


import com.example.disipline.UI.Habits.habit_screen.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// In-memory "database" shared by every ViewModel.
// Later: replace the inside of this object with Room, keep the same functions.
object HabitRepository {

    private val _habits = MutableStateFlow(sampleHabits)
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    fun addHabit(title: String, emoji: String, category: String, target: Int) {
        _habits.update { list ->
            val newHabit = Habit(
                id = (list.maxOfOrNull { it.id } ?: 0) + 1,
                title = title,
                emoji = emoji,
                category = category,
                completedSessions = 0,
                targetSessions = target
            )
            list + newHabit
        }
    }

    // Removes the whole habit and all of its sessions.
    fun deleteHabit(habitId: Int) {
        _habits.update { list -> list.filter { it.id != habitId } }
    }

    fun addSession(
        habitId: Int,
        name: String,
        start: String,
        end: String,
        goal: String,
        date: String
    ) {
        changeSessions(habitId) { sessions ->
            val session = Session(
                id = (sessions.maxOfOrNull { it.id } ?: 0) + 1,
                name = name,
                startTime = start,
                endTime = end,
                goal = goal,
                date = date
            )
            sessions + session
        }
    }

    fun toggleSession(habitId: Int, sessionId: Int) {
        changeSessions(habitId) { sessions ->
            sessions.map { if (it.id == sessionId) it.copy(isDone = !it.isDone) else it }
        }
    }

    fun deleteSession(habitId: Int, sessionId: Int) {
        changeSessions(habitId) { sessions -> sessions.filter { it.id != sessionId } }
    }

    private fun changeSessions(habitId: Int, change: (List<Session>) -> List<Session>) {
        _habits.update { list ->
            list.map { habit ->
                if (habit.id == habitId) habit.withSessions(change(habit.sessions)) else habit
            }
        }
    }

    // Replaces the sessions and recalculates the counts from them.
    private fun Habit.withSessions(newSessions: List<Session>): Habit = copy(
        sessions = newSessions,
        completedSessions = newSessions.count { it.isDone },
        targetSessions = maxOf(targetSessions, newSessions.size)
    )
}