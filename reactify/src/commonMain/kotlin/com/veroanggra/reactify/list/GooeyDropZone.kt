package com.veroanggra.reactify.list

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene

public object GooeyDropZoneDefaults {
    public val CornerRadius: Dp = 16.dp
    public val Smoothness: Dp = 16.dp
    public val PullRadius: Dp = 56.dp
    public val hoverSpec: AnimationSpec<Float> = spring(dampingRatio = 0.6f, stiffness = 400f)
}

@Composable
public fun rememberGooeyDropZoneState(): GooeyDropZoneState = remember { GooeyDropZoneState() }

/**
 * A drop target that reaches a short gooey tendril towards any external drag point reported via
 * [updateDrag] once it is close enough, and bulges slightly once that point is inside its bounds.
 * Unlike [GooeyTarget], the drag does not have to come from a registered [GooeySource]: call
 * [updateDrag] with the drag's current position (in root coordinates) from your own gesture
 * handling, and `null` once it ends.
 */
@Stable
public class GooeyDropZoneState internal constructor() {
    internal var bounds: Rect by mutableStateOf(Rect.Zero)
    private var dragPoint: Offset? by mutableStateOf(null)

    public val isHovering: Boolean get() = dragPoint?.let { bounds.contains(it) } == true

    public fun updateDrag(point: Offset?) {
        dragPoint = point
    }

    internal fun blobs(color: Color, cornerRadius: Float, hoverGrow: Float, pullRadius: Float): List<com.veroanggra.reactify.gooey.Blob> {
        val origin = bounds.topLeft
        return dropZoneBlobs(bounds.translate(-origin), cornerRadius, hoverGrow, dragPoint?.minus(origin), pullRadius, color)
    }
}

/** Draws [state]'s gooey background behind [content]; wrap the area you want to accept drops. */
@Composable
public fun GooeyDropZone(
    state: GooeyDropZoneState,
    color: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GooeyDropZoneDefaults.CornerRadius,
    smoothness: Dp = GooeyDropZoneDefaults.Smoothness,
    pullRadius: Dp = GooeyDropZoneDefaults.PullRadius,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    content: @Composable BoxScope.() -> Unit = {},
) {
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val smoothnessPx = with(density) { smoothness.toPx() }
    val pullRadiusPx = with(density) { pullRadius.toPx() }
    val hoverGrow by animateFloatAsState(if (state.isHovering) 1f else 0f, GooeyDropZoneDefaults.hoverSpec, label = "gooeyDropZoneHover")
    Box(modifier.onGloballyPositioned { state.bounds = it.rootBounds() }) {
        Canvas(Modifier.matchParentSize()) {
            val blobs = state.blobs(color, cornerRadiusPx, hoverGrow, pullRadiusPx)
            if (blobs.isNotEmpty()) with(renderer) { draw(GooeyScene(blobs, smoothness = smoothnessPx)) }
        }
        content()
    }
}
