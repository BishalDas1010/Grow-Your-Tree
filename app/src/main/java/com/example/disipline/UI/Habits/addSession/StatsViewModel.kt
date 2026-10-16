package com.example.disipline.UI.Habits.addSession


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disipline.UI.Stas.BarData
import com.example.disipline.UI.Stas.StatTileData
import com.example.disipline.UI.Stas.StatsPageData
import com.example.disipline.UI.Stas.StatsPeriod
import com.example.disipline.data.Session
import com.example.disipline.data.reposotry.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

/**
 * Aggregates the existing `sessions` table into what the Statistics screen shows.
 *
 * No DB schema changes: we reuse HabitRepository.habits, which already
 * emits every habit with its full session list.
 */
class StatsViewModel : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.Week)
    val period: StateFlow<StatsPeriod> = _period.asStateFlow()

    /** Emits a fresh page when the tab changes OR any session changes. */
    val page: StateFlow<StatsPageData> = combine(
        HabitRepository.habits,
        _period
    ) { habits, p ->
        habits.flatMap { it.sessions }.toPageData(p)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsPageData.EMPTY
    )

    fun setPeriod(p: StatsPeriod) {
        if (_period.value != p) _period.value = p
    }
}

// =====================================================================
// Aggregation — pure functions on List<Session>
// =====================================================================

private const val DEFAULT_SESSION_MINUTES = 25

/** Duration in minutes from the "08:00 AM" strings. Falls back to 25m. */
private fun Session.durationMinutes(): Int {
    val s = parseTime(startTime) ?: return DEFAULT_SESSION_MINUTES
    val e = parseTime(endTime) ?: return DEFAULT_SESSION_MINUTES
    return if (e > s) e - s else DEFAULT_SESSION_MINUTES
}

/** "08:00 AM" -> minutes since midnight. */
private fun parseTime(text: String?): Int? {
    if (text.isNullOrBlank()) return null
    val parts = text.trim().split(" ")
    if (parts.size != 2) return null
    val hm = parts[0].split(":")
    if (hm.size != 2) return null
    val hour = hm[0].toIntOrNull() ?: return null
    val minute = hm[1].toIntOrNull() ?: return null
    val isPm = parts[1].equals("PM", ignoreCase = true)
    return ((hour % 12) + if (isPm) 12 else 0) * 60 + minute
}

/** Best-effort date parse for the many formats the app might store. */
private val dateFormats = listOf(
    "dd MMM yyyy",     // 12 Oct 2026
    "MMM dd, yyyy",    // Oct 12, 2026
    "yyyy-MM-dd",      // 2026-10-12
    "dd/MM/yyyy",
    "MM/dd/yyyy"
)

private fun parseDate(text: String?): Long? {
    if (text.isNullOrBlank()) return null
    val clean = text.trim()
    for (fmt in dateFormats) {
        try {
            val d = SimpleDateFormat(fmt, Locale.US).apply { isLenient = false }
                .parse(clean) ?: continue
            return d.time
        } catch (_: Exception) { /* try next */ }
    }
    return null
}

private fun startOfDay(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun List<Session>.toPageData(period: StatsPeriod): StatsPageData {
    val done = filter { it.isDone }

    val totalMinutes = done.sumOf { it.durationMinutes() }
    val count = done.size
    val avg = if (count == 0) 0 else totalMinutes / count
    val goalsDone = done.count { it.goal.isNotBlank() }
    val streak = computeStreak(done)

    return StatsPageData(
        dateRange = formatRange(period),
        totalMinutes = totalMinutes,
        totalTrend = null,
        bars = bucketIntoBars(done, period),
        tiles = listOf(
            StatTileData("Focus Sessions", count),
            StatTileData("Avg. Session", avg, suffix = " min"),
            StatTileData("Completed Goals", goalsDone),
            StatTileData(
                "Current Streak", streak,
                suffix = if (streak == 1) " day" else " days",
                emoji = "🔥"
            )
        )
    )
}

// ---------- Bucketing ----------

private fun bucketIntoBars(
    sessions: List<Session>,
    period: StatsPeriod
): List<BarData> {
    val cal = Calendar.getInstance()

    // 1) Pre-create buckets so empty slots still show a stub bar.
    val buckets: LinkedHashMap<String, Int> = linkedMapOf()
    when (period) {
        StatsPeriod.Day -> {
            // 6-hour windows over today
            buckets["12a"] = 0; buckets["6a"] = 0
            buckets["12p"] = 0; buckets["6p"] = 0
        }
        StatsPeriod.Week -> {
            // Current week, Monday-first
            val c = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val dow = c.get(Calendar.DAY_OF_WEEK)
            val offset = (dow + 5) % 7
            c.add(Calendar.DAY_OF_MONTH, -offset)
            for (i in 0 until 7) {
                val label = SimpleDateFormat("EEE", Locale.US).format(c.time)
                buckets[label] = 0
                c.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        StatsPeriod.Month -> {
            buckets["Wk 1"] = 0; buckets["Wk 2"] = 0
            buckets["Wk 3"] = 0; buckets["Wk 4"] = 0; buckets["Wk 5"] = 0
        }
        StatsPeriod.Year -> {
            val fmt = SimpleDateFormat("MMM", Locale.US)
            for (m in 0 until 12) {
                cal.set(Calendar.MONTH, m)
                buckets[fmt.format(cal.time)] = 0
            }
        }
    }

    // 2) Assign each session to its bucket.
    val now = System.currentTimeMillis()
    val today0 = startOfDay(now)

    sessions.forEach { s ->
        val millis = parseDate(s.date) ?: return@forEach
        val key = when (period) {
            StatsPeriod.Day -> {
                if (startOfDay(millis) != today0) return@forEach
                val hour = Calendar.getInstance().apply { timeInMillis = millis }
                    .get(Calendar.HOUR_OF_DAY)
                // Use startTime's hour if the date is just a date string.
                val hourFromTime = parseTime(s.startTime)?.let { it / 60 } ?: hour
                when {
                    hourFromTime < 6 -> "12a"
                    hourFromTime < 12 -> "6a"
                    hourFromTime < 18 -> "12p"
                    else -> "6p"
                }
            }
            StatsPeriod.Week -> {
                if (millis < today0 - 6 * 86_400_000L) return@forEach
                SimpleDateFormat("EEE", Locale.US).format(Date(millis))
            }
            StatsPeriod.Month -> {
                val c = Calendar.getInstance().apply { timeInMillis = millis }
                if (c.get(Calendar.MONTH) != Calendar.getInstance().get(Calendar.MONTH) ||
                    c.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)
                ) return@forEach
                "Wk ${(c.get(Calendar.DAY_OF_MONTH) - 1) / 7 + 1}"
            }
            StatsPeriod.Year -> {
                val c = Calendar.getInstance().apply { timeInMillis = millis }
                if (c.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))
                    return@forEach
                SimpleDateFormat("MMM", Locale.US).format(Date(millis))
            }
        } ?: return@forEach

        if (buckets.containsKey(key)) {
            buckets[key] = (buckets[key] ?: 0) + s.durationMinutes()
        }
    }

    // 3) Normalize to 0..1 using the max bucket.
    val max = buckets.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    return buckets.entries.map { (label, minutes) ->
        BarData(
            label = label,
            value = minutes.toFloat() / max.toFloat(),
            isHighlighted = minutes == max && minutes > 0
        )
    }
}

// ---------- Streak ----------

/** Consecutive days ending today (or yesterday) with at least one done session. */
private fun computeStreak(done: List<Session>): Int {
    if (done.isEmpty()) return 0
    val days = done.mapNotNull { parseDate(it.date) }
        .map { startOfDay(it) }
        .distinct()
        .sortedDescending()

    val today = startOfDay(System.currentTimeMillis())
    val oneDay = 86_400_000L
    if (days.first() < today - oneDay) return 0

    var streak = 1
    var prev = days.first()
    for (i in 1 until days.size) {
        if (days[i] == prev - oneDay) { streak++; prev = days[i] } else break
    }
    return streak
}

// ---------- Formatting ----------

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

private fun formatRange(period: StatsPeriod): String {
    val cal = Calendar.getInstance()
    return when (period) {
        StatsPeriod.Day ->
            SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(cal.time)

        StatsPeriod.Week -> {
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            cal.add(Calendar.DAY_OF_MONTH, -((dow + 5) % 7))
            val start = cal.time
            cal.add(Calendar.DAY_OF_MONTH, 6)
            val end = cal.time
            val fmt = SimpleDateFormat("MMM d", Locale.US)
            "${fmt.format(start)} - ${fmt.format(end)}"
        }

        StatsPeriod.Month ->
            SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)

        StatsPeriod.Year ->
            SimpleDateFormat("yyyy", Locale.US).format(cal.time)
    }
}