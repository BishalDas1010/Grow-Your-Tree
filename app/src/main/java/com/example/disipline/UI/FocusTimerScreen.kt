package com.example.disipline.UI

// =====================================================================
// FOCUS TIMER SCREEN (the screen shown WHILE a session is running)
//
// Values shown come from TWO sources:
//   - `focusSettings`  -> mode, plan, blocked apps (chosen on the Focus screen)
//   - `state`          -> live countdown (produced by FocusTimerViewModel)
//
// ForestGrowthView() lives in ForestGrowthView.kt in the same package.
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

data class FocusTimerUiState(
    val taskName: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean,
    val whiteNoiseOn: Boolean,
    val completed: Boolean = false
)

private fun formatTime(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val minutes = safe / 60
    val seconds = safe % 60
    return "%02d:%02d".format(minutes, seconds)
}


private val ICON_BACK = R.drawable.ic_launcher_foreground
private val EndRed = Color(0xFFE5484D)
private val TOP_BAR_ICON_SLOT = 48.dp

// =====================================================================
// 3. MAIN SCREEN
// =====================================================================

@Composable
fun FocusTimerScreen(
    // Settings chosen on the previous screen (mode, plan, blocked apps).
    // Null when the screen is opened directly without going through Focus.
    focusSettings: FocusSettings? = null,
    // Live countdown state produced by FocusTimerViewModel.
    state: FocusTimerUiState,
    onBack: () -> Unit = {},
    onPauseResume: () -> Unit = {},
    onEnd: () -> Unit = {},
    onWhiteNoiseToggle: () -> Unit = {},

) {
    val progress = if (state.totalSeconds > 0) {
        (state.remainingSeconds.toFloat() / state.totalSeconds).coerceIn(0f, 1f)
    } else {
        0f
    }

    val growthProgress = (1f - progress).coerceIn(0f, 1f)

    Scaffold(
        containerColor = BgDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerTopBar(taskName = state.taskName, onBack = onBack)

            Spacer(Modifier.weight(1f))

            TimerRing(
                progress = progress,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.82f)
                        .clip(CircleShape)
                        .background(IconCircle)
                        .semantics { contentDescription = "Growing tree" },
                    contentAlignment = Alignment.Center
                ) {
                    ForestGrowthView(
                        progress = growthProgress,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Big time
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

            // ---- Details pulled from the Focus screen ----
            if (focusSettings != null) {
                Spacer(Modifier.height(16.dp))

                // Mode chip (Focus / Short Break / Long Break)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Surface)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = focusSettings.mode.label,
                        color = Accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Plan: "Focus 7m  →  Break 5m  →  Focus 7m"
                if (focusSettings.plan.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = focusSettings.plan.joinToString("  →  ") {
                            val tag = if (it.type == SegmentType.Focus) "Focus" else "Break"
                            "$tag ${it.minutes}m"
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Blocked apps
                if (focusSettings.blockedApps.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Blocked: ${focusSettings.blockedApps.joinToString(", ")}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.weight(1f))

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

@Composable
private fun TimerTopBar(taskName: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(TOP_BAR_ICON_SLOT)
        ) {
            Icon(
                painter = painterResource(ICON_BACK),
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Focus Session",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = taskName,
                color = TextSecondary,
                fontSize = 14.sp,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(TOP_BAR_ICON_SLOT))
    }
}

@Composable
private fun TimerRing(
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val inset = strokeWidth / 2
            val topLeft = Offset(inset, inset)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            drawArc(
                color = IconCircle,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            if (progress > 0f) {
                val fullSweep = progress >= 1f
                drawArc(
                    color = Accent,
                    startAngle = -90f,
                    sweepAngle = if (fullSweep) 360f else 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidth,
                        cap = if (fullSweep) StrokeCap.Butt else StrokeCap.Round
                    )
                )
            }
        }

        content()
    }
}

@Composable
private fun ControlsRow(
    isRunning: Boolean,
    whiteNoiseOn: Boolean,
    onPauseResume: () -> Unit,
    onEnd: () -> Unit,
    onWhiteNoiseToggle: () -> Unit
) {
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
                    .then(
                        if (whiteNoiseOn) Modifier.border(2.dp, Accent, CircleShape)
                        else Modifier
                    )
                    .clickable(onClick = onWhiteNoiseToggle)
                    .semantics {
                        contentDescription =
                            if (whiteNoiseOn) "White noise on" else "White noise off"
                    },
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
                    .clickable(onClick = onPauseResume)
                    .semantics {
                        contentDescription =
                            if (isRunning) "Pause timer" else "Resume timer"
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isRunning) {
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
                    .clickable(onClick = onEnd)
                    .semantics { contentDescription = "End session" },
                contentAlignment = Alignment.Center
            ) {
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

@Composable
private fun ControlColumn(
    label: String,
    labelColor: Color,
    modifier: Modifier = Modifier,
    button: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            button()
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}
