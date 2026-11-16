package com.veroanggra.reactify.sample

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware neutrals for the component demo screens. The List/Cart mockups keep their fixed
 * cream-card look regardless of this value — only the plain component demos adapt.
 */
public data class DemoColors(
    val isDark: Boolean,
    val background: Color,
    val onBackground: Color,
    val onBackgroundMuted: Color,
    /** Card / panel fill, low-opacity tint of [onBackground] over [background]. */
    val surface: Color,
    /** Secondary buttons, step markers, tracks — a stronger tint than [surface]. */
    val neutralChip: Color,
    /** Opaque neutral for bars and icon placeholders. */
    val neutralSolid: Color,
)

public val DarkDemoColors: DemoColors = DemoColors(
    isDark = true,
    background = Color.Black,
    onBackground = Color.White,
    onBackgroundMuted = Color.White.copy(alpha = 0.6f),
    surface = Color.White.copy(alpha = 0.06f),
    neutralChip = Color.White.copy(alpha = 0.12f),
    neutralSolid = Color(0xFF2A2A2E),
)

public val LightDemoColors: DemoColors = DemoColors(
    isDark = false,
    background = Color.White,
    onBackground = Color.Black,
    onBackgroundMuted = Color.Black.copy(alpha = 0.6f),
    surface = Color.Black.copy(alpha = 0.045f),
    neutralChip = Color.Black.copy(alpha = 0.08f),
    neutralSolid = Color(0xFFE4E4E8),
)

public val LocalDemoColors: androidx.compose.runtime.ProvidableCompositionLocal<DemoColors> =
    compositionLocalOf { DarkDemoColors }
