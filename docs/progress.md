# Progress Indicators

Liquid progress bars and rings. The determinate variants have a round head that races ahead when
the value jumps, stretching a thin neck behind it that the fill then catches up with and absorbs;
the indeterminate variants are drops chasing each other, merging and pinching apart as the gap
between them changes.

## Quick start

These snippets are self-contained; paste them into a `@Composable` and they run as-is.

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.progress.GooeyLinearProgress

@Composable
fun Upload() {
    var progress by remember { mutableFloatStateOf(0f) }

    GooeyLinearProgress(
        progress = progress,
        color = Color(0xFFC8F5D4),
        modifier = Modifier.fillMaxWidth(),
    )
    Button(onClick = { progress += 0.25f }) { Text("Next chunk") }
}
```

Leave `progress` out and you get the indeterminate version; the same two shapes exist as rings:

```kotlin
import com.veroanggra.reactify.progress.GooeyCircularProgress

GooeyLinearProgress(color = Color(0xFFC4E4F7), modifier = Modifier.fillMaxWidth())

GooeyCircularProgress(progress = progress, color = Color(0xFFC8F5D4))
GooeyCircularProgress(color = Color(0xFFC4E4F7))
```

There is no state object to create: pass the value in, and every change animates from wherever
the indicator currently is — tapping "Next chunk" twice quickly retargets mid-flight instead of
queueing.

## How it works

1. **Two animations, one value.** The determinate composables animate `progress` twice: a fast
   `headSpec` moves the round head, a slower `fillSpec` moves the fill behind it. While the two
   disagree, a chain of capsules joins them, thinnest in the middle in proportion to how far the
   head has pulled away. When the fill arrives the chain sits inside it and the blend hides it.
2. **One colour per scene.** Everything gooey — fill, neck, head — shares `color`, so all renderers
   merge it the same way. The track is *not* part of the scene: it is a plain rounded rect (or
   ring) in `trackColor` drawn underneath, which is why it never bulges towards the head.
3. **The head fits inside the bounds.** The head is 1.25× as thick as the track, so the track is
   inset: a `height` of 12.dp gives a 9.6.dp track with the head's overhang above and below. Set
   `height` to the total you want to reserve, not the bar thickness.
4. **Indeterminate loops are seamless.** The linear sweep grows in at the start of its cycle and
   shrinks away at the end; the ring's three drops orbit one, two and three times per cycle, so
   the last frame of one cycle is the first frame of the next.
5. **Shader-safe blob counts.** A full ring is sixteen capsules plus up to six neck segments and
   the head, inside the 24-blob limit of the AGSL and SkSL renderers; the CPU renderer has no limit.

## Parameters reference

**`GooeyLinearProgress`** — both overloads share every parameter except `progress`.

| Parameter | Default | Description |
| --- | --- | --- |
| `progress` | — (omit for indeterminate) | Fraction done, clamped to 0..1. |
| `color` | — | Fill and head colour. |
| `modifier` | `Modifier` | Width comes from here (`fillMaxWidth()`, `width(…)`); without a constraint the bar is 240.dp wide. |
| `trackColor` | `color.copy(alpha = 0.15f)` | Plain track drawn under the liquid. Pass `Color.Transparent` for none. |
| `height` | `12.dp` | Total height, head included. |
| `smoothness` | `10.dp` | Blend distance between head, neck and fill. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

**`GooeyCircularProgress`**

| Parameter | Default | Description |
| --- | --- | --- |
| `progress` | — (omit for indeterminate) | Fraction of a turn, clockwise from 12 o'clock. |
| `color` | — | Arc and head colour. |
| `size` | `48.dp` | Outer diameter, head included. |
| `strokeWidth` | `6.dp` | Ring thickness; the head is 1.25× as wide. |
| `trackColor` | `color.copy(alpha = 0.15f)` | Plain ring drawn under the liquid. |
| `smoothness` | `10.dp` | Blend distance. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

**`GooeyProgressDefaults`** exposes `headSpec` and `fillSpec` for reference; to change the feel,
drive your own `animateFloatAsState` values and feed the result to `progress` — the head/fill split
still applies on top of whatever curve you supply.

## Tips & troubleshooting

!!! tip "The head is clipped at the ends"
    The indicator draws only inside its own bounds. Keep `height` (or `size`) as the *total* you
    want, rather than shrinking it to the track thickness and letting the head hang out.

!!! tip "No neck appears when the value changes"
    Small steps (a few percent) keep head and fill close enough that `smoothness` blends them
    into one drop. The stretch is proportional to the jump — try 25% steps, or lower `smoothness`.

!!! tip "The bar is 240.dp wide"
    That is the fallback width; pass `Modifier.fillMaxWidth()` or an explicit `width(…)`.
