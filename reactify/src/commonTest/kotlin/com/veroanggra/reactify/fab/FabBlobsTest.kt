package com.veroanggra.reactify.fab

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val center = Offset(50f, 200f)

class FabBlobsTest {
    @Test
    fun zeroProgressDrawsOnlyTheMainButton() {
        val blobs = fabMenuBlobs(center, mainRadius = 28f, childRadius = 20f, spacing = 64f, itemCount = 3, progress = 0f, color = Color.White)
        assertEquals(1, blobs.size)
        assertEquals(center, (blobs.single() as Blob.Circle).center)
    }

    @Test
    fun noItemsDrawsOnlyTheMainButtonEvenAtFullProgress() {
        val blobs = fabMenuBlobs(center, 28f, 20f, 64f, itemCount = 0, progress = 1f, color = Color.White)
        assertEquals(1, blobs.size)
    }

    @Test
    fun fullProgressSpawnsEveryChildWithNoNeckLeft() {
        val blobs = fabMenuBlobs(center, 28f, 20f, spacing = 64f, itemCount = 2, progress = 1f, color = Color.White)
        val circles = blobs.filterIsInstance<Blob.Circle>()
        assertEquals(3, circles.size) // main + 2 children
        // no capsule necks remain once every child has fully settled
        assertTrue(blobs.filterIsInstance<Blob.Capsule>().isEmpty())
    }

    @Test
    fun earlierChildrenAreAlwaysAtLeastAsFarAlongAsLaterOnes() {
        val first = fabChildLocalProgress(0.2f, index = 0, itemCount = 3)
        val second = fabChildLocalProgress(0.2f, index = 1, itemCount = 3)
        val third = fabChildLocalProgress(0.2f, index = 2, itemCount = 3)
        assertTrue(first > 0f)
        assertTrue(first >= second)
        assertTrue(second >= third)
    }

    @Test
    fun laterChildrenStackFurtherFromTheMainButton() {
        val blobs = fabMenuBlobs(center, 28f, 20f, spacing = 64f, itemCount = 2, progress = 1f, color = Color.White)
        val children = blobs.filterIsInstance<Blob.Circle>().drop(1)
        assertTrue(children[1].center.y < children[0].center.y) // further up (smaller y) than the first
    }
}
