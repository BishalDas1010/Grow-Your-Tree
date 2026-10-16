package com.example.disipline.UI

// =====================================================================
// FOCUS TIMER SCREEN - matches the reference design
//
//  - Deep green background with soft curved waves
//  - Leaf image (bottom-left) loaded from YOUR drawable: R.drawable.leaf
//  - Status bar (notifications / clock / battery) stays visible
//  - Back button, centred title + tagline
//  - Ring: dim track, bright mint progress, glowing knob at the tip
//  - Big time, "/ total", mode pill card (target icon, divider, chevron)
//  - Controls on ONE centre line: White Noise | Pause (big) | End
//
// ForestGrowthView() lives in ForestGrowthView.kt in the same package.
// R.drawable.left  = your back-arrow drawable
// R.drawable.leaf  = your leaf image  (rename below if your file differs)
// =====================================================================

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.disipline.R
import kotlin.math.PI
import com.example.disipline.ui.theme.AppColors
import kotlin.math.cos
import kotlin.math.sin

// =====================================================================
// 1. PALETTE (Centralized AppColors)
// =====================================================================

private object Deep {
    val Bg            get() = AppColors.Background
    val BgTop         get() = AppColors.Background
    val Wave          get() = AppColors.ElevatedSurface.copy(alpha = 0.5f)
    val InnerCircle   get() = AppColors.Background
    val Surface       get() = AppColors.Surface
    val Card          get() = AppColors.Surface
    val Border        get() = AppColors.Outline
    val Track         get() = AppColors.ElevatedSurface
    val Accent        get() = AppColors.PrimaryAccent
    val AccentSoft    get() = AppColors.PrimaryButton
    val AccentDeep    get() = AppColors.PrimaryAccent
    val TextPrimary   get() = AppColors.PrimaryText
    val TextSecondary get() = AppColors.SecondaryText
    val IconGray      get() = AppColors.SecondaryText
    val EndBg         get() = AppColors.Error.copy(alpha = 0.15f)
    val EndBorder     get() = AppColors.Error.copy(alpha = 0.4f)
    val EndIcon       get() = AppColors.Error
}

private val TOP_BAR_BUTTON = 40.dp
private val CONTROL_SLOT = 80.dp          // height every control is centred in
private val SMALL_BUTTON = 60.dp
private val BIG_BUTTON = 80.dp

// =====================================================================
// 2. DATA
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
    return "%02d:%02d".format(safe / 60, safe % 60)
}

private fun modeTagline(label: String): String = when {
    label.contains("long", ignoreCase = true) -> "Time to recharge"
    label.contains("break", ignoreCase = true) -> "Take a breather"
    else -> "Stay on track!"
}

// =====================================================================
// 3. MAIN SCREEN
// =====================================================================

@Composable
fun FocusTimerScreen(
    focusSettings: FocusSettings? = null,
    state: FocusTimerUiState,
    onBack: () -> Unit = {},
    onPauseResume: () -> Unit = {},
    onEnd: () -> Unit = {},
    onWhiteNoiseToggle: () -> Unit = {},
    onModeClick: () -> Unit = {},
) {
    // Status bar stays visible with light icons on the dark background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    val progress = if (state.totalSeconds > 0) {
        (state.remainingSeconds.toFloat() / state.totalSeconds).coerceIn(0f, 1f)
    } else 0f
    val growthProgress = (1f - progress).coerceIn(0f, 1f)

    val modeLabel = focusSettings?.mode?.label ?: "Focus"

    Scaffold(containerColor = Deep.Bg) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Deep.BgTop, Deep.Bg)))
        ) {
            // Decorative waves. Draws behind the status bar too.
            BackgroundDecor(modifier = Modifier.fillMaxSize())

            // Leaf image from your drawable folder (bottom-left)
            Image(
                painter = painterResource(id = R.drawable.leaff),   // <- your file name
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 8.dp)
                    .size(width = 70.dp, height = 110.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimerTopBar(
                    subtitle = state.taskName.ifBlank { "Focus. Be Productive." },
                    onBack = onBack
                )

                // Ring area
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val ringSize: Dp = minOf(maxWidth * 0.68f, maxHeight)
                    TimerRing(progress = progress, modifier = Modifier.size(ringSize)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.86f)
                                .clip(CircleShape)
                                .background(Deep.InnerCircle)
                                .semantics { contentDescription = "Growing tree" },
                            contentAlignment = Alignment.Center
                        ) {
                            ForestGrowthView(
                                progress = growthProgress,
                                modifier = Modifier.fillMaxSize(0.8f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = formatTime(state.remainingSeconds),
                    color = Deep.TextPrimary,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "/ ${formatTime(state.totalSeconds)}",
                    color = Deep.TextSecondary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                ModePill(
                    label = modeLabel,
                    tagline = modeTagline(modeLabel),
                    onClick = onModeClick
                )

                Spacer(Modifier.height(32.dp))

                ControlsRow(
                    isRunning = state.isRunning,
                    whiteNoiseOn = state.whiteNoiseOn,
                    onPauseResume = onPauseResume,
                    onEnd = onEnd,
                    onWhiteNoiseToggle = onWhiteNoiseToggle
                )

                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

// =====================================================================
// 4. BACKGROUND (waves only - leaf is an image now)
// =====================================================================

@Composable
private fun BackgroundDecor(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Top-left soft wave
        val top = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.62f, 0f)
            cubicTo(w * 0.40f, h * 0.05f, w * 0.12f, h * 0.10f, 0f, h * 0.24f)
            close()
        }
        drawPath(
            top,
            Brush.linearGradient(
                listOf(Deep.Wave.copy(alpha = 0.85f), Deep.Wave.copy(alpha = 0f)),
                start = Offset(0f, 0f),
                end = Offset(w * 0.6f, h * 0.2f)
            )
        )

        // Bottom wave
        val bottom = Path().apply {
            moveTo(0f, h * 0.90f)
            cubicTo(w * 0.30f, h * 0.87f, w * 0.65f, h * 0.97f, w, h * 0.88f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            bottom,
            Brush.verticalGradient(
                listOf(Deep.Wave.copy(alpha = 0.75f), Deep.Wave.copy(alpha = 0.15f)),
                startY = h * 0.86f,
                endY = h
            )
        )
    }
}

// =====================================================================
// 5. UI PIECES
// =====================================================================

@Composable
private fun TimerTopBar(subtitle: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(TOP_BAR_BUTTON)
                .clip(CircleShape)
                .background(Deep.Surface.copy(alpha = 0.8f))
                .border(1.dp, Deep.Border, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.left),
                contentDescription = "Back",
                tint = Deep.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = TOP_BAR_BUTTON + 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Focus Session",
                color = Deep.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Deep.TextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
            val strokeWidth = 12.dp.toPx()
            val inset = strokeWidth / 2
            val topLeft = Offset(inset + 6.dp.toPx(), inset + 6.dp.toPx())
            val arcSize = Size(
                size.width - strokeWidth - 12.dp.toPx(),
                size.height - strokeWidth - 12.dp.toPx()
            )

            // Track
            drawArc(
                color = Deep.Track.copy(alpha = 0.85f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            if (progress > 0f) {
                val full = progress >= 1f
                val sweep = if (full) 360f else 360f * progress

                // Soft glow under the progress arc
                drawArc(
                    color = Deep.Accent.copy(alpha = 0.16f),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidth + 10.dp.toPx(),
                        cap = if (full) StrokeCap.Butt else StrokeCap.Round
                    )
                )
                // Progress
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Deep.AccentDeep, Deep.Accent, Deep.AccentDeep)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidth,
                        cap = if (full) StrokeCap.Butt else StrokeCap.Round
                    )
                )

                // Glowing knob at the tip
                if (!full) {
                    val radius = arcSize.width / 2f
                    val centre = Offset(size.width / 2f, size.height / 2f)
                    val angle = (-90f + sweep) * (PI / 180.0)
                    val tip = Offset(
                        centre.x + radius * cos(angle).toFloat(),
                        centre.y + radius * sin(angle).toFloat()
                    )
                    drawCircle(Deep.Accent.copy(alpha = 0.30f), radius = 16.dp.toPx(), center = tip)
                    drawCircle(Deep.AccentSoft, radius = 9.dp.toPx(), center = tip)
                }
            }
        }
        content()
    }
}

@Composable
private fun ModePill(label: String, tagline: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .fillMaxWidth(0.76f)
            .height(58.dp)
            .clip(shape)
            .background(Deep.Card.copy(alpha = 0.9f))
            .border(1.dp, Deep.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TargetIcon(modifier = Modifier.size(38.dp))

        Spacer(Modifier.width(14.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(0.55f)
                .background(Deep.Border)
        )
        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = label,
                color = Deep.Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = tagline,
                color = Deep.TextSecondary,
                fontSize = 13.sp,
                maxLines = 1
            )
        }

        ChevronDown(modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TargetIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val sw = 2.6.dp.toPx()
        drawCircle(Deep.Accent, radius = size.minDimension * 0.46f, center = c, style = Stroke(sw))
        drawCircle(Deep.Accent, radius = size.minDimension * 0.28f, center = c, style = Stroke(sw))
        drawCircle(Deep.Accent, radius = size.minDimension * 0.08f, center = c)
        // arrow
        val tip = Offset(size.width * 0.86f, size.height * 0.14f)
        drawLine(Deep.Accent, c, tip, strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(Deep.Accent, tip, Offset(tip.x - size.width * 0.16f, tip.y), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(Deep.Accent, tip, Offset(tip.x, tip.y + size.height * 0.16f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
private fun ChevronDown(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sw = 2.4.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.32f)
            lineTo(size.width * 0.5f, size.height * 0.68f)
            lineTo(size.width * 0.85f, size.height * 0.32f)
        }
        drawPath(
            path,
            Deep.TextPrimary,
            style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun SpeakerIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(w * 0.05f, h * 0.38f)
            lineTo(w * 0.25f, h * 0.38f)
            lineTo(w * 0.52f, h * 0.16f)
            lineTo(w * 0.52f, h * 0.84f)
            lineTo(w * 0.25f, h * 0.62f)
            lineTo(w * 0.05f, h * 0.62f)
            close()
        }
        drawPath(body, color)
        val sw = 2.2.dp.toPx()
        listOf(0.20f, 0.36f).forEach { r ->
            val rr = w * r
            drawArc(
                color = color,
                startAngle = -50f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(w * 0.54f - rr, h * 0.5f - rr),
                size = Size(rr * 2f, rr * 2f),
                style = Stroke(width = sw, cap = StrokeCap.Round)
            )
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // ---- Left: White Noise ----
        ControlColumn(
            label = "White Noise",
            labelColor = if (whiteNoiseOn) Deep.Accent else Deep.TextSecondary,
            buttonSize = SMALL_BUTTON,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(SMALL_BUTTON)
                    .clip(CircleShape)
                    .background(Deep.Surface.copy(alpha = 0.7f))
                    .border(
                        width = if (whiteNoiseOn) 2.dp else 1.dp,
                        color = if (whiteNoiseOn) Deep.Accent else Deep.Border,
                        shape = CircleShape
                    )
                    .clickable(onClick = onWhiteNoiseToggle)
                    .semantics {
                        contentDescription =
                            if (whiteNoiseOn) "White noise on" else "White noise off"
                    },
                contentAlignment = Alignment.Center
            ) {
                SpeakerIcon(
                    color = if (whiteNoiseOn) Deep.Accent else Deep.IconGray,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // ---- Center: Pause / Resume (big, glowing) ----
        ControlColumn(
            label = if (isRunning) "Pause" else "Resume",
            labelColor = Deep.TextPrimary,
            buttonSize = BIG_BUTTON,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(BIG_BUTTON)
                    .drawBehind {
                        drawCircle(
                            Brush.radialGradient(
                                listOf(Deep.Accent.copy(alpha = 0.35f), Color.Transparent),
                                radius = size.minDimension * 0.85f
                            )
                        )
                    }
                    .clip(CircleShape)
                    .background(Deep.Surface)
                    .border(3.dp, Deep.Accent, CircleShape)
                    .clickable(onClick = onPauseResume)
                    .semantics {
                        contentDescription =
                            if (isRunning) "Pause timer" else "Resume timer"
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isRunning) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(width = 8.dp, height = 28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Deep.TextPrimary)
                            )
                        }
                    }
                } else {
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val w = size.width
                        val h = size.height
                        val path = Path().apply {
                            moveTo(w * 0.20f, 0f)
                            lineTo(w * 0.96f, h / 2f)
                            lineTo(w * 0.20f, h)
                            close()
                        }
                        drawPath(path, Deep.TextPrimary)
                    }
                }
            }
        }

        // ---- Right: End ----
        ControlColumn(
            label = "End",
            labelColor = Deep.TextSecondary,
            buttonSize = SMALL_BUTTON,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(SMALL_BUTTON)
                    .clip(CircleShape)
                    .background(Deep.EndBg)
                    .border(1.5.dp, Deep.EndBorder, CircleShape)
                    .clickable(onClick = onEnd)
                    .semantics { contentDescription = "End session" },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Deep.EndIcon)
                )
            }
        }
    }
}

/**
 * Every button is centred inside the same fixed-height slot, so all three
 * share one centre line. The label sits directly under each button's own
 * edge (so "Pause" is lower than the other two, exactly like the design).
 */
@Composable
private fun ControlColumn(
    label: String,
    labelColor: Color,
    buttonSize: Dp,
    modifier: Modifier = Modifier,
    button: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.height(CONTROL_SLOT),
            contentAlignment = Alignment.Center
        ) { button() }

        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 13.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = -(CONTROL_SLOT - buttonSize) / 2)
        )
    }
}

// =====================================================================
// 6. PREVIEW  (open Split view in Android Studio to see it)
// =====================================================================

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 800,
    name = "Focus Timer"
)
@Composable
private fun FocusTimerScreenPreview() {
    FocusTimerScreen(
        state = FocusTimerUiState(
            taskName = "Focus. Be Productive.",
            totalSeconds = 25 * 60,
            remainingSeconds = 24 * 60 + 8,
            isRunning = true,
            whiteNoiseOn = false
        )
    )
}