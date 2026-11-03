package com.veroanggra.reactify.stepper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.gooey.Blob
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.gooey.GooeyScene
import com.veroanggra.reactify.list.rootBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

/** Defaults shared by [rememberGooeyStepperState] and [GooeyStepperHost]. */
public object GooeyStepperDefaults {
    /** Drives one transition between steps; the grow/shrink shape itself lives in [stepperBlobs]. */
    public val animationSpec: AnimationSpec<Float> = tween(durationMillis = 450, easing = LinearEasing)

    /** Blend distance for merging the dots, links and growing thread into one liquid chain. */
    public val Smoothness: Dp = 14.dp
}

/**
 * Creates and remembers a [GooeyStepperState] starting at [currentStep].
 *
 * @param animationSpec drives the 0→1 progress of every transition started by [GooeyStepperState.goTo].
 */
@Composable
public fun rememberGooeyStepperState(
    currentStep: Int,
    animationSpec: AnimationSpec<Float> = GooeyStepperDefaults.animationSpec,
): GooeyStepperState {
    val scope = rememberCoroutineScope()
    return remember(scope) { GooeyStepperState(currentStep, scope) }.also { it.animationSpec = animationSpec }
}

/**
 * Tracks which step inside a [GooeyStepperHost] is current and animates a liquid chain of dots and
 * links growing (or retracting) between the registered step bounds. Call [next], [previous] or
 * [goTo] from your own buttons; the host never changes the step on its own.
 */
@Stable
public class GooeyStepperState internal constructor(initialStep: Int, private val scope: CoroutineScope) {
    internal var animationSpec: AnimationSpec<Float> = GooeyStepperDefaults.animationSpec
    internal var hostBounds: Rect by mutableStateOf(Rect.Zero)

    /** Index of the current step; changes immediately when a transition starts. */
    public var currentStep: Int by mutableStateOf(initialStep)
        private set

    private val stepBounds = mutableStateMapOf<Int, Rect>()
    private val progress = Animatable(1f)
    private var from: Int by mutableStateOf(initialStep)
    private var to: Int by mutableStateOf(initialStep)

    internal val hasSteps: Boolean get() = stepBounds.containsKey(currentStep)

    /** Number of steps that have reported their bounds through [gooeyStep]. */
    public val stepCount: Int get() = stepBounds.size

    /**
     * Moves to [index], which must already have reported its bounds. A transition still in flight
     * retargets from the step the chain has visibly reached, so rapid taps stay fluid.
     */
    public fun goTo(index: Int) {
        if (index == currentStep || !stepBounds.containsKey(index)) return
        from = if (progress.value >= 1f) to else (from + (to - from) * progress.value).roundToInt()
        to = index
        currentStep = index
        scope.launch {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec)
        }
    }

    /** Moves one step forward, if that step has reported its bounds. */
    public fun next(): Unit = goTo(currentStep + 1)

    /** Moves one step back, if that step has reported its bounds. */
    public fun previous(): Unit = goTo(currentStep - 1)

    internal fun registerBounds(index: Int, bounds: Rect) {
        stepBounds[index] = bounds
    }

    internal fun blobs(color: Color, dotRadius: Float?, origin: Offset): List<Blob> {
        val steps = (0 until stepBounds.size).map { stepBounds[it] ?: return emptyList() }
        val radius = dotRadius ?: (steps.minOf { min(it.width, it.height) } / 2f)
        return stepperBlobs(
            progress.value,
            from,
            to,
            steps.map { it.translate(-origin) },
            radius,
            color,
        )
    }
}

/**
 * Draws a gooey chain of dots behind [content], growing link by link between whichever steps call
 * [gooeyStep] as [state] moves. Wrap your row (or column) of step markers with this; the markers
 * report their own bounds and your buttons drive [GooeyStepperState.next]/[GooeyStepperState.previous].
 *
 * @param dotRadius radius of each completed dot; null uses half the smallest side of the step markers.
 */
@Composable
public fun GooeyStepperHost(
    state: GooeyStepperState,
    color: Color,
    modifier: Modifier = Modifier,
    dotRadius: Dp? = null,
    smoothness: Dp = GooeyStepperDefaults.Smoothness,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val dotRadiusPx = dotRadius?.let { with(density) { it.toPx() } }
    val smoothnessPx = with(density) { smoothness.toPx() }
    Box(modifier.onGloballyPositioned { state.hostBounds = it.rootBounds() }) {
        Canvas(Modifier.matchParentSize()) {
            if (!state.hasSteps) return@Canvas
            val origin = state.hostBounds.topLeft
            val scene = GooeyScene(
                blobs = state.blobs(color, dotRadiusPx, origin),
                smoothness = smoothnessPx,
            )
            if (scene.blobs.isNotEmpty()) with(renderer) { draw(scene) }
        }
        content()
    }
}

/** Reports this step marker's bounds to [state] at [index]; indices must be contiguous from 0. */
public fun Modifier.gooeyStep(state: GooeyStepperState, index: Int): Modifier =
    onGloballyPositioned { state.registerBounds(index, it.rootBounds()) }
