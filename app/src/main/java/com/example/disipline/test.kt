package com.example.disipline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// The artwork is drawn in a fixed 920 x 840 design space and scaled to fit.
private const val DESIGN_W = 920f
private const val DESIGN_H = 840f

// ---- Colors ----
private val CloudColor = Color(0xFF1A6045)
private val MidHillTop = Color(0xFF23593F)
private val MidHillBottom = Color(0xFF123D2B)
private val RightDomeTop = Color(0xFF39AE58)
private val RightDomeBottom = Color(0xFF0E3B27)
private val LeftDomeLight = Color(0xFF4DCB6B)
private val LeftDomeDark = Color(0xFF1F7C41)
private val Foreground = Color(0xFF0A2E1F)
private val StemLight = Color(0xFFB9F27C)
private val StemDark = Color(0xFF86D955)
private val LeafUpperLeft = Color(0xFF6CC953)
private val LeafUpperRight = Color(0xFF78D358)
private val LeafLight = Color(0xFFA6E86B)
private val LeafOutline = Color(0xFF062A1B).copy(alpha = 0.6f)
private val VeinColor = Color(0xFF2E8B3A)

// ---- Sprout paths (design coordinates) ----
private val stemPath = Path().apply {
    moveTo(532f, 600f)
    cubicTo(540f, 545f, 556f, 470f, 558f, 390f)
    lineTo(584f, 390f)
    cubicTo(592f, 450f, 612f, 520f, 603f, 598f)
    cubicTo(602f, 616f, 536f, 618f, 532f, 600f)
    close()
}

// Left leaf (smaller): upper half and lower half, split by the vein
private val leftLeafUpper = Path().apply {
    moveTo(320f, 290f)
    cubicTo(365f, 235f, 500f, 225f, 566f, 386f)
    quadraticBezierTo(470f, 315f, 320f, 290f)
    close()
}
private val leftLeafLower = Path().apply {
    moveTo(320f, 290f)
    cubicTo(345f, 400f, 470f, 455f, 566f, 388f)
    quadraticBezierTo(470f, 315f, 320f, 290f)
    close()
}

// Right leaf (larger): left half and right half, split by the vein
private val rightLeafLeft = Path().apply {
    moveTo(572f, 384f)
    cubicTo(560f, 300f, 610f, 200f, 762f, 183f)
    quadraticBezierTo(650f, 275f, 572f, 384f)
    close()
}
private val rightLeafRight = Path().apply {
    moveTo(572f, 384f)
    cubicTo(660f, 400f, 765f, 330f, 762f, 183f)
    quadraticBezierTo(650f, 275f, 572f, 384f)
    close()
}

private val leftVein = Path().apply {
    moveTo(566f, 386f)
    quadraticBezierTo(470f, 318f, 345f, 293f)
}
private val rightVein = Path().apply {
    moveTo(572f, 384f)
    quadraticBezierTo(650f, 285f, 740f, 205f)
}

/**
 * Sprout with clouds and hills. Background is transparent, so put it on
 * your dark green card. Size it with a height (or width); the aspect ratio
 * is fixed automatically.
 */
@Composable
fun TreeDesign(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .aspectRatio(DESIGN_W / DESIGN_H)
            .clipToBounds()
    ) {
        val s = size.width / DESIGN_W

        scale(scaleX = s, scaleY = s, pivot = Offset.Zero) {

            // 1. Faint clouds
            drawCircle(CloudColor.copy(alpha = 0.45f), 65f, Offset(265f, 135f))
            drawCircle(CloudColor.copy(alpha = 0.45f), 40f, Offset(335f, 155f))
            drawCircle(CloudColor.copy(alpha = 0.45f), 38f, Offset(165f, 158f))
            drawRoundRect(
                CloudColor.copy(alpha = 0.45f),
                topLeft = Offset(125f, 150f),
                size = Size(255f, 40f),
                cornerRadius = CornerRadius(20f, 20f)
            )
            drawCircle(CloudColor.copy(alpha = 0.2f), 25f, Offset(790f, 62f))
            drawRoundRect(
                CloudColor.copy(alpha = 0.2f),
                topLeft = Offset(700f, 70f),
                size = Size(130f, 25f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // 2. Hills
            // Muted dome on the left
            drawCircle(
                brush = Brush.verticalGradient(
                    listOf(MidHillTop, MidHillBottom), startY = 440f, endY = 960f
                ),
                radius = 260f,
                center = Offset(230f, 700f)
            )
            // Bright dome on the right
            drawCircle(
                brush = Brush.verticalGradient(
                    listOf(RightDomeTop, RightDomeBottom), startY = 410f, endY = 760f
                ),
                radius = 330f,
                center = Offset(900f, 740f)
            )
            // Bright dome bottom-left
            drawCircle(
                brush = Brush.linearGradient(
                    listOf(LeftDomeLight, LeftDomeDark),
                    start = Offset(150f, 640f),
                    end = Offset(330f, 800f)
                ),
                radius = 180f,
                center = Offset(250f, 800f)
            )

            // 3. Dark foreground hills
            drawCircle(Foreground, 200f, Offset(60f, 880f))
            drawCircle(Foreground, 330f, Offset(480f, 930f))
            drawCircle(Foreground.copy(alpha = 0.92f), 290f, Offset(850f, 900f))
            drawRect(Foreground, Offset(0f, 800f), Size(DESIGN_W, 40f))

            // 4. Stem
            drawPath(
                stemPath,
                Brush.horizontalGradient(listOf(StemLight, StemDark), startX = 530f, endX = 610f)
            )

            // 5. Leaf outlines (dark edge that separates leaves from the background)
            val outline = Stroke(width = 5f)
            drawPath(leftLeafUpper, LeafOutline, style = outline)
            drawPath(leftLeafLower, LeafOutline, style = outline)
            drawPath(rightLeafLeft, LeafOutline, style = outline)
            drawPath(rightLeafRight, LeafOutline, style = outline)

            // 6. Leaf fills: darker half and lighter half on each leaf
            drawPath(leftLeafUpper, LeafUpperLeft)
            drawPath(leftLeafLower, LeafLight)
            drawPath(rightLeafLeft, LeafUpperRight)
            drawPath(rightLeafRight, LeafLight)

            // 7. Veins
            val veinStroke = Stroke(width = 7f, cap = StrokeCap.Round)
            drawPath(leftVein, VeinColor, style = veinStroke)
            drawPath(rightVein, VeinColor, style = veinStroke)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B3A29)
@Composable
fun TreeDesignPreview() {
    Box(

    ) {
        TreeDesign(modifier = Modifier.height(170.dp))
    }
}