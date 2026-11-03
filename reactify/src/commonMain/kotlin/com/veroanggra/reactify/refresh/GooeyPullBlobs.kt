package com.veroanggra.reactify.refresh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.NECK_SEGMENTS
import com.veroanggra.reactify.gooey.easeOut
import com.veroanggra.reactify.gooey.phase
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Fraction of the threshold the drop may travel past it; the overshoot eases out and never reaches this. */
internal const val OVERPULL = 0.15f

/** Drop centre y for a raw [pull]: 1:1 up to [threshold], then soft resistance saturating at (1 + [OVERPULL]) × threshold. */
internal fun dropY(pull: Float, threshold: Float): Float {
    if (pull <= threshold) return pull
    val over = (pull - threshold) / threshold
    return threshold * (1f + OVERPULL * over / (1f + over))
}

/**
 * Blobs for a drop being pulled out of the top edge: a flat lip that sits mostly above y = 0 so
 * only a shallow bulge shows, a drop hanging [pull] px below it, and a neck between them that
 * thins in the middle the further the drop is stretched (the same shape as the dragged token in
 * the transfer). [neckScale] shrinks the neck to nothing when it snaps; [dropScale] shrinks the
 * drop away once refreshing ends.
 */
internal fun pullBlobs(
    pull: Float,
    threshold: Float,
    width: Float,
    dropRadius: Float,
    neckScale: Float,
    dropScale: Float,
    color: Color,
): List<Blob> {
    if (pull <= 0f) return emptyList()
    val emerge = easeOut(phase(pull, 0f, dropRadius))
    val lipBottom = dropRadius * (0.2f + 0.2f * emerge)
    val lipHeight = dropRadius
    val lip = Blob.RoundRect(
        rect = Rect(-dropRadius, lipBottom - lipHeight, width + dropRadius, lipBottom),
        cornerRadius = lipHeight / 2f,
        color = color,
    )

    val anchor = Offset(width / 2f, lipBottom)
    val drop = Offset(width / 2f, dropY(pull, threshold))
    val radius = dropRadius * dropScale * emerge
    val neckRadius = dropRadius * 0.7f * neckScale * emerge
    val vector = drop - anchor
    val stretch = (vector.getDistance() / (dropRadius * 6f)).coerceIn(0f, 1f)

    return buildList {
        add(lip)
        if (neckRadius > 0.01f && vector.getDistance() > 1f) {
            for (i in 0 until NECK_SEGMENTS) {
                val t0 = i / NECK_SEGMENTS.toFloat()
                val t1 = (i + 1) / NECK_SEGMENTS.toFloat()
                val thinning = 1f - 0.65f * sin(PI.toFloat() * (t0 + t1) / 2f) * stretch
                add(Blob.Capsule(anchor + vector * t0, anchor + vector * t1, neckRadius * thinning, color))
            }
        }
        if (radius > 0.01f) add(Blob.Circle(drop, radius, color))
    }
}

/** The held drop with one smaller satellite orbiting it; [t] in [0, 1) is one full turn. */
internal fun refreshingBlobs(center: Offset, dropRadius: Float, t: Float, color: Color): List<Blob> {
    if (dropRadius <= 0.01f) return emptyList()
    val angle = 2f * PI.toFloat() * t
    val satellite = center + Offset(cos(angle), sin(angle)) * (dropRadius * 1.1f)
    return listOf(
        Blob.Circle(center, dropRadius, color),
        Blob.Circle(satellite, dropRadius * 0.45f, color),
    )
}
