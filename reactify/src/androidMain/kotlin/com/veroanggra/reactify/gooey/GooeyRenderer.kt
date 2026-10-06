package com.veroanggra.reactify.gooey

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Draws a [GooeyScene]. Implementations must be cheap to call every frame. */
@Stable
public interface GooeyRenderer {
    public fun DrawScope.draw(scene: GooeyScene)
}

public object GooeyRenderers {
    /** Best renderer available on this device: AGSL on API 33+, otherwise [path]. */
    public fun auto(): GooeyRenderer =
        if (Build.VERSION.SDK_INT >= 33) agsl() else path()

    /** Signed-distance-field renderer built on [android.graphics.RuntimeShader]. */
    @RequiresApi(33)
    public fun agsl(): GooeyRenderer = AgslGooeyRenderer()

    /**
     * CPU fallback that traces the same shapes as [agsl] into a [androidx.compose.ui.graphics.Path].
     * Only blobs sharing a colour merge.
     */
    public fun path(cellSize: Dp = 2.5.dp): GooeyRenderer = PathGooeyRenderer(cellSize)

    /** Draws every blob on its own without merging them. Useful for debugging layouts. */
    public fun plain(): GooeyRenderer = PlainGooeyRenderer
}

private object PlainGooeyRenderer : GooeyRenderer {
    override fun DrawScope.draw(scene: GooeyScene) {
        scene.blobs.forEach { blob ->
            when (blob) {
                is Blob.RoundRect -> drawRoundRect(
                    color = blob.color,
                    topLeft = Offset(blob.rect.left, blob.rect.top),
                    size = Size(blob.rect.width, blob.rect.height),
                    cornerRadius = CornerRadius(blob.cornerRadius),
                )
                is Blob.Circle -> drawCircle(blob.color, blob.radius, blob.center)
                is Blob.Capsule -> drawLine(
                    color = blob.color,
                    start = blob.start,
                    end = blob.end,
                    strokeWidth = blob.radius * 2f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
