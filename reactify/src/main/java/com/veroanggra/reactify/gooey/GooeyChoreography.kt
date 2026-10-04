package com.veroanggra.reactify.gooey

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Timeline of a single transfer, expressed as fractions of the overall progress:
 *
 * ```
 * 0.00 ─ tint in ─ 0.15
 * 0.05 ── bridge grows ── 0.30
 *      0.15 ── token rises ── 0.60
 *                0.35 ── token slides into slot ── 0.85
 *                          0.60 ── bridge breaks & retracts ── 0.95
 *                                   0.75 ── tint out ── 1.00
 * ```
 */
internal object TransferTimeline {
    const val TINT_IN_END = 0.15f
    const val TINT_OUT_START = 0.75f
    const val BRIDGE_GROW_START = 0.05f
    const val BRIDGE_GROW_END = 0.30f
    const val RISE_START = 0.15f
    const val RISE_END = 0.60f
    const val SLIDE_START = 0.35f
    const val SLIDE_END = 0.85f
    const val BREAK_START = 0.60f
    const val BREAK_END = 0.95f
}

internal fun phase(progress: Float, start: Float, end: Float): Float =
    ((progress - start) / (end - start)).coerceIn(0f, 1f)

internal fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

internal fun easeIn(t: Float) = t * t * t
internal fun easeOut(t: Float) = 1f - (1f - t).pow(3)
internal fun easeInOut(t: Float) = if (t < 0.5f) 4f * t * t * t else 1f - (-2f * t + 2f).pow(3) / 2f
private fun easeOutBack(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (t - 1f).pow(3) + c1 * (t - 1f).pow(2)
}

/** Tint fades in quickly, holds while the token travels, and fades out as the bridge settles. */
internal fun tintAlpha(progress: Float, max: Float): Float {
    val fadeIn = easeOut(phase(progress, 0f, TransferTimeline.TINT_IN_END))
    val fadeOut = 1f - easeIn(phase(progress, TransferTimeline.TINT_OUT_START, 1f))
    return (min(fadeIn, fadeOut) * max).coerceIn(0f, 1f)
}

/** Token rises along the bridge first, then slides sideways into its slot with a slight overshoot. */
internal fun tokenRectAt(progress: Float, start: Rect, end: Rect): Rect {
    val rise = easeInOut(phase(progress, TransferTimeline.RISE_START, TransferTimeline.RISE_END))
    val slidePhase = phase(progress, TransferTimeline.SLIDE_START, TransferTimeline.SLIDE_END)
    val slide = easeInOut(slidePhase)
    val cx = lerp(start.center.x, end.center.x, slide)
    val cy = lerp(start.center.y, end.center.y, rise)
    val sizeT = easeOutBack(slidePhase)
    val w = lerp(start.width, end.width, sizeT)
    val h = lerp(start.height, end.height, sizeT)
    return Rect(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
}

/**
 * Vertical pill(s) joining the edge of [source] facing [target] to the facing edge of [target].
 * One pill while growing; after the break it splits in the middle and both halves retract, which
 * the smooth-min turns into a pinching neck.
 */
internal fun bridgeBlobs(
    progress: Float,
    source: Rect,
    target: Rect,
    columnX: Float,
    width: Float,
    color: Color,
): List<Blob> {
    val grow = easeOut(phase(progress, TransferTimeline.BRIDGE_GROW_START, TransferTimeline.BRIDGE_GROW_END))
    if (grow <= 0f) return emptyList()
    val broken = phase(progress, TransferTimeline.BREAK_START, TransferTimeline.BREAK_END)
    if (broken >= 1f) return emptyList()

    val upward = target.center.y < source.center.y
    val from = if (upward) source.top else source.bottom
    val to = if (upward) target.bottom else target.top
    val overlap = width / 2f
    val fromIn = if (upward) from + overlap else from - overlap
    val toIn = if (upward) to - overlap else to + overlap
    val w = width * (1f - 0.35f * easeIn(broken))

    fun pill(a: Float, b: Float) = Blob.RoundRect(
        rect = Rect(columnX - w / 2f, min(a, b), columnX + w / 2f, max(a, b)),
        cornerRadius = w / 2f,
        color = color,
    )

    if (broken <= 0f) return listOf(pill(fromIn, lerp(fromIn, toIn, grow)))
    val mid = (fromIn + toIn) / 2f
    val retract = easeInOut(broken)
    return listOf(
        pill(fromIn, lerp(mid, fromIn, retract)),
        pill(lerp(mid, toIn, retract), toIn),
    )
}

internal fun transferBlobs(
    progress: Float,
    color: Color,
    source: Rect,
    sourceCornerRadius: Float,
    target: Rect,
    targetCornerRadius: Float,
    tokenStart: Rect,
    token: Rect,
    maxTintAlpha: Float,
    withBridge: Boolean = true,
): List<Blob> {
    val alpha = tintAlpha(progress, maxTintAlpha)
    if (alpha < 0.001f) return emptyList()
    val tint = color.copy(alpha = alpha)
    val bridgeWidth = min(tokenStart.width, tokenStart.height) * 0.8f
    val columnX = tokenStart.center.x.coerceIn(
        max(source.left, target.left) + bridgeWidth,
        max(min(source.right, target.right) - bridgeWidth, max(source.left, target.left) + bridgeWidth),
    )
    return buildList {
        add(Blob.RoundRect(source, sourceCornerRadius, tint))
        add(Blob.RoundRect(target, targetCornerRadius, tint))
        if (withBridge) addAll(bridgeBlobs(progress, source, target, columnX, bridgeWidth, tint))
        add(Blob.Circle(token.center, min(token.width, token.height) / 2f, tint))
    }
}

internal const val NECK_SEGMENTS = 4

/**
 * Stretchy neck from [anchor] (where the token sat in the source) to the dragged token, thinning
 * in the middle as it is pulled further. [neckScale] shrinks it to nothing when the neck snaps.
 * The card tints in proportion to how far the token has travelled towards it.
 */
internal fun dragBlobs(
    color: Color,
    source: Rect,
    sourceCornerRadius: Float,
    target: Rect?,
    targetCornerRadius: Float,
    anchor: Offset,
    token: Rect?,
    tokenCenter: Offset,
    tokenRadius: Float,
    neckScale: Float,
    maxTintAlpha: Float,
): List<Blob> {
    val pull = tokenCenter - anchor
    val distance = pull.getDistance()
    val alpha = maxTintAlpha * easeOut((distance / (tokenRadius * 2f)).coerceIn(0f, 1f))
    if (alpha < 0.001f) return emptyList()
    val tint = color.copy(alpha = alpha)
    val stretch = (distance / (tokenRadius * 8f)).coerceIn(0f, 1f)

    return buildList {
        add(Blob.RoundRect(source, sourceCornerRadius, tint))
        if (target != null) {
            val reach = (target.center - anchor).getDistance().coerceAtLeast(1f)
            val towards = ((pull.x * (target.center.x - anchor.x) + pull.y * (target.center.y - anchor.y)) / (reach * reach))
                .coerceIn(0f, 1f)
            add(Blob.RoundRect(target, targetCornerRadius, color.copy(alpha = alpha * towards)))
        }
        if (neckScale > 0f) {
            for (i in 0 until NECK_SEGMENTS) {
                val t0 = i / NECK_SEGMENTS.toFloat()
                val t1 = (i + 1) / NECK_SEGMENTS.toFloat()
                val mid = (t0 + t1) / 2f
                val thinning = 1f - 0.65f * sin(PI.toFloat() * mid) * stretch
                val radius = tokenRadius * 0.7f * thinning * neckScale
                add(Blob.Capsule(anchor + pull * t0, anchor + pull * t1, radius, tint))
            }
        }
        if (token != null) add(Blob.Circle(token.center, min(token.width, token.height) / 2f, tint))
    }
}
