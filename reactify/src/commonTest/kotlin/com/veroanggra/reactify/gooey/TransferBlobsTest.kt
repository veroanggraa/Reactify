package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransferBlobsTest {

    private val source = Rect(0f, 500f, 300f, 560f)
    private val target = Rect(0f, 0f, 300f, 200f)
    private val tokenStart = Rect(10f, 510f, 50f, 550f)
    private val slot = Rect(135f, 85f, 165f, 115f)

    private fun blobsAt(progress: Float) = transferBlobs(
        progress = progress,
        color = Color.Red,
        source = source,
        sourceCornerRadius = 16f,
        target = target,
        targetCornerRadius = 24f,
        tokenStart = tokenStart,
        token = tokenRectAt(progress, tokenStart, slot),
        maxTintAlpha = 0.5f,
    )

    private fun pills(progress: Float) = blobsAt(progress).drop(2).dropLast(1).map { it as Blob.RoundRect }

    @Test
    fun noBlobsAtStartAndEnd() {
        assertTrue(blobsAt(0f).isEmpty())
        assertTrue(blobsAt(1f).isEmpty())
    }

    @Test
    fun tintHoldsAtMaxWhileTravelling() {
        assertEquals(0.5f, tintAlpha(0.5f, 0.5f), 0.001f)
        assertTrue(tintAlpha(0.05f, 0.5f) < 0.5f)
        assertTrue(tintAlpha(0.9f, 0.5f) < 0.5f)
        assertEquals(0f, tintAlpha(-1f, 0.5f), 0.001f)
    }

    @Test
    fun bridgeSpansGapOnceGrownThenSplitsInTwo() {
        val grown = pills(0.4f).single()
        assertTrue(grown.rect.top < target.bottom)
        assertTrue(grown.rect.bottom > source.top)

        val broken = pills(0.8f)
        assertEquals(2, broken.size)
        assertTrue(broken[0].rect.top > broken[1].rect.bottom)
    }

    @Test
    fun bridgeIsGoneBeforeTintFadesOut() {
        assertTrue(pills(0.97f).isEmpty())
    }

    @Test
    fun tokenRisesBeforeSlidingSidewaysAndEndsInSlot() {
        val early = tokenRectAt(0.4f, tokenStart, slot)
        val riseSoFar = (tokenStart.center.y - early.center.y) / (tokenStart.center.y - slot.center.y)
        val slideSoFar = (early.center.x - tokenStart.center.x) / (slot.center.x - tokenStart.center.x)
        assertTrue(riseSoFar > slideSoFar)

        val end = tokenRectAt(1f, tokenStart, slot)
        assertEquals(slot.center.x, end.center.x, 0.01f)
        assertEquals(slot.center.y, end.center.y, 0.01f)
        assertEquals(slot.width, end.width, 0.01f)
    }
}
