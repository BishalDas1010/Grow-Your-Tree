package com.example.disipline.UI



// =====================================================================
// FOCUS TIMER SCREEN (the screen shown WHILE a session is running)
//
//   1. DATA        -> FocusTimerUiState + time formatting helper
//   2. ICONS       -> one place to change icons / colors
//   3. MAIN SCREEN -> FocusTimerScreen() - the only function other files call
//   4. UI PIECES   -> small private composables the screen is built from
//   5. PREVIEW     -> sample for Android Studio
//
// HOW IT CONNECTS TO THE PREVIOUS SCREEN:
//   FocusSessionScreen gives you a FocusSettings (minutes, task, sound...).
//   Your ViewModel turns that into a FocusTimerUiState:
//       totalSeconds     = settings.minutes * 60
//       remainingSeconds = totalSeconds            (then counts down)
//       taskName         = settings.task
//       whiteNoiseOn     = settings.ambientSound
//
// HOW TO LINK IT TO YOUR APP (example, in your NavHost):
//
//   FocusTimerScreen(
//       state = viewModel.timerState.collectAsState().value,
//       onBack = { navController.popBackStack() },
//       onPauseResume = { viewModel.togglePause() },
//       onEnd = { viewModel.endSession(); navController.popBackStack() },
//       onWhiteNoiseToggle = { viewModel.toggleWhiteNoise() }
//   )
//
// IMPORTANT: this file does NOT count time. The countdown (a coroutine or
// a foreground service) lives in your ViewModel / service and updates
// remainingSeconds every second. The screen just redraws whatever it gets.
// =====================================================================

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.R
import com.example.disipline.ui.theme.Accent
import com.example.disipline.ui.theme.BgDark
import com.example.disipline.ui.theme.IconCircle
import com.example.disipline.ui.theme.Surface
import com.example.disipline.ui.theme.TextPrimary
import com.example.disipline.ui.theme.TextSecondary

// =====================================================================
// 1. DATA
// =====================================================================

// Everything the timer screen needs. Time is in SECONDS.
data class FocusTimerUiState(
    val taskName: String,           // "Study DSA" (shown under the title)
    val totalSeconds: Int,          // full length, e.g. 25 * 60
    val remainingSeconds: Int,      // counts down from totalSeconds to 0
    val isRunning: Boolean,         // true = running (shows Pause), false = paused (shows Resume)
    val whiteNoiseOn: Boolean       // white noise currently playing
)

// 1472 -> "24:32"
private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// =====================================================================
// 2. ICONS / COLORS
// Placeholder icon. To use a real one: right click res/drawable ->
// New -> Vector Asset, then change the line below.
// The tree, speaker, pause bars and stop square are drawn with emoji /
// shapes, so they need no drawables. To use a real tree picture later,
// replace the Text("🌳") inside FocusTimerScreen with an Image(...).
// =====================================================================

private val ICON_BACK = R.drawable.ic_launcher_foreground

// Red used by the End button
private val EndRed = Color(0xFFE5484D)

// =====================================================================
// 3. MAIN SCREEN - call this from your navigation
// =====================================================================

@Composable
fun FocusTimerScreen(
    state: FocusTimerUiState,                  // data to show (from ViewModel)
    onBack: () -> Unit = {},                   // back arrow tapped
    onPauseResume: () -> Unit = {},            // center button tapped
    onEnd: () -> Unit = {},                    // red End button tapped
    onWhiteNoiseToggle: () -> Unit = {}        // white noise button tapped
) {
    // Share of time LEFT (1.0 = full ring, 0.0 = empty ring).
    // coerceIn keeps it safe if the value ever goes out of range.
    // To make the ring FILL UP instead, use 1f - this value.
    val progress = if (state.totalSeconds > 0) {
        (state.remainingSeconds.toFloat() / state.totalSeconds).coerceIn(0f, 1f)
    } else {
        0f
    }

    Scaffold(containerColor = BgDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back arrow + "Focus Session" + task name
            TimerTopBar(taskName = state.taskName, onBack = onBack)

            // weight(1f) spacers share the free space, which centers the ring + time
            Spacer(Modifier.weight(1f))

            // The ring with the tree inside
            TimerRing(
                progress = progress,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                // Dark circle behind the tree
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.82f)
                        .clip(CircleShape)
                        .background(IconCircle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌳", fontSize = 110.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Big remaining time and the total under it
            Text(
                text = formatTime(state.remainingSeconds),
                color = TextPrimary,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/ ${formatTime(state.totalSeconds)}",
                color = TextSecondary,
                fontSize = 18.sp
            )

            Spacer(Modifier.weight(1f))

            // White Noise | Pause | End
            ControlsRow(
                isRunning = state.isRunning,
                whiteNoiseOn = state.whiteNoiseOn,
                onPauseResume = onPauseResume,
                onEnd = onEnd,
                onWhiteNoiseToggle = onWhiteNoiseToggle
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// =====================================================================
// 4. UI PIECES
// =====================================================================

// ---- Top: [back]   Focus Session / Study DSA ----
@Composable
private fun TimerTopBar(taskName: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
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

        // Two lines of text centered in the bar
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Focus Session",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = taskName, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// ---- Circular progress ring. Anything passed as content() is drawn in the middle. ----
@Composable
private fun TimerRing(
    progress: Float,                       // 0f..1f, share of the ring that is green
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.aspectRatio(1f),   // keeps the ring a perfect square/circle
        contentAlignment = Alignment.Center
    ) {
        // Canvas = free drawing area. We draw two arcs on top of each other.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val inset = strokeWidth / 2                       // keeps the stroke inside the canvas
            val topLeft = Offset(inset, inset)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            // 1) Dark full circle = the empty track
            drawArc(
                color = IconCircle,
                startAngle = -90f,                            // -90 = start at the top
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // 2) Green arc on top = the progress
            drawArc(
                color = Accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,                 // how far around the circle
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        content()
    }
}

// ---- Bottom controls: White Noise | Pause | End ----
@Composable
private fun ControlsRow(
    isRunning: Boolean,
    whiteNoiseOn: Boolean,
    onPauseResume: () -> Unit,
    onEnd: () -> Unit,
    onWhiteNoiseToggle: () -> Unit
) {
    // Top-aligned so all three labels line up even though the buttons differ in size.
    // Each column gets weight(1f), so the three controls are spread evenly.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // ---- Left: White Noise ----
        ControlColumn(
            label = "White Noise",
            labelColor = if (whiteNoiseOn) Accent else TextSecondary,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Surface)
                    // green ring only while white noise is on
                    .then(
                        if (whiteNoiseOn) Modifier.border(2.dp, Accent, CircleShape)
                        else Modifier
                    )
                    .clickable(onClick = onWhiteNoiseToggle),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔊", fontSize = 22.sp)
            }
        }

        // ---- Center: Pause / Resume ----
        ControlColumn(
            label = if (isRunning) "Pause" else "Resume",
            labelColor = TextSecondary,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Surface)
                    .border(2.dp, Accent, CircleShape)
                    .clickable(onClick = onPauseResume),
                contentAlignment = Alignment.Center
            ) {
                if (isRunning) {
                    // Pause icon = two small vertical bars
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 6.dp, height = 24.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(TextPrimary)
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 6.dp, height = 24.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(TextPrimary)
                        )
                    }
                } else {
                    // Resume icon = play triangle
                    Text(text = "▶", color = TextPrimary, fontSize = 26.sp)
                }
            }
        }

        // ---- Right: End ----
        ControlColumn(
            label = "End",
            labelColor = TextSecondary,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EndRed)
                    .clickable(onClick = onEnd),
                contentAlignment = Alignment.Center
            ) {
                // Stop icon = small white rounded square
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White)
                )
            }
        }
    }
}

// ---- One control = fixed-height slot for the button + a label under it ----
@Composable
private fun ControlColumn(
    label: String,
    labelColor: Color,
    modifier: Modifier = Modifier,       // parent passes weight(1f) here
    button: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Every slot is 72dp tall and centers its button, so smaller buttons
        // sit on the same line as the big Pause button
        Box(
            modifier = Modifier.height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            button()
        }

        Spacer(Modifier.height(8.dp))

        Text(text = label, color = labelColor, fontSize = 12.sp)
    }
}

// =====================================================================
// 5. PREVIEW - delete when you wire in the real ViewModel.
// =====================================================================

@Preview(showBackground = true, backgroundColor = 0xFF08140F, widthDp = 390, heightDp = 800)
@Composable
private fun FocusTimerScreenPreview() {
    FocusTimerScreen(
        state = FocusTimerUiState(
            taskName = "Study DSA",
            totalSeconds = 25 * 60,
            remainingSeconds = 24 * 60 + 32,   // shows 24:32
            isRunning = true,
            whiteNoiseOn = false
        )
    )
}