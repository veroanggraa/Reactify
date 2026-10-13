package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.abs
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * iOS counterpart to the Android `RendererParityTest`: renders the same [ParityScenes] with
 * [SkslGooeyRenderer] and checks them against the same CPU ground truth ([GooeyField]) used to
 * validate AGSL and Path on Android. [SkslGooeyRenderer] only touches Skia, so this renders
 * straight into an off-screen [ImageBitmap] - no UI tree or window is needed, just the simulator
 * process the Kotlin/Native test binary runs in.
 */
class SkslRendererParityTest {

    private val width = 400
    private val height = 600

    // Same budget as AGSL on Android: exact up to anti-aliasing.
    private val tolerancePx = 1f

    @Test fun bridge() = check("bridge", ParityScenes.bridgeScene())
    @Test fun breaking() = check("breaking", ParityScenes.breakingScene())
    @Test fun dragNeck() = check("dragNeck", ParityScenes.dragScene())
    @Test fun notch() = check("notch", ParityScenes.notchScene())
    @Test fun merge() = check("merge", ParityScenes.mergeScene())

    private fun check(name: String, scene: GooeyScene) {
        val renderer = GooeyRenderers.sksl()
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(width.toFloat(), height.toFloat()),
        ) {
            drawRect(Color.Black)
            with(renderer) { draw(scene) }
        }

        val result = compare(bitmap, GooeyField(scene.blobs, scene.cutouts, scene.smoothness))
        assertTrue(
            result.maxErrorPx <= tolerancePx,
            "$name: pixel ${result.maxErrorPx}px from the contour misclassified (tolerance ${tolerancePx}px), wrong=${result.wrongPixels}",
        )
        assertTrue(result.iou >= 0.99f, "$name: IoU ${result.iou} too low")
    }

    private class Result(val maxErrorPx: Float, val wrongPixels: Int, val iou: Float)

    private fun compare(bitmap: ImageBitmap, field: GooeyField): Result {
        val pixels = bitmap.toPixelMap()
        var maxError = 0f
        var wrong = 0
        var intersection = 0
        var union = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[x, y]
                val luminance = max(pixel.red, pixel.green)
                val rendered = luminance >= 0.5f
                val d = field.distance(x + 0.5f, y + 0.5f)
                val expected = d < 0f
                if (rendered && expected) intersection++
                if (rendered || expected) union++
                if (rendered != expected) {
                    wrong++
                    maxError = max(maxError, abs(d))
                }
            }
        }
        return Result(maxError, wrong, if (union == 0) 1f else intersection / union.toFloat())
    }
}
