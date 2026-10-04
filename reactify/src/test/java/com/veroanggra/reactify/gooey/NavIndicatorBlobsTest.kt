package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavIndicatorBlobsTest {

    private val from = Rect(0f, 0f, 60f, 56f)
    private val to = Rect(200f, 0f, 260f, 56f)
    private val cornerRadius = 28f
    private val midX = (from.center.x + to.center.x) / 2f

    private fun blobsAt(progress: Float) = navIndicatorBlobs(progress, from, to, cornerRadius, Color.Red)

    private fun pills(progress: Float) = blobsAt(progress).filterIsInstance<Blob.RoundRect>()
    private fun bridge(progress: Float) = blobsAt(progress).filterIsInstance<Blob.Capsule>()

    private fun xSpan(capsules: List<Blob.Capsule>): ClosedFloatingPointRange<Float> {
        val xs = capsules.flatMap { listOf(it.start.x, it.end.x) }
        return xs.min()..xs.max()
    }

    @Test
    fun onlyOldPillAtStart() {
        val blobs = blobsAt(0f)
        assertEquals(1, blobs.size)
        assertEquals(from, (blobs.single() as Blob.RoundRect).rect)
    }

    @Test
    fun onlyNewPillAtEnd() {
        val blobs = blobsAt(1f)
        assertEquals(1, blobs.size)
        assertEquals(to, (blobs.single() as Blob.RoundRect).rect)
    }

    @Test
    fun bridgeGrowsAsOneNeckingThreadMidTransition() {
        val caps = bridge(0.4f)
        assertTrue(caps.isNotEmpty())
        val span = xSpan(caps)
        assertTrue(span.start < to.left)
        assertTrue(span.endInclusive > from.right)

        // Thinnest near the middle of the thread, thickest near the pills it's anchored to.
        val middleRadius = caps.minByOrNull { kotlin.math.abs((it.start.x + it.end.x) / 2f - midX) }!!.radius
        val endRadius = caps.maxByOrNull { kotlin.math.abs((it.start.x + it.end.x) / 2f - midX) }!!.radius
        assertTrue(middleRadius < endRadius)

        assertEquals(2, pills(0.4f).size)
    }

    @Test
    fun bridgeSplitsIntoTwoTaperedTipsThenDisappearsBeforeTheEnd() {
        val caps = bridge(0.8f)
        assertTrue(caps.isNotEmpty())
        val (fromSide, toSide) = caps.partition { (it.start.x + it.end.x) / 2f < midX }
        assertTrue(fromSide.isNotEmpty())
        assertTrue(toSide.isNotEmpty())
        assertTrue(xSpan(fromSide).endInclusive < xSpan(toSide).start)

        assertTrue(bridge(0.97f).isEmpty())
    }

    @Test
    fun oldPillShrinksWhileNewPillGrows() {
        val earlyOld = pills(0.4f).first { it.rect.left < to.left }
        val lateOld = pills(0.9f).firstOrNull { it.rect.left < to.left }
        assertTrue(lateOld == null || lateOld.rect.width < earlyOld.rect.width)

        val earlyNew = pills(0.3f).first { it.rect.left >= from.right }
        val laterNew = pills(0.6f).first { it.rect.left >= from.right }
        assertTrue(laterNew.rect.width > earlyNew.rect.width)
    }
}
