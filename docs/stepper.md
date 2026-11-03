# Stepper

A liquid chain of dots that grows link by link as the user moves through a flow: a thread stretches
from the current dot to the next one, thins in the middle like a drop being drawn out, then the new
dot swells out of its tip. Going back plays the same picture in reverse. It wraps any row (or column)
of step markers — you add one `Modifier` per marker and one state object.

## Quick start

This snippet is self-contained; paste it into a `@Composable` screen and it runs as-is.

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.veroanggra.reactify.stepper.GooeyStepperHost
import com.veroanggra.reactify.stepper.gooeyStep
import com.veroanggra.reactify.stepper.rememberGooeyStepperState

private val steps = listOf("Cart", "Address", "Payment", "Done")

@Composable
fun MyStepper() {
    val state = rememberGooeyStepperState(currentStep = 0)

    GooeyStepperHost(state = state, color = Color(0xFFFFD6A5), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            steps.forEachIndexed { index, _ ->
                Box(
                    Modifier
                        .size(28.dp)
                        .gooeyStep(state, index)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape),
                )
            }
        }
    }

    Button(onClick = { state.next() }) { Text("Next") }
    Button(onClick = { state.previous() }) { Text("Previous") }
}
```

Three moving parts, always used together:

1. `rememberGooeyStepperState(currentStep)` — one state object per stepper.
2. `Modifier.gooeyStep(state, index)` — on every step marker, so the state knows where it sits.
3. `GooeyStepperHost(state, color) { ... }` — wraps the markers and draws the chain behind them.

The host draws dots and links *behind* your content, so the markers themselves stay yours: a
number, an icon, a check mark — anything. Give them a translucent or transparent background so the
dot shows through.

## How the state works

1. **`rememberGooeyStepperState`** creates a `GooeyStepperState` and remembers it across
   recompositions. It never advances on its own — only `next()`, `previous()` and `goTo(index)` do.
2. Each marker's **`Modifier.gooeyStep(state, index)`** reports its on-screen bounds to the state the
   moment it's laid out (via `onGloballyPositioned`). Indices must run contiguously from 0; the dot
   radius defaults to half the smallest marker.
3. Nothing is drawn until the marker at the *current* step has reported its bounds — expect one
   invisible frame on first composition.
4. `goTo(index)` (which `next()`/`previous()` call) sets `currentStep` immediately and starts the
   transition. Jumping several steps grows each link in order, one after the other.
5. A tap during a transition retargets from the step the chain has visibly reached, so rapid taps
   feel fluid instead of queued.
6. `GooeyStepperHost` reads the live progress every frame, builds the blobs and hands them to the
   renderer on a `Canvas` placed behind `content`.

## Customizing the look

`color` is a plain `Color` read every frame, so you can pass a theme color, a per-step color, or an
`animateColorAsState` value. `dotRadius` sets the completed-dot size explicitly (by default it is
derived from the marker size); `smoothness` controls how far the gooey blend reaches between the
thread and the dots.

```kotlin
GooeyStepperHost(
    state = state,
    color = MaterialTheme.colorScheme.primary,
    dotRadius = 14.dp,
    smoothness = 18.dp,
) { /* ... */ }
```

## Parameters reference

**`rememberGooeyStepperState`**

| Parameter | Default | Description |
| --- | --- | --- |
| `currentStep` | — | Step shown the first time this state is created. Changing it later has no effect — use `goTo(index)`. |
| `animationSpec` | `tween(450ms, LinearEasing)` | Drives the transition progress (0→1) for every step change. |

**`GooeyStepperState`**

| Member | Description |
| --- | --- |
| `currentStep` | Index of the current step; updates as soon as a transition starts. |
| `stepCount` | Number of markers that have reported their bounds. |
| `goTo(index)` | Animates to `index`; ignored if that marker hasn't reported bounds. |
| `next()` / `previous()` | `goTo(currentStep ± 1)`. |

**`GooeyStepperHost`**

| Parameter | Default | Description |
| --- | --- | --- |
| `state` | — | The `GooeyStepperState` created above. |
| `color` | — | Fill color of the dots, links and thread. |
| `dotRadius` | `null` | Radius of a completed dot. `null` uses half the smallest side of the step markers. |
| `smoothness` | `14.dp` | How far the gooey blend reaches between dot and thread. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

**`Modifier.gooeyStep(state, index)`** — apply to each step marker; indices contiguous from 0.

## Tips & troubleshooting

!!! tip "Nothing is drawn"
    Every marker needs `Modifier.gooeyStep(state, index)`, and the index you pass as `currentStep`
    must exist. The chain stays invisible until that marker has reported its bounds.

!!! tip "The dots look clipped"
    The host only draws inside its own bounds. If markers sit flush against the edge, add padding
    to the host so the dot and the gooey blend have room.

!!! tip "Vertical steppers"
    Lay the markers out in a `Column` instead of a `Row` — links follow the marker centers, so
    orientation doesn't matter.
