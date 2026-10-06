package com.veroanggra.reactify.gooey

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
    private val smoothness = 48f

    // AGSL should be exact up to anti-aliasing; Path is limited by its contour grid.
    private val agslTolerancePx = 1f
    private val pathTolerancePx = 3f

    @Test fun bridge_agsl() = checkAgsl("bridge", bridgeScene())
    @Test fun bridge_path() = checkPath("bridge", bridgeScene())
    @Test fun breaking_agsl() = checkAgsl("breaking", breakingScene())
    @Test fun breaking_path() = checkPath("breaking", breakingScene())
    @Test fun dragNeck_agsl() = checkAgsl("dragNeck", dragScene())
    @Test fun dragNeck_path() = checkPath("dragNeck", dragScene())
    @Test fun notch_agsl() = checkAgsl("notch", notchScene())
    @Test fun notch_path() = checkPath("notch", notchScene())
    @Test fun merge_agsl() = checkAgsl("merge", mergeScene())
    @Test fun merge_path() = checkPath("merge", mergeScene())

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

    private val source = Rect(20f, 460f, 380f, 540f)
    private val target = Rect(20f, 40f, 380f, 240f)
    private val tokenStart = Rect(40f, 476f, 88f, 524f)
    private val slot = Rect(176f, 116f, 224f, 164f)

    private fun transferScene(progress: Float) = GooeyScene(
        blobs = transferBlobs(
            progress = progress,
            color = Color.White,
            source = source,
            sourceCornerRadius = 32f,
            target = target,
            targetCornerRadius = 48f,
            tokenStart = tokenStart,
            token = tokenRectAt(progress, tokenStart, slot),
            maxTintAlpha = 1f,
        ).map { it.opaque() },
        smoothness = smoothness,
    )

    private fun bridgeScene() = transferScene(0.4f)

    private fun breakingScene() = transferScene(0.82f)

    private fun dragScene(): GooeyScene {
        val anchor = tokenStart.center
        val finger = Offset(300f, 300f)
        return GooeyScene(
            blobs = dragBlobs(
                color = Color.White,
                source = source,
                sourceCornerRadius = 32f,
                target = null,
                targetCornerRadius = 48f,
                anchor = anchor,
                token = Rect(finger, 24f),
                tokenCenter = finger,
                tokenRadius = 24f,
                neckScale = 1f,
                maxTintAlpha = 1f,
            ).map { it.opaque() },
            smoothness = smoothness,
        )
    }

    private fun notchScene() = GooeyScene(
        blobs = listOf(Blob.RoundRect(target, 48f, Color.White)),
        cutouts = listOf(Blob.Circle(Offset(target.center.x, target.bottom), 60f, Color.White)),
        smoothness = smoothness,
    )

    private fun mergeScene() = GooeyScene(
        blobs = listOf(
            Blob.Circle(Offset(140f, 360f), 60f, Color.White),
            Blob.Circle(Offset(270f, 360f), 50f, Color.White),
        ),
        smoothness = smoothness,
    )

    private fun Blob.opaque(): Blob = when (this) {
        is Blob.RoundRect -> copy(color = Color.White)
        is Blob.Circle -> copy(color = Color.White)
        is Blob.Capsule -> copy(color = Color.White)
    }

    private companion object {
        const val TAG = "parity"
        const val LOG_TAG = "RendererParity"
    }
}
