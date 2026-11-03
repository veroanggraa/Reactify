package com.veroanggra.reactify.badge

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene

public object GooeyBadgeDefaults {
    public val Radius: Dp = 9.dp
    public val Smoothness: Dp = 6.dp
    public val popSpec: AnimationSpec<Float> = spring(dampingRatio = 0.55f, stiffness = 300f)
}

/**
 * Wraps [content] with a badge that pops out of its top-end corner on a short stem as [visible]
 * turns true, and is drawn back in as it turns false.
 */
@Composable
public fun GooeyBadgedBox(
    visible: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    badgeRadius: Dp = GooeyBadgeDefaults.Radius,
    smoothness: Dp = GooeyBadgeDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    badge: @Composable BoxScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val grow by animateFloatAsState(if (visible) 1f else 0f, GooeyBadgeDefaults.popSpec, label = "gooeyBadge")
    val density = LocalDensity.current
    val radiusPx = with(density) { badgeRadius.toPx() }
    val smoothnessPx = with(density) { smoothness.toPx() }

    Box(modifier) {
        content()
        Canvas(Modifier.matchParentSize()) {
            if (grow <= 0.01f) return@Canvas
            val anchor = Offset(size.width - radiusPx * 1.4f, radiusPx * 1.4f)
            val rest = Offset(radiusPx * 0.5f, -radiusPx * 0.5f)
            with(renderer) { draw(GooeyScene(badgeBlobs(anchor, rest, radiusPx, grow, color), smoothness = smoothnessPx)) }
        }
        if (grow > 0.01f) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = badgeRadius * 0.4f, y = -badgeRadius * 0.4f)
                    .size(badgeRadius * 2f)
                    .graphicsLayer { alpha = grow; scaleX = grow; scaleY = grow },
                contentAlignment = Alignment.Center,
            ) { badge() }
        }
    }
}
