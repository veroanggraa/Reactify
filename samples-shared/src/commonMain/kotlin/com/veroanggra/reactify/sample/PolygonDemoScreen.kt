package com.veroanggra.reactify.sample

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene

private val HouseMint = Color(0xFFC8F5D4)

/** A simple house silhouette: a pentagon, well under the 8-vertex AGSL/SkSL cap. */
private fun houseVertices(left: Float, top: Float, size: Float): List<Offset> {
    val peakHeight = size * 0.5f
    val wallTop = top + peakHeight
    return listOf(
        Offset(left, top + size),
        Offset(left, wallTop),
        Offset(left + size / 2f, top),
        Offset(left + size, wallTop),
        Offset(left + size, top + size),
    )
}

@Composable
fun PolygonDemoScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val t by rememberInfiniteTransition(label = "gooeyPolygonDemo").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "smoke",
    )

    DemoScaffold(
        title = "Blob.Polygon",
        subtitle = "Arbitrary simple polygons merging like any other blob.",
        accent = HouseMint,
        modifier = modifier,
    ) {
        DemoCard {
            Canvas(Modifier.fillMaxWidth().height(260.dp)) {
                val house = houseVertices(left = size.width / 2f - 70f, top = size.height - 160f, size = 140f)
                val roofPeak = house[2]
                val smokeY = roofPeak.y - 20f - t * 70f
                val smoke = Blob.Circle(Offset(roofPeak.x, smokeY), radius = 16f + 6f * (1f - t), color = HouseMint)
                val scene = GooeyScene(
                    blobs = listOf(Blob.Polygon(house, HouseMint), smoke),
                    smoothness = 24f,
                )
                with(renderer) { draw(scene) }
            }
        }
    }
}
