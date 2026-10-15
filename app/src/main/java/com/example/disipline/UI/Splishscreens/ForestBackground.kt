package com.example.disipline.UI.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

/**
 * Stylised forest-at-dusk background. Draws:
 *   - a vertical gradient (deep green -> near-black)
 *   - a soft radial "sun"
 *   - a row of tree silhouettes as simple triangles
 *
 * @param sunY relative 0..1 vertical position of the sun (0 = top, 1 = bottom).
 *             Use 0.30f for the splash, 0.20f for onboarding pages.
 * @param showTrees when false, only the gradient + sun are drawn.
 */
@Composable
fun ForestBackground(
    modifier: Modifier = Modifier,
    sunY: Float = 0.30f,
    showTrees: Boolean = true
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Gradient sky
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1F5F47),
                    Color(0xFF0F3626),
                    Color(0xFF061410)
                )
            )
        )

        // 2. Sun glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF1B0),
                    Color(0xFFE5C96A).copy(alpha = 0.6f),
                    Color.Transparent
                ),
                radius = size.minDimension * 0.35f,
                center = Offset(size.width * 0.5f, size.height * sunY)
            )
        )

        // 3. Tree silhouettes
        if (showTrees) {
            val treeColor = Color(0xFF07190F)
            val count = 14
            val slot = size.width / count
            val baseY = size.height * 0.62f

            for (i in 0 until count) {
                val cx = i * slot + slot / 2f
                // vary tree heights a little so it feels natural
                val h = baseY * (0.55f + ((i * 37) % 25) / 100f)
                val top = baseY - h
                val half = slot * 0.75f

                val path = Path().apply {
                    moveTo(cx, top)
                    lineTo(cx - half, baseY)
                    lineTo(cx + half, baseY)
                    close()
                }
                drawPath(path, color = treeColor)
            }
        }
    }
}