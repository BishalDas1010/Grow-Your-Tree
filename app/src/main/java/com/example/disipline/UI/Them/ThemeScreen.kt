package com.example.disipline.UI.Them

import com.example.disipline.ui.theme.AppColors
import com.example.disipline.ui.theme.ThemeManager
import com.example.disipline.ui.theme.ThemePalette
import com.example.disipline.ui.theme.ThemePalettes


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.R

// =====================================================================
// THEME SCREEN
// =====================================================================

@Composable
fun ThemeScreen(
    onBack: () -> Unit = {},
    onApply: (ThemePalette) -> Unit = { ThemeManager.apply(it) }
) {
    // Re-reads every time the active palette changes → previews update live
    val activeId = ThemeManager.active.id

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        ThemeTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = "Make it yours",
                color = AppColors.PrimaryText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Pick a palette — the app updates instantly.",
                color = AppColors.SecondaryText,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            // Grid of palettes, 1 card per row for maximum preview room.
            // Change to chunked(2) if you want a denser grid.
            ThemePalettes.all.forEach { palette ->
                ThemePreviewCard(
                    palette = palette,
                    isActive = palette.id == activeId,
                    onClick = { onApply(palette) }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// =====================================================================
// TOP BAR
// =====================================================================

@Composable
private fun ThemeTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Back",
                tint = AppColors.PrimaryText
            )
        }
        Text(
            text = "Themes",
            color = AppColors.PrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        // Spacer to keep the title visually centered
        Spacer(Modifier.size(40.dp))
    }
}

// =====================================================================
// CARD
// =====================================================================

@Composable
private fun ThemePreviewCard(
    palette: ThemePalette,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    // Border + subtle scale bump when active
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.98f,
        animationSpec = tween(220),
        label = "themeScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(palette.surface)
            .border(
                BorderStroke(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) palette.primaryAccent else palette.outline
                ),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        // ---- Header: name + subtitle + active pill --------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = palette.name,
                        color = palette.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.size(8.dp))
                    if (isActive) ActivePill(palette)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = palette.description,
                    color = palette.secondaryText,
                    fontSize = 12.sp
                )
            }

            // Big circular check when active
            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(palette.primaryAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = palette.buttonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---- Full mock preview ---------------------------------------
        ThemeMockPreview(palette)

        Spacer(Modifier.height(12.dp))

        // ---- Swatch strip --------------------------------------------
        SwatchStrip(palette)
    }
}

@Composable
private fun ActivePill(palette: ThemePalette) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.primaryAccent.copy(alpha = 0.20f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Active",
            color = palette.primaryAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =====================================================================
// MOCK PREVIEW — a mini version of a real screen
// =====================================================================

@Composable
private fun ThemeMockPreview(palette: ThemePalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.background)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // Fake small top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(palette.primaryAccent)
                )
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.primaryText.copy(alpha = 0.75f))
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(palette.iconBackground)
                )
            }

            // Fake card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.outline, RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.primaryText.copy(alpha = 0.85f))
                    )
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.secondaryText.copy(alpha = 0.7f))
                    )
                }
            }

            // Fake CTA button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(palette.primaryButton),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.buttonText.copy(alpha = 0.9f))
                )
            }
        }
    }
}

// =====================================================================
// SWATCHES
// =====================================================================

@Composable
private fun SwatchStrip(palette: ThemePalette) {
    val swatches = listOf(
        palette.background,
        palette.surface,
        palette.elevatedSurface,
        palette.primaryAccent,
        palette.primaryButton,
        palette.primaryText,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        swatches.forEach { color ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .border(1.dp, palette.outline.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            )
        }
    }
}

// =====================================================================
// PREVIEW
// =====================================================================

@Preview(showBackground = true, backgroundColor = 0xFF101814, widthDp = 390, heightDp = 900)
@Composable
private fun ThemeScreenPreview() {
    ThemeScreen()
}