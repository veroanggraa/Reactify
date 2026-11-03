package com.veroanggra.reactify.progress

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.easeIn
import com.veroanggra.reactify.gooey.easeInOut
import com.veroanggra.reactify.gooey.easeOut
import com.veroanggra.reactify.gooey.lerp
import com.veroanggra.reactify.gooey.neckingThread
import com.veroanggra.reactify.gooey.phase
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Radius of a determinate head relative to the track's half thickness. */
internal const val HEAD_SCALE = 1.25f

/** Share of the track an indeterminate sweep covers, end caps included. */
private const val SWEEP_FRACTION = 0.35f

/** A full ring is 16 capsules, leaving room for the neck and head under the shaders' 24-blob cap. */
private const val ARC_STEP_DEG = 22.5f
private const val MAX_NECK_SEGMENTS = 6

/** Half thickness of the track that fits inside a bounds of [height] once the head's overhang is taken off. */
internal fun trackRadius(height: Float): Float = height / 2f / HEAD_SCALE

/**
 * The visible track inside [bounds]: thinner than the bounds by the head's overhang, ends aligned
 * with where the fill's end caps land at 0 and 1.
 */
internal fun linearTrackRect(bounds: Rect): Rect {
    val r = trackRadius(bounds.height)
    val headR = r * HEAD_SCALE
    return Rect(bounds.left + headR - r, bounds.center.y - r, bounds.right - headR + r, bounds.center.y + r)
}

/**
 * Determinate linear indicator: a fill capsule from the start of [bounds] to [fillFraction] and a
 * round head at [headFraction]. When the head runs ahead of the fill a necking thread joins them,
 * thinning in the middle the further they are apart; once the fill catches up the blend swallows it.
 * Every blob stays inside [bounds].
 */
internal fun linearProgressBlobs(bounds: Rect, fillFraction: Float, headFraction: Float, color: Color): List<Blob> {
    val r = trackRadius(bounds.height)
    val headR = r * HEAD_SCALE
    val cy = bounds.center.y
    val startX = bounds.left + headR
    val endX = bounds.right - headR
    val fillX = lerp(startX, endX, fillFraction.coerceIn(0f, 1f))
    val headX = lerp(startX, endX, headFraction.coerceIn(0f, 1f))
    return buildList {
        if (fillX - startX > 0.5f) add(Blob.Capsule(Offset(startX, cy), Offset(fillX, cy), r, color))
        if (headX - fillX > 0.5f) addAll(neckingThread(fillX, headX, cy, r * 2f, color))
        add(Blob.Circle(Offset(headX, cy), headR, color))
    }
}

/**
 * Indeterminate linear indicator for a looping [t] in `[0, 1)`: a leading capsule sweeps left to
 * right while a trailing drop sets off later and lags behind, so the two start merged, split as
 * the leader pulls away and merge again at the far end. Both grow in at the start of the cycle
 * and shrink away at its end so the wrap-around does not pop.
 */
internal fun indeterminateLinearBlobs(bounds: Rect, t: Float, color: Color): List<Blob> {
    val loop = t - floor(t)
    val scale = min(easeOut(phase(loop, 0f, 0.12f)), 1f - easeIn(phase(loop, 0.88f, 1f)))
    if (scale <= 0.02f) return emptyList()
    val r = trackRadius(bounds.height)
    val headR = r * HEAD_SCALE
    val cy = bounds.center.y
    val startX = bounds.left + headR
    val endX = bounds.right - headR
    val segment = max(bounds.width * SWEEP_FRACTION - 2f * r, 0f)
    val leadStart = lerp(startX, endX - segment, easeInOut(loop))
    val trailX = lerp(startX, endX, easeInOut(phase(loop, 0.2f, 1f)))
    return listOf(
        Blob.Capsule(Offset(leadStart, cy), Offset(leadStart + segment, cy), r * scale, color),
        Blob.Circle(Offset(trailX, cy), r * scale, color),
    )
}

/**
 * Determinate ring: an arc of capsules clockwise from 12 o'clock to [sweepFraction] of a turn, a
 * round head at [headFraction], and a necking thread along the ring whenever the head leads.
 * [radius] is the ring's centre line; blobs reach `radius + stroke / 2 * HEAD_SCALE` from [center].
 */
internal fun circularProgressBlobs(
    center: Offset,
    radius: Float,
    stroke: Float,
    sweepFraction: Float,
    headFraction: Float,
    color: Color,
): List<Blob> {
    val r = stroke / 2f
    val sweepDeg = 360f * sweepFraction.coerceIn(0f, 1f)
    val headDeg = 360f * headFraction.coerceIn(0f, 1f)
    return buildList {
        if (sweepDeg > 0.5f) addAll(arcSegments(center, radius, 0f, sweepDeg, r, color) { 1f })
        if (headDeg - sweepDeg > 0.5f) {
            val arcLength = radius * (headDeg - sweepDeg) * DEG_TO_RAD
            val stretch = (arcLength / (stroke * 3f)).coerceIn(0f, 1f)
            addAll(arcSegments(center, radius, sweepDeg, headDeg, r, color, MAX_NECK_SEGMENTS) { mid ->
                1f - 0.6f * stretch * sin(PI.toFloat() * mid)
            })
        }
        add(Blob.Circle(pointOnRing(center, radius, headDeg), r * HEAD_SCALE, color))
    }
}

/**
 * Indeterminate ring for a looping [t] in `[0, 1)`: three drops orbiting at one, two and three
 * turns per cycle, so they keep catching up with and overtaking each other, merging and splitting
 * as they pass. Whole turns per cycle keep the wrap-around seamless.
 */
internal fun indeterminateCircularBlobs(center: Offset, radius: Float, stroke: Float, t: Float, color: Color): List<Blob> {
    val r = stroke / 2f
    val deg = (t - floor(t)) * 360f
    return listOf(
        Blob.Circle(pointOnRing(center, radius, deg), r * HEAD_SCALE, color),
        Blob.Circle(pointOnRing(center, radius, deg * 2f + 120f), r, color),
        Blob.Circle(pointOnRing(center, radius, deg * 3f + 240f), r * 0.8f, color),
    )
}

private const val DEG_TO_RAD = (PI / 180.0).toFloat()

/** Point on the ring at [deg] clockwise from 12 o'clock. */
internal fun pointOnRing(center: Offset, radius: Float, deg: Float): Offset {
    val a = (deg - 90f) * DEG_TO_RAD
    return Offset(center.x + radius * cos(a), center.y + radius * sin(a))
}

/** Capsule chain hugging the ring from [fromDeg] to [toDeg], each segment's radius scaled by [thinningAt] its midpoint. */
private inline fun arcSegments(
    center: Offset,
    radius: Float,
    fromDeg: Float,
    toDeg: Float,
    r: Float,
    color: Color,
    maxSegments: Int = Int.MAX_VALUE,
    thinningAt: (Float) -> Float,
): List<Blob> {
    val span = toDeg - fromDeg
    val n = ceil(span / ARC_STEP_DEG).toInt().coerceIn(1, maxSegments)
    return (0 until n).map { i ->
        val t0 = i / n.toFloat()
        val t1 = (i + 1) / n.toFloat()
        Blob.Capsule(
            start = pointOnRing(center, radius, fromDeg + span * t0),
            end = pointOnRing(center, radius, fromDeg + span * t1),
            radius = max(r * thinningAt((t0 + t1) / 2f), 0f),
            color = color,
        )
    }
}
