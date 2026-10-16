package com.example.disipline.UI


// =====================================================================
// FOCUS SESSION SCREEN (setup screen before a timer starts)
//
//   1. DATA        -> FocusMode, FocusSettings, break plan, session info
//   2. ICONS       -> one place to change every icon
//   3. MAIN SCREEN -> FocusSessionScreen() - the only function other files call
//   4. UI PIECES   -> small private composables the screen is built from
//   5. PREVIEW     -> sample for Android Studio
//
// HOW IT IS USED:
//
//   - Habit detail "Start Next Session" -> focusInfoFor(habit, session)
//     -> FocusSessionScreen(sessionInfo = ...) shows that session's details
//     and pre-fills the task and minutes.
//   - The mode decides the break plan INSIDE the session time:
//       Focus       -> no breaks
//       Short Break -> 5 min breaks, 2 times (3 times if 45+ min)
//       Long Break  -> one 15 min break
//   - The blocked apps list comes from the Block Apps screen (saved list).
// =====================================================================

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.disipline.R
import com.example.disipline.UI.Habits.habit_screen.Habit
import com.example.disipline.blocker.BlockedApp
import com.example.disipline.blocker.loadSelectedApps
import com.example.disipline.data.Session
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.AppColors
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.IconCircle
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


enum class FocusMode(val label: String, val hint: String) {
    Focus("Focus", "No breaks"),
    ShortBreak("Short Break", "5 min breaks, 2 times (3 times for 45+ min)"),
    LongBreak("Long Break", "One 15 min break in the middle")
}

// One piece of a session: either focus time or a break.
enum class SegmentType { Focus, Break }

data class FocusSegment(val type: SegmentType, val minutes: Int)

// Everything the user chose. This is what onStartSession gives you.
data class FocusSettings(
    val mode: FocusMode,
    val minutes: Int,
    val task: String,
    val ambientSound: Boolean,
    val blockApps: Boolean,
    val blockedApps: List<String> = emptyList()   // PACKAGE NAMES of the apps to block
) {
    // Focus / break pieces in order, for example Focus 7, Break 5, Focus 7 ...
    val plan: List<FocusSegment> get() = buildPlan(mode, minutes)
}

// The session the user pressed "Start Next Session" for.
data class FocusSessionInfo(
    val habitId: Int,
    val sessionId: Int,
    val habitTitle: String,
    val sessionName: String,
    val date: String,
    val timeRange: String,
    val goal: String,
    val minutes: Int           // length of the session, from its start and end time
)

// The four quick-pick cards
private val presetMinutes = listOf(15, 25, 45, 60)

// Limits for the - / + custom stepper
private const val MIN_MINUTES = 1
private const val MAX_MINUTES = 180

// Break rules
private const val SHORT_BREAK_MINUTES = 5
private const val LONG_BREAK_MINUTES = 15
private const val MIN_FOCUS_CHUNK = 5      // every focus piece is at least this long

// Colour of the break pieces in the plan bar
private val BreakColor get() = AppColors.Warning

// Splits the session into focus pieces and breaks. The breaks are INSIDE the
// total time. Example: 30 min + Short Break -> Focus 7, Break 5, Focus 7, Break 5, Focus 6.
fun buildPlan(mode: FocusMode, totalMinutes: Int): List<FocusSegment> {
    val breakLength = when (mode) {
        FocusMode.Focus -> 0
        FocusMode.ShortBreak -> SHORT_BREAK_MINUTES
        FocusMode.LongBreak -> LONG_BREAK_MINUTES
    }
    var breaks = when (mode) {
        FocusMode.Focus -> 0
        FocusMode.ShortBreak -> if (totalMinutes >= 45) 3 else 2
        FocusMode.LongBreak -> 1
    }

    // Too short for that many breaks -> use fewer
    while (breaks > 0 && totalMinutes - breaks * breakLength < (breaks + 1) * MIN_FOCUS_CHUNK) {
        breaks--
    }
    if (breaks == 0) return listOf(FocusSegment(SegmentType.Focus, totalMinutes))

    val focusTotal = totalMinutes - breaks * breakLength
    val chunks = breaks + 1
    val base = focusTotal / chunks
    val extra = focusTotal % chunks        // the first pieces get the spare minutes

    val plan = mutableListOf<FocusSegment>()
    for (i in 0 until chunks) {
        plan += FocusSegment(SegmentType.Focus, base + if (i < extra) 1 else 0)
        if (i < breaks) plan += FocusSegment(SegmentType.Break, breakLength)
    }
    return plan
}

// Builds the details for the Focus screen from a habit session.
fun focusInfoFor(habit: Habit, session: Session): FocusSessionInfo {
    val start = timeTextToMinutes(session.startTime)
    val end = timeTextToMinutes(session.endTime)
    val length = if (start != null && end != null && end > start) end - start else 25

    return FocusSessionInfo(
        habitId = habit.id,
        sessionId = session.id,
        habitTitle = habit.title,
        sessionName = session.name,
        date = session.date,
        timeRange = session.timeRange,
        goal = session.goal,
        minutes = length.coerceIn(MIN_MINUTES, MAX_MINUTES)
    )
}

// "08:00 AM" / "02:30 PM" -> minutes since midnight (null if the text is odd)
private fun timeTextToMinutes(text: String): Int? {
    val parts = text.trim().split(" ")
    if (parts.size != 2) return null
    val hm = parts[0].split(":")
    if (hm.size != 2) return null
    val hour = hm[0].toIntOrNull() ?: return null
    val minute = hm[1].toIntOrNull() ?: return null
    val isPm = parts[1].equals("PM", ignoreCase = true)
    return ((hour % 12) + if (isPm) 12 else 0) * 60 + minute
}

// =====================================================================
// 2. ICONS
// Placeholders. To use real icons: right click res/drawable ->
// New -> Vector Asset, then change the lines below.
// The +/- buttons are plain text characters and the switch rows use
// emoji, so they need no drawables.
// =====================================================================

private val ICON_BACK = R.drawable.ic_launcher_foreground
private val ICON_ARROW = R.drawable.ic_launcher_foreground

// =====================================================================
// 3. MAIN SCREEN - call this from your navigation
// =====================================================================

@Composable
fun FocusSessionScreen(
    sessionInfo: FocusSessionInfo? = null,         // set when opened from "Start Next Session"
    initialTask: String = "",                      // pre-fill the Task box
    onBack: () -> Unit = {},                       // back arrow tapped
    onOpenBlockApps: () -> Unit = {},              // "Manage" tapped -> Block Apps screen
    onStartSession: (FocusSettings) -> Unit = {}   // green button tapped
) {
    val context = LocalContext.current

    // ---- Form state: what the user is currently choosing ----
    // remember(sessionInfo): the form restarts with new values when a different session is opened
    var mode by remember(sessionInfo) { mutableStateOf(FocusMode.Focus) }
    var minutes by remember(sessionInfo) { mutableStateOf(sessionInfo?.minutes ?: 25) }
    var task by remember(sessionInfo) { mutableStateOf(sessionInfo?.sessionName ?: initialTask) }
    var ambientSound by remember { mutableStateOf(true) }
    var blockApps by remember { mutableStateOf(true) }

    // The real apps saved on the Block Apps screen
    var savedApps by remember { mutableStateOf<List<BlockedApp>>(emptyList()) }
    // Apps the user UNTICKED for this session only (everything else is ticked)
    var untickedApps by remember { mutableStateOf<Set<String>>(emptySet()) }
    // Package names that will be blocked in this session
    val blockedApps: Set<String> =
        savedApps.map { it.packageName }.filter { it !in untickedApps }.toSet()

    // Reload the saved list every time this screen comes back into view
    var refresh by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(refresh) {
        savedApps = withContext(Dispatchers.IO) { loadSelectedApps(context) }
    }

    val plan = buildPlan(mode, minutes)

    Scaffold(
        containerColor = BgDark,
        // Start button lives in the bottom bar so it stays visible while the form scrolls
        bottomBar = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                StartSessionButton(
                    onClick = {
                        onStartSession(
                            FocusSettings(
                                mode = mode,
                                minutes = minutes,
                                task = task.trim(),
                                ambientSound = ambientSound,
                                blockApps = blockApps,
                                blockedApps = if (blockApps) {
                                    savedApps.map { it.packageName }.filter { it in blockedApps }
                                } else {
                                    emptyList()
                                }
                            )
                        )
                    }
                )
            }
        }
    ) { padding ->
        // verticalScroll so the form still fits on small screens / with the keyboard open
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            FocusTopBar(onBack = onBack)

            // Details of the session chosen on the habit screen
            if (sessionInfo != null) {
                SessionDetailsCard(info = sessionInfo)
                Spacer(Modifier.height(16.dp))
            }

            // Focus | Short Break | Long Break
            ModeRow(
                selected = mode,
                onSelected = { mode = it }
            )
            Text(
                text = mode.hint,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            Spacer(Modifier.height(10.dp))

            // 15 | 25 | 45 | 60 quick picks
            PresetRow(
                selectedMinutes = minutes,
                onSelected = { minutes = it }
            )

            Spacer(Modifier.height(16.dp))

            // Custom (min)   [ - 25 + ]
            CustomMinutesRow(
                minutes = minutes,
                onMinus = { minutes = (minutes - 1).coerceAtLeast(MIN_MINUTES) },
                onPlus = { minutes = (minutes + 1).coerceAtMost(MAX_MINUTES) }
            )

            Spacer(Modifier.height(16.dp))

            // Focus pieces and breaks for the chosen mode and minutes
            PlanCard(mode = mode, plan = plan)

            Spacer(Modifier.height(20.dp))

            // Task label + text box
            Text(
                text = "Task",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                TaskField(value = task, onValueChange = { task = it })
            }

            Spacer(Modifier.height(16.dp))

            // The two switches in one card
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Surface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ToggleRow(
                    emoji = "🎧",
                    title = "Ambient Sound",
                    subtitle = "Forest · Rain · LoFi",
                    checked = ambientSound,
                    onCheckedChange = { ambientSound = it }
                )
                ToggleRow(
                    emoji = "🚫",
                    title = "Block Distracting Apps",
                    subtitle = "Block selected apps during session",
                    checked = blockApps,
                    onCheckedChange = { blockApps = it }
                )
            }

            // Blocked apps list, only shown while blocking is switched on
            if (blockApps) {
                Spacer(Modifier.height(16.dp))
                BlockedAppsCard(
                    apps = savedApps,
                    selected = blockedApps,
                    onToggle = { pkg ->
                        untickedApps = if (pkg in untickedApps) untickedApps - pkg else untickedApps + pkg
                    },
                    onManage = onOpenBlockApps
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}


//   back arrow Focus Session
@Composable
private fun FocusTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(ICON_BACK),
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Text(
            text = "Focus Session",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// ---- Details of the session that is about to start ----
@Composable
private fun SessionDetailsCard(info: FocusSessionInfo) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Starting session",
            color = Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = info.sessionName,
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = info.habitTitle, color = TextSecondary, fontSize = 13.sp)
        if (info.date.isNotBlank()) {
            Text(text = info.date, color = TextSecondary, fontSize = 12.sp)
        }
        Text(text = info.timeRange, color = TextSecondary, fontSize = 12.sp)
        if (info.goal.isNotBlank()) {
            Text(text = "Goal: ${info.goal}", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

// ---- Row of three mode chips ----
@Composable
private fun ModeRow(selected: FocusMode, onSelected: (FocusMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // One chip per enum value; weight(1f) gives each the same width
        FocusMode.entries.forEach { m ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (m == selected) Accent else Surface)
                    .clickable { onSelected(m) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = m.label,
                    color = if (m == selected) OnAccent else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (m == selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ---- Row of four preset cards: 15 / 25 / 45 / 60 ----
@Composable
private fun PresetRow(selectedMinutes: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presetMinutes.forEach { value ->
            val isSelected = value == selectedMinutes
            val shape = RoundedCornerShape(14.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(if (isSelected) IconCircle else Surface)
                    // green border only on the selected card
                    .then(
                        if (isSelected) Modifier.border(BorderStroke(2.dp, Accent), shape)
                        else Modifier
                    )
                    .clickable { onSelected(value) }
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$value",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "min", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

// ---- "Custom (min)  ........  [ -  25  + ]" ----
@Composable
private fun CustomMinutesRow(minutes: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Custom (min)",
            color = TextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)      // pushes the stepper to the right
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Surface)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperButton(symbol = "−", onClick = onMinus)

            Text(
                text = "$minutes",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(56.dp)
            )

            StepperButton(symbol = "+", onClick = onPlus)
        }
    }
}

// ---- Round "-" or "+" button ----
@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = TextPrimary, fontSize = 22.sp)
    }
}

// ---- Session plan: a bar of focus (green) and break (orange) pieces ----
@Composable
private fun PlanCard(mode: FocusMode, plan: List<FocusSegment>) {
    val breaks = plan.filter { it.type == SegmentType.Break }

    val summary = when {
        breaks.isNotEmpty() ->
            "${breaks.size} ${if (breaks.size == 1) "break" else "breaks"} of ${breaks.first().minutes} min"
        mode == FocusMode.Focus -> "No breaks"
        else -> "Session is too short for breaks"
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Session plan",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(text = summary, color = TextSecondary, fontSize = 12.sp)
        }

        Spacer(Modifier.height(10.dp))

        // Each piece is as wide as its minutes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
        ) {
            plan.forEach { segment ->
                Box(
                    modifier = Modifier
                        .weight(segment.minutes.toFloat())
                        .fillMaxSize()
                        .background(if (segment.type == SegmentType.Focus) Accent else BreakColor)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = plan.joinToString("  →  ") {
                "${if (it.type == SegmentType.Focus) "Focus" else "Break"} ${it.minutes}m"
            },
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

// ---- Task text box (plain BasicTextField, styled to match the theme) ----
@Composable
private fun TaskField(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
        cursorBrush = SolidColor(Accent),             // blinking cursor color
        modifier = Modifier.fillMaxWidth(),
        // decorationBox draws the box around the text and the hint when empty
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "What will you work on?",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

// ---- One switch row: [icon]  title / subtitle  ........ [switch] ----
@Composable
private fun ToggleRow(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(IconCircle),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 16.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent,
                checkedBorderColor = Accent,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = Surface,
                uncheckedBorderColor = Outline
            )
        )
    }
}

// ---- List of the REAL saved apps: tap a row to tick / untick it for this session ----
@Composable
private fun BlockedAppsCard(
    apps: List<BlockedApp>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onManage: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Selected Apps",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Manage",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onManage)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (apps.isEmpty()) {
            Text(
                text = "No apps selected yet. Tap Manage to add apps.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            return@Column
        }

        Text(
            text = "${selected.size} of ${apps.size} blocked during the session",
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))

        apps.forEach { app ->
            val isSelected = app.packageName in selected

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggle(app.packageName) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = app.label,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                // Filled green circle with a tick when selected, empty ring when not
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) Modifier.background(Accent)
                            else Modifier.border(BorderStroke(2.dp, Outline), CircleShape)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text(
                            text = "✓",
                            color = OnAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ---- Big green "Start Session  →" button ----
@Composable
private fun StartSessionButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(Accent)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Start Session",
            color = OnAccent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(ICON_ARROW),
            contentDescription = null,
            tint = OnAccent,
            modifier = Modifier.size(20.dp)
        )
    }
}

// =====================================================================
// 5. PREVIEW - delete when you no longer need it.
// =====================================================================

@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 800)
@Composable
private fun FocusSessionScreenPreview() {
    FocusSessionScreen(initialTask = "Study DSA")
}

@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 800)
@Composable
private fun FocusSessionWithDetailsPreview() {
    FocusSessionScreen(
        sessionInfo = FocusSessionInfo(
            habitId = 1,
            sessionId = 3,
            habitTitle = "Study DSA",
            sessionName = "Revision",
            date = "12 Oct 2026",
            timeRange = "02:00 PM - 03:00 PM",
            goal = "Revise important topics",
            minutes = 60
        )
    )
}