@file:Suppress("PackageName")

package com.example.disipline.UI.Habits.habit_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disipline.HabitsViewModel
import com.example.disipline.R
import com.example.disipline.UI.Habits.HabitDetailScreen
import com.example.disipline.UI.Habits.addSession.AddSessionScreen
import com.example.disipline.UI.Habits.sampleHabits
import com.example.disipline.data.Session
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.IconCircle
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary


// =====================================================================
// HABITS SCREEN (MVVM)
//
//    MODEL      -> Session (data package), Habit (this file)
//    VIEWMODEL  -> HabitsViewModel (Viewmodel package) holds the data
//                  and the functions that change it
//    VIEW       -> HabitsRoute connects the ViewModel to HabitsScreen.
//                  HabitsScreen only draws and calls callbacks.
//
// HOW TO USE IT (in your MainActivity or NavHost):
//
//   HabitsRoute(
//       onBack = { ... },
//       onStartSession = { habit, session -> /* open the Focus screen */ }
//   )
// =====================================================================

data class Habit(
    val id: Int,
    val title: String,
    val emoji: String,
    val category: String,          // section name, e.g. "Study"
    val completedSessions: Int,
    val targetSessions: Int,
    val sessions: List<Session> = emptyList()
) {
    val isDone: Boolean get() = completedSessions >= targetSessions
    val hasProgress: Boolean get() = completedSessions > 0
    val progress: Float
        get() = if (targetSessions <= 0) 0f
        else (completedSessions.toFloat() / targetSessions).coerceIn(0f, 1f)
    val subtitle: String
        get() {
            val unit = if (targetSessions == 1) "session" else "sessions"
            return "Daily · $completedSessions/$targetSessions $unit"
        }
}

fun formatTime(totalMinutes: Int): String {
    val hour24 = totalMinutes / 60
    val minute = totalMinutes % 60
    val suffix = if (hour24 < 12) "AM" else "PM"
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    return "%02d:%02d %s".format(hour12, minute, suffix)
}


// =====================================================================
// ROUTE - connects the ViewModel to the screen
// =====================================================================

@Composable
fun HabitsRoute(
    onBack: () -> Unit = {},
    onStartSession: (habit: Habit, session: Session) -> Unit = { _, _ -> },
    viewModel: HabitsViewModel = viewModel()
) {
    val habits by viewModel.habits.collectAsState()
    val sections by viewModel.sections.collectAsState()

    HabitsScreen(
        habits = habits,
        customSections = sections,
        onBack = onBack,
        onAddSection = viewModel::addSection,
        onAddHabit = viewModel::addHabit,
        onDeleteHabit = viewModel::deleteHabit,
        onAddSession = viewModel::addSession,
        onToggleSession = viewModel::toggleSession,
        onDeleteSession = viewModel::deleteSession,
        onStartSession = onStartSession
    )
}


// SCREEN - only draws data and calls callbacks

@Composable
fun HabitsScreen(
    // list of habits
    habits: List<Habit>,
    // top row list
    customSections: List<String>,
    //
    onBack: () -> Unit = {},
    // add button to add new section
    onAddSection: (name: String) -> Unit = {},

    onAddHabit: (title: String, emoji: String, category: String, target: Int) -> Unit = { _, _, _, _ -> },
    onDeleteHabit: (habitId: Int) -> Unit = {},
    onAddSession: (habitId: Int, name: String, start: String, end: String, goal: String, date: String) -> Unit =
        { _, _, _, _, _, _ -> },
    onToggleSession: (habitId: Int, sessionId: Int) -> Unit = { _, _ -> },
    onDeleteSession: (habitId: Int, sessionId: Int) -> Unit = { _, _ -> },
    // "Start Next Session" tapped: gives the habit and the session to start
    onStartSession: (habit: Habit, session: Session) -> Unit = { _, _ -> }
) {

    // Only screen-level UI state lives here. The data lives in the ViewModel.
    var selectedFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var showSectionDialog by remember { mutableStateOf(false) }
    var taskDialogSection by remember { mutableStateOf<String?>(null) }   // null = dialog closed
    var openHabitId by rememberSaveable { mutableStateOf<Int?>(null) }    // null = show the list
    var addingSession by rememberSaveable { mutableStateOf(false) }

    val openHabit = habits.firstOrNull { it.id == openHabitId }

    // Phone back button: add session -> detail -> list
    BackHandler(enabled = openHabit != null) {
        if (addingSession) addingSession = false else openHabitId = null
    }

    when {
        // ---------- 3. ADD SESSION ----------
        openHabit != null && addingSession -> {
            AddSessionScreen(
                habit = openHabit,
                onBack = { addingSession = false },
                onSave = { name, start, end, goal, date ->
                    onAddSession(openHabit.id, name, start, end, goal, date)
                    addingSession = false
                }
            )
        }

        // ---------- 2. HABIT DETAIL ----------
        openHabit != null -> {
            HabitDetailScreen(
                habit = openHabit,
                onBack = { openHabitId = null },
                onAddSession = { addingSession = true },

                // Tapping a session opens its Focus/timer screen. No toggling here.
                onOpenSession = { sessionId ->
                    val session = openHabit.sessions.firstOrNull { it.id == sessionId }
                    if (session != null && !session.isDone) {
                        onStartSession(openHabit, session)
                    }
                },

                onStartNext = {
                    val next = openHabit.sessions.firstOrNull { !it.isDone }
                    if (next != null) onStartSession(openHabit, next)
                },
                onDelete = { sessionId -> onDeleteSession(openHabit.id, sessionId) },
                onDeleteHabit = {
                    val wasLast = habits.count { it.category == openHabit.category } == 1
                    if (wasLast && selectedFilter == openHabit.category) {
                        selectedFilter = null
                    }
                    onDeleteHabit(openHabit.id)
                    addingSession = false
                    openHabitId = null
                }
            )
        }

        // ---------- 1. HABIT LIST ----------
        else -> {
            // Sections typed by the user + any section already used by a habit
            val allSections = (customSections + habits.map { it.category }).distinct()

            val visibleSections = if (selectedFilter == null) {
                allSections
            } else {
                allSections.filter { it == selectedFilter }
            }

            Scaffold(
                containerColor = BgDark,
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    ScreenTopBar(title = "Habits", onBack = onBack)

                    FilterRow(
                        sections = allSections,
                        selected = selectedFilter,
                        onSelected = { selectedFilter = it },
                        onAddSection = { showSectionDialog = true }
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        visibleSections.forEach { section ->
                            // we filter the habits with the section name
                            val sectionHabits = habits.filter {
                                it.category == section
                            }

                            // Section title + the button that adds a task to this section
                            item(key = "header_$section") {
                                SectionHeader(
                                    title = section,
                                    onAddTask = { taskDialogSection = section }
                                )
                            }

                            // if there is nothing in the list
                            if (sectionHabits.isEmpty()) {
                                item(key = "empty_$section") {
                                    Text(
                                        text = "No tasks yet",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            } else {
                                items(sectionHabits, key = { it.id }) { habit ->
                                    HabitRow(
                                        habit = habit,
                                        onClick = { openHabitId = habit.id }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showSectionDialog) {
                AddSectionDialog(
                    onDismiss = { showSectionDialog = false },
                    onConfirm = { name ->
                        val exists = allSections.any { it.equals(name, ignoreCase = true) }
                        if (!exists) onAddSection(name)
                        showSectionDialog = false
                    }
                )
            }

            taskDialogSection?.let { section ->
                AddTaskDialog(
                    section = section,
                    onDismiss = { taskDialogSection = null },
                    onConfirm = { title, emoji, target ->
                        onAddHabit(title, emoji, section, target)
                        taskDialogSection = null
                    }
                )
            }
        }
    }
}


@Composable
fun ProgressBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(Outline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(8.dp)
                .background(Accent)
        )
    }
}


// =====================================================================
// SHARED UI PIECES
// =====================================================================

@Composable
fun ScreenTopBar(title: String, onBack: () -> Unit) {
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
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) Accent else Surface)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) OnAccent else TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmojiCircle(emoji: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(IconCircle),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (size / 2).sp)
    }
}

@Composable
private fun FilterRow(
    sections: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    onAddSection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fixed button, always visible next to "All"
        SmallPillButton(text = "+ Section", onClick = onAddSection)

        // Scrolls sideways because the number of sections can grow
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 8.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipItem(
                    label = "All",
                    selected = selected == null,
                    onClick = { onSelected(null) }
                )
            }
            items(sections, key = { it }) { section ->
                FilterChipItem(
                    label = section,
                    selected = selected == section,
                    onClick = { onSelected(section) }
                )
            }
        }
    }
}

// this button is used for section add
@Composable
fun SmallPillButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Accent else Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) OnAccent else TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// section title and the task add button
@Composable
private fun SectionHeader(title: String, onAddTask: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        // this was the design of the button
        SmallPillButton(text = "+ Task", onClick = onAddTask)
    }
}

@Composable
private fun HabitRow(
    habit: Habit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmojiCircle(emoji = habit.emoji, size = 52)

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = habit.subtitle,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        CompletionIndicator(habit)
    }
}

@Composable
private fun CompletionIndicator(habit: Habit) {
    val base = Modifier.size(28.dp)

    when {
        habit.isDone -> {
            Box(
                modifier = base
                    .clip(CircleShape)
                    .background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "Completed",
                    tint = OnAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        habit.hasProgress -> {
            Box(
                modifier = base
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, Accent), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "In progress",
                    tint = Accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        else -> {
            Box(
                modifier = base
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, Outline), CircleShape)
            )
        }
    }
}

@Composable
private fun AddSectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = {
            Text(
                text = "New section",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("e.g. Work, Fitness", color = TextSecondary) },
                colors = dialogFieldColors()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isNotEmpty()) onConfirm(trimmed)
                }
            ) {
                Text("Add", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AddTaskDialog(
    section: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, emoji: String, target: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("✅") }
    var target by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = {
            Text(
                text = "New task in $section",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text("Task name", color = TextSecondary) },
                    placeholder = { Text("e.g. Read 10 pages", color = TextSecondary) },
                    colors = dialogFieldColors()
                )
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it.take(4) },
                    singleLine = true,
                    label = { Text("Emoji", color = TextSecondary) },
                    colors = dialogFieldColors()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter { c -> c.isDigit() }.take(2) },
                    singleLine = true,
                    label = { Text("Sessions per day", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = dialogFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = title.trim()
                    if (trimmed.isNotEmpty()) {
                        val sessions = (target.toIntOrNull() ?: 1).coerceAtLeast(1)
                        val icon = emoji.trim().ifEmpty { "✅" }
                        onConfirm(trimmed, icon, sessions)
                    }
                }
            ) {
                Text("Add", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = Accent,
    unfocusedBorderColor = Outline,
    cursorColor = Accent,
    focusedLabelColor = Accent,
    unfocusedLabelColor = TextSecondary
)


// PREVIEW


@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 800)
@Composable
fun HabitsScreenPreview() {
    HabitsScreen(
        habits = sampleHabits,
        customSections = listOf("Study")
    )
}
//
//@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 800)
//@Composable
//private fun HabitDetailPreview() {
//    HabitDetailScreen(
//        habit = sampleHabits.first(),
//        onBack = {},
//        onAddSession = {},
//        onToggleSession = {},
//        onStartNext = {},
//        onDelete = {},
//        onDeleteHabit = {}
//    )
//}

@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 800)
@Composable
fun AddSessionPreview() {
    AddSessionScreen(
        habit = sampleHabits.first(),
        onBack = {},
        onSave = { _, _, _, _, _ -> }
    )
}