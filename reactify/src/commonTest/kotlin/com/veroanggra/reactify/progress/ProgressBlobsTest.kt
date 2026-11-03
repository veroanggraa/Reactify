package com.veroanggra.reactify.progress

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProgressBlobsTest {

    private val bounds = Rect(0f, 0f, 300f, 20f)
    private val r = trackRadius(bounds.height)
    private val headR = r * HEAD_SCALE

    private fun linear(fill: Float, head: Float) = linearProgressBlobs(bounds, fill, head, Color.Red)
    private fun fill(blobs: List<Blob>) = blobs.filterIsInstance<Blob.Capsule>().maxByOrNull { it.radius }
    private fun head(blobs: List<Blob>) = blobs.filterIsInstance<Blob.Circle>().single()

    private fun Blob.extent(): Rect = when (this) {
        is Blob.RoundRect -> rect
        is Blob.Circle -> Rect(center, radius)
        is Blob.Capsule -> Rect(
            minOf(start.x, end.x) - radius,
            minOf(start.y, end.y) - radius,
            maxOf(start.x, end.x) + radius,
            maxOf(start.y, end.y) + radius,
        )
        is Blob.Polygon -> error("not produced by linearProgressBlobs")
    }

    private fun assertInside(blobs: List<Blob>, area: Rect) {
        blobs.forEach { blob ->
            val e = blob.extent()
            assertTrue(
                e.left >= area.left - 0.01f && e.top >= area.top - 0.01f &&
                    e.right <= area.right + 0.01f && e.bottom <= area.bottom + 0.01f,
                "$blob leaves $area",
            )
        }
    }

    @Test
    fun atZeroOnlyTheHeadIsDrawn() {
        val blobs = linear(0f, 0f)
        assertEquals(1, blobs.size)
        val head = head(blobs)
        assertEquals(Offset(headR, bounds.center.y), head.center)
        assertEquals(headR, head.radius)
    }

    @Test
    fun atOneTheFillReachesTheEndOfTheTrack() {
        val blobs = linear(1f, 1f)
        val fill = fill(blobs)!!
        assertEquals(headR, fill.start.x)
        assertEquals(bounds.right - headR, fill.end.x)
        assertEquals(linearTrackRect(bounds).right, fill.end.x + fill.radius)
        assertEquals(bounds.right - headR, head(blobs).center.x)
        assertTrue(blobs.filterIsInstance<Blob.Capsule>().size == 1, "no neck once head and fill meet")
    }

    @Test
    fun headAheadOfFillStretchesANeckThinnestInTheMiddle() {
        val blobs = linear(0.3f, 0.7f)
        val fillX = fill(blobs)!!.end.x
        val headX = head(blobs).center.x
        assertTrue(headX > fillX)
        val neck = blobs.filterIsInstance<Blob.Capsule>().filter { it.start.x >= fillX - 0.01f }
        assertTrue(neck.size >= 2, "neck segments expected between fill and head")
        val midX = (fillX + headX) / 2f
        val middle = neck.minByOrNull { abs((it.start.x + it.end.x) / 2f - midX) }!!.radius
        val end = neck.maxByOrNull { abs((it.start.x + it.end.x) / 2f - midX) }!!.radius
        assertTrue(middle < end, "neck should thin in the middle: $middle vs $end")
        assertInside(blobs, bounds)
    }

    @Test
    fun headBehindFillDrawsNoNeck() {
        val blobs = linear(0.7f, 0.3f)
        assertEquals(1, blobs.filterIsInstance<Blob.Capsule>().size)
        assertTrue(head(blobs).center.x < fill(blobs)!!.end.x)
    }

    @Test
    fun indeterminateLinearStaysInsideTheBounds() {
        listOf(0f, 0.05f, 0.5f, 0.9f, 0.99f).forEach { t ->
            assertInside(indeterminateLinearBlobs(bounds, t, Color.Red), bounds)
        }
        assertEquals(2, indeterminateLinearBlobs(bounds, 0.5f, Color.Red).size)
    }

    @Test
    fun indeterminateLinearTrailerLagsThenCatchesUp() {
        fun gap(t: Float): Float {
            val blobs = indeterminateLinearBlobs(bounds, t, Color.Red)
            val lead = blobs.filterIsInstance<Blob.Capsule>().single()
            val trail = blobs.filterIsInstance<Blob.Circle>().single()
            return lead.start.x - trail.center.x
        }
        assertTrue(gap(0.5f) > r * 4f, "trailer should be well behind mid-cycle")
        assertTrue(gap(0.95f) < gap(0.5f), "trailer should close in near the end")
    }

    @Test
    fun circularArcHugsTheRing() {
        val center = Offset(100f, 100f)
        val radius = 40f
        val blobs = circularProgressBlobs(center, radius, 8f, 0.4f, 0.75f, Color.Red)
        val capsules = blobs.filterIsInstance<Blob.Capsule>()
        assertTrue(capsules.isNotEmpty())
        capsules.flatMap { listOf(it.start, it.end) }.forEach { p ->
            assertTrue(abs((p - center).getDistance() - radius) <= 0.5f, "$p is off the ring")
        }
        val head = blobs.filterIsInstance<Blob.Circle>().single()
        assertTrue(abs((head.center - center).getDistance() - radius) <= 0.5f)
        // Neck between 40% and 75% is thinner than the arc itself.
        assertTrue(capsules.minOf { it.radius } < capsules.maxOf { it.radius })
    }

    @Test
    fun circularFullRingFitsUnderTheShaderBlobCap() {
        val blobs = circularProgressBlobs(Offset(50f, 50f), 40f, 8f, 1f, 1f, Color.Red)
        assertTrue(blobs.size <= 24, "${blobs.size} blobs")
        val neck = circularProgressBlobs(Offset(50f, 50f), 40f, 8f, 0f, 1f, Color.Red)
        assertTrue(neck.size <= 24, "${neck.size} blobs")
    }

    @Test
    fun indeterminateCircularLoopsSeamlessly() {
        val center = Offset(50f, 50f)
        val a = indeterminateCircularBlobs(center, 40f, 8f, 0f, Color.Red).map { (it as Blob.Circle).center }
        val b = indeterminateCircularBlobs(center, 40f, 8f, 1f, Color.Red).map { (it as Blob.Circle).center }
        a.zip(b).forEach { (p, q) -> assertTrue((p - q).getDistance() < 0.01f) }
        indeterminateCircularBlobs(center, 40f, 8f, 0.37f, Color.Red).forEach {
            assertTrue(abs(((it as Blob.Circle).center - center).getDistance() - 40f) <= 0.5f)
        }
    }
}
