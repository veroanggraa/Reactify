# Chip Group

Multi-select chips whose selected background bridges automatically when two selected chips sit
close together — no special bridge shape needed, just the same smooth-min every other gooey shape
uses, applied to chips that happen to be near each other.

## Quick start

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import com.veroanggra.reactify.chip.GooeyChipGroupHost
import com.veroanggra.reactify.chip.gooeyChip
import com.veroanggra.reactify.chip.rememberGooeyChipGroupState

@Composable
fun Tags(labels: List<String>) {
    val state = rememberGooeyChipGroupState()
    GooeyChipGroupHost(state = state, color = Color(0xFFC8F5D4)) {
        Row {
            labels.forEachIndexed { index, label ->
                Text(
                    label,
                    modifier = Modifier
                        .gooeyChip(state, index)
                        .clickable { state.toggle(index) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}
```

## How it works

Each selected chip draws its own `RoundRect` blob at its registered bounds; that's it — there is no
extra "bridge" blob. Two selected chips sitting closer together than the host's `smoothness` merge
into one liquid shape purely because the smooth-min already pulls any two same-colour blobs that
close together; chips further apart stay visually separate. Keep `smoothness` smaller than the gap
between *unselected* neighbours in your layout, or distant chips would bridge too.

## Parameters reference

| Parameter | Default | Description |
| --- | --- | --- |
| `color` | — | Selected-chip background colour. |
| `cornerRadius` | `16.dp` | Corner radius of each selected chip. |
| `smoothness` | `10.dp` | Merge distance; only chips closer than this bridge. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

**`GooeyChipGroupState`** — `selected: Set<Int>`, `isSelected(index)`, `toggle(index)`.

## Tips & troubleshooting

!!! tip "Two selected chips aren't bridging"
    Check the actual gap between them against `smoothness` — a `FlowRow`/`Row` with generous
    spacing may put them further apart than the blend distance.
