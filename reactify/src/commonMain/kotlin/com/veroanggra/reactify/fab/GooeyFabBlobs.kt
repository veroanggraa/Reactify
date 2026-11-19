package com.veroanggra.reactify.fab

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.easeOut
import com.veroanggra.reactify.gooey.phase

/** How much of the overall 0..1 timeline each child's own stagger window is offset from the last. */
private const val STAGGER_FRACTION = 0.6f

/**
 * This child's own 0..1 progress within the shared [progress] timeline: children start one after
 * another (index 0 first) and each still has room to finish easing in before 1.
 */
internal fun fabChildLocalProgress(progress: Float, index: Int, itemCount: Int): Float {
    if (itemCount <= 0) return 0f
    val window = 1f / (itemCount + (1f - STAGGER_FRACTION))
    val start = index * window * STAGGER_FRACTION
    return easeOut(phase(progress, start, (start + window).coerceAtMost(1f)))
}

/**
 * Main FAB circle plus one child circle per item, each spawning up a vertical column as its own
 * [fabChildLocalProgress] window opens, still joined to the main circle by a neck that thins away
 * as the child settles into its resting spot [spacing] further out than the last.
 */
internal fun fabMenuBlobs(
    center: Offset,
    mainRadius: Float,
    childRadius: Float,
    spacing: Float,
    itemCount: Int,
    progress: Float,
    color: Color,
): List<Blob> {
    val main = Blob.Circle(center, mainRadius, color)
    if (itemCount <= 0 || progress <= 0.01f) return listOf(main)
    return buildList {
        add(main)
        for (i in 0 until itemCount) {
            val local = fabChildLocalProgress(progress, i, itemCount)
            if (local <= 0.01f) continue
            val childCenter = Offset(center.x, center.y - spacing * (i + 1) * local)
            val neckVisible = 1f - local
            if (neckVisible > 0.02f) add(Blob.Capsule(center, childCenter, childRadius * 0.6f * neckVisible, color))
            add(Blob.Circle(childCenter, childRadius * local, color))
        }
    }
}
