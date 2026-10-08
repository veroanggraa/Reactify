package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GooeyContourTest {

    private val area = Rect(0f, 0f, 400f, 400f)
    private fun circle(x: Float, y: Float, r: Float) = Blob.Circle(Offset(x, y), r, Color.Red)

    @Test
    fun fieldMatchesExactDistanceForLoneCircle() {
        val field = GooeyField(listOf(circle(100f, 100f, 50f)), emptyList(), smoothness = 20f)
        assertEquals(-50f, field.distance(100f, 100f), 0.001f)
        assertEquals(10f, field.distance(160f, 100f), 0.001f)
    }

    @Test
    fun smoothMinPullsFieldBelowPlainMinBetweenNearbyBlobs() {
        val blobs = listOf(circle(100f, 200f, 40f), circle(200f, 200f, 40f))
        val field = GooeyField(blobs, emptyList(), smoothness = 40f)
        assertTrue(field.distance(150f, 200f) < 10f)
    }

    @Test
    fun loneCircleTracesOneAccurateLoop() {
        val field = GooeyField(listOf(circle(200f, 200f, 80f)), emptyList(), smoothness = 20f)
        val loops = ContourTracer().trace(field, area, cell = 5f)
        assertEquals(1, loops.size)
        val loop = loops.single()
        for (i in loop.indices step 2) {
            assertEquals(80f, hypot(loop[i] - 200f, loop[i + 1] - 200f), 1f)
        }
    }

    @Test
    fun distantBlobsStaySeparateAndNearbyBlobsMerge() {
        val tracer = ContourTracer()
        val far = GooeyField(listOf(circle(80f, 200f, 40f), circle(320f, 200f, 40f)), emptyList(), 20f)
        assertEquals(2, tracer.trace(far, area, 5f).size)

        // Smooth-min lowers the field by at most k/4, so a 20px gap needs k > 40 to bridge.
        val near = GooeyField(listOf(circle(150f, 200f, 40f), circle(250f, 200f, 40f)), emptyList(), 60f)
        assertEquals(1, tracer.trace(near, area, 5f).size)
    }

    @Test
    fun cutoutInsideBlobProducesHole() {
        val card = Blob.RoundRect(Rect(50f, 50f, 350f, 350f), 24f, Color.Red)
        val field = GooeyField(listOf(card), listOf(circle(200f, 200f, 50f)), smoothness = 10f)
        assertEquals(2, ContourTracer().trace(field, area, 5f).size)
    }

    @Test
    fun blobCrossingAreaEdgeIsStillClosed() {
        val field = GooeyField(listOf(circle(0f, 200f, 80f)), emptyList(), smoothness = 20f)
        val loops = ContourTracer().trace(field, area, 5f)
        assertEquals(1, loops.size)
    }
}
