package com.example.disipline.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized Design System Colors for DisciplineX (Sage & Forest Palette).
 * Editing this object updates the theme and colors across the entire application.
 */
object AppColors {
    val Background = Color(0xFF101814)
    val Surface = Color(0xFF1A2A22)
    val ElevatedSurface = Color(0xFF263B31)
    val PrimaryAccent = Color(0xFFA3BFA8)
    val PrimaryButton = Color(0xFFD5E8D7)
    val ButtonText = Color(0xFF101814)
    val PrimaryText = Color(0xFFF1F5F0)
    val SecondaryText = Color(0xFFA1ADA5)
    val Outline = Color(0xFF34483D)
    val IconBackground = Color(0xFF22372B)
    val Success = Color(0xFF8BD66B)
    val Warning = Color(0xFFE4C66A)
    val Error = Color(0xFFE58B85)
}

// Global semantic tokens mapped to AppColors for backward compatibility & easy access
val BgDark get() = AppColors.Background
val Surface get() = AppColors.Surface
val ElevatedSurface get() = AppColors.ElevatedSurface
val IconCircle get() = AppColors.IconBackground
val Accent get() = AppColors.PrimaryAccent
val PrimaryButton get() = AppColors.PrimaryButton
val OnAccent get() = AppColors.ButtonText
val TextPrimary get() = AppColors.PrimaryText
val TextSecondary get() = AppColors.SecondaryText
val Outline get() = AppColors.Outline
val Success get() = AppColors.Success
val Warning get() = AppColors.Warning
val Error get() = AppColors.Error
