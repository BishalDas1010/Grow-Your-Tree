package com.example.disipline.UI.Habits

import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.R
import com.example.disipline.UI.Habits.habit_screen.Habit
import com.example.disipline.UI.Habits.habit_screen.PrimaryButton
import com.example.disipline.UI.Habits.habit_screen.ProgressBar
import com.example.disipline.UI.Habits.habit_screen.ScreenTopBar
import com.example.disipline.UI.Habits.habit_screen.SmallPillButton
import com.example.disipline.UI.Habits.habit_screen.formatTime
import com.example.disipline.data.Session
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.IconCircle
import com.example.disipline.ui.theme.OnAccent
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary

import com.example.disipline.ui.theme.AppColors

// SCREEN 2 - HABIT DETAIL

private val DeleteRed get() = AppColors.Error

@Composable
fun TimeField(label: String, minutes: Int, onPicked: (Int) -> Unit) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(12.dp)

    Column {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Surface)
                .border(BorderStroke(1.dp, Outline), shape)
                .clickable {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onPicked(hour * 60 + minute) },
                        minutes / 60,
                        minutes % 60,
                        false
                    ).show()
                }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = formatTime(minutes),
                color = TextPrimary,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun CheckCircle(done: Boolean) {
    val base = Modifier.size(28.dp)
    if (done) {
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
    } else {
        Box(
            modifier = base
                .clip(CircleShape)
                .border(BorderStroke(2.dp, Outline), CircleShape)
        )
    }
}

@Composable
fun SessionRow(
    session: Session,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckCircle(done = session.isDone)

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = session.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(2.dp))

            if (session.date.isNotBlank()) {
                Text(
                    text = session.date,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Text(
                text = session.timeRange,
                color = TextSecondary,
                fontSize = 12.sp
            )

            if (session.goal.isNotBlank()) {
                Text(
                    text = session.goal,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Delete session button
        IconButton(onClick = onDelete) {
            Text(
                text = "✕",
                color = DeleteRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DeleteHabitDialog(
    habitTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = {
            Text(
                text = "Delete habit?",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "\"$habitTitle\" and all of its sessions will be removed.",
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = DeleteRed, fontWeight = FontWeight.Bold)
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
fun HabitDetailScreen(
    habit: Habit,
    onBack: () -> Unit,
    onAddSession: () -> Unit,
    onOpenSession: (Int) -> Unit,   // NEW: replaces onToggleSession
    onStartNext: () -> Unit,
    onDelete: (Int) -> Unit,
    onDeleteHabit: () -> Unit
) {
    val hasSessions = habit.sessions.isNotEmpty()
    val allDone = hasSessions && habit.sessions.all { it.isDone }
    var showDeleteHabitDialog by remember { mutableStateOf(false) }

    if (showDeleteHabitDialog) {
        DeleteHabitDialog(
            habitTitle = habit.title,
            onConfirm = {
                showDeleteHabitDialog = false
                onDeleteHabit()
            },
            onDismiss = {
                showDeleteHabitDialog = false
            }
        )
    }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                PrimaryButton(
                    text = when {
                        !hasSessions -> "Add your first session"
                        allDone -> "All Sessions Completed"
                        else -> "Start Next Session"
                    },
                    enabled = !allDone,
                    //implement there naviagtion to the  time page
                    onClick = if (hasSessions) onStartNext else onAddSession
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScreenTopBar(title = "", onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Big emoji + title
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(IconCircle),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = habit.emoji, fontSize = 36.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = habit.title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = habit.subtitle,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Today's progress card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Today's Progress",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${habit.completedSessions} / ${habit.targetSessions} completed",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        ProgressBar(fraction = habit.progress)
                    }
                }

                // "Sessions" title + Add Session button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sessions",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        SmallPillButton(text = "+ Add Session", onClick = onAddSession)
                    }
                }

                if (!hasSessions) {
                    item {
                        Text(
                            text = "No sessions yet. Tap + Add Session.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(habit.sessions, key = { it.id }) { session ->
                        SessionRow(
                            session = session,
                            // Completed sessions can't be reopened or un-checked
                            onClick = { if (!session.isDone) onOpenSession(session.id) },
                            onDelete = { onDelete(session.id) }
                        )
                    }
                }

                // Delete whole habit
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { showDeleteHabitDialog = true }) {
                            Text(
                                text = "Delete habit",
                                color = DeleteRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
//
//@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 800)
//@Composable
//private fun HabitDetailPreview() {
//    HabitDetailScreen(
//        habit = sampleHabits.first(),
//        onBack = {},
//        onAddSession = {},
//        onStartNext = {},
//        onDeleteHabit = {},
//    )
//}