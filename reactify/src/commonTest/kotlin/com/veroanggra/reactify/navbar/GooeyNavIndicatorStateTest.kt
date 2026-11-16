package com.veroanggra.reactify.navbar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/**
 * [GooeyNavIndicatorState.select] launches its grow/break transition on the scope passed at
 * construction; this dispatcher never runs anything posted to it, so these tests exercise only
 * the state's synchronous bookkeeping (selection, bounds registration, blob wiring) without the
 * animation ever starting. The blob shape mid-transition is already covered by
 * SlidingIndicatorBlobsTest, which both the nav indicator and the segmented control share.
 */
private object NeverDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) = Unit
}

private fun testState(initialIndex: Int = 0): GooeyNavIndicatorState =
    GooeyNavIndicatorState(initialIndex, CoroutineScope(NeverDispatcher))

class GooeyNavIndicatorStateTest {

    @Test
    fun noIndicatorUntilTheSelectedIndexRegistersBounds() {
        val state = testState(initialIndex = 1)
        assertFalse(state.hasIndicator)
        state.registerBounds(0, Rect(0f, 0f, 50f, 30f))
        assertFalse(state.hasIndicator)
        state.registerBounds(1, Rect(60f, 0f, 110f, 30f))
        assertTrue(state.hasIndicator)
    }

    @Test
    fun selectIgnoresAnUnregisteredIndex() {
        val state = testState()
        state.registerBounds(0, Rect(0f, 0f, 50f, 30f))
        state.select(5)
        assertEquals(0, state.selectedIndex)
    }

    @Test
    fun selectIgnoresTheAlreadySelectedIndex() {
        val state = testState()
        state.registerBounds(0, Rect(0f, 0f, 50f, 30f))
        state.select(0)
        assertEquals(0, state.selectedIndex)
    }

    @Test
    fun selectUpdatesSelectedIndexImmediately() {
        val state = testState()
        state.registerBounds(0, Rect(0f, 0f, 50f, 30f))
        state.registerBounds(1, Rect(60f, 0f, 110f, 30f))
        state.select(1)
        assertEquals(1, state.selectedIndex)
    }

    @Test
    fun blobsDefaultCornerRadiusToHalfTheRectHeight() {
        val state = testState()
        state.registerBounds(0, Rect(10f, 20f, 70f, 76f)) // 56 tall
        val blob = state.blobs(Color.Red, cornerRadius = null, origin = Offset.Zero).single() as Blob.RoundRect
        assertEquals(Rect(10f, 20f, 70f, 76f), blob.rect)
        assertEquals(28f, blob.cornerRadius)
    }

    @Test
    fun blobsUseTheExplicitCornerRadiusWhenGiven() {
        val state = testState()
        state.registerBounds(0, Rect(10f, 20f, 70f, 76f))
        val blob = state.blobs(Color.Red, cornerRadius = 6f, origin = Offset.Zero).single() as Blob.RoundRect
        assertEquals(6f, blob.cornerRadius)
    }

    @Test
    fun blobsAreTranslatedByOrigin() {
        val state = testState()
        state.registerBounds(0, Rect(100f, 50f, 150f, 80f))
        val blob = state.blobs(Color.Red, cornerRadius = null, origin = Offset(100f, 50f)).single() as Blob.RoundRect
        assertEquals(Rect(0f, 0f, 50f, 30f), blob.rect)
    }
}
