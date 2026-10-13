package com.example.disipline.UI.Habits

import com.example.disipline.UI.Habits.habit_screen.Habit
import com.example.disipline.data.Session


val sampleHabits = listOf(
    Habit(
        1, "Study DSA", "🌱", "Study", 2, 3,
        sessions = listOf(
            Session(
                1,
                "Learn DSA concepts",
                "08:00 AM",
                "09:00 AM",
                "Read notes and watch lecture",
                true,
                date = "12 Oct 2026"
            ),
            Session(
                2,
                "Solve coding problems",
                "10:00 AM",
                "11:00 AM",
                "Solve at least 3 problems",
                true,
                date = "12 Oct 2026"
            ),
            Session(
                3,
                "Revision",
                "02:00 PM",
                "03:00 PM",
                "Revise important topics",
                date = "12 Oct 2026"
            )
        )
    )
)
