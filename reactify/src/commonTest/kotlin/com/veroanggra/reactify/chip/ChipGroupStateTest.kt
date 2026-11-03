package com.veroanggra.reactify.chip

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChipGroupStateTest {
    @Test
    fun toggleAddsThenRemoves() {
        val state = GooeyChipGroupState()
        assertFalse(state.isSelected(0))
        state.toggle(0)
        assertTrue(state.isSelected(0))
        state.toggle(0)
        assertFalse(state.isSelected(0))
    }

    @Test
    fun onlySelectedChipsProduceBlobs() {
        val state = GooeyChipGroupState()
        state.registerBounds(0, Rect(0f, 0f, 50f, 30f))
        state.registerBounds(1, Rect(60f, 0f, 110f, 30f))
        state.toggle(1)
        val blobs = state.blobs(Color.White, cornerRadius = 15f, origin = Offset.Zero)
        assertEquals(1, blobs.size)
        assertEquals(Rect(60f, 0f, 110f, 30f), (blobs.single() as Blob.RoundRect).rect)
    }

    @Test
    fun multipleSelectedChipsEachProduceARoundRect() {
        val state = GooeyChipGroupState()
        state.registerBounds(0, Rect(0f, 0f, 50f, 30f))
        state.registerBounds(1, Rect(60f, 0f, 110f, 30f))
        state.toggle(0)
        state.toggle(1)
        val blobs = state.blobs(Color.White, cornerRadius = 15f, origin = Offset.Zero)
        assertEquals(2, blobs.size)
        assertTrue(blobs.all { it is Blob.RoundRect })
    }

    @Test
    fun blobsAreTranslatedByOrigin() {
        val state = GooeyChipGroupState()
        state.registerBounds(0, Rect(100f, 50f, 150f, 80f))
        state.toggle(0)
        val blobs = state.blobs(Color.White, cornerRadius = 15f, origin = Offset(100f, 50f))
        assertEquals(Rect(0f, 0f, 50f, 30f), (blobs.single() as Blob.RoundRect).rect)
    }
}
