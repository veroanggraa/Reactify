package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
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

    private fun Blob.opaque(): Blob = when (this) {
        is Blob.RoundRect -> copy(color = Color.White)
        is Blob.Circle -> copy(color = Color.White)
        is Blob.Capsule -> copy(color = Color.White)
    }
}
