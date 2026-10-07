package com.veroanggra.reactify.list

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
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
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.ContourTracer
import com.veroanggra.reactify.gooey.GooeyField
import com.veroanggra.reactify.gooey.addSmoothLoop
import kotlin.math.max

/**
 * Rounded rectangle with a smooth concave notch centred on its bottom edge, shaped by the same
 * distance field as the gooey renderers. Animate [notchRadius] from 0 to open the notch, and pass
 * the same value to [GooeyTarget] so transfer overlays follow the notch.
 */
@Immutable
public data class GooeyNotchShape(
    val cornerRadius: Dp,
    val notchRadius: Dp,
    val smoothness: Dp = GooeyTransferDefaults.Smoothness,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val corner = with(density) { cornerRadius.toPx() }
        val notch = with(density) { notchRadius.toPx() }
        val bounds = size.toRect()
        if (notch < 0.5f) return Outline.Rounded(RoundRect(bounds, CornerRadius(corner)))

        val field = GooeyField(
            blobs = listOf(Blob.RoundRect(bounds, corner, Color.Black)),
            cutouts = listOf(Blob.Circle(Offset(size.width / 2f, size.height), notch, Color.Black)),
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