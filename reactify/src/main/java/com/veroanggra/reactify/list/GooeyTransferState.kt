package com.veroanggra.reactify.list

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.GooeyScene
import com.veroanggra.reactify.gooey.TransferTimeline
import com.veroanggra.reactify.gooey.dragBlobs
import com.veroanggra.reactify.gooey.tokenRectAt
import com.veroanggra.reactify.gooey.transferBlobs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.sqrt

public object GooeyTransferDefaults {
    public val CornerRadius: Dp = 16.dp
    public val Smoothness: Dp = 24.dp
    public val TokenSize: Dp = 32.dp
    public val TokenSpacing: Dp = 6.dp

    /** Releasing a drag faster than this towards the target commits the transfer. */
    public val FlingVelocity: Dp = 1000.dp
    public const val TransferredAlpha: Float = 0.45f
    public const val MaxTintAlpha: Float = 0.55f

    /** Drives the master progress; per-phase easing lives in the choreography, so keep this linear. */
    public val animationSpec: AnimationSpec<Float> = tween(durationMillis = 1100, easing = LinearEasing)
}


@Composable
public fun rememberGooeyTransferState(
    animationSpec: AnimationSpec<Float> = GooeyTransferDefaults.animationSpec,
): GooeyTransferState {
    val scope = rememberCoroutineScope()
    return remember(scope) { GooeyTransferState(scope) }.also { it.animationSpec = animationSpec }
}

/**
 * Coordinates transfers of tokens from [GooeySource]s into a single [GooeyTarget] inside a
 * [GooeyTransferHost]. All bounds are stored in root coordinates and converted to host-local
 * coordinates when the scene is built.
 */
@Stable
public class GooeyTransferState internal constructor(private val scope: CoroutineScope) {

    internal var animationSpec: AnimationSpec<Float> = GooeyTransferDefaults.animationSpec

    internal var hostBounds: Rect by mutableStateOf(Rect.Zero)
    internal var targetBounds: Rect? by mutableStateOf(null)
    internal var targetCornerRadius: Float = 0f
    internal var targetNotchRadius: Float = 0f

    private val sources = mutableMapOf<Any, SourceEntry>()
    internal val items = mutableStateListOf<TransferredItem>()
    internal var drag: DragSession? by mutableStateOf(null)
        private set

    /** Transferred keys, newest first. */
    public val transferredKeys: List<Any> get() = items.map { it.key }

    public val isAnimating: Boolean get() = drag != null || items.any { it.isInFlight }

    public fun isTransferred(key: Any): Boolean = items.any { it.key == key }

    /**
     * Starts moving the token of the source registered with [key] into the target. A token that
     * is on its way back to its source turns around instead.
     */
    public fun transfer(key: Any) {
        items.firstOrNull { it.key == key && it.isReturning }?.let { item ->
            item.isReturning = false
            scope.launch { item.progress.animateTo(1f, animationSpec) }
            return
        }
        if (isTransferred(key) || drag?.source?.key == key) return
        val source = sources[key]?.takeUnless { it.bounds.isEmpty } ?: return
        start(TransferredItem(key, source, startProgress = 0f))
    }

    /**
     * Sends the token for [key] back to its source by playing the transfer in reverse, then
     * removes it. If the source is not composed (e.g. scrolled out of a lazy list) the token is
     * removed immediately, since there is nowhere to animate to.
     */
    public fun returnToSource(key: Any) {
        val item = items.firstOrNull { it.key == key } ?: return
        if (item.isReturning) return
        val source = sources[key]?.takeUnless { it.bounds.isEmpty }
        if (source == null) {
            items.remove(item)
            return
        }
        item.prepareReturn(source)
        scope.launch {
            item.progress.animateTo(0f, animationSpec)
            if (item.isReturning) items.remove(item)
        }
    }

    /** Removes [key] from the target without animation. */
    public fun remove(key: Any) {
        items.removeAll { it.key == key }
    }

    internal fun register(entry: SourceEntry) {
        sources[entry.key] = entry
    }

    internal fun unregister(entry: SourceEntry) {
        if (sources[entry.key] === entry) sources.remove(entry.key)
    }

    private fun start(item: TransferredItem) {
        items.add(0, item)
        scope.launch { item.progress.animateTo(1f, animationSpec) }
    }

    internal fun startDrag(key: Any): Boolean {
        if (drag != null || isTransferred(key)) return false
        val source = sources[key]?.takeUnless { it.bounds.isEmpty } ?: return false
        drag = DragSession(source, source.anchorBounds ?: source.bounds)
        return true
    }

    internal fun dragBy(delta: Offset) {
        val session = drag?.takeUnless { it.released } ?: return
        session.delta += delta
    }

    /** Commits the drag into a transfer when released near or flung towards the target. */
    internal fun endDrag(velocity: Offset, flingThreshold: Float) {
        val session = drag?.takeUnless { it.released } ?: return
        val commit = shouldCommitDrag(
            anchor = session.anchor.center,
            tokenCenter = session.anchor.center + session.delta,
            velocity = velocity,
            target = targetBounds,
            flingThreshold = flingThreshold,
        )
        if (!commit) {
            cancelDrag()
            return
        }
        session.released = true
        start(
            TransferredItem(
                key = session.source.key,
                source = session.source,
                startProgress = TransferTimeline.RISE_START,
                tokenStartOverride = session.tokenRect(),
                withBridge = false,
            ),
        )
        scope.launch {
            session.neck.animateTo(0f, tween(durationMillis = 220))
            if (drag === session) drag = null
        }
    }

    internal fun cancelDrag() {
        val session = drag?.takeUnless { it.released } ?: return
        session.released = true
        scope.launch {
            animate(
                typeConverter = Offset.VectorConverter,
                initialValue = session.delta,
                targetValue = Offset.Zero,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
            ) { value, _ -> session.delta = value }
            if (drag === session) drag = null
        }
    }

    internal fun tokenBounds(item: TransferredItem): Rect {
        val start = item.tokenStart
        val end = item.slotBounds ?: targetBounds?.let { target ->
            val size = min(start.width, start.height)
            Rect(target.center, size / 2f)
        } ?: start
        return tokenRectAt(item.progress.value, start, end)
    }

    internal fun buildScene(smoothness: Float, maxTintAlpha: Float): GooeyScene {
        val origin = hostBounds.topLeft
        val target = targetBounds?.translate(-origin)
        val blobs = ArrayList<Blob>()
        if (target != null) {
            items.forEach { item ->
                if (!item.isInFlight) return@forEach
                blobs += transferBlobs(
                    progress = item.progress.value,
                    color = item.color,
                    source = item.sourceBounds.translate(-origin),
                    sourceCornerRadius = item.sourceCornerRadius,
                    target = target,
                    targetCornerRadius = targetCornerRadius,
                    tokenStart = item.tokenStart.translate(-origin),
                    token = tokenBounds(item).translate(-origin),
                    maxTintAlpha = maxTintAlpha,
                    withBridge = item.withBridge,
                )
            }
        }
        drag?.let { session ->
            val anchor = session.anchor.center - origin
            blobs += dragBlobs(
                color = session.source.color,
                source = session.source.bounds.translate(-origin),
                sourceCornerRadius = session.source.cornerRadius,
                target = target,
                targetCornerRadius = targetCornerRadius,
                anchor = anchor,
                token = if (session.drawsToken) session.tokenRect().translate(-origin) else null,
                tokenCenter = anchor + session.delta,
                tokenRadius = min(session.anchor.width, session.anchor.height) / 2f,
                neckScale = session.neck.value,
                maxTintAlpha = maxTintAlpha,
            )
        }
        val cutouts = if (target != null && targetNotchRadius > 0f && blobs.isNotEmpty()) {
            listOf(Blob.Circle(Offset(target.center.x, target.bottom), targetNotchRadius, Color.Transparent))
        } else {
            emptyList()
        }
        return GooeyScene(blobs, cutouts, smoothness)
    }
}

internal fun shouldCommitDrag(
    anchor: Offset,
    tokenCenter: Offset,
    velocity: Offset,
    target: Rect?,
    flingThreshold: Float,
): Boolean {
    if (target == null) return false
    if (target.contains(tokenCenter)) return true
    val toTarget = target.center - anchor
    val lengthSq = toTarget.getDistanceSquared()
    if (lengthSq == 0f) return true
    val pull = tokenCenter - anchor
    val fraction = (pull.x * toTarget.x + pull.y * toTarget.y) / lengthSq
    val speed = (velocity.x * toTarget.x + velocity.y * toTarget.y) / sqrt(lengthSq)
    return fraction >= 0.5f || (fraction > 0.1f && speed >= flingThreshold)
}

internal class SourceEntry(val key: Any) {
    var bounds: Rect by mutableStateOf(Rect.Zero)
    var anchorBounds: Rect? by mutableStateOf(null)
    var color: Color = Color.Unspecified
    var cornerRadius: Float = 0f
    var token: @Composable () -> Unit = {}
}

/**
 * A token moving into (or resting in) the target. Source bounds are read live so in-flight
 * transfers follow their row while it scrolls; after the row leaves composition the last known
 * bounds are used.
 */
internal class TransferredItem(
    val key: Any,
    source: SourceEntry,
    startProgress: Float,
    tokenStartOverride: Rect? = null,
    withBridge: Boolean = true,
) {
    private var sourceEntry = source
    private var tokenStartOverride: Rect? = tokenStartOverride
    var withBridge: Boolean = withBridge
        private set
    val color: Color = source.color
    val token: @Composable () -> Unit = source.token
    val sourceCornerRadius: Float = source.cornerRadius
    val sourceBounds: Rect get() = sourceEntry.bounds
    val tokenStart: Rect get() = tokenStartOverride ?: sourceEntry.anchorBounds ?: sourceEntry.bounds

    val progress = Animatable(startProgress)
    var slotBounds: Rect? by mutableStateOf(null)
    var isReturning: Boolean by mutableStateOf(false)
    val isInFlight: Boolean get() = progress.value < 1f

    /**
     * Reverse playback must end at the row as it is now: rebind to the currently composed source
     * (the original may have been disposed while scrolled away) and restore the full bridge, even
     * for transfers that started from a drag.
     */
    fun prepareReturn(source: SourceEntry) {
        sourceEntry = source
        tokenStartOverride = null
        withBridge = true
        isReturning = true
    }
}

internal class DragSession(val source: SourceEntry, val anchor: Rect) {
    var delta: Offset by mutableStateOf(Offset.Zero)

    /** True once the finger is up: either handed over to a transfer or springing back. */
    var released: Boolean by mutableStateOf(false)

    /** Neck thickness multiplier; animates to 0 when the neck snaps after a commit. */
    val neck = Animatable(1f)

    /** Whether this session still draws the token itself (dragging or springing back). */
    val drawsToken: Boolean get() = neck.value >= 1f

    fun tokenRect(): Rect = anchor.translate(delta)
}
