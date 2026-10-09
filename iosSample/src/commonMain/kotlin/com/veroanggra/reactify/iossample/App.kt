package com.veroanggra.reactify.iossample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene
import kotlin.math.sin

/**
 * Smoke test for [GooeyRenderers.auto] on iOS (resolves to the SkSL RuntimeEffect-backed
 * renderer): two circles drift together and apart so the smooth-min merge/pinch is visible.
 */
@Composable
public fun App() {
    val renderer = remember { GooeyRenderers.auto() }
    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var startNanos = -1L
        while (true) {
            withFrameNanos { nanos ->
                if (startNanos < 0) startNanos = nanos
                elapsedSeconds = (nanos - startNanos) / 1_000_000_000f
            }
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = 70f
        val separation = 90f + 90f * sin(elapsedSeconds)
        val scene = GooeyScene(
            blobs = listOf(
                Blob.Circle(Offset(centerX - separation, centerY), radius, Color(0xFF6750A4)),
                Blob.Circle(Offset(centerX + separation, centerY), radius, Color(0xFF00BFA5)),
            ),
            smoothness = 60f,
        )
        with(renderer) { draw(scene) }
    }
}
