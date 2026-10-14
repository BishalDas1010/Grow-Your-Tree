package com.example.disipline.UI.taskcomple


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------- Colors ----------
private val BgTop = Color(0xFF0E2A22)
private val BgBottom = Color(0xFF071612)
private val CardBorder = Color(0xFF1F4A3B)
private val StatsBg = Color(0xFF0F2A22)
private val StatsDivider = Color(0xFF1E3D33)
private val Mint = Color(0xFF6BF0A0)
private val MintDark = Color(0xFF06261A)
private val Gold = Color(0xFFFFC83D)
private val SubText = Color(0xFFB8CFC6)
private val LabelText = Color(0xFF8FA89E)

// ---------- Animation state ----------
private class CompletionAnim {
    val title = Animatable(0f)
    val emojiRotation = Animatable(0f)
    val island = Animatable(0f)
    val trunk = Animatable(0f)
    val foliage = Animatable(0f)
    val confetti = Animatable(0f)
    val leaves = Animatable(0f)
    val statsCard = Animatable(0f)
    val stats = List(3) { Animatable(0f) }
    val counter = Animatable(0f)
    val buttons = List(2) { Animatable(0f) }
}

/** Fade + slide-up driven in the draw phase (no recomposition per frame). */
private fun Modifier.entrance(progress: () -> Float, slideDp: Float = 24f): Modifier =
    graphicsLayer {
        val p = progress().coerceIn(0f, 1f)
        alpha = p
        translationY = (1f - p) * slideDp.dp.toPx()
    }

// ---------- Screen ----------
@Composable
fun FocusCompleteScreen(
    focusSeconds: Int = 25 * 60,
    xp: Int = 10,
    treesGrown: Int = 1,
    onClose: () -> Unit = {},
    onPlantAnother: () -> Unit = {},
    onBackHome: () -> Unit = {}
) {
    val anim = remember { CompletionAnim() }
    val haptic = LocalHapticFeedback.current

    // Whole completion sequence, runs once when the screen appears
    LaunchedEffect(Unit) {
        // 1. Title pops in
        launch {
            anim.title.animateTo(
                1f,
                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
            )
        }

        // 2. Island rises -> trunk grows -> foliage bursts out -> confetti + leaves
        launch {
            delay(150)
            anim.island.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            anim.trunk.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
            launch {
                anim.foliage.animateTo(
                    1f,
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessVeryLow)
                )
            }
            delay(400)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            launch { anim.confetti.animateTo(1f, tween(2200, easing = LinearOutSlowInEasing)) }
            launch { anim.leaves.animateTo(1f, tween(1000)) }
            launch {
                // party emoji wobble
                anim.emojiRotation.animateTo(-18f, tween(120))
                anim.emojiRotation.animateTo(18f, tween(160))
                anim.emojiRotation.animateTo(-10f, tween(140))
                anim.emojiRotation.animateTo(0f, tween(140))
            }
        }

        // 3. Stats card slides in
        launch {
            delay(1500)
            anim.statsCard.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }

        // 4. Stat items pop in one by one, numbers count up
        launch {
            delay(1700)
            anim.stats.forEachIndexed { i, a ->
                launch {
                    delay(i * 120L)
                    a.animateTo(
                        1f,
                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                    )
                }
            }
            launch {
                delay(100)
                anim.counter.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
            }
        }

        // 5. Buttons slide up
        launch {
            delay(2200)
            anim.buttons.forEachIndexed { i, a ->
                launch {
                    delay(i * 130L)
                    a.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance({ anim.title.value }, slideDp = 0f),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(painterResource(R.drawable.ic_launcher_foreground), contentDescription = "Close", tint = Color.White)
                }
            }

            // Title + emoji
            Row(
                modifier = Modifier.graphicsLayer {
                    val p = anim.title.value
                    val sc = 0.6f + 0.4f * p
                    scaleX = sc
                    scaleY = sc
                    alpha = p.coerceIn(0f, 1f)
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Great Job! ",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "\uD83C\uDF89",
                    fontSize = 30.sp,
                    modifier = Modifier.graphicsLayer { rotationZ = anim.emojiRotation.value }
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "You completed a focus session!",
                color = SubText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.entrance({ (anim.title.value - 0.3f) / 0.7f }, slideDp = 12f)
            )

            Spacer(Modifier.height(12.dp))

            TreeScene(
                anim = anim,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(Modifier.height(12.dp))

            val counter = anim.counter.value
            StatsRow(
                anim = anim,
                focusTime = formatTime((focusSeconds * counter).toInt()),
                xpText = "+${(xp * counter).roundToInt()} XP",
                treesText = treesGrown.toString()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onPlantAnother,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .entrance({ anim.buttons[0].value }, slideDp = 40f),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Mint,
                    contentColor = MintDark
                )
            ) {
                Text("Plant Another Tree", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .entrance({ anim.buttons[1].value }, slideDp = 40f),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatsBg,
                    contentColor = Color.White
                )
            ) {
                Text("Back to Home", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun formatTime(totalSeconds: Int): String =
    "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

// ---------- Stats ----------
@Composable
private fun StatsRow(
    anim: CompletionAnim,
    focusTime: String,
    xpText: String,
    treesText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .entrance({ anim.statsCard.value }, slideDp = 30f)
            .background(StatsBg, RoundedCornerShape(18.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            icon =painterResource(R.drawable.ic_launcher_foreground),
            tint = Mint,
            value = focusTime,
            label = "Focus Time",
            progress = { anim.stats[0].value },
            modifier = Modifier.weight(1f)
        )
        VerticalDivider()
        StatItem(
            icon =painterResource(R.drawable.ic_launcher_foreground),
            tint = Gold,
            value = xpText,
            label = "Experience",
            progress = { anim.stats[1].value },
            modifier = Modifier.weight(1f)
        )
        VerticalDivider()
        StatItem(
            icon =painterResource(R.drawable.ic_launcher_foreground),
            tint = Mint,
            value = treesText,
            label = "Tree Grown",
            progress = { anim.stats[2].value },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(StatsDivider)
    )
}

@Composable
private fun StatItem(
    icon: Painter,
    tint: Color,
    value: String,
    label: String,
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.graphicsLayer {
            val p = progress()
            alpha = p.coerceIn(0f, 1f)
            val sc = 0.6f + 0.4f * p
            scaleX = sc
            scaleY = sc
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(label, color = LabelText, fontSize = 11.sp)
    }
}

// ---------- Falling leaves ----------
private data class LeafSpec(
    val xFrac: Float,
    val size: Dp,
    val color: Color,
    val phase: Float,
    val speed: Int,      // integer so the loop wraps seamlessly
    val dir: Float,      // spin direction
    val sway: Float      // horizontal sway in dp
)

private val leafSpecs = listOf(
    LeafSpec(0.08f, 16.dp, Color(0xFFF2A93B), 0.00f, 1, 1f, 14f),
    LeafSpec(0.20f, 12.dp, Color(0xFF7ED957), 0.35f, 2, -1f, 10f),
    LeafSpec(0.32f, 12.dp, Color(0xFF7ED957), 0.70f, 1, 1f, 12f),
    LeafSpec(0.48f, 10.dp, Color(0xFFF2A93B), 0.15f, 2, -1f, 8f),
    LeafSpec(0.62f, 14.dp, Color(0xFFF2A93B), 0.55f, 1, -1f, 16f),
    LeafSpec(0.76f, 12.dp, Color(0xFF7ED957), 0.85f, 2, 1f, 10f),
    LeafSpec(0.88f, 18.dp, Color(0xFF8FE36B), 0.25f, 1, 1f, 14f),
    LeafSpec(0.94f, 12.dp, Color(0xFFF2A93B), 0.60f, 2, -1f, 8f)
)

// ---------- Confetti ----------
private data class Particle(
    val angle: Float,
    val speed: Float,
    val sizeFrac: Float,
    val color: Color,
    val spin: Float,
    val rect: Boolean
)

private val confettiColors = listOf(
    Color(0xFF6BF0A0), Color(0xFFFFC83D), Color(0xFFF2A93B),
    Color(0xFF8FE36B), Color(0xFFFFFFFF), Color(0xFF4DD0E1)
)

private val particles: List<Particle> = run {
    val r = Random(42)
    List(48) {
        Particle(
            angle = -r.nextFloat() * PI.toFloat(),          // upward half-circle
            speed = 0.20f + r.nextFloat() * 0.45f,
            sizeFrac = 0.018f + r.nextFloat() * 0.018f,
            color = confettiColors[r.nextInt(confettiColors.size)],
            spin = (r.nextFloat() - 0.5f) * 3f,
            rect = r.nextBoolean()
        )
    }
}

// ---------- Tree scene ----------
@Composable
private fun TreeScene(anim: CompletionAnim, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "idle")
    val leafTime by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "leafTime"
    )
    val bob by transition.animateFloat(
        -5f, 5f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val sway by transition.animateFloat(
        -1.5f, 1.5f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
    )
    val pulse by transition.animateFloat(
        0.94f, 1.06f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    BoxWithConstraints(modifier = modifier) {
        val h = maxHeight

        // Tree (floats gently once fully grown)
        TreeCanvas(
            islandP = { anim.island.value },
            trunkP = { anim.trunk.value },
            foliageP = { anim.foliage.value },
            sway = { sway },
            pulse = { pulse },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = bob.dp.toPx() * anim.foliage.value.coerceIn(0f, 1f)
                }
        )

        // Confetti burst
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = anim.confetti.value
            if (t > 0f && t < 1f) {
                val s = min(size.width, size.height)
                val origin = Offset(size.width / 2f, (size.height - s) / 2f + s * 0.32f)
                val alpha = (1f - ((t - 0.45f) / 0.55f)).coerceIn(0f, 1f)
                particles.forEach { p ->
                    val dist = p.speed * s * t
                    val px = origin.x + cos(p.angle) * dist
                    val py = origin.y + sin(p.angle) * dist + 0.9f * s * t * t
                    val sz = s * p.sizeFrac
                    if (p.rect) {
                        rotate(p.spin * t * 360f, Offset(px, py)) {
                            drawRect(
                                color = p.color,
                                topLeft = Offset(px - sz / 2f, py - sz * 0.3f),
                                size = Size(sz, sz * 0.6f),
                                alpha = alpha
                            )
                        }
                    } else {
                        drawCircle(p.color, sz / 2f, Offset(px, py), alpha = alpha)
                    }
                }
            }
        }

        // Continuously falling leaves (start after the tree has grown)
        leafSpecs.forEach { leaf ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = maxWidth * leaf.xFrac)
                    .size(leaf.size)
                    .graphicsLayer {
                        val p = (leafTime * leaf.speed + leaf.phase) % 1f
                        val edgeFade = min(1f, min(p / 0.12f, (1f - p) / 0.12f))
                        translationY = (p * 1.1f - 0.05f) * h.toPx()
                        translationX = sin(p * 2f * PI.toFloat() * 2f + leaf.phase * 6f) *
                                leaf.sway.dp.toPx()
                        rotationZ = p * 360f * leaf.dir
                        alpha = anim.leaves.value * edgeFade
                    }
                    .background(
                        leaf.color,
                        RoundedCornerShape(
                            topStartPercent = 100,
                            topEndPercent = 10,
                            bottomEndPercent = 100,
                            bottomStartPercent = 10
                        )
                    )
            )
        }
    }
}

private data class Blob(val cx: Float, val cy: Float, val r: Float, val color: Color)

private val blobs = listOf(
    // back / dark layer
    Blob(0.27f, 0.42f, 0.15f, Color(0xFF2E8B3E)),
    Blob(0.73f, 0.42f, 0.15f, Color(0xFF2E8B3E)),
    Blob(0.50f, 0.26f, 0.19f, Color(0xFF2E8B3E)),
    Blob(0.38f, 0.30f, 0.15f, Color(0xFF38A24A)),
    Blob(0.62f, 0.30f, 0.15f, Color(0xFF38A24A)),
    // mid layer
    Blob(0.32f, 0.46f, 0.13f, Color(0xFF45B858)),
    Blob(0.68f, 0.46f, 0.13f, Color(0xFF45B858)),
    Blob(0.50f, 0.42f, 0.16f, Color(0xFF4CC262)),
    Blob(0.50f, 0.24f, 0.15f, Color(0xFF52C968)),
    // highlights
    Blob(0.40f, 0.22f, 0.09f, Color(0xFF7ADB7E)),
    Blob(0.60f, 0.34f, 0.08f, Color(0xFF7ADB7E)),
    Blob(0.30f, 0.38f, 0.07f, Color(0xFF8BE58A)),
    Blob(0.52f, 0.34f, 0.07f, Color(0xFF8BE58A)),
    Blob(0.70f, 0.40f, 0.06f, Color(0xFF8BE58A))
)

@Composable
private fun TreeCanvas(
    islandP: () -> Float,
    trunkP: () -> Float,
    foliageP: () -> Float,
    sway: () -> Float,
    pulse: () -> Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val ip = islandP().coerceIn(0f, 1f)
        val tp = trunkP().coerceIn(0f, 1f)
        val fp = foliageP().coerceAtLeast(0f)

        val cw = size.width
        val ch = size.height
        val s = min(cw, ch)
        val ox = (cw - s) / 2f
        val oy = (ch - s) / 2f

        fun x(f: Float) = ox + s * f
        fun y(f: Float) = oy + s * f

        // Soft glow behind the tree (grows with the foliage, then pulses)
        val glowR = s * 0.55f * (0.5f + 0.5f * fp.coerceAtMost(1f)) * pulse()
        val glowCenter = Offset(x(0.5f), y(0.45f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x5538C97A), Color.Transparent),
                center = glowCenter,
                radius = glowR
            ),
            radius = glowR,
            center = glowCenter,
            alpha = fp.coerceIn(0f, 1f)
        )

        // Island rises up while fading in
        translate(top = (1f - ip) * s * 0.18f) {
            val island = Path().apply {
                moveTo(x(0.10f), y(0.74f))
                lineTo(x(0.90f), y(0.74f))
                cubicTo(x(0.88f), y(0.86f), x(0.68f), y(0.95f), x(0.50f), y(1.00f))
                cubicTo(x(0.32f), y(0.95f), x(0.12f), y(0.86f), x(0.10f), y(0.74f))
                close()
            }
            drawPath(
                path = island,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6B4226), Color(0xFF3E2414)),
                    startY = y(0.74f),
                    endY = y(1.0f)
                ),
                alpha = ip
            )
            drawOval(
                color = Color(0xFF5A3720),
                topLeft = Offset(x(0.10f), y(0.69f)),
                size = Size(s * 0.80f, s * 0.11f),
                alpha = ip
            )
            drawOval(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF2E7D32), Color(0xFF66BB6A), Color(0xFF2E7D32))
                ),
                topLeft = Offset(x(0.14f), y(0.685f)),
                size = Size(s * 0.72f, s * 0.07f),
                alpha = ip
            )
            listOf(0.30f to 0.76f, 0.45f to 0.79f, 0.62f to 0.77f, 0.72f to 0.75f)
                .forEach { (fx, fy) ->
                    drawCircle(
                        Color(0xFFE0A93B),
                        radius = s * 0.008f,
                        center = Offset(x(fx), y(fy)),
                        alpha = ip
                    )
                }
        }

        // Trunk + branches grow upward from the soil
        if (tp > 0f) {
            scale(1f, tp, pivot = Offset(x(0.5f), y(0.74f))) {
                val trunk = Path().apply {
                    moveTo(x(0.40f), y(0.74f))
                    cubicTo(x(0.45f), y(0.72f), x(0.46f), y(0.66f), x(0.46f), y(0.55f))
                    lineTo(x(0.54f), y(0.55f))
                    cubicTo(x(0.54f), y(0.66f), x(0.55f), y(0.72f), x(0.60f), y(0.74f))
                    close()
                }
                drawPath(
                    path = trunk,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF8A5A2B), Color(0xFFB8793A), Color(0xFF8A5A2B)),
                        startX = x(0.40f),
                        endX = x(0.60f)
                    )
                )
                val branchColor = Color(0xFF9A6630)
                val stroke = s * 0.025f
                drawLine(branchColor, Offset(x(0.48f), y(0.60f)), Offset(x(0.34f), y(0.46f)), strokeWidth = stroke)
                drawLine(branchColor, Offset(x(0.52f), y(0.60f)), Offset(x(0.66f), y(0.46f)), strokeWidth = stroke)
                drawLine(branchColor, Offset(x(0.50f), y(0.58f)), Offset(x(0.50f), y(0.40f)), strokeWidth = stroke)
            }
        }

        // Foliage springs out from the top of the trunk, then sways gently
        if (fp > 0.001f) {
            val pivot = Offset(x(0.5f), y(0.58f))
            scale(fp, fp, pivot) {
                rotate(sway(), pivot) {
                    blobs.forEach { b ->
                        drawCircle(
                            color = b.color,
                            radius = s * b.r,
                            center = Offset(x(b.cx), y(b.cy))
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun FocusCompleteScreenPreview() {
    FocusCompleteScreen()
}