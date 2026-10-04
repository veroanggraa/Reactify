package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Timeline of a nav-indicator transition, as fractions of progress:
 * ```
 * 0.00 ─── old pill at full width ─── 0.30
 *               0.00 ── bridge grows ── 0.50
 *                    0.15 ── new pill grows in ── 0.70
 *                                  0.30 ── old pill shrinks away ── 1.00
 *                                       0.50 ── bridge breaks & retracts ── 0.95
 * ```
 */
internal object NavIndicatorTimeline {
    const val BRIDGE_GROW_END = 0.5f
    const val BREAK_START = 0.5f
    const val BREAK_END = 0.95f
    const val OLD_SHRINK_START = 0.3f
    const val NEW_GROW_START = 0.15f
    const val NEW_GROW_END = 0.7f
}

/**
 * Blobs for an indicator sliding from [from] to [to]: the old pill shrinks away while a new one
 * grows at the destination, joined for most of the transition by a bridge that grows then pinches
 * apart — the same grow/break shape as [bridgeBlobs], rotated to a horizontal row of nav items.
 */
internal fun navIndicatorBlobs(progress: Float, from: Rect, to: Rect, cornerRadius: Float, color: Color): List<Blob> {
    val oldScale = 1f - easeIn(phase(progress, NavIndicatorTimeline.OLD_SHRINK_START, 1f))
    val newScale = easeOut(phase(progress, NavIndicatorTimeline.NEW_GROW_START, NavIndicatorTimeline.NEW_GROW_END))
    val bridgeWidth = min(from.height, to.height) * 0.9f

    return buildList {
        scaledPill(from, oldScale, cornerRadius, color)?.let { add(it) }
        scaledPill(to, newScale, cornerRadius, color)?.let { add(it) }
        addAll(navBridgeBlobs(progress, from, to, bridgeWidth, color))
    }
}

private fun scaledPill(rect: Rect, widthScale: Float, cornerRadius: Float, color: Color): Blob.RoundRect? {
    if (widthScale <= 0.02f) return null
    val w = rect.width * widthScale
    val cx = rect.center.x
    return Blob.RoundRect(Rect(cx - w / 2f, rect.top, cx + w / 2f, rect.bottom), cornerRadius, color)
}

private const val BRIDGE_SEGMENTS = 6

/**
 * Horizontal gooey thread joining the facing edges of [from] and [to]. While growing it is one
 * thread anchored at both pills, thinnest in the middle like a stretched drop of liquid (the same
 * idea as the dragged neck in [dragBlobs]); once it starts breaking it splits into two threads,
 * each still anchored at its own pill but tapering to a free tip that retracts and thins to
 * nothing, like the bridge pinching apart and being drawn back in.
 */
internal fun navBridgeBlobs(progress: Float, from: Rect, to: Rect, width: Float, color: Color): List<Blob> {
    val grow = easeOut(phase(progress, 0f, NavIndicatorTimeline.BRIDGE_GROW_END))
    if (grow <= 0f) return emptyList()
    val broken = phase(progress, NavIndicatorTimeline.BREAK_START, NavIndicatorTimeline.BREAK_END)
    if (broken >= 1f) return emptyList()

    val forward = to.center.x >= from.center.x
    val rowY = (from.center.y + to.center.y) / 2f
    val edgeFrom = if (forward) from.right else from.left
    val edgeTo = if (forward) to.left else to.right
    val overlap = width / 2f
    val fromIn = if (forward) edgeFrom - overlap else edgeFrom + overlap
    val toIn = if (forward) edgeTo + overlap else edgeTo - overlap
    val endX = lerp(fromIn, toIn, grow)

    if (broken <= 0f) return neckingThread(fromIn, endX, rowY, width, color)

    val mid = (fromIn + toIn) / 2f
    val retract = easeInOut(broken)
    return taperedTip(fromIn, lerp(mid, fromIn, retract), rowY, width, color) +
        taperedTip(toIn, lerp(mid, toIn, retract), rowY, width, color)
}

/** Thread anchored at both ends, thinning towards the middle in proportion to how far it's stretched. */
private fun neckingThread(startX: Float, endX: Float, rowY: Float, width: Float, color: Color): List<Blob> {
    val stretch = (abs(endX - startX) / (width * 3f)).coerceIn(0f, 1f)
    return threadSegments(startX, endX, rowY, width, color) { mid -> 1f - 0.6f * stretch * sin(PI.toFloat() * mid) }
}

/** Thread anchored at [anchorX], tapering towards its free, retracting tip at [tipX]. */
private fun taperedTip(anchorX: Float, tipX: Float, rowY: Float, width: Float, color: Color): List<Blob> {
    if (abs(tipX - anchorX) < 1f) return emptyList()
    val stretch = (abs(tipX - anchorX) / (width * 2.5f)).coerceIn(0f, 1f)
    return threadSegments(anchorX, tipX, rowY, width, color) { tipFactor -> 1f - 0.7f * stretch * tipFactor }
}

private inline fun threadSegments(
    startX: Float,
    endX: Float,
    rowY: Float,
    width: Float,
    color: Color,
    thinningAt: (Float) -> Float,
): List<Blob> = (0 until BRIDGE_SEGMENTS).map { i ->
    val t0 = i / BRIDGE_SEGMENTS.toFloat()
    val t1 = (i + 1) / BRIDGE_SEGMENTS.toFloat()
    val radius = max(width / 2f * thinningAt((t0 + t1) / 2f), 0f)
    Blob.Capsule(
        start = Offset(lerp(startX, endX, t0), rowY),
        end = Offset(lerp(startX, endX, t1), rowY),
        radius = radius,
        color = color,
    )
}

/** Simple linear interpolation of [from]/[to], used only to retarget a transition in flight. */
internal fun lerpRect(from: Rect, to: Rect, t: Float): Rect = Rect(
    lerp(from.left, to.left, t),
    lerp(from.top, to.top, t),
    lerp(from.right, to.right, t),
    lerp(from.bottom, to.bottom, t),
)
