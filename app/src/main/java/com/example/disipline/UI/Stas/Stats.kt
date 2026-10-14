package com.example.disipline.UI.Stas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disipline.R
import com.example.disipline.data.Session
import com.example.disipline.data.reposotry.HabitRepository
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

// =====================================================================
// STATISTICS — single file
//
// 1. DATA        -> StatsPeriod, BarData, Trend, StatTileData, StatsPageData
// 2. VIEWMODEL   -> StatsViewModel (reads HabitRepository, no DB changes)
// 3. AGGREGATION -> private pure functions converting List<Session> -> page
// 4. ROUTE       -> StatisticsRoute() wires ViewModel to screen
// 5. SCREEN      -> StatisticsScreen() is the pure UI
// 6. UI PIECES   -> top bar, tabs, card, chart, grid, tile
// 7. PREVIEWS
//
// NO database changes. Reads the existing sessions table via HabitRepository.
// =====================================================================

// =====================================================================
// 1. DATA
// =====================================================================

enum class StatsPeriod(val label: String) {
    Day("Day"),
    Week("Week"),
    Month("Month"),
    Year("Year")
}

data class BarData(
    val label: String,
    val value: Float,                 // 0f..1f share of the tallest bar
    val isHighlighted: Boolean = false
)

data class Trend(val percent: Int, val isPositive: Boolean)

data class StatTileData(
    val label: String,
    val value: String,
    val emoji: String? = null,
    val trend: Trend? = null
)

data class StatsPageData(
    val dateRange: String,
    val totalFocusTime: String,
    val bars: List<BarData>,
    val tiles: List<StatTileData>
) {
    companion object {
        val EMPTY = StatsPageData(
            dateRange = "",
            totalFocusTime = "0m",
            bars = emptyList(),
            tiles = emptyList()
        )
    }
}

// =====================================================================
// 2. VIEWMODEL
// =====================================================================

class StatsViewModel : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.Week)
    val period: StateFlow<StatsPeriod> = _period.asStateFlow()

    /**
     * Re-emits whenever:
     *   - the selected period changes
     *   - any session in the DB changes (habits Flow is reactive)
     *
     * flowOn(Default) moves the aggregation off the main thread.
     * stateIn(WhileSubscribed) stops observing when the screen is gone.
     */
    val page: StateFlow<StatsPageData> = combine(
        HabitRepository.habits,
        _period
    ) { habits, p ->
        habits.flatMap { it.sessions }.toPageData(p)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsPageData.EMPTY
        )

    fun setPeriod(p: StatsPeriod) {
        if (_period.value != p) _period.value = p
    }
}

// =====================================================================
// 3. AGGREGATION — pure helpers on List<Session>
// =====================================================================

private const val DEFAULT_SESSION_MINUTES = 25

private fun Session.durationMinutes(): Int {
    val s = parseTime(startTime) ?: return DEFAULT_SESSION_MINUTES
    val e = parseTime(endTime) ?: return DEFAULT_SESSION_MINUTES
    return if (e > s) e - s else DEFAULT_SESSION_MINUTES
}

/** "08:00 AM" -> minutes since midnight. */
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

/** Best-effort date parse. Add formats as needed. */
private val dateFormats = listOf(
    "dd MMM yyyy",     // 12 Oct 2026
    "MMM dd, yyyy",    // Oct 12, 2026
    "yyyy-MM-dd",
    "dd/MM/yyyy",
    "MM/dd/yyyy"
)

private fun parseDate(text: String): Long? {
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
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
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
        totalFocusTime = formatDuration(totalMinutes),
        bars = bucketIntoBars(done, period),
        tiles = listOf(
            StatTileData("Focus Sessions", count.toString()),
            StatTileData("Avg. Session", "$avg min"),
            StatTileData("Completed Goals", goalsDone.toString()),
            StatTileData("Current Streak", "$streak days", emoji = "🔥")
        )
    )
}

// ---------- Bucketing ----------

private fun bucketIntoBars(
    sessions: List<Session>,
    period: StatsPeriod
): List<BarData> {
    val cal = Calendar.getInstance()

    // Pre-create buckets so empty slots still render a stub bar.
    val buckets: LinkedHashMap<String, Int> = linkedMapOf()
    when (period) {
        StatsPeriod.Day -> {
            buckets["12a"] = 0
            buckets["6a"] = 0
            buckets["12p"] = 0
            buckets["6p"] = 0
        }
        StatsPeriod.Week -> {
            val c = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val offset = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
            c.add(Calendar.DAY_OF_MONTH, -offset)
            val fmt = SimpleDateFormat("EEE", Locale.US)
            for (i in 0 until 7) {
                buckets[fmt.format(c.time)] = 0
                c.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        StatsPeriod.Month -> {
            for (w in 1..5) buckets["Wk $w"] = 0
        }
        StatsPeriod.Year -> {
            val fmt = SimpleDateFormat("MMM", Locale.US)
            for (m in 0 until 12) {
                cal.set(Calendar.MONTH, m)
                buckets[fmt.format(cal.time)] = 0
            }
        }
    }

    // Assign each session to its bucket.
    val today0 = startOfDay(System.currentTimeMillis())
    sessions.forEach { s ->
        val millis = parseDate(s.date) ?: return@forEach
        val key: String? = when (period) {
            StatsPeriod.Day -> {
                if (startOfDay(millis) != today0) return@forEach
                val hour = parseTime(s.startTime)?.let { it / 60 }
                    ?: Calendar.getInstance().apply { timeInMillis = millis }
                        .get(Calendar.HOUR_OF_DAY)
                when {
                    hour < 6 -> "12a"
                    hour < 12 -> "6a"
                    hour < 18 -> "12p"
                    else -> "6p"
                }
            }
            StatsPeriod.Week -> {
                if (millis < today0 - 6 * 86_400_000L) return@forEach
                SimpleDateFormat("EEE", Locale.US).format(Date(millis))
            }
            StatsPeriod.Month -> {
                val c = Calendar.getInstance().apply { timeInMillis = millis }
                val now = Calendar.getInstance()
                if (c.get(Calendar.MONTH) != now.get(Calendar.MONTH) ||
                    c.get(Calendar.YEAR) != now.get(Calendar.YEAR)
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
            buckets[key as String] = (buckets[key] ?: 0) + s.durationMinutes()
        }
    }

    // Normalize to 0..1 by the tallest bucket.
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
        if (days[i] == prev - oneDay) {
            streak++
            prev = days[i]
        } else break
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
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DAY_OF_MONTH, -((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7))
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

// =====================================================================
// 4. ROUTE
// =====================================================================

@Composable
fun StatisticsRoute(
    onBack: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: StatsViewModel = viewModel()
) {
    val period by viewModel.period.collectAsState()
    val page by viewModel.page.collectAsState()

    StatisticsScreen(
        page = page,
        period = period,
        onPeriodChange = { viewModel.setPeriod(it) },
        onBack = onBack,
        onSettingsClick = onSettingsClick
    )
}

// =====================================================================
// 5. SCREEN — pure UI
// =====================================================================

@Composable
fun StatisticsScreen(
    page: StatsPageData,
    period: StatsPeriod,
    onPeriodChange: (StatsPeriod) -> Unit,
    onBack: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            StatsTopBar(onBack = onBack, onSettingsClick = onSettingsClick)

            Spacer(Modifier.height(16.dp))

            PeriodTabs(selected = period, onSelected = onPeriodChange)

            Spacer(Modifier.height(16.dp))

            Text(
                text = page.dateRange.ifBlank { "—" },
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            TotalFocusCard(
                totalText = page.totalFocusTime,
                bars = page.bars.ifEmpty { List(7) { BarData("", 0f) } }
            )

            Spacer(Modifier.height(16.dp))

            StatsGrid(tiles = page.tiles)

            Spacer(Modifier.height(24.dp))
        }
    }
}

// =====================================================================
// 6. UI PIECES
// =====================================================================

@Composable
private fun StatsTopBar(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Text(
            text = "Statistics",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Settings",
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun PeriodTabs(
    selected: StatsPeriod,
    onSelected: (StatsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatsPeriod.entries.forEach { p ->
            val isSelected = p == selected

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Accent else Color.Transparent)
                    .clickable { onSelected(p) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = p.label,
                    color = if (isSelected) OnAccent else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TotalFocusCard(
    totalText: String,
    bars: List<BarData>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Text("Total Focus Time", color = TextSecondary, fontSize = 13.sp)

        Spacer(Modifier.height(4.dp))

        Text(
            text = totalText,
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        BarChart(bars = bars)

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BarChart(bars: List<BarData>) {
    val maxBarHeight = 110.dp
    val minBarHeight = 6.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxBarHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            val height = (minBarHeight.value +
                    (maxBarHeight.value - minBarHeight.value) *
                    bar.value.coerceIn(0f, 1f)).dp

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 3.dp)
                    .height(height)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Accent,
                                Accent.copy(alpha = 0.55f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun StatsGrid(tiles: List<StatTileData>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { tile ->
                    StatTile(data = tile, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(
    data: StatTileData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = data.label,
            color = TextSecondary,
            fontSize = 12.sp,
            maxLines = 2
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (data.emoji != null) {
                Text(text = data.emoji, fontSize = 20.sp)
            }

            Text(
                text = data.value,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            data.trend?.let { TrendBadge(it) }
        }
    }
}

@Composable
private fun TrendBadge(trend: Trend) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = if (trend.isPositive) "↑" else "↓",
            color = Accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${trend.percent}%",
            color = Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =====================================================================
// 7. PREVIEWS
// =====================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0E0E10, widthDp = 390, heightDp = 800)
@Composable
private fun StatisticsScreenPreview() {
    StatisticsScreen(
        page = StatsPageData(
            dateRange = "Mar 24 - Mar 30, 2025",
            totalFocusTime = "12h 40m",
            bars = listOf(
                BarData("Mon", 0.55f),
                BarData("Tue", 0.38f),
                BarData("Wed", 0.82f),
                BarData("Thu", 0.44f),
                BarData("Fri", 0.26f),
                BarData("Sat", 0.60f),
                BarData("Sun", 0.95f, isHighlighted = true)
            ),
            tiles = listOf(
                StatTileData("Focus Sessions", "28", trend = Trend(12, true)),
                StatTileData("Avg. Session", "27 min"),
                StatTileData("Completed Goals", "18", trend = Trend(25, true)),
                StatTileData("Current Streak", "12 days", emoji = "🔥")
            )
        ),
        period = StatsPeriod.Week,
        onPeriodChange = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0E10, widthDp = 390, heightDp = 800)
@Composable
private fun StatisticsScreenEmptyPreview() {
    StatisticsScreen(
        page = StatsPageData.EMPTY,
        period = StatsPeriod.Day,
        onPeriodChange = {}
    )
}