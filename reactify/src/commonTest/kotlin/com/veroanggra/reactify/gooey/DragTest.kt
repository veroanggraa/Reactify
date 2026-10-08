package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.list.shouldCommitDrag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DragTest {

    private val target = Rect(0f, 0f, 400f, 200f)
    private val source = Rect(0f, 600f, 400f, 660f)
    private val anchor = Offset(40f, 630f)

    private fun neck(pull: Offset, neckScale: Float = 1f) = dragBlobs(
        color = Color.Red,
        source = source,
        sourceCornerRadius = 16f,
        target = target,
        targetCornerRadius = 24f,
        anchor = anchor,
        token = null,
        tokenCenter = anchor + pull,
        tokenRadius = 20f,
        neckScale = neckScale,
        maxTintAlpha = 0.5f,
    ).filterIsInstance<Blob.Capsule>()

    @Test
    fun neckJoinsAnchorToFingerAndThinsInTheMiddle() {
        val capsules = neck(Offset(0f, -300f))
        assertEquals(NECK_SEGMENTS, capsules.size)
        assertEquals(anchor, capsules.first().start)
        assertEquals(anchor + Offset(0f, -300f), capsules.last().end)
        assertTrue(capsules[1].radius < capsules[0].radius)
    }

    @Test
    fun shortPullKeepsNeckThick() {
        val short = neck(Offset(0f, -60f))
        val long = neck(Offset(0f, -300f))
        assertTrue(short.minOf { it.radius } > long.minOf { it.radius })
    }

    @Test
    fun snappedNeckHasNoCapsules() {
        assertTrue(neck(Offset(0f, -300f), neckScale = 0f).isEmpty())
    }

    @Test
    fun commitsWhenReleasedOverTargetOrPastHalfway() {
        assertTrue(shouldCommitDrag(anchor, Offset(200f, 100f), Offset.Zero, target, 2000f))
        assertTrue(shouldCommitDrag(anchor, anchor + Offset(0f, -300f), Offset.Zero, target, 2000f))
    }

    @Test
    fun cancelsShortSlowOrSidewaysDrags() {
        assertFalse(shouldCommitDrag(anchor, anchor + Offset(0f, -80f), Offset.Zero, target, 2000f))
        assertFalse(
            shouldCommitDrag(
                anchor,
                anchor + Offset(0f, 80f),
                Offset(0f, -3000f),
                target,
                2000f
            )
        )
        assertFalse(shouldCommitDrag(anchor, anchor + Offset(0f, -300f), Offset.Zero, null, 2000f))
    }

    @Test
    fun upwardFlingCommitsShortDrag() {
        assertTrue(
            shouldCommitDrag(
                anchor,
                anchor + Offset(0f, -80f),
                Offset(0f, -3000f),
                target,
                2000f
            )
        )
    }

    @Test
    fun capsuleDistanceMatchesSegmentDistance() {
        val capsule = Blob.Capsule(Offset(0f, 0f), Offset(100f, 0f), 10f, Color.Red)
        val field = GooeyField(listOf(capsule), emptyList(), smoothness = 10f)
        assertEquals(-10f, field.distance(50f, 0f), 0.001f)
        assertEquals(20f, field.distance(50f, 30f), 0.001f)
        assertEquals(10f, field.distance(-20f, 0f), 0.001f)
    }
}
