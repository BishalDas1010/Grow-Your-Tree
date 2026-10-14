package com.example.disipline.UI.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disipline.data.Session
import com.example.disipline.data.Local.profile.BlockedAppEntity
import com.example.disipline.data.Local.profile.ProfileEntity
import com.example.disipline.data.reposotry.HabitRepository
import com.example.disipline.data.reposotry.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The four numbers shown in the Profile screen chips. */
data class ProfileStats(
    val focusSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val habitsTracked: Int = 0,
    val treesGrown: Int = 0
) {
    /** "4h 30m" / "45m" / "2h" — ready to drop straight into the Total Focus chip. */
    val totalFocusLabel: String
        get() {
            val h = totalFocusMinutes / 60
            val m = totalFocusMinutes % 60
            return when {
                h == 0 -> "${m}m"
                m == 0 -> "${h}h"
                else -> "${h}h ${m}m"
            }
        }
}

class ProfileViewModel : ViewModel() {

    val profile: StateFlow<ProfileEntity?> = ProfileRepository.profile
    val blockedApps: StateFlow<List<BlockedAppEntity>> = ProfileRepository.blockedApps

    /**
     * Derived from the same habits flow that powers the Stats screen.
     * Whenever a session is marked done, this re-emits and the chips update.
     */
    val stats: StateFlow<ProfileStats> = HabitRepository.habits
        .map { habits ->
            val done = habits.flatMap { it.sessions }.filter { it.isDone }
            ProfileStats(
                focusSessions = done.size,
                totalFocusMinutes = done.sumOf { it.durationMinutes() },
                habitsTracked = habits.size,
                treesGrown = done.size          // one tree per completed session
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileStats()
        )

    // ---- existing methods, unchanged ----

    fun updateName(name: String) = ProfileRepository.updateName(name)
    fun updateSubtitle(subtitle: String) = ProfileRepository.updateSubtitle(subtitle)
    fun updateQuote(quote: String) = ProfileRepository.updateQuote(quote)
    fun updateAvatar(uri: Uri) = ProfileRepository.updateAvatarFromUri(uri)
    fun clearAvatar() = ProfileRepository.clearAvatar()

    fun addBlockedApp(packageName: String, appName: String) =
        ProfileRepository.addBlockedApp(packageName, appName)

    fun removeBlockedApp(packageName: String) =
        ProfileRepository.removeBlockedApp(packageName)
}

// ---- Helpers ----

/** Duration in minutes from the "08:00 AM" strings. Falls back to 25m. */
private fun Session.durationMinutes(): Int {
    val s = parseTime(startTime) ?: return 25
    val e = parseTime(endTime) ?: return 25
    return if (e > s) e - s else 25
}

private fun parseTime(text: String): Int? {
    val parts = text.trim().split(" ")
    if (parts.size != 2) return null
    val hm = parts[0].split(":")
    if (hm.size != 2) return null
    val hour = hm[0].toIntOrNull() ?: return null
    val minute = hm[1].toIntOrNull() ?: return null
    val isPm = parts[1].equals("PM", ignoreCase = true)
    return ((hour % 12) + if (isPm) 12 else 0) * 60 + minute
}