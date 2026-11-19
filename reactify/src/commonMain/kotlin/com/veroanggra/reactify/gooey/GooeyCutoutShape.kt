package com.veroanggra.reactify.gooey

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * A `BottomAppBar`-style shape with a smooth concave cutout centred on its top edge, sized to
 * clear a FAB resting in it, shaped by the same distance field as the gooey renderers.
 *
 * @param fabRadius radius of the FAB the cutout clears; the cutout itself is [fabRadius] + [margin].
 * @param progress how open the cutout is, 0 (flat edge, FAB undocked) to 1 (fully open).
 * @param cutoutX horizontal position of the cutout's centre; null centres it on the bar.
 */
@Immutable
public data class GooeyCutoutShape(
    val fabRadius: Dp,
    val progress: Float,
    val margin: Dp = 8.dp,
    val cutoutX: Dp? = null,
    val smoothness: Dp = 24.dp,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val bounds = size.toRect()
        val notch = with(density) { (fabRadius + margin).toPx() } * progress.coerceIn(0f, 1f)
        if (notch < 0.5f) return Outline.Rectangle(bounds)

        val x = cutoutX?.let { with(density) { it.toPx() } } ?: (size.width / 2f)
        val field = GooeyField(
            blobs = listOf(Blob.RoundRect(bounds, 0f, Color.Black)),
            cutouts = listOf(Blob.Circle(Offset(x, 0f), notch, Color.Black)),
            smoothness = with(density) { smoothness.toPx() },
        )
        val cell = max(with(density) { 1.dp.toPx() }, 1.5f)
        val loops = sharedTracer.trace(field, bounds.inflate(cell * 2f), cell)
        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            loops.forEach { addSmoothLoop(it) }
        }
        return Outline.Generic(path)
    }

    private companion object {
        // Outlines are created on the UI thread, so a single tracer can be reused.
        val sharedTracer = ContourTracer()
    }
}
