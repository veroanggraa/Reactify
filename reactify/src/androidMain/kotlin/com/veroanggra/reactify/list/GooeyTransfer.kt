package com.veroanggra.reactify.list

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.toSize
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import kotlin.math.roundToInt

/**
 * Root of a gooey transfer. The gooey overlay and in-flight tokens are drawn above [content],
 * so sources and targets may live anywhere inside it, including inside scrolling containers.
 */
@Composable
public fun GooeyTransferHost(
    state: GooeyTransferState,
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    smoothness: Dp = GooeyTransferDefaults.Smoothness,
    maxTintAlpha: Float = GooeyTransferDefaults.MaxTintAlpha,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.onGloballyPositioned { state.hostBounds = it.rootBounds() }) {
        content()
        Canvas(Modifier.matchParentSize()) {
            val scene = state.buildScene(smoothness.toPx(), maxTintAlpha)
            if (!scene.isEmpty) with(renderer) { draw(scene) }
        }
        state.items.forEach { item ->
            if (item.isInFlight) {
                key(item.key) {
                    OverlayToken(state, base = { item.tokenStart }, current = { state.tokenBounds(item) }, item.token)
                }
            }
        }
        state.drag?.let { session ->
            if (session.drawsToken) {
                key(session) {
                    OverlayToken(state, base = { session.anchor }, current = { session.tokenRect() }, session.source.token)
                }
            }
        }
    }
}

/**
 * Token drawn above the gooey layer. Measured at [base]'s size and transformed to [current]
 * each frame; both are read in layout/draw so scrolling or dragging never recomposes.
 */
@Composable
private fun OverlayToken(
    state: GooeyTransferState,
    base: () -> Rect,
    current: () -> Rect,
    token: @Composable () -> Unit,
) {
    Box(
        Modifier
            .layout { measurable, _ ->
                val start = base()
                val w = start.width.roundToInt().coerceAtLeast(1)
                val h = start.height.roundToInt().coerceAtLeast(1)
                val placeable = measurable.measure(Constraints.fixed(w, h))
                layout(w, h) { placeable.place(0, 0) }
            }
            .graphicsLayer {
                val start = base()
                val bounds = current()
                val origin = state.hostBounds.topLeft
                transformOrigin = TransformOrigin(0f, 0f)
                translationX = bounds.left - origin.x
                translationY = bounds.top - origin.y
                scaleX = bounds.width / start.width.coerceAtLeast(1f)
                scaleY = bounds.height / start.height.coerceAtLeast(1f)
            },
    ) { token() }
}

@Stable
public interface GooeySourceScope {
    public val isTransferred: Boolean

    /** Marks where the token sits inside the source, so the flying copy starts from there. */
    public fun Modifier.tokenAnchor(): Modifier
}

/**
 * An item whose [token] can be transferred into the [GooeyTarget] by tapping it or, when
 * [dragEnabled], by long-pressing and dragging the token out. Long-press keeps the drag from
 * fighting with scrolling containers.
 *
 * @param color tint of the gooey overlay for this source.
 * @param cornerRadius must match the visual corner radius of [content]'s background.
 */
@Composable
public fun GooeySource(
    state: GooeyTransferState,
    key: Any,
    color: Color,
    token: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GooeyTransferDefaults.CornerRadius,
    enabled: Boolean = true,
    dragEnabled: Boolean = true,
    transferredAlpha: Float = GooeyTransferDefaults.TransferredAlpha,
    content: @Composable GooeySourceScope.() -> Unit,
) {
    val entry = remember(state, key) { SourceEntry(key) }
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val flingThreshold = with(density) { GooeyTransferDefaults.FlingVelocity.toPx() }
    val haptics = LocalHapticFeedback.current
    SideEffect {
        entry.color = color
        entry.token = token
        entry.cornerRadius = cornerRadiusPx
    }
    DisposableEffect(state, entry) {
        state.register(entry)
        onDispose { state.unregister(entry) }
    }

    val transferred = state.isTransferred(key)
    val scope = remember(entry) { SourceScopeImpl(entry) }
    scope.isTransferred = transferred
    val canDrag = dragEnabled && enabled && !transferred

    Box(
        modifier
            .onGloballyPositioned { entry.bounds = it.rootBounds() }
            .graphicsLayer { alpha = if (transferred) transferredAlpha else 1f }
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(enabled = enabled && !transferred) { state.transfer(key) }
            .pointerInput(state, key, canDrag) {
                if (!canDrag) return@pointerInput
                val velocity = VelocityTracker()
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        velocity.resetTracking()
                        if (state.startDrag(key)) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        velocity.addPosition(change.uptimeMillis, change.position)
                        state.dragBy(amount)
                    },
                    onDragEnd = {
                        val v = velocity.calculateVelocity()
                        state.endDrag(Offset(v.x, v.y), flingThreshold)
                    },
                    onDragCancel = { state.cancelDrag() },
                )
            },
    ) { scope.content() }
}

private class SourceScopeImpl(private val entry: SourceEntry) : GooeySourceScope {
    override var isTransferred: Boolean by mutableStateOf(false)

    override fun Modifier.tokenAnchor(): Modifier =
        onGloballyPositioned { entry.anchorBounds = it.rootBounds() }
}

@Stable
public interface GooeyTargetScope {
    /**
     * Row of transferred tokens, newest first. Hidden slots reserve space for tokens in flight.
     *
     * @param removable when true, tapping a resting token sends it back to its source via
     * [GooeyTransferState.returnToSource].
     */
    @Composable
    public fun TransferredTokens(
        modifier: Modifier = Modifier,
        tokenSize: Dp = GooeyTransferDefaults.TokenSize,
        spacing: Dp = GooeyTransferDefaults.TokenSpacing,
        removable: Boolean = true,
    )
}

/**
 * The container tokens are transferred into.
 *
 * @param cornerRadius must match the visual corner radius of [content]'s background.
 * @param notchRadius current radius of a [GooeyNotchShape] notch on the background, if any.
 */
@Composable
public fun GooeyTarget(
    state: GooeyTransferState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GooeyTransferDefaults.CornerRadius,
    notchRadius: Dp = 0.dp,
    content: @Composable GooeyTargetScope.() -> Unit,
) {
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val notchRadiusPx = with(density) { notchRadius.toPx() }
    SideEffect {
        state.targetCornerRadius = cornerRadiusPx
        state.targetNotchRadius = notchRadiusPx
    }
    DisposableEffect(state) {
        onDispose { state.targetBounds = null }
    }
    val scope = remember(state) { TargetScopeImpl(state) }
    Box(modifier.onGloballyPositioned { state.targetBounds = it.rootBounds() }) {
        scope.content()
    }
}

private class TargetScopeImpl(private val state: GooeyTransferState) : GooeyTargetScope {
    @Composable
    override fun TransferredTokens(modifier: Modifier, tokenSize: Dp, spacing: Dp, removable: Boolean) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            state.items.forEach { item ->
                key(item.key) {
                    val resting by remember(item) { derivedStateOf { !item.isInFlight && !item.isReturning } }
                    Box(
                        Modifier
                            .size(tokenSize)
                            .onGloballyPositioned { item.slotBounds = it.rootBounds() }
                            .animatePlacement()
                            .graphicsLayer { alpha = if (item.isInFlight) 0f else 1f }
                            .clickable(
                                enabled = removable && resting,
                                onClickLabel = "Remove",
                                interactionSource = null,
                                indication = null,
                            ) { state.returnToSource(item.key) },
                    ) { item.token() }
                }
            }
        }
    }
}

/**
 * Springs content from its previous placement to the new one. Modifiers before this one still
 * report the final position, which is what flying tokens aim for.
 */
@Composable
private fun Modifier.animatePlacement(): Modifier {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var placed by remember { mutableStateOf<Offset?>(null) }
    return onPlaced { coordinates ->
        val position = coordinates.positionInParent()
        val previous = placed
        placed = position
        if (previous != null && previous != position) {
            scope.launch {
                offset.snapTo(offset.value + previous - position)
                offset.animateTo(Offset.Zero, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow))
            }
        }
    }.offset { offset.value.round() }
}

internal fun LayoutCoordinates.rootBounds(): Rect = Rect(positionInRoot(), size.toSize())
