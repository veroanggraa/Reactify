package com.veroanggra.reactify.refresh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

public object GooeyPullToRefreshDefaults {
    /** Pull distance at which releasing starts a refresh. */
    public val Threshold: Dp = 72.dp

    /** Radius of the drop that hangs from the top edge. */
    public val DropRadius: Dp = 14.dp

    /** Blend distance for merging the lip, neck and drop into one liquid shape. */
    public val Smoothness: Dp = 16.dp
}

/** Creates and remembers the state for a [GooeyPullToRefresh]. */
@Composable
public fun rememberGooeyPullToRefreshState(): GooeyPullToRefreshState {
    val scope = rememberCoroutineScope()
    return remember(scope) { GooeyPullToRefreshState(scope) }
}

/**
 * Tracks how far the content inside a [GooeyPullToRefresh] has been pulled past its start and
 * whether a refresh is in progress. The pull is driven by nested scrolling, so the content must
 * be scrollable (a `LazyColumn`, `Modifier.verticalScroll`, ...).
 */
@Stable
public class GooeyPullToRefreshState internal constructor(private val scope: CoroutineScope) {
    internal val pullPx = Animatable(0f)
    internal val neckScale = Animatable(1f)
    internal val dropScale = Animatable(1f)
    internal var thresholdPx: Float = 1f
    internal var onRefresh: () -> Unit = {}

    /** Pull distance as a fraction of the threshold; exceeds 1 while over-pulled. */
    public val pullProgress: Float get() = pullPx.value / thresholdPx

    /** True from the moment a refresh is triggered until the host is told it finished. */
    public var isRefreshing: Boolean by mutableStateOf(false)
        private set

    // Raw pull as of the latest gesture event; pullPx catches up on the next frame.
    private var rawPull = 0f

    internal val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || isRefreshing) return Offset.Zero
            if (available.y >= 0f || rawPull <= 0f) return Offset.Zero
            val consumedY = max(available.y, -rawPull / RESISTANCE)
            setPull(rawPull + consumedY * RESISTANCE)
            return Offset(0f, consumedY)
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput || isRefreshing || available.y <= 0f) return Offset.Zero
            setPull(rawPull + available.y * RESISTANCE)
            return Offset(0f, available.y)
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (isRefreshing || rawPull <= 0f) return Velocity.Zero
            if (rawPull >= thresholdPx) startRefresh() else release()
            return available
        }
    }

    private fun setPull(value: Float) {
        rawPull = max(value, 0f)
        scope.launch { pullPx.snapTo(rawPull) }
    }

    private fun release() {
        rawPull = 0f
        scope.launch { pullPx.animateTo(0f, tween(durationMillis = 300)) }
    }

    internal fun startRefresh() {
        if (isRefreshing) return
        isRefreshing = true
        rawPull = thresholdPx
        scope.launch { pullPx.animateTo(thresholdPx, tween(durationMillis = 250)) }
        scope.launch { neckScale.animateTo(0f, tween(durationMillis = 250)) }
        onRefresh()
    }

    internal fun finishRefresh() {
        if (!isRefreshing) return
        scope.launch {
            dropScale.animateTo(0f, tween(durationMillis = 220))
            rawPull = 0f
            pullPx.snapTo(0f)
            neckScale.snapTo(1f)
            dropScale.snapTo(1f)
            isRefreshing = false
        }
    }

    private companion object {
        const val RESISTANCE = 0.5f
    }
}

/**
 * Pull-to-refresh whose indicator is a drop of liquid stretched out of the top edge: it hangs
 * from a neck that thins as it's pulled, snaps free when the pull passes [threshold], and spins a
 * satellite while [isRefreshing]. [content] must be scrollable — the pull comes from the scroll
 * it can't consume. Set [isRefreshing] back to false when your work is done and the drop melts away.
 */
@Composable
public fun GooeyPullToRefresh(
    state: GooeyPullToRefreshState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    threshold: Dp = GooeyPullToRefreshDefaults.Threshold,
    dropRadius: Dp = GooeyPullToRefreshDefaults.DropRadius,
    smoothness: Dp = GooeyPullToRefreshDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    val dropRadiusPx = with(density) { dropRadius.toPx() }
    val smoothnessPx = with(density) { smoothness.toPx() }
    state.thresholdPx = thresholdPx
    state.onRefresh = onRefresh

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) state.startRefresh() else state.finishRefresh()
    }

    val satellite: State<Float>? = if (state.isRefreshing) {
        rememberInfiniteTransition(label = "gooeyRefresh").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
            label = "satellite",
        )
    } else {
        null
    }

    Box(modifier.nestedScroll(state.connection)) {
        content()
        Canvas(Modifier.matchParentSize()) {
            val pull = state.pullPx.value
            if (pull <= 0f) return@Canvas
            val refreshing = state.isRefreshing
            val blobs = pullBlobs(
                pull = pull,
                threshold = thresholdPx,
                width = size.width,
                dropRadius = dropRadiusPx,
                neckScale = state.neckScale.value,
                dropScale = if (refreshing) 0f else state.dropScale.value,
                color = color,
            ) + if (refreshing) {
                refreshingBlobs(
                    center = Offset(size.width / 2f, dropY(pull, thresholdPx)),
                    dropRadius = dropRadiusPx * state.dropScale.value,
                    t = satellite?.value ?: 0f,
                    color = color,
                )
            } else {
                emptyList()
            }
            val scene = GooeyScene(blobs = blobs, smoothness = smoothnessPx)
            if (!scene.isEmpty) with(renderer) { draw(scene) }
        }
    }
}
