package com.example.disipline.UI.Them


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.ui.theme.AppColors
import com.example.disipline.ui.theme.ThemePalette
import com.example.disipline.ui.theme.ThemePalettes

/**
 * Dropdown surface listing every theme in [ThemePalettes.all].
 * The caller decides when to show it (e.g. via `expanded` state) and
 * what to do when a theme is picked.
 *
 * @param activeId  id of the currently-selected palette — that row gets a check
 * @param onPick    fired when the user taps a row
 */
@Composable
fun ThemeDropdownSurface(
    activeId: String,
    onPick: (ThemePalette) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AppColors.ElevatedSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Outline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.fillMaxWidth()) {
            ThemePalettes.all.forEachIndexed { index, palette ->
                ThemeRow(
                    palette = palette,
                    isActive = palette.id == activeId,
                    onClick = { onPick(palette) }
                )
                if (index < ThemePalettes.all.lastIndex) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(start = 44.dp)
                            .background(AppColors.Outline.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeRow(
    palette: ThemePalette,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2-color swatch — background + accent
        Row(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(1.dp, palette.outline, CircleShape)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(palette.background)
            )
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(palette.primaryAccent)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                palette.name,
                color = AppColors.PrimaryText,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
            )
            Text(
                palette.description,
                color = AppColors.SecondaryText,
                fontSize = 11.sp
            )
        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AppColors.PrimaryAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✓",
                    color = AppColors.ButtonText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Rotating chevron for the "Themes" menu row.
 * 0° when closed, 90° when expanded.
 */
@Composable
fun RotatingChevron(expanded: Boolean) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(200),
        label = "chevron"
    )
    Text(
        "›",
        color = AppColors.SecondaryText,
        fontSize = 22.sp,
        fontWeight = FontWeight.Light,
        modifier = Modifier.rotate(rotation)
    )
}