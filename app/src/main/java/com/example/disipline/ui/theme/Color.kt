package com.example.disipline.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class ThemePalette(
    val id: String,
    val name: String,
    val description: String,        // shown under the name in the picker
    val isDark: Boolean,

    // Backgrounds & surfaces
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,

    // Accents & buttons
    val primaryAccent: Color,
    val primaryButton: Color,
    val buttonText: Color,

    // Text
    val primaryText: Color,
    val secondaryText: Color,

    // Lines & icon holders
    val outline: Color,
    val iconBackground: Color,

    // Semantic
    val success: Color,
    val warning: Color,
    val error: Color,
)



object ThemePalettes {

    /** The original DisciplineX palette. */
    val SageForest = ThemePalette(
        id = "sage_forest",
        name = "Sage Forest",
        description = "Calm · Minimal · Premium",
        isDark = true,
        background       = Color(0xFF101814),
        surface          = Color(0xFF1A2A22),
        elevatedSurface  = Color(0xFF263B31),
        primaryAccent    = Color(0xFFA3BFA8),
        primaryButton    = Color(0xFFD5E8D7),
        buttonText       = Color(0xFF101814),
        primaryText      = Color(0xFFF1F5F0),
        secondaryText    = Color(0xFFA1ADA5),
        outline          = Color(0xFF34483D),
        iconBackground   = Color(0xFF22372B),
        success          = Color(0xFF8BD66B),
        warning          = Color(0xFFE4C66A),
        error            = Color(0xFFE58B85),
    )

    /** Deep, focused, cool blue. */
    val MidnightInk = ThemePalette(
        id = "midnight_ink",
        name = "Midnight Ink",
        description = "Deep · Focused · Cool",
        isDark = true,
        background       = Color(0xFF0A0E17),
        surface          = Color(0xFF141A28),
        elevatedSurface  = Color(0xFF1F2937),
        primaryAccent    = Color(0xFF7BA7F5),
        primaryButton    = Color(0xFFB8CCF5),
        buttonText       = Color(0xFF0A0E17),
        primaryText      = Color(0xFFE8EEF8),
        secondaryText    = Color(0xFF8A94AB),
        outline          = Color(0xFF26314A),
        iconBackground   = Color(0xFF1A2438),
        success          = Color(0xFF6EE7B7),
        warning          = Color(0xFFFBBF6E),
        error            = Color(0xFFF87171),
    )

    /** Cozy, energetic, amber. */
    val WarmEmber = ThemePalette(
        id = "warm_ember",
        name = "Warm Ember",
        description = "Cozy · Bold · Energetic",
        isDark = true,
        background       = Color(0xFF16100A),
        surface          = Color(0xFF221810),
        elevatedSurface  = Color(0xFF2E2014),
        primaryAccent    = Color(0xFFF0A15A),
        primaryButton    = Color(0xFFF5D4A8),
        buttonText       = Color(0xFF16100A),
        primaryText      = Color(0xFFF8EEE2),
        secondaryText    = Color(0xFFA89078),
        outline          = Color(0xFF3D2A18),
        iconBackground   = Color(0xFF2A1D12),
        success          = Color(0xFFA3D66B),
        warning          = Color(0xFFF0C95A),
        error            = Color(0xFFE88B7A),
    )

    /** Soft, gentle, rose. */
    val RoseQuartz = ThemePalette(
        id = "rose_quartz",
        name = "Rose Quartz",
        description = "Soft · Gentle · Warm",
        isDark = true,
        background       = Color(0xFF17121A),
        surface          = Color(0xFF221B28),
        elevatedSurface  = Color(0xFF2E2535),
        primaryAccent    = Color(0xFFD996B0),
        primaryButton    = Color(0xFFEBC0D0),
        buttonText       = Color(0xFF17121A),
        primaryText      = Color(0xFFF5EAF0),
        secondaryText    = Color(0xFFA896A2),
        outline          = Color(0xFF3D2F44),
        iconBackground   = Color(0xFF2A1F30),
        success          = Color(0xFF8BD68F),
        warning          = Color(0xFFE4C66A),
        error            = Color(0xFFE58B85),
    )

    /** Aquatic, cool, teal. */
    val OceanDepth = ThemePalette(
        id = "ocean_depth",
        name = "Ocean Depth",
        description = "Calm · Aquatic · Fresh",
        isDark = true,
        background       = Color(0xFF0A1316),
        surface          = Color(0xFF12212A),
        elevatedSurface  = Color(0xFF1C3039),
        primaryAccent    = Color(0xFF6FC7C7),
        primaryButton    = Color(0xFFA8DCDC),
        buttonText       = Color(0xFF0A1316),
        primaryText      = Color(0xFFE4F0EF),
        secondaryText    = Color(0xFF7F9A99),
        outline          = Color(0xFF234048),
        iconBackground   = Color(0xFF173038),
        success          = Color(0xFF7DDD9A),
        warning          = Color(0xFFE4C66A),
        error            = Color(0xFFE58B85),
    )

    /** Neutral, modern, monochrome. */
    val SlateMono = ThemePalette(
        id = "slate_mono",
        name = "Slate Mono",
        description = "Neutral · Modern · Quiet",
        isDark = true,
        background       = Color(0xFF121214),
        surface          = Color(0xFF1C1C20),
        elevatedSurface  = Color(0xFF27272C),
        primaryAccent    = Color(0xFFC8C8D0),
        primaryButton    = Color(0xFFE0E0E8),
        buttonText       = Color(0xFF121214),
        primaryText      = Color(0xFFF0F0F2),
        secondaryText    = Color(0xFF909098),
        outline          = Color(0xFF38383F),
        iconBackground   = Color(0xFF22222A),
        success          = Color(0xFF7DDD9A),
        warning          = Color(0xFFE4C66A),
        error            = Color(0xFFE58B85),
    )

    /** Dreamy, soft, lavender. */
    val LavenderMist = ThemePalette(
        id = "lavender_mist",
        name = "Lavender Mist",
        description = "Dreamy · Calm · Soft",
        isDark = true,
        background       = Color(0xFF100E1A),
        surface          = Color(0xFF1B1728),
        elevatedSurface  = Color(0xFF261F35),
        primaryAccent    = Color(0xFFB5A5E0),
        primaryButton    = Color(0xFFD5C9F0),
        buttonText       = Color(0xFF100E1A),
        primaryText      = Color(0xFFEDE8F5),
        secondaryText    = Color(0xFF968FAB),
        outline          = Color(0xFF322A44),
        iconBackground   = Color(0xFF221C30),
        success          = Color(0xFF7DDD9A),
        warning          = Color(0xFFE4C66A),
        error            = Color(0xFFE58B85),
    )

    /** Dramatic, deep, crimson. */
    val CherryNoir = ThemePalette(
        id = "cherry_noir",
        name = "Cherry Noir",
        description = "Dramatic · Bold · Deep",
        isDark = true,
        background       = Color(0xFF140A0C),
        surface          = Color(0xFF200E13),
        elevatedSurface  = Color(0xFF2D131A),
        primaryAccent    = Color(0xFFE8838E),
        primaryButton    = Color(0xFFF5B5BE),
        buttonText       = Color(0xFF140A0C),
        primaryText      = Color(0xFFF5E4E6),
        secondaryText    = Color(0xFFA88A90),
        outline          = Color(0xFF3D1820),
        iconBackground   = Color(0xFF2A1218),
        success          = Color(0xFF7DDD9A),
        warning          = Color(0xFFE4C66A),
        error            = Color(0xFFE58B85),
    )

    /** Ordered list — used by the picker sheet. */
    val all: List<ThemePalette> = listOf(
        SageForest,
        MidnightInk,
        WarmEmber,
        RoseQuartz,
        OceanDepth,
        SlateMono,
        LavenderMist,
        CherryNoir,
    )

    fun byId(id: String): ThemePalette =
        all.firstOrNull { it.id == id } ?: SageForest
}


object ThemeManager {

    private var appContext: Context? = null

    var active: ThemePalette by mutableStateOf(ThemePalettes.SageForest)
        private set

    fun apply(palette: ThemePalette) {
        active = palette
        appContext?.let { ThemePrefs.save(it, palette.id) }
    }

    fun applyById(id: String) {
        apply(ThemePalettes.byId(id))
    }

    /** Load the saved palette at app startup. */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        val savedId = ThemePrefs.load(context) ?: return
        active = ThemePalettes.byId(savedId)
    }

    /** Apply AND persist — use this from any UI picker. */
    fun applyAndSave(context: Context, palette: ThemePalette) {
        appContext = context.applicationContext
        active = palette
        ThemePrefs.save(context, palette.id)
    }
}

object AppColors {
    val Background       get() = ThemeManager.active.background
    val Surface          get() = ThemeManager.active.surface
    val ElevatedSurface  get() = ThemeManager.active.elevatedSurface

    val PrimaryAccent    get() = ThemeManager.active.primaryAccent
    val PrimaryButton    get() = ThemeManager.active.primaryButton
    val ButtonText       get() = ThemeManager.active.buttonText

    val PrimaryText      get() = ThemeManager.active.primaryText
    val SecondaryText    get() = ThemeManager.active.secondaryText

    val Outline          get() = ThemeManager.active.outline
    val IconBackground   get() = ThemeManager.active.iconBackground

    val Success          get() = ThemeManager.active.success
    val Warning          get() = ThemeManager.active.warning
    val Error            get() = ThemeManager.active.error
}

// =====================================================================
// 5. Backward-compat aliases (unchanged names)
// =====================================================================

val BgDark          get() = AppColors.Background
val Surface         get() = AppColors.Surface
val ElevatedSurface get() = AppColors.ElevatedSurface
val IconCircle      get() = AppColors.IconBackground
val Accent          get() = AppColors.PrimaryAccent
val PrimaryButton   get() = AppColors.PrimaryButton
val OnAccent        get() = AppColors.ButtonText
val TextPrimary     get() = AppColors.PrimaryText
val TextSecondary   get() = AppColors.SecondaryText
val Outline         get() = AppColors.Outline
val Success         get() = AppColors.Success
val Warning         get() = AppColors.Warning
val Error           get() = AppColors.Error