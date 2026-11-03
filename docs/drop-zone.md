# Drop Zone

A drop target that reaches a short gooey tendril towards a drag point as it gets close, and bulges
slightly once that point is inside its bounds — without the drag having to come from a registered
[Transfer](transfer.md) source. Drive it from your own gesture handling.

## Quick start

```kotlin
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.veroanggra.reactify.list.GooeyDropZone
import com.veroanggra.reactify.list.rememberGooeyDropZoneState

@Composable
fun Trash() {
    val state = rememberGooeyDropZoneState()
    var tokenRoot by remember { mutableStateOf(Offset.Zero) }
    var tokenOffset by remember { mutableStateOf(Offset.Zero) }

    GooeyDropZone(state = state, color = Color(0xFFC4E4F7)) {
        Box(
            Modifier
                .offset { IntOffset(tokenOffset.x.roundToInt(), tokenOffset.y.roundToInt()) }
                .onGloballyPositioned { tokenRoot = it.positionInRoot() - tokenOffset }
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = { tokenOffset = Offset.Zero; state.updateDrag(null) }) { change, delta ->
                        change.consume()
                        tokenOffset += delta
                        state.updateDrag(tokenRoot + tokenOffset)
                    }
                },
        ) { /* your draggable item */ }
    }
}
```

## How it works

The zone itself is one `RoundRect` blob, scaled up slightly while `isHovering` is true. While a drag
point reported through `updateDrag` is close but not yet inside the zone, a `Capsule` reaches from
the nearest point on the zone's edge towards it — the same "being pulled towards its target" read
as a dragged transfer token, minus the requirement that the drag come from a registered source.
`updateDrag` takes **root coordinates**; pass `null` once the drag ends.

## Parameters reference

| Parameter | Default | Description |
| --- | --- | --- |
| `color` | — | Zone and tendril colour. |
| `cornerRadius` | `16.dp` | Zone corner radius. |
| `pullRadius` | `56.dp` | How far out the tendril reaches. |
| `smoothness` | `16.dp` | Blend distance between the zone and its tendril. |
| `renderer` | `GooeyRenderers.auto()` | See [Choosing a Renderer](renderers.md). |

**`GooeyDropZoneState`** — `isHovering: Boolean`, `updateDrag(point: Offset?)`.
