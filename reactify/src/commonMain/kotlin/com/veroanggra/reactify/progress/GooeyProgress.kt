package com.veroanggra.reactify.progress

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene

public object GooeyProgressDefaults {
    /** Width of a linear indicator when the caller's modifier does not constrain it. */
    public val LinearWidth: Dp = 240.dp

    /** Total height of a linear indicator. The track inside is thinner so the round head fits. */
    public val Height: Dp = 12.dp

    /** Outer diameter of a circular indicator, head included. */
    public val CircularSize: Dp = 48.dp

    /** Thickness of a circular indicator's ring. */
    public val StrokeWidth: Dp = 6.dp

    /** Blend distance; large enough for the head and fill to read as one drop when they touch. */
    public val Smoothness: Dp = 10.dp

    /** Moves the head. Faster than [fillSpec], so the head leads and a neck stretches behind it. */
    public val headSpec: AnimationSpec<Float> = tween(durationMillis = 250, easing = LinearOutSlowInEasing)

    /** Moves the fill; it catches up with the head and swallows the neck. */
    public val fillSpec: AnimationSpec<Float> = tween(durationMillis = 650, easing = FastOutSlowInEasing)
}

private const val LINEAR_CYCLE_MS = 1600
private const val CIRCULAR_CYCLE_MS = 2000

/**
 * Determinate linear progress: a liquid fill whose round head races ahead when [progress] jumps,
 * stretching a neck that the fill then catches up with and absorbs.
 *
 * @param progress fraction done, clamped to 0..1. Each change animates from wherever the bar is.
 * @param color fill and head colour. The track is drawn plainly in [trackColor] underneath.
 * @param height total height, head included; the track is `height / 1.25` thick.
 */
@Composable
public fun GooeyLinearProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = color.copy(alpha = 0.15f),
    height: Dp = GooeyProgressDefaults.Height,
    smoothness: Dp = GooeyProgressDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val target = progress.coerceIn(0f, 1f)
    val head by animateFloatAsState(target, GooeyProgressDefaults.headSpec, label = "gooeyProgressHead")
    val fill by animateFloatAsState(target, GooeyProgressDefaults.fillSpec, label = "gooeyProgressFill")
    val smoothnessPx = with(LocalDensity.current) { smoothness.toPx() }
    Canvas(modifier.width(GooeyProgressDefaults.LinearWidth).height(height)) {
        val bounds = Rect(Offset.Zero, size)
        drawLinearTrack(bounds, trackColor)
        val scene = GooeyScene(linearProgressBlobs(bounds, fill, head, color), smoothness = smoothnessPx)
        with(renderer) { draw(scene) }
    }
}

/**
 * Indeterminate linear progress: a capsule sweeps along the track with a drop chasing it, the two
 * merging and splitting as the gap between them changes. Same layout rules as the determinate overload.
 */
@Composable
public fun GooeyLinearProgress(
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = color.copy(alpha = 0.15f),
    height: Dp = GooeyProgressDefaults.Height,
    smoothness: Dp = GooeyProgressDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val t by rememberInfiniteTransition(label = "gooeyLinearProgress").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(LINEAR_CYCLE_MS, easing = LinearEasing)),
        label = "cycle",
    )
    val smoothnessPx = with(LocalDensity.current) { smoothness.toPx() }
    Canvas(modifier.width(GooeyProgressDefaults.LinearWidth).height(height)) {
        val bounds = Rect(Offset.Zero, size)
        drawLinearTrack(bounds, trackColor)
        val scene = GooeyScene(indeterminateLinearBlobs(bounds, t, color), smoothness = smoothnessPx)
        if (!scene.isEmpty) with(renderer) { draw(scene) }
    }
}

/**
 * Determinate circular progress: an arc grows clockwise from 12 o'clock with a round head that
 * runs ahead along the ring when [progress] jumps, trailing a neck the arc then catches up with.
 *
 * @param size outer diameter, head included.
 * @param strokeWidth thickness of the ring; the head is 1.25× as wide.
 */
@Composable
public fun GooeyCircularProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = GooeyProgressDefaults.CircularSize,
    strokeWidth: Dp = GooeyProgressDefaults.StrokeWidth,
    trackColor: Color = color.copy(alpha = 0.15f),
    smoothness: Dp = GooeyProgressDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val target = progress.coerceIn(0f, 1f)
    val head by animateFloatAsState(target, GooeyProgressDefaults.headSpec, label = "gooeyProgressHead")
    val fill by animateFloatAsState(target, GooeyProgressDefaults.fillSpec, label = "gooeyProgressFill")
    val smoothnessPx = with(LocalDensity.current) { smoothness.toPx() }
    Canvas(modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val radius = ringRadius(stroke)
        drawCircle(trackColor, radius, center, style = Stroke(stroke))
        val scene = GooeyScene(circularProgressBlobs(center, radius, stroke, fill, head, color), smoothness = smoothnessPx)
        with(renderer) { draw(scene) }
    }
}

/**
 * Indeterminate circular progress: three drops orbit the ring at different speeds, merging as
 * they overtake each other and pinching apart again. Same layout rules as the determinate overload.
 */
@Composable
public fun GooeyCircularProgress(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = GooeyProgressDefaults.CircularSize,
    strokeWidth: Dp = GooeyProgressDefaults.StrokeWidth,
    trackColor: Color = color.copy(alpha = 0.15f),
    smoothness: Dp = GooeyProgressDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val t by rememberInfiniteTransition(label = "gooeyCircularProgress").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(CIRCULAR_CYCLE_MS, easing = LinearEasing)),
        label = "cycle",
    )
    val smoothnessPx = with(LocalDensity.current) { smoothness.toPx() }
    Canvas(modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val radius = ringRadius(stroke)
        drawCircle(trackColor, radius, center, style = Stroke(stroke))
        val scene = GooeyScene(indeterminateCircularBlobs(center, radius, stroke, t, color), smoothness = smoothnessPx)
        with(renderer) { draw(scene) }
    }
}

private fun DrawScope.drawLinearTrack(bounds: Rect, color: Color) {
    val track = linearTrackRect(bounds)
    drawRoundRect(color, topLeft = track.topLeft, size = track.size, cornerRadius = CornerRadius(track.height / 2f))
}

/** Ring centre line that keeps the head (1.25× the stroke) inside the canvas. */
private fun DrawScope.ringRadius(stroke: Float): Float = size.minDimension / 2f - stroke / 2f * HEAD_SCALE
