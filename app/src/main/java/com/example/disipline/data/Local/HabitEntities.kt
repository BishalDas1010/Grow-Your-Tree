package com.example.disipline.data.Local


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val emoji: String,
    val category: String,
    val targetSessions: Int
)

// Sessions are deleted automatically when their habit is deleted (CASCADE).
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val name: String,
    val startTime: String,
    val endTime: String,
    val goal: String,
    val isDone: Boolean,
    val date: String
)

// Sections the user added (the three default ones are not stored).
@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey val name: String
)

// A habit together with its sessions, read in one query.
data class HabitWithSessions(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumn = "id", entityColumn = "habitId")
    val sessions: List<SessionEntity>
)