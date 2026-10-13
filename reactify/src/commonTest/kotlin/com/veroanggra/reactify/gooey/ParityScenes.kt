package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

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

    private fun Blob.opaque(): Blob = when (this) {
        is Blob.RoundRect -> copy(color = Color.White)
        is Blob.Circle -> copy(color = Color.White)
        is Blob.Capsule -> copy(color = Color.White)
    }
}
