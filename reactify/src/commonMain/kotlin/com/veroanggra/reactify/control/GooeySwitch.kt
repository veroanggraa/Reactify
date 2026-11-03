package com.veroanggra.reactify.control

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene

public object GooeyControlDefaults {
    public val SwitchWidth: Dp = 52.dp
    public val SwitchHeight: Dp = 32.dp
    public val SliderHeight: Dp = 32.dp
    public val SliderTrackThickness: Dp = 8.dp
    public val BubbleRise: Dp = 40.dp
    public val Smoothness: Dp = 8.dp
    public val toggleSpec: AnimationSpec<Float> = spring(dampingRatio = 0.6f, stiffness = 400f)
    public val dragSpec: AnimationSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 500f)
}

/**
 * A toggle switch whose thumb bulges past the track and pulls it into a liquid shape as it slides,
 * instead of just sitting on top of it.
 */
@Composable
public fun GooeySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = color.copy(alpha = 0.3f),
    width: Dp = GooeyControlDefaults.SwitchWidth,
    height: Dp = GooeyControlDefaults.SwitchHeight,
    smoothness: Dp = GooeyControlDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val progress by animateFloatAsState(if (checked) 1f else 0f, GooeyControlDefaults.toggleSpec, label = "gooeySwitch")
    val smoothnessPx = with(LocalDensity.current) { smoothness.toPx() }
    Canvas(
        modifier
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .size(width, height),
    ) {
        val thumbRadius = size.height / 2f * 0.92f
        val trackHeight = size.height * 0.72f
        val track = Rect(
            left = thumbRadius,
            top = (size.height - trackHeight) / 2f,
            right = size.width - thumbRadius,
            bottom = (size.height + trackHeight) / 2f,
        )
        drawRoundRect(
            trackColor,
            topLeft = Offset(0f, (size.height - trackHeight) / 2f),
            size = Size(size.width, trackHeight),
            cornerRadius = CornerRadius(trackHeight / 2f),
        )
        val scene = GooeyScene(trackAndThumb(progress, track, thumbRadius, color), smoothness = smoothnessPx)
        with(renderer) { draw(scene) }
    }
}
