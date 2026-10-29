package com.veroanggra.reactify.gooey

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.unit.dp

/** Signed-distance-field renderer built on [android.graphics.RuntimeShader]. */
@RequiresApi(33)
public fun GooeyRenderers.agsl(): GooeyRenderer = AgslGooeyRenderer()

/**
 * Below API 33 there is no AGSL, so the fallback traces the field on the CPU every frame
 * ([PathGooeyRenderer]). A coarser grid keeps that affordable on mid-range hardware; on a
 * genuinely small heap even that is too much, so [isLowEndDevice] skips merging entirely.
 */
private val FALLBACK_CELL_SIZE = 4.dp

internal actual fun defaultGooeyRenderer(): GooeyRenderer = when {
    Build.VERSION.SDK_INT >= 33 -> GooeyRenderers.agsl()
    isLowEndDevice() -> GooeyRenderers.plain()
    else -> GooeyRenderers.path(cellSize = FALLBACK_CELL_SIZE)
}

/**
 * Heuristic for "too weak to afford CPU gooey merging": the per-app heap ceiling ART assigns is
 * sized by the device's RAM tier, so a small one is a reasonable proxy for low-end hardware
 * without requiring a [android.content.Context] just to ask [android.app.ActivityManager].
 */
private fun isLowEndDevice(): Boolean = Runtime.getRuntime().maxMemory() < LOW_END_HEAP_BYTES

private const val LOW_END_HEAP_BYTES = 192L * 1024 * 1024
