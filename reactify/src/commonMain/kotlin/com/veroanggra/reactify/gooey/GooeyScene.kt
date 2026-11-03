package com.veroanggra.reactify.gooey

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

/** A primitive shape the gooey renderer merges with its neighbours. Coordinates are host-local pixels. */
@Immutable
public sealed interface Blob {
    public val color: Color

    @Immutable
    public data class RoundRect(
        val rect: Rect,
        val cornerRadius: Float,
        override val color: Color,
    ) : Blob

    @Immutable
    public data class Circle(
        val center: Offset,
        val radius: Float,
        override val color: Color,
    ) : Blob

    /** Segment from [start] to [end] with rounded ends; chains of these form stretched necks. */
    @Immutable
    public data class Capsule(
        val start: Offset,
        val end: Offset,
        val radius: Float,
        override val color: Color,
    ) : Blob

    /**
     * An arbitrary simple polygon (no self-intersections), exact rather than approximated from
     * circles/round rects. At most one `Polygon` per [GooeyScene] is merged by the AGSL and SkSL
     * renderers (and at most [MAX_POLYGON_VERTICES] vertices); [PathGooeyRenderer] and the CPU
     * [GooeyField] reference have no such limit.
     */
    @Immutable
    public data class Polygon(
        val vertices: List<Offset>,
        override val color: Color,
    ) : Blob
}

/** Hard cap on vertices per [Blob.Polygon] shared by the AGSL and SkSL renderers. */
public const val MAX_POLYGON_VERTICES: Int = 8

internal fun Blob.bounds(): Rect = when (this) {
    is Blob.RoundRect -> rect
    is Blob.Circle -> Rect(center, radius)
    is Blob.Capsule -> Rect(
        minOf(start.x, end.x) - radius,
        minOf(start.y, end.y) - radius,
        maxOf(start.x, end.x) + radius,
        maxOf(start.y, end.y) + radius,
    )
    is Blob.Polygon -> {
        var left = Float.POSITIVE_INFINITY
        var top = Float.POSITIVE_INFINITY
        var right = Float.NEGATIVE_INFINITY
        var bottom = Float.NEGATIVE_INFINITY
        vertices.forEach {
            left = minOf(left, it.x)
            top = minOf(top, it.y)
            right = maxOf(right, it.x)
            bottom = maxOf(bottom, it.y)
        }
        Rect(left, top, right, bottom)
    }
}

/**
 * Everything a [GooeyRenderer] needs for one frame.
 *
 * @param cutouts shapes smoothly subtracted from [blobs], e.g. the notch around a button.
 * @param smoothness blend distance in pixels; larger values make blobs merge from further apart.
 */
@Immutable
public data class GooeyScene(
    val blobs: List<Blob>,
    val cutouts: List<Blob> = emptyList(),
    val smoothness: Float,
) {
    public val isEmpty: Boolean get() = blobs.isEmpty()
}
