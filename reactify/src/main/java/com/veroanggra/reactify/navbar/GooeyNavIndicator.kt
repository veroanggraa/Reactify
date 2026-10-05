package com.veroanggra.reactify.navbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.veroanggra.reactify.gooey.lerpRect
import com.veroanggra.reactify.gooey.slidingIndicatorBlobs
import com.veroanggra.reactify.list.rootBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public object GooeyNavIndicatorDefaults {
    /** Drives the transition; the grow/shrink/break shape itself lives in [slidingIndicatorBlobs]. */
    public val animationSpec: AnimationSpec<Float> = tween(durationMillis = 450, easing = LinearEasing)

    /** Blend distance for merging the old pill, new pill and bridge into one liquid shape. */
    public val Smoothness: Dp = 18.dp
}

@Composable
public fun rememberGooeyNavIndicatorState(
    selectedIndex: Int,
    animationSpec: AnimationSpec<Float> = GooeyNavIndicatorDefaults.animationSpec,
): GooeyNavIndicatorState {
    val scope = rememberCoroutineScope()
    return remember(scope) { GooeyNavIndicatorState(selectedIndex, scope) }.also { it.animationSpec = animationSpec }
}

/**
 * Tracks which item inside a [GooeyNavIndicatorHost] is selected and animates a gooey bridge
 * between registered item bounds, the same grow/break mechanics as the list-to-card transfer.
 * Call [select] from each item's click handler.
 */
@Stable
public class GooeyNavIndicatorState internal constructor(initialIndex: Int, private val scope: CoroutineScope) {
    internal var animationSpec: AnimationSpec<Float> = GooeyNavIndicatorDefaults.animationSpec
    internal var hostBounds: Rect by mutableStateOf(Rect.Zero)

    public var selectedIndex: Int by mutableStateOf(initialIndex)
        private set

    private val itemBounds = mutableStateMapOf<Int, Rect>()
    private val progress = Animatable(1f)
    private var from: Rect by mutableStateOf(Rect.Zero)
    private var to: Rect by mutableStateOf(Rect.Zero)
    private var placed = false

    internal val hasIndicator: Boolean get() = placed

    /** Moves the indicator to [index]. A transition already in flight retargets from where it is now. */
    public fun select(index: Int) {
        if (index == selectedIndex) return
        val target = itemBounds[index] ?: return
        selectedIndex = index
        from = lerpRect(from, to, progress.value)
        to = target
        scope.launch {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec)
        }
    }

    internal fun registerBounds(index: Int, bounds: Rect) {
        itemBounds[index] = bounds
        if (!placed && index == selectedIndex) {
            from = bounds
            to = bounds
            placed = true
        }
    }

    internal fun blobs(color: Color, cornerRadius: Float?, origin: Offset): List<Blob> {
        val radius = cornerRadius ?: (to.height / 2f)
        return slidingIndicatorBlobs(
            progress.value,
            from.translate(-origin),
            to.translate(-origin),
            radius,
            color
        )
    }
}

/**
 * Draws a gooey indicator behind [content], bridging between whichever items call
 * [gooeyNavItem] as [state]'s selection changes. Wrap a navigation bar row with this;
 * items report their own bounds and call [GooeyNavIndicatorState.select] on click.
 *
 * @param cornerRadius corner radius of the indicator pill; null draws a stadium shape (half height).
 */
@Composable
public fun GooeyNavIndicatorHost(
    state: GooeyNavIndicatorState,
    color: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp? = null,
    smoothness: Dp = GooeyNavIndicatorDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val cornerRadiusPx = cornerRadius?.let { with(density) { it.toPx() } }
    val smoothnessPx = with(density) { smoothness.toPx() }
    Box(modifier.onGloballyPositioned { state.hostBounds = it.rootBounds() }) {
        Canvas(Modifier.matchParentSize()) {
            if (!state.hasIndicator) return@Canvas
            val origin = state.hostBounds.topLeft
            val scene = GooeyScene(
                blobs = state.blobs(color, cornerRadiusPx, origin),
                smoothness = smoothnessPx,
            )
            if (scene.blobs.isNotEmpty()) with(renderer) { draw(scene) }
        }
        content()
    }
}

/** Reports this item's bounds to [state] at [index]; pair with `onClick = { state.select(index) }`. */
public fun Modifier.gooeyNavItem(state: GooeyNavIndicatorState, index: Int): Modifier =
    onGloballyPositioned { state.registerBounds(index, it.rootBounds()) }
