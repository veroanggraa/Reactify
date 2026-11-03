package com.veroanggra.reactify.stepper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.easeOut
import com.veroanggra.reactify.gooey.lerp
import com.veroanggra.reactify.gooey.phase
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Timeline of one stepper transition, as fractions of progress. The thread grows link by link over
 * the whole transition; the destination dot grows in during the last part so the thread visibly
 * reaches the step before the dot swells out of it.
 */
internal object StepperTimeline {
    const val DOT_GROW_START = 0.6f
}

private const val THREAD_SEGMENTS = 6

/** Solid links and thread ends are this fraction of the dot radius. */
internal const val LINK_RADIUS_FACTOR = 0.45f

/**
 * Blobs for a stepper whose completed chain runs from step 0 up to `min(from, to)`, plus the
 * moving chain of links between [from] and [to]: a necking thread grows along each link in order,
 * turning solid once fully grown, and the destination dot grows in at the end. Moving backwards is
 * the same picture with [progress] mirrored, so the dot shrinks first and the thread then retracts.
 * Every step with an index below the current reach is a full dot; coordinates are host-local.
 */
internal fun stepperBlobs(
    progress: Float,
    from: Int,
    to: Int,
    steps: List<Rect>,
    dotRadius: Float,
    color: Color,
): List<Blob> {
    if (steps.isEmpty()) return emptyList()
    val last = steps.lastIndex
    val base = min(from, to).coerceIn(0, last)
    val head = max(from, to).coerceIn(0, last)
    val p = if (to >= from) progress.coerceIn(0f, 1f) else 1f - progress.coerceIn(0f, 1f)
    val linkRadius = dotRadius * LINK_RADIUS_FACTOR
    val centers = steps.map { it.center }

    return buildList {
        for (i in 0..base) {
            add(Blob.Circle(centers[i], dotRadius, color))
            if (i < base) add(Blob.Capsule(centers[i], centers[i + 1], linkRadius, color))
        }
        if (head == base) return@buildList

        val links = head - base
        for (k in base until head) {
            val local = phase(p, (k - base) / links.toFloat(), (k - base + 1) / links.toFloat())
            if (local <= 0f) break
            if (local >= 1f) {
                add(Blob.Capsule(centers[k], centers[k + 1], linkRadius, color))
                if (k + 1 < head) add(Blob.Circle(centers[k + 1], dotRadius, color))
            } else {
                addAll(linkThread(centers[k], centers[k + 1], easeOut(local), linkRadius * 2f, color))
            }
        }

        val dot = easeOut(phase(p, StepperTimeline.DOT_GROW_START, 1f))
        if (dot > 0.02f) add(Blob.Circle(centers[head], dotRadius * dot, color))
    }
}

/**
 * Thread from [start] reaching [grow] of the way to [end], anchored at [start] and thinning in the
 * middle in proportion to how far it has stretched, like a drop of liquid being drawn out.
 */
private fun linkThread(start: Offset, end: Offset, grow: Float, width: Float, color: Color): List<Blob> {
    val tip = Offset(lerp(start.x, end.x, grow), lerp(start.y, end.y, grow))
    val stretch = ((tip - start).getDistance() / (width * 3f)).coerceIn(0f, 1f)
    return (0 until THREAD_SEGMENTS).map { i ->
        val t0 = i / THREAD_SEGMENTS.toFloat()
        val t1 = (i + 1) / THREAD_SEGMENTS.toFloat()
        val thinning = 1f - 0.6f * stretch * sin(PI.toFloat() * (t0 + t1) / 2f)
        Blob.Capsule(
            start = Offset(lerp(start.x, tip.x, t0), lerp(start.y, tip.y, t0)),
            end = Offset(lerp(start.x, tip.x, t1), lerp(start.y, tip.y, t1)),
            radius = max(width / 2f * thinning, 0f),
            color = color,
        )
    }
}
