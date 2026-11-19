# FAB Speed-dial

A FAB that spawns its child actions up a vertical column, one after another, each still joined to
the main button by a neck that thins away as it settles into place — the same staggered,
budding choreography as [Transfer](transfer.md), applied to several children instead of one token.

## Quick start

```kotlin
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.fab.GooeyFabMenu
import com.veroanggra.reactify.fab.rememberGooeyFabState

@Composable
fun SpeedDial() {
    val state = rememberGooeyFabState()
    val labels = listOf("A", "B", "C")

    GooeyFabMenu(
        state = state,
        itemCount = labels.size,
        color = Color(0xFFC8F5D4),
        mainContent = { Text(if (state.expanded) "×" else "+") },
        itemContent = { index, _ -> Text(labels[index]) },
    )
}
```

## How it works

Each child has its own stagger window within the shared 0..1 timeline (`fabChildLocalProgress`):
earlier children start sooner and finish first, their windows overlapping just enough for a smooth
cascade rather than a rigid one-at-a-time queue. A child still joined by its neck fades that neck
away (`1 - localProgress`) as it settles into its resting spot, `spacing` further out than the last.
`itemContent`'s own box is already faded and scaled by that same local progress, so your content
and the blob agree on timing without you tracking it separately.

## Parameters reference

| Parameter | Default | Description |
| --- | --- | --- |
| `itemCount` | — | Number of children. |
| `color` | — | Main button and children colour. |
| `mainRadius` / `childRadius` | `28.dp` / `20.dp` | Button sizes. |
| `spacing` | `64.dp` | Distance between consecutive resting spots. |
| `mainContent` | *(none)* | Content centred in the main button. |
| `itemContent` | *(none)* | `(index, localProgress) -> Unit`, centred in each child. |
| `smoothness` | `10.dp` | Blend distance between the main button, necks and children. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

**`GooeyFabState`** — `expanded: Boolean`, `toggle()`, `expand()`, `collapse()`.
