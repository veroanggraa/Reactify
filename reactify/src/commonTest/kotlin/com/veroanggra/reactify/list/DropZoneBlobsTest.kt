package com.veroanggra.reactify.list

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val zone = Rect(0f, 0f, 100f, 100f)

class DropZoneBlobsTest {
    @Test
    fun emptyZoneDrawsNothing() {
        assertTrue(dropZoneBlobs(Rect.Zero, 16f, 0f, null, 56f, Color.White).isEmpty())
    }

    @Test
    fun noDragDrawsOnlyTheZone() {
        val blobs = dropZoneBlobs(zone, 16f, hoverGrow = 0f, dragPoint = null, pullRadius = 56f, color = Color.White)
        assertEquals(1, blobs.size)
        assertEquals(zone, (blobs.single() as Blob.RoundRect).rect)
    }

    @Test
    fun farDragDrawsOnlyTheZone() {
        val blobs = dropZoneBlobs(zone, 16f, 0f, dragPoint = Offset(500f, 500f), pullRadius = 56f, color = Color.White)
        assertEquals(1, blobs.size)
    }

    @Test
    fun nearDragAddsATendrilFromTheNearestEdgePoint() {
        val drag = Offset(150f, 50f) // 50px right of the zone's right edge, same height
        val blobs = dropZoneBlobs(zone, 16f, 0f, drag, pullRadius = 56f, color = Color.White)
        assertEquals(2, blobs.size)
        val tendril = blobs[1] as Blob.Capsule
        assertEquals(Offset(100f, 50f), tendril.start) // nearest point on the zone's edge
        assertEquals(drag, tendril.end)
    }

    @Test
    fun hoveringScalesTheZoneUp() {
        val grown = (dropZoneBlobs(zone, 16f, hoverGrow = 1f, null, 56f, Color.White).single() as Blob.RoundRect).rect
        assertTrue(grown.width > zone.width)
    }

    @Test
    fun dragInsideTheZoneDrawsNoTendril() {
        val blobs = dropZoneBlobs(zone, 16f, 1f, dragPoint = Offset(50f, 50f), pullRadius = 56f, color = Color.White)
        assertEquals(1, blobs.size)
    }
}
