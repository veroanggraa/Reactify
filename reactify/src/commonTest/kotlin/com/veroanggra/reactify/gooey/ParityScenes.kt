package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.badge.badgeBlobs
import com.veroanggra.reactify.chip.GooeyChipGroupState
import com.veroanggra.reactify.control.sliderBlobs
import com.veroanggra.reactify.fab.fabMenuBlobs
import com.veroanggra.reactify.list.dropZoneBlobs
import com.veroanggra.reactify.progress.linearProgressBlobs
import com.veroanggra.reactify.refresh.pullBlobs
import com.veroanggra.reactify.stepper.stepperBlobs

/**
 * Fixed scenes shared by every renderer's parity test (one per platform shader). Keeping them in
 * one place means "parity" actually means something: AGSL, Path and SkSL are all checked against
 * the exact same geometry, so a renderer can't drift without its test catching it.
 */
internal object ParityScenes {
    val smoothness = 48f

    private val source = Rect(20f, 460f, 380f, 540f)
    private val target = Rect(20f, 40f, 380f, 240f)
    private val tokenStart = Rect(40f, 476f, 88f, 524f)
    private val slot = Rect(176f, 116f, 224f, 164f)

    private fun transferScene(progress: Float) = GooeyScene(
        blobs = transferBlobs(
            progress = progress,
            color = Color.White,
            source = source,
            sourceCornerRadius = 32f,
            target = target,
            targetCornerRadius = 48f,
            tokenStart = tokenStart,
            token = tokenRectAt(progress, tokenStart, slot),
            maxTintAlpha = 1f,
        ).map { it.opaque() },
        smoothness = smoothness,
    )

    fun bridgeScene() = transferScene(0.4f)

    fun breakingScene() = transferScene(0.82f)

    fun dragScene(): GooeyScene {
        val anchor = tokenStart.center
        val finger = Offset(300f, 300f)
        return GooeyScene(
            blobs = dragBlobs(
                color = Color.White,
                source = source,
                sourceCornerRadius = 32f,
                target = null,
                targetCornerRadius = 48f,
                anchor = anchor,
                token = Rect(finger, 24f),
                tokenCenter = finger,
                tokenRadius = 24f,
                neckScale = 1f,
                maxTintAlpha = 1f,
            ).map { it.opaque() },
            smoothness = smoothness,
        )
    }

    fun notchScene() = GooeyScene(
        blobs = listOf(Blob.RoundRect(target, 48f, Color.White)),
        cutouts = listOf(Blob.Circle(Offset(target.center.x, target.bottom), 60f, Color.White)),
        smoothness = smoothness,
    )

    fun mergeScene() = GooeyScene(
        blobs = listOf(
            Blob.Circle(Offset(140f, 360f), 60f, Color.White),
            Blob.Circle(Offset(270f, 360f), 50f, Color.White),
        ),
        smoothness = smoothness,
    )

    fun progressNeckScene() = GooeyScene(
        blobs = linearProgressBlobs(Rect(40f, 280f, 360f, 320f), fillFraction = 0.3f, headFraction = 0.7f, color = Color.White),
        smoothness = 20f, // 48f swallows the 16px-thick neck
    )

    fun stepperScene() = GooeyScene(
        blobs = stepperBlobs(
            progress = 0.45f, from = 1, to = 2,
            steps = listOf(50f, 150f, 250f, 350f).map { Rect(Offset(it, 300f), 22f) },
            dotRadius = 22f, color = Color.White,
        ),
        smoothness = smoothness,
    )

    fun pullScene() = GooeyScene(
        blobs = pullBlobs(
            pull = 150f, threshold = 200f, width = 400f, dropRadius = 36f,
            neckScale = 1f, dropScale = 1f, color = Color.White,
        ),
        smoothness = smoothness,
    )

    fun sliderScene() = GooeyScene(
        blobs = sliderBlobs(
            progress = 0.6f, track = Rect(40f, 290f, 360f, 310f),
            thumbRadius = 15f, bubbleRadius = 20f, bubbleRise = 40f, dragged = 1f, color = Color.White,
        ),
        smoothness = 8f,
    )

    fun badgeScene() = GooeyScene(
        blobs = badgeBlobs(
            anchor = Offset(340f, 60f), restOffset = Offset(16f, -16f),
            badgeRadius = 16f, grow = 0.5f, color = Color.White,
        ),
        smoothness = 6f,
    )

    fun fabMenuScene() = GooeyScene(
        blobs = fabMenuBlobs(
            center = Offset(200f, 520f), mainRadius = 28f, childRadius = 20f,
            spacing = 64f, itemCount = 3, progress = 0.35f, color = Color.White,
        ),
        smoothness = 10f,
    )

    fun dropZoneScene() = GooeyScene(
        blobs = dropZoneBlobs(
            zoneRect = Rect(60f, 230f, 340f, 370f), cornerRadius = 24f, hoverGrow = 0.5f,
            dragPoint = Offset(380f, 300f), pullRadius = 70f, color = Color.White,
        ),
        smoothness = 16f,
    )

    fun cutoutScene() = GooeyScene(
        blobs = listOf(Blob.RoundRect(Rect(0f, 0f, 400f, 100f), 0f, Color.White)),
        cutouts = listOf(Blob.Circle(Offset(200f, 0f), 36f, Color.White)),
        smoothness = 24f,
    )

    fun chipGroupScene(): GooeyScene {
        val state = GooeyChipGroupState()
        state.registerBounds(0, Rect(60f, 280f, 160f, 320f))
        state.registerBounds(1, Rect(168f, 280f, 260f, 320f)) // close enough to bridge
        state.registerBounds(2, Rect(300f, 280f, 380f, 320f)) // far: stays separate
        state.toggle(0)
        state.toggle(1)
        state.toggle(2)
        return GooeyScene(state.blobs(Color.White, cornerRadius = 20f, origin = Offset.Zero), smoothness = 10f)
    }

    private fun Blob.opaque(): Blob = when (this) {
        is Blob.RoundRect -> copy(color = Color.White)
        is Blob.Circle -> copy(color = Color.White)
        is Blob.Capsule -> copy(color = Color.White)
    }
}
