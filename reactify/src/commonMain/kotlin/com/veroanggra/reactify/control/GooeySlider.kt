package com.veroanggra.reactify.control

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene
import com.veroanggra.reactify.gooey.lerp
import kotlinx.coroutines.launch

/**
 * A slider whose thumb pulls a value bubble up out of itself while dragged, connected by a
 * thinning neck, and reabsorbs it on release.
 *
 * @param value current fraction, clamped to 0..1.
 * @param label content centred in the bubble while dragging, e.g. a short value label.
 */
@Composable
public fun GooeySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = color.copy(alpha = 0.25f),
    height: Dp = GooeyControlDefaults.SliderHeight,
    bubbleRise: Dp = GooeyControlDefaults.BubbleRise,
    smoothness: Dp = GooeyControlDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    label: @Composable BoxScope.() -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val dragged = remember { Animatable(0f) }
    val density = LocalDensity.current
    val smoothnessPx = with(density) { smoothness.toPx() }
    val bubbleRisePx = with(density) { bubbleRise.toPx() }
    val clamped = value.coerceIn(0f, 1f)

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val thumbRadiusPx = with(density) { height.toPx() } / 2f * 0.95f
        val thumbX = lerp(thumbRadiusPx, widthPx - thumbRadiusPx, clamped)
        val bubbleSize = with(density) { (thumbRadiusPx * 2.6f).toDp() }

        Box(Modifier.fillMaxWidth().height(height + bubbleRise), contentAlignment = Alignment.BottomStart) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .pointerInput(widthPx) {
                        detectHorizontalDragGestures(
                            onDragStart = { scope.launch { dragged.animateTo(1f, GooeyControlDefaults.dragSpec) } },
                            onDragEnd = { scope.launch { dragged.animateTo(0f, GooeyControlDefaults.dragSpec) } },
                            onDragCancel = { scope.launch { dragged.animateTo(0f, GooeyControlDefaults.dragSpec) } },
                        ) { change, _ ->
                            change.consume()
                            val usable = (widthPx - thumbRadiusPx * 2f).coerceAtLeast(1f)
                            onValueChange(((change.position.x - thumbRadiusPx) / usable).coerceIn(0f, 1f))
                        }
                    },
            ) {
                val trackThickness = GooeyControlDefaults.SliderTrackThickness.toPx()
                val track = Rect(
                    left = thumbRadiusPx,
                    top = (size.height - trackThickness) / 2f,
                    right = size.width - thumbRadiusPx,
                    bottom = (size.height + trackThickness) / 2f,
                )
                drawRoundRect(
                    trackColor,
                    topLeft = Offset(0f, track.top),
                    size = Size(size.width, trackThickness),
                    cornerRadius = CornerRadius(trackThickness / 2f),
                )
                val blobs = sliderBlobs(clamped, track, thumbRadiusPx, thumbRadiusPx * 1.3f, bubbleRisePx, dragged.value, color)
                with(renderer) { draw(GooeyScene(blobs, smoothness = smoothnessPx)) }
            }
            if (dragged.value > 0.01f) {
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = with(density) { thumbX.toDp() } - bubbleSize / 2f)
                        .size(bubbleSize)
                        .graphicsLayer { alpha = dragged.value; scaleX = dragged.value; scaleY = dragged.value },
                    contentAlignment = Alignment.Center,
                ) { label() }
            }
        }
    }
}
