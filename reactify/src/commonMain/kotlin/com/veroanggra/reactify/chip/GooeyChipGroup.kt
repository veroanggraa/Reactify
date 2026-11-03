package com.veroanggra.reactify.chip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene
import com.veroanggra.reactify.list.rootBounds

public object GooeyChipGroupDefaults {
    public val CornerRadius: Dp = 16.dp

    /** Merge distance between selected chips; small enough that only ones touching (or nearly) bridge. */
    public val Smoothness: Dp = 10.dp
}

@Composable
public fun rememberGooeyChipGroupState(): GooeyChipGroupState = remember { GooeyChipGroupState() }

/**
 * Tracks which chips inside a [GooeyChipGroupHost] are selected. Each selected chip draws its own
 * rounded-rect blob at its registered bounds; two selected chips sitting close enough together
 * (closer than the host's `smoothness`) merge into one liquid shape automatically through the same
 * smooth-min every other gooey shape uses — no separate bridge shape is needed for chips that are
 * simply laid out next to each other.
 */
@Stable
public class GooeyChipGroupState internal constructor() {
    internal var hostBounds: Rect by mutableStateOf(Rect.Zero)
    private val itemBounds = mutableStateMapOf<Int, Rect>()
    private val selectedIndices = mutableStateSetOf<Int>()

    public val selected: Set<Int> get() = selectedIndices

    public fun isSelected(index: Int): Boolean = index in selectedIndices

    /** Flips [index] in or out of [selected]. */
    public fun toggle(index: Int) {
        if (!selectedIndices.add(index)) selectedIndices.remove(index)
    }

    internal fun registerBounds(index: Int, bounds: Rect) {
        itemBounds[index] = bounds
    }

    internal fun blobs(color: Color, cornerRadius: Float, origin: Offset): List<Blob> =
        selectedIndices.mapNotNull { itemBounds[it] }.map { Blob.RoundRect(it.translate(-origin), cornerRadius, color) }
}

/**
 * Draws a gooey background behind [content], one rounded-rect blob per selected chip registered
 * with [gooeyChip]. Pair each chip's `onClick` with [GooeyChipGroupState.toggle].
 */
@Composable
public fun GooeyChipGroupHost(
    state: GooeyChipGroupState,
    color: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GooeyChipGroupDefaults.CornerRadius,
    smoothness: Dp = GooeyChipGroupDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val smoothnessPx = with(density) { smoothness.toPx() }
    Box(modifier.onGloballyPositioned { state.hostBounds = it.rootBounds() }) {
        Canvas(Modifier.matchParentSize()) {
            val origin = state.hostBounds.topLeft
            val blobs = state.blobs(color, cornerRadiusPx, origin)
            if (blobs.isNotEmpty()) with(renderer) { draw(GooeyScene(blobs, smoothness = smoothnessPx)) }
        }
        content()
    }
}

/** Reports this chip's bounds to [state] at [index]; pair with `onClick = { state.toggle(index) }`. */
public fun Modifier.gooeyChip(state: GooeyChipGroupState, index: Int): Modifier =
    onGloballyPositioned { state.registerBounds(index, it.rootBounds()) }
