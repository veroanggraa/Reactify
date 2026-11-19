package com.veroanggra.reactify.fab

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public object GooeyFabDefaults {
    public val MainRadius: Dp = 28.dp
    public val ChildRadius: Dp = 20.dp
    public val Spacing: Dp = 64.dp
    public val Smoothness: Dp = 10.dp
    public val spec: AnimationSpec<Float> = tween(durationMillis = 420)
}

@Composable
public fun rememberGooeyFabState(): GooeyFabState {
    val scope = rememberCoroutineScope()
    return remember(scope) { GooeyFabState(scope) }
}

/** Whether a [GooeyFabMenu]'s children are spawned out; drives the animation behind [toggle]. */
@Stable
public class GooeyFabState internal constructor(private val scope: CoroutineScope) {
    internal val progress = Animatable(0f)
    public var expanded: Boolean by mutableStateOf(false)
        private set

    public fun toggle() {
        if (expanded) collapse() else expand()
    }

    public fun expand() {
        expanded = true
        scope.launch { progress.animateTo(1f, GooeyFabDefaults.spec) }
    }

    public fun collapse() {
        expanded = false
        scope.launch { progress.animateTo(0f, GooeyFabDefaults.spec) }
    }
}

/**
 * A FAB that spawns [itemCount] child actions up a vertical column, one after another, each still
 * joined to the main button by a neck that thins away as it settles into place.
 *
 * @param itemContent an item's own content and its local 0..1 progress (0 tucked away in the main
 * button, 1 fully settled); the item's own box is already faded and scaled by that progress.
 */
@Composable
public fun GooeyFabMenu(
    state: GooeyFabState,
    itemCount: Int,
    color: Color,
    modifier: Modifier = Modifier,
    mainRadius: Dp = GooeyFabDefaults.MainRadius,
    childRadius: Dp = GooeyFabDefaults.ChildRadius,
    spacing: Dp = GooeyFabDefaults.Spacing,
    smoothness: Dp = GooeyFabDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    mainContent: @Composable () -> Unit = {},
    itemContent: @Composable (index: Int, progress: Float) -> Unit = { _, _ -> },
) {
    val density = LocalDensity.current
    val mainRadiusPx = with(density) { mainRadius.toPx() }
    val childRadiusPx = with(density) { childRadius.toPx() }
    val spacingPx = with(density) { spacing.toPx() }
    val smoothnessPx = with(density) { smoothness.toPx() }
    val totalHeight = mainRadius * 2 + spacing * itemCount

    Box(modifier.width(mainRadius * 2).height(totalHeight), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height - mainRadiusPx)
            val blobs = fabMenuBlobs(center, mainRadiusPx, childRadiusPx, spacingPx, itemCount, state.progress.value, color)
            with(renderer) { draw(GooeyScene(blobs, smoothness = smoothnessPx)) }
        }
        for (i in 0 until itemCount) {
            val local = fabChildLocalProgress(state.progress.value, i, itemCount)
            if (local <= 0.01f) continue
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -spacing * (i + 1) * local)
                    .size(childRadius * 2)
                    .graphicsLayer { alpha = local; scaleX = local; scaleY = local },
                contentAlignment = Alignment.Center,
            ) { itemContent(i, local) }
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .size(mainRadius * 2)
                .clickable { state.toggle() },
            contentAlignment = Alignment.Center,
        ) { mainContent() }
    }
}
