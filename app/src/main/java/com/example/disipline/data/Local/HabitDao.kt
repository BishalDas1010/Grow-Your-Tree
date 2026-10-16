package com.example.disipline.data.Local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HabitDao {

    // Emits a new list every time the tables change.
    @Transaction
    @Query("SELECT * FROM habits ORDER BY id")
    abstract fun observeHabits(): Flow<List<HabitWithSessions>>

    @Query("SELECT name FROM sections ORDER BY rowid")
    abstract fun observeSections(): Flow<List<String>>

    @Insert
    abstract suspend fun insertHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    abstract suspend fun deleteHabit(habitId: Int)

    @Insert
    abstract suspend fun insertSessionRow(session: SessionEntity)

    // The daily target never drops below the number of sessions (same rule as before).
    @Query(
        "UPDATE habits SET targetSessions = " +
                "MAX(targetSessions, (SELECT COUNT(*) FROM sessions WHERE habitId = :habitId)) " +
                "WHERE id = :habitId"
    )
    abstract suspend fun raiseTarget(habitId: Int)

    @Transaction
    open suspend fun insertSession(session: SessionEntity) {
        insertSessionRow(session)
        raiseTarget(session.habitId)
    }

    @Query("UPDATE sessions SET isDone = NOT isDone WHERE id = :sessionId")
    abstract suspend fun toggleSession(sessionId: Int)

    @Query("UPDATE sessions SET isDone = 1, endTime = :endTime WHERE id = :sessionId")
    abstract suspend fun completeSession(sessionId: Int, endTime: String)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    abstract suspend fun deleteSession(sessionId: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertSection(section: SectionEntity)
}