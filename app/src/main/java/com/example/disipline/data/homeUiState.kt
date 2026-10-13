package com.example.disipline.data

import com.example.disipline.UI.Goal

data class HomeUiState(
    val userName: String,           // "Alex"
    val quote: String,              // motivational text in the top card
    val focusSessions: Int,         // sessions completed today
    val focusTime: String,          // already formatted, e.g. "2h 15m"
    val treesGrown: Int,            // total trees
    val goals: List<Goal>           // today's goals
)