package com.veroanggra.reactify.control

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.lerp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Track capsule spanning [track]'s full width plus a thumb circle at [progress] along it. The
 * thumb's radius is left to the caller: a touch bigger than the track's half-height makes it bulge
 * past the track's edge so the smooth-min reads one liquid pill instead of a circle resting on it.
 */
internal fun trackAndThumb(progress: Float, track: Rect, thumbRadius: Float, color: Color): List<Blob> {
    val trackRadius = track.height / 2f
    val thumbX = lerp(track.left + trackRadius, track.right - trackRadius, progress.coerceIn(0f, 1f))
    return listOf(
        Blob.Capsule(
            Offset(track.left + trackRadius, track.center.y),
            Offset(track.right - trackRadius, track.center.y),
            trackRadius,
            color,
        ),
        Blob.Circle(Offset(thumbX, track.center.y), thumbRadius, color),
    )
}

/** [trackAndThumb] plus a value bubble rising off the thumb while [dragged] is above zero. */
internal fun sliderBlobs(
    progress: Float,
    track: Rect,
    thumbRadius: Float,
    bubbleRadius: Float,
    bubbleRise: Float,
    dragged: Float,
    color: Color,
): List<Blob> {
    val base = trackAndThumb(progress, track, thumbRadius, color)
    if (dragged <= 0.01f) return base
    val thumbCenter = (base[1] as Blob.Circle).center
    val bubbleCenter = Offset(thumbCenter.x, thumbCenter.y - bubbleRise * dragged)
    return base + risingNeck(thumbCenter, bubbleCenter, thumbRadius * 0.55f, dragged, color) +
        Blob.Circle(bubbleCenter, bubbleRadius * (0.3f + 0.7f * dragged), color)
}

private const val NECK_SEGMENTS = 4

/** Thread from [from] to [to], thinning in the middle in proportion to [stretch]; any direction. */
private fun risingNeck(from: Offset, to: Offset, baseRadius: Float, stretch: Float, color: Color): List<Blob> {
    val vector = to - from
    return (0 until NECK_SEGMENTS).map { i ->
        val t0 = i / NECK_SEGMENTS.toFloat()
        val t1 = (i + 1) / NECK_SEGMENTS.toFloat()
        val thinning = 1f - 0.5f * stretch * sin(PI.toFloat() * (t0 + t1) / 2f)
        Blob.Capsule(from + vector * t0, from + vector * t1, baseRadius * thinning, color)
    }
}
