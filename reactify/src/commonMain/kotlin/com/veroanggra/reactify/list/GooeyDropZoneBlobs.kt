package com.veroanggra.reactify.list

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob

/**
 * The zone's own rounded rect, scaled up by [hoverGrow] while a drag is over it, plus a short
 * tendril reaching from its nearest edge towards [dragPoint] while that point is close but not yet
 * inside — the same "being pulled towards" read as a dragged token nearing its target, without
 * needing the caller's drag to be a registered Transfer source.
 */
internal fun dropZoneBlobs(
    zoneRect: Rect,
    cornerRadius: Float,
    hoverGrow: Float,
    dragPoint: Offset?,
    pullRadius: Float,
    color: Color,
): List<Blob> {
    if (zoneRect.isEmpty) return emptyList()
    val scale = 1f + 0.06f * hoverGrow
    val cx = zoneRect.center.x
    val cy = zoneRect.center.y
    val scaled = Rect(
        cx - zoneRect.width / 2f * scale,
        cy - zoneRect.height / 2f * scale,
        cx + zoneRect.width / 2f * scale,
        cy + zoneRect.height / 2f * scale,
    )
    val zone = Blob.RoundRect(scaled, cornerRadius, color)
    if (dragPoint == null) return listOf(zone)

    val nearest = Offset(
        dragPoint.x.coerceIn(scaled.left, scaled.right),
        dragPoint.y.coerceIn(scaled.top, scaled.bottom),
    )
    val distance = (dragPoint - nearest).getDistance()
    if (distance < 1f || distance > pullRadius) return listOf(zone)
    val pull = 1f - (distance / pullRadius)
    return listOf(zone, Blob.Capsule(nearest, dragPoint, pullRadius * 0.25f * pull, color))
}
