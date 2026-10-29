package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Fallback for devices without AGSL. Traces the same distance field as [AgslGooeyRenderer] on
 * the CPU and fills the smoothed contour. Blobs only merge with blobs of the same colour, so
 * concurrent transfers of different colours overlap instead of blending.
 *
 * @param cellSize grid spacing of the contour; smaller is smoother but costs more CPU.
 */
internal class PathGooeyRenderer(private val cellSize: Dp = 2.5.dp) : GooeyRenderer {
    private val tracer = ContourTracer()

    // Re-tracing is the expensive part; a scene that is structurally identical to last frame's
    // (e.g. a settled indicator redrawn for an unrelated recomposition) can just replay these.
    private var lastScene: GooeyScene? = null
    private var lastSize: Size = Size.Unspecified
    private val cachedPaths = LinkedHashMap<Color, Path>()

    override fun DrawScope.draw(scene: GooeyScene) {
        if (scene.isEmpty) return
        if (scene == lastScene && size == lastSize) {
            cachedPaths.forEach { (color, path) -> drawPath(path, color) }
            return
        }

        val cell = max(cellSize.toPx(), 2f)
        val canvas = Rect(Offset.Zero, size)
        cachedPaths.clear()

        scene.blobs.groupBy { it.color }.forEach { (color, group) ->
            // Identical shapes would swell each other under smooth-min; keep one copy.
            val blobs = group.distinctBy { if (it is Blob.RoundRect) it.rect to it.cornerRadius else it }
            val area = blobs.boundsOf().inflate(scene.smoothness + cell).intersect(canvas)
            if (area.isEmpty) return@forEach
            val field = GooeyField(blobs, scene.cutouts, scene.smoothness)
            val loops = tracer.trace(field, area, cell)
            if (loops.isEmpty()) return@forEach
            val path = Path().apply { fillType = PathFillType.EvenOdd }
            loops.forEach { path.addSmoothLoop(it) }
            cachedPaths[color] = path
            drawPath(path, color)
        }
        lastScene = scene
        lastSize = size
    }
}

/** Quadratic curves through edge midpoints, using the traced vertices as control points. */
internal fun Path.addSmoothLoop(points: FloatArray) {
    val n = points.size / 2
    fun x(i: Int) = points[(i % n) * 2]
    fun y(i: Int) = points[(i % n) * 2 + 1]
    moveTo((x(n - 1) + x(0)) / 2f, (y(n - 1) + y(0)) / 2f)
    for (i in 0 until n) {
        quadraticTo(x(i), y(i), (x(i) + x(i + 1)) / 2f, (y(i) + y(i + 1)) / 2f)
    }
    close()
}

internal fun List<Blob>.boundsOf(): Rect {
    var left = Float.POSITIVE_INFINITY
    var top = Float.POSITIVE_INFINITY
    var right = Float.NEGATIVE_INFINITY
    var bottom = Float.NEGATIVE_INFINITY
    forEach { blob ->
        val r = blob.bounds()
        left = minOf(left, r.left)
        top = minOf(top, r.top)
        right = maxOf(right, r.right)
        bottom = maxOf(bottom, r.bottom)
    }
    return if (isEmpty()) Rect.Zero else Rect(left, top, right, bottom)
}
