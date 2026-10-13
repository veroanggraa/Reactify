package com.veroanggra.reactify.gooey

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * Renders fixed scenes with each renderer and compares them with a reference rasterised on the
 * CPU from [GooeyField], i.e. the exact maths the shader is meant to implement. A pixel counts as
 * wrong when it is classified inside/outside differently from the reference; the metric is how
 * far (in px) the worst wrong pixel lies from the true contour.
 */
@RunWith(AndroidJUnit4::class)
class RendererParityTest {

    @get:Rule
    val rule = createComposeRule()

    private val width = 400
    private val height = 600

    // AGSL should be exact up to anti-aliasing; Path is limited by its contour grid.
    private val agslTolerancePx = 1f
    private val pathTolerancePx = 3f

    @Test fun bridge_agsl() = checkAgsl("bridge", ParityScenes.bridgeScene())
    @Test fun bridge_path() = checkPath("bridge", ParityScenes.bridgeScene())
    @Test fun breaking_agsl() = checkAgsl("breaking", ParityScenes.breakingScene())
    @Test fun breaking_path() = checkPath("breaking", ParityScenes.breakingScene())
    @Test fun dragNeck_agsl() = checkAgsl("dragNeck", ParityScenes.dragScene())
    @Test fun dragNeck_path() = checkPath("dragNeck", ParityScenes.dragScene())
    @Test fun notch_agsl() = checkAgsl("notch", ParityScenes.notchScene())
    @Test fun notch_path() = checkPath("notch", ParityScenes.notchScene())
    @Test fun merge_agsl() = checkAgsl("merge", ParityScenes.mergeScene())
    @Test fun merge_path() = checkPath("merge", ParityScenes.mergeScene())

    private fun checkAgsl(name: String, scene: GooeyScene) {
        assumeTrue("AGSL needs API 33+", Build.VERSION.SDK_INT >= 33)
        check("${name}_agsl", scene, GooeyRenderers.agsl(), agslTolerancePx)
    }

    private fun checkPath(name: String, scene: GooeyScene) =
        check("${name}_path", scene, GooeyRenderers.path(), pathTolerancePx)

    private fun check(name: String, scene: GooeyScene, renderer: GooeyRenderer, tolerancePx: Float) {
        rule.setContent {
            val density = LocalDensity.current
            Box(
                Modifier
                    .testTag(TAG)
                    .size(with(density) { width.toDp() }, with(density) { height.toDp() })
                    .background(Color.Black),
            ) {
                Canvas(Modifier.size(with(density) { width.toDp() }, with(density) { height.toDp() })) {
                    with(renderer) { draw(scene) }
                }
            }
        }
        val bitmap = rule.onNodeWithTag(TAG).captureToImage().asAndroidBitmap()
        val result = compare(bitmap, GooeyField(scene.blobs, scene.cutouts, scene.smoothness))
        save(name, bitmap)
        Log.i(LOG_TAG, "$name: maxError=${"%.2f".format(result.maxErrorPx)}px wrong=${result.wrongPixels} iou=${"%.4f".format(result.iou)}")
        assertTrue(
            "$name: pixel ${"%.2f".format(result.maxErrorPx)}px from the contour misclassified (tolerance ${tolerancePx}px)",
            result.maxErrorPx <= tolerancePx,
        )
        assertTrue("$name: IoU ${result.iou} too low", result.iou >= 0.99)
    }

    private class Result(val maxErrorPx: Float, val wrongPixels: Int, val iou: Float)

    private fun compare(bitmap: Bitmap, field: GooeyField): Result {
        var maxError = 0f
        var wrong = 0
        var intersection = 0
        var union = 0
        for (y in 0 until minOf(height, bitmap.height)) {
            for (x in 0 until minOf(width, bitmap.width)) {
                val pixel = bitmap.getPixel(x, y)
                val luminance = max((pixel shr 16) and 0xFF, (pixel shr 8) and 0xFF)
                val rendered = luminance >= 128
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

    private fun save(name: String, bitmap: Bitmap) {
        val dir = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("parity") ?: return
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private companion object {
        const val TAG = "parity"
        const val LOG_TAG = "RendererParity"
    }
}
