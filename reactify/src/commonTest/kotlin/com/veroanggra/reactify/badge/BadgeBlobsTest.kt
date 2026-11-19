package com.veroanggra.reactify.badge

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val anchor = Offset(100f, 10f)
private val rest = Offset(5f, -5f)

class BadgeBlobsTest {
    @Test
    fun zeroGrowDrawsNothing() {
        assertTrue(badgeBlobs(anchor, rest, 9f, 0f, Color.White).isEmpty())
    }

    @Test
    fun fullyGrownSitsAtRestOffsetWithNoStem() {
        val blobs = badgeBlobs(anchor, rest, badgeRadius = 9f, grow = 1f, color = Color.White)
        assertEquals(1, blobs.size) // stem is sin(PI) == 0 at grow = 1
        val badge = blobs.single() as Blob.Circle
        assertEquals(anchor + rest, badge.center)
        assertEquals(9f, badge.radius)
    }

    @Test
    fun midPopShowsAStem() {
        val blobs = badgeBlobs(anchor, rest, badgeRadius = 9f, grow = 0.5f, color = Color.White)
        assertEquals(2, blobs.size)
        val stem = blobs[0] as Blob.Capsule
        assertEquals(anchor, stem.start)
    }

    @Test
    fun growIsClampedAndMonotonicInRadius() {
        val quarter = (badgeBlobs(anchor, rest, 9f, 0.25f, Color.White).last() as Blob.Circle).radius
        val half = (badgeBlobs(anchor, rest, 9f, 0.5f, Color.White).last() as Blob.Circle).radius
        assertTrue(half > quarter)
        val overshoot = (badgeBlobs(anchor, rest, 9f, 1.5f, Color.White).last() as Blob.Circle).radius
        assertEquals(9f, overshoot)
    }
}
