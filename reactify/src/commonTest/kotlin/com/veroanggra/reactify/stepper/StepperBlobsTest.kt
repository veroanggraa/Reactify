package com.veroanggra.reactify.stepper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StepperBlobsTest {

    private val dotRadius = 14f
    private val steps = (0 until 4).map { i -> Rect(Offset(40f + i * 100f, 50f), dotRadius) }
    private val linkRadius = dotRadius * LINK_RADIUS_FACTOR

    private fun blobsAt(progress: Float, from: Int, to: Int) =
        stepperBlobs(progress, from, to, steps, dotRadius, Color.Red)

    private fun dotAt(blobs: List<Blob>, index: Int) =
        blobs.filterIsInstance<Blob.Circle>().firstOrNull { it.center == steps[index].center }

    private fun solidLink(blobs: List<Blob>, index: Int) =
        blobs.filterIsInstance<Blob.Capsule>().firstOrNull {
            it.start == steps[index].center && it.end == steps[index + 1].center && it.radius == linkRadius
        }

    private fun threadBetween(blobs: List<Blob>, index: Int) =
        blobs.filterIsInstance<Blob.Capsule>().filter {
            it.start.x >= steps[index].center.x && it.end.x <= steps[index + 1].center.x && solidLink(listOf(it), index) == null
        }

    @Test
    fun restingChainIsDotsJoinedBySolidLinks() {
        val blobs = blobsAt(1f, 2, 2)
        assertEquals(5, blobs.size)
        for (i in 0..2) assertEquals(dotRadius, dotAt(blobs, i)!!.radius)
        for (i in 0..1) assertTrue(solidLink(blobs, i) != null)
        assertNull(dotAt(blobs, 3))
        assertTrue(blobs.none { it is Blob.Capsule && it.end.x > steps[2].center.x })
    }

    @Test
    fun forwardThreadGrowsBeforeTheDestinationDot() {
        val blobs = blobsAt(0.3f, 1, 2)
        val thread = threadBetween(blobs, 1)
        assertTrue(thread.isNotEmpty())
        val tipX = thread.maxOf { it.end.x }
        assertTrue(tipX > steps[1].center.x)
        assertTrue(tipX < steps[2].center.x)

        val midX = (steps[1].center.x + tipX) / 2f
        val middle = thread.minByOrNull { abs((it.start.x + it.end.x) / 2f - midX) }!!.radius
        val end = thread.maxByOrNull { abs((it.start.x + it.end.x) / 2f - midX) }!!.radius
        assertTrue(middle < end)

        assertNull(dotAt(blobs, 2))
        assertNull(solidLink(blobs, 1))
    }

    @Test
    fun destinationDotGrowsInNearTheEnd() {
        val dot = dotAt(blobsAt(0.9f, 1, 2), 2)
        assertTrue(dot != null)
        assertTrue(dot.radius > 0f)
        assertTrue(dot.radius < dotRadius)
        assertEquals(dotRadius, dotAt(blobsAt(1f, 1, 2), 2)!!.radius)
    }

    @Test
    fun backwardIsForwardMirrored() {
        for (p in listOf(0.1f, 0.3f, 0.5f, 0.7f, 0.9f)) {
            assertEquals(blobsAt(1f - p, 1, 2), blobsAt(p, 2, 1))
        }
    }

    @Test
    fun jumpGrowsLinksInOrder() {
        val blobs = blobsAt(0.5f, 0, 3)
        assertTrue(solidLink(blobs, 0) != null)
        assertEquals(dotRadius, dotAt(blobs, 1)!!.radius)
        val thread = threadBetween(blobs, 1)
        assertTrue(thread.isNotEmpty())
        assertTrue(thread.maxOf { it.end.x } < steps[2].center.x)
        assertNull(dotAt(blobs, 2))
        assertNull(dotAt(blobs, 3))
        assertTrue(blobs.none { it is Blob.Capsule && it.end.x > steps[2].center.x })
    }
}
