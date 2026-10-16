package com.example.disipline.UI.Habits.addSession

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disipline.UI.Habits.TimeField
import com.example.disipline.UI.Habits.habit_screen.EmojiCircle
import com.example.disipline.UI.Habits.habit_screen.Habit
import com.example.disipline.UI.Habits.habit_screen.PrimaryButton
import com.example.disipline.UI.Habits.habit_screen.ScreenTopBar
import com.example.disipline.UI.Habits.habit_screen.dialogFieldColors
import com.example.disipline.Viewmodel.AddSessionUiState
import com.example.disipline.Viewmodel.AddSessionViewModel
import com.example.disipline.Viewmodel.formatDate
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.AppColors
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.Outline
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary
import java.util.Calendar

// SCREEN 3 - ADD SESSION

@Composable
fun DateField(label: String, dateMillis: Long, onPicked: (Long) -> Unit) {
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
                    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val picked = Calendar.getInstance().apply {
                                set(year, month, day)
                            }
                            onPicked(picked.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDate(dateMillis),
                color = TextPrimary,
                fontSize = 15.sp
            )
        }
    }
}


// Connects the ViewModel to the UI.
// The ViewModel is kept per habit, so
// a half-filled form survives rotation and the back button.
@Composable
fun AddSessionScreen(
    habit: Habit,
    onBack: () -> Unit,
    onSave: (name: String, start: String, end: String, goal: String, date: String) -> Unit,
    viewModel: AddSessionViewModel = viewModel(key = "add_session_${habit.id}")
) {
    val state by viewModel.uiState.collectAsState()

    AddSessionContent(
        habit = habit,
        state = state,
        onNameChange = viewModel::onNameChange,
        onGoalChange = viewModel::onGoalChange,
        onStartChange = viewModel::onStartChange,
        onEndChange = viewModel::onEndChange,
        onDateChange = viewModel::onDateChange,
        onBack = onBack,
        onSave = {
            onSave(
                state.name.trim(),
                state.startText,
                state.endText,
                state.goal.trim(),
                state.dateText
            )
            viewModel.reset()
        }
    )
}

// ---------------------------------------------------------------------
// Stateless UI: only draws the state it is given.
// ---------------------------------------------------------------------
@Composable
fun AddSessionContent(
    habit: Habit,
    state: AddSessionUiState,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onStartChange: (Int) -> Unit,
    onEndChange: (Int) -> Unit,
    onDateChange: (Long) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                PrimaryButton(
                    text = "Save Session",
                    enabled = state.canSave,
                    onClick = onSave
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScreenTopBar(title = "Add Session", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Which habit this session belongs to
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmojiCircle(emoji = habit.emoji, size = 44)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = habit.title,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily Habit",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    label = { Text("Session name", color = TextSecondary) },
                    placeholder = { Text("e.g. Learn DSA concepts", color = TextSecondary) },
                    colors = dialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                DateField(
                    label = "Date",
                    dateMillis = state.dateMillis,
                    onPicked = onDateChange
                )

                TimeField(
                    label = "Start time",
                    minutes = state.startMinutes,
                    onPicked = onStartChange
                )
                TimeField(
                    label = "End time",
                    minutes = state.endMinutes,
                    onPicked = onEndChange
                )

                if (!state.timeOk) {
                    Text(
                        text = "End time must be after start time",
                        color = AppColors.Error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = state.goal,
                    onValueChange = onGoalChange,
                    minLines = 3,
                    label = { Text("Goal / Description", color = TextSecondary) },
                    placeholder = {
                        Text("e.g. Read notes and watch lecture videos", color = TextSecondary)
                    },
                    colors = dialogFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Live preview of the session
                Text(
                    text = "Preview",
                    color = Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmojiCircle(emoji = habit.emoji, size = 44)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = state.name.ifBlank { "Session name" },
                            color = if (state.name.isBlank()) TextSecondary else TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.dateText,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${state.startText} - ${state.endText}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        if (state.goal.isNotBlank()) {
                            Text(
                                text = state.goal,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}