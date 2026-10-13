package com.example.disipline.data

data class Session(
    val id: Int,
    val name: String,
    val startTime: String,         // e.g. "08:00 AM"
    val endTime: String,           // e.g. "09:00 AM"
    val goal: String,              // short description, can be empty
    val isDone: Boolean = false,
    val date: String = ""          // e.g. "12 Oct 2026"
) {
    val timeRange: String get() = "$startTime - $endTime"
}