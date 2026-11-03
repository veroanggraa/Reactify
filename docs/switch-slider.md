# Switch & Slider

A toggle switch and a slider whose thumb bulges past its track, pulling it into one liquid pill
instead of sitting on top of it. The slider's thumb also pulls a value bubble up out of itself
while dragged, joined by a thinning neck, and reabsorbs it on release.

## Quick start

These snippets are self-contained; paste them into a `@Composable` and they run as-is.

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.control.GooeySwitch

@Composable
fun Toggle() {
    var checked by remember { mutableStateOf(false) }
    GooeySwitch(checked = checked, onCheckedChange = { checked = it }, color = Color(0xFFC8F5D4))
}
```

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.control.GooeySlider

@Composable
fun Volume() {
    var value by remember { mutableFloatStateOf(0.4f) }
    GooeySlider(
        value = value,
        onValueChange = { value = it },
        color = Color(0xFFC8F5D4),
        modifier = Modifier.fillMaxWidth(),
        label = { Text("${(value * 100).toInt()}") },
    )
}
```

## How it works

Both controls share one shape: a `Capsule` track plus a `Circle` thumb slightly larger than the
track's half-height, so the smooth-min reads one liquid pill rather than a circle resting on a
bar. `GooeySlider` adds a value bubble (another `Circle`) that rises off the thumb while a drag is
in progress, joined by a short chain of capsules that thin in the middle like any other gooey
neck, and shrinks back into the thumb on release. `label` is your own content (typically a short
`Text`), centred in the bubble and faded/scaled in with it — the shape and the value it displays
are drawn separately, same as everywhere else in this library.

## Parameters reference

**`GooeySwitch`**

| Parameter | Default | Description |
| --- | --- | --- |
| `checked` | — | Current state. |
| `onCheckedChange` | — | Called on tap. |
| `color` | — | Track and thumb colour. |
| `trackColor` | `color.copy(alpha = 0.3f)` | Plain track drawn under the liquid. |
| `width` / `height` | `52.dp` / `32.dp` | Overall size. |
| `smoothness` | `8.dp` | Blend distance between track and thumb. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

**`GooeySlider`**

| Parameter | Default | Description |
| --- | --- | --- |
| `value` | — | Fraction, clamped to 0..1. |
| `onValueChange` | — | Called continuously while dragging. |
| `color` | — | Track, thumb and bubble colour. |
| `height` | `32.dp` | Track row height; width comes from the modifier. |
| `bubbleRise` | `40.dp` | How far the value bubble rises above the track. |
| `label` | *(none)* | Content centred in the bubble while dragging. |
| `smoothness` | `8.dp` | Blend distance. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

## Tips & troubleshooting

!!! tip "The bubble doesn't appear"
    It only shows while a drag is in progress (`dragged > 0`); tapping without dragging moves the
    thumb but never rises a bubble, same as a plain tap-to-set slider.
