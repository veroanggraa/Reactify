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
}

internal fun Blob.bounds(): Rect = when (this) {
    is Blob.RoundRect -> rect
    is Blob.Circle -> Rect(center, radius)
    is Blob.Capsule -> Rect(
        minOf(start.x, end.x) - radius,
        minOf(start.y, end.y) - radius,
        maxOf(start.x, end.x) + radius,
        maxOf(start.y, end.y) + radius,
    )
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
