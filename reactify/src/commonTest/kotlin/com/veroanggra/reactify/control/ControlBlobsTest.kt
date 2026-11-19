package com.veroanggra.reactify.control

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val track = Rect(0f, 0f, 100f, 20f)

class ControlBlobsTest {
    @Test
    fun trackIsOneCapsuleSpanningTheInset() {
        val blobs = trackAndThumb(0f, track, thumbRadius = 14f, color = Color.White)
        val capsule = blobs[0] as Blob.Capsule
        assertEquals(10f, capsule.start.x) // track.height / 2
        assertEquals(90f, capsule.end.x)
        assertEquals(10f, capsule.radius)
    }

    @Test
    fun thumbSitsAtTrackStartAtZeroProgress() {
        val thumb = trackAndThumb(0f, track, thumbRadius = 14f, color = Color.White)[1] as Blob.Circle
        assertEquals(Offset(10f, 10f), thumb.center)
        assertEquals(14f, thumb.radius)
    }

    @Test
    fun thumbSitsAtTrackEndAtFullProgress() {
        val thumb = trackAndThumb(1f, track, thumbRadius = 14f, color = Color.White)[1] as Blob.Circle
        assertEquals(Offset(90f, 10f), thumb.center)
    }

    @Test
    fun progressIsClampedToTrackBounds() {
        val below = trackAndThumb(-0.5f, track, thumbRadius = 14f, color = Color.White)[1] as Blob.Circle
        val above = trackAndThumb(1.5f, track, thumbRadius = 14f, color = Color.White)[1] as Blob.Circle
        assertEquals(10f, below.center.x)
        assertEquals(90f, above.center.x)
    }

    @Test
    fun noDragDrawsOnlyTrackAndThumb() {
        val blobs = sliderBlobs(0.5f, track, thumbRadius = 14f, bubbleRadius = 18f, bubbleRise = 40f, dragged = 0f, color = Color.White)
        assertEquals(2, blobs.size)
    }

    @Test
    fun dragAddsNeckSegmentsAndABubbleAboveTheThumb() {
        val blobs = sliderBlobs(0.5f, track, thumbRadius = 14f, bubbleRadius = 18f, bubbleRise = 40f, dragged = 1f, color = Color.White)
        assertTrue(blobs.size > 2)
        val bubble = blobs.last() as Blob.Circle
        assertTrue(bubble.center.y < track.center.y) // risen above the track
        val capsules = blobs.filterIsInstance<Blob.Capsule>().drop(1) // skip the track capsule
        assertTrue(capsules.isNotEmpty())
    }

    @Test
    fun bubbleGrowsWithDragFraction() {
        val half = (sliderBlobs(0.5f, track, 14f, 18f, 40f, dragged = 0.5f, color = Color.White).last() as Blob.Circle).radius
        val full = (sliderBlobs(0.5f, track, 14f, 18f, 40f, dragged = 1f, color = Color.White).last() as Blob.Circle).radius
        assertTrue(full > half)
    }
}
