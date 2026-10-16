//package com.example.disipline.ui.theme
//
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.runtime.Composable
//
//private val DisciplineColorScheme = darkColorScheme(
//    primary = AppColors.PrimaryAccent,
//    onPrimary = AppColors.ButtonText,
//    primaryContainer = AppColors.PrimaryButton,
//    onPrimaryContainer = AppColors.ButtonText,
//    secondary = AppColors.PrimaryAccent,
//    onSecondary = AppColors.ButtonText,
//    secondaryContainer = AppColors.ElevatedSurface,
//    onSecondaryContainer = AppColors.PrimaryText,
//    tertiary = AppColors.IconBackground,
//    onTertiary = AppColors.PrimaryText,
//    background = AppColors.Background,
//    onBackground = AppColors.PrimaryText,
//    surface = AppColors.Surface,
//    onSurface = AppColors.PrimaryText,
//    surfaceVariant = AppColors.ElevatedSurface,
//    onSurfaceVariant = AppColors.SecondaryText,
//    outline = AppColors.Outline,
//    outlineVariant = AppColors.IconBackground,
//    error = AppColors.Error,
//    onError = AppColors.Background
//)
//
///**
// * DisciplineX centralized Material 3 theme.
// */
//@Composable
//fun DisciplineXTheme(
//    content: @Composable () -> Unit
//) {
//    MaterialTheme(
//        colorScheme = DisciplineColorScheme,
//        typography = Typography,
//        content = content
//    )
//}
//
///**
// * Alias for backward compatibility across existing views.
// */
//@Composable
//fun DisiplineTheme(
//    darkTheme: Boolean = true,
//    dynamicColor: Boolean = false,
//    content: @Composable () -> Unit
//) {
//    DisciplineXTheme(content = content)
//}
package com.example.disipline.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Builds a Material 3 color scheme from the *current* AppColors.
 *
 * MUST be called inside a @Composable (not as a top-level val) so it
 * re-evaluates whenever ThemeManager.active changes.
 */
private fun buildColorScheme() = darkColorScheme(
    primary              = AppColors.PrimaryAccent,
    onPrimary            = AppColors.ButtonText,
    primaryContainer     = AppColors.PrimaryButton,
    onPrimaryContainer   = AppColors.ButtonText,
    secondary            = AppColors.PrimaryAccent,
    onSecondary          = AppColors.ButtonText,
    secondaryContainer   = AppColors.ElevatedSurface,
    onSecondaryContainer = AppColors.PrimaryText,
    tertiary             = AppColors.IconBackground,
    onTertiary           = AppColors.PrimaryText,
    background           = AppColors.Background,
    onBackground         = AppColors.PrimaryText,
    surface              = AppColors.Surface,
    onSurface            = AppColors.PrimaryText,
    surfaceVariant       = AppColors.ElevatedSurface,
    onSurfaceVariant     = AppColors.SecondaryText,
    outline              = AppColors.Outline,
    outlineVariant       = AppColors.IconBackground,
    error                = AppColors.Error,
    onError              = AppColors.Background
)

/**
 * DisciplineX centralized Material 3 theme.
 *
 * Re-reads AppColors on every recomposition → palette changes propagate
 * through the whole app instantly.
 */
@Composable
fun DisciplineXTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = buildColorScheme(),
        typography = Typography,
        content = content
    )
}

/**
 * Alias for backward compatibility across existing views.
 */
@Composable
fun DisiplineTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    DisciplineXTheme(content = content)
}