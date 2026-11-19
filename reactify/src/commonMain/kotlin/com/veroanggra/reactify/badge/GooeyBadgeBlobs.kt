package com.veroanggra.reactify.badge

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.math.PI
import kotlin.math.sin

/**
 * A badge circle springing out of [anchor] to rest at `anchor + restOffset`, joined to the anchor
 * by a short stem that is only visible mid-pop (zero at both ends of [grow]) — the same budding
 * shape a drop makes leaving a surface, not an abrupt circle appearing out of nowhere.
 */
internal fun badgeBlobs(anchor: Offset, restOffset: Offset, badgeRadius: Float, grow: Float, color: Color): List<Blob> {
    val clamped = grow.coerceIn(0f, 1f)
    if (clamped <= 0.01f) return emptyList()
    val center = anchor + restOffset * clamped
    val stem = sin(PI.toFloat() * clamped)
    return buildList {
        if (stem > 0.02f) add(Blob.Capsule(anchor, center, badgeRadius * 0.55f * stem, color))
        add(Blob.Circle(center, badgeRadius * clamped, color))
    }
}
