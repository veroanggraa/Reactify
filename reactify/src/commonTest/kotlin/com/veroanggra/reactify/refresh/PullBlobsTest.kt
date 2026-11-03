package com.veroanggra.reactify.refresh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PullBlobsTest {

    private val threshold = 100f
    private val width = 400f
    private val dropRadius = 14f

    private fun blobsAt(pull: Float, neckScale: Float = 1f, dropScale: Float = 1f) =
        pullBlobs(pull, threshold, width, dropRadius, neckScale, dropScale, Color.Red)

    private fun drop(pull: Float, dropScale: Float = 1f) =
        blobsAt(pull, dropScale = dropScale).filterIsInstance<Blob.Circle>().singleOrNull()

    private fun neck(pull: Float, neckScale: Float = 1f) =
        blobsAt(pull, neckScale = neckScale).filterIsInstance<Blob.Capsule>()

    @Test
    fun noPullDrawsNothing() {
        assertTrue(blobsAt(0f).isEmpty())
        assertTrue(blobsAt(-10f).isEmpty())
    }

    @Test
    fun dropDescendsWithPullAndHangsFromANeck() {
        val early = drop(0.3f * threshold)!!
        val later = drop(0.6f * threshold)!!
        assertTrue(later.center.y > early.center.y)
        assertEquals(width / 2f, later.center.x)

        val caps = neck(0.6f * threshold)
        assertTrue(caps.isNotEmpty())
        val neckMidY = (caps.first().start.y + caps.last().end.y) / 2f
        val middle = caps.minByOrNull { abs((it.start.y + it.end.y) / 2f - neckMidY) }!!.radius
        val end = caps.maxByOrNull { abs((it.start.y + it.end.y) / 2f - neckMidY) }!!.radius
        assertTrue(middle < end)
    }

    @Test
    fun snappedNeckHasNoCapsules() {
        assertTrue(neck(0.6f * threshold, neckScale = 0f).isEmpty())
        assertTrue(drop(0.6f * threshold) != null)
    }

    @Test
    fun overPullSaturates() {
        val far = drop(3f * threshold)!!
        assertTrue(far.center.y <= 1.15f * threshold)
        assertTrue(far.center.y > threshold)
    }

    @Test
    fun zeroDropScaleRemovesTheDrop() {
        assertEquals(null, drop(0.6f * threshold, dropScale = 0f))
        assertTrue(blobsAt(0.6f * threshold, dropScale = 0f).any { it is Blob.RoundRect })
    }

    @Test
    fun satelliteOrbitsTheDrop() {
        val center = Offset(200f, threshold)
        val at0 = refreshingBlobs(center, dropRadius, 0f, Color.Red).filterIsInstance<Blob.Circle>()
        val atHalf = refreshingBlobs(center, dropRadius, 0.5f, Color.Red).filterIsInstance<Blob.Circle>()
        assertEquals(2, at0.size)
        val satellite0 = at0.minByOrNull { it.radius }!!
        val satelliteHalf = atHalf.minByOrNull { it.radius }!!
        assertTrue(satellite0.center.x > center.x)
        assertTrue(satelliteHalf.center.x < center.x)
        assertTrue((satellite0.center - center).getDistance() <= 2f * dropRadius)
        assertTrue((satelliteHalf.center - center).getDistance() <= 2f * dropRadius)
    }
}
