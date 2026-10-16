package com.example.disipline.UI.Stas

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disipline.R
import com.example.disipline.UI.Habits.addSession.StatsViewModel
import com.example.disipline.data.Session
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.AppColors
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

// =====================================================================
// 1. DATA
// =====================================================================

enum class StatsPeriod(val label: String) {
    Day("Today"),
    Week("Week"),
    Month("Month"),
    Year("Year")
}

data class BarData(
    val label: String,
    val value: Float,
    val minutes: Int = 0,
    val isHighlighted: Boolean = false
)

data class Trend(val percent: Int, val isPositive: Boolean)

data class StatTileData(
    val label: String,
    val value: Int,
    val suffix: String = "",
    val emoji: String? = null,
    val trend: Trend? = null
)

data class StatsPageData(
    val dateRange: String,
    val totalMinutes: Int,
    val totalTrend: Trend?,
    val bars: List<BarData>,
    val tiles: List<StatTileData>
) {
    companion object {
        val EMPTY = StatsPageData(
            dateRange = "",
            totalMinutes = 0,
            totalTrend = null,
            bars = emptyList(),
            tiles = emptyList()
        )
    }
}

// =====================================================================
// 2. AGGREGATION
// =====================================================================

private const val DEFAULT_SESSION_MINUTES = 25
private const val DAY_MS = 86_400_000L

private class ParsedSession(
    val dayStart: Long,
    val minutes: Int,
    val startHour: Int?,
    val hasGoal: Boolean
)

private class Range(val start: Long, val end: Long)

private class Totals(val count: Int, val minutes: Int, val goals: Int) {
    val avg: Int get() = if (count == 0) 0 else (minutes.toFloat() / count).roundToInt()
}

private val TIME_REGEX = Regex("""^\s*(\d{1,2}):(\d{2})\s*([AaPp][Mm])?\s*$""")

private fun parseTime(text: String): Int? {
    val m = TIME_REGEX.matchEntire(text) ?: return null
    val hour = m.groupValues[1].toInt()
    val minute = m.groupValues[2].toInt()
    if (minute !in 0..59) return null
    val suffix = m.groupValues[3]
    return if (suffix.isEmpty()) {
        if (hour !in 0..23) null else hour * 60 + minute
    } else {
        if (hour !in 1..12) return null
        val isPm = suffix.equals("PM", ignoreCase = true)
        ((hour % 12) + if (isPm) 12 else 0) * 60 + minute
    }
}

private fun Session.durationMinutes(): Int {
    val s = parseTime(startTime) ?: return DEFAULT_SESSION_MINUTES
    val e = parseTime(endTime) ?: return DEFAULT_SESSION_MINUTES
    val d = if (e >= s) e - s else e + 24 * 60 - s
    return if (d > 0) d else DEFAULT_SESSION_MINUTES
}

private val dateFormats = listOf(
    "dd MMM yyyy",
    "MMM dd, yyyy",
    "yyyy-MM-dd",
    "dd/MM/yyyy",
    "MM/dd/yyyy"
)

private fun parseDate(text: String, parsers: List<SimpleDateFormat>): Long? {
    val clean = text.trim()
    for (p in parsers) {
        try {
            return p.parse(clean)?.time ?: continue
        } catch (_: Exception) { }
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

private fun dayBefore(dayStart: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = dayStart
        add(Calendar.DAY_OF_MONTH, -1)
    }.timeInMillis

private fun List<Session>.parseAll(): List<ParsedSession> {
    val parsers = dateFormats.map { SimpleDateFormat(it, Locale.US).apply { isLenient = false } }
    return mapNotNull { s ->
        val millis = parseDate(s.date, parsers) ?: return@mapNotNull null
        ParsedSession(
            dayStart = startOfDay(millis),
            minutes = s.durationMinutes(),
            startHour = parseTime(s.startTime)?.div(60),
            hasGoal = s.goal.isNotBlank()
        )
    }
}

private fun rangeOf(period: StatsPeriod, offset: Int = 0): Range {
    val c = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    when (period) {
        StatsPeriod.Day -> c.add(Calendar.DAY_OF_MONTH, offset)
        StatsPeriod.Week -> {
            c.add(Calendar.DAY_OF_MONTH, -((c.get(Calendar.DAY_OF_WEEK) + 5) % 7))
            c.add(Calendar.DAY_OF_MONTH, 7 * offset)
        }
        StatsPeriod.Month -> {
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.add(Calendar.MONTH, offset)
        }
        StatsPeriod.Year -> {
            c.set(Calendar.MONTH, Calendar.JANUARY)
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.add(Calendar.YEAR, offset)
        }
    }
    val start = c.timeInMillis
    when (period) {
        StatsPeriod.Day -> c.add(Calendar.DAY_OF_MONTH, 1)
        StatsPeriod.Week -> c.add(Calendar.DAY_OF_MONTH, 7)
        StatsPeriod.Month -> c.add(Calendar.MONTH, 1)
        StatsPeriod.Year -> c.add(Calendar.YEAR, 1)
    }
    return Range(start, c.timeInMillis)
}

private fun List<ParsedSession>.inRange(r: Range) =
    filter { it.dayStart >= r.start && it.dayStart < r.end }

private fun List<ParsedSession>.totals() =
    Totals(count = size, minutes = sumOf { it.minutes }, goals = count { it.hasGoal })

private fun trendOf(current: Int, previous: Int): Trend? {
    if (previous <= 0) return null
    val pct = ((current - previous) * 100f / previous).roundToInt()
    if (pct == 0) return null
    return Trend(abs(pct), pct > 0)
}

private fun List<Session>.toPageData(period: StatsPeriod): StatsPageData {
    val all = filter { it.isDone }.parseAll()

    val range = rangeOf(period, 0)
    val prevRange = rangeOf(period, -1)

    val current = all.inRange(range)
    val c = current.totals()
    val p = all.inRange(prevRange).totals()

    val streak = computeStreak(all)

    return StatsPageData(
        dateRange = formatRange(period, range),
        totalMinutes = c.minutes,
        totalTrend = trendOf(c.minutes, p.minutes),
        bars = bucketIntoBars(current, period, range),
        tiles = listOf(
            StatTileData("Focus Sessions", c.count, trend = trendOf(c.count, p.count)),
            StatTileData("Avg. Session", c.avg, " min", trend = trendOf(c.avg, p.avg)),
            StatTileData("Completed Goals", c.goals, trend = trendOf(c.goals, p.goals)),
            StatTileData(
                "Current Streak", streak,
                if (streak == 1) " day" else " days",
                emoji = "🔥"
            )
        )
    )
}

private fun bucketIntoBars(
    sessions: List<ParsedSession>,
    period: StatsPeriod,
    range: Range
): List<BarData> {
    val labels: List<String> = when (period) {
        StatsPeriod.Day -> listOf("12a", "6a", "12p", "6p")
        StatsPeriod.Week -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        StatsPeriod.Month -> {
            val c = Calendar.getInstance().apply { timeInMillis = range.start }
            val weeks = (c.getActualMaximum(Calendar.DAY_OF_MONTH) + 6) / 7
            List(weeks) { "Wk ${it + 1}" }
        }
        StatsPeriod.Year -> listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
    }

    val minutes = IntArray(labels.size)
    val cal = Calendar.getInstance()

    sessions.forEach { s ->
        val idx = when (period) {
            StatsPeriod.Day -> (s.startHour ?: 0) / 6
            StatsPeriod.Week -> ((s.dayStart - range.start + DAY_MS / 2) / DAY_MS).toInt()
            StatsPeriod.Month -> {
                cal.timeInMillis = s.dayStart
                (cal.get(Calendar.DAY_OF_MONTH) - 1) / 7
            }
            StatsPeriod.Year -> {
                cal.timeInMillis = s.dayStart
                cal.get(Calendar.MONTH)
            }
        }
        if (idx in minutes.indices) minutes[idx] += s.minutes
    }

    val max = minutes.maxOrNull() ?: 0
    return labels.mapIndexed { i, label ->
        BarData(
            label = label,
            value = if (max > 0) minutes[i].toFloat() / max else 0f,
            minutes = minutes[i],
            isHighlighted = max > 0 && minutes[i] == max
        )
    }
}

private fun computeStreak(all: List<ParsedSession>): Int {
    val today = startOfDay(System.currentTimeMillis())
    val days = all.map { it.dayStart }.filter { it <= today }.toHashSet()

    var cursor = when {
        today in days -> today
        dayBefore(today) in days -> dayBefore(today)
        else -> return 0
    }
    var streak = 0
    while (cursor in days) {
        streak++
        cursor = dayBefore(cursor)
    }
    return streak
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

private fun formatRange(period: StatsPeriod, r: Range): String {
    val start = Date(r.start)
    val lastDay = Date(r.end - 1)
    return when (period) {
        StatsPeriod.Day -> SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(start)
        StatsPeriod.Week -> {
            val f = SimpleDateFormat("MMM d", Locale.US)
            val y = SimpleDateFormat("yyyy", Locale.US)
            "${f.format(start)} - ${f.format(lastDay)}, ${y.format(lastDay)}"
        }
        StatsPeriod.Month -> SimpleDateFormat("MMMM yyyy", Locale.US).format(start)
        StatsPeriod.Year -> SimpleDateFormat("yyyy", Locale.US).format(start)
    }
}

// =====================================================================
// 3. ROUTE
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
// 4. SCREEN
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
        contentWindowInsets = WindowInsets(15, 0, 15, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(
                    top = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding()
                )
        ) {
            Spacer(Modifier.height(12.dp))

            StatsTopBar(onBack = onBack, onSettingsClick = onSettingsClick)

            Spacer(Modifier.height(16.dp))

            PeriodTabs(selected = period, onSelected = onPeriodChange)

            Spacer(Modifier.height(16.dp))

            Crossfade(
                targetState = page.dateRange.ifBlank { "—" },
                animationSpec = tween(250),
                label = "dateRange",
                modifier = Modifier.fillMaxWidth()
            ) { text ->
                Text(
                    text = text,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            TotalFocusCard(
                totalMinutes = page.totalMinutes,
                trend = page.totalTrend,
                bars = page.bars.ifEmpty { List(7) { BarData("", 0f) } }
            )

            Spacer(Modifier.height(16.dp))

            StatsGrid(tiles = page.tiles)

            Spacer(Modifier.height(24.dp))
        }
    }
}

// =====================================================================
// 5. UI PIECES
// =====================================================================

@Composable
private fun animatedInt(target: Int): Int {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val value by animateIntAsState(
        targetValue = if (started) target else 0,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "count"
    )
    return value
}

@Composable
private fun StatsTopBar(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(30.dp)) {
            Icon(
                painter = painterResource(R.drawable.left),
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


    }
}

@Composable
private fun PeriodTabs(
    selected: StatsPeriod,
    onSelected: (StatsPeriod) -> Unit
) {
    val entries = StatsPeriod.entries
    val selectedIndex = entries.indexOf(selected)
    val tabHeight = 36.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Surface)
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / entries.size

        val indicatorX by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "tabIndicator"
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(indicatorX.roundToPx(), 0) }
                .width(tabWidth)
                .height(tabHeight)
                .clip(RoundedCornerShape(50))
                .background(Accent)
        )

        Row {
            entries.forEach { p ->
                val isSelected = p == selected
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) OnAccent else TextSecondary,
                    animationSpec = tween(200),
                    label = "tabText"
                )
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .height(tabHeight)
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelected(p) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = p.label,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalFocusCard(
    totalMinutes: Int,
    trend: Trend?,
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatDuration(animatedInt(totalMinutes)),
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            trend?.let { TrendBadge(it) }
        }

        Spacer(Modifier.height(20.dp))

        BarChart(bars = bars)

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Crossfade(
                    targetState = bar.label,
                    animationSpec = tween(250),
                    label = "barLabel",
                    modifier = Modifier.weight(1f)
                ) { label ->
                    Text(
                        text = label,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private val MaxBarHeight = 110.dp
private const val MIN_BAR_FRACTION = 6f / 110f

@Composable
private fun BarChart(bars: List<BarData>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaxBarHeight)
    ) {
        bars.forEachIndexed { index, bar ->
            ChartBar(bar = bar, index = index)
        }
    }
}

@Composable
private fun RowScope.ChartBar(bar: BarData, index: Int) {
    val fraction = remember { Animatable(0f) }
    val target = MIN_BAR_FRACTION + (1f - MIN_BAR_FRACTION) * bar.value.coerceIn(0f, 1f)

    LaunchedEffect(target) {
        delay(index * 30L)
        fraction.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val alpha by animateFloatAsState(
        targetValue = when {
            bar.isHighlighted -> 1f
            bar.value > 0f -> 0.65f
            else -> 0.3f
        },
        animationSpec = tween(300),
        label = "barAlpha"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 3.dp)
            .drawBehind {
                val h = size.height * fraction.value.coerceIn(0f, 1f)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Accent, Accent.copy(alpha = 0.55f)),
                        startY = size.height - h,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, size.height - h),
                    size = Size(size.width, h),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    alpha = alpha
                )
            }
    )
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
                text = "${animatedInt(data.value)}${data.suffix}",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            data.trend?.let { TrendBadge(it) }
        }
    }
}

private val NegativeColor get() = AppColors.Error

@Composable
private fun TrendBadge(trend: Trend) {
    val color = if (trend.isPositive) Accent else NegativeColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = if (trend.isPositive) "↑" else "↓",
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${trend.percent}%",
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =====================================================================
// 6. PREVIEWS
// =====================================================================

@Preview(showBackground = true, backgroundColor = 0xFF101814, widthDp = 390, heightDp = 800)
@Composable
private fun StatisticsScreenPreview() {
        StatisticsScreen(
            page = StatsPageData(
                dateRange = "Mar 24 - Mar 30, 2025",
                totalMinutes = 12 * 60 + 40,
                totalTrend = Trend(8, true),
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
                    StatTileData("Focus Sessions", 28, trend = Trend(12, true)),
                    StatTileData("Avg. Session", 27, " min", trend = Trend(5, false)),
                    StatTileData("Completed Goals", 18, trend = Trend(25, true)),
                    StatTileData("Current Streak", 12, " days", emoji = "🔥")
                )
            ),
            period = StatsPeriod.Week,
            onPeriodChange = {}
        )
    }


@Preview(showBackground = true, backgroundColor = 0xFF101814, widthDp = 390, heightDp = 800)
@Composable
private fun StatisticsScreenEmptyPreview() {
        StatisticsScreen(
            page = StatsPageData.EMPTY,
            period = StatsPeriod.Day,
            onPeriodChange = {}
        )
    }
