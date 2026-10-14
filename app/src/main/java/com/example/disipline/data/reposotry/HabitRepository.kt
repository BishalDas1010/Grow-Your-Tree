package com.example.disipline.data.reposotry

import com.example.disipline.UI.Habits.sampleHabits
import com.example.disipline.data.Session


import com.example.disipline.UI.Habits.habit_screen.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
//
//// In-memory "database" shared by every ViewModel.
//// Later: replace the inside of this object with Room, keep the same functions.
//object HabitRepository {
//
//    private val _habits = MutableStateFlow(sampleHabits)
//    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()
//
//    fun addHabit(title: String, emoji: String, category: String, target: Int) {
//        _habits.update { list ->
//            val newHabit = Habit(
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
//        _habits.update { list -> list.filter { it.id != habitId } }
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






import android.content.Context
import com.example.disipline.data.Local.HabitDao
import com.example.disipline.data.Local.HabitDatabase
import com.example.disipline.data.Local.HabitEntity
import com.example.disipline.data.Local.HabitWithSessions
import com.example.disipline.data.Local.SectionEntity
import com.example.disipline.data.Local.SessionEntity


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Offline database (Room / SQLite) shared by every ViewModel.
// Call HabitRepository.init(context) once, before any ViewModel uses it.
object HabitRepository {

    private lateinit var dao: HabitDao
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        if (!::dao.isInitialized) {
            dao = HabitDatabase.get(context).habitDao()
        }
    }

    // Updates by itself whenever the database changes.
    val habits: StateFlow<List<Habit>> by lazy {
        dao.observeHabits()
            .map { rows -> rows.map { it.toHabit() } }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    // Only the sections the user added.
    val customSections: StateFlow<List<String>> by lazy {
        dao.observeSections()
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    fun addSection(name: String) {
        scope.launch { dao.insertSection(SectionEntity(name)) }
    }

    fun addHabit(title: String, emoji: String, category: String, target: Int) {
        scope.launch {
            dao.insertHabit(
                HabitEntity(
                    title = title,
                    emoji = emoji,
                    category = category,
                    targetSessions = target
                )
            )
        }
    }

    // Removes the whole habit; its sessions are deleted with it.
    fun deleteHabit(habitId: Int) {
        scope.launch { dao.deleteHabit(habitId) }
    }

    fun addSession(
        habitId: Int,
        name: String,
        start: String,
        end: String,
        goal: String,
        date: String
    ) {
        scope.launch {
            dao.insertSession(
                SessionEntity(
                    habitId = habitId,
                    name = name,
                    startTime = start,
                    endTime = end,
                    goal = goal,
                    isDone = false,
                    date = date
                )
            )
        }
    }

    // Session ids are unique across the whole database, so habitId is not needed here.
    fun toggleSession(habitId: Int, sessionId: Int) {
        scope.launch { dao.toggleSession(sessionId) }
    }

    fun deleteSession(habitId: Int, sessionId: Int) {
        scope.launch { dao.deleteSession(sessionId) }
    }

    // Database row -> the Habit the screens already use.
    private fun HabitWithSessions.toHabit(): Habit {
        val list = sessions.sortedBy { it.id }.map {
            Session(
                id = it.id,
                name = it.name,
                startTime = it.startTime,
                endTime = it.endTime,
                goal = it.goal,
                isDone = it.isDone,
                date = it.date
            )
        }
        return Habit(
            id = habit.id,
            title = habit.title,
            emoji = habit.emoji,
            category = habit.category,
            completedSessions = list.count { it.isDone },
            targetSessions = maxOf(habit.targetSessions, list.size),
            sessions = list
        )
    }
}